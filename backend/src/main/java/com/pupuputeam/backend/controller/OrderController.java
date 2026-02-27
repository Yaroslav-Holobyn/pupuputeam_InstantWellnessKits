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
import com.pupuputeam.backend.dto.response.OrderImportResponse;
import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService service;

    @PostMapping
    public OrderResponse create(@Valid @RequestBody OrderCreateRequest request) {
        return service.create(request);
    }

    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public OrderImportResponse importCsv(@RequestPart("file") MultipartFile file) {
        return service.importCsv(file);
    }

    @GetMapping
    public Page<OrderResponse> get(OrderFilterDto filters,
                                   @PageableDefault(size = 20) Pageable pageable) {
        return service.getFilteredOrders(filters, pageable);
    }
}