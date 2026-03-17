ALTER TABLE events
DROP COLUMN image_name,
DROP COLUMN image_type,
DROP COLUMN image_data;

ALTER TABLE events
ADD COLUMN image_key VARCHAR(255);