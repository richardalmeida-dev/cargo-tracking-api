package com.example.tracking.shipment;

import com.example.tracking.AbstractIntegrationTest;
import com.example.tracking.shipment.dto.AddTrackingEventRequest;
import com.example.tracking.shipment.dto.CreateShipmentRequest;
import com.example.tracking.shipment.dto.ShipmentResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
class ShipmentControllerIT extends AbstractIntegrationTest {

    private static final String API_KEY_HEADER = "X-API-Key";
    private static final String API_KEY_VALUE = "test-api-key";

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    ShipmentService shipmentService;

    @Autowired
    ShipmentRepository shipmentRepository;

    @Autowired
    TrackingEventRepository trackingEventRepository;

    @BeforeEach
    void cleanup() {
        trackingEventRepository.deleteAll();
        shipmentRepository.deleteAll();
    }

    @Test
    void createShipment_returns201WithLocationAndCreatedStatus() throws Exception {
        CreateShipmentRequest req = new CreateShipmentRequest("São Paulo", "Curitiba", "Alice");

        mockMvc.perform(post("/api/v1/shipments")
                        .header(API_KEY_HEADER, API_KEY_VALUE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.status").value("CREATED"))
                .andExpect(jsonPath("$.trackingCode").value(notNullValue()));
    }

    @Test
    void addEvent_invalidTransition_returns409Problem() throws Exception {
        ShipmentResponse created = shipmentService.create(
                new CreateShipmentRequest("São Paulo", "Curitiba", "Bob"));
        UUID id = created.id();

        AddTrackingEventRequest event = new AddTrackingEventRequest(
                ShipmentStatus.DELIVERED, "Anywhere", null, OffsetDateTime.now().minusMinutes(1));

        mockMvc.perform(post("/api/v1/shipments/{id}/events", id)
                        .header(API_KEY_HEADER, API_KEY_VALUE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(event)))
                .andExpect(status().isConflict())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value("Invalid state transition"))
                .andExpect(jsonPath("$.detail").value(containsString("Cannot transition")));
    }

    @Test
    void missingApiKey_returns401Problem() throws Exception {
        CreateShipmentRequest req = new CreateShipmentRequest("São Paulo", "Curitiba", "Alice");

        mockMvc.perform(post("/api/v1/shipments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value("Unauthorized"));
    }
}
