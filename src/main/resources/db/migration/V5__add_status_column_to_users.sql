-- 1️⃣ 상태 코드 테이블 생성
CREATE TABLE user_status_codes (
                                   code VARCHAR(50) PRIMARY KEY,
                                   description VARCHAR(100) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 2️⃣ 기본 상태 코드 데이터 삽입
INSERT INTO user_status_codes (code, description) VALUES
                                                      ('PENDING', '가입 승인 대기'),
                                                      ('ACTIVE', '정상 사용 중'),
                                                      ('INACTIVE', '휴직/비활성'),
                                                      ('SUSPENDED', '관리자 정지'),
                                                      ('TERMINATED', '퇴사');

-- 3️⃣ users 테이블에 상태 코드 컬럼 추가
ALTER TABLE users
    ADD COLUMN status_code VARCHAR(50) NOT NULL DEFAULT 'PENDING' AFTER role,
ADD CONSTRAINT fk_user_status FOREIGN KEY (status_code)
    REFERENCES user_status_codes(code);
