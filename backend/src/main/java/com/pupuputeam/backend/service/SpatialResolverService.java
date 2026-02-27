package com.pupuputeam.backend.service;

import com.pupuputeam.backend.model.JurisdictionSnapshot;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SpatialResolverService {

    private final JdbcTemplate jdbc;

    public JurisdictionSnapshot resolve(double lat, double lon) {
        return jdbc.queryForObject("""
        
                        WITH p AS (
             SELECT ST_SetSRID(ST_MakePoint(?, ?), 4326) AS geom
           ),
           muni_hit AS (
             SELECT m.muni_name, m.muni_type
             FROM ny_muni m, p
             WHERE ST_Intersects(m.geom, p.geom)
             ORDER BY ST_Area(m.geom) ASC
             LIMIT 1
           ),
           hit AS (
             SELECT
               EXISTS (SELECT 1 FROM ny_state s, p WHERE ST_Intersects(s.geom, p.geom)) AS in_ny,
               (SELECT c.county_name FROM ny_county c, p WHERE ST_Intersects(c.geom, p.geom) LIMIT 1) AS county_name,
               (SELECT muni_name FROM muni_hit) AS muni_name,
               (SELECT muni_type FROM muni_hit) AS muni_type
           )
           SELECT
             h.in_ny,
             CASE
               WHEN h.county_name IN (
                 'Nassau','Suffolk','Westchester','Rockland',
                 'Putnam','Dutchess','Orange',
                 'Bronx','Kings','New York','Queens','Richmond'
               ) THEN true
               ELSE false
             END AS in_mctd,
             h.county_name,
             h.muni_name,
             h.muni_type,
             tr_county.total_rate AS county_total_rate,
             tr_city.total_rate   AS city_total_rate
           FROM hit h
           LEFT JOIN ny_tax_rate tr_county
             ON (
                   (tr_county.jurisdiction_type = 'COUNTY'
                    AND tr_county.jurisdiction_name = h.county_name)
                OR (tr_county.jurisdiction_type = 'NYC'
                    AND h.county_name IN ('Bronx','Kings','New York','Queens','Richmond'))
                )
           LEFT JOIN ny_tax_rate tr_city
             ON tr_city.jurisdiction_type = 'CITY'
            AND tr_city.jurisdiction_name = h.muni_name
        """,
                (rs, i) -> new JurisdictionSnapshot(
                        rs.getBoolean("in_ny"),
                        rs.getBoolean("in_mctd"),
                        rs.getString("county_name"),
                        rs.getString("muni_name"),
                        rs.getString("muni_type"),
                        rs.getBigDecimal("county_total_rate"),
                        rs.getBigDecimal("city_total_rate")
                ),
                lon, lat
        );
    }
}