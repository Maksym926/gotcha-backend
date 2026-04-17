INSERT INTO users (username, email, password, role, account_status)
VALUES (
           'admin',
           'admin2@gotcha.com',
           '$2a$10$dzNn5r1lUDGSSLyz5CDfuuUWCHN7CPT8uSUB7z/TegD03uRZhW6xi',
           'ADMIN',
           'ACTIVE'

       ) ON CONFLICT (email) DO NOTHING;