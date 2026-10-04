-- =============================================================================
-- Script de inicializacion del contenedor PostgreSQL
-- Se ejecuta UNA sola vez, cuando el volumen esta vacio.
-- =============================================================================
-- La migracion Flyway V1__esquema_inicial.sql fue generada con pg_dump en una
-- maquina local y contiene 35 sentencias "ALTER ... OWNER TO rome".
-- Ese rol no existe en este contenedor, por lo que la migracion fallaba con:
--     ERROR: role "rome" does not exist
--
-- No modificamos V1 a proposito: ya esta aplicada en Neon y en la base local,
-- y editar el archivo cambiaria su checksum y Flyway rechazaria la migracion
-- ("Migration checksum mismatch"). En su lugar creamos el rol aqui.
--
-- Este rol existe solo para satisfy las sentencias OWNER TO; la aplicacion
-- conecta con el usuario 'bmanager'.
DO $$
BEGIN
    IF NOT EXISTS (SELECT FROM pg_catalog.pg_roles WHERE rolname = 'rome') THEN
        CREATE ROLE rome WITH LOGIN CREATEDB;
    END IF;
END
$$;