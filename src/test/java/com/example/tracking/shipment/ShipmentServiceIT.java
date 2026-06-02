package com.example.tracking.shipment;

import com.example.tracking.AbstractIntegrationTest;
import com.example.tracking.error.InvalidTransitionException;
import com.example.tracking.shipment.dto.AddTrackingEventRequest;
import com.example.tracking.shipment.dto.CreateShipmentRequest;
import com.example.tracking.shipment.dto.ShipmentResponse;
import com.example.tracking.shipment.dto.TimelineResponse;
import com.example.tracking.shipment.dto.TrackingEventResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ShipmentServiceIT extends AbstractIntegrationTest {

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
    void validTransition_persistsEventsAndUpdatesStatus() {
        ShipmentResponse created = service.create(
                new CreateShipmentRequest("São Paulo", "Curitiba", "Alice"));
        UUID id = created.id();
        assertThat(created.status()).isEqualTo(ShipmentStatus.CREATED);

        OffsetDateTime t1 = OffsetDateTime.now().minusMinutes(2);
        OffsetDateTime t2 = OffsetDateTime.now().minusMinutes(1);

        TrackingEventResponse e1 = service.addEvent(id,
                new AddTrackingEventRequest(ShipmentStatus.PICKED_UP, "Warehouse SP", null, t1));
        TrackingEventResponse e2 = service.addEvent(id,
                new AddTrackingEventRequest(ShipmentStatus.IN_TRANSIT, "Highway BR-116", null, t2));

        assertThat(e1.status()).isEqualTo(ShipmentStatus.PICKED_UP);
        assertThat(e2.status()).isEqualTo(ShipmentStatus.IN_TRANSIT);

        Shipment reloaded = shipmentRepository.findById(id).orElseThrow();
        assertThat(reloaded.getStatus()).isEqualTo(ShipmentStatus.IN_TRANSIT);

        TimelineResponse timeline = service.getTimeline(id);
        assertThat(timeline.shipmentId()).isEqualTo(id);
        assertThat(timeline.currentStatus()).isEqualTo(ShipmentStatus.IN_TRANSIT);
        assertThat(timeline.events()).hasSize(2);
        assertThat(timeline.events().get(0).status()).isEqualTo(ShipmentStatus.PICKED_UP);
        assertThat(timeline.events().get(1).status()).isEqualTo(ShipmentStatus.IN_TRANSIT);
    }

    @Test
    void invalidTransition_doesNotChangeStatusOrRecordEvent() {
        ShipmentResponse created = service.create(
                new CreateShipmentRequest("São Paulo", "Curitiba", "Bob"));
        UUID id = created.id();
        OffsetDateTime occurredAt = OffsetDateTime.now().minusMinutes(1);

        assertThatThrownBy(() -> service.addEvent(id,
                new AddTrackingEventRequest(ShipmentStatus.DELIVERED, "Anywhere", null, occurredAt)))
                .isInstanceOf(InvalidTransitionException.class);

        Shipment reloaded = shipmentRepository.findById(id).orElseThrow();
        assertThat(reloaded.getStatus()).isEqualTo(ShipmentStatus.CREATED);

        TimelineResponse timeline = service.getTimeline(id);
        assertThat(timeline.currentStatus()).isEqualTo(ShipmentStatus.CREATED);
        assertThat(timeline.events()).isEmpty();
    }
}
