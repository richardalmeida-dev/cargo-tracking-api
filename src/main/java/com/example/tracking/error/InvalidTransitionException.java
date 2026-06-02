package com.example.tracking.error;

import com.example.tracking.shipment.ShipmentStatus;

public class InvalidTransitionException extends RuntimeException {

    private final ShipmentStatus from;
    private final ShipmentStatus to;

    public InvalidTransitionException(ShipmentStatus from, ShipmentStatus to) {
        super("Cannot transition from " + from + " to " + to);
        this.from = from;
        this.to = to;
    }

    public ShipmentStatus getFrom() {
        return from;
    }

    public ShipmentStatus getTo() {
        return to;
    }
}
