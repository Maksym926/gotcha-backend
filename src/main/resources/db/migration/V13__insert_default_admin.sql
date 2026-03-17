-- Insert default admin user
-- Password is 'admin123' (hashed with BCrypt)
INSERT INTO users (username, email, password, role, account_status)
VALUES (
    'admin',
    'admin2@gotcha.com',
    '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN96.K9WxkU5rP3qLzVKK',
    'ADMIN',
    'ACTIVE'
) ON CONFLICT (email) DO NOTHING;
