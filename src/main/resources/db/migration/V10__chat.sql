-- 채팅방
CREATE TABLE IF NOT EXISTS chat_room (
  id            BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  type          ENUM('DIRECT','GROUP') NOT NULL,
  name          VARCHAR(191) NULL,                  -- GROUP일 때 방 이름
  created_by    BIGINT NOT NULL,                    -- gw.users.id (작성자)
  created_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 방-회원 매핑
CREATE TABLE IF NOT EXISTS chat_room_member (
  room_id       BIGINT NOT NULL,
  user_id       BIGINT NOT NULL,                    -- gw.users.id
  role          ENUM('OWNER','MEMBER') NOT NULL DEFAULT 'MEMBER',
  joined_at     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (room_id, user_id),
  CONSTRAINT fk_crm_room FOREIGN KEY (room_id) REFERENCES gw.chat_room(id) ON DELETE CASCADE,
  CONSTRAINT fk_crm_user FOREIGN KEY (user_id) REFERENCES gw.users(id) ON DELETE CASCADE,
  INDEX idx_crm_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 메시지
CREATE TABLE IF NOT EXISTS chat_message (
  id            BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  room_id       BIGINT NOT NULL,
  sender_id     BIGINT NOT NULL,                    -- gw.users.id
  content       TEXT NULL,                          -- 텍스트
  content_type  ENUM('TEXT','FILE','SYSTEM') NOT NULL DEFAULT 'TEXT',
  file_url      TEXT NULL,                          -- FILE일 때
  created_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_cm_room FOREIGN KEY (room_id) REFERENCES gw.chat_room(id) ON DELETE CASCADE,
  CONSTRAINT fk_cm_sender FOREIGN KEY (sender_id) REFERENCES gw.users(id),
  INDEX idx_cm_room_time (room_id, created_at),
  INDEX idx_cm_sender_time (sender_id, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 읽음 체크 (unread 카운트 계산용)
CREATE TABLE IF NOT EXISTS chat_message_read (
  message_id    BIGINT NOT NULL,
  user_id       BIGINT NOT NULL,
  read_at       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (message_id, user_id),
  CONSTRAINT fk_cmr_msg FOREIGN KEY (message_id) REFERENCES gw.chat_message(id) ON DELETE CASCADE,
  INDEX idx_cmr_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
