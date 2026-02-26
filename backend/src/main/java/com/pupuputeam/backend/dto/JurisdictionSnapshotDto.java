package com.pupuputeam.backend.dto;

import com.pupuputeam.backend.entity.JurisdictionsType;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record JurisdictionSnapshotDto (
        @NotNull
        String name,
        @NotNull
        BigDecimal taxRate,
        @NotNull
        JurisdictionsType type
){}
