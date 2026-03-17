CREATE TABLE events (
    event_id BIGSERIAL PRIMARY KEY,

    title VARCHAR(150) NOT NULL,

    description TEXT,

    event_date TIMESTAMP NOT NULL,

    location VARCHAR(150),

    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);