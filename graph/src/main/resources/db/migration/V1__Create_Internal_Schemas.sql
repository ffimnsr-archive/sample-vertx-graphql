CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE IF NOT EXISTS "countries" (
  id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
  name VARCHAR (100) UNIQUE NOT NULL,
  alpha2 VARCHAR (3) NULL,
  alpha3 VARCHAR (4) NULL,
  phone_code VARCHAR (5) NULL,
  currency_code VARCHAR (5) NULL,
  created_at TIMESTAMP NOT NULL DEFAULT NOW(),
  updated_at TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS "industries" (
  id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
  name VARCHAR (100) UNIQUE NOT NULL,
  description TEXT NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT NOW(),
  updated_at TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS "ranks" (
  id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
  name VARCHAR (100) UNIQUE NOT NULL,
  description TEXT NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT NOW(),
  updated_at TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS "work_functions" (
  id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
  name VARCHAR (100) UNIQUE NOT NULL,
  description TEXT NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT NOW(),
  updated_at TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS "experience_levels" (
  id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
  name VARCHAR (100) UNIQUE NOT NULL,
  description TEXT NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT NOW(),
  updated_at TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS "project_categories" (
  id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
  name VARCHAR (100) UNIQUE NOT NULL,
  description TEXT NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT NOW(),
  updated_at TIMESTAMP NOT NULL
);
