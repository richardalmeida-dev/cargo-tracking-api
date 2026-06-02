package com.example.tracking.shipment;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ShipmentRepository extends JpaRepository<Shipment, UUID> {

    Page<Shipment> findByStatus(ShipmentStatus status, Pageable pageable);
}
