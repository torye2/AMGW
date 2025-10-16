-- 출퇴근 로그
CREATE TABLE attendance_log (
                                id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                user_id BIGINT NOT NULL,
                                work_date DATE NOT NULL,
                                check_in_at DATETIME(3) NULL,
                                check_out_at DATETIME(3) NULL,
                                source ENUM('WEB','MOBILE','ADMIN','AUTO') NOT NULL DEFAULT 'WEB',
                                note VARCHAR(255) NULL,
                                created_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
                                updated_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
                                UNIQUE KEY uk_attlog_user_date (user_id, work_date),
                                KEY ix_attlog_user_created (user_id, created_at DESC),
                                CONSTRAINT fk_attlog_user FOREIGN KEY (user_id) REFERENCES users(id)
                                    ON UPDATE CASCADE ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 근태 신청
CREATE TABLE attendance_request (
                                    id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                    user_id BIGINT NOT NULL,
                                    type ENUM('VACATION','SICK','WFH','OUT') NOT NULL,
                                    status ENUM('PENDING','APPROVED','REJECTED') NOT NULL DEFAULT 'PENDING',
                                    start_date DATE NOT NULL,
                                    end_date DATE NOT NULL,
                                    start_time TIME NULL,
                                    end_time TIME NULL,
                                    reason VARCHAR(500) NULL,
                                    approver_id BIGINT NULL,
                                    created_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
                                    updated_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
                                    INDEX ix_attreq_user_created (user_id, created_at DESC),
                                    INDEX ix_attreq_period (start_date, end_date),
                                    INDEX ix_attreq_status (status),
                                    CONSTRAINT fk_attreq_user FOREIGN KEY (user_id) REFERENCES users(id)
                                        ON UPDATE CASCADE ON DELETE RESTRICT,
                                    CONSTRAINT fk_attreq_approver FOREIGN KEY (approver_id) REFERENCES users(id)
                                        ON UPDATE CASCADE ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 결재 문서
CREATE TABLE approval_doc (
                              id BIGINT PRIMARY KEY AUTO_INCREMENT,
                              drafter_id BIGINT NOT NULL,
                              approver_id BIGINT NOT NULL,
                              title VARCHAR(200) NOT NULL,
                              body MEDIUMTEXT NOT NULL,
                              doc_type ENUM('GENERAL','VACATION','EXPENSE') NOT NULL DEFAULT 'GENERAL',
                              status ENUM('DRAFT','SUBMITTED','APPROVED','REJECTED') NOT NULL DEFAULT 'SUBMITTED',
                              created_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
                              updated_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
                              INDEX ix_appr_drafter (drafter_id, created_at DESC),
                              INDEX ix_appr_approver_status (approver_id, status, created_at DESC),
                              CONSTRAINT fk_appr_drafter FOREIGN KEY (drafter_id) REFERENCES users(id)
                                  ON UPDATE CASCADE ON DELETE RESTRICT,
                              CONSTRAINT fk_appr_approver FOREIGN KEY (approver_id) REFERENCES users(id)
                                  ON UPDATE CASCADE ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
