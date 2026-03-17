CREATE TABLE staticContent (
    content_id BIGSERIAL PRIMARY KEY,

    section_key VARCHAR(100) NOT NULL,

    title VARCHAR(150) NOT NULL,

    description TEXT,

    image_key VARCHAR(255)

);