-- Prepara Neon: garantiza el negocio por defecto que el seed (datos_prueba_neon.sql)
-- asume como :biz_id. Se ejecuta ANTES del seed, tras una purga.
INSERT INTO public.businesses (id, name, address, city, country, phone, email, website, currency, timezone)
VALUES (
  'b0000000-0000-0000-0000-000000000001',
  'BeautyManager Salon',
  'Av. Principal 123',
  'Ciudad de Mexico',
  'MX',
  '+52 55 1234 5678',
  'contacto@beautymanager.com',
  'https://beautymanager.com',
  'USD',
  'America/Mexico_City'
)
ON CONFLICT (id) DO NOTHING;
