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
    @Column(name = "lon")
    @NotNull
    private Double longitude;
    @Column(name = "lat")
    @NotNull
    private Double latitude;
    @NotNull
    @Column(columnDefinition = "geometry(Point, 4326)", name = "geom")
    private Point point;
    @Column
    @NotNull
    private BigDecimal subtotal;
    @Column
    @NotNull
    private BigDecimal compositeTaxRate;
    @Column
    @NotNull
    private BigDecimal taxRate;
    @Column
    @NotNull
    private BigDecimal taxAmount;
    @Column(columnDefinition = "timestamptz", updatable = false, name = "ts")
    @NotNull
    private Instant timestamp;
    @Embedded
    @NotNull
    private TaxBreakDown breakDown;
    @Column
    @NotNull
    private Boolean inNy;
    @Column
    @NotNull
    private Boolean inMctd;
    @Column
    private String muniName;
    @Column
    private String muniType;
    @Column
    @NotNull
    private String countyType;

}
