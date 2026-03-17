    CREATE TABLE event_rsvp(
    rsvp_id BIGSERIAL PRIMARY KEY,

    status VARCHAR(20),

    user_id BIGINT NOT NULL,

    event_id BIGINT NOT NULL,

    CONSTRAINT fk_user
        FOREIGN KEY (user_id)
        REFERENCES users(user_id),

    CONSTRAINT fk_event
        FOREIGN KEY (event_id)
        REFERENCES events(event_id)

)