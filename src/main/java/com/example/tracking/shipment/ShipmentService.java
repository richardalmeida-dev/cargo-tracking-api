package com.example.tracking.shipment;

import com.example.tracking.error.ShipmentNotFoundException;
import com.example.tracking.shipment.dto.AddTrackingEventRequest;
import com.example.tracking.shipment.dto.CreateShipmentRequest;
import com.example.tracking.shipment.dto.ShipmentResponse;
import com.example.tracking.shipment.dto.TimelineResponse;
import com.example.tracking.shipment.dto.TrackingEventResponse;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.List;
import java.util.UUID;

@Service
public class ShipmentService {

    private static final String ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final int CODE_LENGTH = 8;
    private static final int MAX_CODE_ATTEMPTS = 5;
    private static final SecureRandom RANDOM = new SecureRandom();

    private final ShipmentRepository shipmentRepository;
    private final TrackingEventRepository trackingEventRepository;

    public ShipmentService(ShipmentRepository shipmentRepository,
                           TrackingEventRepository trackingEventRepository) {
        this.shipmentRepository = shipmentRepository;
        this.trackingEventRepository = trackingEventRepository;
    }

    /**
     * Cria um shipment com um tracking code único. O método propositalmente <b>não</b> é
     * {@code @Transactional}: cada {@code save} roda na sua própria transação curta, então
     * uma colisão no constraint único de {@code tracking_code} pode ser capturada e o loop
     * gera outro código. Numa transação envolvente, o primeiro erro deixaria o contexto
     * em rollback-only e a tentativa seguinte quebraria com {@code UnexpectedRollbackException}.
     */
    public ShipmentResponse create(CreateShipmentRequest req) {
        for (int attempt = 1; attempt <= MAX_CODE_ATTEMPTS; attempt++) {
            String trackingCode = generateTrackingCode();
            Shipment shipment = Shipment.create(req.origin(), req.destination(), req.recipientName(), trackingCode);
            try {
                Shipment saved = shipmentRepository.save(shipment);
                return ShipmentResponse.from(saved);
            } catch (DataIntegrityViolationException e) {
                // único constraint relevante é tracking_code; colisão → regenera
            }
        }
        throw new IllegalStateException("Failed to generate unique tracking code after " + MAX_CODE_ATTEMPTS + " attempts");
    }

    /**
     * Aplica um evento ao shipment. Se a transição for inválida, a exceção do domínio
     * propaga e o {@code @Transactional} faz rollback — status e timeline ficam intactos.
     * {@link org.springframework.dao.OptimisticLockingFailureException} também é deixada
     * propagar para o handler global, que a mapeia em 409 Conflict.
     */
    @Transactional
    public TrackingEventResponse addEvent(UUID id, AddTrackingEventRequest req) {
        Shipment shipment = shipmentRepository.findById(id)
                .orElseThrow(() -> new ShipmentNotFoundException(id));
        TrackingEvent event = shipment.addEvent(req.status(), req.location(), req.description(), req.occurredAt());
        shipmentRepository.save(shipment);
        return TrackingEventResponse.from(event);
    }

    @Transactional(readOnly = true)
    public ShipmentResponse getById(UUID id) {
        return shipmentRepository.findById(id)
                .map(ShipmentResponse::from)
                .orElseThrow(() -> new ShipmentNotFoundException(id));
    }

    @Transactional(readOnly = true)
    public Page<ShipmentResponse> list(ShipmentStatus status, Pageable pageable) {
        Page<Shipment> page = (status != null)
                ? shipmentRepository.findByStatus(status, pageable)
                : shipmentRepository.findAll(pageable);
        return page.map(ShipmentResponse::from);
    }

    @Transactional(readOnly = true)
    public TimelineResponse getTimeline(UUID id) {
        Shipment shipment = shipmentRepository.findById(id)
                .orElseThrow(() -> new ShipmentNotFoundException(id));
        List<TrackingEvent> events = trackingEventRepository.findByShipmentIdOrderByOccurredAtAsc(id);
        return TimelineResponse.of(shipment, events);
    }

    private String generateTrackingCode() {
        StringBuilder sb = new StringBuilder(4 + CODE_LENGTH);
        sb.append("SHP-");
        for (int i = 0; i < CODE_LENGTH; i++) {
            sb.append(ALPHABET.charAt(RANDOM.nextInt(ALPHABET.length())));
        }
        return sb.toString();
    }
}
