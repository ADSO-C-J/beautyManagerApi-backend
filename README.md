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
- **PostgreSQL** 15+
- Variables de entorno:

```bash
export DB_URL=jdbc:postgresql://localhost:5432/beautymanager
export DB_USERNAME=postgres
export DB_PASSWORD=postgres
```

## Ejecución

```bash
git clone git@github.com:ADSO-C-J/beautyManagerApi-backend.git
cd beautyManagerApi-backend
./mvnw compile
./mvnw spring-boot:run
# http://localhost:8080
```

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
