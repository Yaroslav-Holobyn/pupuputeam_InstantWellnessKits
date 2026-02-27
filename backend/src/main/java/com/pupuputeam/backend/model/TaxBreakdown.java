package com.pupuputeam.backend.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class TaxBreakdown {

    private BigDecimal stateRate;
    private BigDecimal countyRate;
    private BigDecimal cityRate;
    private BigDecimal specialRate;

    public BigDecimal composite() {
        return stateRate
                .add(countyRate)
                .add(cityRate)
                .add(specialRate);
    }
}