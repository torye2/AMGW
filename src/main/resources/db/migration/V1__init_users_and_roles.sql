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
INSERT INTO users (username, password, name, email, role) VALUES
('admin', '$2a$10$z4vK2T3XqKfLzvZ1t7cWOeZ7qQCM.tYkq5xP1m80xTxZKxQzvTxJ6', '관리자', 'admin@example.com', 'ADMIN'),
('rkdgywh', '$2a$10$4Borv55PFh4G7/Mg8WlPpOrhIQny/6JoseJHNZjaXHLbtNyMUWHl2', '강효조', 'rkdgywh6343@naver.com', 'ADMIN'),
('rkdgyqls', '$2a$10$rnDCwVtIAFE4AYwwkwKFgOQe6wNz4VewxnUU2kFZShZsyayz/utZu', '강효빈', 'gyqls6343@naver.com', 'EMPLOYEE'),
('sw010111', '$2a$10$GucvYfH1peyxf71Sd468K.RxK6CzPJvVr6ho3w0WbnAAPOBmmtFBi', '우승우', 'sws8347@gmail.com', 'ADMIN'),
('leeoksou', '$2a$10$j08/XdL0sf/XSN8g/w8FpOxxjGUWPdN0lFsmzcFFoQmq9hrGQ5KKK', '이옥순', 'rkdgywh6343@gmail.com', 'EMPLOYEE'),
('1111', '$2a$10$E8Vn5GP4UxzP4Cc/vQWAuu1Qc1zm.YTSfjZDT1tqRzaN.vBKXg8ha', '홍길동', '1111@naver.com', 'ADMIN'),
('2222', '$2a$10$PTSQYACXyr6BwJlOmWQMGujbkWiCo9YC.Mxmpo3hFI8/dYiFTQSVO', '홍이동', '2222@naver.com', 'ADMIN'),
('3333', '$2a$10$j8tySYuf9vYUhvRoMQ16puw.HAHnyxJ/SRm40OclwiM4k49IxuTlK', '홍삼동', '3333@naver.com', 'ADMIN'),
('emailtest', '$2a$10$Ly0K.on2g61OO3xrVuQEb.N5PvzDZZXSXxLa.5pTqhCyE/wlr7I0S', '이메일', 'test@email.local', 'EMPLOYEE'),
('emailtestuser', '$2a$10$FKUn4KBG.xIW9qcpB/5/Aufb7Y95azjLYf3noRUw0LpRJERpEHp3K', '임해일', 'emailtest@test.local', 'EMPLOYEE'),
('email', '$2a$10$G1WKFQfXnLB2ngp8Od23Xe2o1icHrI9NjYTKNz6zVbjGfg.RcqmSu', 'email', 'testemail@mail.local', 'EMPLOYEE');

-- 위 password는 'admin123'의 BCrypt 예시
