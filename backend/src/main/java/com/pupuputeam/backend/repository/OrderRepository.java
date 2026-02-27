package com.pupuputeam.backend.repository;

import com.pupuputeam.backend.dto.request.OrderCreateRequest;
import com.pupuputeam.backend.model.Order;
import com.pupuputeam.backend.model.TaxBreakdown;
import com.pupuputeam.backend.model.JurisdictionSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface OrderRepository extends CustomOrderRepository, JpaRepository<Order, Long>, JpaSpecificationExecutor<Order> {

}