# 🪟 MainStream Database Seeding - Windows Anleitung

Anleitung zum Befüllen der Datenbank mit Testdaten unter Windows.

## 🎯 Voraussetzungen

Du benötigst einen **MySQL Client**. Dieser ist enthalten in:

1. **MySQL Community Server** - https://dev.mysql.com/downloads/mysql/
2. **MariaDB** - https://mariadb.org/download/
3. **XAMPP** - https://www.apachefriends.org/
4. **WAMP** - https://www.wampserver.com/

Nach der Installation muss der MySQL-Pfad in der **PATH-Umgebungsvariable** sein, z.B.:
- `C:\Program Files\MySQL\MySQL Server 8.0\bin`
- `C:\xampp\mysql\bin`

### ✅ Prüfe MySQL Installation

Öffne PowerShell oder CMD und teste:

```bash
mysql --version
```

Wenn du eine Versionsnummer siehst (z.B. `mysql  Ver 8.0.35`), ist alles bereit!

## 🚀 Methode 1: PowerShell Script (Empfohlen)

### Schritt 1: PowerShell öffnen

Rechtsklick auf das **Start-Menü** → **Windows PowerShell** oder **Terminal**

### Schritt 2: In Projekt-Verzeichnis wechseln

```powershell
cd C:\dein\pfad\zu\mainstream
```

### Schritt 3: Script ausführen

```powershell
.\seed-database.ps1
```

**Falls Fehler "Ausführung von Skripts ist auf diesem System deaktiviert":**

```powershell
# Execution Policy temporär ändern (nur für diese Sitzung)
Set-ExecutionPolicy -ExecutionPolicy Bypass -Scope Process

# Dann nochmal:
.\seed-database.ps1
```

Das Script:
- ✅ Prüft MySQL Installation
- ✅ Testet Datenbankverbindung
- ✅ Zeigt aktuelle Daten
- ✅ Fragt nach Bestätigung
- ✅ Löscht alte Testdaten
- ✅ Importiert neue Daten
- ✅ Zeigt Statistiken

---

## 🚀 Methode 2: Batch Script (CMD)

### Schritt 1: CMD öffnen

**Start-Menü** → `cmd` eingeben → **Eingabetaste**

### Schritt 2: In Projekt-Verzeichnis wechseln

```cmd
cd C:\dein\pfad\zu\mainstream
```

### Schritt 3: Script ausführen

```cmd
seed-database.bat
```

Einfachere Version ohne erweiterte Checks, aber funktioniert überall.

---

## 🚀 Methode 3: Direkt mit MySQL (Manuell)

### Variante A: Mit MySQL Command Line Client

```cmd
# In Projekt-Verzeichnis wechseln
cd C:\dein\pfad\zu\mainstream

# Script ausführen
mysql -u mainstream -p -h localhost mainstream < mainstream-backend\src\main\resources\db\seed-testdata.sql

# Passwort eingeben wenn gefragt: taxcRH51#
```

### Variante B: Mit MySQL Workbench (GUI)

1. **MySQL Workbench** öffnen
2. Verbindung zur `mainstream` Datenbank herstellen
3. **File** → **Open SQL Script**
4. Datei auswählen: `mainstream-backend\src\main\resources\db\seed-testdata.sql`
5. **⚡ Execute** (Blitz-Symbol) klicken
6. Fertig! ✅

---

## 🚀 Methode 4: Java TestDataSeeder (Plattform-unabhängig)

Falls MySQL Client Probleme macht, nutze die Java-Version:

### Schritt 1: Backend starten mit Seed-Profile

```cmd
cd mainstream-backend
mvnw spring-boot:run -Dspring-boot.run.profiles=dev,seed-data
```

### Oder mit Gradle:

```cmd
cd mainstream-backend
gradlew bootRun --args='--spring.profiles.active=dev,seed-data'
```

Das Backend startet und füllt automatisch die Datenbank beim Start!

---

## 🔧 Konfiguration anpassen

### PowerShell:

```powershell
$env:DB_HOST = "localhost"
$env:DB_PORT = "3306"
$env:DB_USER = "mainstream"
$env:DB_PASSWORD = "dein-passwort"

.\seed-database.ps1
```

### CMD/Batch:

```cmd
set DB_HOST=localhost
set DB_PORT=3306
set DB_USER=mainstream
set DB_PASSWORD=dein-passwort

seed-database.bat
```

---

## 👤 Test-Accounts nach Seeding

**Admin:**
- Email: `admin@mainstream.app`
- Passwort: `password123`

**Test-User (16 Accounts):**
- `test.mueller@mainstream.app`
- `test.schmidt@mainstream.app`
- `test.weber@mainstream.app`
- `test.wagner@mainstream.app`
- ... und weitere

**Alle haben das Passwort:** `password123`

---

## 🔍 Daten prüfen

### Mit MySQL Command Line:

```cmd
mysql -u mainstream -p -h localhost mainstream

# In MySQL dann:
SELECT COUNT(*) FROM users WHERE email LIKE 'test%@mainstream.app';
SELECT COUNT(*) FROM predefined_routes;
SELECT COUNT(*) FROM runs;
```

### Mit MySQL Workbench:

Verbindung herstellen und SQL ausführen:

```sql
-- Alle Test-User
SELECT email, first_name, last_name FROM users
WHERE email LIKE 'test%@mainstream.app';

-- Alle Routen
SELECT name, distance_meters/1000 as km FROM predefined_routes;

-- Runs pro User
SELECT u.first_name, COUNT(r.id) as runs
FROM users u
LEFT JOIN runs r ON u.id = r.user_id
WHERE u.email LIKE 'test%@mainstream.app'
GROUP BY u.id;
```

---

## 🐛 Troubleshooting

### Problem: "mysql ist nicht als interner oder externer Befehl erkannt"

**Lösung:**

1. MySQL/MariaDB installieren (siehe Voraussetzungen)
2. MySQL bin-Verzeichnis zur PATH-Variable hinzufügen:

   **Windows 11/10:**
   - Start → "Umgebungsvariablen" suchen
   - **Umgebungsvariablen bearbeiten**
   - **Path** auswählen → **Bearbeiten**
   - **Neu** → Pfad hinzufügen (z.B. `C:\Program Files\MySQL\MySQL Server 8.0\bin`)
   - **OK** → **OK** → **OK**
   - **CMD/PowerShell neu starten!**

3. Oder vollständigen Pfad verwenden:

   ```cmd
   "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql" -u mainstream -p ...
   ```

### Problem: "Access denied for user 'mainstream'@'localhost'"

**Lösung:**

1. Passwort in `application.properties` prüfen
2. Oder eigenes Passwort setzen:

   ```cmd
   set DB_PASSWORD=dein-passwort
   seed-database.bat
   ```

### Problem: "Can't connect to MySQL server on 'localhost'"

**Lösung:**

1. Prüfe ob MySQL/MariaDB läuft:
   - **Task-Manager** → **Dienste** → Nach "MySQL" oder "MariaDB" suchen
   - Wenn gestoppt: Rechtsklick → **Starten**

2. Bei XAMPP/WAMP: Control Panel öffnen und MySQL/MariaDB starten

3. Port prüfen (Standard: 3306):

   ```cmd
   netstat -an | findstr "3306"
   ```

### Problem: PowerShell Execution Policy Fehler

**Lösung:**

```powershell
# Temporär für diese Sitzung erlauben
Set-ExecutionPolicy -ExecutionPolicy Bypass -Scope Process

# Oder dauerhaft für aktuellen User
Set-ExecutionPolicy -ExecutionPolicy RemoteSigned -Scope CurrentUser
```

### Problem: Script findet Seed-Datei nicht

**Lösung:**

Stelle sicher, dass du das Script aus dem **Projekt-Root** ausführst:

```cmd
cd C:\dein\pfad\zu\mainstream
dir mainstream-backend\src\main\resources\db\seed-testdata.sql

# Sollte die Datei anzeigen. Dann:
seed-database.bat
```

---

## 🎉 Erfolg prüfen

Nach erfolgreichem Seeding:

1. **Backend starten:**
   ```cmd
   cd mainstream-backend
   mvnw spring-boot:run
   ```

2. **Frontend starten:**
   ```cmd
   cd mainstream-frontend
   npm start
   ```

3. **Browser öffnen:** http://localhost:4200

4. **Einloggen** mit `test.mueller@mainstream.app` / `password123`

5. **Landing Page aufrufen:** http://localhost:4200/landing

Du solltest jetzt sehen:
- ✅ Personal Stats mit Daten
- ✅ Community Map mit 4 Routen
- ✅ User-Avatare auf den Routen

---

## 📚 Weitere Hilfe

- **Allgemeine Dokumentation:** `DATABASE_SEEDING.md`
- **SQL-Script Direktpfad:** `mainstream-backend\src\main\resources\db\seed-testdata.sql`
- **Java TestDataSeeder:** `mainstream-backend\src\main\java\com\mainstream\util\TestDataSeeder.java`

Bei Problemen: Issue auf GitHub erstellen! 🚀
