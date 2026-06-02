package com.example.tracking.shipment.dto;

import com.example.tracking.shipment.ShipmentStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;

import java.time.OffsetDateTime;

public record AddTrackingEventRequest(
        @NotNull ShipmentStatus status,
        String location,
        String description,
        @NotNull @PastOrPresent OffsetDateTime occurredAt
) {
}
