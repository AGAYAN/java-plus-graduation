CREATE TABLE IF NOT EXISTS category
(
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    CONSTRAINT uq_category_name UNIQUE (name)
);

CREATE TABLE IF NOT EXISTS compilation
(
    id BIGINT GENERATED ALWAYS AS IDENTITY,
    title VARCHAR(50) NOT NULL,
    pinned BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT pk_compilation PRIMARY KEY (id),
    CONSTRAINT uq_compilation_title UNIQUE (title)
);

CREATE TABLE IF NOT EXISTS event
(
    id BIGINT GENERATED ALWAYS AS IDENTITY,
    annotation VARCHAR(2000) NOT NULL,
    category_id BIGINT NOT NULL,
    description VARCHAR(7000) NOT NULL,
    event_date TIMESTAMP WITHOUT TIME ZONE  NOT NULL,
    latitude FLOAT NOT NULL CHECK (latitude BETWEEN -90 AND 90),
    longitude FLOAT NOT NULL CHECK (longitude BETWEEN -180 AND 180),
    paid BOOLEAN NOT NULL DEFAULT FALSE,
    created_on TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    initiator_id BIGINT NOT NULL,
    participant_limit INT NOT NULL DEFAULT 0,
    title VARCHAR(120) NOT NULL,
    published_on TIMESTAMP WITHOUT TIME ZONE,
    request_moderation BOOLEAN NOT NULL DEFAULT TRUE,
    state VARCHAR(20) NOT NULL CHECK (state IN ('PENDING', 'PUBLISHED', 'CANCELED')),
    CONSTRAINT pk_event PRIMARY KEY (id),
    CONSTRAINT fk_category FOREIGN KEY (category_id) REFERENCES category (id) ON DELETE RESTRICT,
    CONSTRAINT fk_initiator FOREIGN KEY (initiator_id) REFERENCES users (id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_event_category ON event (category_id);
CREATE INDEX IF NOT EXISTS idx_event_initiator ON event (initiator_id);
CREATE INDEX IF NOT EXISTS idx_event_id_initiator ON event (id, initiator_id);
CREATE INDEX IF NOT EXISTS idx_event_state ON event (state);
CREATE INDEX IF NOT EXISTS idx_event_date ON event (event_date);

-- CREATE INDEX IF NOT EXISTS idx_participation_request_event ON request (event_id);
-- CREATE INDEX IF NOT EXISTS idx_participation_request_requester ON request (user_id);
-- CREATE INDEX IF NOT EXISTS idx_participation_request_event_status ON request (event_id, status);
-- CREATE INDEX IF NOT EXISTS idx_participation_request_requester_event ON request (user_id, event_id);
-- CREATE INDEX IF NOT EXISTS idx_participation_request_id_event_status ON request (id, event_id, status);