package com.pupuputeam.backend.service;

import com.pupuputeam.backend.dto.request.OrderCreateRequest;
import com.pupuputeam.backend.dto.response.OrderResponse;
import com.pupuputeam.backend.mapper.OrderMapper;
import com.pupuputeam.backend.model.JurisdictionSnapshot;
import com.pupuputeam.backend.model.Order;
import com.pupuputeam.backend.model.TaxBreakdown;
import com.pupuputeam.backend.repository.OrderRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {

    private final SpatialResolverService spatial;
    private final TaxCalculationService taxService;
    private final OrderRepository repository;
    private final OrderMapper mapper;

    public OrderResponse create(OrderCreateRequest request) {

        JurisdictionSnapshot snapshot =
                spatial.resolve(request.latitude(), request.longitude());

        log.info("SNAPSHOT: inNy={}, inMctd={}, county={}, countyTotal={}, muni={}, muniType={}, cityTotal={}",
                snapshot.isInNy(),
                snapshot.isInMctd(),
                snapshot.getCountyName(),
                snapshot.getCountyTotalRate(),
                snapshot.getMuniName(),
                snapshot.getMuniType(),
                snapshot.getCityTotalRate()
        );

        TaxBreakdown breakdown =
                taxService.calculate(snapshot);

        Order order = repository.save(
                request,
                snapshot,
                breakdown
        );

        return mapper.toResponse(order);
    }
}