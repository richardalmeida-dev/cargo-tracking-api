package com.example.tracking.shipment;

import com.example.tracking.error.InvalidTransitionException;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Raiz do agregado de tracking. Mantém a invariante do ciclo de vida do shipment:
 * o status só muda por {@link #addEvent} e cada mudança grava um {@link TrackingEvent}
 * imutável na timeline. Concorrência é detectada por {@link Version}.
 */
@Entity
@Table(name = "shipments")
public class Shipment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(length = 64, nullable = false, unique = true)
    private String trackingCode;

    @Enumerated(EnumType.STRING)
    @Column(length = 32, nullable = false)
    private ShipmentStatus status;

    @Column(nullable = false)
    private String origin;

    @Column(nullable = false)
    private String destination;

    @Column(nullable = false)
    private String recipientName;

    @Version
    private Long version;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private OffsetDateTime updatedAt;

    @OneToMany(mappedBy = "shipment", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TrackingEvent> events = new ArrayList<>();

    protected Shipment() {
    }

    public static Shipment create(String origin, String destination, String recipientName, String trackingCode) {
        Shipment shipment = new Shipment();
        shipment.origin = origin;
        shipment.destination = destination;
        shipment.recipientName = recipientName;
        shipment.trackingCode = trackingCode;
        shipment.status = ShipmentStatus.CREATED;
        return shipment;
    }

    /**
     * Aplica uma transição e registra o evento correspondente. O guard roda <b>antes</b>
     * de qualquer mutação — uma transição inválida não altera o status nem grava evento.
     * A regra mora aqui de propósito: domínio rico, não service anêmico.
     *
     * @throws InvalidTransitionException quando {@code target} não é alcançável a partir do status atual.
     */
    public TrackingEvent addEvent(ShipmentStatus target, String location,
                                  String description, OffsetDateTime occurredAt) {
        if (!this.status.canTransitionTo(target)) {
            throw new InvalidTransitionException(this.status, target);
        }
        this.status = target;
        TrackingEvent event = new TrackingEvent(this, target, location, description, occurredAt);
        this.events.add(event);
        return event;
    }

    public UUID getId() {
        return id;
    }

    public String getTrackingCode() {
        return trackingCode;
    }

    public ShipmentStatus getStatus() {
        return status;
    }

    public String getOrigin() {
        return origin;
    }

    public String getDestination() {
        return destination;
    }

    public String getRecipientName() {
        return recipientName;
    }

    public Long getVersion() {
        return version;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public List<TrackingEvent> getEvents() {
        return Collections.unmodifiableList(events);
    }
}
