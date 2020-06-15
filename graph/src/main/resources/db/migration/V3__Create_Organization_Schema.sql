CREATE TABLE IF NOT EXISTS organizations (
  id BIGINT UNSIGNED PRIMARY KEY DEFAULT UUID_SHORT(),
  name VARCHAR (100) UNIQUE NOT NULL,
  description TEXT NOT NULL,
  managed_by_id BIGINT UNSIGNED NULL DEFAULT NULL,
  created_by_id BIGINT UNSIGNED NULL DEFAULT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT NOW(),
  updated_at TIMESTAMP NOT NULL,
  CONSTRAINT organizations_managed_by_id_fkey FOREIGN KEY (managed_by_id) REFERENCES user_clues (id) ON UPDATE NO ACTION ON DELETE SET NULL,
  CONSTRAINT organizations_created_by_id_fkey FOREIGN KEY (created_by_id) REFERENCES user_clues (id) ON UPDATE NO ACTION ON DELETE SET NULL
);

CREATE INDEX organizations_index ON organizations (created_by_id, managed_by_id);
