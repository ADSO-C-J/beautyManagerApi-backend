-- =============================================================================
-- BeautyManager — Esquema PostgreSQL
-- Versión: 2.0
-- =============================================================================
-- Convenciones:
--   · PK: UUID v4 generado con gen_random_uuid()
--   · Timestamps: created_at, updated_at en toda tabla, deleted_at para soft delete
--   · Enums: tipos PostgreSQL nativos
--   · Nomenclatura: snake_case, tablas en plural
--   · Precios: NUMERIC(10,2) en lugar de strings
--   · Duraciones: INTEGER en minutos en lugar de strings
-- =============================================================================

-- ---------------------------------------------------------------------------
-- EXTENSIONES
-- ---------------------------------------------------------------------------
CREATE EXTENSION IF NOT EXISTS "pgcrypto";   -- gen_random_uuid()
CREATE EXTENSION IF NOT EXISTS "pg_trgm";    -- búsqueda fuzzy por similitud

-- ---------------------------------------------------------------------------
-- FUNCIÓN HELPER: actualiza updated_at automáticamente en cada UPDATE
-- ---------------------------------------------------------------------------
CREATE OR REPLACE FUNCTION set_updated_at()
RETURNS TRIGGER AS $$
BEGIN
  NEW.updated_at = NOW();
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- =============================================================================
-- BLOQUE 1: TIPOS ENUMERADOS
-- =============================================================================

CREATE TYPE user_role AS ENUM (
  'administrador',
  'estilista',
  'recepcionista',
  'cliente'
);

CREATE TYPE appointment_status AS ENUM (
  'pendiente',
  'confirmada',
  'en_proceso',
  'completada',
  'cancelada',
  'no_presentado'
);

CREATE TYPE client_frequency AS ENUM (
  'alta',
  'media',
  'baja'
);

CREATE TYPE service_category AS ENUM (
  'cabello',
  'manos',
  'pies',
  'caballeros',
  'facial',
  'otro'
);

CREATE TYPE skin_tone AS ENUM (
  'muy_clara',
  'clara',
  'morena_clara',
  'morena',
  'morena_oscura',
  'oscura'
);

CREATE TYPE hair_type AS ENUM (
  'lacio',
  'ondulado',
  'rizado',
  'crespo',
  'afro'
);

CREATE TYPE face_shape AS ENUM (
  'ovalado',
  'redondo',
  'cuadrado',
  'corazon',
  'diamante',
  'rectangular'
);

CREATE TYPE day_of_week AS ENUM (
  'lunes',
  'martes',
  'miercoles',
  'jueves',
  'viernes',
  'sabado',
  'domingo'
);

CREATE TYPE payment_method AS ENUM (
  'efectivo',
  'tarjeta_credito',
  'tarjeta_debito',
  'transferencia',
  'otro'
);

CREATE TYPE payment_status AS ENUM (
  'pendiente',
  'pagado',
  'reembolsado',
  'fallido'
);

-- =============================================================================
-- BLOQUE 2: USUARIOS Y AUTENTICACIÓN
-- =============================================================================

CREATE TABLE users (
  id                UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
  email             TEXT        NOT NULL UNIQUE,
  password_hash     TEXT        NOT NULL,
  name              TEXT        NOT NULL,
  phone             TEXT,
  avatar_url        TEXT,
  role              user_role   NOT NULL DEFAULT 'cliente',
  is_active         BOOLEAN     NOT NULL DEFAULT TRUE,
  email_verified_at TIMESTAMPTZ,
  last_login_at     TIMESTAMPTZ,
  created_at        TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  updated_at        TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  deleted_at        TIMESTAMPTZ,

  CONSTRAINT users_email_format CHECK (email ~* '^[^@]+@[^@]+\.[^@]+$'),
  CONSTRAINT users_phone_format CHECK (
    phone IS NULL OR phone ~ '^\+?[0-9\s\-\(\)]{7,20}$'
  )
);

CREATE INDEX idx_users_email     ON users (email)     WHERE deleted_at IS NULL;
CREATE INDEX idx_users_role      ON users (role)      WHERE deleted_at IS NULL;
CREATE INDEX idx_users_is_active ON users (is_active) WHERE deleted_at IS NULL;

CREATE TRIGGER trg_users_updated_at
  BEFORE UPDATE ON users
  FOR EACH ROW EXECUTE FUNCTION set_updated_at();

-- Tokens de sesión / refresh tokens
CREATE TABLE user_sessions (
  id            UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id       UUID        NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  refresh_token TEXT        NOT NULL UNIQUE,
  expires_at    TIMESTAMPTZ NOT NULL,
  ip_address    INET,
  user_agent    TEXT,
  created_at    TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_user_sessions_user_id ON user_sessions (user_id);
CREATE INDEX idx_user_sessions_token   ON user_sessions (refresh_token);
CREATE INDEX idx_user_sessions_expires ON user_sessions (expires_at);

-- =============================================================================
-- BLOQUE 3: NEGOCIO (SALÓN)
-- =============================================================================

CREATE TABLE businesses (
  id          UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
  name        TEXT        NOT NULL,
  address     TEXT,
  city        TEXT,
  country     CHAR(2)     NOT NULL DEFAULT 'MX',
  phone       TEXT,
  email       TEXT,
  website     TEXT,
  logo_url    TEXT,
  currency    CHAR(3)     NOT NULL DEFAULT 'USD',
  timezone    TEXT        NOT NULL DEFAULT 'America/Mexico_City',
  created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  updated_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TRIGGER trg_businesses_updated_at
  BEFORE UPDATE ON businesses
  FOR EACH ROW EXECUTE FUNCTION set_updated_at();

-- Horarios de atención del negocio por día
CREATE TABLE business_hours (
  id          UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
  business_id UUID        NOT NULL REFERENCES businesses(id) ON DELETE CASCADE,
  day         day_of_week NOT NULL,
  opens_at    TIME,
  closes_at   TIME,
  is_closed   BOOLEAN     NOT NULL DEFAULT FALSE,
  created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  updated_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),

  UNIQUE (business_id, day),
  CONSTRAINT valid_hours CHECK (
    is_closed = TRUE OR (
      opens_at IS NOT NULL AND
      closes_at IS NOT NULL AND
      opens_at < closes_at
    )
  )
);

CREATE TRIGGER trg_business_hours_updated_at
  BEFORE UPDATE ON business_hours
  FOR EACH ROW EXECUTE FUNCTION set_updated_at();

-- =============================================================================
-- BLOQUE 4: PERSONAL (STAFF)
-- =============================================================================

-- El staff es un subconjunto de users (rol: estilista, recepcionista, administrador)
CREATE TABLE staff (
  id              UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id         UUID         NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
  business_id     UUID         NOT NULL REFERENCES businesses(id) ON DELETE CASCADE,
  specialty       TEXT,
  bio             TEXT,
  hire_date       DATE,
  commission_pct  NUMERIC(5,2) NOT NULL DEFAULT 0
                               CHECK (commission_pct BETWEEN 0 AND 100),
  is_active       BOOLEAN      NOT NULL DEFAULT TRUE,
  created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
  updated_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_staff_business_id ON staff (business_id) WHERE is_active = TRUE;
CREATE INDEX idx_staff_user_id     ON staff (user_id);

CREATE TRIGGER trg_staff_updated_at
  BEFORE UPDATE ON staff
  FOR EACH ROW EXECUTE FUNCTION set_updated_at();

-- Horario semanal de disponibilidad de cada estilista
CREATE TABLE staff_schedules (
  id          UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
  staff_id    UUID        NOT NULL REFERENCES staff(id) ON DELETE CASCADE,
  day         day_of_week NOT NULL,
  starts_at   TIME        NOT NULL,
  ends_at     TIME        NOT NULL,
  is_active   BOOLEAN     NOT NULL DEFAULT TRUE,
  created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  updated_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),

  UNIQUE (staff_id, day),
  CONSTRAINT valid_schedule CHECK (starts_at < ends_at)
);

CREATE TRIGGER trg_staff_schedules_updated_at
  BEFORE UPDATE ON staff_schedules
  FOR EACH ROW EXECUTE FUNCTION set_updated_at();

-- =============================================================================
-- BLOQUE 5: CLIENTES
-- =============================================================================

CREATE TABLE clients (
  id            UUID             PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id       UUID             UNIQUE REFERENCES users(id) ON DELETE SET NULL,
  business_id   UUID             NOT NULL REFERENCES businesses(id) ON DELETE CASCADE,
  name          TEXT             NOT NULL,
  email         TEXT,
  phone         TEXT,
  birth_date    DATE,
  address       TEXT,
  frequency     client_frequency NOT NULL DEFAULT 'baja',
  total_visits  INTEGER          NOT NULL DEFAULT 0 CHECK (total_visits >= 0),
  total_spent   NUMERIC(10,2)    NOT NULL DEFAULT 0 CHECK (total_spent >= 0),
  last_visit_at DATE,
  notes         TEXT,
  is_active     BOOLEAN          NOT NULL DEFAULT TRUE,
  created_at    TIMESTAMPTZ      NOT NULL DEFAULT NOW(),
  updated_at    TIMESTAMPTZ      NOT NULL DEFAULT NOW(),
  deleted_at    TIMESTAMPTZ,

  CONSTRAINT clients_email_format CHECK (
    email IS NULL OR email ~* '^[^@]+@[^@]+\.[^@]+$'
  )
);

CREATE INDEX idx_clients_business_id ON clients (business_id) WHERE deleted_at IS NULL;
CREATE INDEX idx_clients_email       ON clients (email)       WHERE deleted_at IS NULL AND email IS NOT NULL;
CREATE INDEX idx_clients_last_visit  ON clients (last_visit_at DESC);
CREATE INDEX idx_clients_frequency   ON clients (frequency);
-- Búsqueda fuzzy por nombre
CREATE INDEX idx_clients_name_trgm   ON clients USING gin (name gin_trgm_ops);

CREATE TRIGGER trg_clients_updated_at
  BEFORE UPDATE ON clients
  FOR EACH ROW EXECUTE FUNCTION set_updated_at();

-- Preferencias del cliente (estructura clave-valor flexible)
CREATE TABLE client_preferences (
  id          UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
  client_id   UUID        NOT NULL REFERENCES clients(id) ON DELETE CASCADE,
  key         TEXT        NOT NULL,
  value       TEXT        NOT NULL,
  created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  updated_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),

  UNIQUE (client_id, key)
);

CREATE TRIGGER trg_client_preferences_updated_at
  BEFORE UPDATE ON client_preferences
  FOR EACH ROW EXECUTE FUNCTION set_updated_at();

-- Notas del equipo sobre el cliente
CREATE TABLE client_notes (
  id          UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
  client_id   UUID        NOT NULL REFERENCES clients(id) ON DELETE CASCADE,
  staff_id    UUID        REFERENCES staff(id) ON DELETE SET NULL,
  content     TEXT        NOT NULL,
  created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  updated_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  deleted_at  TIMESTAMPTZ
);

CREATE INDEX idx_client_notes_client_id ON client_notes (client_id) WHERE deleted_at IS NULL;

CREATE TRIGGER trg_client_notes_updated_at
  BEFORE UPDATE ON client_notes
  FOR EACH ROW EXECUTE FUNCTION set_updated_at();

-- =============================================================================
-- BLOQUE 6: SERVICIOS
-- =============================================================================

CREATE TABLE services (
  id              UUID             PRIMARY KEY DEFAULT gen_random_uuid(),
  business_id     UUID             NOT NULL REFERENCES businesses(id) ON DELETE CASCADE,
  name            TEXT             NOT NULL,
  description     TEXT,
  category        service_category NOT NULL,
  duration_min    INTEGER          NOT NULL CHECK (duration_min > 0),
  price           NUMERIC(10,2)    NOT NULL CHECK (price >= 0),
  is_popular      BOOLEAN          NOT NULL DEFAULT FALSE,
  is_active       BOOLEAN          NOT NULL DEFAULT TRUE,
  display_order   INTEGER          NOT NULL DEFAULT 0,
  created_at      TIMESTAMPTZ      NOT NULL DEFAULT NOW(),
  updated_at      TIMESTAMPTZ      NOT NULL DEFAULT NOW(),
  deleted_at      TIMESTAMPTZ,

  UNIQUE (business_id, name)
);

CREATE INDEX idx_services_business_id ON services (business_id) WHERE deleted_at IS NULL;
CREATE INDEX idx_services_category    ON services (category)    WHERE deleted_at IS NULL;
CREATE INDEX idx_services_is_popular  ON services (is_popular)  WHERE deleted_at IS NULL;

CREATE TRIGGER trg_services_updated_at
  BEFORE UPDATE ON services
  FOR EACH ROW EXECUTE FUNCTION set_updated_at();

-- Qué servicios puede realizar cada estilista
CREATE TABLE staff_services (
  staff_id    UUID NOT NULL REFERENCES staff(id)    ON DELETE CASCADE,
  service_id  UUID NOT NULL REFERENCES services(id) ON DELETE CASCADE,
  created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),

  PRIMARY KEY (staff_id, service_id)
);

-- =============================================================================
-- BLOQUE 7: CITAS
-- =============================================================================

CREATE TABLE appointments (
  id                  UUID               PRIMARY KEY DEFAULT gen_random_uuid(),
  business_id         UUID               NOT NULL REFERENCES businesses(id) ON DELETE CASCADE,
  client_id           UUID               NOT NULL REFERENCES clients(id)    ON DELETE RESTRICT,
  staff_id            UUID               REFERENCES staff(id)               ON DELETE SET NULL,
  scheduled_at        TIMESTAMPTZ        NOT NULL,
  ends_at             TIMESTAMPTZ        NOT NULL,
  status              appointment_status NOT NULL DEFAULT 'pendiente',
  notes               TEXT,
  cancellation_reason TEXT,
  cancelled_by        UUID               REFERENCES users(id) ON DELETE SET NULL,
  cancelled_at        TIMESTAMPTZ,
  created_by          UUID               REFERENCES users(id) ON DELETE SET NULL,
  created_at          TIMESTAMPTZ        NOT NULL DEFAULT NOW(),
  updated_at          TIMESTAMPTZ        NOT NULL DEFAULT NOW(),
  deleted_at          TIMESTAMPTZ,

  CONSTRAINT valid_appointment_window CHECK (ends_at > scheduled_at),
  CONSTRAINT cancelled_fields_consistent CHECK (
    (status = 'cancelada' AND cancelled_at IS NOT NULL) OR
    (status <> 'cancelada')
  )
);

CREATE INDEX idx_appointments_business_id  ON appointments (business_id)  WHERE deleted_at IS NULL;
CREATE INDEX idx_appointments_client_id    ON appointments (client_id)    WHERE deleted_at IS NULL;
CREATE INDEX idx_appointments_staff_id     ON appointments (staff_id)     WHERE deleted_at IS NULL;
CREATE INDEX idx_appointments_scheduled_at ON appointments (scheduled_at) WHERE deleted_at IS NULL;
CREATE INDEX idx_appointments_status       ON appointments (status)       WHERE deleted_at IS NULL;
-- Índice para detectar conflictos de horario del estilista
CREATE INDEX idx_appointments_availability ON appointments (staff_id, scheduled_at, ends_at)
  WHERE deleted_at IS NULL AND status NOT IN ('cancelada', 'no_presentado');

CREATE TRIGGER trg_appointments_updated_at
  BEFORE UPDATE ON appointments
  FOR EACH ROW EXECUTE FUNCTION set_updated_at();

-- Servicios dentro de una cita (una cita puede incluir varios servicios)
CREATE TABLE appointment_services (
  id               UUID          PRIMARY KEY DEFAULT gen_random_uuid(),
  appointment_id   UUID          NOT NULL REFERENCES appointments(id) ON DELETE CASCADE,
  service_id       UUID          NOT NULL REFERENCES services(id)     ON DELETE RESTRICT,
  price_at_time    NUMERIC(10,2) NOT NULL,   -- snapshot del precio al momento de agendar
  duration_at_time INTEGER       NOT NULL,   -- snapshot de duración al momento de agendar
  created_at       TIMESTAMPTZ   NOT NULL DEFAULT NOW(),

  UNIQUE (appointment_id, service_id)
);

CREATE INDEX idx_appt_services_appointment_id ON appointment_services (appointment_id);
CREATE INDEX idx_appt_services_service_id     ON appointment_services (service_id);

-- =============================================================================
-- BLOQUE 8: PAGOS
-- =============================================================================

CREATE TABLE payments (
  id             UUID           PRIMARY KEY DEFAULT gen_random_uuid(),
  appointment_id UUID           NOT NULL REFERENCES appointments(id) ON DELETE RESTRICT,
  amount         NUMERIC(10,2)  NOT NULL CHECK (amount > 0),
  method         payment_method NOT NULL,
  status         payment_status NOT NULL DEFAULT 'pendiente',
  reference      TEXT,
  paid_at        TIMESTAMPTZ,
  notes          TEXT,
  created_by     UUID           REFERENCES users(id) ON DELETE SET NULL,
  created_at     TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
  updated_at     TIMESTAMPTZ    NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_payments_appointment_id ON payments (appointment_id);
CREATE INDEX idx_payments_status         ON payments (status);
CREATE INDEX idx_payments_paid_at        ON payments (paid_at DESC);

CREATE TRIGGER trg_payments_updated_at
  BEFORE UPDATE ON payments
  FOR EACH ROW EXECUTE FUNCTION set_updated_at();

-- =============================================================================
-- BLOQUE 9: VALORACIONES
-- =============================================================================

CREATE TABLE reviews (
  id             UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
  appointment_id UUID        NOT NULL UNIQUE REFERENCES appointments(id) ON DELETE CASCADE,
  client_id      UUID        NOT NULL REFERENCES clients(id) ON DELETE CASCADE,
  staff_id       UUID        REFERENCES staff(id) ON DELETE SET NULL,
  rating         SMALLINT    NOT NULL CHECK (rating BETWEEN 1 AND 5),
  comment        TEXT,
  is_public      BOOLEAN     NOT NULL DEFAULT TRUE,
  created_at     TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  updated_at     TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_reviews_staff_id  ON reviews (staff_id);
CREATE INDEX idx_reviews_client_id ON reviews (client_id);
CREATE INDEX idx_reviews_rating    ON reviews (rating);

CREATE TRIGGER trg_reviews_updated_at
  BEFORE UPDATE ON reviews
  FOR EACH ROW EXECUTE FUNCTION set_updated_at();

-- =============================================================================
-- BLOQUE 10: ANÁLISIS FACIAL
-- =============================================================================

CREATE TABLE facial_analyses (
  id             UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
  client_id      UUID         NOT NULL REFERENCES clients(id) ON DELETE CASCADE,
  staff_id       UUID         REFERENCES staff(id) ON DELETE SET NULL,
  image_url      TEXT,
  skin_tone      skin_tone,
  skin_tone_hex  CHAR(7),
  hair_type      hair_type,
  face_shape     face_shape,
  confidence_pct NUMERIC(5,2) CHECK (confidence_pct BETWEEN 0 AND 100),
  raw_result     JSONB,
  created_at     TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_facial_analyses_client_id  ON facial_analyses (client_id);
CREATE INDEX idx_facial_analyses_created_at ON facial_analyses (created_at DESC);

-- Recomendaciones generadas por el análisis
CREATE TABLE facial_recommendations (
  id          UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
  analysis_id UUID        NOT NULL REFERENCES facial_analyses(id) ON DELETE CASCADE,
  category    TEXT        NOT NULL,
  title       TEXT        NOT NULL,
  description TEXT        NOT NULL,
  created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_facial_recs_analysis_id ON facial_recommendations (analysis_id);

-- =============================================================================
-- BLOQUE 11: NOTIFICACIONES
-- =============================================================================

CREATE TABLE notification_preferences (
  id                    UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id               UUID        NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
  appointment_reminders BOOLEAN     NOT NULL DEFAULT TRUE,
  new_clients           BOOLEAN     NOT NULL DEFAULT TRUE,
  cancellations         BOOLEAN     NOT NULL DEFAULT TRUE,
  monthly_reports       BOOLEAN     NOT NULL DEFAULT FALSE,
  system_updates        BOOLEAN     NOT NULL DEFAULT FALSE,
  created_at            TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  updated_at            TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TRIGGER trg_notification_prefs_updated_at
  BEFORE UPDATE ON notification_preferences
  FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE TABLE notifications (
  id          UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id     UUID        NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  type        TEXT        NOT NULL,
  title       TEXT        NOT NULL,
  body        TEXT,
  is_read     BOOLEAN     NOT NULL DEFAULT FALSE,
  read_at     TIMESTAMPTZ,
  metadata    JSONB,
  created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_notifications_user_id ON notifications (user_id, is_read);
CREATE INDEX idx_notifications_created ON notifications (created_at DESC);

-- =============================================================================
-- BLOQUE 12: AUDITORÍA
-- =============================================================================

CREATE TABLE audit_logs (
  id          BIGSERIAL   PRIMARY KEY,
  user_id     UUID        REFERENCES users(id) ON DELETE SET NULL,
  action      TEXT        NOT NULL,
  table_name  TEXT,
  record_id   UUID,
  old_values  JSONB,
  new_values  JSONB,
  ip_address  INET,
  created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_audit_logs_user_id    ON audit_logs (user_id);
CREATE INDEX idx_audit_logs_table      ON audit_logs (table_name, record_id);
CREATE INDEX idx_audit_logs_created_at ON audit_logs (created_at DESC);

-- =============================================================================
-- BLOQUE 13: VISTAS
-- =============================================================================

-- Citas con toda la info desnormalizada
CREATE OR REPLACE VIEW v_appointments_full AS
SELECT
  a.id,
  a.scheduled_at,
  a.ends_at,
  a.status,
  a.notes,
  a.created_at,
  c.id            AS client_id,
  c.name          AS client_name,
  c.email         AS client_email,
  c.phone         AS client_phone,
  s.id            AS staff_id,
  u.name          AS staff_name,
  s.specialty     AS staff_specialty,
  COALESCE(
    jsonb_agg(
      jsonb_build_object(
        'id',       svc.id,
        'name',     svc.name,
        'category', svc.category,
        'price',    aps.price_at_time,
        'duration', aps.duration_at_time
      )
    ) FILTER (WHERE svc.id IS NOT NULL),
    '[]'::jsonb
  )               AS services,
  COALESCE(SUM(aps.price_at_time), 0)    AS total_price,
  COALESCE(SUM(aps.duration_at_time), 0) AS total_duration_min
FROM appointments a
  JOIN  clients  c   ON c.id = a.client_id
  LEFT JOIN staff s  ON s.id = a.staff_id
  LEFT JOIN users u  ON u.id = s.user_id
  LEFT JOIN appointment_services aps ON aps.appointment_id = a.id
  LEFT JOIN services svc             ON svc.id = aps.service_id
WHERE a.deleted_at IS NULL
GROUP BY a.id, c.id, s.id, u.name;

-- Rendimiento por estilista
CREATE OR REPLACE VIEW v_staff_performance AS
SELECT
  s.id                                  AS staff_id,
  u.name                                AS staff_name,
  s.specialty,
  COUNT(a.id) FILTER (
    WHERE a.status = 'completada'
  )                                     AS total_appointments,
  COALESCE(SUM(t.total), 0)             AS total_revenue,
  ROUND(AVG(r.rating)::NUMERIC, 2)      AS avg_rating,
  COUNT(r.id)                           AS total_reviews
FROM staff s
  JOIN  users u ON u.id = s.user_id
  LEFT JOIN appointments a ON a.staff_id = s.id AND a.deleted_at IS NULL
  LEFT JOIN (
    SELECT appointment_id, SUM(price_at_time) AS total
    FROM appointment_services GROUP BY appointment_id
  ) t ON t.appointment_id = a.id
  LEFT JOIN reviews r ON r.staff_id = s.id
WHERE s.is_active = TRUE
GROUP BY s.id, u.name, s.specialty;

-- Resumen de clientes para listados
CREATE OR REPLACE VIEW v_clients_summary AS
SELECT
  c.id,
  c.name,
  c.email,
  c.phone,
  c.frequency,
  c.total_visits,
  c.total_spent,
  c.last_visit_at,
  c.created_at,
  MIN(a.scheduled_at) FILTER (
    WHERE a.scheduled_at > NOW()
    AND   a.status IN ('pendiente', 'confirmada')
  ) AS next_appointment_at
FROM clients c
  LEFT JOIN appointments a ON a.client_id = c.id AND a.deleted_at IS NULL
WHERE c.deleted_at IS NULL
GROUP BY c.id;

-- =============================================================================
-- BLOQUE 14: DATOS INICIALES (SEED)
-- =============================================================================

INSERT INTO businesses (id, name, address, phone, email, currency, timezone) VALUES (
  'b0000000-0000-0000-0000-000000000001',
  'BeautyManager Salón',
  'Av. Principal 123, Ciudad',
  '+1 (555) 000-0001',
  'contacto@beautymanager.com',
  'USD',
  'America/Mexico_City'
);

INSERT INTO business_hours (business_id, day, opens_at, closes_at, is_closed) VALUES
  ('b0000000-0000-0000-0000-000000000001', 'lunes',     '09:00', '18:00', FALSE),
  ('b0000000-0000-0000-0000-000000000001', 'martes',    '09:00', '18:00', FALSE),
  ('b0000000-0000-0000-0000-000000000001', 'miercoles', '09:00', '18:00', FALSE),
  ('b0000000-0000-0000-0000-000000000001', 'jueves',    '09:00', '18:00', FALSE),
  ('b0000000-0000-0000-0000-000000000001', 'viernes',   '09:00', '18:00', FALSE),
  ('b0000000-0000-0000-0000-000000000001', 'sabado',    '09:00', '15:00', FALSE),
  ('b0000000-0000-0000-0000-000000000001', 'domingo',   NULL,    NULL,    TRUE);

INSERT INTO services (business_id, name, description, category, duration_min, price, is_popular, display_order) VALUES
  ('b0000000-0000-0000-0000-000000000001', 'Corte de cabello',    'Corte profesional para damas y caballeros',   'cabello',    45,  25.00, TRUE,  1),
  ('b0000000-0000-0000-0000-000000000001', 'Tinte completo',      'Aplicación de tinte profesional en todo el cabello', 'cabello', 120, 80.00, TRUE, 2),
  ('b0000000-0000-0000-0000-000000000001', 'Mechas/Luces',        'Aplicación de mechas para dar dimensión',     'cabello',   150,  95.00, FALSE, 3),
  ('b0000000-0000-0000-0000-000000000001', 'Manicure',            'Cuidado y embellecimiento de uñas y manos',   'manos',      60,  30.00, TRUE,  4),
  ('b0000000-0000-0000-0000-000000000001', 'Pedicure',            'Cuidado y embellecimiento de uñas y pies',    'pies',       60,  35.00, FALSE, 5),
  ('b0000000-0000-0000-0000-000000000001', 'Peinado especial',    'Peinado elaborado para eventos especiales',   'cabello',    90,  50.00, FALSE, 6),
  ('b0000000-0000-0000-0000-000000000001', 'Corte + Barba',       'Corte de cabello y arreglo de barba',         'caballeros', 60,  35.00, TRUE,  7),
  ('b0000000-0000-0000-0000-000000000001', 'Tratamiento capilar', 'Hidratación y reparación capilar profunda',   'cabello',    60,  45.00, FALSE, 8);
