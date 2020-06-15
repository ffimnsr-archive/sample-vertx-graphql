CREATE TABLE IF NOT EXISTS project_clues (
  id BIGINT UNSIGNED PRIMARY KEY DEFAULT UUID_SHORT(),
  project_id BIGINT UNSIGNED NULL DEFAULT NULL,
  requirements TEXT NULL DEFAULT NULL,
  environments TEXT NULL DEFAULT NULL,
  repository_http_url TEXT NULL DEFAULT NULL,
  repository_ssh_url TEXT NULL DEFAULT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT NOW(),
  updated_at TIMESTAMP NOT NULL,
  CONSTRAINT project_clues_project_id_fkey FOREIGN KEY (project_id) REFERENCES projects (id) ON UPDATE NO ACTION ON DELETE SET NULL
);

CREATE INDEX project_clues_index ON project_clues (project_id);
