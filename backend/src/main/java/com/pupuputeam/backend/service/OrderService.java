package com.pupuputeam.backend.service;

import com.pupuputeam.backend.dto.request.OrderCreateRequest;
import com.pupuputeam.backend.dto.request.OrderFilterDto;
import com.pupuputeam.backend.dto.response.OrderResponse;
import com.pupuputeam.backend.mapper.OrderMapper;
import com.pupuputeam.backend.model.JurisdictionSnapshot;
import com.pupuputeam.backend.model.Order;
import com.pupuputeam.backend.model.TaxBreakdown;
import com.pupuputeam.backend.repository.OrderRepository;
import com.pupuputeam.backend.repository.specification.OrderSpecifications;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.pupuputeam.backend.dto.response.OrderImportResponse;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderService {

    private final SpatialResolverService spatial;
    private final TaxCalculationService taxService;
    private final OrderRepository repository;
    private final OrderMapper mapper;
    @Transactional
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
    public Page<OrderResponse> getFilteredOrders(OrderFilterDto filters, Pageable pageable) {

        Specification<Order> spec = Specification
                .where(OrderSpecifications.amountInRange(filters.minAmount(), filters.maxAmount()))
                .and(OrderSpecifications.compositeTaxRateInRange(filters.minTaxRate(), filters.maxTaxRate()))
                .and(OrderSpecifications.timeBetween(filters.startTime(), filters.endTime()))
                .and(OrderSpecifications.cityNameEquals(filters.cityName()))
                .and(OrderSpecifications.countyNameEquals(filters.countyName()))
                .and(OrderSpecifications.muniNameEquals(filters.muniName()));
        Page<Order> orders= repository.findAll(spec, pageable);
        return orders.map(mapper::toResponse);

    }

    @Transactional
    public OrderImportResponse importCsv(MultipartFile file) {
        long start = System.currentTimeMillis();
        try (InputStream is = file.getInputStream()) {
            int imported = repository.importCsv(is);

            long end = System.currentTimeMillis();
            long duration = end - start;
            log.info("CSV import finished. Rows: {}. Time: {} ms ({} sec)",
                    imported,
                    duration,
                    duration / 1000.0
            );

            return new OrderImportResponse(imported);
        } catch (IOException e) {
            throw new IllegalArgumentException("Cannot read CSV file", e);
        }
    }
}