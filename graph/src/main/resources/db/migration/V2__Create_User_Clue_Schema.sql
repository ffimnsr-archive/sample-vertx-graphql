CREATE TABLE IF NOT EXISTS user_clues (
  id BIGINT UNSIGNED PRIMARY KEY DEFAULT UUID_SHORT(),
  global_id TEXT NOT NULL,
  username VARCHAR(60) UNIQUE NOT NULL,
  avatar TEXT NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT NOW(),
  updated_at TIMESTAMP NOT NULL
);

CREATE INDEX user_clues_index ON user_clues (global_id);
