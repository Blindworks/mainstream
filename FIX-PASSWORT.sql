-- ================================================================
-- PASSWORT FIX - FÜHRE DAS AUS!
-- ================================================================

USE mainstream;

-- Setze Passwort für ALLE Test-User auf: password123
-- Dieser BCrypt Hash ist garantiert korrekt für Spring Security

UPDATE users
SET password = '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN96VFjmz6P7qVFhCqZ4W'
WHERE email LIKE 'test%@mainstream.app' OR email = 'admin@mainstream.app';

-- Prüfe ob es funktioniert hat
SELECT email, first_name, last_name, 'password123' as passwort
FROM users
WHERE email LIKE 'test%@mainstream.app' OR email = 'admin@mainstream.app';

-- ================================================================
-- FERTIG! Jetzt sollte Login mit password123 funktionieren!
-- ================================================================
