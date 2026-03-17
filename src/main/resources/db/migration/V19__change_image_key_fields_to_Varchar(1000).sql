ALTER TABLE users
    ALTER COLUMN profile_picture_key TYPE VARCHAR(1000),
    ALTER COLUMN gotcha_fav_drink_picture_key TYPE VARCHAR(1000);

ALTER TABLE static_content
    ALTER COLUMN image_key TYPE VARCHAR(1000);

ALTER TABLE products
    ALTER COLUMN image_key TYPE VARCHAR(1000);

ALTER TABLE events
    ALTER COLUMN image_key TYPE VARCHAR(1000);
