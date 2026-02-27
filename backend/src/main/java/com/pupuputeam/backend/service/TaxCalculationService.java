package com.pupuputeam.backend.service;

import com.pupuputeam.backend.model.JurisdictionSnapshot;
import com.pupuputeam.backend.model.TaxBreakdown;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class TaxCalculationService {

    private static final BigDecimal STATE_RATE = new BigDecimal("0.04000");
    private static final BigDecimal MCTD_RATE = new BigDecimal("0.00375");

    public TaxBreakdown calculate(JurisdictionSnapshot snapshot) {
        if (!snapshot.isInNy()) return new TaxBreakdown(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);

        BigDecimal state = STATE_RATE;
        BigDecimal special = snapshot.isInMctd() ? MCTD_RATE : BigDecimal.ZERO;

        BigDecimal county = BigDecimal.ZERO;
        BigDecimal city = BigDecimal.ZERO;

        boolean isNyc = isNyc(snapshot.getCountyName());

        if (isNyc) {
            if (snapshot.getCountyTotalRate() != null) {
                city = snapshot.getCountyTotalRate()
                        .subtract(state)
                        .subtract(special)
                        .max(BigDecimal.ZERO);
            }
        } else {
            if (snapshot.getCountyTotalRate() != null) {
                county = snapshot.getCountyTotalRate()
                        .subtract(state)
                        .subtract(special)
                        .max(BigDecimal.ZERO);
            }

            if (snapshot.getCityTotalRate() != null && snapshot.getCountyTotalRate() != null) {
                city = snapshot.getCityTotalRate()
                        .subtract(snapshot.getCountyTotalRate())
                        .max(BigDecimal.ZERO);
            }
        }

        return new TaxBreakdown(state, county, city, special);
    }

    private boolean isNyc(String county) {
        if (county == null) return false;
        return switch (county) {
            case "Bronx", "Kings", "New York", "Queens", "Richmond" -> true;
            default -> false;
        };
    }
}