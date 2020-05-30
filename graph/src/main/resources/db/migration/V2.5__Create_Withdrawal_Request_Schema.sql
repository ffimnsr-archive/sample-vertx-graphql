CREATE TABLE IF NOT EXISTS "withdrawal_requests" (
  id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
  user_id UUID NULL DEFAULT NULL,
  amount DECIMAL NULL DEFAULT NULL,
  reference_no VARCHAR (60) NULL DEFAULT NULL,
  remarks TEXT NULL DEFAULT NULL,
  approved_by_id UUID NULL DEFAULT NULL,
  approved_at TIMESTAMP NULL DEFAULT NULL,
  status SMALLINT NULL DEFAULT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT NOW(),
  updated_at TIMESTAMP NOT NULL,
  CONSTRAINT "withdrawal_requests_user_id_fkey" FOREIGN KEY ("user_id") REFERENCES "user_clues" ("id") ON UPDATE NO ACTION ON DELETE SET NULL,
  CONSTRAINT "withdrawal_requests_approved_by_id_fkey" FOREIGN KEY ("approved_by_id") REFERENCES "user_clues" ("id") ON UPDATE NO ACTION ON DELETE SET NULL
);

CREATE INDEX "withdrawal_requests_index" ON "withdrawal_requests" ("user_id", "approved_by_id");
