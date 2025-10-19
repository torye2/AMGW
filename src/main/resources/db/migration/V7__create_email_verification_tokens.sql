CREATE TABLE email_verification_tokens (
                                           id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                           user_id BIGINT NOT NULL,
                                           token VARCHAR(128) NOT NULL UNIQUE,
                                           purpose ENUM('EMAIL_VERIFY') NOT NULL DEFAULT 'EMAIL_VERIFY',
                                           expires_at DATETIME NOT NULL,
                                           used_at DATETIME NULL,
                                           sent_ip VARCHAR(64) NULL,
                                           sent_ua VARCHAR(255) NULL,
                                           created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                           INDEX idx_user_purpose (user_id, purpose, expires_at),
                                           CONSTRAINT fk_evt_user FOREIGN KEY (user_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
