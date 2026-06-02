package com.example.tracking.error;

import java.util.UUID;

public class ShipmentNotFoundException extends RuntimeException {

    private final UUID id;

    public ShipmentNotFoundException(UUID id) {
        super("Shipment not found: " + id);
        this.id = id;
    }

    public UUID getId() {
        return id;
    }
}
