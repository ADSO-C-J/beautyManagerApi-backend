-- =============================================================================
-- BeautyManager - DATOS DE PRUEBA (seed ADITIVO, para Neon)
-- =============================================================================
-- A diferencia de datos_prueba.sql, este script NO hace TRUNCATE: solo INSERTA
-- filas nuevas y es IDEMPOTENTE (UUID fijos + ON CONFLICT DO NOTHING), asi que
-- se puede reejecutar sin duplicar nada. Los datos existentes NO se tocan.
--
-- Uso (Neon):
--   PGPASSWORD=<pass> psql -h <host> -U <user> -d <db> \
--     -v biz_id=<business-uuid> \
--     -f src/main/resources/db/seed/datos_prueba_neon.sql
--
-- Password de los usuarios de prueba: Password123
--   prueba.estilista@beautymanager.com   (estilista)
--   prueba.recepcion@beautymanager.com  (recepcionista)
--   prueba.cliente@beautymanager.com    (cliente)
-- =============================================================================

BEGIN;

-- Negocio destino (por defecto BeautyManager). Sobreescribir con -v biz_id=...
\if :{?biz_id}
\else
\set biz_id 'b0000000-0000-0000-0000-000000000001'
\endif

-- -----------------------------------------------------------------------------
-- users
-- -----------------------------------------------------------------------------
INSERT INTO public.users (id, email, password_hash, name, phone, role, is_active, email_verified_at) VALUES
('b1000000-0000-0000-0000-000000000001', 'prueba.estilista@beautymanager.com',  '$2a$10$kfaORch6A9fOBOKIzZuZ6edDE3IJCoStjc9a0I6marM4UZ54ZdNJS', 'Prueba Estilista',   '+52 55 1111 0011', 'estilista',     TRUE, NOW()),
('b1000000-0000-0000-0000-000000000002', 'prueba.recepcion@beautymanager.com', '$2a$10$kfaORch6A9fOBOKIzZuZ6edDE3IJCoStjc9a0I6marM4UZ54ZdNJS', 'Prueba Recepcion',  '+52 55 1111 0012', 'recepcionista', TRUE, NOW()),
('b1000000-0000-0000-0000-000000000003', 'prueba.cliente@beautymanager.com',   '$2a$10$kfaORch6A9fOBOKIzZuZ6edDE3IJCoStjc9a0I6marM4UZ54ZdNJS', 'Prueba Cliente',    '+52 55 1111 0013', 'cliente',       TRUE, NOW()),
('b1000000-0000-0000-0000-000000000004', 'prueba.estilista2@beautymanager.com','$2a$10$kfaORch6A9fOBOKIzZuZ6edDE3IJCoStjc9a0I6marM4UZ54ZdNJS', 'Prueba Estilista 2', '+52 55 1111 0014', 'estilista',      TRUE, NOW())
ON CONFLICT (id) DO NOTHING;

-- -----------------------------------------------------------------------------
-- staff
-- -----------------------------------------------------------------------------
INSERT INTO public.staff (id, user_id, business_id, specialty, bio, hire_date, commission_pct, is_active) VALUES
('b2000000-0000-0000-0000-000000000001', 'b1000000-0000-0000-0000-000000000001', :'biz_id', 'Color y balayage', 'Estilista de prueba especializado en color',   '2023-05-10', 15.00, TRUE),
('b2000000-0000-0000-0000-000000000002', 'b1000000-0000-0000-0000-000000000004', :'biz_id', 'Cortes',           'Estilista de prueba especializado en cortes', '2024-02-01', 12.50, TRUE)
ON CONFLICT (id) DO NOTHING;

-- -----------------------------------------------------------------------------
-- services
-- -----------------------------------------------------------------------------
-- El seed crea sus propios servicios con UUIDs propios (prefijo be). Antes
-- referenciaba servicios que ya existian en Neon, lo que hacia fallar la FK
-- appointment_services_service_id_fkey en bases nuevas (Docker).
INSERT INTO public.services (id, business_id, name, description, category, duration_min, price, is_popular, display_order) VALUES
('be000000-0000-0000-0000-000000000001', :'biz_id', 'Corte de cabello', 'Corte para mujer u hombre',        'cabello',   45,  25.00, TRUE,  1),
('be000000-0000-0000-0000-000000000002', :'biz_id', 'Tinte completo',    'Coloracion de raiz a puntas',       'cabello',  120,  80.00, TRUE,  2),
('be000000-0000-0000-0000-000000000003', :'biz_id', 'Manicure',          'Uñas de gel con esmaltado',         'manos',     60,  30.00, FALSE, 3)
ON CONFLICT (id) DO NOTHING;

-- -----------------------------------------------------------------------------
-- business_hours  (estaba vacia en Neon)
-- -----------------------------------------------------------------------------
INSERT INTO public.business_hours (id, business_id, day, opens_at, closes_at, is_closed) VALUES
('b3000000-0000-0000-0000-000000000001', :'biz_id', 'lunes',     '09:00', '18:00', FALSE),
('b3000000-0000-0000-0000-000000000002', :'biz_id', 'martes',    '09:00', '18:00', FALSE),
('b3000000-0000-0000-0000-000000000003', :'biz_id', 'miercoles', '09:00', '18:00', FALSE),
('b3000000-0000-0000-0000-000000000004', :'biz_id', 'jueves',    '09:00', '18:00', FALSE),
('b3000000-0000-0000-0000-000000000005', :'biz_id', 'viernes',   '09:00', '19:00', FALSE),
('b3000000-0000-0000-0000-000000000006', :'biz_id', 'sabado',    '09:00', '15:00', FALSE),
('b3000000-0000-0000-0000-000000000007', :'biz_id', 'domingo',   NULL,    NULL,    TRUE)
ON CONFLICT (id) DO NOTHING;

-- -----------------------------------------------------------------------------
-- staff_schedules
-- -----------------------------------------------------------------------------
INSERT INTO public.staff_schedules (id, staff_id, day, starts_at, ends_at, is_active) VALUES
('b4000000-0000-0000-0000-000000000001', 'b2000000-0000-0000-0000-000000000001', 'lunes',     '09:00', '18:00', TRUE),
('b4000000-0000-0000-0000-000000000002', 'b2000000-0000-0000-0000-000000000001', 'martes',    '09:00', '18:00', TRUE),
('b4000000-0000-0000-0000-000000000003', 'b2000000-0000-0000-0000-000000000001', 'miercoles', '09:00', '18:00', TRUE),
('b4000000-0000-0000-0000-000000000004', 'b2000000-0000-0000-0000-000000000001', 'jueves',    '09:00', '18:00', TRUE),
('b4000000-0000-0000-0000-000000000005', 'b2000000-0000-0000-0000-000000000001', 'viernes',   '09:00', '19:00', TRUE),
('b4000000-0000-0000-0000-000000000006', 'b2000000-0000-0000-0000-000000000002', 'martes',    '10:00', '19:00', TRUE),
('b4000000-0000-0000-0000-000000000007', 'b2000000-0000-0000-0000-000000000002', 'jueves',    '10:00', '19:00', TRUE),
('b4000000-0000-0000-0000-000000000008', 'b2000000-0000-0000-0000-000000000002', 'sabado',    '09:00', '14:00', TRUE)
ON CONFLICT (id) DO NOTHING;

-- -----------------------------------------------------------------------------
-- clients  (8 clientes: activos e inactivos, distintas frecuencias)
-- -----------------------------------------------------------------------------
INSERT INTO public.clients (id, user_id, business_id, name, email, phone, birth_date, address, frequency, total_visits, total_spent, last_visit_at, notes, is_active) VALUES
('b5000000-0000-0000-0000-000000000001', NULL, :'biz_id', 'Ana Ruiz',       'ana.ruiz@ejemplo.com',      '+52 55 2222 0001', '1992-04-12', 'Calle Juarez 101',       'alta',  12, 1450.00, CURRENT_DATE -  7, 'Cliente frecuente, prefiere citas por la manana.', TRUE),
('b5000000-0000-0000-0000-000000000002', NULL, :'biz_id', 'Luis Soto',       'luis.soto@ejemplo.com',      '+52 55 2222 0002', '1988-11-03', 'Av. Reforma 222',         'media',  6,  620.00, CURRENT_DATE - 21, NULL,                          TRUE),
('b5000000-0000-0000-0000-000000000003', NULL, :'biz_id', 'Maria Hernandez', 'maria.hernandez@ejemplo.com','+52 55 2222 0003', '1995-08-25', 'Calle Insurgentes 333',  'alta',  15, 1890.00, CURRENT_DATE -  3, 'Alergica a siliconas.',                      TRUE),
('b5000000-0000-0000-0000-000000000004', NULL, :'biz_id', 'Carlos Mendoza',  'carlos.mendoza@ejemplo.com', '+52 55 2222 0004', '1990-01-17', 'Paseo de la Reforma 444', 'media',  8,  910.00, CURRENT_DATE - 14, NULL,                          TRUE),
('b5000000-0000-0000-0000-000000000005', NULL, :'biz_id', 'Sofia Ramirez',   'sofia.ramirez@ejemplo.com',  '+52 55 2222 0005', '1998-06-30', 'Colima 555',              'baja',   2,  180.00, CURRENT_DATE - 45, 'Nueva cliente, primera visita pendiente.',     TRUE),
('b5000000-0000-0000-0000-000000000006', NULL, :'biz_id', 'Diego Fernandez', 'diego.fernandez@ejemplo.com','+52 55 2222 0006', '1985-03-09', 'Roma Norte 666',          'media',  5,  540.00, CURRENT_DATE - 30, NULL,                          TRUE),
('b5000000-0000-0000-0000-000000000007', NULL, :'biz_id', 'Valeria Ortiz',   'valeria.ortiz@ejemplo.com',  '+52 55 2222 0007', '1993-12-21', 'Condesa 777',             'baja',   1,   95.00, CURRENT_DATE - 60, NULL,                          TRUE),
('b5000000-0000-0000-0000-000000000008', NULL, :'biz_id', 'Ricardo Vazquez', 'ricardo.vazquez@ejemplo.com','+52 55 2222 0008', '1979-07-14', 'Polanco 888',             'baja',   0,    0.00, NULL,                 'Cliente inactivo, para probar el filtro.',    FALSE)
ON CONFLICT (id) DO NOTHING;

-- -----------------------------------------------------------------------------
-- client_notes
-- -----------------------------------------------------------------------------
INSERT INTO public.client_notes (id, client_id, staff_id, content) VALUES
('b6000000-0000-0000-0000-000000000001', 'b5000000-0000-0000-0000-000000000001', 'b2000000-0000-0000-0000-000000000001', 'Prefiere tintes en tonos calidos.'),
('b6000000-0000-0000-0000-000000000002', 'b5000000-0000-0000-0000-000000000003', 'b2000000-0000-0000-0000-000000000001', 'Confirmar alergia a siliconas antes de cada servicio.')
ON CONFLICT (id) DO NOTHING;

-- -----------------------------------------------------------------------------
-- client_preferences
-- -----------------------------------------------------------------------------
INSERT INTO public.client_preferences (id, client_id, key, value) VALUES
('b7000000-0000-0000-0000-000000000001', 'b5000000-0000-0000-0000-000000000001', 'horario_preferido',  'manana'),
('b7000000-0000-0000-0000-000000000002', 'b5000000-0000-0000-0000-000000000002', 'contacto_preferido', 'whatsapp')
ON CONFLICT (id) DO NOTHING;

-- -----------------------------------------------------------------------------
-- notifications
-- -----------------------------------------------------------------------------
INSERT INTO public.notifications (id, user_id, type, title, body, is_read, metadata) VALUES
('b8000000-0000-0000-0000-000000000001', 'b1000000-0000-0000-0000-000000000002', 'appointment', 'Nueva cita agendada',  'Ana Ruiz agendo una cita de corte',       FALSE, '{"clientId":"b5000000-0000-0000-0000-000000000001"}'),
('b8000000-0000-0000-0000-000000000002', 'b1000000-0000-0000-0000-000000000001', 'reminder',    'Recordatorio de cita', 'Tienes cita con Maria Hernandez manana', FALSE, NULL),
('b8000000-0000-0000-0000-000000000003', 'b1000000-0000-0000-0000-000000000002', 'system',      'Mantenimiento',        'Se cargo el seed de pruebas',            TRUE,  NULL)
ON CONFLICT (id) DO NOTHING;

-- -----------------------------------------------------------------------------
-- facial_analyses  (+ recomendaciones)
-- -----------------------------------------------------------------------------
INSERT INTO public.facial_analyses (id, client_id, staff_id, image_url, skin_tone, skin_tone_hex, hair_type, face_shape, confidence_pct, raw_result) VALUES
('b9000000-0000-0000-0000-000000000001', 'b5000000-0000-0000-0000-000000000001', 'b2000000-0000-0000-0000-000000000001', 'https://cdn.beautymanager.com/analisis/ana-01.jpg',    'morena_clara', '#C68642', 'ondulado', 'ovalado',  93.25, '{"modelo":"facial-v1","confianza":0.9325}'),
('b9000000-0000-0000-0000-000000000002', 'b5000000-0000-0000-0000-000000000003', NULL,                                   'https://cdn.beautymanager.com/analisis/maria-02.jpg',  'clara',        '#F1C27D', 'lacio',    'redondo',  87.50, '{"modelo":"facial-v1","confianza":0.875}'),
('b9000000-0000-0000-0000-000000000003', 'b5000000-0000-0000-0000-000000000004', NULL,                                   'https://cdn.beautymanager.com/analisis/carlos-03.jpg', 'oscura',       '#5C3836', 'rizado',   'cuadrado', 79.00, '{"modelo":"facial-v1","confianza":0.79}')
ON CONFLICT (id) DO NOTHING;

INSERT INTO public.facial_recommendations (id, analysis_id, category, title, description) VALUES
('ba000000-0000-0000-0000-000000000001', 'b9000000-0000-0000-0000-000000000001', 'cabello',      'Hidratacion profunda', 'Mascarilla capilar una vez por semana'),
('ba000000-0000-0000-0000-000000000002', 'b9000000-0000-0000-0000-000000000001', 'cuidado_piel', 'Protector solar',      'FPS 50 a diario'),
('ba000000-0000-0000-0000-000000000003', 'b9000000-0000-0000-0000-000000000002', 'cabello',      'Capas largas',         'Favorecen el rostro redondo'),
('ba000000-0000-0000-0000-000000000004', 'b9000000-0000-0000-0000-000000000003', 'otro',         'Evitar calor extremo', 'Apto para cabello rizado')
ON CONFLICT (id) DO NOTHING;

-- -----------------------------------------------------------------------------
-- appointments
-- Fechas relativas a CURRENT_DATE para que las consultas por rango siempre
-- devuelvan algo. Las citas 'cancelada' exigen cancelled_at NOT NULL
-- (CHECK cancelled_fields_consistent).
-- -----------------------------------------------------------------------------
INSERT INTO public.appointments (id, business_id, client_id, staff_id, scheduled_at, ends_at, status, notes, cancellation_reason, cancelled_by, cancelled_at, created_by) VALUES
('bb000000-0000-0000-0000-000000000001', :'biz_id', 'b5000000-0000-0000-0000-000000000001', 'b2000000-0000-0000-0000-000000000001', CURRENT_DATE + 1 + TIME '10:00', CURRENT_DATE + 1 + TIME '10:45', 'confirmada', 'Corte de cabello', NULL, NULL, NULL, 'b1000000-0000-0000-0000-000000000002'),
('bb000000-0000-0000-0000-000000000002', :'biz_id', 'b5000000-0000-0000-0000-000000000003', 'b2000000-0000-0000-0000-000000000001', CURRENT_DATE + 2 + TIME '12:00', CURRENT_DATE + 2 + TIME '14:00', 'confirmada', 'Tinte completo',   NULL, NULL, NULL, 'b1000000-0000-0000-0000-000000000002'),
('bb000000-0000-0000-0000-000000000003', :'biz_id', 'b5000000-0000-0000-0000-000000000004', 'b2000000-0000-0000-0000-000000000002', CURRENT_DATE + 3 + TIME '16:00', CURRENT_DATE + 3 + TIME '16:45', 'pendiente',  'Corte + barba',    NULL, NULL, NULL, 'b1000000-0000-0000-0000-000000000002'),
('bb000000-0000-0000-0000-000000000004', :'biz_id', 'b5000000-0000-0000-0000-000000000002', 'b2000000-0000-0000-0000-000000000001', CURRENT_DATE - 5 + TIME '11:00', CURRENT_DATE - 5 + TIME '11:45', 'completada', 'Corte de cabello', NULL, NULL, NULL, 'b1000000-0000-0000-0000-000000000002'),
('bb000000-0000-0000-0000-000000000005', :'biz_id', 'b5000000-0000-0000-0000-000000000006', 'b2000000-0000-0000-0000-000000000002', CURRENT_DATE - 9 + TIME '15:00', CURRENT_DATE - 9 + TIME '15:30', 'completada', 'Manicure',         NULL, NULL, NULL, 'b1000000-0000-0000-0000-000000000002'),
('bb000000-0000-0000-0000-000000000006', :'biz_id', 'b5000000-0000-0000-0000-000000000005', NULL,                                 CURRENT_DATE + 5 + TIME '09:00', CURRENT_DATE + 5 + TIME '10:00', 'pendiente',  'Primera visita',    NULL, NULL, NULL, 'b1000000-0000-0000-0000-000000000002'),
('bb000000-0000-0000-0000-000000000007', :'biz_id', 'b5000000-0000-0000-0000-000000000007', 'b2000000-0000-0000-0000-000000000001', CURRENT_DATE - 2 + TIME '13:00', CURRENT_DATE - 2 + TIME '14:00', 'cancelada',  'Cliente cancelo',   'El cliente no puede asistir', 'b1000000-0000-0000-0000-000000000002', NOW() - INTERVAL '2 days', NULL)
ON CONFLICT (id) DO NOTHING;

-- -----------------------------------------------------------------------------
-- appointment_services / staff_services
-- -----------------------------------------------------------------------------
INSERT INTO public.appointment_services (id, appointment_id, service_id, price_at_time, duration_at_time) VALUES
('bc000000-0000-0000-0000-000000000001', 'bb000000-0000-0000-0000-000000000001', 'be000000-0000-0000-0000-000000000001', 25.00,  45),
('bc000000-0000-0000-0000-000000000002', 'bb000000-0000-0000-0000-000000000002', 'be000000-0000-0000-0000-000000000002', 80.00, 120),
('bc000000-0000-0000-0000-000000000003', 'bb000000-0000-0000-0000-000000000005', 'be000000-0000-0000-0000-000000000003', 30.00,  60)
ON CONFLICT (id) DO NOTHING;

INSERT INTO public.staff_services (staff_id, service_id) VALUES
('b2000000-0000-0000-0000-000000000001', 'be000000-0000-0000-0000-000000000001'),
('b2000000-0000-0000-0000-000000000001', 'be000000-0000-0000-0000-000000000002'),
('b2000000-0000-0000-0000-000000000002', 'be000000-0000-0000-0000-000000000003')
ON CONFLICT DO NOTHING;

-- -----------------------------------------------------------------------------
-- payments + reviews  (solo sobre citas completadas / coherentes)
-- -----------------------------------------------------------------------------
INSERT INTO public.payments (id, appointment_id, amount, method, status, reference, paid_at, notes, created_by) VALUES
('bd000000-0000-0000-0000-000000000001', 'bb000000-0000-0000-0000-000000000004', 25.00, 'tarjeta_credito', 'pagado',    'PAG-SEED-0001', NOW() - INTERVAL '5 days', 'Pago con tarjeta',        'b1000000-0000-0000-0000-000000000002'),
('bd000000-0000-0000-0000-000000000002', 'bb000000-0000-0000-0000-000000000005', 30.00, 'efectivo',        'pagado',    'PAG-SEED-0002', NOW() - INTERVAL '9 days', 'Pago en efectivo',       'b1000000-0000-0000-0000-000000000002'),
('bd000000-0000-0000-0000-000000000003', 'bb000000-0000-0000-0000-000000000001', 25.00, 'transferencia',   'pendiente', NULL,            NULL,                    'Cita futura, sin cobrar', 'b1000000-0000-0000-0000-000000000002')
ON CONFLICT (id) DO NOTHING;

INSERT INTO public.reviews (id, appointment_id, client_id, staff_id, rating, comment, is_public) VALUES
('be000000-0000-0000-0000-000000000001', 'bb000000-0000-0000-0000-000000000004', 'b5000000-0000-0000-0000-000000000002', 'b2000000-0000-0000-0000-000000000001', 5, 'Excelente servicio, muy profesional.', TRUE),
('be000000-0000-0000-0000-000000000002', 'bb000000-0000-0000-0000-000000000005', 'b5000000-0000-0000-0000-000000000006', 'b2000000-0000-0000-0000-000000000002', 4, 'Muy buen corte, volvere.',            TRUE)
ON CONFLICT (id) DO NOTHING;

-- -----------------------------------------------------------------------------
-- Resumen
-- -----------------------------------------------------------------------------
\echo '--- Totales en la base tras el seed ---'
SELECT 'users' tabla, count(*) n FROM public.users
UNION ALL SELECT 'staff', count(*) FROM public.staff
UNION ALL SELECT 'clients', count(*) FROM public.clients
UNION ALL SELECT 'services', count(*) FROM public.services
UNION ALL SELECT 'appointments', count(*) FROM public.appointments
UNION ALL SELECT 'payments', count(*) FROM public.payments
UNION ALL SELECT 'reviews', count(*) FROM public.reviews
UNION ALL SELECT 'business_hours', count(*) FROM public.business_hours
UNION ALL SELECT 'facial_analyses', count(*) FROM public.facial_analyses
ORDER BY 1;

COMMIT;
