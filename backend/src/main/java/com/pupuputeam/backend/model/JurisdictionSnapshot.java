package com.pupuputeam.backend.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class JurisdictionSnapshot {

    private boolean inNy;
    private boolean inMctd;
    private String countyName;
    private String muniName;
    private String muniType;
    private BigDecimal countyTotalRate;
    private BigDecimal cityTotalRate;
}