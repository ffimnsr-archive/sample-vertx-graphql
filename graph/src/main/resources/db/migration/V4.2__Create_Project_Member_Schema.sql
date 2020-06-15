CREATE TABLE IF NOT EXISTS project_members (
  id BIGINT UNSIGNED PRIMARY KEY DEFAULT UUID_SHORT(),
  user_id BIGINT UNSIGNED NULL DEFAULT NULL,
  project_id BIGINT UNSIGNED NULL DEFAULT NULL,
  work_function_id BIGINT UNSIGNED NULL DEFAULT NULL,
  start_date DATE NULL DEFAULT NULL,
  end_date DATE NULL DEFAULT NULL,
  status SMALLINT NULL DEFAULT NULL,
  remarks TEXT NULL DEFAULT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT NOW(),
  updated_at TIMESTAMP NOT NULL,
  CONSTRAINT project_members_user_id_fkey FOREIGN KEY (user_id) REFERENCES user_clues (id) ON UPDATE NO ACTION ON DELETE SET NULL,
  CONSTRAINT project_members_project_id_fkey FOREIGN KEY (project_id) REFERENCES projects (id) ON UPDATE NO ACTION ON DELETE SET NULL,
  CONSTRAINT project_members_work_function_id_fkey FOREIGN KEY (work_function_id) REFERENCES work_functions (id) ON UPDATE NO ACTION ON DELETE SET NULL
);

CREATE INDEX project_members_index ON project_members (user_id, project_id, work_function_id);
