@echo off
REM ================================================================
REM MainStream Database Seeding Script für Windows (CMD)
REM Einfache Version ohne interaktive Checks
REM ================================================================

setlocal

REM Database Credentials
if "%DB_HOST%"=="" set DB_HOST=localhost
if "%DB_PORT%"=="" set DB_PORT=3306
if "%DB_NAME%"=="" set DB_NAME=mainstream
if "%DB_USER%"=="" set DB_USER=mainstream
if "%DB_PASSWORD%"=="" set DB_PASSWORD=taxcRH51#

set SEED_FILE=mainstream-backend\src\main\resources\db\seed-testdata.sql

echo ================================================================
echo   MAINSTREAM DATABASE SEEDER
echo ================================================================
echo.

REM Prüfe ob Seed-Datei existiert
if not exist "%SEED_FILE%" (
    echo [ERROR] Seed-Datei nicht gefunden: %SEED_FILE%
    echo.
    echo Bitte stelle sicher, dass du das Script aus dem Projekt-Root ausfuehrst.
    echo.
    pause
    exit /b 1
)

echo [INFO] Seed-Datei gefunden: %SEED_FILE%
echo [INFO] Host: %DB_HOST%:%DB_PORT%
echo [INFO] Database: %DB_NAME%
echo [INFO] User: %DB_USER%
echo.

REM Warnung
echo [WARNING] Existierende Testdaten werden GELOESCHT!
echo.
set /p CONFIRM="Fortfahren? (ja/nein): "
echo.

if /i not "%CONFIRM%"=="ja" if /i not "%CONFIRM%"=="j" if /i not "%CONFIRM%"=="yes" if /i not "%CONFIRM%"=="y" (
    echo [INFO] Abgebrochen.
    pause
    exit /b 0
)

echo ================================================================
echo   IMPORTIERE TESTDATEN
echo ================================================================
echo.

REM Importiere Seed-Daten
echo [INFO] Fuehre Seed-Script aus...
mysql -h %DB_HOST% -P %DB_PORT% -u %DB_USER% -p%DB_PASSWORD% -D %DB_NAME% < "%SEED_FILE%"

if %ERRORLEVEL% neq 0 (
    echo.
    echo [ERROR] Fehler beim Importieren der Testdaten!
    echo.
    echo Moegliche Ursachen:
    echo - MySQL Client nicht installiert oder nicht im PATH
    echo - Falsche Datenbank-Credentials
    echo - Datenbank nicht erreichbar
    echo.
    pause
    exit /b 1
)

echo.
echo [SUCCESS] Testdaten erfolgreich importiert!
echo.

echo ================================================================
echo   IMPORTIERTE DATEN
echo ================================================================
echo.

REM Zeige Statistiken
for /f %%i in ('mysql -h %DB_HOST% -P %DB_PORT% -u %DB_USER% -p%DB_PASSWORD% -D %DB_NAME% -se "SELECT COUNT(*) FROM users WHERE email LIKE 'test%%@mainstream.app';" 2^>nul') do set USER_COUNT=%%i
for /f %%i in ('mysql -h %DB_HOST% -P %DB_PORT% -u %DB_USER% -p%DB_PASSWORD% -D %DB_NAME% -se "SELECT COUNT(*) FROM predefined_routes;" 2^>nul') do set ROUTE_COUNT=%%i
for /f %%i in ('mysql -h %DB_HOST% -P %DB_PORT% -u %DB_USER% -p%DB_PASSWORD% -D %DB_NAME% -se "SELECT COUNT(*) FROM runs;" 2^>nul') do set RUN_COUNT=%%i

echo Test-User angelegt:     %USER_COUNT%
echo Routen angelegt:        %ROUTE_COUNT%
echo Runs angelegt:          %RUN_COUNT%
echo.

echo Test-User Login-Daten:
echo   Email:    test.mueller@mainstream.app
echo   Passwort: password123
echo.
echo   Admin-Account:
echo   Email:    admin@mainstream.app
echo   Passwort: password123
echo.

echo ================================================================
echo   Datenbank erfolgreich befuellt!
echo ================================================================
echo.
echo Du kannst dich jetzt mit den Test-Accounts einloggen.
echo Die Landing Page sollte nun mit Daten gefuellt sein!
echo.

pause
