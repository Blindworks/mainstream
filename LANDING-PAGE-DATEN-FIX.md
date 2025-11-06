# 🎉 Landing Page Community Map Fix - Daten anzeigen

## Was wurde gefixt?

**Problem:** Die Landing Page zeigte keine anderen Runs, weil:
1. ❌ Seed-Daten hatten keine `user_activities` Records erstellt
2. ❌ Frontend nutzte nur Mock-Daten statt echte Backend-API
3. ❌ Kein Backend-Endpoint für Community-Daten

**Lösung:**
1. ✅ Seed-Script erstellt jetzt automatisch `user_activities` Records
2. ✅ Neuer Backend-Endpoint: `GET /api/activities/community`
3. ✅ Frontend verbindet sich mit echtem Backend-API
4. ✅ Alle Änderungen committed und gepusht!

---

## 🚀 So bekommst du die Daten angezeigt

### Schritt 1: Datenbank mit neuen Seed-Daten befüllen

Öffne **MySQL Workbench** und führe das aktualisierte Seed-Script aus:

```sql
-- Inhalt von EINFACH-AUSFUEHREN.sql komplett in MySQL Workbench kopieren und ausführen
```

**Was passiert:**
- ✅ Löscht alte Testdaten
- ✅ Erstellt 16 Test-User
- ✅ Erstellt 4 Routen (10km Main, 5km Ostpark, 7km Nordmainufer, 12km Stadtwald)
- ✅ Erstellt 18 Runs mit route_id
- ✅ **NEU:** Erstellt automatisch `user_activities` Records für jeden Run!
  - Completion Percentage: 95-100%
  - Direction: forward/reverse (70% forward, 30% reverse)
  - Matched Route ID verknüpft mit predefined_routes

**Erwartete Ausgabe:**
```
Test-User: 16
Routen: 4
Runs: 18
User Activities (Completions): 16
```

---

### Schritt 2: Backend neu starten

Das Backend muss neu gestartet werden, damit die neuen API-Endpoints geladen werden.

```bash
cd mainstream-backend
./mvnw spring-boot:run
```

**Warte bis du siehst:**
```
Started MainstreamApplication in X.XXX seconds
```

---

### Schritt 3: Frontend starten (falls noch nicht läuft)

```bash
cd mainstream-frontend
npm start
```

**Warte auf:**
```
✔ Browser application bundle generation complete.
Application is running at http://localhost:4200
```

---

### Schritt 4: Landing Page testen

1. **Browser öffnen:** http://localhost:4200
2. **Einloggen** mit einem Test-Account:
   - Email: `test.mueller@mainstream.app`
   - Passwort: `password123`
3. **Landing Page aufrufen:** http://localhost:4200/landing

---

## 🎨 Was du jetzt sehen solltest

### ✅ Community Map (rechte Seite, 65%)

Die Community Map sollte jetzt zeigen:

1. **4 Routen mit SVG-Pfaden:**
   - 🌊 10km Vollrunde Main
   - 🌳 5km Ostpark Loop
   - ⬆️ 7km Nordmainufer
   - 🏔️ 12km Stadtwald Trail

2. **User-Avatare auf den Routen:**
   - Echte Avatare von Test-Usern
   - Positioniert auf den Routen die sie gelaufen sind
   - Max. 10 Avatare pro Route (bei mehr gibt es "+X")

3. **Direction-Indicator:**
   - Grüne Pfeile zeigen Laufrichtung (→ forward, ← reverse)

4. **Statistiken beim Hover:**
   - Anzahl Completions
   - Average Completion %
   - Unique Users

### ✅ Personal Stats (linke Seite, 35%)

Je nach Test-User solltest du sehen:
- **Today's Runs:** Deine heutigen Läufe (wenn vorhanden)
- **This Week:** Wochenstatistik
- **Recent Achievements:** Letzte Erfolge

---

## 🔍 Überprüfen ob es funktioniert

### Browser Developer Console öffnen (F12)

**Wenn alles funktioniert:**
```
✅ Keine roten Fehler
✅ Network Tab zeigt erfolgreiche Requests:
   - GET http://localhost:8080/api/routes?activeOnly=true (Status 200)
   - GET http://localhost:8080/api/activities/community (Status 200)
```

**Falls Fehler auftreten:**

#### Fehler: "Failed to load resource: net::ERR_CONNECTION_REFUSED"
- ❌ Backend läuft nicht
- ✅ Backend neu starten (siehe Schritt 2)

#### Fehler: "404 Not Found" auf /api/activities/community
- ❌ Backend läuft mit altem Code
- ✅ Git pull machen und Backend neu starten:
  ```bash
  git pull origin claude/landing-page-community-map-011CUs5eCqn7geUFXziHDRHL
  cd mainstream-backend
  ./mvnw clean spring-boot:run
  ```

#### Keine Daten sichtbar, aber keine Fehler
- ❌ Seed-Daten nicht korrekt importiert
- ✅ SQL nochmal ausführen (Schritt 1)
- ✅ In MySQL prüfen:
  ```sql
  SELECT COUNT(*) FROM user_activities
  WHERE user_id IN (SELECT id FROM users WHERE email LIKE 'test%@mainstream.app');
  -- Sollte mindestens 16 sein!
  ```

---

## 🧪 Daten in Datenbank prüfen

```sql
-- Alle User Activities mit Route-Zuordnung
SELECT
    ua.id,
    u.first_name,
    u.last_name,
    pr.name as route_name,
    ua.direction,
    ua.route_completion_percentage,
    ua.activity_start_time
FROM user_activities ua
JOIN users u ON ua.user_id = u.id
JOIN predefined_routes pr ON ua.matched_route_id = pr.id
WHERE u.email LIKE 'test%@mainstream.app'
ORDER BY ua.activity_start_time DESC;
```

**Erwartetes Ergebnis:**
- Mindestens 16 Records
- Mit verschiedenen Usern
- Alle 4 Routen sollten vorkommen
- Completion Percentage zwischen 95-100%
- Direction entweder 'forward' oder 'reverse'

---

## 🛠️ Technische Details der Änderungen

### Backend-Änderungen:

1. **Neuer Endpoint in `UserActivityController`:**
   ```java
   @GetMapping("/community")
   public ResponseEntity<List<UserActivityDto>> getCommunityActivities()
   ```

2. **Neue Service-Methode:**
   ```java
   public List<UserActivity> getAllActivitiesWithRoutes()
   ```

3. **UserActivityDto erweitert:**
   - `userFirstName`
   - `userLastName`
   - `userAvatarUrl`

### Frontend-Änderungen:

1. **CommunityMapService umgestellt:**
   - `USE_MOCK_DATA = false`
   - Nutzt jetzt `HttpClient` für API-Calls
   - Lädt von `/api/routes` und `/api/activities/community`

2. **Automatische DTO-zu-Model Konvertierung:**
   - `PredefinedRouteDto` → `Route`
   - `UserActivityDto` → `RouteCompletion` + `CompletionUser`

### Seed-Daten erweitert:

```sql
-- Neue INSERT Statements für user_activities
INSERT INTO user_activities (...)
SELECT r.user_id, r.id, NULL, r.route_id, ...
FROM runs r WHERE r.route_id IS NOT NULL;
```

---

## 🎯 Wenn alles funktioniert...

Du solltest jetzt eine funktionierende Community Map sehen mit:
- ✅ Echten Routen aus der Datenbank
- ✅ Echten User-Avataren aus der Datenbank
- ✅ Echten Completion-Daten
- ✅ Statistiken die dynamisch berechnet werden
- ✅ Keine Mock-Daten mehr!

---

## 📝 Nächste Schritte (Optional)

1. **Echte GPS-Pfade generieren:**
   - Aktuell nutzen die Routen Mock-SVG-Pfade
   - TODO: GPS-Trackpoints von predefined_routes in SVG-Pfade konvertieren

2. **Route-Matching verbessern:**
   - Completion-Percentage dynamisch berechnen
   - Matching-Accuracy aus echten GPS-Daten

3. **Personal Stats mit echten Daten füllen:**
   - PersonalStatsComponent ebenfalls auf echte API umstellen
   - `/api/runs/stats` Endpoint nutzen

---

## 🆘 Support

Bei Problemen:
1. Browser Console checken (F12)
2. Backend Logs prüfen
3. Datenbank-Queries ausführen (siehe oben)
4. Issue auf GitHub erstellen mit Fehlerdetails

---

**Viel Erfolg! Die Landing Page sollte jetzt mit echten Community-Daten funktionieren! 🚀**
