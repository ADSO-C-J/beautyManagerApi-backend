-- =============================================================================
-- Tokens JWT revocados (cierre de sesión real)
-- =============================================================================
-- El JWT es stateless, así que para invalidarlo antes de su expiración se
-- guarda su identificador único (claim 'jti') hasta que caduque. A partir de
-- ese momento la fila ya no es necesaria y puede purgarse.
-- =============================================================================

CREATE TABLE revoked_tokens (
  jti         TEXT        PRIMARY KEY,
  user_id     UUID        REFERENCES users(id) ON DELETE CASCADE,
  expires_at  TIMESTAMPTZ NOT NULL,
  revoked_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Permite purgar eficientemente los tokens ya expirados.
CREATE INDEX idx_revoked_tokens_expires_at ON revoked_tokens (expires_at);

CREATE INDEX idx_revoked_tokens_user_id ON revoked_tokens (user_id);