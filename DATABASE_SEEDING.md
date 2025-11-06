# MainStream Database Seeding

Dieses Dokument beschreibt, wie du die MainStream-Datenbank mit Testdaten füllen kannst, um die Landing Page mit Community-Map zu testen.

## 🎯 Was wird erstellt?

Das Seed-Script erstellt:
- **16 Test-User** (1 Admin + 15 reguläre User)
- **4 Standard-Routen** (10km Main, 5km Ostpark, 7km Nordmainufer, 12km Stadtwald)
- **20+ Runs** verteilt über die letzten 30 Tage
- Realistische Daten (Distanzen, Zeiten, Paces, Höhenmeter, Wetter)

## 🚀 Schnellstart

### Methode 1: Bash-Script (Empfohlen)

```bash
# Script ausführen
./seed-database.sh
```

Das Script:
- Prüft die Datenbankverbindung
- Zeigt aktuelle Daten an
- Fragt nach Bestätigung
- Löscht alte Testdaten
- Importiert neue Testdaten
- Zeigt Statistiken an

### Methode 2: Direkt mit MySQL

```bash
# Aus dem Projekt-Root:
mysql -u mainstream -p mainstream < mainstream-backend/src/main/resources/db/seed-testdata.sql
# Passwort eingeben: taxcRH51#
```

### Methode 3: Via Docker (falls DB in Docker läuft)

```bash
# Wenn deine Datenbank in einem Docker-Container läuft:
docker exec -i mainstream-db mysql -u mainstream -p'taxcRH51#' mainstream < mainstream-backend/src/main/resources/db/seed-testdata.sql
```

## 👤 Test-Accounts

Nach dem Seeding kannst du dich mit folgenden Accounts einloggen:

### Admin-Account
- **Email:** admin@mainstream.app
- **Passwort:** password123

### Test-User (Auswahl)
- **Email:** test.mueller@mainstream.app - **Passwort:** password123
- **Email:** test.schmidt@mainstream.app - **Passwort:** password123
- **Email:** test.weber@mainstream.app - **Passwort:** password123
- **Email:** test.wagner@mainstream.app - **Passwort:** password123

Alle Test-User haben das gleiche Passwort: **password123**

Weitere User:
- test.becker@mainstream.app
- test.hoffmann@mainstream.app
- test.koch@mainstream.app
- test.richter@mainstream.app
- test.klein@mainstream.app
- test.wolf@mainstream.app
- test.neumann@mainstream.app
- test.schwarz@mainstream.app
- test.zimmermann@mainstream.app
- test.fischer@mainstream.app
- test.meyer@mainstream.app

## 📊 Erstellte Daten im Detail

### Standard-Routen

1. **10km Vollrunde Main**
   - Distanz: 10.2 km
   - Höhenmeter: 45m
   - Beschreibung: Komplette Mainufer-Runde, beide Seiten

2. **5km Ostpark Loop**
   - Distanz: 5.3 km
   - Höhenmeter: 15m
   - Beschreibung: Schnelle Runde durch den Ostpark

3. **7km Nordmainufer**
   - Distanz: 7.1 km
   - Höhenmeter: 20m
   - Beschreibung: Nordseite des Mainufers, Hin und Zurück

4. **12km Stadtwald Trail**
   - Distanz: 12.5 km
   - Höhenmeter: 180m
   - Beschreibung: Anspruchsvolle Trail-Strecke

### Runs

Das Script erstellt über 20 Runs mit:
- Verschiedenen Distanzen (4.5 km - 15 km)
- Realistischen Paces (4:00 - 7:40 min/km)
- Unterschiedlichen Zeitpunkten (letzte 30 Tage)
- Wetterbedingungen
- Höhenprofilen
- Verknüpfung zu Standard-Routen

## 🔧 Konfiguration

### Datenbank-Credentials ändern

Falls deine Datenbank andere Credentials verwendet:

```bash
export DB_HOST=localhost
export DB_PORT=3306
export DB_USER=mainstream
export DB_PASSWORD='dein-passwort'

./seed-database.sh
```

### Einzelne Tabellen leeren

```sql
-- Nur Test-User löschen
DELETE FROM users WHERE email LIKE 'test%@mainstream.app';

-- Nur Runs löschen
DELETE FROM runs WHERE user_id IN (
  SELECT id FROM users WHERE email LIKE 'test%@mainstream.app'
);

-- Nur Routen löschen
DELETE FROM predefined_routes;
```

## 🧪 Testing der Landing Page

Nach dem Seeding:

1. **Backend starten** (falls noch nicht läuft):
   ```bash
   cd mainstream-backend
   ./mvnw spring-boot:run
   ```

2. **Frontend starten**:
   ```bash
   cd mainstream-frontend
   npm start
   ```

3. **Login** mit einem Test-Account (z.B. test.mueller@mainstream.app / password123)

4. **Landing Page aufrufen**: http://localhost:4200/landing

Du solltest jetzt sehen:
- **Personal Stats** mit Daten für den eingeloggten User
- **Community Map** mit den 4 Routen und User-Avataren

## 🔍 Daten verifizieren

### Alle Test-User anzeigen
```sql
SELECT id, email, first_name, last_name, fitness_level
FROM users
WHERE email LIKE 'test%@mainstream.app';
```

### Alle Routen anzeigen
```sql
SELECT id, name, distance_meters, elevation_gain_meters
FROM predefined_routes;
```

### Runs pro User anzeigen
```sql
SELECT
    u.first_name,
    u.last_name,
    COUNT(r.id) as run_count,
    SUM(r.distance_meters)/1000 as total_km
FROM users u
LEFT JOIN runs r ON u.id = r.user_id
WHERE u.email LIKE 'test%@mainstream.app'
GROUP BY u.id, u.first_name, u.last_name
ORDER BY run_count DESC;
```

### Runs mit Routen-Verknüpfung
```sql
SELECT
    r.title,
    r.distance_meters/1000 as distance_km,
    pr.name as route_name,
    u.first_name,
    u.last_name,
    r.start_time
FROM runs r
JOIN users u ON r.user_id = u.id
LEFT JOIN predefined_routes pr ON r.route_id = pr.id
WHERE u.email LIKE 'test%@mainstream.app'
ORDER BY r.start_time DESC;
```

## ⚠️ Wichtige Hinweise

1. **Passwort-Hashing**: Die Passwörter sind mit BCrypt gehashed (Spring Security Standard)
2. **Testdaten-Kennzeichnung**: Alle Test-User haben E-Mails mit `test.*@mainstream.app`
3. **Produktiv-Daten**: Das Script löscht **nur** Test-User, nicht deine echten Daten
4. **Wiederholte Ausführung**: Du kannst das Script beliebig oft ausführen - alte Testdaten werden gelöscht
5. **GPS-Punkte**: Das Seed-Script erstellt keine GPS-Punkte - diese werden normalerweise beim FIT/GPX-Upload generiert

## 🐛 Troubleshooting

### "MySQL Client nicht installiert"
```bash
# Debian/Ubuntu
sudo apt-get install mysql-client

# macOS
brew install mysql-client

# Windows (Git Bash)
# Verwende die MySQL-Installation oder Methode 3 (Docker)
```

### "Verbindung zur Datenbank fehlgeschlagen"
- Prüfe ob MariaDB läuft: `systemctl status mariadb` oder `docker ps`
- Prüfe Credentials in `mainstream-backend/src/main/resources/application.properties`
- Prüfe Firewall/Port 3306

### "Access denied for user"
- Passwort in application.properties prüfen
- Oder als Environment-Variable setzen: `export DB_PASSWORD='dein-passwort'`

### Backend findet keine Daten
- Prüfe ob Backend die richtige Datenbank verwendet
- Prüfe application.properties: `spring.datasource.url`
- Backend neu starten nach Seed

## 📝 Nächste Schritte

Nach erfolgreichem Seeding:

1. **Community Map anpassen**: Passe die SVG-Pfade in `community-map.service.ts` an echte Routen-Geometrie an
2. **Route-Matching**: Implementiere das Route-Matching-System im Backend
3. **Echte GPS-Daten**: Lade FIT/GPX-Dateien hoch um echte GPS-Tracks zu erstellen
4. **Achievements**: Füge Trophy-System Testdaten hinzu
5. **Performance**: Bei mehr als 100 Users - Pagination für Community Map implementieren

## 🤝 Support

Bei Problemen:
1. Logs prüfen: `tail -f mainstream-backend/logs/spring-boot.log`
2. Frontend Console öffnen (F12) und nach Fehlern suchen
3. SQL direkt in MySQL testen
4. Issue in GitHub erstellen

---

**Viel Erfolg beim Testen! 🏃‍♂️💨**
