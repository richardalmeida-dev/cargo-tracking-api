package com.example.tracking.shipment.dto;

import com.example.tracking.shipment.ShipmentStatus;
import com.example.tracking.shipment.TrackingEvent;

import java.time.OffsetDateTime;
import java.util.UUID;

public record TrackingEventResponse(
        UUID id,
        ShipmentStatus status,
        String location,
        String description,
        OffsetDateTime occurredAt,
        OffsetDateTime recordedAt
) {
    public static TrackingEventResponse from(TrackingEvent e) {
        return new TrackingEventResponse(
                e.getId(),
                e.getStatus(),
                e.getLocation(),
                e.getDescription(),
                e.getOccurredAt(),
                e.getRecordedAt()
        );
    }
}
