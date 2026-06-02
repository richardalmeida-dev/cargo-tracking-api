-- Tabela raiz do agregado. Os tipos batem com o mapeamento JPA:
--   uuid          ⇔ @Id UUID (GenerationType.UUID)
--   varchar(32)   ⇔ @Enumerated(STRING) ShipmentStatus (cabe o maior nome do enum)
--   timestamptz   ⇔ OffsetDateTime sem ambiguidade de fuso
--   version       suporta @Version para optimistic locking
-- Índice em status acelera o filtro paginado de /api/v1/shipments?status=...

CREATE TABLE shipments (
    id              uuid         PRIMARY KEY,
    tracking_code   varchar(64)  NOT NULL UNIQUE,
    status          varchar(32)  NOT NULL,
    origin          varchar(255) NOT NULL,
    destination     varchar(255) NOT NULL,
    recipient_name  varchar(255) NOT NULL,
    version         bigint       NOT NULL DEFAULT 0,
    created_at      timestamptz  NOT NULL DEFAULT now(),
    updated_at      timestamptz  NOT NULL DEFAULT now()
);

CREATE INDEX idx_shipments_status ON shipments (status);
