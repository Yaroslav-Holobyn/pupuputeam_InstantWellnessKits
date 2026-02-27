package com.pupuputeam.backend.dto.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record OrderResponse(
        Long id,
        Instant ts,
        BigDecimal subtotal,

        Double lat,
        Double lon,

        Boolean inNy,
        Boolean inMctd,

        String countyName,
        String muniName,
        String muniType,

        BigDecimal stateRate,
        BigDecimal countyRate,
        BigDecimal cityRate,
        BigDecimal specialRate,

        BigDecimal compositeTaxRate,
        BigDecimal taxAmount,
        BigDecimal totalAmount,

        List<JurisdictionDto> jurisdictions
) {}