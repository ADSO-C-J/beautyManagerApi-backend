# Despliegue en producción — BeautyManager API

> Documento de referencia del proceso de containerización y despliegue de
> `beautyManagerApi-backend` en **Render**, conectado a **Neon (PostgreSQL)**.
>
> **Servicio en producción:** https://beautymanagerapi-backend.onrender.com
> **Commit del despliegue:** `98a02e8`
> **Fecha:** 2026-10-04

---

## Índice

1. [Arquitectura de la solución](#1-arquitectura-de-la-solución)
2. [Conceptos previos](#2-conceptos-previos)
3. [Los tres entornos y cómo se eligen](#3-los-tres-entornos-y-cómo-se-eligen)
4. [Ejecución local](#4-ejecución-local)
5. [Ejecutar localmente contra producción (Neon)](#5-ejecutar-localmente-contra-producción-neon)
6. [Problemas encontrados y su causa raíz](#6-problemas-encontrados-y-su-causa-raíz)
7. [Flujo de actualización a producción](#7-flujo-de-actualización-a-producción)
8. [Modelo de seguridad](#8-modelo-de-seguridad)
9. [Diagnóstico de fallos](#9-diagnóstico-de-fallos)
10. [Variables de entorno](#10-variables-de-entorno)
11. [Checklist](#11-checklist)

---

## 1. Arquitectura de la solución

```
┌─────────────────────────────────────────────────────────────┐
│                    DESARROLLO LOCAL                          │
│   IntelliJ ──► JVM local ──► localhost:5432/5433 (Postgres) │
└─────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│                    DOCKER LOCAL (compose)                   │
│   bm-api      ──► contenedor JRE    ──► db:5432             │
│   bm-postgres ──► postgres:18-alpine ◄── volumen pgdata     │
│   (expuesto en el host como 127.0.0.1:5434)                 │
└─────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│                    PRODUCCIÓN (Render)                       │
│   HTTPS ──► proxy Render ──► contenedor (Dockerfile)        │
│                               puerto $PORT = 10000          │
│                                        │                    │
│                                        ▼                    │
│                          Neon pooler (PostgreSQL 18.6)      │
└─────────────────────────────────────────────────────────────┘
```

| Componente | Tecnología | Función |
|---|---|---|
| Runtime | Java 21 (JRE) | Ejecuta la aplicación |
| Framework | Spring Boot 4.1.0 | Web, inyección de dependencias, JPA |
| Base de datos | Neon PostgreSQL 18.6 | Persistencia (vía pooler) |
| Migraciones | Flyway 12.4.0 | Versionado del esquema |
| Build | Maven 3.9 | Empaquetado a `.jar` |
| Contenedor | Docker | Empaquetado reproducible |
| Plataforma | Render | Orquestación, TLS y despliegue |

---

## 2. Conceptos previos

### 2.1 Imagen vs. contenedor

- **Imagen**: plantilla inmutable con el sistema de archivos completo (runtime,
  dependencias, aplicación). Se crea con `docker build`.
- **Contenedor**: instancia en ejecución de una imagen.

Un contenedor **no es una máquina virtual**: no emula hardware ni tiene kernel
propio; comparte el del anfitrión. Por eso arranca en segundos.

### 2.2 Por qué un Dockerfile multi-stage

```dockerfile
FROM maven:3.9-eclipse-temurin-21 AS build     # Etapa 1: compila
RUN mvn -B -q clean package -DskipTests

FROM eclipse-temurin:21-jre-jammy AS runtime   # Etapa 2: solo el JRE
COPY --from=build /build/target/*.jar app.jar
```

La etapa 1 contiene Maven, el JDK, el código fuente y el caché de descargas:
nada de eso hace falta para *ejecutar* la app. La etapa 2 copia solo el `.jar`.

Resultado: **513 MB** en vez de >1 GB, y sin toolchain de compilación en
producción (menor superficie de ataque).

### 2.3 Capas y caché

Cada instrucción genera una capa; Docker reutiliza las que no cambiaron:

```dockerfile
COPY pom.xml ./                    # capa 1
RUN mvn dependency:go-offline      # capa 2 (dependencias)
COPY src ./src                     # capa 3 (código)
RUN mvn clean package              # capa 4 (compila)
```

Si solo cambias un `.java`, las capas 1-2 se reutilizan. Por eso el orden
importa: **primero el `pom.xml`, después el código**.

### 2.4 Variables de entorno y placeholders

| Sintaxis | Comportamiento |
|---|---|
| `${VARIABLE}` | Falla si no está definida |
| `${VARIABLE:valor}` | Usa `valor` si no está definida |
| `${VARIABLE:?mensaje}` | Falla con `mensaje` (solo Compose) |

La primera forma es una **decisión de seguridad deliberada** (véase §8).

### 2.5 El puerto en plataformas PaaS

Las plataformas PaaS no siempre usan el 8080. Render inyecta el puerto asignado
en `PORT` (aquí, **10000**). Si la app escucha en otro puerto, el proxy no la
encuentra. De ahí:

```properties
server.port=${PORT:8080}
```

---

## 3. Los tres entornos y cómo se eligen

La base de datos se selecciona por el **perfil de Spring**, no editando URLs.

| Cómo arrancas | Perfil | Base de datos | Variables |
|---|---|---|---|
| `./mvnw spring-boot:run` | *(ninguno)* | **Neon** | `DB_*` |
| `... -Dspring-boot.run.profiles=local` | `local` | **PostgreSQL local** | `LOCAL_DB_*` |
| `docker compose up -d` | `local` | **Postgres del contenedor** | `LOCAL_DB_*` |
| Render (producción) | *(ninguno)* | **Neon** | `DB_*` |

### Mecanismo de resolución

`application.properties` (todos los entornos):

```properties
spring.datasource.url      = ${DB_URL:jdbc:postgresql://localhost:5432/beautymanager}
spring.datasource.username = ${DB_USERNAME:postgres}
spring.datasource.password = ${DB_PASSWORD:postgres}
```

`application-local.properties` (solo con perfil `local`):

```properties
spring.datasource.url      = ${LOCAL_DB_URL:jdbc:postgresql://localhost:5432/beautymanager}
spring.datasource.username = ${LOCAL_DB_USERNAME:postgres}
spring.datasource.password = ${LOCAL_DB_PASSWORD:postgres}
```

El perfil `local` **sobrescribe** el archivo base; por eso cada perfil lee
variables distintas.

### Precedencia

```
1. Variables de entorno del sistema (Render, IntelliJ, export)
2. application-<perfil>.properties
3. application.properties
4. Defaults dentro de ${VARIABLE:default}
5. spring.config.import → archivo .env
```

> ⚠️ Las variables de entorno **ganan** al `.env`. Si defines `DB_URL` en la run
> configuration de IntelliJ, el `.env` se ignora para esa clave.

### Carga del `.env`

Spring Boot **no** lee `.env` de forma nativa; se carga por configuración:

```properties
spring.config.import=optional:file:.env[.properties]
```

- `[.properties]` es **obligatorio**: indica usar punto como separador. Sin él,
  `DB_URL` se interpretaría como `db.url`.
- `optional:` evita fallar si el archivo no existe (CI, producción).
- Es **relativo al directorio de trabajo** (la raíz del proyecto).

---

## 4. Ejecución local

### 4.1 Opción A — Maven + IntelliJ (desarrollo rápido)

```bash
# 1. Verifica que el 8080 esté libre
lsof -i :8080 -sTCP:LISTEN

# 2. Arranca con perfil local (PostgreSQL de tu máquina)
./mvnw spring-boot:run -Dspring-boot.run.profiles=local

# O desde IntelliJ: Run > Edit Configurations > Active profiles: local
```

**Requisitos**: JDK 21+, PostgreSQL local, y un `.env` con las variables.

> ⚠️ **Conflicto de puertos:** si el contenedor está levantado, ocupa el 8080 y
> Spring falla con `Port 8080 was already in use`. Solución:
> `docker compose stop api`. IntelliJ y Docker **no pueden coexistir** en el 8080.

### 4.2 Opción B — Docker Compose (entorno completo)

```bash
docker compose up -d --build     # compila y levanta ambos servicios
docker compose logs -f api        # sigue el arranque
docker compose ps                 # estado
```

| Servicio | URL en el host |
|---|---|
| API | `http://localhost:8080` |
| PostgreSQL | `127.0.0.1:5434` |

```bash
docker compose restart api          # reiniciar la API
docker compose down                 # parar (conserva datos)
docker compose down -v              # parar y BORRAR la base
docker compose exec db psql -U bmanager -d beautymanager   # consola SQL
```

> Tras cambiar código Java, `docker compose restart api` **no** recoge el
> cambio: la imagen contiene el `.jar` compilado. Usa
> `docker compose up -d --build api`.

### 4.3 Datos de prueba

```bash
# 1. Crear el negocio (la base del contenedor arranca vacía)
docker compose exec -T db psql -U bmanager -d beautymanager -c \
  "INSERT INTO public.businesses (id, name, city, currency)
   VALUES ('d0000000-0000-0000-0000-000000000001','BeautyManager Docker','CDMX','MXN')
   ON CONFLICT (id) DO NOTHING;"

# 2. Cargar el seed
docker compose exec -T db psql -U bmanager -d beautymanager \
  -v biz_id=d0000000-0000-0000-0000-000000000001 \
  < src/main/resources/db/seed/datos_prueba_neon.sql
```

Usuarios creados (contraseña `Password123`):

| Email | Rol |
|---|---|
| `prueba.estilista@beautymanager.com` | estilista |
| `prueba.estilista2@beautymanager.com` | estilista |
| `prueba.recepcion@beautymanager.com` | recepcionista |
| `prueba.cliente@beautymanager.com` | cliente |

---

## 5. Ejecutar localmente contra producción (Neon)

Ejecutar la app en tu máquina **contra la base real**, para reproducir fallos de
usuarios sin esperar un redeploy:

```bash
# Sin perfil local -> lee DB_* del .env -> Neon
./mvnw spring-boot:run
```

En el log debe aparecer:

```
Database JDBC URL [jdbc:postgresql://ep-...neon.tech:5432/BEAUTYMANAGER]
```

### ⚠️ Advertencia: estás sobre la base real

Lo que escribas (crear clientes, agendar citas, borrar) **afecta producción**.
No hay rollback.

| Seed | Comportamiento | ¿Cuándo? |
|---|---|---|
| `datos_prueba.sql` | `TRUNCATE ... CASCADE` (**destructivo**) | Base vacía, empezar de cero |
| `datos_prueba_neon.sql` | Solo `INSERT`, idempotente | Hay datos que no quieres perder |

Existe un respaldo en el esquema `bm_backup` (creado antes de la consolidación de
negocios) por si hubiera que revertir.

### Alternativa más segura

Si solo necesitas depurar, levanta Docker con su propia base aislada:

```bash
docker compose up -d
```

Tarda un poco más, pero **no toca producción**.

---

## 6. Problemas encontrados y su causa raíz

Cada fallo se documenta con su **causa real**, porque el mensaje en pantalla
suele apuntar a otra cosa.

### 6.1 `Port 8080 was already in use`

**Causa real:** el contenedor `bm-api` de Docker estaba levantado ocupando el
puerto.

**Diagnóstico:**

```bash
lsof -i :8080 -sTCP:LISTEN -P -n
# com.docke  10297 ... TCP *:8080 (LISTEN)   ← el proxy de Docker
```

**Solución:** `docker compose stop api`.

> La lección: *"already in use"* nunca significa un error de código. Significa
> que hay otro proceso. Siempre `lsof` primero.

### 6.2 `role "neondb_owner" does not exist`

**Causa real:** la app usaba el **usuario** de Neon pero el **host** local. La
run configuration de IntelliJ tenía:

```xml
<env name="DB_URL" value="jdbc:postgresql://localhost:5432/beautymanager DB_USERNAME=rome DB_PASSWORD=admin" />
```

Al pegar varias variables en el diálogo de IntelliJ, **todas acabaron dentro
del valor de `DB_URL`** como un único string. `DB_USERNAME` y `DB_PASSWORD`
nunca se crearon. La config era imposible: host local + usuario de Neon.

Además, la variable de entorno **ganaba al `.env`**, así que el `.env` correcto
nunca se aplicaba.

**Solución:** eliminar el bloque `<envs>` y dejar que el `.env` resuelva todo.

### 6.3 Contenedor PostgreSQL en estado `unhealthy`

**Causa real:** **PostgreSQL 18 movió la ruta de datos** de
`/var/lib/postgresql/data` a `/var/lib/postgresql`. El compose montaba el volumen
en la ruta antigua y la imagen oficial lo detectaba como obsoleto:

```
Counter to that, there appears to be PostgreSQL data in:
  /var/lib/postgresql/data (unused mount/volume)
```

**Solución:**

```yaml
volumes:
  - pgdata:/var/lib/postgresql     # no /var/lib/postgresql/data
```

### 6.4 Puerto 5433 también ocupado

**Causa real:** la máquina tenía **dos** PostgreSQL locales: uno en 5432 y otro
en 5433 (este último invisible para `lsof`, porque escucha en `*:5433` con socket
en `/tmp/.s.PGSQL.5433`).

**Solución:** usar el 5434 y parametrizarlo:

```yaml
ports:
  - "127.0.0.1:${POSTGRES_HOST_PORT:-5434}:5432"
```

> La app sigue conectando a `db:5432` dentro de la red de Docker: el puerto del
> host **no le afecta**.

### 6.5 `role "rome" does not exist` al migrar

**Causa real:** `V1__esquema_inicial.sql` se generó con `pg_dump` en una máquina
local y contiene 35 sentencias `ALTER ... OWNER TO rome`. Ese rol no existe en
el contenedor.

**Por qué NO se modificó la migración:** ya estaba aplicada en Neon y en la base
local. Editarla cambiaría su *checksum* y Flyway rechazaría el arranque con
`Migration checksum mismatch`.

**Solución:** crear el rol en el contenedor sin tocar la migración:

```sql
-- docker/initdb/01-crear-rol-rome.sql
DO $$
BEGIN
    IF NOT EXISTS (SELECT FROM pg_catalog.pg_roles WHERE rolname = 'rome') THEN
        CREATE ROLE rome WITH LOGIN CREATEDB;
    END IF;
END
$$;
```

Montado en `/docker-entrypoint-initdb.d`, que PostgreSQL ejecuta **solo cuando
el volumen está vacío**.

### 6.6 `/actuator/health` devolvía 401

**Causa real (doble):**

1. `spring-boot-starter-actuator` **no estaba en el `pom.xml`**. El endpoint
   devolvía 404 y Spring reenviaba a `/error`, que sí está protegido → 401.
2. `/actuator/health` no estaba en la lista de rutas públicas.

**Solución:** añadir la dependencia y abrir solo ese endpoint:

```java
.requestMatchers("/actuator/health", "/actuator/health/**").permitAll()
```

`/actuator/env` y `/actuator/metrics` **siguen requiriendo token**: exponen
información sensible.

### 6.7 `Could not resolve placeholder 'JWT_SECRET'`

**Causa real:** faltaba la variable en el panel de Render.

**No era un bug**, sino el comportamiento correcto: la configuración es

```properties
app.jwt.secret=${JWT_SECRET}      # sin valor por defecto, a propósito
```

Si hubiera un default, el deploy habría funcionado firmando con una clave
conocida y públicamente visible. Que falle es lo seguro (véase §9).

**Solución:** definir `JWT_SECRET` en Render → Environment.

### 6.8 Documentación que mentía: HS256 vs. HS512

El código y los comentarios afirmaban HS256, pero los tokens emitidos tenían
`"alg":"HS512"`.

**Causa real:** el código llama a `signWith(signingKey)` **sin fijar el
algoritmo**, así que JJWT lo elige según el tamaño de la clave. Una clave de 64
hex = 256 bits, suficiente para HS512.

**Solución:** se corrigieron **los comentarios**, no la lógica. HS512 es más
seguro que HS256; lo peligroso era que la documentación inducía a recortar la
clave "a 32 bytes para HS256" y romper la aplicación.

### 6.9 Seed acoplado a Neon

El seed fallaba en bases nuevas con errores de clave foránea.

**Causa real:** referenciaba `a0000000-...` (el admin que existe en Neon) y
servicios con UUIDs que solo existen allí.

**Solución:** hacerlo autocontenido: crea sus propios servicios y apunta a los
usuarios que él mismo genera. Sigue siendo **aditivo e idempotente**
(`ON CONFLICT DO NOTHING`).

---

## 7. Flujo de actualización a producción

### 7.1 Flujo completo

```
 ┌─ 1. develops ──────────────────────────────┐
 │  Editas código en IntelliJ                 │
 │  ./mvnw spring-boot:run -D...profiles=local │
 │  Pruebas en local (PostgreSQL)             │
 └───────────────┬───────────────────────────┘
                 │ git add / commit
                 ▼
 ┌─ 2. review ────────────────────────────────┐
 │  git push origin <rama>                    │
 │  Pull Request  →  revisión del equipo     │
 └───────────────┬───────────────────────────┘
                 │ merge a main
                 ▼
 ┌─ 3. Render detecta el push ───────────────┐
 │  Rebuild de la imagen Docker               │
 │  (~2 min, la mayoría de capas en caché)    │
 │  Arranque del contenedor                   │
 │  Flyway aplica migraciones nuevas         │
 └───────────────┬───────────────────────────┘
                 │
                 ▼
 ┌─ 4. Verificación ─────────────────────────┐
 │  curl .../actuator/health                 │
 │  Login + endpoints clave                  │
 └───────────────────────────────────────────┘
```

### 7.2 Comandos concretos

```bash
# 1. Verifica que todo compila y las pruebas pasan
./mvnw clean test

# 2. Commit con mensaje descriptivo
git add -A
git commit -m "feat: agrega endpoint de reportes"

# 3. Sube a una rama de trabajo
git push -u origin feat/reportes

# 4. Abre el Pull Request en GitHub → se revisa → merge a main

# 5. Render hace el redeploy automáticamente
#    (no hay que hacer nada en el panel)
```

### 7.3 Actualizaciones de configuración (sin tocar código)

Si solo cambian variables (p.ej. subir el TTL del token):

Render → tu servicio → **Environment** → editar → **Save**.

Render dispara un **redeploy automático**. No hace falta commit ni push.

### 7.4 Rollback

Render guarda el historial de deploys. Para volver a una versión anterior:
**Rollback → Deploy** sobre el deploy correspondiente.

En Git, revertir un commit concreto:

```bash
git revert <hash>
git push origin main          # Render redespliega
```

### 7.5 Rollback del secreto JWT

Si consideras que el `JWT_SECRET` sefiltró, cámbialo en Render. Ten en cuenta:
**los JWT no tienen estado**, así que rotar el secreto invalida todos los tokens
emitidos y **todos los usuarios pierden la sesión**; tendrán que hacer login de
nuevo.

### 7.6 Migraciones de base de datos

Flyway aplica automáticamente las migraciones nuevas al arrancar, y solo una vez
gracias a `flyway_schema_history`.

**Reglas para escribir migraciones:**

1. Nomenclatura estricta: `V<n>__<descripcion>.sql`
2. **Nunca modifiques una migración ya aplicada**: Flyway verifica su checksum
   y rechazará el arranque si cambió (`Migration checksum mismatch`)
3. Para "corregir" algo ya aplicado, crea una migración nueva

```bash
# Ejemplo: una nueva migración
docker compose exec -T db psql -U bmanager -d beautymanager \
  -v biz_id=d0000000-0000-0000-0000-000000000001 \
  < src/main/resources/db/seed/datos_prueba_neon.sql

# Ver el estado
docker compose exec db psql -U bmanager -d beautymanager \
  -c "SELECT version, description, success FROM flyway_schema_history ORDER BY installed_rank;"
```

### 7.7 Verificación tras cada despliegue

```bash
U=https://beautymanagerapi-backend.onrender.com

# 1. Salud
curl $U/actuator/health
# esperado: {"groups":["liveness","readiness"],"status":"UP"}

# 2. Login
TOKEN=$(curl -s -X POST $U/api/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"email":"root@beautymanager.com","password":"Admin2026!"}' \
  | python3 -c 'import json,sys; print(json.load(sys.stdin)["token"])')

# 3. Endpoint protegido
curl -s -H "Authorization: Bearer $TOKEN" $U/api/clients
```

En los logs de Render, estas líneas confirman un despliegue correcto:

```
Tomcat started on port 10000                       ← server.port=${PORT:8080} funcionó
Database JDBC URL [...neon.tech...]                ← conecta a Neon, no al contenedor
Schema "public" is up to date / Migrating schema  ← Flyway correcto
Your service is live
```
---

## 8. Modelo de seguridad

### 8.1 Decisiones aplicadas

| Decisión | Motivo |
|---|---|
| `.env` en `.gitignore` | Contiene contraseñas reales |
| `.env` excluido del contexto Docker | Los secretos no entran en la imagen |
| `JWT_SECRET` sin valor por defecto | Evita firmar con una clave conocida |
| Compose aborta si falta `JWT_SECRET` | Falla ruidosamente, no en silencio |
| Contenedor como usuario no-root | Reduce el daño si se compromete |
| PostgreSQL ligado a `127.0.0.1` | No expone la base a la red local |
| Solo `/actuator/health` público | `/env` y `/metrics` exigen token |
| Credenciales en el panel de Render | Nunca en el repositorio |

### 8.2 Verificación de que no hay fugas

```bash
git check-ignore -v .env                        # debe estar ignorado
git grep -n 'npg_' -- $(git ls-files)           # no debe encontrar nada
git grep -n 'JWT_SECRET' -- $(git ls-files)     # solo en .env.example
```

---

## 9. Diagnóstico de fallos

| Síntoma | Causa probable | Verificación |
|---|---|---|
| `Port 8080 was already in use` | Otro proceso en el puerto | `lsof -i :8080 -sTCP:LISTEN` |
| `role "X" does not exist` | Host y usuario no coinciden | Revisar la URL completa |
| `Could not resolve placeholder 'JWT_SECRET'` | Falta la variable en Render | Panel → Environment |
| `/actuator/health` → 401 | Falta la dependencia actuator | Revisar `pom.xml` |
| `Migration checksum mismatch` | Se editó una migración aplicada | Revertir y crear una nueva |
| `address already in use` (puerto) | Otro servicio ocupa el puerto | `lsof` o `netstat` |
| Contenedor `unhealthy` en bucle | Healthcheck o volumen mal configurado | `docker compose logs` |
| `Your service is live` pero no responde | Plan Free dormido | Esperar ~1 min |

### Cómo leer un stack trace de Spring Boot

Las **causas raíz están al final**, en las líneas `Caused by:`. El mensaje
principal (`Web server failed to start...`) es solo el síntoma. En este proyecto
se resolvieron el error de puerto, el de rol inexistente y el de migración
leyendo siempre la última línea `Caused by:`.

---

## 10. Variables de entorno

### En Render (producción) — panel → Environment

| Variable | Valor | Obligatoria |
|---|---|---|
| `DB_URL` | `jdbc:postgresql://ep-...-pooler.<region>.aws.neon.tech:5432/BEAUTYMANAGER?sslmode=require` | Sí |
| `DB_USERNAME` | `neondb_owner` | Sí |
| `DB_PASSWORD` | contraseña de Neon | Sí |
| `JWT_SECRET` | `openssl rand -hex 32` | Sí |
| `JWT_EXPIRATION` | `86400000` (24 h) | No |
| `API_URL` | URL del frontend | No |

> No definas `SPRING_PROFILES_ACTIVE=local`: apuntaría al Postgres del
> contenedor y rompería la conexión a Neon.

### En local — archivo `.env`

Además de las anteriores, el perfil `local` usa:

| Variable | Valor |
|---|---|
| `LOCAL_DB_URL` | `jdbc:postgresql://localhost:5432/beautymanager` |
| `LOCAL_DB_USERNAME` | tu usuario local |
| `LOCAL_DB_PASSWORD` | tu contraseña local |

---

## 11. Checklist

### Antes de subir cambios

```bash
./mvnw clean test                                  # compila y prueba
docker compose up -d --build                       # valida la imagen
curl http://localhost:8080/actuator/health         # debe responder UP
git check-ignore -q .env && echo ".env ignorado OK" # sin secretos
```

### Después de desplegar

```bash
U=https://beautymanagerapi-backend.onrender.com
curl -s $U/actuator/health                         # {"status":"UP"}

TOKEN=$(curl -s -X POST $U/api/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"email":"root@beautymanager.com","password":"Admin2026!"}' \
  | python3 -c 'import json,sys; print(json.load(sys.stdin)["token"])')

curl -s -H "Authorization: Bearer $TOKEN" $U/api/clients | head -c 200
```

En los logs de Render, confirmar:

- [ ] `Tomcat started on port 10000` (el `server.port` dinámico)
- [ ] `Database JDBC URL [...neon.tech...]` (conecta a Neon)
- [ ] `Your service is live`

---

## Referencias

- [Spring Boot — Externalized Configuration](https://docs.spring.io/spring-boot/reference/features/external-config.html)
- [Docker — Multi-stage builds](https://docs.docker.com/build/building/multi-stage/)
- [Docker — Compose](https://docs.docker.com/compose/)
- [Render — Port Binding](https://render.com/docs/web-services#port-binding)
- [Neon — Connection pooling](https://neon.tech/docs/connect/connection-pooling)
- [Flyway — Migrations](https://documentation.red-gate.com/fd/migrations-184127470.html)

