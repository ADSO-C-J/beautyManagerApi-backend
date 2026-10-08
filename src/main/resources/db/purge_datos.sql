-- Purga de datos de negocio para pruebas de flujo.
-- Trunca TODAS las tablas de negocio y reinicia identidades, dejando intacto
-- flyway_schema_history (no re-ejecuta migraciones ni rompe el arranque).
-- Uso: psql "$URI" -v ON_ERROR_STOP=1 -f purge_datos.sql
DO $$
DECLARE
    tablas text;
BEGIN
    SELECT string_agg(format('public.%I', tablename), ', ')
    INTO tablas
    FROM pg_tables
    WHERE schemaname = 'public'
      AND tablename <> 'flyway_schema_history';

    IF tablas IS NULL THEN
        RAISE NOTICE 'No hay tablas para purgar.';
        RETURN;
    END IF;

    EXECUTE 'TRUNCATE TABLE ' || tablas || ' RESTART IDENTITY CASCADE';
    RAISE NOTICE 'Tablas purgadas: %', tablas;
END $$;
