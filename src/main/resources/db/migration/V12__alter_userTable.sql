


ALTER TABLE users ADD COLUMN IF NOT EXISTS mood VARCHAR(255);
ALTER TABLE users ADD COLUMN IF NOT EXISTS subscription_status VARCHAR(50);
ALTER TABLE users ADD COLUMN IF NOT EXISTS gotcha_fav_drink VARCHAR(255);
ALTER TABLE users ADD COLUMN IF NOT EXISTS gotcha_fav_drink_picture_key VARCHAR(255);


ALTER TABLE users RENAME COLUMN status TO account_status;


ALTER TABLE users DROP COLUMN IF EXISTS profilepicturekey;
