package com.pupuputeam.backend.entity;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.math.BigDecimal;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class TaxBreakDown {
    @NotNull
    private BigDecimal stateRate;
    @NotNull
    private BigDecimal countryRate;
    @NotNull
    private BigDecimal cityRate;
    @NotNull
    private BigDecimal specialRate;
}
