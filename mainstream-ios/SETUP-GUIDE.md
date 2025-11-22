# Mainstream iOS App - Setup Guide

Diese Anleitung führt dich Schritt für Schritt durch die Einrichtung der Mainstream iOS App in Xcode.

## Schritt 1: Xcode-Projekt erstellen

1. Öffne **Xcode**
2. Wähle **"Create a new Xcode project"**
3. Wähle **iOS** → **App**
4. Klicke auf **"Next"**

## Schritt 2: Projekt konfigurieren

Gib folgende Informationen ein:

- **Product Name:** `Mainstream`
- **Team:** Wähle dein Team aus (oder lasse es leer für lokale Entwicklung)
- **Organization Identifier:** `com.mainstream` (oder deine eigene)
- **Bundle Identifier:** Wird automatisch generiert (`com.mainstream.Mainstream`)
- **Interface:** `SwiftUI`
- **Language:** `Swift`
- **Storage:** `None` (keine Core Data erforderlich)
- **Include Tests:** Optional (kannst du aktivieren)

Klicke auf **"Next"** und wähle einen Speicherort für dein Projekt.

## Schritt 3: Projekt-Struktur einrichten

1. Lösche die automatisch erstellte Datei **`ContentView.swift`** (Rechtsklick → Delete → Move to Trash)

2. Erstelle die Ordnerstruktur in Xcode:
   - Rechtsklick auf `Mainstream` (das blaue Projekt-Icon)
   - Wähle **"New Group"**
   - Erstelle folgende Gruppen:
     - `Models`
     - `Services`
     - `Views`

## Schritt 4: Dateien hinzufügen

### Models hinzufügen:

1. Rechtsklick auf den `Models` Ordner → **"New File"**
2. Wähle **"Swift File"**
3. Erstelle folgende Dateien und kopiere den Inhalt aus den entsprechenden Dateien:
   - `User.swift`
   - `Trophy.swift`
   - `Route.swift`
   - `Run.swift`

### Services hinzufügen:

1. Rechtsklick auf den `Services` Ordner → **"New File"**
2. Erstelle folgende Dateien:
   - `APIService.swift`
   - `AuthService.swift`
   - `TrophyService.swift`
   - `RouteService.swift`
   - `RunService.swift`

### Views hinzufügen:

1. Rechtsklick auf den `Views` Ordner → **"New File"**
2. Erstelle folgende Dateien:
   - `LoginView.swift`
   - `MainTabView.swift`
   - `TrophiesView.swift`
   - `RoutesView.swift`
   - `UserRunsView.swift`
   - `ProfileView.swift`

### App-Datei ersetzen:

1. Ersetze den Inhalt von `MainstreamApp.swift` (im Hauptverzeichnis) mit dem bereitgestellten Code

## Schritt 5: Info.plist konfigurieren

1. Klicke auf das Projekt (blaues Icon ganz oben)
2. Wähle das **Target** "Mainstream"
3. Gehe zum Tab **"Info"**
4. Klicke auf das **"+"** Icon
5. Füge folgenden Key hinzu:
   - **Key:** `App Transport Security Settings` (Type: Dictionary)
   - Klicke auf das Dreieck, um es zu erweitern
   - Klicke auf das **"+"** neben dem Eintrag
   - **Key:** `Allow Arbitrary Loads` (Type: Boolean)
   - **Value:** `YES`

**Hinweis:** Dies erlaubt HTTP-Verbindungen für die Entwicklung. Für Production sollte HTTPS verwendet werden!

## Schritt 6: Backend-URL konfigurieren

1. Öffne `Services/APIService.swift`
2. Finde die Zeile:
   ```swift
   var baseURL: String = "http://localhost:8080"
   ```
3. Ändere die URL zu deiner Backend-URL:
   - Für lokales Backend auf deinem Mac: `http://localhost:8080`
   - Für Backend auf anderem Rechner: `http://IP-ADRESSE:8080`
   - Beispiel: `http://192.168.1.100:8080`

**Wichtig:** Wenn du auf einem echten iPhone testest, verwende die IP-Adresse deines Macs, nicht `localhost`!

### Backend-URL finden (für echtes iPhone):

1. Öffne **Systemeinstellungen** auf deinem Mac
2. Gehe zu **Netzwerk**
3. Wähle deine aktive Verbindung (WLAN oder Ethernet)
4. Notiere die **IP-Adresse** (z.B. `192.168.1.100`)
5. Verwende diese IP in der Backend-URL: `http://192.168.1.100:8080`

## Schritt 7: Deployment Target einstellen

1. Klicke auf das Projekt (blaues Icon)
2. Wähle das Target "Mainstream"
3. Gehe zum Tab **"General"**
4. Unter **"Minimum Deployments"** setze:
   - **iOS:** `17.0` (oder höher, je nach Xcode-Version)

## Schritt 8: Build und Run

1. Wähle ein Zielgerät aus:
   - **Simulator:** Wähle z.B. "iPhone 15 Pro"
   - **Echtes iPhone:** Verbinde dein iPhone und wähle es aus

2. Klicke auf den **Play-Button** (▶️) oder drücke **Cmd + R**

3. Die App sollte jetzt bauen und starten!

## Troubleshooting

### Problem: "No such module 'Combine'"
- **Lösung:** Stelle sicher, dass das Deployment Target mindestens iOS 13.0 ist

### Problem: "Failed to connect to backend"
- **Lösung 1:** Überprüfe, ob das Backend läuft
- **Lösung 2:** Überprüfe die Backend-URL in `APIService.swift`
- **Lösung 3:** Stelle sicher, dass dein iPhone/Simulator und Backend im selben Netzwerk sind
- **Lösung 4:** Verwende die IP-Adresse statt `localhost` für echte Geräte

### Problem: "App Transport Security blocked"
- **Lösung:** Überprüfe, ob die Info.plist korrekt konfiguriert ist (siehe Schritt 5)

### Problem: Build-Fehler "Cannot find type 'User' in scope"
- **Lösung:** Stelle sicher, dass alle Dateien zum Target hinzugefügt sind
  1. Wähle die Datei aus
  2. Öffne den **File Inspector** (rechte Sidebar)
  3. Stelle sicher, dass unter **"Target Membership"** ein Häkchen bei "Mainstream" ist

### Problem: "Signing for 'Mainstream' requires a development team"
- **Lösung:**
  1. Gehe zu Projekt → Target → "Signing & Capabilities"
  2. Aktiviere "Automatically manage signing"
  3. Wähle dein Team aus (oder erstelle eine kostenlose Apple Developer ID)

## Testen der App

### Login-Test:

1. Starte das Backend
2. Erstelle einen Test-User (oder verwende einen existierenden)
3. Starte die App
4. Gib E-Mail und Passwort ein
5. Klicke auf "Anmelden"

### Test-Credentials (falls Datenbank geseedet wurde):
- **E-Mail:** `admin@mainstream.de`
- **Passwort:** `admin123`

## Backend-URL zur Laufzeit ändern

Du kannst die Backend-URL auch in der App ändern:

1. Melde dich an
2. Gehe zum Tab **"Profil"**
3. Wähle **"App-Einstellungen"**
4. Ändere die **"Backend URL"**
5. Klicke auf **"Speichern"**
6. Starte die App neu

## Nächste Schritte

Jetzt kannst du:
- Die Trophäen-Ansicht erkunden
- Vordefinierte Routen ansehen
- Deine Läufe durchsehen
- Dein Profil anpassen

Viel Erfolg mit der Mainstream iOS App! 🏃‍♂️🎉
