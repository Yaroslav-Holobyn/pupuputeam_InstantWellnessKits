package com.pupuputeam.backend.dto.response;

import java.math.BigDecimal;

public record JurisdictionDto(
        String type,
        String name,
        BigDecimal rate
) {}