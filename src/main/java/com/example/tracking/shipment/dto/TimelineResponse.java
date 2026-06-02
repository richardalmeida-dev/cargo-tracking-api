package com.example.tracking.shipment.dto;

import com.example.tracking.shipment.Shipment;
import com.example.tracking.shipment.ShipmentStatus;
import com.example.tracking.shipment.TrackingEvent;

import java.util.List;
import java.util.UUID;

public record TimelineResponse(
        UUID shipmentId,
        ShipmentStatus currentStatus,
        List<TrackingEventResponse> events
) {
    public static TimelineResponse of(Shipment s, List<TrackingEvent> events) {
        return new TimelineResponse(
                s.getId(),
                s.getStatus(),
                events.stream().map(TrackingEventResponse::from).toList()
        );
    }
}
