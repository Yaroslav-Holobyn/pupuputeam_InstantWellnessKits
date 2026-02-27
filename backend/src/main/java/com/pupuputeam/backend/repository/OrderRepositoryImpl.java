package com.pupuputeam.backend.repository;

import com.pupuputeam.backend.dto.request.OrderCreateRequest;
import com.pupuputeam.backend.model.Order;
import com.pupuputeam.backend.model.TaxBreakdown;
import com.pupuputeam.backend.model.JurisdictionSnapshot;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;

@Repository
@RequiredArgsConstructor
public class OrderRepositoryImpl implements CustomOrderRepository {

    private final JdbcTemplate jdbc;

    @Override
    public Order save(
            OrderCreateRequest request,
            JurisdictionSnapshot snapshot,
            TaxBreakdown breakdown
    ) {

        BigDecimal composite = breakdown.composite();
        BigDecimal taxAmount = request.subtotal().multiply(composite)
                .setScale(2, BigDecimal.ROUND_HALF_UP);

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
                Timestamp.from(request.timestamp() != null ? request.timestamp() : Instant.now()),
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
                .timestamp(request.timestamp())
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
}