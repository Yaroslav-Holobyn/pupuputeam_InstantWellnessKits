package com.pupuputeam.backend.repository;

import com.pupuputeam.backend.dto.request.OrderCreateRequest;
import com.pupuputeam.backend.model.JurisdictionSnapshot;
import com.pupuputeam.backend.model.Order;
import com.pupuputeam.backend.model.TaxBreakdown;

public interface CustomOrderRepository {
    Order save(
            OrderCreateRequest request,
            JurisdictionSnapshot snapshot,
            TaxBreakdown breakdown
    );
}
