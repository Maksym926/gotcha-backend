CREATE TABLE staticContent (
    content_id BIGSERIAL PRIMARY KEY,

    title VARCHAR(150) NOT NULL,

    description TEXT,

    image_key VARCHAR(255)

);