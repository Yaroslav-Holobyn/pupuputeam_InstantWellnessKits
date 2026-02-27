package com.pupuputeam.backend.controller;

import com.pupuputeam.backend.dto.request.OrderCreateRequest;
import com.pupuputeam.backend.dto.request.OrderFilterDto;
import com.pupuputeam.backend.dto.response.OrderResponse;
import com.pupuputeam.backend.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService service;

    @PostMapping
    public OrderResponse create(@Valid @RequestBody OrderCreateRequest request) {
        return service.create(request);
    }

    @GetMapping
    public Page<OrderResponse> get(OrderFilterDto filters,
                                   @PageableDefault(size = 20) Pageable pageable) {
        return service.getFilteredOrders(filters, pageable);
    }
}