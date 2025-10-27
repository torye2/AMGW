DROP TABLE IF EXISTS compliment;

DROP TABLE IF EXISTS upload_file;

DROP TABLE IF EXISTS notice;

CREATE TABLE compliment (
    compliment_id BIGINT NOT NULL AUTO_INCREMENT, 
    user_id BIGINT NOT NULL,                                           
    compliment_count INT DEFAULT 0,                
    compliment_title VARCHAR(255) NOT NULL,       
    compliment_detail LONGTEXT,                        
    registration_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fix_time TIMESTAMP DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP, 
    PRIMARY KEY (compliment_id),
    FOREIGN KEY (user_id) REFERENCES users(id)    
) ENGINE=InnoDB
DEFAULT CHARSET=utf8mb4
COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE notice (
  notice_id BIGINT NOT NULL AUTO_INCREMENT,           
  file_id BIGINT DEFAULT NULL,                        
  notice_count INT DEFAULT 0,                        
  notice_title VARCHAR(255) NOT NULL,                 
  notice_detail LONGTEXT,                            
  user_id BIGINT NOT NULL,                            
  registration_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP, 
  fix_time TIMESTAMP NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP, 
  important TINYINT(1) DEFAULT 0,                    
  PRIMARY KEY (notice_id),
  KEY fk_notice_user (user_id),
  CONSTRAINT fk_notice_user FOREIGN KEY (user_id) REFERENCES users(id)
    ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB
DEFAULT CHARSET=utf8mb4
COLLATE=utf8mb4_0900_ai_ci;

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
