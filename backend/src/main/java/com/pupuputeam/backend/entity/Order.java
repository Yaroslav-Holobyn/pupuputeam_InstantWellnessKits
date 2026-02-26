package com.pupuputeam.backend.entity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.locationtech.jts.geom.Point;
import java.math.BigDecimal;
import java.time.Instant;
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

    @NotNull
    @Column(name = "ts", columnDefinition = "timestamptz", updatable = false)
    private Instant timestamp;

    @NotNull
    @Column(name = "subtotal", precision = 12, scale = 2)
    private BigDecimal subtotal;

    @NotNull
    @Column(name = "lat")
    private Double latitude;

    @NotNull
    @Column(name = "lon")
    private Double longitude;

    @NotNull
    @Column(name = "geom", columnDefinition = "geometry(Point, 4326)")
    private Point point;

    @NotNull
    @Column(name = "in_ny")
    private Boolean inNy;

    @NotNull
    @Column(name = "in_mctd")
    private Boolean inMctd;

    @Column(name = "county_name")
    private String countyName;

    @Column(name = "muni_name")
    private String muniName;

    @Column(name = "muni_type")
    private String muniType;

    @NotNull
    @Column(name = "state_rate", precision = 8, scale = 5)
    private BigDecimal stateRate;

    @NotNull
    @Column(name = "county_rate", precision = 8, scale = 5)
    private BigDecimal countyRate;

    @NotNull
    @Column(name = "city_rate", precision = 8, scale = 5)
    private BigDecimal cityRate;

    @NotNull
    @Column(name = "special_rate", precision = 8, scale = 5)
    private BigDecimal specialRate;

    @NotNull
    @Column(name = "composite_tax_rate", precision = 8, scale = 5)
    private BigDecimal compositeTaxRate;

    @NotNull
    @Column(name = "tax_amount", precision = 12, scale = 2)
    private BigDecimal taxAmount;

    @NotNull
    @Column(name = "total_amount", precision = 12, scale = 2)
    private BigDecimal totalAmount;
}
