CREATE TABLE IF NOT EXISTS "bank_accounts" (
  id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
  user_id UUID NULL DEFAULT NULL,
  account_name VARCHAR (100) NOT NULL,
  account_no VARCHAR (60) NOT NULL,
  bank_address TEXT NULL,
  bank_branch VARCHAR (45) NULL,
  bank_name VARCHAR (60) NULL,
  bank_swift_code VARCHAR (60) NULL,
  bank_routing_number VARCHAR (45) NULL,
  created_at TIMESTAMP NOT NULL DEFAULT NOW(),
  updated_at TIMESTAMP NOT NULL,
  CONSTRAINT "bank_accounts_user_id_fkey" FOREIGN KEY ("user_id") REFERENCES "user_clues" ("id") ON UPDATE NO ACTION ON DELETE SET NULL
);

CREATE INDEX "bank_accounts_index" ON "bank_accounts" ("user_id");
