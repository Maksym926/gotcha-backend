CREATE TABLE products(
    product_id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(255) NOT NULL,
    brand VARCHAR(100) NOT NULL,
    price NUMERIC(10,2) NOT NULL DEFAULT 0,
    category VARCHAR(100) NOT NULL,
    release_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    product_available BOOLEAN NOT NULL DEFAULT TRUE,
    stock_quantity BIGINT NOT NULL DEFAULT 0,
    image_key VARCHAR(255)
);

CREATE TABLE orders (
    order_id BIGSERIAL PRIMARY KEY,

    order_code Varchar(100) NOT NULL UNIQUE,

    user_id BIGINT NOT NULL,

    status Varchar(100) NOT NULL DEFAULT 'PENDING',

    total_price NUMERIC(10,2) NOT NULL DEFAULT 0,

    create_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    update_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_order_user
        FOREIGN KEY(user_id)
        REFERENCES users(user_id)

);

CREATE TABLE order_items(
    order_item_id BIGSERIAL PRIMARY KEY,

    order_id BIGINT NOT NULL,
    CONSTRAINT fk_order_order_item
        FOREIGN KEY(order_id)
        REFERENCES orders(order_id),

    product_id BIGINT NOT NULL,
    CONSTRAINT fk_product_order_item
            FOREIGN KEY(product_id)
            REFERENCES products(product_id),

    quantity BIGINT NOT NULL DEFAULT 1,

    total_price NUMERIC(10, 2) NOT NULL


);