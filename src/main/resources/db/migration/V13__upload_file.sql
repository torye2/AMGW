-- Notice 테이블

drop table notice;

CREATE TABLE notice (
  notice_id BIGINT NOT NULL AUTO_INCREMENT,
  file_id BIGINT DEFAULT NULL,
  notice_count INT DEFAULT 0,
  notice_title VARCHAR(255) NOT NULL,
  notice_detail TEXT,
  user_id BIGINT NOT NULL,
  registration_time TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
  fix_time TIMESTAMP NULL DEFAULT NULL,
  important TINYINT(1) DEFAULT 0,
  PRIMARY KEY (notice_id),
  KEY fk_notice_user (user_id),
  CONSTRAINT fk_notice_user FOREIGN KEY (user_id) REFERENCES users (id)
) ENGINE=InnoDB
DEFAULT CHARSET=utf8mb4
COLLATE=utf8mb4_0900_ai_ci;


-- Upload_File 테이블
CREATE TABLE upload_file (
  file_id BIGINT NOT NULL AUTO_INCREMENT,
  attach_idx BIGINT DEFAULT NULL,
  orig_name VARCHAR(255) NOT NULL,
  stored_name VARCHAR(255) NOT NULL,
  rel_path VARCHAR(500) NOT NULL,
  context_type VARCHAR(100) DEFAULT NULL,
  file_size INT DEFAULT NULL,
  user_id BIGINT NOT NULL,
  registration_time TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (file_id),
  CONSTRAINT fk_upload_notice FOREIGN KEY (attach_idx)
    REFERENCES notice (notice_id)
    ON DELETE CASCADE
) ENGINE=InnoDB
DEFAULT CHARSET=utf8mb4
COLLATE=utf8mb4_0900_ai_ci;