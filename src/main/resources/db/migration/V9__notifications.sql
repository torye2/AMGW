CREATE TABLE gw.notifications (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,            -- 알림 대상 유저
    type VARCHAR(20) NOT NULL,          -- chat, approval, userRequest, vacation
    summary VARCHAR(200) NOT NULL,      -- 알림 표시 문구
    data JSON,                           -- 추가 JSON 데이터
    read_flag CHAR(1) DEFAULT 'N',      -- 읽음 여부 ('Y'/'N')
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
