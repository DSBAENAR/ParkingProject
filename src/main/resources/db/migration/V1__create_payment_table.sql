CREATE TABLE payment (
    id                       BIGSERIAL PRIMARY KEY,
    stripe_payment_intent_id VARCHAR(255) NOT NULL UNIQUE,
    amount                   BIGINT NOT NULL,
    currency                 VARCHAR(3) NOT NULL,
    status                   VARCHAR(20) NOT NULL,
    vehicle_id               VARCHAR(255),
    customer_id              VARCHAR(255),
    description              VARCHAR(255),
    created_at               TIMESTAMP NOT NULL,
    updated_at               TIMESTAMP NOT NULL
);

CREATE INDEX idx_payment_vehicle_id ON payment(vehicle_id);
