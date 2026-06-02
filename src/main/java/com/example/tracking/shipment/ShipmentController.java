package com.example.tracking.shipment;

import com.example.tracking.shipment.dto.AddTrackingEventRequest;
import com.example.tracking.shipment.dto.CreateShipmentRequest;
import com.example.tracking.shipment.dto.ShipmentResponse;
import com.example.tracking.shipment.dto.TimelineResponse;
import com.example.tracking.shipment.dto.TrackingEventResponse;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/shipments")
public class ShipmentController {

    private final ShipmentService shipmentService;

    public ShipmentController(ShipmentService shipmentService) {
        this.shipmentService = shipmentService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<ShipmentResponse> create(@Valid @RequestBody CreateShipmentRequest req) {
        ShipmentResponse resp = shipmentService.create(req);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(resp.id())
                .toUri();
        return ResponseEntity.created(location).body(resp);
    }

    @GetMapping("/{id}")
    public ShipmentResponse getById(@PathVariable UUID id) {
        return shipmentService.getById(id);
    }

    @GetMapping
    public PagedModel<ShipmentResponse> list(
            @RequestParam(required = false) ShipmentStatus status,
            @ParameterObject @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<ShipmentResponse> page = shipmentService.list(status, pageable);
        return new PagedModel<>(page);
    }

    @PostMapping("/{id}/events")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<TrackingEventResponse> addEvent(@PathVariable UUID id,
                                                          @Valid @RequestBody AddTrackingEventRequest req) {
        TrackingEventResponse resp = shipmentService.addEvent(id, req);
        return ResponseEntity.status(201).body(resp);
    }

    @GetMapping("/{id}/timeline")
    public TimelineResponse getTimeline(@PathVariable UUID id) {
        return shipmentService.getTimeline(id);
    }
}
