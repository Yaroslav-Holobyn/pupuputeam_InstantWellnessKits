package com.pupuputeam.backend.entity;


import jakarta.validation.constraints.NotNull;
import jakarta.persistence.*;
import lombok.*;
import org.locationtech.jts.geom.Polygon;
import java.math.BigDecimal;

@Table (name = "tax_jurisdictions")
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Jurisdiction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NotNull
    @Column
    private String name;
    @NotNull
    @Column
    @Enumerated(EnumType.STRING)
    private JurisdictionsType type;
    @NotNull
    @Column
    private BigDecimal taxRate;
    @NotNull
    @Column(columnDefinition = "geometry(Polygon, 4326)")
    private Polygon boundary;
}
