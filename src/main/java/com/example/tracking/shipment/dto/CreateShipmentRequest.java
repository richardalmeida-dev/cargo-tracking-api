package com.example.tracking.shipment.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateShipmentRequest(
        @NotBlank String origin,
        @NotBlank String destination,
        @NotBlank String recipientName
) {
}
