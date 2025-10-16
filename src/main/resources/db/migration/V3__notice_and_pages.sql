CREATE TABLE notice (
                        notice_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                        file_id BIGINT NULL,
                        notice_count INT DEFAULT 0,
                        notice_title VARCHAR(255) NOT NULL,
                        notice_detail TEXT,
                        user_id BIGINT NOT NULL,
                        registration_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        fix_time TIMESTAMP NULL,
                        important BOOLEAN DEFAULT FALSE,
                        CONSTRAINT fk_notice_user FOREIGN KEY (user_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE upload_file (
                             file_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                             attach_idx BIGINT,
                             orig_name VARCHAR(255) NOT NULL,
                             stored_name VARCHAR(255) NOT NULL,
                             rel_path VARCHAR(500) NOT NULL,
                             context_type VARCHAR(100),
                             file_size BIGINT,
                             user_id BIGINT NOT NULL,
                             registration_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                             CONSTRAINT fk_upload_user FOREIGN KEY (user_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE pages (
                       id BIGINT AUTO_INCREMENT PRIMARY KEY,
                       page_name VARCHAR(50) NOT NULL,
                       url VARCHAR(100) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

INSERT INTO pages (page_name, url) VALUES
                                       ('홈','/'),
                                       ('전자결재','/approvals.html'),
                                       ('근태관리','/attendance.html'),
                                       ('문서관리','/docs.html'),
                                       ('공지사항','/notice.html'),
                                       ('관리자','/admin.html');
