package com.pupuputeam.backend.controller;

import com.pupuputeam.backend.dto.request.OrderCreateRequest;
import com.pupuputeam.backend.dto.response.OrderResponse;
import com.pupuputeam.backend.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService service;

    @PostMapping
    public OrderResponse create(@Valid @RequestBody OrderCreateRequest request) {
        return service.create(request);
    }
}