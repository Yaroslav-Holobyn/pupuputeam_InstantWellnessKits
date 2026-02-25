CREATE EXTENSION IF NOT EXISTS postgis;

CREATE TABLE IF NOT EXISTS ny_state (
    state_code TEXT PRIMARY KEY, -- 'NY'
    geom geometry(MultiPolygon, 4326) NOT NULL
    );
CREATE INDEX IF NOT EXISTS ny_state_geom_gist ON ny_state USING GIST (geom);

CREATE TABLE IF NOT EXISTS ny_county (
    county_name TEXT PRIMARY KEY,
    county_fips TEXT,
    geom geometry(MultiPolygon, 4326) NOT NULL
    );
CREATE INDEX IF NOT EXISTS ny_county_geom_gist ON ny_county USING GIST (geom);

CREATE TABLE IF NOT EXISTS ny_muni (
    id BIGSERIAL PRIMARY KEY,
    muni_name TEXT NOT NULL,
    muni_type TEXT,
    county_name TEXT,
    fips_code TEXT,
    geom geometry(MultiPolygon, 4326) NOT NULL
    );
CREATE INDEX IF NOT EXISTS ny_muni_geom_gist ON ny_muni USING GIST (geom);
CREATE INDEX IF NOT EXISTS ny_muni_type_idx ON ny_muni(muni_type);
CREATE INDEX IF NOT EXISTS ny_muni_name_idx ON ny_muni(muni_name);

CREATE TABLE IF NOT EXISTS ny_tax_rate (
    jurisdiction_type TEXT NOT NULL,
    jurisdiction_name TEXT NOT NULL,
    total_rate NUMERIC(8,5) NOT NULL,
    reporting_code TEXT,
    special_zone TEXT,
    effective_from DATE NOT NULL DEFAULT DATE '2025-03-01',
    PRIMARY KEY (jurisdiction_type, jurisdiction_name, effective_from)
    );
CREATE INDEX IF NOT EXISTS ny_tax_rate_name_idx ON ny_tax_rate(jurisdiction_name);

CREATE TABLE IF NOT EXISTS orders (
    id BIGSERIAL PRIMARY KEY,
    ts TIMESTAMPTZ NOT NULL,
    subtotal NUMERIC(12,2) NOT NULL,

    lat DOUBLE PRECISION NOT NULL,
    lon DOUBLE PRECISION NOT NULL,
    geom geometry(Point, 4326) NOT NULL,

    in_ny BOOLEAN NOT NULL DEFAULT false,
    in_mctd BOOLEAN NOT NULL DEFAULT false,

    county_name TEXT,
    county_fips TEXT,
    muni_name TEXT,
    muni_type TEXT,
    muni_fips TEXT,

    rule_used TEXT,

    composite_tax_rate NUMERIC(8,5),
    tax_amount NUMERIC(12,2),
    total_amount NUMERIC(12,2)
    );
CREATE INDEX IF NOT EXISTS orders_ts_idx ON orders(ts);
CREATE INDEX IF NOT EXISTS orders_geom_gist ON orders USING GIST (geom);

CREATE TABLE IF NOT EXISTS seed_status (
    id INT PRIMARY KEY,
    seeded_at TIMESTAMPTZ NOT NULL,
    notes TEXT
);