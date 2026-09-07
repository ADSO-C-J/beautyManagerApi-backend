-- V2: user_sessions.ip_address pasa de inet a text para mapeo directo con String en JPA.
-- host() extrae la IP como texto; NULL se preserva.
ALTER TABLE user_sessions
    ALTER COLUMN ip_address TYPE text
    USING host(ip_address);
