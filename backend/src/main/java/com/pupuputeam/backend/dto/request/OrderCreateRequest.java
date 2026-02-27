package com.pupuputeam.backend.dto.request;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.Instant;

public record OrderCreateRequest(
        @NotNull Double latitude,
        @NotNull Double longitude,
        @NotNull BigDecimal subtotal,
        Instant timestamp
) {}