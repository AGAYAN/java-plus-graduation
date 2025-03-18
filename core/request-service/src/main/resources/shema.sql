CREATE TABLE IF NOT EXISTS request
(
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    created TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    status VARCHAR(10) NOT NULL CHECK (status IN ('PENDING', 'CANCELED', 'CONFIRMED', 'REJECTED')),
    user_id BIGINT NOT NULL,
    event_id BIGINT NOT NULL,
    CONSTRAINT fk_request_event FOREIGN KEY (event_id) REFERENCES event (id) ON DELETE CASCADE,
    CONSTRAINT fk_requester FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

-- CREATE INDEX IF NOT EXISTS idx_event_category ON event (category_id);
-- CREATE INDEX IF NOT EXISTS idx_event_initiator ON event (initiator_id);
-- CREATE INDEX IF NOT EXISTS idx_event_id_initiator ON event (id, initiator_id);
-- CREATE INDEX IF NOT EXISTS idx_event_state ON event (state);
-- CREATE INDEX IF NOT EXISTS idx_event_date ON event (event_date);

CREATE INDEX IF NOT EXISTS idx_participation_request_event ON request (event_id);
CREATE INDEX IF NOT EXISTS idx_participation_request_requester ON request (user_id);
CREATE INDEX IF NOT EXISTS idx_participation_request_event_status ON request (event_id, status);
CREATE INDEX IF NOT EXISTS idx_participation_request_requester_event ON request (user_id, event_id);
CREATE INDEX IF NOT EXISTS idx_participation_request_id_event_status ON request (id, event_id, status);