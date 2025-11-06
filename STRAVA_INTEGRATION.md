# Strava Integration Setup

Diese Anleitung erklärt, wie die Strava-Integration für MainStream eingerichtet wird.

## Voraussetzungen

1. Ein Strava-Konto
2. Eine registrierte Strava-API-Anwendung

## Schritt 1: Strava API Anwendung erstellen

1. Gehe zu [Strava API Settings](https://www.strava.com/settings/api)
2. Erstelle eine neue Anwendung mit folgenden Details:
   - **Application Name**: MainStream (oder ein Name deiner Wahl)
   - **Category**: Wähle eine passende Kategorie (z.B. "Fitness")
   - **Website**: `http://localhost:4200` (für lokale Entwicklung)
   - **Authorization Callback Domain**: `localhost`

3. Nach dem Erstellen erhältst du:
   - **Client ID**
   - **Client Secret**

## Schritt 2: Umgebungsvariablen konfigurieren

### Backend-Konfiguration

Setze die folgenden Umgebungsvariablen oder aktualisiere die `application.properties`:

```bash
export STRAVA_CLIENT_ID="your-client-id-here"
export STRAVA_CLIENT_SECRET="your-client-secret-here"
```

Oder in `mainstream-backend/src/main/resources/application.properties`:

```properties
mainstream.strava.client-id=your-client-id-here
mainstream.strava.client-secret=your-client-secret-here
```

## Schritt 3: Datenbank aktualisieren

Die neue Strava-Integration fügt neue Spalten zur `users` und `runs` Tabelle hinzu:

### Users Tabelle:
- `strava_user_id` (BIGINT)
- `strava_access_token` (VARCHAR(500))
- `strava_refresh_token` (VARCHAR(500))
- `strava_token_expires_at` (DATETIME)
- `strava_connected_at` (DATETIME)

### Runs Tabelle:
- `strava_activity_id` (BIGINT)

Da du Hibernate mit `ddl-auto=update` verwendest, werden die Spalten automatisch erstellt beim nächsten Start der Anwendung.

## Schritt 4: Backend starten

```bash
cd mainstream-backend
./mvnw spring-boot:run
```

## Schritt 5: Frontend starten

```bash
cd mainstream-frontend
npm install
ng serve
```

## Verwendung

### Strava-Konto verbinden

1. Melde dich in MainStream an
2. Gehe zu **Profil** (`/profile`)
3. Klicke auf **Connect to Strava**
4. Autorisiere MainStream in dem sich öffnenden Strava-Fenster
5. Nach erfolgreicher Autorisierung wird dein Konto verbunden

### Aktivitäten synchronisieren

1. Nachdem dein Strava-Konto verbunden ist, klicke auf **Sync Activities**
2. Die Synchronisation lädt alle Lauf-Aktivitäten der letzten 30 Tage
3. Die synchronisierten Runs erscheinen in deiner Run-Liste

### Strava-Konto trennen

1. Gehe zu **Profil** (`/profile`)
2. Klicke auf **Disconnect**
3. Bestätige die Aktion

## API-Endpunkte

Die folgenden REST-Endpunkte sind verfügbar:

### GET `/api/strava/auth-url`
Gibt die Strava-Autorisierungs-URL zurück.

**Response:**
```json
{
  "authUrl": "https://www.strava.com/oauth/authorize?..."
}
```

### POST `/api/strava/connect?code={authorization_code}`
Verbindet das Strava-Konto eines Benutzers.

**Headers:** `X-User-Id: {userId}`

**Response:**
```json
{
  "success": true,
  "message": "Successfully connected to Strava",
  "stravaUserId": 12345678,
  "connectedAt": "2025-11-06T10:30:00"
}
```

### DELETE `/api/strava/disconnect`
Trennt das Strava-Konto.

**Headers:** `X-User-Id: {userId}`

**Response:**
```json
{
  "success": true,
  "message": "Successfully disconnected from Strava"
}
```

### POST `/api/strava/sync?since={ISO-DateTime}`
Synchronisiert Aktivitäten von Strava.

**Headers:** `X-User-Id: {userId}`

**Query Parameters:**
- `since` (optional): Synchronisiert nur Aktivitäten nach diesem Datum (ISO 8601 Format)
- Standard: 30 Tage zurück

**Response:**
```json
{
  "success": true,
  "message": "Successfully synced activities from Strava",
  "syncedCount": 5,
  "runs": [...]
}
```

### GET `/api/strava/status`
Gibt den Verbindungsstatus zurück.

**Headers:** `X-User-Id: {userId}`

**Response:**
```json
{
  "connected": true,
  "stravaUserId": 12345678,
  "connectedAt": "2025-11-06T10:30:00"
}
```

## Datenmodell

### Strava Activity → Run Mapping

Folgende Felder werden von Strava-Aktivitäten übernommen:

| Strava Feld | Run Feld | Konvertierung |
|-------------|----------|---------------|
| `name` | `title` | Direkt |
| `description` | `description` | Direkt |
| `start_date_local` | `startTime` | Zu LocalDateTime |
| `moving_time` | `durationSeconds` | Direkt |
| `distance` | `distanceMeters` | Meter (Direkt) |
| `average_speed` | `averageSpeedKmh` | m/s → km/h (× 3.6) |
| `max_speed` | `maxSpeedKmh` | m/s → km/h (× 3.6) |
| `total_elevation_gain` | `elevationGainMeters` | Direkt |
| `calories` | `caloriesBurned` | Direkt |
| `id` | `stravaActivityId` | Direkt |

### Wichtige Hinweise

- Nur Aktivitäten vom Typ "Run" werden synchronisiert
- Bereits synchronisierte Aktivitäten werden übersprungen (basierend auf `stravaActivityId`)
- Strava-Tokens werden automatisch aktualisiert, wenn sie ablaufen
- Alle synchronisierten Runs haben den Status `COMPLETED`

## Fehlerbehebung

### "User not found" Fehler
Stelle sicher, dass der `X-User-Id` Header korrekt gesetzt ist und der Benutzer existiert.

### "User is not connected to Strava" Fehler
Der Benutzer muss zuerst sein Strava-Konto über `/api/strava/connect` verbinden.

### Token Expiration
Access Tokens laufen nach einigen Stunden ab. Der Service aktualisiert sie automatisch mit dem Refresh Token.

### Keine Aktivitäten synchronisiert
- Prüfe, ob die Aktivitäten vom Typ "Run" sind
- Prüfe das `since` Datum - Aktivitäten davor werden nicht synchronisiert
- Prüfe, ob die Aktivitäten bereits synchronisiert wurden

## Sicherheitshinweise

1. **Niemals** Client ID und Client Secret in Git committen
2. Verwende Umgebungsvariablen für sensitive Daten
3. In Produktion: HTTPS für alle API-Aufrufe verwenden
4. Access Tokens werden verschlüsselt in der Datenbank gespeichert (empfohlen für Produktion)

## Support

Bei Problemen oder Fragen:
1. Prüfe die Backend-Logs auf Fehler
2. Prüfe die Browser-Konsole auf Frontend-Fehler
3. Verifiziere die Strava API Settings

## Weitere Informationen

- [Strava API Dokumentation](https://developers.strava.com/docs/reference/)
- [Strava OAuth Flow](https://developers.strava.com/docs/authentication/)
