-- Crea (o actualiza) el usuario root con acceso total.
-- Credenciales: root@beautymanager.com / Admin2026!
-- Hash BCrypt generado con el propio BCryptPasswordEncoder del backend.
-- Idempotente: si el email ya existe, actualiza rol, contraseña y estado.
INSERT INTO public.users (id, name, email, password_hash, phone, role, is_active, created_at)
VALUES (
  'a0000000-0000-0000-0000-000000000000',
  'Root Administrator',
  'root@beautymanager.com',
  '$2a$10$O6aqTk6EMBVDzrt3Ci6gXuYXmSReFHDrXQRSF0dg5bx5KhISMOXMu',
  '+52 55 0000 0000',
  'administrador',
  true,
  now()
)
ON CONFLICT (email) DO UPDATE SET
  name = EXCLUDED.name,
  password_hash = EXCLUDED.password_hash,
  phone = EXCLUDED.phone,
  role = EXCLUDED.role,
  is_active = true,
  deleted_at = NULL;
