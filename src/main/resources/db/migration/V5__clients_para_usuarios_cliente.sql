-- ============================================================================
-- V5: ficha en 'clients' para los usuarios con rol 'cliente' que no la tenían.
--
-- El registro público (/api/auth/register) y la creación de usuarios con rol
-- 'cliente' solo insertaban en 'users', por lo que esos clientes no aparecían
-- en el módulo Clientes del administrador (que lista la tabla 'clients') ni
-- podían agendar citas (los citas referencian clients.id, no users.id).
--
-- Idempotente: solo crea la ficha cuando el usuario todavía no tiene ninguna.
-- OJO: clients.user_id es UNIQUE, así que el NOT EXISTS ignora deleted_at; si la
-- fila existe pero está borrada lógicamente no se toca (el administrador decide
-- si debe volver a aparecer).
-- El negocio se toma el primero creado (mismo criterio que resolveBusinessId);
-- si no hubiera negocios no se inserta nada (clients.business_id es NOT NULL).
-- ============================================================================
INSERT INTO public.clients (
    id,
    user_id,
    business_id,
    name,
    email,
    phone,
    frequency,
    total_visits,
    total_spent,
    is_active,
    created_at,
    updated_at
)
SELECT gen_random_uuid(),
       u.id,
       b.id,
       u.name,
       u.email,
       u.phone,
       'baja'::public.client_frequency,
       0,
       0,
       TRUE,
       now(),
       now()
FROM public.users u
CROSS JOIN (
    SELECT id FROM public.businesses ORDER BY created_at ASC LIMIT 1
) b
WHERE u.role = 'cliente'
  AND u.deleted_at IS NULL
  AND NOT EXISTS (
      SELECT 1
      FROM public.clients c
      WHERE c.user_id = u.id
  );
