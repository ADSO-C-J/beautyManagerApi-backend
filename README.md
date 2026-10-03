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
# Obligatoria en produccion: secreto para firmar los JWT (HS256, mínimo 32 bytes / 64 hex)
# Genera uno con: openssl rand -hex 32
JWT_SECRET=change-this-with-a-secure-32-byte-secret
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

## Pruebas

```bash
./mvnw test
```
