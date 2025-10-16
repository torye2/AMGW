-- 사용자 및 권한 관리 초기 스키마
CREATE TABLE users (
                       id BIGINT PRIMARY KEY AUTO_INCREMENT,
                       username VARCHAR(191) NOT NULL UNIQUE,            -- 로그인 ID
                       password VARCHAR(255) NOT NULL,                   -- 암호(BCrypt)
                       name VARCHAR(100) NOT NULL,
                       email VARCHAR(191) UNIQUE,
                       role ENUM('ADMIN','EMPLOYEE','GUEST') NOT NULL DEFAULT 'EMPLOYEE',  -- 권한
                       department VARCHAR(100) NULL,
                       position VARCHAR(100) NULL,
                       last_login_at DATETIME NULL,
                       created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                       updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 기본 관리자 계정
INSERT INTO users (username, password, name, email, role)
VALUES ('admin', '$2a$10$z4vK2T3XqKfLzvZ1t7cWOeZ7qQCM.tYkq5xP1m80xTxZKxQzvTxJ6', '관리자', 'admin@example.com', 'ADMIN');
-- 위 password는 'admin123'의 BCrypt 예시
