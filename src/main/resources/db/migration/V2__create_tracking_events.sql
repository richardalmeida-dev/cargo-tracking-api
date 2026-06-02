-- Timeline imutável de eventos do shipment. ON DELETE CASCADE acompanha o
-- orphanRemoval do relacionamento JPA: apagar o shipment apaga seus eventos.
-- Índice em shipment_id serve as consultas de timeline ordenada por occurred_at.

CREATE TABLE tracking_events (
    id           uuid         PRIMARY KEY,
    shipment_id  uuid         NOT NULL REFERENCES shipments(id) ON DELETE CASCADE,
    status       varchar(32)  NOT NULL,
    location     varchar(255),
    description  varchar(500),
    occurred_at  timestamptz  NOT NULL,
    recorded_at  timestamptz  NOT NULL DEFAULT now()
);

CREATE INDEX idx_tracking_events_shipment_id ON tracking_events (shipment_id);
