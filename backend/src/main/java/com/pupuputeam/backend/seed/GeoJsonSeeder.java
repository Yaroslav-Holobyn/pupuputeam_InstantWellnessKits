package com.pupuputeam.backend.seed;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.ClassPathResource;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;

public class GeoJsonSeeder {

    private final ObjectMapper mapper = new ObjectMapper();

    public void seedState(Connection c, String classpathFile) throws Exception {
        try (var st = c.createStatement()) {
            st.executeUpdate("TRUNCATE ny_state;");
        }

        try (InputStream in = new ClassPathResource(classpathFile).getInputStream()) {
            JsonNode root = mapper.readTree(in);

            JsonNode geomNode = null;

            if ("FeatureCollection".equals(text(root, "type")) && root.has("features")) {
                JsonNode f0 = root.get("features").isArray() && root.get("features").size() > 0
                        ? root.get("features").get(0) : null;
                if (f0 != null) geomNode = f0.get("geometry");
            } else if ("Feature".equals(text(root, "type"))) {
                geomNode = root.get("geometry");
            } else if (root.has("coordinates")) {
                geomNode = root;
            }

            if (geomNode == null) return;

            String sql = """
                INSERT INTO ny_state(state_code, geom)
                VALUES ('NY', ST_SetSRID(ST_Multi(ST_MakeValid(ST_GeomFromGeoJSON(?))), 4326))
                """;

            try (PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setString(1, geomNode.toString());
                ps.executeUpdate();
            }
        }
    }

    public void seedCounties(Connection c, String classpathFile) throws Exception {
        try (var st = c.createStatement()) {
            st.executeUpdate("TRUNCATE ny_county;");
        }

        String sql = """
            INSERT INTO ny_county(county_name, county_fips, geom)
            VALUES (?, ?, ST_SetSRID(ST_Multi(ST_MakeValid(ST_GeomFromGeoJSON(?))), 4326))
            """;

        try (PreparedStatement ps = c.prepareStatement(sql);
             InputStream in = new ClassPathResource(classpathFile).getInputStream()) {

            JsonNode root = mapper.readTree(in);
            JsonNode features = root.get("features");
            if (features == null || !features.isArray()) return;

            int batch = 0;
            for (JsonNode f : features) {
                JsonNode props = f.get("properties");
                JsonNode geom = f.get("geometry");
                if (props == null || geom == null) continue;

                String countyName = pick(props, "NAME", "county_name");
                String countyFips = pick(props, "FIPS", "COUNTYFP", "county_fips");

                ps.setString(1, countyName);
                ps.setString(2, countyFips);
                ps.setString(3, geom.toString());
                ps.addBatch();

                batch++;
                if (batch % 1000 == 0) ps.executeBatch();
            }
            ps.executeBatch();
        }
    }

    public void seedMunis(Connection c, String classpathFile) throws Exception {
        try (var st = c.createStatement()) {
            st.executeUpdate("TRUNCATE ny_muni;");
        }

        String sql = """
            INSERT INTO ny_muni(muni_name, muni_type, county_name, fips_code, geom)
            VALUES (?, ?, ?, ?, ST_SetSRID(ST_Multi(ST_MakeValid(ST_GeomFromGeoJSON(?))), 4326))
            """;

        try (PreparedStatement ps = c.prepareStatement(sql);
             InputStream in = new ClassPathResource(classpathFile).getInputStream()) {

            JsonNode root = mapper.readTree(in);
            JsonNode features = root.get("features");
            if (features == null || !features.isArray()) return;

            int batch = 0;
            for (JsonNode f : features) {
                JsonNode props = f.get("properties");
                JsonNode geom = f.get("geometry");
                if (props == null || geom == null) continue;

                String name = pick(props, "NAME", "MUNI_NAME", "muni_name");
                String muniType = pick(props, "MUNI_TYPE", "TYPE", "muni_type");
                String countyName = pick(props, "COUNTY", "county_name");
                String fips = pick(props, "FIPS_CODE", "FIPS", "fips_code");

                ps.setString(1, name);
                ps.setString(2, muniType);
                ps.setString(3, countyName);
                ps.setString(4, fips);
                ps.setString(5, geom.toString());
                ps.addBatch();

                batch++;
                if (batch % 1000 == 0) ps.executeBatch();
            }
            ps.executeBatch();
        }
    }

    private static String pick(JsonNode props, String... keys) {
        for (String k : keys) {
            JsonNode n = props.get(k);
            if (n != null && !n.isNull()) {
                String v = n.asText();
                if (v != null && !v.isBlank()) return v;
            }
        }
        return null;
    }

    private static String text(JsonNode node, String key) {
        JsonNode n = node.get(key);
        return (n == null || n.isNull()) ? null : n.asText();
    }
}