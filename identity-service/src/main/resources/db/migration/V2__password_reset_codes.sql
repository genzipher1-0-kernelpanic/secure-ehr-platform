CREATE TABLE IF NOT EXISTS password_reset_codes (
                                      id            BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
                                      user_id       BIGINT UNSIGNED NOT NULL,
                                      code_hash     VARBINARY(64) NOT NULL,
                                      expires_at    TIMESTAMP NOT NULL,
                                      used_at       TIMESTAMP NULL,
                                      created_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                      PRIMARY KEY (id),
                                      KEY idx_prc_user (user_id),
                                      KEY idx_prc_exp (expires_at),
                                      CONSTRAINT fk_prc_user
                                          FOREIGN KEY (user_id) REFERENCES users(id)
                                              ON DELETE CASCADE
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;
