package com.example.tracking.shipment;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "tracking_events")
public class TrackingEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(nullable = false)
    private Shipment shipment;

    @Enumerated(EnumType.STRING)
    @Column(length = 32, nullable = false)
    private ShipmentStatus status;

    private String location;

    @Column(length = 500)
    private String description;

    @Column(nullable = false)
    private OffsetDateTime occurredAt;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private OffsetDateTime recordedAt;

    protected TrackingEvent() {
    }

    public TrackingEvent(Shipment shipment, ShipmentStatus status, String location,
                         String description, OffsetDateTime occurredAt) {
        this.shipment = shipment;
        this.status = status;
        this.location = location;
        this.description = description;
        this.occurredAt = occurredAt;
    }

    public UUID getId() {
        return id;
    }

    public Shipment getShipment() {
        return shipment;
    }

    public ShipmentStatus getStatus() {
        return status;
    }

    public String getLocation() {
        return location;
    }

    public String getDescription() {
        return description;
    }

    public OffsetDateTime getOccurredAt() {
        return occurredAt;
    }

    public OffsetDateTime getRecordedAt() {
        return recordedAt;
    }
}
