package com.pupuputeam.backend.seed;

import org.postgresql.PGConnection;
import org.postgresql.copy.CopyManager;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import javax.sql.DataSource;
import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.*;

@Configuration
public class SeedRunner {

    private static final Date EFFECTIVE_FROM = Date.valueOf("2025-03-01");

    @Bean
    public ApplicationRunner seedOnFirstRun(DataSource dataSource) {
        return args -> {
            try (Connection c = dataSource.getConnection()) {
                c.setAutoCommit(false);

                if (!isSeeded(c, 1)) {
                    GeoJsonSeeder geo = new GeoJsonSeeder();
                    geo.seedState(c, "seed/state.json");
                    geo.seedCounties(c, "seed/counties.json");
                    geo.seedMunis(c, "seed/cities_towns.json");
                    markSeeded(c, 1, "seed polygons: state+counties+munis");
                }

                if (!isSeeded(c, 2)) {
                    seedRatesFromCsv(c);
                    markSeeded(c, 2, "seed rates: ny_tax_rate from ny_pub718_rates.csv");
                }

                c.commit();
                System.out.println("✅ SEED COMPLETED");
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        };
    }

    private void seedRatesFromCsv(Connection c) throws Exception {
        try (Statement st = c.createStatement()) {
            st.executeUpdate("TRUNCATE ny_tax_rate_raw;");
            st.executeUpdate("TRUNCATE ny_tax_rate;");
        }

        copyCsvViaPgCopy(
                c,
                "seed/ny_pub718_rates.csv",
                "ny_tax_rate_raw(jurisdiction, tax_rate, reporting_code, special_zone)"
        );

        try (PreparedStatement ps = c.prepareStatement("""
            INSERT INTO ny_tax_rate(jurisdiction_type, jurisdiction_name, total_rate, reporting_code, special_zone, effective_from)
            VALUES (?, ?, ?, ?, ?, ?)
        """)) {
            try (Statement st = c.createStatement();
                 ResultSet rs = st.executeQuery("""
                    SELECT jurisdiction, tax_rate, reporting_code, special_zone
                    FROM ny_tax_rate_raw
                    WHERE jurisdiction IS NOT NULL AND tax_rate IS NOT NULL
                 """)) {

                int batch = 0;
                while (rs.next()) {
                    String jurisdictionRaw = rs.getString(1);
                    String taxRatePercent = rs.getString(2);
                    String reportingCode = rs.getString(3);
                    String specialZone = rs.getString(4);

                    String j = normalizeJurisdiction(jurisdictionRaw);

                    String type;
                    String name;

                    if (j.equalsIgnoreCase("New York City")) {
                        type = "NYC";
                        name = "New York City";
                    } else if (j.toLowerCase().endsWith("(city)")) {
                        type = "CITY";
                        name = stripCitySuffix(j);
                    } else {
                        type = "COUNTY";
                        name = j;
                    }

                    BigDecimal rate = percentToFraction(taxRatePercent);

                    ps.setString(1, type);
                    ps.setString(2, name);
                    ps.setBigDecimal(3, rate);
                    ps.setString(4, blankToNull(reportingCode));
                    ps.setString(5, blankToNull(specialZone));
                    ps.setDate(6, EFFECTIVE_FROM);
                    ps.addBatch();

                    batch++;
                    if (batch % 500 == 0) ps.executeBatch();
                }
                ps.executeBatch();
            }
        }

        try (Statement st = c.createStatement()) {
            st.executeUpdate("TRUNCATE ny_tax_rate_raw;");
        }
    }

    private static String normalizeJurisdiction(String s) {
        String t = s == null ? null : s.trim();
        if (t == null) return null;
        while (t.startsWith("*")) t = t.substring(1).trim();
        return t;
    }

    private static String stripCitySuffix(String s) {
        String t = s.trim();
        if (t.toLowerCase().endsWith("(city)")) {
            return t.substring(0, t.length() - "(city)".length()).trim();
        }
        return t;
    }

    private static BigDecimal percentToFraction(String percentText) {
        BigDecimal p = new BigDecimal(percentText.trim());
        return p.divide(new BigDecimal("100.0")).setScale(5, java.math.RoundingMode.HALF_UP);
    }

    private static String blankToNull(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    private boolean isSeeded(Connection c, int id) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement("SELECT 1 FROM seed_status WHERE id=?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    private void markSeeded(Connection c, int id, String notes) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(
                "INSERT INTO seed_status(id, seeded_at, notes) VALUES (?, now(), ?) " +
                        "ON CONFLICT (id) DO UPDATE SET seeded_at=now(), notes=EXCLUDED.notes"
        )) {
            ps.setInt(1, id);
            ps.setString(2, notes);
            ps.executeUpdate();
        }
    }

    private void copyCsvViaPgCopy(Connection c, String classpathFile, String tableAndCols) throws Exception {
        PGConnection pg = c.unwrap(PGConnection.class);
        CopyManager copyManager = pg.getCopyAPI();

        ClassPathResource r = new ClassPathResource(classpathFile);
        try (InputStream in = r.getInputStream()) {
            String sql = "COPY " + tableAndCols + " FROM STDIN WITH (FORMAT csv, HEADER true, ENCODING 'UTF8')";
            copyManager.copyIn(sql, in);
        }
    }
}