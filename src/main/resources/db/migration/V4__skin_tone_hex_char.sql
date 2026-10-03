-- ============================================================================
-- V4: facial_analyses.skin_tone_hex debe ser CHAR(7) (bpchar).
--
-- En la base de datos de Neon esta columna quedo creada como character
-- varying(7). La entidad FacialAnalysisEntity la declara como CHAR(7)
-- (@JdbcTypeCode(SqlTypes.CHAR)), por lo que spring.jpa.hibernate.ddl-auto=validate
-- fallaba al arrancar con:
--   "found [varchar], but expecting [char(7)]".
--
-- ALTER TYPE es idempotente: si ya era bpchar no cambia nada.
-- ============================================================================
ALTER TABLE public.facial_analyses
    ALTER COLUMN skin_tone_hex TYPE CHAR(7) USING skin_tone_hex::CHAR(7);
