CREATE TABLE IF NOT EXISTS "inquiries" (
  id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
  subject TEXT NOT NULL,
  message TEXT NOT NULL,
  submitted_by VARCHAR (100) NOT NULL,
  submitted_by_email VARCHAR (100) NOT NULL,
  submitted_by_id UUID NULL DEFAULT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT NOW(),
  updated_at TIMESTAMP NOT NULL
  CONSTRAINT "inquiries_submitted_by_id_fkey" FOREIGN KEY ("submitted_by_id") REFERENCES "user_clues" ("id") ON UPDATE NO ACTION ON DELETE SET NULL,
);

CREATE INDEX "inquiries_index" ON "inquiries" ("submitted_by_id");
