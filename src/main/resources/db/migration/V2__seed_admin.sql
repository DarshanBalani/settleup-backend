-- Seed Bootstrap Admin Account (admin@settleup.com / Admin@123456)
INSERT INTO users (name, email, password, role, is_active, created_at)
VALUES (
    'System Administrator',
    'admin@settleup.com',
    '$2a$10$E2UPv7arXnm9.x.1sJqOue4O871sN2R/0.T9wB054yW9Z986Jk39i',
    'ADMIN',
    TRUE,
    CURRENT_TIMESTAMP
);
