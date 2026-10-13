-- Single-user local development schema. Authentication and scoring are separate later work.
-- All business identifiers are scoped to a user; existing demo UUID/string IDs are preserved.
CREATE TABLE app_user (
  id VARCHAR(64) NOT NULL PRIMARY KEY,
  nickname VARCHAR(40) NOT NULL,
  bio VARCHAR(500) NULL,
  avatar MEDIUMTEXT NULL,
  created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
) ENGINE=InnoDB;

CREATE TABLE personal_stock (
  user_id VARCHAR(64) NOT NULL PRIMARY KEY,
  stock_code VARCHAR(32) NOT NULL,
  initial_index DECIMAL(14,2) NOT NULL DEFAULT 1000.00,
  UNIQUE KEY uk_stock_code (stock_code),
  CONSTRAINT fk_stock_user FOREIGN KEY (user_id) REFERENCES app_user(id)
) ENGINE=InnoDB;

CREATE TABLE workspace_state (
  user_id VARCHAR(64) NOT NULL PRIMARY KEY,
  initialized BOOLEAN NOT NULL DEFAULT FALSE,
  imported_from VARCHAR(32) NULL,
  updated_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  CONSTRAINT fk_workspace_user FOREIGN KEY (user_id) REFERENCES app_user(id)
) ENGINE=InnoDB;

CREATE TABLE event_category (
  user_id VARCHAR(64) NOT NULL,
  id VARCHAR(64) NOT NULL,
  name VARCHAR(24) NOT NULL,
  `system` BOOLEAN NOT NULL,
  active BOOLEAN NOT NULL,
  PRIMARY KEY (user_id, id),
  UNIQUE KEY uk_category_name (user_id, name),
  CONSTRAINT fk_category_user FOREIGN KEY (user_id) REFERENCES app_user(id)
) ENGINE=InnoDB;

CREATE TABLE growth_goal (
  user_id VARCHAR(64) NOT NULL,
  id VARCHAR(64) NOT NULL,
  title VARCHAR(120) NOT NULL,
  category_id VARCHAR(64) NOT NULL,
  description VARCHAR(2000) NULL,
  start_date DATE NOT NULL,
  due_date DATE NOT NULL,
  progress INT NOT NULL,
  status VARCHAR(16) NOT NULL,
  completed_date DATE NULL,
  PRIMARY KEY (user_id, id),
  CONSTRAINT fk_goal_category FOREIGN KEY (user_id, category_id) REFERENCES event_category(user_id, id),
  CONSTRAINT ck_goal_progress CHECK (progress BETWEEN 0 AND 100),
  CONSTRAINT ck_goal_dates CHECK (due_date >= start_date),
  CONSTRAINT ck_goal_status CHECK (status IN ('ACTIVE', 'COMPLETED'))
) ENGINE=InnoDB;

CREATE TABLE growth_task (
  user_id VARCHAR(64) NOT NULL,
  id VARCHAR(64) NOT NULL,
  title VARCHAR(120) NOT NULL,
  description VARCHAR(2000) NULL,
  due_date DATE NOT NULL,
  due_time VARCHAR(5) NOT NULL,
  priority VARCHAR(8) NOT NULL,
  goal_id VARCHAR(64) NULL,
  completed BOOLEAN NOT NULL,
  PRIMARY KEY (user_id, id),
  CONSTRAINT fk_task_user FOREIGN KEY (user_id) REFERENCES app_user(id),
  CONSTRAINT fk_task_goal FOREIGN KEY (user_id, goal_id) REFERENCES growth_goal(user_id, id)
) ENGINE=InnoDB;

CREATE TABLE growth_event (
  user_id VARCHAR(64) NOT NULL,
  id VARCHAR(64) NOT NULL,
  title VARCHAR(120) NOT NULL,
  category_id VARCHAR(64) NOT NULL,
  description VARCHAR(4000) NULL,
  event_date DATE NOT NULL,
  importance VARCHAR(8) NOT NULL,
  source_goal_id VARCHAR(64) NULL,
  evaluation_status VARCHAR(16) NOT NULL,
  contribution DECIMAL(14,2) NULL,
  PRIMARY KEY (user_id, id),
  CONSTRAINT fk_event_category FOREIGN KEY (user_id, category_id) REFERENCES event_category(user_id, id),
  CONSTRAINT fk_event_goal FOREIGN KEY (user_id, source_goal_id) REFERENCES growth_goal(user_id, id),
  CONSTRAINT ck_event_evaluation CHECK (
    (evaluation_status = 'PENDING' AND contribution IS NULL) OR
    (evaluation_status = 'DEMO' AND contribution IS NOT NULL)
  )
) ENGINE=InnoDB;

CREATE TABLE finance_record (
  user_id VARCHAR(64) NOT NULL,
  id VARCHAR(64) NOT NULL,
  type VARCHAR(8) NOT NULL,
  amount DECIMAL(12,2) NOT NULL,
  category VARCHAR(40) NOT NULL,
  record_date DATE NOT NULL,
  note VARCHAR(500) NULL,
  PRIMARY KEY (user_id, id),
  CONSTRAINT fk_finance_user FOREIGN KEY (user_id) REFERENCES app_user(id),
  CONSTRAINT ck_finance_type CHECK (type IN ('INCOME', 'EXPENSE')),
  CONSTRAINT ck_finance_amount CHECK (amount > 0)
) ENGINE=InnoDB;

-- Each asset save appends an independent snapshot. It never changes finance_record.
CREATE TABLE asset_snapshot (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  user_id VARCHAR(64) NOT NULL,
  cash DECIMAL(12,2) NOT NULL,
  other DECIMAL(12,2) NOT NULL,
  debt DECIMAL(12,2) NOT NULL,
  updated_at TIMESTAMP(6) NOT NULL,
  KEY idx_asset_user_latest (user_id, id),
  CONSTRAINT fk_asset_user FOREIGN KEY (user_id) REFERENCES app_user(id),
  CONSTRAINT ck_asset_amounts CHECK (cash >= 0 AND other >= 0 AND debt >= 0)
) ENGINE=InnoDB;
