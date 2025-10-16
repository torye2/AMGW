-- 문서 관리: 기본 문서 메타데이터
CREATE TABLE documents (
                           doc_id       BIGINT PRIMARY KEY AUTO_INCREMENT,
                           owner_id     BIGINT NOT NULL,
                           folder_id    BIGINT NULL,
                           title        VARCHAR(255) NOT NULL,
                           storage_key  VARCHAR(512) NOT NULL,      -- ex) /files/docs/{uuid}.docx
                           mime_type    VARCHAR(128) NOT NULL,
                           size_bytes   BIGINT NOT NULL,
                           version      INT NOT NULL DEFAULT 1,
                           lock_state   TINYINT NOT NULL DEFAULT 0, -- 0: unlocked, 1: locked
                           created_at   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                           updated_at   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                           INDEX ix_doc_owner (owner_id),
                           INDEX ix_doc_folder (folder_id),
                           CONSTRAINT fk_doc_owner FOREIGN KEY (owner_id) REFERENCES users(id)
                               ON UPDATE CASCADE ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 문서 버전 관리: 각 편집본 저장
CREATE TABLE document_versions (
                                   ver_id       BIGINT PRIMARY KEY AUTO_INCREMENT,
                                   doc_id       BIGINT NOT NULL,
                                   version      INT NOT NULL,
                                   storage_key  VARCHAR(512) NOT NULL,      -- ex) /files/versions/{doc_uuid}/{ver}.docx
                                   editor_id    BIGINT NOT NULL,
                                   change_note  VARCHAR(255) NULL,
                                   created_at   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                   UNIQUE KEY uk_doc_version (doc_id, version),
                                   INDEX ix_doc_ver (doc_id, created_at DESC),
                                   CONSTRAINT fk_ver_doc FOREIGN KEY (doc_id) REFERENCES documents(doc_id)
                                       ON UPDATE CASCADE ON DELETE CASCADE,
                                   CONSTRAINT fk_ver_editor FOREIGN KEY (editor_id) REFERENCES users(id)
                                       ON UPDATE CASCADE ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 문서 공유: 사용자/부서/링크 단위 접근제어
CREATE TABLE document_shares (
                                 share_id     BIGINT PRIMARY KEY AUTO_INCREMENT,
                                 doc_id       BIGINT NOT NULL,
                                 subject_type ENUM('USER','DEPT','LINK') NOT NULL,
                                 subject_id   BIGINT NULL,
                                 role         ENUM('OWNER','EDIT','VIEW','COMMENT') NOT NULL,
                                 link_token   CHAR(43) NULL,
                                 expires_at   DATETIME NULL,
                                 created_by   BIGINT NOT NULL,
                                 created_at   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                 INDEX ix_share_doc (doc_id),
                                 INDEX ix_share_subject (subject_type, subject_id),
                                 INDEX ix_share_token (link_token),
                                 CONSTRAINT fk_share_doc FOREIGN KEY (doc_id) REFERENCES documents(doc_id)
                                     ON UPDATE CASCADE ON DELETE CASCADE,
                                 CONSTRAINT fk_share_creator FOREIGN KEY (created_by) REFERENCES users(id)
                                     ON UPDATE CASCADE ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 문서 댓글(선택 기능)
CREATE TABLE document_comments (
                                   comment_id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                   doc_id     BIGINT NOT NULL,
                                   author_id  BIGINT NOT NULL,
                                   content    TEXT NOT NULL,
                                   created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                   INDEX ix_comment_doc (doc_id, created_at DESC),
                                   CONSTRAINT fk_comment_doc FOREIGN KEY (doc_id) REFERENCES documents(doc_id)
                                       ON UPDATE CASCADE ON DELETE CASCADE,
                                   CONSTRAINT fk_comment_author FOREIGN KEY (author_id) REFERENCES users(id)
                                       ON UPDATE CASCADE ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
