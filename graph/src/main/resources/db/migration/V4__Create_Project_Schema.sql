CREATE TABLE IF NOT EXISTS projects (
  id BIGINT UNSIGNED PRIMARY KEY DEFAULT UUID_SHORT(),
  public_code VARCHAR (50) UNIQUE NOT NULL,
  name VARCHAR (100) NOT NULL,
  description TEXT NOT NULL,
  parent_organization_id BIGINT UNSIGNED NULL DEFAULT NULL,
  managed_by_id BIGINT UNSIGNED NULL DEFAULT NULL,
  created_by_id BIGINT UNSIGNED NULL DEFAULT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT NOW(),
  updated_at TIMESTAMP NOT NULL,
  CONSTRAINT projects_parent_organization_id_fkey FOREIGN KEY (parent_organization_id) REFERENCES organizations (id) ON UPDATE NO ACTION ON DELETE SET NULL,
  CONSTRAINT projects_managed_by_id_fkey FOREIGN KEY (managed_by_id) REFERENCES user_clues (id) ON UPDATE NO ACTION ON DELETE SET NULL,
  CONSTRAINT projects_created_by_id_fkey FOREIGN KEY (created_by_id) REFERENCES user_clues (id) ON UPDATE NO ACTION ON DELETE SET NULL
);

CREATE INDEX projects_index ON projects (parent_organization_id, managed_by_id, created_by_id);
