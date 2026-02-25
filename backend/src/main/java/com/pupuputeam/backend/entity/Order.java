package com.pupuputeam.backend.entity;
import com.pupuputeam.backend.dto.JurisdictionSnapshotDto;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column
    @NotNull
    private Double longitude;
    @Column
    @NotNull
    private Double latitude;
    @Column
    @NotNull
    private BigDecimal subtotal;
    @Column
    @NotNull
    private BigDecimal compositeTaxRate;
    @Column
    @NotNull
    private BigDecimal taxRate;
    @Column(columnDefinition = "timestamptz", updatable = false)
    @NotNull
    private Instant timestamp;
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    @NotNull
    private List<JurisdictionSnapshotDto> jurisdictions;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    @NotNull
    private TaxBreakDown breakDown;
}
