CREATE TABLE upload_file (
  file_id INT NOT NULL AUTO_INCREMENT,
  attach_idx INT DEFAULT NULL,
  orig_name VARCHAR(255) NOT NULL,
  stored_name VARCHAR(255) NOT NULL,
  rel_path VARCHAR(500) NOT NULL,
  context_type VARCHAR(100) DEFAULT NULL,
  file_size INT DEFAULT NULL,
  user_id VARCHAR(50) NOT NULL,
  registration_time TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (file_id),
  CONSTRAINT fk_upload_notice
    FOREIGN KEY (attach_idx)
    REFERENCES notice (notice_id)
    ON DELETE CASCADE
) ENGINE=InnoDB
DEFAULT CHARSET=utf8mb4
COLLATE=utf8mb4_0900_ai_ci;