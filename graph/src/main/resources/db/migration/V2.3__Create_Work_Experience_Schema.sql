CREATE TABLE IF NOT EXISTS "work_experiences" (
  id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
  user_id UUID NULL DEFAULT NULL,
  title VARCHAR (100) NOT NULL,
  organization VARCHAR (200) NOT NULL,
  location VARCHAR (200) NOT NULL,
  from_date DATE NOT NULL,
  to_date DATE NOT NULL,
  description TEXT NULL DEFAULT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT NOW(),
  updated_at TIMESTAMP NOT NULL,
  CONSTRAINT "work_experiences_user_id_fkey" FOREIGN KEY ("user_id") REFERENCES "user_clues" ("id") ON UPDATE NO ACTION ON DELETE SET NULL
);

CREATE INDEX "work_experiences_index" ON "work_experiences" ("user_id");
