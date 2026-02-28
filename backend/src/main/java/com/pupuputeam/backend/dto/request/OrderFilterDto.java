package com.pupuputeam.backend.dto.request;

import java.math.BigDecimal;
import java.time.Instant;

public record OrderFilterDto(
        BigDecimal minAmount,
        BigDecimal maxAmount,
        Instant startTime,
        Instant endTime,
        String countyName,
        String muniName,
        BigDecimal minTaxRate,
        BigDecimal maxTaxRate
){
}
