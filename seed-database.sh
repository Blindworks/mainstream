#!/bin/bash

# ================================================================
# MainStream Database Seeding Script
# Füllt die Datenbank mit Testdaten für die Landing Page
# ================================================================

set -e  # Exit on error

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Database credentials (aus application.properties)
DB_HOST="${DB_HOST:-localhost}"
DB_PORT="${DB_PORT:-3306}"
DB_NAME="${DB_NAME:-mainstream}"
DB_USER="${DB_USER:-mainstream}"
DB_PASSWORD="${DB_PASSWORD:-taxcRH51#}"

SEED_FILE="mainstream-backend/src/main/resources/db/seed-testdata.sql"

# Funktionen
print_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

print_header() {
    echo ""
    echo "================================================================"
    echo "$1"
    echo "================================================================"
    echo ""
}

# Check if mysql client is installed
check_mysql_client() {
    if ! command -v mysql &> /dev/null; then
        print_error "MySQL Client ist nicht installiert!"
        print_info "Installation unter Debian/Ubuntu: sudo apt-get install mysql-client"
        print_info "Installation unter macOS: brew install mysql-client"
        exit 1
    fi
}

# Test database connection
test_connection() {
    print_info "Teste Datenbankverbindung..."
    if mysql -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USER" -p"$DB_PASSWORD" -e "USE $DB_NAME;" 2>/dev/null; then
        print_success "Verbindung zur Datenbank erfolgreich!"
        return 0
    else
        print_error "Verbindung zur Datenbank fehlgeschlagen!"
        print_info "Host: $DB_HOST:$DB_PORT"
        print_info "Database: $DB_NAME"
        print_info "User: $DB_USER"
        return 1
    fi
}

# Check if seed file exists
check_seed_file() {
    if [ ! -f "$SEED_FILE" ]; then
        print_error "Seed-Datei nicht gefunden: $SEED_FILE"
        exit 1
    fi
    print_success "Seed-Datei gefunden: $SEED_FILE"
}

# Show current data count
show_current_data() {
    print_header "AKTUELLE DATEN"

    local user_count=$(mysql -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USER" -p"$DB_PASSWORD" -D"$DB_NAME" -se "SELECT COUNT(*) FROM users WHERE email LIKE 'test%@mainstream.app';" 2>/dev/null || echo "0")
    local route_count=$(mysql -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USER" -p"$DB_PASSWORD" -D"$DB_NAME" -se "SELECT COUNT(*) FROM predefined_routes;" 2>/dev/null || echo "0")
    local run_count=$(mysql -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USER" -p"$DB_PASSWORD" -D"$DB_NAME" -se "SELECT COUNT(*) FROM runs;" 2>/dev/null || echo "0")

    echo "Test-User:        $user_count"
    echo "Routen:           $route_count"
    echo "Runs:             $run_count"
    echo ""
}

# Clean existing test data
clean_test_data() {
    print_header "LÖSCHE EXISTIERENDE TESTDATEN"

    print_info "Lösche GPS-Punkte von Test-Runs..."
    mysql -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USER" -p"$DB_PASSWORD" -D"$DB_NAME" -e "
        DELETE FROM gps_points
        WHERE run_id IN (
            SELECT id FROM runs
            WHERE user_id IN (
                SELECT id FROM users WHERE email LIKE 'test%@mainstream.app'
            )
        );
    " 2>/dev/null || true

    print_info "Lösche Test-Runs..."
    mysql -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USER" -p"$DB_PASSWORD" -D"$DB_NAME" -e "
        DELETE FROM runs
        WHERE user_id IN (
            SELECT id FROM users WHERE email LIKE 'test%@mainstream.app'
        );
    " 2>/dev/null || true

    print_info "Lösche Route-TrackPoints..."
    mysql -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USER" -p"$DB_PASSWORD" -D"$DB_NAME" -e "
        DELETE FROM route_track_points;
    " 2>/dev/null || true

    print_info "Lösche Predefined Routes..."
    mysql -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USER" -p"$DB_PASSWORD" -D"$DB_NAME" -e "
        DELETE FROM predefined_routes;
    " 2>/dev/null || true

    print_info "Lösche Test-User..."
    mysql -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USER" -p"$DB_PASSWORD" -D"$DB_NAME" -e "
        DELETE FROM users WHERE email LIKE 'test%@mainstream.app';
    " 2>/dev/null || true

    print_success "Testdaten gelöscht!"
}

# Import seed data
import_seed_data() {
    print_header "IMPORTIERE TESTDATEN"

    print_info "Führe Seed-Script aus..."
    if mysql -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USER" -p"$DB_PASSWORD" -D"$DB_NAME" < "$SEED_FILE"; then
        print_success "Testdaten erfolgreich importiert!"
    else
        print_error "Fehler beim Importieren der Testdaten!"
        exit 1
    fi
}

# Show final statistics
show_statistics() {
    print_header "IMPORTIERTE DATEN"

    local user_count=$(mysql -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USER" -p"$DB_PASSWORD" -D"$DB_NAME" -se "SELECT COUNT(*) FROM users WHERE email LIKE 'test%@mainstream.app';" 2>/dev/null)
    local route_count=$(mysql -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USER" -p"$DB_PASSWORD" -D"$DB_NAME" -se "SELECT COUNT(*) FROM predefined_routes;" 2>/dev/null)
    local run_count=$(mysql -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USER" -p"$DB_PASSWORD" -D"$DB_NAME" -se "SELECT COUNT(*) FROM runs WHERE user_id IN (SELECT id FROM users WHERE email LIKE 'test%@mainstream.app');" 2>/dev/null)

    echo "✓ Test-User angelegt:     $user_count"
    echo "✓ Routen angelegt:        $route_count"
    echo "✓ Runs angelegt:          $run_count"
    echo ""

    print_info "Test-User Login-Daten:"
    echo "  Email:    test.mueller@mainstream.app (oder andere test.*@mainstream.app)"
    echo "  Passwort: password123"
    echo ""
    echo "  Admin-Account:"
    echo "  Email:    admin@mainstream.app"
    echo "  Passwort: password123"
    echo ""
}

# Main execution
main() {
    print_header "MAINSTREAM DATABASE SEEDER"

    # Check prerequisites
    print_info "Prüfe Voraussetzungen..."
    check_mysql_client
    check_seed_file

    # Test connection
    if ! test_connection; then
        print_error "Kann nicht fortfahren ohne Datenbankverbindung!"
        print_info ""
        print_info "Möglicherweise musst du die Datenbank-Credentials anpassen:"
        print_info "  export DB_HOST=localhost"
        print_info "  export DB_PORT=3306"
        print_info "  export DB_USER=mainstream"
        print_info "  export DB_PASSWORD='taxcRH51#'"
        exit 1
    fi

    # Show current state
    show_current_data

    # Ask for confirmation
    print_warning "Möchtest du die Datenbank mit Testdaten füllen?"
    print_warning "Existierende Testdaten werden GELÖSCHT!"
    echo ""
    read -p "Fortfahren? (ja/nein): " -r
    echo ""

    if [[ ! $REPLY =~ ^[Jj][Aa]$ ]] && [[ ! $REPLY =~ ^[Yy]([Ee][Ss])?$ ]]; then
        print_info "Abgebrochen."
        exit 0
    fi

    # Clean and import
    clean_test_data
    import_seed_data
    show_statistics

    print_success "=========================================="
    print_success "  Datenbank erfolgreich befüllt! 🎉"
    print_success "=========================================="
    echo ""
    print_info "Du kannst dich jetzt mit den Test-Accounts einloggen."
    print_info "Die Landing Page sollte nun mit Daten gefüllt sein!"
    echo ""
}

# Run main function
main "$@"
