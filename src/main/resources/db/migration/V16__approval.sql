-- 1) approval_step
CREATE TABLE approval_step (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  doc_id BIGINT NOT NULL,
  order_no INT NOT NULL,
  approver_id BIGINT NOT NULL,
  status VARCHAR(20) NOT NULL,       -- PENDING / APPROVED / REJECTED / SKIPPED
  comment VARCHAR(1000) NULL,
  acted_at DATETIME NULL,
  CONSTRAINT fk_step_doc FOREIGN KEY (doc_id) REFERENCES approval_doc(id) ON DELETE CASCADE,
  CONSTRAINT uq_step_doc_order UNIQUE (doc_id, order_no)
) ENGINE=InnoDB;

CREATE INDEX idx_step_doc ON approval_step(doc_id);
CREATE INDEX idx_step_approver_status ON approval_step(approver_id, status);

-- 2) approval_action (타임라인)
CREATE TABLE approval_action (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  doc_id BIGINT NOT NULL,
  actor_id BIGINT NOT NULL,
  action_type VARCHAR(20) NOT NULL,  -- CREATE/SAVE_DRAFT/SUBMIT/APPROVE/REJECT/COMMENT/ATTACH_UPLOAD
  note VARCHAR(1000) NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_action_doc FOREIGN KEY (doc_id) REFERENCES approval_doc(id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE INDEX idx_action_doc_created ON approval_action(doc_id, created_at);

-- 3) approval_attachment (첨부)
CREATE TABLE approval_attachment (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  doc_id BIGINT NOT NULL,
  original_name VARCHAR(255) NOT NULL,
  stored_path  VARCHAR(500) NOT NULL,
  content_type VARCHAR(100) NULL,
  size BIGINT NULL,
  uploaded_by BIGINT NULL,
  uploaded_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_attach_doc FOREIGN KEY (doc_id) REFERENCES approval_doc(id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE INDEX idx_attach_doc ON approval_attachment(doc_id);

-- 4) approval_doc 컬럼 추가
ALTER TABLE approval_doc
  ADD COLUMN current_step_index INT NOT NULL DEFAULT 0,
  ADD COLUMN reject_reason VARCHAR(1000) NULL,
  ADD COLUMN submitted_at DATETIME NULL,
  ADD COLUMN decided_at DATETIME NULL,
  ADD COLUMN due_date DATE NULL;

-- 인덱스(선택): 목록/검색 최적화
CREATE INDEX idx_doc_created ON approval_doc(created_at);
CREATE INDEX idx_doc_drafter ON approval_doc(drafter_id, created_at);
CREATE INDEX idx_doc_approver_status ON approval_doc(approver_id, status, created_at);
