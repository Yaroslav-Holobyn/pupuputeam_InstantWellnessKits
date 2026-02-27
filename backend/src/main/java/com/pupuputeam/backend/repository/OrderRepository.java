package com.pupuputeam.backend.repository;

import com.pupuputeam.backend.dto.request.OrderCreateRequest;
import com.pupuputeam.backend.model.Order;
import com.pupuputeam.backend.model.TaxBreakdown;
import com.pupuputeam.backend.model.JurisdictionSnapshot;

public interface OrderRepository {

    Order save(
            OrderCreateRequest request,
            JurisdictionSnapshot snapshot,
            TaxBreakdown breakdown
    );
}