CREATE TABLE IF NOT EXISTS "work_preferences" (
  id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
  user_id UUID NULL DEFAULT NULL,
  work_function_id NULL DEFAULT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT NOW(),
  updated_at TIMESTAMP NOT NULL,
  CONSTRAINT "work_preferences_user_id_fkey" FOREIGN KEY ("user_id") REFERENCES "user_clues" ("id") ON UPDATE NO ACTION ON DELETE SET NULL,
  CONSTRAINT "work_preferences_work_function_id_fkey" FOREIGN KEY ("work_function_id") REFERENCES "work_functions" ("id") ON UPDATE NO ACTION ON DELETE SET NULL
);

CREATE INDEX "work_preferences_index" ON "work_preferences" ("user_id", "work_function_id");
