-- 2. Таблица категорий
CREATE TABLE IF NOT EXISTS category (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    CONSTRAINT uq_category_name UNIQUE (name)
);

-- 3. Таблица событий
CREATE TABLE IF NOT EXISTS event (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    annotation VARCHAR(2000) NOT NULL,
    category_id BIGINT NOT NULL,
    description VARCHAR(7000) NOT NULL,
    event_date TIMESTAMP WITHOUT TIME ZONE NOT NULL,
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
    CONSTRAINT fk_event_category FOREIGN KEY (category_id) REFERENCES category(id) ON DELETE RESTRICT
);

-- Индексы для таблицы event
CREATE INDEX IF NOT EXISTS idx_event_category ON event (category_id);
CREATE INDEX IF NOT EXISTS idx_event_initiator ON event (initiator_id);
CREATE INDEX IF NOT EXISTS idx_event_id_initiator ON event (id, initiator_id);
CREATE INDEX IF NOT EXISTS idx_event_state ON event (state);
CREATE INDEX IF NOT EXISTS idx_event_date ON event (event_date);

-- 4. Таблица подборок (compilation)
CREATE TABLE IF NOT EXISTS compilation (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    title VARCHAR(50) NOT NULL,
    pinned BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT uq_compilation_title UNIQUE (title)
);

-- 5. Связка событий и подборок
CREATE TABLE IF NOT EXISTS compilation_event (
    compilation_id BIGINT NOT NULL,
    event_id BIGINT NOT NULL,
    PRIMARY KEY (compilation_id, event_id),
    FOREIGN KEY (compilation_id) REFERENCES compilation(id) ON DELETE CASCADE,
    FOREIGN KEY (event_id) REFERENCES event(id) ON DELETE CASCADE
);

-- 6. Таблица заявок на участие
CREATE TABLE IF NOT EXISTS request (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    event_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL CHECK (status IN ('PENDING', 'CONFIRMED', 'REJECTED')),
    created TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_request_event FOREIGN KEY (event_id) REFERENCES event(id) ON DELETE CASCADE,
    CONSTRAINT uq_request_user_event UNIQUE (user_id, event_id)
);

-- Индексы для request
CREATE INDEX IF NOT EXISTS idx_participation_request_event ON request (event_id);
CREATE INDEX IF NOT EXISTS idx_participation_request_requester ON request (user_id);
CREATE INDEX IF NOT EXISTS idx_participation_request_event_status ON request (event_id, status);
CREATE INDEX IF NOT EXISTS idx_participation_request_requester_event ON request (user_id, event_id);
CREATE INDEX IF NOT EXISTS idx_participation_request_id_event_status ON request (id, event_id, status);