-- ================================================================
-- MainStream Database Seed Script
-- Erstellt Testdaten für die Landing Page mit Community Map
-- ================================================================

-- Lösche existierende Testdaten (optional - auskommentiert für Sicherheit)
-- DELETE FROM gps_points WHERE run_id IN (SELECT id FROM runs WHERE user_id IN (SELECT id FROM users WHERE email LIKE 'test%@mainstream.app'));
-- DELETE FROM runs WHERE user_id IN (SELECT id FROM users WHERE email LIKE 'test%@mainstream.app'));
-- DELETE FROM route_track_points;
-- DELETE FROM predefined_routes;
-- DELETE FROM users WHERE email LIKE 'test%@mainstream.app';

-- ================================================================
-- 1. TEST USERS
-- Passwort für alle Test-User: "password123"
-- BCrypt Hash: $2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy
-- ================================================================

INSERT INTO users (
    email, password, first_name, last_name, date_of_birth, gender, phone_number,
    profile_picture_url, bio, fitness_level, preferred_distance_unit, is_active,
    is_public_profile, role, created_at, updated_at
) VALUES
    -- Admin User
    ('admin@mainstream.app',
     '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
     'Admin', 'User', '1985-03-15', 'MALE', '+49 170 1234567',
     'https://ui-avatars.com/api/?name=Admin+User&background=e91e63&color=fff&size=128',
     'Plattform Administrator und Lauf-Enthusiast', 'EXPERT', 'KILOMETERS', true, true, 'ADMIN',
     NOW(), NOW()),

    -- Regular Test Users (15 Personen)
    ('test.mueller@mainstream.app',
     '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
     'Max', 'Müller', '1990-06-20', 'MALE', '+49 171 2345678',
     'https://ui-avatars.com/api/?name=Max+Mueller&background=9c27b0&color=fff&size=128',
     'Laufe gerne am Main entlang', 'INTERMEDIATE', 'KILOMETERS', true, true, 'USER',
     NOW(), NOW()),

    ('test.schmidt@mainstream.app',
     '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
     'Anna', 'Schmidt', '1988-11-12', 'FEMALE', '+49 172 3456789',
     'https://ui-avatars.com/api/?name=Anna+Schmidt&background=673ab7&color=fff&size=128',
     'Marathon-Läuferin aus Frankfurt', 'ADVANCED', 'KILOMETERS', true, true, 'USER',
     NOW(), NOW()),

    ('test.weber@mainstream.app',
     '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
     'Tim', 'Weber', '1995-02-28', 'MALE', '+49 173 4567890',
     'https://ui-avatars.com/api/?name=Tim+Weber&background=3f51b5&color=fff&size=128',
     'Morgenläufer - immer vor der Arbeit', 'INTERMEDIATE', 'KILOMETERS', true, true, 'USER',
     NOW(), NOW()),

    ('test.wagner@mainstream.app',
     '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
     'Sarah', 'Wagner', '1992-09-05', 'FEMALE', '+49 174 5678901',
     'https://ui-avatars.com/api/?name=Sarah+Wagner&background=2196f3&color=fff&size=128',
     'Trail-Running Fan', 'ADVANCED', 'KILOMETERS', true, true, 'USER',
     NOW(), NOW()),

    ('test.becker@mainstream.app',
     '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
     'Jan', 'Becker', '1987-04-17', 'MALE', '+49 175 6789012',
     'https://ui-avatars.com/api/?name=Jan+Becker&background=03a9f4&color=fff&size=128',
     'Hobbyjogger', 'BEGINNER', 'KILOMETERS', true, true, 'USER',
     NOW(), NOW()),

    ('test.hoffmann@mainstream.app',
     '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
     'Lisa', 'Hoffmann', '1993-07-22', 'FEMALE', '+49 176 7890123',
     'https://ui-avatars.com/api/?name=Lisa+Hoffmann&background=00bcd4&color=fff&size=128',
     'Laufen ist meine Meditation', 'INTERMEDIATE', 'KILOMETERS', true, true, 'USER',
     NOW(), NOW()),

    ('test.koch@mainstream.app',
     '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
     'Tom', 'Koch', '1991-01-30', 'MALE', '+49 177 8901234',
     'https://ui-avatars.com/api/?name=Tom+Koch&background=009688&color=fff&size=128',
     'Stadtpark-Läufer', 'INTERMEDIATE', 'KILOMETERS', true, true, 'USER',
     NOW(), NOW()),

    ('test.richter@mainstream.app',
     '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
     'Emma', 'Richter', '1994-12-08', 'FEMALE', '+49 178 9012345',
     'https://ui-avatars.com/api/?name=Emma+Richter&background=4caf50&color=fff&size=128',
     'Neue Läuferin, voller Motivation!', 'BEGINNER', 'KILOMETERS', true, true, 'USER',
     NOW(), NOW()),

    ('test.klein@mainstream.app',
     '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
     'Lukas', 'Klein', '1989-08-14', 'MALE', '+49 170 1122334',
     'https://ui-avatars.com/api/?name=Lukas+Klein&background=8bc34a&color=fff&size=128',
     'Ultra-Marathon Vorbereitung', 'EXPERT', 'KILOMETERS', true, true, 'USER',
     NOW(), NOW()),

    ('test.wolf@mainstream.app',
     '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
     'Sophie', 'Wolf', '1996-05-25', 'FEMALE', '+49 171 2233445',
     'https://ui-avatars.com/api/?name=Sophie+Wolf&background=cddc39&color=fff&size=128',
     'Laufe für den guten Zweck', 'INTERMEDIATE', 'KILOMETERS', true, true, 'USER',
     NOW(), NOW()),

    ('test.neumann@mainstream.app',
     '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
     'Felix', 'Neumann', '1990-10-03', 'MALE', '+49 172 3344556',
     'https://ui-avatars.com/api/?name=Felix+Neumann&background=ffc107&color=fff&size=128',
     'Intervalltraining-Spezialist', 'ADVANCED', 'KILOMETERS', true, true, 'USER',
     NOW(), NOW()),

    ('test.schwarz@mainstream.app',
     '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
     'Laura', 'Schwarz', '1997-03-19', 'FEMALE', '+49 173 4455667',
     'https://ui-avatars.com/api/?name=Laura+Schwarz&background=ff9800&color=fff&size=128',
     'Laufe jeden Tag!', 'INTERMEDIATE', 'KILOMETERS', true, true, 'USER',
     NOW(), NOW()),

    ('test.zimmermann@mainstream.app',
     '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
     'Ben', 'Zimmermann', '1986-11-27', 'MALE', '+49 174 5566778',
     'https://ui-avatars.com/api/?name=Ben+Zimmermann&background=ff5722&color=fff&size=128',
     'Abendläufer nach der Arbeit', 'INTERMEDIATE', 'KILOMETERS', true, true, 'USER',
     NOW(), NOW()),

    ('test.fischer@mainstream.app',
     '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
     'Marie', 'Fischer', '1992-06-11', 'FEMALE', '+49 175 6677889',
     'https://ui-avatars.com/api/?name=Marie+Fischer&background=795548&color=fff&size=128',
     'Naturliebhaberin und Läuferin', 'ADVANCED', 'KILOMETERS', true, true, 'USER',
     NOW(), NOW()),

    ('test.meyer@mainstream.app',
     '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
     'Paul', 'Meyer', '1988-09-01', 'MALE', '+49 176 7788990',
     'https://ui-avatars.com/api/?name=Paul+Meyer&background=607d8b&color=fff&size=128',
     'Wettkampf-Läufer', 'EXPERT', 'KILOMETERS', true, true, 'USER',
     NOW(), NOW());

-- ================================================================
-- 2. PREDEFINED ROUTES (Standard-Routen für Frankfurt Main)
-- Diese entsprechen den Routes aus dem Frontend Mock
-- ================================================================

INSERT INTO predefined_routes (
    name, description, original_filename, distance_meters, elevation_gain_meters,
    elevation_loss_meters, start_latitude, start_longitude, is_active, created_at, updated_at
) VALUES
    ('10km Vollrunde Main',
     'Komplette Mainufer-Runde, beide Seiten. Start am Eisernen Steg, Richtung Osten bis Offenbach, dann zurück am Nordufer.',
     'main_vollrunde_10km.gpx',
     10200.00, 45.00, 45.00,
     50.1074, 8.6841, true, NOW(), NOW()),

    ('5km Ostpark Loop',
     'Schnelle Runde durch den Ostpark. Ideal für Intervalltraining und Tempo-Läufe.',
     'ostpark_loop_5km.gpx',
     5300.00, 15.00, 15.00,
     50.1234, 8.7123, true, NOW(), NOW()),

    ('7km Nordmainufer',
     'Nordseite des Mainufers, Hin und Zurück. Flache Strecke, perfekt für Einsteiger.',
     'nordmainufer_7km.gpx',
     7100.00, 20.00, 20.00,
     50.1145, 8.6512, true, NOW(), NOW()),

    ('12km Stadtwald Trail',
     'Anspruchsvolle Trail-Strecke durch den Stadtwald. Viele Höhenmeter und naturbelassene Wege.',
     'stadtwald_trail_12km.gpx',
     12500.00, 180.00, 180.00,
     50.0823, 8.6545, true, NOW(), NOW());

-- ================================================================
-- 3. RUNS (Verschiedene Runs für Test-User in den letzten 30 Tagen)
-- Wir erstellen Runs die teilweise die Routen matchen
-- ================================================================

-- Runs für die letzten 30 Tage
INSERT INTO runs (
    user_id, title, description, start_time, end_time, duration_seconds,
    distance_meters, average_pace_seconds_per_km, max_speed_kmh, average_speed_kmh,
    calories_burned, elevation_gain_meters, elevation_loss_meters,
    run_type, status, weather_condition, temperature_celsius, humidity_percentage,
    is_public, route_id, created_at, updated_at
) VALUES
    -- Max Müller - 10km Vollrunde Main (Heute - 2 Tage)
    ((SELECT id FROM users WHERE email = 'test.mueller@mainstream.app'),
     'Morgenrunde am Main', 'Schöne Runde bei Sonnenaufgang',
     DATE_SUB(NOW(), INTERVAL 2 DAY) + INTERVAL 7 HOUR,
     DATE_SUB(NOW(), INTERVAL 2 DAY) + INTERVAL 8 HOUR + INTERVAL 2 MINUTE,
     3720, 10200.00, 365.00, 18.5, 9.8,
     620, 45.00, 45.00,
     'OUTDOOR', 'COMPLETED', 'Sonnig', 18, 65,
     true, 1, NOW(), NOW()),

    -- Anna Schmidt - 5km Ostpark (Heute - 1 Tag)
    ((SELECT id FROM users WHERE email = 'test.schmidt@mainstream.app'),
     'Tempo-Training Ostpark', 'Schnelles Intervalltraining',
     DATE_SUB(NOW(), INTERVAL 1 DAY) + INTERVAL 17 HOUR,
     DATE_SUB(NOW(), INTERVAL 1 DAY) + INTERVAL 17 HOUR + INTERVAL 25 MINUTE,
     1500, 5300.00, 283.00, 21.5, 12.7,
     340, 15.00, 15.00,
     'OUTDOOR', 'COMPLETED', 'Bewölkt', 16, 70,
     true, 2, NOW(), NOW()),

    -- Tim Weber - 7km Nordmainufer (Heute)
    ((SELECT id FROM users WHERE email = 'test.weber@mainstream.app'),
     'Feierabend-Lauf', 'Entspannter Lauf nach der Arbeit',
     DATE_SUB(NOW(), INTERVAL 3 HOUR),
     DATE_SUB(NOW(), INTERVAL 2 HOUR) + INTERVAL 25 MINUTE,
     2700, 7100.00, 380.00, 16.2, 9.5,
     450, 20.00, 20.00,
     'OUTDOOR', 'COMPLETED', 'Leicht bewölkt', 20, 55,
     true, 3, NOW(), NOW()),

    -- Sarah Wagner - Stadtwald Trail (Vor 5 Tagen)
    ((SELECT id FROM users WHERE email = 'test.wagner@mainstream.app'),
     'Trail-Abenteuer Stadtwald', 'Tolle Trail-Strecke mit vielen Höhenmetern',
     DATE_SUB(NOW(), INTERVAL 5 DAY) + INTERVAL 9 HOUR,
     DATE_SUB(NOW(), INTERVAL 5 DAY) + INTERVAL 10 HOUR + INTERVAL 35 MINUTE,
     4200, 12500.00, 336.00, 19.8, 10.7,
     780, 180.00, 180.00,
     'TRAIL', 'COMPLETED', 'Sonnig', 22, 50,
     true, 4, NOW(), NOW()),

    -- Jan Becker - Teilstrecke Nordmainufer (Vor 3 Tagen)
    ((SELECT id FROM users WHERE email = 'test.becker@mainstream.app'),
     'Kurze Runde', 'Nur 5km geschafft heute',
     DATE_SUB(NOW(), INTERVAL 3 DAY) + INTERVAL 18 HOUR,
     DATE_SUB(NOW(), INTERVAL 3 DAY) + INTERVAL 18 HOUR + INTERVAL 32 MINUTE,
     1920, 5000.00, 384.00, 15.8, 9.4,
     320, 14.00, 14.00,
     'OUTDOOR', 'COMPLETED', 'Regnerisch', 14, 85,
     true, 3, NOW(), NOW()),

    -- Lisa Hoffmann - 10km Vollrunde Main (Vor 7 Tagen)
    ((SELECT id FROM users WHERE email = 'test.hoffmann@mainstream.app'),
     'Wochenend-Langstrecke', 'Komplette Mainrunde',
     DATE_SUB(NOW(), INTERVAL 7 DAY) + INTERVAL 8 HOUR,
     DATE_SUB(NOW(), INTERVAL 7 DAY) + INTERVAL 9 HOUR + INTERVAL 10 MINUTE,
     4200, 10200.00, 412.00, 16.5, 8.7,
     600, 45.00, 45.00,
     'OUTDOOR', 'COMPLETED', 'Heiter', 19, 60,
     true, 1, NOW(), NOW()),

    -- Tom Koch - Ostpark (Vor 2 Tage)
    ((SELECT id FROM users WHERE email = 'test.koch@mainstream.app'),
     'Ostpark Durchquerung', 'Lockerer Dauerlauf',
     DATE_SUB(NOW(), INTERVAL 2 DAY) + INTERVAL 12 HOUR,
     DATE_SUB(NOW(), INTERVAL 2 DAY) + INTERVAL 12 HOUR + INTERVAL 28 MINUTE,
     1680, 5300.00, 317.00, 19.2, 11.3,
     350, 15.00, 15.00,
     'OUTDOOR', 'COMPLETED', 'Sonnig', 21, 52,
     true, 2, NOW(), NOW()),

    -- Emma Richter - Kurzer Lauf Nordmain (Vor 4 Tage)
    ((SELECT id FROM users WHERE email = 'test.richter@mainstream.app'),
     'Mein erster 5km Lauf!', 'Bin super stolz!',
     DATE_SUB(NOW(), INTERVAL 4 DAY) + INTERVAL 16 HOUR,
     DATE_SUB(NOW(), INTERVAL 4 DAY) + INTERVAL 16 HOUR + INTERVAL 40 MINUTE,
     2400, 5200.00, 461.00, 13.5, 7.8,
     380, 12.00, 12.00,
     'OUTDOOR', 'COMPLETED', 'Bewölkt', 17, 68,
     true, 3, NOW(), NOW()),

    -- Lukas Klein - Stadtwald Trail (Vor 8 Tage)
    ((SELECT id FROM users WHERE email = 'test.klein@mainstream.app'),
     'Ultra-Training Stadtwald', 'Vorbereitung für Ultra-Marathon',
     DATE_SUB(NOW(), INTERVAL 8 DAY) + INTERVAL 6 HOUR,
     DATE_SUB(NOW(), INTERVAL 8 DAY) + INTERVAL 7 HOUR + INTERVAL 15 MINUTE,
     3900, 12500.00, 312.00, 21.3, 11.5,
     850, 180.00, 180.00,
     'TRAIL', 'COMPLETED', 'Heiter', 16, 72,
     true, 4, NOW(), NOW()),

    -- Sophie Wolf - 10km Main (Vor 6 Tage)
    ((SELECT id FROM users WHERE email = 'test.wolf@mainstream.app'),
     'Charity-Lauf Vorbereitung', 'Für einen guten Zweck',
     DATE_SUB(NOW(), INTERVAL 6 DAY) + INTERVAL 10 HOUR,
     DATE_SUB(NOW(), INTERVAL 6 DAY) + INTERVAL 11 HOUR + INTERVAL 5 MINUTE,
     3900, 10200.00, 382.00, 17.2, 9.4,
     580, 45.00, 45.00,
     'OUTDOOR', 'COMPLETED', 'Sonnig', 20, 58,
     true, 1, NOW(), NOW()),

    -- Felix Neumann - Ostpark Speed (Vor 1 Tag)
    ((SELECT id FROM users WHERE email = 'test.neumann@mainstream.app'),
     'Intervall-Session', 'Harte Intervalle im Ostpark',
     DATE_SUB(NOW(), INTERVAL 1 DAY) + INTERVAL 6 HOUR,
     DATE_SUB(NOW(), INTERVAL 1 DAY) + INTERVAL 6 HOUR + INTERVAL 22 MINUTE,
     1320, 5300.00, 249.00, 23.5, 14.4,
     400, 15.00, 15.00,
     'TRACK', 'COMPLETED', 'Klar', 15, 62,
     true, 2, NOW(), NOW()),

    -- Laura Schwarz - Nordmain (Heute - 5 Stunden)
    ((SELECT id FROM users WHERE email = 'test.schwarz@mainstream.app'),
     'Daily Run', 'Meine tägliche Runde',
     DATE_SUB(NOW(), INTERVAL 5 HOUR),
     DATE_SUB(NOW(), INTERVAL 4 HOUR) + INTERVAL 30 MINUTE,
     2700, 7100.00, 380.00, 16.8, 9.5,
     440, 20.00, 20.00,
     'OUTDOOR', 'COMPLETED', 'Heiter', 19, 60,
     true, 3, NOW(), NOW()),

    -- Ben Zimmermann - Main Vollrunde (Vor 9 Tage)
    ((SELECT id FROM users WHERE email = 'test.zimmermann@mainstream.app'),
     'Abendrunde am Wasser', 'Entspannte Runde am Main',
     DATE_SUB(NOW(), INTERVAL 9 DAY) + INTERVAL 19 HOUR,
     DATE_SUB(NOW(), INTERVAL 9 DAY) + INTERVAL 20 HOUR + INTERVAL 8 MINUTE,
     4080, 10200.00, 400.00, 16.0, 9.0,
     610, 45.00, 45.00,
     'OUTDOOR', 'COMPLETED', 'Dämmerung', 18, 65,
     true, 1, NOW(), NOW()),

    -- Marie Fischer - Stadtwald (Vor 10 Tage)
    ((SELECT id FROM users WHERE email = 'test.fischer@mainstream.app'),
     'Naturlauf im Stadtwald', 'Genießen der Natur beim Laufen',
     DATE_SUB(NOW(), INTERVAL 10 DAY) + INTERVAL 8 HOUR,
     DATE_SUB(NOW(), INTERVAL 10 DAY) + INTERVAL 9 HOUR + INTERVAL 20 MINUTE,
     3840, 12500.00, 307.00, 20.5, 11.7,
     770, 180.00, 180.00,
     'TRAIL', 'COMPLETED', 'Sonnig', 21, 55,
     true, 4, NOW(), NOW()),

    -- Paul Meyer - Ostpark Race (Vor 4 Tage)
    ((SELECT id FROM users WHERE email = 'test.meyer@mainstream.app'),
     'Wettkampf-Simulation', 'Volle Power im Ostpark',
     DATE_SUB(NOW(), INTERVAL 4 DAY) + INTERVAL 7 HOUR,
     DATE_SUB(NOW(), INTERVAL 4 DAY) + INTERVAL 7 HOUR + INTERVAL 20 MINUTE,
     1200, 5300.00, 226.00, 25.2, 15.9,
     420, 15.00, 15.00,
     'TRACK', 'COMPLETED', 'Klar', 17, 58,
     true, 2, NOW(), NOW()),

    -- Weitere Runs für Vielfalt
    -- Max Müller - Zweiter Lauf (Vor 10 Tage)
    ((SELECT id FROM users WHERE email = 'test.mueller@mainstream.app'),
     'Entspannter Sonntags-Lauf', NULL,
     DATE_SUB(NOW(), INTERVAL 10 DAY) + INTERVAL 11 HOUR,
     DATE_SUB(NOW(), INTERVAL 10 DAY) + INTERVAL 11 HOUR + INTERVAL 55 MINUTE,
     3300, 9800.00, 337.00, 18.2, 10.7,
     590, 42.00, 42.00,
     'OUTDOOR', 'COMPLETED', 'Sonnig', 22, 48,
     true, 1, NOW(), NOW()),

    -- Anna Schmidt - Zweiter Lauf (Vor 8 Tage)
    ((SELECT id FROM users WHERE email = 'test.schmidt@mainstream.app'),
     'Marathon-Training: Lange Einheit', 'Building endurance',
     DATE_SUB(NOW(), INTERVAL 8 DAY) + INTERVAL 6 HOUR,
     DATE_SUB(NOW(), INTERVAL 8 DAY) + INTERVAL 7 HOUR + INTERVAL 45 MINUTE,
     4500, 15000.00, 300.00, 22.0, 12.0,
     950, 85.00, 85.00,
     'OUTDOOR', 'COMPLETED', 'Bewölkt', 19, 62,
     true, NULL, NOW(), NOW()),

    -- Tim Weber - Recovery Run (Vor 5 Tage)
    ((SELECT id FROM users WHERE email = 'test.weber@mainstream.app'),
     'Regenerations-Lauf', 'Ganz locker',
     DATE_SUB(NOW(), INTERVAL 5 DAY) + INTERVAL 6 HOUR + INTERVAL 30 MINUTE,
     DATE_SUB(NOW(), INTERVAL 5 DAY) + INTERVAL 7 HOUR,
     1800, 4500.00, 400.00, 14.5, 9.0,
     280, 12.00, 12.00,
     'OUTDOOR', 'COMPLETED', 'Nieselregen', 13, 88,
     true, NULL, NOW(), NOW()),

    -- Sarah Wagner - Zweiter Trail (Vor 12 Tage)
    ((SELECT id FROM users WHERE email = 'test.wagner@mainstream.app'),
     'Stadtwald Explorer', 'Neue Wege entdeckt',
     DATE_SUB(NOW(), INTERVAL 12 DAY) + INTERVAL 10 HOUR,
     DATE_SUB(NOW(), INTERVAL 12 DAY) + INTERVAL 11 HOUR + INTERVAL 30 MINUTE,
     4050, 12800.00, 316.00, 20.2, 11.4,
     785, 185.00, 185.00,
     'TRAIL', 'COMPLETED', 'Heiter', 20, 54,
     true, 4, NOW(), NOW());

-- ================================================================
-- Hinweise zur Nutzung:
-- ================================================================
-- 1. Alle Test-User haben das Passwort: "password123"
-- 2. Die User-IDs werden dynamisch via Subquery ermittelt
-- 3. Route-IDs sind fest (1-4) basierend auf Einfügereihenfolge
-- 4. GPS-Punkte und RouteTrackPoints werden vom Backend generiert
--    wenn FIT/GPX-Dateien hochgeladen werden
-- 5. Für die Community Map werden die route_id Verknüpfungen genutzt
--
-- Um das Script auszuführen:
-- mysql -u mainstream -p mainstream < seed-testdata.sql
--
-- Oder via Docker:
-- docker exec -i mainstream-db mysql -u mainstream -p mainstream < seed-testdata.sql
-- ================================================================

SELECT 'Seed-Script erfolgreich ausgeführt!' as Status;
SELECT COUNT(*) as 'Angelegte User' FROM users WHERE email LIKE 'test%@mainstream.app';
SELECT COUNT(*) as 'Angelegte Routen' FROM predefined_routes;
SELECT COUNT(*) as 'Angelegte Runs' FROM runs WHERE user_id IN (SELECT id FROM users WHERE email LIKE 'test%@mainstream.app');
