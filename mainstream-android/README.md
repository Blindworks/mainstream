# Mainstream Android App

Eine moderne Android-App für die Mainstream-Plattform mit Fokus auf Trophäen und Laufrouten.

## Features

- **Login & Authentifizierung**: JWT-basierte Authentifizierung mit Token-Management
- **Trophäen-Ansicht**: Übersicht über verdiente Trophäen, Fortschritt und Tagesherausforderungen
- **Routen-Ansicht**: Liste aller verfügbaren Laufrouten mit Details und Statistiken
- **Moderne UI**: Material Design 3 mit Jetpack Compose

## Tech Stack

- **Kotlin**: Moderne Programmiersprache für Android
- **Jetpack Compose**: Deklaratives UI-Framework
- **Hilt**: Dependency Injection
- **Retrofit**: REST API Client
- **DataStore**: Lokale Datenspeicherung für Token
- **Material Design 3**: Modernes Design-System
- **Navigation Compose**: Navigation zwischen Screens
- **Coil**: Bildlade-Bibliothek
- **Google Maps**: Kartendarstellung für Routen

## Projektstruktur

```
app/src/main/java/com/mainstream/app/
├── data/
│   ├── api/          # Retrofit API Interfaces
│   ├── local/        # DataStore und lokale Datenverwaltung
│   ├── model/        # Datenmodelle (User, Trophy, Route)
│   └── repository/   # Repository-Klassen für Datenzugriff
├── di/               # Dependency Injection Module
├── navigation/       # Navigation-Konfiguration
└── ui/
    ├── auth/         # Login/Registration Screens
    ├── trophies/     # Trophäen-Ansicht
    ├── routes/       # Routen-Ansicht
    └── theme/        # App-Theming
```

## API-Konfiguration

Die App verbindet sich standardmäßig mit `http://10.0.2.2:8080` (Android Emulator localhost).

Für ein physisches Gerät muss die BASE_URL in `NetworkModule.kt` angepasst werden:

```kotlin
private const val BASE_URL = "http://YOUR_SERVER_IP:8080/"
```

## Setup & Build

1. **Voraussetzungen**:
   - Android Studio Hedgehog oder neuer
   - JDK 17
   - Android SDK 34

2. **Projekt öffnen**:
   ```bash
   cd mainstream-android
   # Öffne das Projekt in Android Studio
   ```

3. **Backend starten**:
   ```bash
   # Im Hauptverzeichnis
   cd mainstream-backend
   ./mvnw spring-boot:run
   ```

4. **App bauen & ausführen**:
   - In Android Studio: Run → Run 'app'
   - Oder via Command Line:
   ```bash
   ./gradlew assembleDebug
   ```

## Verwendete API-Endpunkte

### Authentifizierung
- `POST /api/auth/login` - Benutzer-Login
- `POST /api/auth/register` - Benutzer-Registrierung
- `GET /api/auth/user` - Aktuellen Benutzer abrufen
- `POST /api/auth/validate` - Token validieren

### Trophäen
- `GET /api/trophies` - Alle Trophäen
- `GET /api/trophies/my` - Benutzertrophäen
- `GET /api/trophies/progress` - Trophäen-Fortschritt
- `GET /api/trophies/daily/today` - Trophäe des Tages
- `GET /api/trophies/weekly` - Wöchentliche Trophäen

### Routen
- `GET /api/routes` - Alle Routen
- `GET /api/routes/with-stats` - Routen mit Statistiken
- `GET /api/routes/{id}` - Einzelne Route mit Trackpoints

## Features im Detail

### Login
- E-Mail und Passwort-basierte Authentifizierung
- JWT-Token wird sicher in DataStore gespeichert
- Automatische Session-Verwaltung
- Fehlerbehandlung und Benutzer-Feedback

### Trophäen
- Anzeige aller verdienten Trophäen
- Fortschrittsbalken für noch nicht erreichte Trophäen
- Kategorisierung nach Schwierigkeitsgrad
- Trophäe des Tages hervorgehoben
- Pull-to-Refresh Unterstützung

### Routen
- Liste aller verfügbaren Laufrouten
- Detailansicht mit Distanz, Höhenunterschied
- Statistiken (Heute, diese Woche, gesamt)
- Bild-Vorschau wenn verfügbar
- Städte-Filter möglich

## Nächste Schritte

Mögliche Erweiterungen:
- [ ] Kartenansicht für Routen mit Google Maps
- [ ] Aktivitäts-Tracking
- [ ] Benutzer-Profil Bearbeitung
- [ ] Push-Benachrichtigungen für neue Trophäen
- [ ] Social Features (Freunde, Ranglisten)
- [ ] Offline-Modus
- [ ] Integration mit Strava/Garmin/Nike

## Lizenz

Dieses Projekt ist Teil der Mainstream-Plattform.
