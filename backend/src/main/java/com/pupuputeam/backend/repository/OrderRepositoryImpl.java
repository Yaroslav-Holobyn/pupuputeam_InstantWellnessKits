package com.pupuputeam.backend.repository;

import com.pupuputeam.backend.dto.request.OrderCreateRequest;
import com.pupuputeam.backend.model.JurisdictionSnapshot;
import com.pupuputeam.backend.model.Order;
import com.pupuputeam.backend.model.TaxBreakdown;
import lombok.RequiredArgsConstructor;
import org.postgresql.PGConnection;
import org.postgresql.copy.CopyManager;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.Instant;

@Repository
@RequiredArgsConstructor
public class OrderRepositoryImpl implements CustomOrderRepository {

    private final JdbcTemplate jdbc;
    private final DataSource dataSource;

    @Override
    public Order save(
            OrderCreateRequest request,
            JurisdictionSnapshot snapshot,
            TaxBreakdown breakdown
    ) {

        Instant ts = (request.timestamp() != null) ? request.timestamp() : Instant.now();

        BigDecimal composite = breakdown.composite();
        BigDecimal taxAmount = request.subtotal()
                .multiply(composite)
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal total = request.subtotal().add(taxAmount);

        Long id = jdbc.queryForObject("""
                INSERT INTO orders (
                    ts,
                    subtotal,
                    lat,
                    lon,
                    geom,
                    in_ny,
                    in_mctd,
                    county_name,
                    muni_name,
                    muni_type,
                    state_rate,
                    county_rate,
                    city_rate,
                    special_rate,
                    composite_tax_rate,
                    tax_amount,
                    total_amount
                )
                VALUES (
                    ?, ?, ?, ?,
                    ST_SetSRID(ST_MakePoint(?, ?), 4326),
                    ?, ?, ?, ?, ?,
                    ?, ?, ?, ?, ?, ?, ?
                )
                RETURNING id
                """,
                Long.class,
                Timestamp.from(ts),
                request.subtotal(),
                request.latitude(),
                request.longitude(),
                request.longitude(),
                request.latitude(),
                snapshot.isInNy(),
                snapshot.isInMctd(),
                snapshot.getCountyName(),
                snapshot.getMuniName(),
                snapshot.getMuniType(),
                breakdown.getStateRate(),
                breakdown.getCountyRate(),
                breakdown.getCityRate(),
                breakdown.getSpecialRate(),
                composite,
                taxAmount,
                total
        );

        return Order.builder()
                .id(id)
                .subtotal(request.subtotal())
                .latitude(request.latitude())
                .longitude(request.longitude())
                .timestamp(ts)
                .inNy(snapshot.isInNy())
                .inMctd(snapshot.isInMctd())
                .countyName(snapshot.getCountyName())
                .muniName(snapshot.getMuniName())
                .muniType(snapshot.getMuniType())
                .stateRate(breakdown.getStateRate())
                .countyRate(breakdown.getCountyRate())
                .cityRate(breakdown.getCityRate())
                .specialRate(breakdown.getSpecialRate())
                .compositeTaxRate(composite)
                .taxAmount(taxAmount)
                .totalAmount(total)
                .build();
    }

    @Override
    public int importCsv(InputStream csvStream) {
        try (Connection c = dataSource.getConnection()) {
            c.setAutoCommit(false);

            try (Statement st = c.createStatement()) {
                st.execute("""
                CREATE TEMP TABLE order_import_raw (
                    src_id     BIGINT,
                    lon        DOUBLE PRECISION,
                    lat        DOUBLE PRECISION,
                    ts_text    TEXT,
                    subtotal   NUMERIC
                ) ON COMMIT DROP
            """);
            }

            CopyManager copy = c.unwrap(PGConnection.class).getCopyAPI();
            copy.copyIn("""
            COPY order_import_raw (src_id, lon, lat, ts_text, subtotal)
            FROM STDIN WITH (FORMAT csv, HEADER true)
        """, csvStream);

            final String insertSql = """
            WITH cleaned AS (
              SELECT
                r.src_id,
                r.lon,
                r.lat,
                r.subtotal,
                (substring(r.ts_text from 1 for 19))::timestamp AS ts_local
              FROM order_import_raw r
            ),
            pts AS (
              SELECT
                c.*,
                ST_SetSRID(ST_MakePoint(c.lon, c.lat), 4326) AS geom
              FROM cleaned c
            ),
            resolved AS (
              SELECT
                p.*,
                (s.state_code IS NOT NULL) AS in_ny,
                co.county_name,
                mu.muni_name,
                mu.muni_type
              FROM pts p
              LEFT JOIN ny_state s
                ON s.geom && p.geom
               AND ST_Contains(s.geom, p.geom)

              LEFT JOIN LATERAL (
                SELECT county_name
                FROM ny_county co
                WHERE co.geom && p.geom
                  AND ST_Contains(co.geom, p.geom)
                LIMIT 1
              ) co ON true

              LEFT JOIN LATERAL (
                SELECT m.muni_name, m.muni_type
                FROM ny_muni_subdivided m
                WHERE m.county_name = co.county_name
                  AND m.geom && p.geom
                  AND ST_Contains(m.geom, p.geom)
                LIMIT 1
              ) mu ON true
            ),
            flags AS (
              SELECT
                r.*,
                CASE
                  WHEN r.county_name IN (
                    'Nassau','Suffolk','Westchester','Rockland',
                    'Putnam','Dutchess','Orange',
                    'Bronx','Kings','New York','Queens','Richmond'
                  ) THEN true
                  ELSE false
                END AS in_mctd,
                CASE
                  WHEN r.county_name IN ('Bronx','Kings','New York','Queens','Richmond')
                  THEN true
                  ELSE false
                END AS is_nyc
              FROM resolved r
            ),
            rates AS (
              SELECT
                f.*,
                tr_county.total_rate AS county_total_rate,
                tr_city.total_rate   AS city_total_rate
              FROM flags f
              LEFT JOIN ny_tax_rate tr_county
                ON (
                      (tr_county.jurisdiction_type = 'COUNTY'
                       AND tr_county.jurisdiction_name = f.county_name)
                   OR (tr_county.jurisdiction_type = 'NYC'
                       AND f.county_name IN ('Bronx','Kings','New York','Queens','Richmond'))
                   )
              LEFT JOIN ny_tax_rate tr_city
                ON tr_city.jurisdiction_type = 'CITY'
               AND tr_city.jurisdiction_name = f.muni_name
            ),
            computed AS (
              SELECT
                r.*,

                (r.ts_local AT TIME ZONE 'America/New_York') AS ts_utc,

                CASE WHEN r.in_ny THEN 0.04000::numeric ELSE 0::numeric END AS state_rate,
                CASE WHEN r.in_ny AND r.in_mctd THEN 0.00375::numeric ELSE 0::numeric END AS special_rate,

                CASE
                  WHEN NOT r.in_ny THEN 0::numeric
                  WHEN r.is_nyc THEN 0::numeric
                  WHEN r.county_total_rate IS NULL THEN 0::numeric
                  ELSE GREATEST(r.county_total_rate - 0.04375::numeric, 0::numeric)
                END AS county_rate,

                CASE
                  WHEN NOT r.in_ny THEN 0::numeric
                  WHEN r.is_nyc THEN
                    GREATEST(COALESCE(r.county_total_rate, 0) - 0.04375::numeric, 0::numeric)
                  ELSE
                    GREATEST(COALESCE(r.city_total_rate, 0) - COALESCE(r.county_total_rate, 0), 0::numeric)
                END AS city_rate
              FROM rates r
            ),
            final_calc AS (
              SELECT
                c.*,
                (c.state_rate + c.county_rate + c.city_rate + c.special_rate) AS composite_tax_rate,
                ROUND(c.subtotal * (c.state_rate + c.county_rate + c.city_rate + c.special_rate), 2) AS tax_amount,
                (c.subtotal + ROUND(c.subtotal * (c.state_rate + c.county_rate + c.city_rate + c.special_rate), 2)) AS total_amount
              FROM computed c
            )
            INSERT INTO orders (
              ts, subtotal, lat, lon, geom,
              in_ny, in_mctd,
              county_name, muni_name, muni_type,
              state_rate, county_rate, city_rate, special_rate,
              composite_tax_rate, tax_amount, total_amount
            )
            SELECT
              f.ts_utc,
              f.subtotal,
              f.lat,
              f.lon,
              f.geom,
              f.in_ny,
              f.in_mctd,
              f.county_name,
              f.muni_name,
              f.muni_type,
              f.state_rate,
              f.county_rate,
              f.city_rate,
              f.special_rate,
              f.composite_tax_rate,
              f.tax_amount,
              f.total_amount
            FROM final_calc f
            ORDER BY f.src_id
        """;

            int inserted;
            try (Statement st = c.createStatement()) {
                st.execute("SET LOCAL work_mem = '256MB'");
                inserted = st.executeUpdate(insertSql);
            }

            c.commit();
            return inserted;
        } catch (Exception e) {
            throw new IllegalStateException("CSV import failed", e);
        }
    }

}