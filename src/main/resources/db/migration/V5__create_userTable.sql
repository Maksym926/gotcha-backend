CREATE TABLE users(
    user_id BIGSERIAL PRIMARY KEY,

    username VARCHAR(50) NOT NULL UNIQUE,

    password VARCHAR(255) NOT NULL,

    email VARCHAR(100) NOT NULL UNIQUE,

    status VARCHAR(20),

    gotcha_coins BIGINT DEFAULT 0,

    role VARCHAR(20) NOT NULL
)