ALTER TABLE products
    ALTER COLUMN price TYPE BIGINT USING price::BIGINT;

ALTER TABLE orders
    ALTER COLUMN total_price TYPE BIGINT USING total_price::BIGINT;

ALTER TABLE order_items
    ALTER COLUMN total_price TYPE BIGINT USING total_price::BIGINT;
