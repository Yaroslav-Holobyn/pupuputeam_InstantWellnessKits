package com.pupuputeam.backend.mapper;

import com.pupuputeam.backend.dto.response.JurisdictionDto;
import com.pupuputeam.backend.dto.response.OrderResponse;
import com.pupuputeam.backend.model.Order;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Component
public class OrderMapper {

    public OrderResponse toResponse(Order o) {

        List<JurisdictionDto> jurisdictions = new ArrayList<>();

        jurisdictions.add(new JurisdictionDto(
                "STATE",
                "New York State",
                o.getStateRate()
        ));

        if (o.getCountyRate().compareTo(BigDecimal.ZERO) > 0) {
            jurisdictions.add(new JurisdictionDto(
                    "COUNTY",
                    o.getCountyName(),
                    o.getCountyRate()
            ));
        }

        if (o.getCityRate().compareTo(BigDecimal.ZERO) > 0) {
            jurisdictions.add(new JurisdictionDto(
                    "CITY",
                    o.getMuniName(),
                    o.getCityRate()
            ));
        }

        if (o.getSpecialRate().compareTo(BigDecimal.ZERO) > 0) {
            jurisdictions.add(new JurisdictionDto(
                    "SPECIAL",
                    "MCTD",
                    o.getSpecialRate()
            ));
        }

        return new OrderResponse(
                o.getId(),
                o.getTimestamp(),
                o.getSubtotal(),
                o.getLatitude(),
                o.getLongitude(),
                o.getInNy(),
                o.getInMctd(),
                o.getCountyName(),
                o.getMuniName(),
                o.getMuniType(),
                o.getStateRate(),
                o.getCountyRate(),
                o.getCityRate(),
                o.getSpecialRate(),
                o.getCompositeTaxRate(),
                o.getTaxAmount(),
                o.getTotalAmount(),
                jurisdictions
        );
    }
}