CREATE TABLE calendar_event (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  title VARCHAR(200) NOT NULL,
  location VARCHAR(200),
  description TEXT,
  start_utc TIMESTAMP(6) NOT NULL,
  end_utc TIMESTAMP(6),
  all_day BOOLEAN NOT NULL,
  color VARCHAR(16),
  created_at TIMESTAMP(6) NOT NULL,
  updated_at TIMESTAMP(6) NOT NULL
);
CREATE INDEX idx_calendar_user_time ON calendar_event(user_id, start_utc, end_utc);
