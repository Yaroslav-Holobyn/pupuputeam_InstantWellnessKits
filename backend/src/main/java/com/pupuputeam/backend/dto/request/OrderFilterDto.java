package com.pupuputeam.backend.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.time.Instant;

public record OrderFilterDto(
        BigDecimal minAmount,
        @PositiveOrZero(message = "max amount cannot be negative")
        BigDecimal maxAmount,
        @PastOrPresent(message = "start time cannot be in future")
        Instant startTime,
        Instant endTime,
        String countyName,
        String muniName,
        BigDecimal minTaxRate,
        @PositiveOrZero(message = "max tax rate cannot be negative")
        @DecimalMax(value = "1.0",message = "tax rate cannot be above than 100%")
        BigDecimal maxTaxRate
){
}
