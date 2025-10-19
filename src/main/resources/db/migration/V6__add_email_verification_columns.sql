ALTER TABLE users
    ADD COLUMN email_verify_status ENUM('PENDING','VERIFIED') NOT NULL DEFAULT 'PENDING' AFTER email,
  ADD COLUMN email_verified_at DATETIME NULL AFTER email_verify_status;
