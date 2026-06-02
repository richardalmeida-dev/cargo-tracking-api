package com.example.tracking.shipment;

import com.example.tracking.AbstractIntegrationTest;
import com.example.tracking.shipment.dto.AddTrackingEventRequest;
import com.example.tracking.shipment.dto.CreateShipmentRequest;
import com.example.tracking.shipment.dto.ShipmentResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.OptimisticLockingFailureException;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ShipmentOptimisticLockingIT extends AbstractIntegrationTest {

    @Autowired
    ShipmentService service;

    @Autowired
    ShipmentRepository shipmentRepository;

    @Autowired
    TrackingEventRepository trackingEventRepository;

    @BeforeEach
    void cleanup() {
        trackingEventRepository.deleteAll();
        shipmentRepository.deleteAll();
    }

    @Test
    void concurrentUpdate_onStaleCopy_throwsOptimisticLockingFailure() {
        ShipmentResponse created = service.create(
                new CreateShipmentRequest("São Paulo", "Curitiba", "Carol"));
        UUID id = created.id();

        Shipment stale = shipmentRepository.findById(id).orElseThrow();
        assertThat(stale.getStatus()).isEqualTo(ShipmentStatus.CREATED);
        assertThat(stale.getVersion()).isEqualTo(0L);

        service.addEvent(id, new AddTrackingEventRequest(
                ShipmentStatus.EXCEPTION, "Warehouse SP", null, OffsetDateTime.now().minusMinutes(1)));

        Shipment fresh = shipmentRepository.findById(id).orElseThrow();
        assertThat(fresh.getStatus()).isEqualTo(ShipmentStatus.EXCEPTION);
        assertThat(fresh.getVersion()).isEqualTo(1L);

        assertThatThrownBy(() -> shipmentRepository.saveAndFlush(stale))
                .isInstanceOf(OptimisticLockingFailureException.class);
    }
}
