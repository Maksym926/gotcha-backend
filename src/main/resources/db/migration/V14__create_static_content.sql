CREATE TABLE static_content (
    content_id BIGSERIAL PRIMARY KEY,
    section_key VARCHAR(100) NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    image_key VARCHAR(255) NOT NULL
);