-- V13__create_events_table.sql

CREATE TABLE events (
    id             CHAR(36)     NOT NULL,
    title          VARCHAR(255) NOT NULL,
    date           DATE         NOT NULL,
    start_time     TIME,
    end_time       TIME,
    location       VARCHAR(255),
    color          VARCHAR(20)  NOT NULL DEFAULT 'BLUE',
    created_by_id  CHAR(36)     NOT NULL,
    created_at     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_events
        PRIMARY KEY (id),

    CONSTRAINT fk_events_created_by
        FOREIGN KEY (created_by_id)
        REFERENCES users(id)
        ON DELETE RESTRICT
);

-- ── Join table ─────────────────────────────────────────────

CREATE TABLE event_participants (
    event_id  CHAR(36) NOT NULL,
    user_id   CHAR(36) NOT NULL,

    CONSTRAINT pk_event_participants
        PRIMARY KEY (event_id, user_id),

    CONSTRAINT fk_ep_event
        FOREIGN KEY (event_id)
        REFERENCES events(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_ep_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE
);

-- ── Indexes ───────────────────────────────────────────────

CREATE INDEX idx_events_date
    ON events(date);

CREATE INDEX idx_events_date_created_by
    ON events(date, created_by_id);

CREATE INDEX idx_ep_user_id
    ON event_participants(user_id);

CREATE INDEX idx_events_created_at
    ON events(created_at);