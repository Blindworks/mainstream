# Mainstream iOS App

Eine native iOS-App für das Mainstream Lauf-Tracking-System, entwickelt mit SwiftUI.

## Features

### 🏆 Trophäen
- Anzeige aller verfügbaren Trophäen
- Übersicht über erhaltene und verfügbare Trophäen
- Filterung nach Status (Alle, Erhalten, Verfügbar)
- Detaillierte Trophäeninformationen mit Kategorien und Beschreibungen

### 🗺️ Routen
- Liste aller vordefinierten Laufrouten
- Detailansicht mit Streckenprofil und Statistiken
- Anzeige von Distanz, Höhenmetern und Laufzahlen
- Filter nach Stadt möglich

### 🏃 Läufe
- Übersicht aller persönlichen Läufe
- Detailansicht mit vollständigen Laufstatistiken
- Pace, Geschwindigkeit, Kalorien, Höhenmeter
- Zuordnung zu vordefinierten Routen

### 👤 Profil
- Benutzerinformationen
- App-Einstellungen
- Backend-URL-Konfiguration
- Logout-Funktion

## Projekt-Struktur

```
Mainstream/
├── Models/               # Datenmodelle
│   ├── User.swift
│   ├── Trophy.swift
│   ├── Route.swift
│   └── Run.swift
├── Services/            # API-Services
│   ├── APIService.swift
│   ├── AuthService.swift
│   ├── TrophyService.swift
│   ├── RouteService.swift
│   └── RunService.swift
├── Views/               # SwiftUI Views
│   ├── LoginView.swift
│   ├── MainTabView.swift
│   ├── TrophiesView.swift
│   ├── RoutesView.swift
│   ├── UserRunsView.swift
│   └── ProfileView.swift
└── MainstreamApp.swift  # App-Einstiegspunkt
```

## Einrichtung

### Voraussetzungen
- macOS mit Xcode 15.0 oder höher
- iOS 17.0 oder höher (Target)
- Laufendes Mainstream Backend

### Installation

1. **Xcode-Projekt erstellen:**
   - Öffne Xcode
   - Wähle "Create a new Xcode project"
   - Wähle "iOS" → "App"
   - Projektname: `Mainstream`
   - Interface: SwiftUI
   - Language: Swift
   - Bundle Identifier: `com.mainstream.app` (oder deine eigene)

2. **Dateien hinzufügen:**
   - Lösche die automatisch erstellte `ContentView.swift`
   - Kopiere alle Dateien aus diesem Verzeichnis in dein Xcode-Projekt
   - Stelle sicher, dass die Ordnerstruktur (Models, Services, Views) erhalten bleibt

3. **Backend-URL konfigurieren:**
   - In `Services/APIService.swift` die `baseURL` anpassen:
   ```swift
   var baseURL: String = "http://deine-backend-url:8080"
   ```
   - Oder in der App unter Profil → Einstellungen konfigurieren

4. **Info.plist konfigurieren:**
   - Für HTTP-Verbindungen (localhost/development) App Transport Security konfigurieren
   - Siehe `Info.plist` Beispiel unten

### Info.plist Konfiguration

Für Development mit lokalem Backend (HTTP):

```xml
<key>NSAppTransportSecurity</key>
<dict>
    <key>NSAllowsArbitraryLoads</key>
    <true/>
</dict>
```

**Wichtig:** Für Production sollte das Backend HTTPS verwenden!

## API-Endpunkte

Die App verwendet folgende Backend-Endpunkte:

### Authentication
- `POST /api/auth/login` - Login
- `POST /api/auth/validate` - Token-Validierung

### Trophäen
- `GET /api/trophies` - Alle Trophäen
- `GET /api/trophies/my` - Erhaltene Trophäen des Users

### Routen
- `GET /api/routes/with-stats` - Routen mit Statistiken
- `GET /api/routes/{id}` - Route Details

### Läufe
- `GET /api/runs?page=0&size=20` - Läufe des Users (paginiert)
- `GET /api/runs/{id}` - Lauf Details

## Verwendung

1. **Login:**
   - Starte die App
   - Gib deine E-Mail und Passwort ein
   - Nach erfolgreichem Login gelangst du zur Hauptansicht

2. **Navigation:**
   - Verwende die Tab-Bar am unteren Bildschirmrand
   - Wechsle zwischen Trophäen, Routen, Läufen und Profil

3. **Trophäen:**
   - Sieh alle verfügbaren Trophäen
   - Filtere nach Status
   - Erhalte Details zu jeder Trophäe

4. **Routen:**
   - Durchsuche vordefinierte Routen
   - Tippe auf eine Route für Details
   - Sieh Statistiken und Laufzahlen

5. **Läufe:**
   - Sieh deine persönlichen Läufe
   - Tippe auf einen Lauf für Details
   - Lade mehr Läufe mit "Mehr laden"

## Entwicklung

### Anpassungen

**Backend-URL ändern:**
```swift
// In APIService.swift
var baseURL: String = "http://neue-url:8080"
```

**Styling anpassen:**
- Farben können in den Views angepasst werden
- SwiftUI unterstützt dynamische Farben und Dark Mode automatisch

### Fehlerbehandlung

Die App verwendet strukturierte Fehlerbehandlung:
- `APIError` enum für API-spezifische Fehler
- Benutzerfreundliche Fehlermeldungen
- Automatische Token-Validierung und Logout bei Authentifizierungsfehlern

### Datenpersistenz

- Login-Token wird in `UserDefaults` gespeichert
- Automatische Session-Wiederherstellung beim App-Start
- Sichere Token-Verwaltung über `AuthService`

## Technologie-Stack

- **SwiftUI** - Modernes UI-Framework von Apple
- **Combine** - Reaktive Programmierung für State Management
- **URLSession** - Netzwerk-Kommunikation
- **async/await** - Moderne asynchrone Programmierung

## Roadmap

Zukünftige Features:
- [ ] Lauf-Aufzeichnung mit GPS
- [ ] Offline-Unterstützung
- [ ] Push-Benachrichtigungen für neue Trophäen
- [ ] Teilen von Läufen und Erfolgen
- [ ] Detaillierte Laufanalyse mit Charts
- [ ] Apple Health Integration
- [ ] Widget-Support

## Lizenz

Dieses Projekt ist Teil des Mainstream Lauf-Tracking-Systems.

## Support

Bei Fragen oder Problemen:
1. Überprüfe die Backend-Verbindung
2. Stelle sicher, dass das Backend läuft
3. Überprüfe die API-URL in den Einstellungen
4. Kontaktiere das Entwicklungsteam
