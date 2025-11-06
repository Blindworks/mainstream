# ================================================================
# MainStream Database Seeding Script für Windows (PowerShell)
# Füllt die Datenbank mit Testdaten für die Landing Page
# ================================================================

# Fehler bei Problemen stoppen
$ErrorActionPreference = "Stop"

# Database Credentials (aus application.properties)
$DB_HOST = if ($env:DB_HOST) { $env:DB_HOST } else { "localhost" }
$DB_PORT = if ($env:DB_PORT) { $env:DB_PORT } else { "3306" }
$DB_NAME = if ($env:DB_NAME) { $env:DB_NAME } else { "mainstream" }
$DB_USER = if ($env:DB_USER) { $env:DB_USER } else { "mainstream" }
$DB_PASSWORD = if ($env:DB_PASSWORD) { $env:DB_PASSWORD } else { "taxcRH51#" }

$SEED_FILE = "mainstream-backend\src\main\resources\db\seed-testdata.sql"

# Funktionen für farbige Ausgabe
function Write-Info {
    param([string]$Message)
    Write-Host "[INFO] $Message" -ForegroundColor Blue
}

function Write-Success {
    param([string]$Message)
    Write-Host "[SUCCESS] $Message" -ForegroundColor Green
}

function Write-Warning {
    param([string]$Message)
    Write-Host "[WARNING] $Message" -ForegroundColor Yellow
}

function Write-Error-Message {
    param([string]$Message)
    Write-Host "[ERROR] $Message" -ForegroundColor Red
}

function Write-Header {
    param([string]$Message)
    Write-Host ""
    Write-Host "================================================================" -ForegroundColor Cyan
    Write-Host $Message -ForegroundColor Cyan
    Write-Host "================================================================" -ForegroundColor Cyan
    Write-Host ""
}

# Prüfe ob MySQL Client installiert ist
function Test-MySQLClient {
    Write-Info "Prüfe MySQL Client Installation..."

    $mysqlPath = Get-Command mysql -ErrorAction SilentlyContinue

    if (-not $mysqlPath) {
        Write-Error-Message "MySQL Client ist nicht installiert oder nicht im PATH!"
        Write-Host ""
        Write-Info "Installation-Optionen:"
        Write-Info "1. MySQL Community Server: https://dev.mysql.com/downloads/mysql/"
        Write-Info "2. MariaDB: https://mariadb.org/download/"
        Write-Info "3. XAMPP: https://www.apachefriends.org/"
        Write-Host ""
        Write-Info "Nach Installation muss MySQL im PATH sein, z.B.:"
        Write-Info "C:\Program Files\MySQL\MySQL Server 8.0\bin"
        Write-Host ""
        return $false
    }

    Write-Success "MySQL Client gefunden: $($mysqlPath.Source)"
    return $true
}

# Teste Datenbankverbindung
function Test-DatabaseConnection {
    Write-Info "Teste Datenbankverbindung..."

    $testQuery = "USE $DB_NAME;"

    try {
        $result = & mysql -h $DB_HOST -P $DB_PORT -u $DB_USER -p"$DB_PASSWORD" -e $testQuery 2>&1

        if ($LASTEXITCODE -eq 0) {
            Write-Success "Verbindung zur Datenbank erfolgreich!"
            return $true
        }
        else {
            Write-Error-Message "Verbindung zur Datenbank fehlgeschlagen!"
            Write-Info "Host: ${DB_HOST}:${DB_PORT}"
            Write-Info "Database: $DB_NAME"
            Write-Info "User: $DB_USER"
            Write-Host ""
            Write-Host "Fehler: $result" -ForegroundColor Red
            return $false
        }
    }
    catch {
        Write-Error-Message "Verbindung zur Datenbank fehlgeschlagen!"
        Write-Host "Fehler: $_" -ForegroundColor Red
        return $false
    }
}

# Prüfe ob Seed-Datei existiert
function Test-SeedFile {
    if (-not (Test-Path $SEED_FILE)) {
        Write-Error-Message "Seed-Datei nicht gefunden: $SEED_FILE"
        Write-Info "Bitte stelle sicher, dass du das Script aus dem Projekt-Root ausführst."
        return $false
    }
    Write-Success "Seed-Datei gefunden: $SEED_FILE"
    return $true
}

# Zeige aktuelle Daten
function Show-CurrentData {
    Write-Header "AKTUELLE DATEN"

    try {
        $userCount = & mysql -h $DB_HOST -P $DB_PORT -u $DB_USER -p"$DB_PASSWORD" -D $DB_NAME -se "SELECT COUNT(*) FROM users WHERE email LIKE 'test%@mainstream.app';" 2>$null
        $routeCount = & mysql -h $DB_HOST -P $DB_PORT -u $DB_USER -p"$DB_PASSWORD" -D $DB_NAME -se "SELECT COUNT(*) FROM predefined_routes;" 2>$null
        $runCount = & mysql -h $DB_HOST -P $DB_PORT -u $DB_USER -p"$DB_PASSWORD" -D $DB_NAME -se "SELECT COUNT(*) FROM runs;" 2>$null

        Write-Host "Test-User:        $userCount"
        Write-Host "Routen:           $routeCount"
        Write-Host "Runs:             $runCount"
        Write-Host ""
    }
    catch {
        Write-Warning "Konnte aktuelle Daten nicht abrufen."
    }
}

# Lösche existierende Testdaten
function Remove-TestData {
    Write-Header "LÖSCHE EXISTIERENDE TESTDATEN"

    try {
        Write-Info "Lösche GPS-Punkte von Test-Runs..."
        & mysql -h $DB_HOST -P $DB_PORT -u $DB_USER -p"$DB_PASSWORD" -D $DB_NAME -e @"
DELETE FROM gps_points
WHERE run_id IN (
    SELECT id FROM runs
    WHERE user_id IN (
        SELECT id FROM users WHERE email LIKE 'test%@mainstream.app'
    )
);
"@ 2>$null

        Write-Info "Lösche Test-Runs..."
        & mysql -h $DB_HOST -P $DB_PORT -u $DB_USER -p"$DB_PASSWORD" -D $DB_NAME -e @"
DELETE FROM runs
WHERE user_id IN (
    SELECT id FROM users WHERE email LIKE 'test%@mainstream.app'
);
"@ 2>$null

        Write-Info "Lösche User-Activities..."
        & mysql -h $DB_HOST -P $DB_PORT -u $DB_USER -p"$DB_PASSWORD" -D $DB_NAME -e @"
DELETE FROM user_activities
WHERE user_id IN (
    SELECT id FROM users WHERE email LIKE 'test%@mainstream.app'
);
"@ 2>$null

        Write-Info "Lösche User-Activities mit Route-Referenzen..."
        & mysql -h $DB_HOST -P $DB_PORT -u $DB_USER -p"$DB_PASSWORD" -D $DB_NAME -e "DELETE FROM user_activities WHERE matched_route_id IS NOT NULL;" 2>$null

        Write-Info "Lösche Route-TrackPoints..."
        & mysql -h $DB_HOST -P $DB_PORT -u $DB_USER -p"$DB_PASSWORD" -D $DB_NAME -e "DELETE FROM route_track_points;" 2>$null

        Write-Info "Lösche Predefined Routes..."
        & mysql -h $DB_HOST -P $DB_PORT -u $DB_USER -p"$DB_PASSWORD" -D $DB_NAME -e "DELETE FROM predefined_routes;" 2>$null

        Write-Info "Lösche Test-User..."
        & mysql -h $DB_HOST -P $DB_PORT -u $DB_USER -p"$DB_PASSWORD" -D $DB_NAME -e "DELETE FROM users WHERE email LIKE 'test%@mainstream.app';" 2>$null

        Write-Success "Testdaten gelöscht!"
    }
    catch {
        Write-Warning "Fehler beim Löschen der Testdaten (möglicherweise existierten keine Daten)."
    }
}

# Importiere Seed-Daten
function Import-SeedData {
    Write-Header "IMPORTIERE TESTDATEN"

    Write-Info "Führe Seed-Script aus..."

    try {
        Get-Content $SEED_FILE | & mysql -h $DB_HOST -P $DB_PORT -u $DB_USER -p"$DB_PASSWORD" -D $DB_NAME

        if ($LASTEXITCODE -eq 0) {
            Write-Success "Testdaten erfolgreich importiert!"
            return $true
        }
        else {
            Write-Error-Message "Fehler beim Importieren der Testdaten!"
            return $false
        }
    }
    catch {
        Write-Error-Message "Fehler beim Importieren der Testdaten!"
        Write-Host "Fehler: $_" -ForegroundColor Red
        return $false
    }
}

# Zeige finale Statistiken
function Show-Statistics {
    Write-Header "IMPORTIERTE DATEN"

    try {
        $userCount = & mysql -h $DB_HOST -P $DB_PORT -u $DB_USER -p"$DB_PASSWORD" -D $DB_NAME -se "SELECT COUNT(*) FROM users WHERE email LIKE 'test%@mainstream.app';" 2>$null
        $routeCount = & mysql -h $DB_HOST -P $DB_PORT -u $DB_USER -p"$DB_PASSWORD" -D $DB_NAME -se "SELECT COUNT(*) FROM predefined_routes;" 2>$null
        $runCount = & mysql -h $DB_HOST -P $DB_PORT -u $DB_USER -p"$DB_PASSWORD" -D $DB_NAME -se "SELECT COUNT(*) FROM runs WHERE user_id IN (SELECT id FROM users WHERE email LIKE 'test%@mainstream.app');" 2>$null

        Write-Host "✓ Test-User angelegt:     $userCount" -ForegroundColor Green
        Write-Host "✓ Routen angelegt:        $routeCount" -ForegroundColor Green
        Write-Host "✓ Runs angelegt:          $runCount" -ForegroundColor Green
        Write-Host ""

        Write-Info "Test-User Login-Daten:"
        Write-Host "  Email:    test.mueller@mainstream.app (oder andere test.*@mainstream.app)"
        Write-Host "  Passwort: password123"
        Write-Host ""
        Write-Host "  Admin-Account:"
        Write-Host "  Email:    admin@mainstream.app"
        Write-Host "  Passwort: password123"
        Write-Host ""
    }
    catch {
        Write-Warning "Konnte Statistiken nicht abrufen."
    }
}

# Hauptfunktion
function Main {
    Write-Header "MAINSTREAM DATABASE SEEDER"

    # Prüfe Voraussetzungen
    Write-Info "Prüfe Voraussetzungen..."

    if (-not (Test-MySQLClient)) {
        exit 1
    }

    if (-not (Test-SeedFile)) {
        exit 1
    }

    if (-not (Test-DatabaseConnection)) {
        Write-Error-Message "Kann nicht fortfahren ohne Datenbankverbindung!"
        Write-Host ""
        Write-Info "Möglicherweise musst du die Datenbank-Credentials anpassen:"
        Write-Info '  $env:DB_HOST = "localhost"'
        Write-Info '  $env:DB_PORT = "3306"'
        Write-Info '  $env:DB_USER = "mainstream"'
        Write-Info '  $env:DB_PASSWORD = "taxcRH51#"'
        Write-Host ""
        exit 1
    }

    # Zeige aktuellen Status
    Show-CurrentData

    # Frage nach Bestätigung
    Write-Warning "Möchtest du die Datenbank mit Testdaten füllen?"
    Write-Warning "Existierende Testdaten werden GELÖSCHT!"
    Write-Host ""
    $confirmation = Read-Host "Fortfahren? (ja/nein)"
    Write-Host ""

    if ($confirmation -ne "ja" -and $confirmation -ne "j" -and $confirmation -ne "yes" -and $confirmation -ne "y") {
        Write-Info "Abgebrochen."
        exit 0
    }

    # Cleanup und Import
    Remove-TestData

    if (Import-SeedData) {
        Show-Statistics

        Write-Host ""
        Write-Success "=========================================="
        Write-Success "  Datenbank erfolgreich befüllt! 🎉"
        Write-Success "=========================================="
        Write-Host ""
        Write-Info "Du kannst dich jetzt mit den Test-Accounts einloggen."
        Write-Info "Die Landing Page sollte nun mit Daten gefüllt sein!"
        Write-Host ""
    }
    else {
        Write-Error-Message "Seeding fehlgeschlagen!"
        exit 1
    }
}

# Script ausführen
Main
