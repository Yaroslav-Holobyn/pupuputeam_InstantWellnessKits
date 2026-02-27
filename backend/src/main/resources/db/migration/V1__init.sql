CREATE EXTENSION IF NOT EXISTS postgis;

CREATE TABLE ny_state (
    state_code TEXT PRIMARY KEY, -- 'NY'
    geom geometry(MultiPolygon, 4326) NOT NULL
    );
CREATE INDEX ny_state_geom_gist ON ny_state USING GIST (geom);

CREATE TABLE ny_county (
    county_name TEXT PRIMARY KEY,
    geom geometry(MultiPolygon, 4326) NOT NULL
    );
CREATE INDEX ny_county_geom_gist ON ny_county USING GIST (geom);

CREATE TABLE ny_muni (
    id BIGSERIAL PRIMARY KEY,
    muni_name TEXT NOT NULL,
    muni_type TEXT,
    county_name TEXT,
    geom geometry(MultiPolygon, 4326) NOT NULL
    );
CREATE INDEX ny_muni_geom_gist ON ny_muni USING GIST (geom);

CREATE TABLE ny_tax_rate (
    jurisdiction_type TEXT NOT NULL,
    jurisdiction_name TEXT NOT NULL,
    total_rate NUMERIC(8,5) NOT NULL,
    special_zone TEXT,
    effective_from DATE NOT NULL DEFAULT '2025-03-01',
    PRIMARY KEY (jurisdiction_type, jurisdiction_name)
    );


CREATE TABLE orders (
    id BIGSERIAL PRIMARY KEY,
    ts TIMESTAMPTZ NOT NULL,
    subtotal NUMERIC(12,2) NOT NULL,

    lat DOUBLE PRECISION NOT NULL,
    lon DOUBLE PRECISION NOT NULL,
    geom geometry(Point, 4326) NOT NULL,

    in_ny BOOLEAN NOT NULL DEFAULT false,
    in_mctd BOOLEAN NOT NULL DEFAULT false,

    county_name TEXT,
    muni_name TEXT,
    muni_type TEXT,

    state_rate   NUMERIC(8,5) NOT NULL DEFAULT 0,
    county_rate  NUMERIC(8,5) NOT NULL DEFAULT 0,
    city_rate    NUMERIC(8,5) NOT NULL DEFAULT 0,
    special_rate NUMERIC(8,5) NOT NULL DEFAULT 0,

    composite_tax_rate NUMERIC(8,5),
    tax_amount NUMERIC(12,2),
    total_amount NUMERIC(12,2)
    );
CREATE INDEX orders_ts_idx ON orders(ts);
CREATE INDEX orders_geom_gist ON orders USING GIST (geom);

CREATE TABLE seed_status (
    id INT PRIMARY KEY,
    seeded_at TIMESTAMPTZ NOT NULL,
    notes TEXT
);

CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    email TEXT NOT NULL UNIQUE,
    password TEXT NOT NULL
);