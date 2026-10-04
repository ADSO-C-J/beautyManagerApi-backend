# BeautyManager API

API RESTful para la gestión de salones de belleza. Sistema de administración de servicios, clientes, citas, personal y pagos.

## Stack Tecnológico

| Tecnología | Versión | Propósito |
|---|---|---|
| Java | 21 | Lenguaje base |
| Spring Boot | 4.1.0 | Framework web |
| Spring WebMVC | 7.0.8 | API REST |
| Spring Data JPA | 4.1.0 | ORM / Persistencia |
| Spring Security | 7.1.0 | Autenticación y autorización |
| Spring Validation | — | Validación de DTOs |
| PostgreSQL | — | Base de datos relacional |
| Flyway | — | Migraciones de BD |
| Lombok | — | Reducción de boilerplate |
| BCrypt | — | Hashing de contraseñas |

## Requisitos

- **Java 21** o superior
- **Maven** (o usar el wrapper `./mvnw`)
- **PostgreSQL** 15+ (o una instancia Neon, ver más abajo)

### Elegir base de datos: Neon o PostgreSQL local

El cambio se hace con el **perfil de Spring**, no editando URLs:

| Cómo arrancas | Perfil | Base de datos | Variables |
|---|---|---|---|
| `./mvnw spring-boot:run` | (ninguno) | **Neon** | `DB_*` |
| `./mvnw spring-boot:run -Dspring-boot.run.profiles=local` | `local` | **PostgreSQL local** | `LOCAL_DB_*` |

```bash
# -> Neon
./mvnw spring-boot:run

# -> PostgreSQL local (localhost:5432)
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

En IntelliJ: **Run → Edit Configurations → Active profiles**.
Escribe `local` para la base local, déjalo vacío para Neon.

> ⚠️ **Las dos bases tienen datos distintos y NO están sincronizadas.**
> Tu local tiene 6 usuarios, Neon tiene 4. Flyway aplica las migraciones
> faltantes en la base contra la que arrancas, pero no copia los datos.
> No assumes que un usuario que existe en una existe en la otra.

### Datos de prueba (seed)

Hay dos seeds en `src/main/resources/db/seed/`:

| Archivo | Tipo | Cuándo usarlo |
|---|---|---|
| `datos_prueba.sql` | **Destructivo** (`TRUNCATE ... CASCADE`) | Base local vacía, quieres empezar de cero |
| `datos_prueba_neon.sql` | **Aditivo** (solo `INSERT`, idempotente) | Ya hay datos que no quieres perder |

`datos_prueba_neon.sql` **no borra nada**: usa UUID fijos y `ON CONFLICT DO NOTHING`,
así que puedes reejecutarlo las veces que quieras sin duplicar filas.

```bash
# Neon (aditivo)
PGPASSWORD=<pass> psql -h <host> -U <user> -d <db> \
  -f src/main/resources/db/seed/datos_prueba_neon.sql

# Otro negocio (por defecto usa b0000000-...-0001)
PGPASSWORD=<pass> psql -h <host> -U <user> -d <db> \
  -v biz_id=<business-uuid> \
  -f src/main/resources/db/seed/datos_prueba_neon.sql
```

Qué agrega: 4 usuarios, 2 staff, 8 clientes, 7 días de horario, 7 citas
(pasadas/futuras/canceladas), pagos, reseñas, análisis faciales, notas y
preferencias. Las citas usan fechas **relativas a `CURRENT_DATE`**, así que las
consultas por rango siempre devuelven algo.

Usuarios creados (contraseña `Password123`):

| Email | Rol |
|---|---|
| `prueba.estilista@beautymanager.com` | estilista |
| `prueba.estilista2@beautymanager.com` | estilista |
| `prueba.recepcion@beautymanager.com` | recepcionista |
| `prueba.cliente@beautymanager.com` | cliente |

> `/api/appointments` **exige** `dateFrom` y `dateTo`; sin ellos responde 401:
> ```
> GET /api/appointments?dateFrom=2020-01-01T00:00:00&dateTo=2030-12-31T23:59:59
> ```

### Configuración: el archivo `.env`

Las credenciales **no se versionan**. Viven en un archivo `.env` en la raíz del
proyecto, que ya está en `.gitignore` (junto con `.env.*`, excepto `.env.example`).

```bash
cp .env.example .env
```

Edita `.env` con tus valores:

```bash
# --- NEON (base por defecto) ---
# Usa el host "-pooler." de Neon, que es el recomendado para aplicaciones.
# NO añadas `channel_binding`: el driver JDBC de PostgreSQL no lo soporta.
DB_URL=jdbc:postgresql://ep-xxx-pooler.<region>.aws.neon.tech:5432/<DB>?sslmode=require
DB_USERNAME=<usuario>
DB_PASSWORD=<contrasena>

# --- PostgreSQL LOCAL (perfil "local") ---
LOCAL_DB_URL=jdbc:postgresql://localhost:5432/beautymanager
LOCAL_DB_USERNAME=<usuario_local>
LOCAL_DB_PASSWORD=<contrasena_local>

# --- JWT (compartido) ---
# Secreto de firma (HMAC). Minimo 32 bytes; `openssl rand -hex 32` da 64 hex.
# Obligatorio en produccion. Genera uno con: openssl rand -hex 32
# El compose ABORTA si falta: no hay valor por defecto, porque firmar con una
# clave conocida permitiria a cualquiera falsificar tokens.
JWT_SECRET=<genera-uno-con-openssl-rand-hex-32>
# Opcional (por defecto 24 h = 86400000 ms)
JWT_EXPIRATION=86400000

# --- Aplicacion ---
API_URL=http://localhost:5173
```

Spring carga el `.env` automáticamente gracias a
`spring.config.import=optional:file:.env[.properties]` en `application.properties`
(el sufijo `[.properties]` es necesario para que respete los nombres con `_`).
No hace falta ninguna librería extra.

> El `.env` **solo** se lee cuando la app se ejecuta desde la raíz del proyecto
> (Spring busca las rutas relativas ahí). Si la arrancas desde otro directorio,
> exporta las variables o usa `-Dspring.config.additional-location`.

**Precedencia:** variables de entorno del sistema > `.env` > `application*.properties`.

> ⚠️ No pongas `DB_URL` en las *Environment variables* de la run configuration
> de IntelliJ: tiene prioridad sobre el `.env` y lo anula. Si lo haces,
> asegúrate de que sea **una sola variable por línea**, porque el diálogo de
> IntelliJ no separa varios `CAMPO=valor` pegados en un mismo campo.

Para producción, define las variables en el entorno del servidor; el `.env` es
solo para desarrollo local.

> Si usas el perfil `local` (`application-local.properties`), el secreto JWT ya
> trae un valor de desarrollo por defecto, así que no necesitas configurar nada.

## Ejecución

### Opción 1: Docker (recomendado)

Levanta la API **y** PostgreSQL 18 en un solo comando:

```bash
docker compose up -d --build
docker compose logs -f api        # sigue el arranque
curl http://localhost:8080/actuator/health
```

| Servicio | URL | Notas |
|---|---|---|
| API | http://localhost:8080 | Swagger en `/swagger-ui.html` |
| Health | http://localhost:8080/actuator/health | público, lo usa el HEALTHCHECK |
| PostgreSQL | `127.0.0.1:5434` | user/db/pass: `bmanager` |

> El puerto **5434** es porque ya tienes PostgreSQL locales ocupando 5432 y 5433.
> Para usar otro: `POSTGRES_HOST_PORT=5555 docker compose up -d`. Dentro de la red
> de Docker la app siempre conecta a `db:5432`, así que el puerto del host no le
> afecta.

Comandos habituales:

```bash
docker compose ps                 # estado
docker compose restart api        # reiniciar solo la API
docker compose down               # parar (CONSERVA los datos)
docker compose down -v            # parar y BORRAR la base de datos
docker compose exec db psql -U bmanager -d beautymanager   # consola SQL
```

> Tras cambiar código Java, `docker compose restart api` **no** basta: la imagen
> contiene el jar compilado. Usa `docker compose up -d --build api`.

Detalles de la configuración:

- El `Dockerfile` es **multi-stage**: compila con Maven 3.9 + JDK 21 y la imagen
  final solo lleva el JRE y el jar (sin toolchain de build).
- La app corre como **usuario no-root** (`appuser`), no como root.
- `depends_on: condition: service_healthy` + `pg_isready` garantizan que Flyway
  no intente migrar antes de que PostgreSQL acepte conexiones.
- El volumen se monta en `/var/lib/postgresql` (no en `.../data`): **PostgreSQL 18+
  cambió esa ruta** y con la antigua el contenedor queda `unhealthy`.
- `docker/initdb/01-crear-rol-rome.sql` crea el rol `rome`, al que apuntan las 35
  sentencias `OWNER TO rome` de `V1__esquema_inicial.sql` (generada con `pg_dump` en
  local). No se modifica esa migración porque ya está aplicada en Neon y en tu base
  local: editarla cambiaría su checksum y Flyway la rechazaría.
- El puerto de PostgreSQL está ligado a `127.0.0.1` (no a `0.0.0.0`), para no
  exponer la base a la red local.
- `.dockerignore` excluye `.env`, `.git`, `.idea` y `target/` del contexto de
  build: tus credenciales nunca entran en la imagen.
- Las migraciones de Flyway se ejecutan solas al arrancar (V1-V4).

### Datos de prueba en Docker

La base del contenedor arranca vacía (solo el esquema). Para poblarla necesitas
primero un negocio, porque casi todas las tablas cuelgan de `business_id`:

```bash
docker compose exec -T db psql -U bmanager -d beautymanager -c \
  "INSERT INTO public.businesses (id, name, city, currency)
   VALUES ('d0000000-0000-0000-0000-000000000001','BeautyManager Docker','CDMX','MXN')
   ON CONFLICT (id) DO NOTHING;"

docker compose exec -T db psql -U bmanager -d beautymanager \
  -v biz_id=d0000000-0000-0000-0000-000000000001 \
  < src/main/resources/db/seed/datos_prueba_neon.sql
```

Resultado: 4 users, 8 clients, 7 appointments, 3 services, 2 staff.
Usuarios de prueba (password `Password123`): `prueba.estilista@beautymanager.com`,
`prueba.recepcion@beautymanager.com`, `prueba.cliente@beautymanager.com`,
`prueba.estilista2@beautymanager.com`.

> Este compose usa **PostgreSQL local**, no Neon. Para apuntar a Neon dentro de
> Docker, quita el servicio `db` y pasa `DB_URL`, `DB_USERNAME` y `DB_PASSWORD`
> como variables de entorno al contenedor.

### Opción 2: Maven directo (sin Docker)

```bash
git clone git@github.com:ADSO-C-J/beautyManagerApi-backend.git
cd beautyManagerApi-backend
./mvnw compile
./mvnw spring-boot:run
# http://localhost:8080
```

Con el perfil `local` (usa `application-local.properties` para el JWT de desarrollo):

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

> IntelliJ / VS Code **no** exportan variables de entorno por sí solos, pero Spring
> lee el `.env` de la raíz del proyecto automáticamente, así que la BD y el JWT
> funcionan sin configurar nada. Para el secreto JWT en `local` también hay valor
> por defecto.

---

## Estructura del Proyecto

```
src/main/java/com/beautyManager/beautyManagerApi/
├── BeautyManagerApiApplication.java    # Entry point
├── config/
│   ├── SecurityConfig.java             # Seguridad
│   └── UserRoleConverter.java          # Conversor de enums JPA
├── controller/
│   ├── UserController.java             # CRUD de usuarios
│   └── ServiceController.java          # CRUD de servicios
├── dto/
│   ├── UserRequestDTO.java             # Request body de usuarios
│   ├── UserResponseDTO.java            # Response body de usuarios
│   └── serviceDto/
│       ├── ServiceRequestDTO.java
│       └── ServiceResponseDTO.java
├── entity/
│   ├── User.java                       # Entidad: users
│   └── ServiceEntity.java              # Entidad: services
├── enums/
│   ├── UserRole.java
│   └── TypeServices.java
├── exception/
│   ├── GlobalExceptionHandler.java     # Manejador global de errores
│   └── ResourceNotFoundException.java  # Excepción 404
├── repository/
│   ├── UserRepository.java
│   └── ServiceRepository.java
└── service/
    ├── userService/
    │   ├── UserService.java
    │   └── UserServiceImpl.java
    └── serviceService/
        ├── ServiceService.java
        └── ServiceServiceImpl.java

src/main/resources/
├── application.properties
└── db/
    ├── schema.sql
    └── migration/
        └── V1__esquema_inicial.sql
```

---

## Endpoints de la API

### Usuarios (`/api/users`)

| Método | Ruta | Descripción |
|---|---|---|
| `GET` | `/api/users` | Obtener todos los usuarios activos |
| `GET` | `/api/users/{id}` | Obtener usuario por ID |
| `POST` | `/api/users` | Crear un nuevo usuario |
| `PUT` | `/api/users/{id}` | Actualizar un usuario |
| `DELETE` | `/api/users/{id}` | Soft delete |

#### POST /api/users

```json
{
    "name": "Juan Pérez",
    "email": "juan@example.com",
    "password": "123456",
    "phone": "+1 (555) 000-0001",
    "role": "estilista"
}
```

| Campo | Tipo | Req | Descripción |
|---|---|---|---|
| `name` | string | ✅ | Nombre del usuario |
| `email` | string | ✅ | Email válido (único) |
| `password` | string | ✅ | Mínimo 6 caracteres (BCrypt) |
| `phone` | string | ❌ | Teléfono |
| `role` | enum | ✅ | `administrador`, `estilista`, `recepcionista`, `cliente` |

#### GET /api/users

```json
[
  {
    "id": "uuid",
    "name": "Juan Pérez",
    "email": "juan@example.com",
    "phone": "+1 (555) 000-0001",
    "avatarUrl": null,
    "role": "estilista",
    "isActive": true,
    "createdAt": "2026-06-26T12:00:00",
    "updatedAt": "2026-06-26T12:00:00"
  }
]
```

### Servicios (`/api/services`)

| Método | Ruta | Descripción |
|---|---|---|
| `GET` | `/api/services` | Obtener servicios activos |
| `GET` | `/api/services/{id}` | Obtener servicio por ID |
| `POST` | `/api/services` | Crear un servicio |
| `PUT` | `/api/services/{id}` | Actualizar un servicio |
| `DELETE` | `/api/services/{id}` | Soft delete |

#### POST /api/services

```json
{
    "businessId": "b0000000-0000-0000-0000-000000000001",
    "name": "Corte degradado",
    "description": "Corte moderno para caballero",
    "category": "cabello",
    "duration_min": 45,
    "price": 25.99
}
```

| Campo | Tipo | Req | Descripción |
|---|---|---|---|
| `businessId` | UUID | ✅ | ID del negocio |
| `name` | string | ✅ | Nombre (único por negocio) |
| `description` | string | ❌ | Descripción |
| `category` | enum | ✅ | `cabello`, `manos`, `pies`, `caballeros`, `facial`, `otro` |
| `duration_min` | integer | ✅ | Minutos (positivo) |
| `price` | number | ✅ | Decimal (positivo) |

#### GET /api/services

```json
[
  {
    "id": "uuid",
    "name": "Corte degradado",
    "description": "Corte moderno para caballero",
    "duration_min": 45,
    "price": 25.99
  }
]
```

---

## Esquema de Base de Datos

PostgreSQL con enums nativos y soft delete.

### Enums

`user_role`, `service_category`, `appointment_status`, `client_frequency`, `payment_method`, `payment_status`, `day_of_week`, `skin_tone`, `hair_type`, `face_shape`.

### Tablas

`users`, `user_sessions`, `businesses`, `business_hours`, `staff`, `staff_schedules`, `clients`, `client_preferences`, `client_notes`, `services`, `staff_services`, `appointments`, `appointment_services`, `payments`.

### Convenciones

- **IDs**: UUID v4
- **Timestamps**: `created_at`, `updated_at`
- **Soft delete**: `deleted_at`
- **Nombres**: `snake_case`, plural
- **Precios**: `NUMERIC(10,2)`
- **Duraciones**: `INTEGER` minutos

---

## Datos Semilla

**Negocio:** `b0000000-0000-0000-0000-000000000001` — BeautyManager Salón

**Servicios:** Corte de cabello (45min/$25), Tinte completo (120min/$80), Manicure clásico (30min/$18), Pedicura spa (45min/$30), Corte caballero (30min/$15), Limpieza facial (60min/$35).

---

## Manejo de Errores

```json
// 404
{ "timestamp": "...", "status": 404, "message": "..." }

// 400 (validación)
{ "timestamp": "...", "status": 400, "errors": { "campo": "mensaje" } }
```

---

## Seguridad

- CSRF deshabilitado (temporal)
- Rutas permitidas sin autenticación (`permitAll`)
- Contraseñas hasheadas con BCrypt
- Preparado para JWT / sesión

---

## Despliegue en Render

Render construye la imagen desde el `Dockerfile` de la raíz (**ignora
`docker-compose.yml`**) y despliega contra **Neon**, no contra el Postgres del
contenedor.

**1. Sube los archivos de Docker al repositorio.** Render construye desde GitHub,
así que el `Dockerfile` debe estar commiteado; si no, falla con
`Dockerfile not found`.

**2. Crea el Web Service** en [dashboard.render.com](https://dashboard.render.com):

| Campo | Valor |
|---|---|
| Runtime | **Docker** (se detecta solo) |
| Branch | `main` |
| Health Check Path | `/actuator/health` |

**3. Define las variables de entorno** en el panel (Environment):

```
DB_URL       = jdbc:postgresql://ep-...-pooler.<region>.aws.neon.tech/BEAUTYMANAGER?sslmode=require
DB_USERNAME  = neondb_owner
DB_PASSWORD  = <contraseña de Neon>
JWT_SECRET   = <openssl rand -hex 32>
```

> **No definas `SPRING_PROFILES_ACTIVE=local`.** Ese perfil apunta al Postgres
> local del contenedor y la conexión a Neon fallaría. Sin perfil, la app usa
> `application.properties`, que es el que lee las `DB_*`.

Detalles importantes:

- El puerto lo define `server.port=${PORT:8080}`. Render inyecta un `PORT`
  aleatorio; sin esa línea la app escucharía en 8080 y Render no la encontraría.
- El health check usa `/actuator/health`, que es el único endpoint de Actuator
  público; `/actuator/env` y `/actuator/metrics` siguen requiriendo token.
- El plan **Free se duerme** tras 15 min de inactividad y el primer request
  tarda ~1 minuto. Para producción usa el plan Starter.
- Neon se conecta bien porque el host `pooler` acepta cualquier IP (no aplica la
  whitelist de IPs, que sí limitaría un host directo).
- Render gestiona el TLS y reenvía HTTP al contenedor: no hace falta nada extra.
- El `Dockerfile` corre como usuario no-root y expone `JAVA_OPTS` para que
  Render pueda ajustar la memoria.

**Verificar:**

```bash
curl https://<tu-servicio>.onrender.com/actuator/health
# {"groups":["liveness","readiness"],"status":"UP"}
```

En los logs de Render debes ver `Database JDBC URL [...neon.tech...]`.

> Las migraciones de Flyway se ejecutan solas al arrancar. Si más de una
> instancia levanta a la vez, Render lo serializa por defecto en el despliegue.

## Pruebas

```bash
./mvnw test
```
