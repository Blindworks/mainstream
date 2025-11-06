-- ================================================================
-- EINFACH IN MYSQL WORKBENCH KOPIEREN UND AUSFÜHREN
-- ================================================================

USE mainstream;

-- Lösche alte Testdaten (in richtiger Reihenfolge wegen Foreign Keys)
DELETE FROM gps_points WHERE run_id IN (SELECT id FROM runs WHERE user_id IN (SELECT id FROM users WHERE email LIKE 'test%@mainstream.app'));
DELETE FROM runs WHERE user_id IN (SELECT id FROM users WHERE email LIKE 'test%@mainstream.app');
DELETE FROM user_activities WHERE user_id IN (SELECT id FROM users WHERE email LIKE 'test%@mainstream.app');
DELETE FROM user_activities WHERE matched_route_id IS NOT NULL;
DELETE FROM route_track_points;
DELETE FROM predefined_routes;
DELETE FROM users WHERE email LIKE 'test%@mainstream.app';

-- ================================================================
-- TEST USERS (Passwort für alle: password123)
-- BCrypt Hash: $2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN96VFjmz6P7qVFhCqZ4W
-- ================================================================

INSERT INTO users (email, password, first_name, last_name, date_of_birth, gender, fitness_level, is_active, is_public_profile, role, profile_picture_url, bio, preferred_distance_unit, created_at, updated_at) VALUES
('admin@mainstream.app', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN96VFjmz6P7qVFhCqZ4W', 'Admin', 'User', '1985-03-15', 'MALE', 'EXPERT', 1, 1, 'ADMIN', 'https://ui-avatars.com/api/?name=Admin+User&background=e91e63&color=fff&size=128', 'Plattform Administrator', 'KILOMETERS', NOW(), NOW()),
('test.mueller@mainstream.app', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN96VFjmz6P7qVFhCqZ4W', 'Max', 'Müller', '1990-06-20', 'MALE', 'INTERMEDIATE', 1, 1, 'USER', 'https://ui-avatars.com/api/?name=Max+Mueller&background=9c27b0&color=fff&size=128', 'Laufe gerne am Main', 'KILOMETERS', NOW(), NOW()),
('test.schmidt@mainstream.app', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN96VFjmz6P7qVFhCqZ4W', 'Anna', 'Schmidt', '1988-11-12', 'FEMALE', 'ADVANCED', 1, 1, 'USER', 'https://ui-avatars.com/api/?name=Anna+Schmidt&background=673ab7&color=fff&size=128', 'Marathon-Läuferin', 'KILOMETERS', NOW(), NOW()),
('test.weber@mainstream.app', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN96VFjmz6P7qVFhCqZ4W', 'Tim', 'Weber', '1995-02-28', 'MALE', 'INTERMEDIATE', 1, 1, 'USER', 'https://ui-avatars.com/api/?name=Tim+Weber&background=3f51b5&color=fff&size=128', 'Morgenläufer', 'KILOMETERS', NOW(), NOW()),
('test.wagner@mainstream.app', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN96VFjmz6P7qVFhCqZ4W', 'Sarah', 'Wagner', '1992-09-05', 'FEMALE', 'ADVANCED', 1, 1, 'USER', 'https://ui-avatars.com/api/?name=Sarah+Wagner&background=2196f3&color=fff&size=128', 'Trail-Running Fan', 'KILOMETERS', NOW(), NOW()),
('test.becker@mainstream.app', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN96VFjmz6P7qVFhCqZ4W', 'Jan', 'Becker', '1987-04-17', 'MALE', 'BEGINNER', 1, 1, 'USER', 'https://ui-avatars.com/api/?name=Jan+Becker&background=03a9f4&color=fff&size=128', 'Hobbyjogger', 'KILOMETERS', NOW(), NOW()),
('test.hoffmann@mainstream.app', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN96VFjmz6P7qVFhCqZ4W', 'Lisa', 'Hoffmann', '1993-07-22', 'FEMALE', 'INTERMEDIATE', 1, 1, 'USER', 'https://ui-avatars.com/api/?name=Lisa+Hoffmann&background=00bcd4&color=fff&size=128', 'Laufen ist Meditation', 'KILOMETERS', NOW(), NOW()),
('test.koch@mainstream.app', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN96VFjmz6P7qVFhCqZ4W', 'Tom', 'Koch', '1991-01-30', 'MALE', 'INTERMEDIATE', 1, 1, 'USER', 'https://ui-avatars.com/api/?name=Tom+Koch&background=009688&color=fff&size=128', 'Stadtpark-Läufer', 'KILOMETERS', NOW(), NOW()),
('test.richter@mainstream.app', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN96VFjmz6P7qVFhCqZ4W', 'Emma', 'Richter', '1994-12-08', 'FEMALE', 'BEGINNER', 1, 1, 'USER', 'https://ui-avatars.com/api/?name=Emma+Richter&background=4caf50&color=fff&size=128', 'Neue Läuferin', 'KILOMETERS', NOW(), NOW()),
('test.klein@mainstream.app', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN96VFjmz6P7qVFhCqZ4W', 'Lukas', 'Klein', '1989-08-14', 'MALE', 'EXPERT', 1, 1, 'USER', 'https://ui-avatars.com/api/?name=Lukas+Klein&background=8bc34a&color=fff&size=128', 'Ultra-Marathon', 'KILOMETERS', NOW(), NOW()),
('test.wolf@mainstream.app', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN96VFjmz6P7qVFhCqZ4W', 'Sophie', 'Wolf', '1996-05-25', 'FEMALE', 'INTERMEDIATE', 1, 1, 'USER', 'https://ui-avatars.com/api/?name=Sophie+Wolf&background=cddc39&color=fff&size=128', 'Charity-Läuferin', 'KILOMETERS', NOW(), NOW()),
('test.neumann@mainstream.app', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN96VFjmz6P7qVFhCqZ4W', 'Felix', 'Neumann', '1990-10-03', 'MALE', 'ADVANCED', 1, 1, 'USER', 'https://ui-avatars.com/api/?name=Felix+Neumann&background=ffc107&color=fff&size=128', 'Intervalltraining', 'KILOMETERS', NOW(), NOW()),
('test.schwarz@mainstream.app', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN96VFjmz6P7qVFhCqZ4W', 'Laura', 'Schwarz', '1997-03-19', 'FEMALE', 'INTERMEDIATE', 1, 1, 'USER', 'https://ui-avatars.com/api/?name=Laura+Schwarz&background=ff9800&color=fff&size=128', 'Daily Runner', 'KILOMETERS', NOW(), NOW()),
('test.zimmermann@mainstream.app', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN96VFjmz6P7qVFhCqZ4W', 'Ben', 'Zimmermann', '1986-11-27', 'MALE', 'INTERMEDIATE', 1, 1, 'USER', 'https://ui-avatars.com/api/?name=Ben+Zimmermann&background=ff5722&color=fff&size=128', 'Abendläufer', 'KILOMETERS', NOW(), NOW()),
('test.fischer@mainstream.app', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN96VFjmz6P7qVFhCqZ4W', 'Marie', 'Fischer', '1992-06-11', 'FEMALE', 'ADVANCED', 1, 1, 'USER', 'https://ui-avatars.com/api/?name=Marie+Fischer&background=795548&color=fff&size=128', 'Naturliebhaberin', 'KILOMETERS', NOW(), NOW()),
('test.meyer@mainstream.app', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN96VFjmz6P7qVFhCqZ4W', 'Paul', 'Meyer', '1988-09-01', 'MALE', 'EXPERT', 1, 1, 'USER', 'https://ui-avatars.com/api/?name=Paul+Meyer&background=607d8b&color=fff&size=128', 'Wettkampf-Läufer', 'KILOMETERS', NOW(), NOW());

-- ================================================================
-- ROUTEN
-- ================================================================

INSERT INTO predefined_routes (name, description, original_filename, distance_meters, elevation_gain_meters, elevation_loss_meters, start_latitude, start_longitude, is_active, created_at, updated_at) VALUES
('10km Vollrunde Main', 'Komplette Mainufer-Runde', 'main_10km.gpx', 10200.00, 45.00, 45.00, 50.1074, 8.6841, 1, NOW(), NOW()),
('5km Ostpark Loop', 'Schnelle Ostpark-Runde', 'ostpark_5km.gpx', 5300.00, 15.00, 15.00, 50.1234, 8.7123, 1, NOW(), NOW()),
('7km Nordmainufer', 'Nordseite Hin und Zurück', 'nordmain_7km.gpx', 7100.00, 20.00, 20.00, 50.1145, 8.6512, 1, NOW(), NOW()),
('12km Stadtwald Trail', 'Trail durch den Stadtwald', 'stadtwald_12km.gpx', 12500.00, 180.00, 180.00, 50.0823, 8.6545, 1, NOW(), NOW());

-- ================================================================
-- RUNS
-- ================================================================

INSERT INTO runs (user_id, title, description, start_time, end_time, duration_seconds, distance_meters, average_pace_seconds_per_km, max_speed_kmh, average_speed_kmh, calories_burned, elevation_gain_meters, elevation_loss_meters, run_type, status, weather_condition, temperature_celsius, humidity_percentage, is_public, route_id, created_at, updated_at)
SELECT u.id, 'Morgenrunde am Main', 'Schöne Runde', DATE_SUB(NOW(), INTERVAL 2 DAY) + INTERVAL 7 HOUR, DATE_SUB(NOW(), INTERVAL 2 DAY) + INTERVAL 8 HOUR, 3720, 10200.00, 365.00, 18.5, 9.8, 620, 45.00, 45.00, 'OUTDOOR', 'COMPLETED', 'Sonnig', 18, 65, 1, 1, NOW(), NOW() FROM users u WHERE u.email = 'test.mueller@mainstream.app'
UNION ALL
SELECT u.id, 'Tempo-Training Ostpark', 'Schnelles Intervalltraining', DATE_SUB(NOW(), INTERVAL 1 DAY) + INTERVAL 17 HOUR, DATE_SUB(NOW(), INTERVAL 1 DAY) + INTERVAL 17 HOUR + INTERVAL 25 MINUTE, 1500, 5300.00, 283.00, 21.5, 12.7, 340, 15.00, 15.00, 'OUTDOOR', 'COMPLETED', 'Bewölkt', 16, 70, 1, 2, NOW(), NOW() FROM users u WHERE u.email = 'test.schmidt@mainstream.app'
UNION ALL
SELECT u.id, 'Feierabend-Lauf', 'Entspannt nach Arbeit', DATE_SUB(NOW(), INTERVAL 0 DAY) + INTERVAL 17 HOUR, DATE_SUB(NOW(), INTERVAL 0 DAY) + INTERVAL 17 HOUR + INTERVAL 45 MINUTE, 2700, 7100.00, 380.00, 16.2, 9.5, 450, 20.00, 20.00, 'OUTDOOR', 'COMPLETED', 'Heiter', 20, 55, 1, 3, NOW(), NOW() FROM users u WHERE u.email = 'test.weber@mainstream.app'
UNION ALL
SELECT u.id, 'Trail-Abenteuer', 'Viele Höhenmeter', DATE_SUB(NOW(), INTERVAL 5 DAY) + INTERVAL 9 HOUR, DATE_SUB(NOW(), INTERVAL 5 DAY) + INTERVAL 10 HOUR + INTERVAL 35 MINUTE, 4200, 12500.00, 336.00, 19.8, 10.7, 780, 180.00, 180.00, 'TRAIL', 'COMPLETED', 'Sonnig', 22, 50, 1, 4, NOW(), NOW() FROM users u WHERE u.email = 'test.wagner@mainstream.app'
UNION ALL
SELECT u.id, 'Kurze Runde', 'Nur 5km heute', DATE_SUB(NOW(), INTERVAL 3 DAY) + INTERVAL 18 HOUR, DATE_SUB(NOW(), INTERVAL 3 DAY) + INTERVAL 18 HOUR + INTERVAL 32 MINUTE, 1920, 5000.00, 384.00, 15.8, 9.4, 320, 14.00, 14.00, 'OUTDOOR', 'COMPLETED', 'Regen', 14, 85, 1, 3, NOW(), NOW() FROM users u WHERE u.email = 'test.becker@mainstream.app'
UNION ALL
SELECT u.id, 'Wochenend-Langstrecke', 'Komplette Mainrunde', DATE_SUB(NOW(), INTERVAL 7 DAY) + INTERVAL 8 HOUR, DATE_SUB(NOW(), INTERVAL 7 DAY) + INTERVAL 9 HOUR + INTERVAL 10 MINUTE, 4200, 10200.00, 412.00, 16.5, 8.7, 600, 45.00, 45.00, 'OUTDOOR', 'COMPLETED', 'Heiter', 19, 60, 1, 1, NOW(), NOW() FROM users u WHERE u.email = 'test.hoffmann@mainstream.app'
UNION ALL
SELECT u.id, 'Ostpark Durchquerung', 'Lockerer Dauerlauf', DATE_SUB(NOW(), INTERVAL 2 DAY) + INTERVAL 12 HOUR, DATE_SUB(NOW(), INTERVAL 2 DAY) + INTERVAL 12 HOUR + INTERVAL 28 MINUTE, 1680, 5300.00, 317.00, 19.2, 11.3, 350, 15.00, 15.00, 'OUTDOOR', 'COMPLETED', 'Sonnig', 21, 52, 1, 2, NOW(), NOW() FROM users u WHERE u.email = 'test.koch@mainstream.app'
UNION ALL
SELECT u.id, 'Mein erster 5km Lauf!', 'Super stolz', DATE_SUB(NOW(), INTERVAL 4 DAY) + INTERVAL 16 HOUR, DATE_SUB(NOW(), INTERVAL 4 DAY) + INTERVAL 16 HOUR + INTERVAL 40 MINUTE, 2400, 5200.00, 461.00, 13.5, 7.8, 380, 12.00, 12.00, 'OUTDOOR', 'COMPLETED', 'Bewölkt', 17, 68, 1, 3, NOW(), NOW() FROM users u WHERE u.email = 'test.richter@mainstream.app'
UNION ALL
SELECT u.id, 'Ultra-Training Stadtwald', 'Ultra-Vorbereitung', DATE_SUB(NOW(), INTERVAL 8 DAY) + INTERVAL 6 HOUR, DATE_SUB(NOW(), INTERVAL 8 DAY) + INTERVAL 7 HOUR + INTERVAL 15 MINUTE, 3900, 12500.00, 312.00, 21.3, 11.5, 850, 180.00, 180.00, 'TRAIL', 'COMPLETED', 'Heiter', 16, 72, 1, 4, NOW(), NOW() FROM users u WHERE u.email = 'test.klein@mainstream.app'
UNION ALL
SELECT u.id, 'Charity-Lauf Vorbereitung', 'Guter Zweck', DATE_SUB(NOW(), INTERVAL 6 DAY) + INTERVAL 10 HOUR, DATE_SUB(NOW(), INTERVAL 6 DAY) + INTERVAL 11 HOUR + INTERVAL 5 MINUTE, 3900, 10200.00, 382.00, 17.2, 9.4, 580, 45.00, 45.00, 'OUTDOOR', 'COMPLETED', 'Sonnig', 20, 58, 1, 1, NOW(), NOW() FROM users u WHERE u.email = 'test.wolf@mainstream.app'
UNION ALL
SELECT u.id, 'Intervall-Session', 'Harte Intervalle', DATE_SUB(NOW(), INTERVAL 1 DAY) + INTERVAL 6 HOUR, DATE_SUB(NOW(), INTERVAL 1 DAY) + INTERVAL 6 HOUR + INTERVAL 22 MINUTE, 1320, 5300.00, 249.00, 23.5, 14.4, 400, 15.00, 15.00, 'TRACK', 'COMPLETED', 'Klar', 15, 62, 1, 2, NOW(), NOW() FROM users u WHERE u.email = 'test.neumann@mainstream.app'
UNION ALL
SELECT u.id, 'Daily Run', 'Tägliche Runde', DATE_SUB(NOW(), INTERVAL 0 DAY) + INTERVAL 7 HOUR, DATE_SUB(NOW(), INTERVAL 0 DAY) + INTERVAL 7 HOUR + INTERVAL 30 MINUTE, 2700, 7100.00, 380.00, 16.8, 9.5, 440, 20.00, 20.00, 'OUTDOOR', 'COMPLETED', 'Heiter', 19, 60, 1, 3, NOW(), NOW() FROM users u WHERE u.email = 'test.schwarz@mainstream.app'
UNION ALL
SELECT u.id, 'Abendrunde am Wasser', 'Entspannt am Main', DATE_SUB(NOW(), INTERVAL 9 DAY) + INTERVAL 19 HOUR, DATE_SUB(NOW(), INTERVAL 9 DAY) + INTERVAL 20 HOUR + INTERVAL 8 MINUTE, 4080, 10200.00, 400.00, 16.0, 9.0, 610, 45.00, 45.00, 'OUTDOOR', 'COMPLETED', 'Dämmerung', 18, 65, 1, 1, NOW(), NOW() FROM users u WHERE u.email = 'test.zimmermann@mainstream.app'
UNION ALL
SELECT u.id, 'Naturlauf Stadtwald', 'Natur genießen', DATE_SUB(NOW(), INTERVAL 10 DAY) + INTERVAL 8 HOUR, DATE_SUB(NOW(), INTERVAL 10 DAY) + INTERVAL 9 HOUR + INTERVAL 20 MINUTE, 3840, 12500.00, 307.00, 20.5, 11.7, 770, 180.00, 180.00, 'TRAIL', 'COMPLETED', 'Sonnig', 21, 55, 1, 4, NOW(), NOW() FROM users u WHERE u.email = 'test.fischer@mainstream.app'
UNION ALL
SELECT u.id, 'Wettkampf-Simulation', 'Volle Power', DATE_SUB(NOW(), INTERVAL 4 DAY) + INTERVAL 7 HOUR, DATE_SUB(NOW(), INTERVAL 4 DAY) + INTERVAL 7 HOUR + INTERVAL 20 MINUTE, 1200, 5300.00, 226.00, 25.2, 15.9, 420, 15.00, 15.00, 'TRACK', 'COMPLETED', 'Klar', 17, 58, 1, 2, NOW(), NOW() FROM users u WHERE u.email = 'test.meyer@mainstream.app'
UNION ALL
SELECT u.id, 'Entspannter Sonntag', NULL, DATE_SUB(NOW(), INTERVAL 10 DAY) + INTERVAL 11 HOUR, DATE_SUB(NOW(), INTERVAL 10 DAY) + INTERVAL 11 HOUR + INTERVAL 55 MINUTE, 3300, 9800.00, 337.00, 18.2, 10.7, 590, 42.00, 42.00, 'OUTDOOR', 'COMPLETED', 'Sonnig', 22, 48, 1, 1, NOW(), NOW() FROM users u WHERE u.email = 'test.mueller@mainstream.app'
UNION ALL
SELECT u.id, 'Marathon-Training', 'Lange Einheit', DATE_SUB(NOW(), INTERVAL 8 DAY) + INTERVAL 6 HOUR, DATE_SUB(NOW(), INTERVAL 8 DAY) + INTERVAL 7 HOUR + INTERVAL 45 MINUTE, 4500, 15000.00, 300.00, 22.0, 12.0, 950, 85.00, 85.00, 'OUTDOOR', 'COMPLETED', 'Bewölkt', 19, 62, 1, NULL, NOW(), NOW() FROM users u WHERE u.email = 'test.schmidt@mainstream.app'
UNION ALL
SELECT u.id, 'Regenerations-Lauf', 'Ganz locker', DATE_SUB(NOW(), INTERVAL 5 DAY) + INTERVAL 6 HOUR + INTERVAL 30 MINUTE, DATE_SUB(NOW(), INTERVAL 5 DAY) + INTERVAL 7 HOUR, 1800, 4500.00, 400.00, 14.5, 9.0, 280, 12.00, 12.00, 'OUTDOOR', 'COMPLETED', 'Niesel', 13, 88, 1, NULL, NOW(), NOW() FROM users u WHERE u.email = 'test.weber@mainstream.app'
UNION ALL
SELECT u.id, 'Stadtwald Explorer', 'Neue Wege', DATE_SUB(NOW(), INTERVAL 12 DAY) + INTERVAL 10 HOUR, DATE_SUB(NOW(), INTERVAL 12 DAY) + INTERVAL 11 HOUR + INTERVAL 30 MINUTE, 4050, 12800.00, 316.00, 20.2, 11.4, 785, 185.00, 185.00, 'TRAIL', 'COMPLETED', 'Heiter', 20, 54, 1, 4, NOW(), NOW() FROM users u WHERE u.email = 'test.wagner@mainstream.app';

-- ================================================================
-- FERTIG!
-- ================================================================

SELECT 'FERTIG! Testdaten wurden importiert!' as Status;
SELECT COUNT(*) as 'Test-User' FROM users WHERE email LIKE 'test%@mainstream.app';
SELECT COUNT(*) as 'Routen' FROM predefined_routes;
SELECT COUNT(*) as 'Runs' FROM runs WHERE user_id IN (SELECT id FROM users WHERE email LIKE 'test%@mainstream.app');
