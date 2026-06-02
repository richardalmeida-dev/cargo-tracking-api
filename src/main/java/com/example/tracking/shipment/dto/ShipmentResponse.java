package com.example.tracking.shipment.dto;

import com.example.tracking.shipment.Shipment;
import com.example.tracking.shipment.ShipmentStatus;

import java.time.OffsetDateTime;
import java.util.UUID;

public record ShipmentResponse(
        UUID id,
        String trackingCode,
        ShipmentStatus status,
        String origin,
        String destination,
        String recipientName,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
    public static ShipmentResponse from(Shipment s) {
        return new ShipmentResponse(
                s.getId(),
                s.getTrackingCode(),
                s.getStatus(),
                s.getOrigin(),
                s.getDestination(),
                s.getRecipientName(),
                s.getCreatedAt(),
                s.getUpdatedAt()
        );
    }
}
