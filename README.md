# 🏃‍♂️ MainStream

**MainStream** ist eine umfassende Laufplattform, die es Läufern ermöglicht, ihre Aktivitäten zu verfolgen, mit der Community zu interagieren und ihre Fortschritte durch Trophäen und Wettbewerbe zu messen.

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://openjdk.java.net/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.3-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Angular](https://img.shields.io/badge/Angular-19-red.svg)](https://angular.io/)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

## 📋 Inhaltsverzeichnis

- [Überblick](#-überblick)
- [Features](#-features)
- [Tech Stack](#-tech-stack)
- [Projektstruktur](#-projektstruktur)
- [Voraussetzungen](#-voraussetzungen)
- [Installation](#-installation)
- [Konfiguration](#-konfiguration)
- [Datenbank Setup](#-datenbank-setup)
- [Entwicklung](#-entwicklung)
- [API Dokumentation](#-api-dokumentation)
- [Integrationen](#-integrationen)
- [Testing](#-testing)
- [Deployment](#-deployment)
- [Mitwirken](#-mitwirken)
- [Support](#-support)
- [Lizenz](#-lizenz)

## 🎯 Überblick

MainStream ist eine Full-Stack-Webanwendung für Läufer, die ihre Trainingsaktivitäten tracken, analysieren und mit anderen teilen möchten. Die Plattform bietet umfassende Funktionen von der manuellen Eingabe über Dateiuploads bis hin zu automatischen Syncs mit populären Fitness-Plattformen.

### Highlights

- 🗺️ **GPS-basiertes Activity Tracking** mit interaktiven Karten (Leaflet)
- 📊 **Detaillierte Statistiken** und Performance-Analysen
- 🏆 **Trophäen-System** für Erfolge und Meilensteine
- 🤝 **Community Features** mit Routen und Wettbewerben
- 🔗 **Drittanbieter-Integrationen** (Strava, Garmin, Nike)
- 📁 **Datei-Import** (FIT, GPX Formate)
- 💳 **Premium-Abonnements** mit erweiterten Features
- 📧 **E-Mail-Benachrichtigungen** für wichtige Events

## ✨ Features

### Core Features

#### 🏃 Activity Management
- Manuelle Eingabe von Laufaktivitäten
- Upload von FIT- und GPX-Dateien
- Automatische Berechnung von Pace, Geschwindigkeit und Statistiken
- GPS-Track Visualisierung auf interaktiven Karten
- Höhenprofil-Analyse
- Wetterinformationen zu Läufen

#### 📈 Statistiken & Analytics
- Persönliches Dashboard mit Gesamtstatistiken
- Wöchentliche, monatliche und jährliche Übersichten
- Distanz-, Geschwindigkeits- und Höhenanalysen
- Performance-Trends und Fortschritte
- Kalorienverbrauch-Tracking

#### 🏆 Trophäen & Achievements
- Automatische Trophy-Erkennung basierend auf Leistungen
- Verschiedene Kategorien (Distanz, Geschwindigkeit, Häufigkeit)
- Achievement-System für besondere Meilensteine
- Fortschrittsanzeige für laufende Ziele

#### 🗺️ Routen & Community
- Vordefinierte Community-Routen
- Route-Matching für gelaufene Strecken
- Community-Map mit beliebten Laufstrecken
- Öffentliche und private Routen

#### 🏅 Wettbewerbe
- Teilnahme an zeitlich begrenzten Challenges
- Ranglisten und Leaderboards
- Team- und Einzelwettbewerbe
- Automatische Wettbewerbsauswertung

#### 🔗 Drittanbieter-Integrationen
- **Strava**: OAuth 2.0 Integration für Activity-Sync
- **Garmin Connect**: Automatischer Import von Garmin-Aktivitäten
- **Nike Run Club**: Datenimport (geplant/in Entwicklung)
- Automatische Token-Verwaltung und -Erneuerung

#### 💎 Premium-Funktionen
- Erweiterte Statistiken und Analysen
- Unbegrenzte Datei-Uploads
- Zugriff auf exklusive Wettbewerbe
- Werbefreie Erfahrung
- Prioritäts-Support

#### 👤 Benutzerverwaltung
- Sichere Registrierung und Authentifizierung (JWT)
- Profilmanagement mit Avataren
- Datenschutz-Einstellungen
- Account-Verwaltung und Löschung

## 🛠️ Tech Stack

### Backend

| Technologie | Version | Verwendung |
|-------------|---------|------------|
| **Java** | 21 | Programmiersprache |
| **Spring Boot** | 3.3.3 | Application Framework |
| **Spring Security** | 6.x | Authentifizierung & Autorisierung |
| **Spring Data JPA** | 3.x | Datenzugriff & ORM |
| **MariaDB** | 10.x+ | Datenbank |
| **Liquibase** | 4.x | Datenbank-Migrationen |
| **JWT** | 0.12.3 | Token-basierte Authentifizierung |
| **MapStruct** | 1.5.5 | DTO Mapping |
| **Lombok** | - | Code-Generierung |
| **Maven** | 3.x | Build-Tool & Dependency Management |

#### Spezielle Libraries

- **Garmin FIT SDK** (21.176.0) - Parsing von FIT-Dateien
- **JPX** (3.1.0) - GPX-Datei-Verarbeitung
- **Spring Boot Mail** - E-Mail-Versand
- **Spring Boot Actuator** - Monitoring & Health Checks

### Frontend

| Technologie | Version | Verwendung |
|-------------|---------|------------|
| **Angular** | 19 | Frontend-Framework |
| **Angular Material** | 19.2.19 | UI-Komponenten |
| **TypeScript** | 5.6.2 | Programmiersprache |
| **RxJS** | 7.8.0 | Reactive Programming |
| **Leaflet** | 1.9.4 | Kartenvisualisierung |
| **Transloco** | 8.1.0 | Internationalisierung (i18n) |

### DevOps & Tools

- **Git** - Versionskontrolle
- **Docker** (optional) - Containerisierung
- **npm** - Frontend Package Manager
- **Jasmine & Karma** - Testing

## 📁 Projektstruktur

```
mainstream/
├── mainstream-backend/          # Spring Boot Backend
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/mainstream/
│   │   │   │   ├── activity/       # Activity/Run Management
│   │   │   │   ├── competition/    # Wettbewerbe
│   │   │   │   ├── dashboard/      # Dashboard & Statistiken
│   │   │   │   ├── email/          # E-Mail Service
│   │   │   │   ├── fitfile/        # FIT/GPX File Processing
│   │   │   │   ├── garmin/         # Garmin Integration
│   │   │   │   ├── strava/         # Strava Integration
│   │   │   │   ├── nike/           # Nike Integration
│   │   │   │   ├── subscription/   # Premium-Abonnements
│   │   │   │   ├── user/           # Benutzerverwaltung
│   │   │   │   ├── security/       # Security & JWT
│   │   │   │   └── config/         # Konfiguration
│   │   │   └── resources/
│   │   │       ├── db/             # Liquibase Migrations & Seeds
│   │   │       ├── templates/      # E-Mail Templates
│   │   │       └── application.properties
│   │   └── test/
│   └── pom.xml
│
├── mainstream-frontend/         # Angular Frontend
│   ├── src/
│   │   ├── app/
│   │   │   ├── core/           # Core Module (Guards, Interceptors)
│   │   │   ├── shared/         # Shared Components & Services
│   │   │   ├── features/       # Feature Modules
│   │   │   │   ├── runs/
│   │   │   │   ├── routes/
│   │   │   │   ├── trophies/
│   │   │   │   ├── subscriptions/
│   │   │   │   └── users/
│   │   │   └── pages/          # Page Components
│   │   │       ├── home/
│   │   │       ├── landing-page/
│   │   │       ├── runs/
│   │   │       ├── routes/
│   │   │       ├── trophies/
│   │   │       ├── competitions/
│   │   │       ├── strava/
│   │   │       └── garmin/
│   │   └── assets/
│   └── package.json
│
├── seed-database.sh             # Datenbank-Seeding Script
├── DATABASE_SEEDING.md          # Seeding-Anleitung
├── STRAVA_INTEGRATION.md        # Strava Setup
├── GARMIN_INTEGRATION.md        # Garmin Setup
└── README.md                    # Diese Datei
```

## 📋 Voraussetzungen

### Software-Anforderungen

- **Java Development Kit (JDK)** 21 oder höher
- **Node.js** 18+ und npm
- **MariaDB** 10.x oder höher (oder MySQL 8.x)
- **Maven** 3.8+ (oder verwende den mitgelieferten Maven Wrapper)
- **Git** für Versionskontrolle

### Optional

- **Docker** & Docker Compose für containerisiertes Setup
- **Postman** oder ähnliches für API-Testing

## 🚀 Installation

### 1. Repository klonen

```bash
git clone https://github.com/Blindworks/mainstream.git
cd mainstream
```

### 2. Datenbank einrichten

```bash
# MariaDB/MySQL Server starten (oder Docker verwenden)
sudo systemctl start mariadb

# Datenbank und Benutzer erstellen
mysql -u root -p
```

```sql
CREATE DATABASE mainstream CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'mainstream'@'localhost' IDENTIFIED BY 'IhrSicheresPasswort';
GRANT ALL PRIVILEGES ON mainstream.* TO 'mainstream'@'localhost';
FLUSH PRIVILEGES;
EXIT;
```

### 3. Backend konfigurieren

```bash
cd mainstream-backend
```

Erstelle oder bearbeite `src/main/resources/application.properties`:

```properties
# Datenbank-Konfiguration
spring.datasource.url=jdbc:mariadb://localhost:3306/mainstream
spring.datasource.username=mainstream
spring.datasource.password=IhrSicheresPasswort
spring.jpa.hibernate.ddl-auto=update

# JWT-Konfiguration
jwt.secret=IhrSuperSicheresJWTSecretHierEinfügen
jwt.expiration=86400000

# E-Mail-Konfiguration (optional)
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=ihre-email@gmail.com
spring.mail.password=ihr-app-passwort
```

### 4. Backend starten

```bash
# Mit Maven Wrapper (empfohlen)
./mvnw clean install
./mvnw spring-boot:run

# Oder mit installiertem Maven
mvn clean install
mvn spring-boot:run
```

Backend läuft jetzt auf `http://localhost:8080`

### 5. Frontend konfigurieren und starten

```bash
cd ../mainstream-frontend

# Dependencies installieren
npm install

# Development Server starten
npm start
# oder
ng serve
```

Frontend läuft jetzt auf `http://localhost:4200`

## ⚙️ Konfiguration

### Umgebungsvariablen

Für sensible Daten empfiehlt sich die Verwendung von Umgebungsvariablen:

```bash
# Backend
export DB_PASSWORD=IhrSicheresPasswort
export JWT_SECRET=IhrSuperSicheresJWTSecret
export STRAVA_CLIENT_ID=IhreStravaClientID
export STRAVA_CLIENT_SECRET=IhreStravaClientSecret
export GARMIN_CLIENT_ID=IhreGarminClientID
export GARMIN_CLIENT_SECRET=IhreGarminClientSecret

# Anwendung starten
./mvnw spring-boot:run
```

### application.properties Übersicht

```properties
# Server
server.port=8080

# Datenbank
spring.datasource.url=jdbc:mariadb://localhost:3306/mainstream
spring.datasource.username=mainstream
spring.datasource.password=${DB_PASSWORD}
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false

# Liquibase
spring.liquibase.enabled=true
spring.liquibase.change-log=classpath:db/changelog/db.changelog-master.xml

# JWT
jwt.secret=${JWT_SECRET}
jwt.expiration=86400000

# File Upload
spring.servlet.multipart.max-file-size=50MB
spring.servlet.multipart.max-request-size=50MB
mainstream.upload.directory=./uploads

# Strava Integration
mainstream.strava.client-id=${STRAVA_CLIENT_ID}
mainstream.strava.client-secret=${STRAVA_CLIENT_SECRET}
mainstream.strava.redirect-uri=http://localhost:4200/strava/callback

# Garmin Integration
mainstream.garmin.client-id=${GARMIN_CLIENT_ID}
mainstream.garmin.client-secret=${GARMIN_CLIENT_SECRET}
mainstream.garmin.redirect-uri=http://localhost:4200/garmin/callback

# E-Mail
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=${MAIL_USERNAME}
spring.mail.password=${MAIL_PASSWORD}
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true

# Actuator
management.endpoints.web.exposure.include=health,info
management.endpoint.health.show-details=when-authorized
```

## 🗄️ Datenbank Setup

### Automatische Migrations (Liquibase)

Liquibase führt automatisch beim Anwendungsstart alle Datenbank-Migrationen aus. Migrations befinden sich in:

```
mainstream-backend/src/main/resources/db/changelog/
```

### Test-Daten importieren

Für Entwicklungszwecke können Sie die Datenbank mit Testdaten füllen:

```bash
# Einfaches Bash-Script (empfohlen)
./seed-database.sh

# Oder manuell mit MySQL
mysql -u mainstream -p mainstream < mainstream-backend/src/main/resources/db/seed-testdata.sql
```

**Testbenutzer nach Seeding:**
- Admin: `admin@mainstream.app` / `password123`
- User: `test.mueller@mainstream.app` / `password123`

Weitere Details siehe [DATABASE_SEEDING.md](DATABASE_SEEDING.md)

## 💻 Entwicklung

### Backend-Entwicklung

```bash
cd mainstream-backend

# Tests ausführen
./mvnw test

# Mit Auto-Reload (Spring Boot DevTools)
./mvnw spring-boot:run

# Build für Produktion
./mvnw clean package
```

### Frontend-Entwicklung

```bash
cd mainstream-frontend

# Development Server mit Auto-Reload
ng serve

# Tests ausführen
ng test

# E2E Tests
ng e2e

# Production Build
ng build --configuration production
```

### Code-Qualität

**Backend:**
- Folge Spring Boot Best Practices
- Verwende Lombok für Boilerplate-Reduktion
- MapStruct für DTO-Mappings
- Schreibe Unit- und Integration-Tests

**Frontend:**
- Folge Angular Style Guide
- Verwende Angular Material Komponenten
- Reactive Programming mit RxJS
- TypeScript Strict Mode aktiviert

## 📚 API Dokumentation

### Authentifizierung

Alle geschützten Endpunkte erfordern einen JWT-Token im Header:

```
Authorization: Bearer <jwt-token>
```

Alternativ wird für Legacy-Zwecke auch `X-User-Id` unterstützt.

### Wichtige Endpunkte

#### Authentifizierung

```
POST   /api/users/register          - Registrierung
POST   /api/users/login             - Login
POST   /api/users/logout            - Logout
GET    /api/users/me                - Aktueller Benutzer
```

#### Aktivitäten/Runs

```
GET    /api/runs                    - Alle Runs abrufen
GET    /api/runs/{id}               - Einzelnen Run abrufen
POST   /api/runs                    - Run erstellen
PUT    /api/runs/{id}               - Run aktualisieren
DELETE /api/runs/{id}               - Run löschen
GET    /api/runs/{id}/gps           - GPS-Daten abrufen
```

#### Datei-Upload

```
POST   /api/fit/upload              - FIT-Datei hochladen
POST   /api/gpx/upload              - GPX-Datei hochladen
```

#### Strava Integration

```
GET    /api/strava/auth-url         - OAuth URL abrufen
POST   /api/strava/connect          - Konto verbinden
DELETE /api/strava/disconnect       - Konto trennen
POST   /api/strava/sync             - Aktivitäten synchronisieren
GET    /api/strava/status           - Verbindungsstatus
```

#### Garmin Integration

```
GET    /api/garmin/auth-url         - OAuth URL abrufen
POST   /api/garmin/connect          - Konto verbinden
DELETE /api/garmin/disconnect       - Konto trennen
POST   /api/garmin/sync             - Aktivitäten synchronisieren
GET    /api/garmin/status           - Verbindungsstatus
```

#### Wettbewerbe

```
GET    /api/competitions            - Alle Wettbewerbe
GET    /api/competitions/{id}       - Wettbewerb Details
POST   /api/competitions/{id}/join  - An Wettbewerb teilnehmen
GET    /api/competitions/{id}/leaderboard - Rangliste
```

#### Dashboard

```
GET    /api/dashboard/stats         - Persönliche Statistiken
GET    /api/dashboard/recent        - Neueste Aktivitäten
GET    /api/dashboard/achievements  - Errungenschaften
```

Vollständige API-Dokumentation (Swagger/OpenAPI) verfügbar unter:
```
http://localhost:8080/swagger-ui.html (geplant)
```

## 🔗 Integrationen

### Strava

Für die Strava-Integration benötigen Sie:

1. Strava-Developer-Account
2. Registrierte Strava-Anwendung
3. Client ID und Client Secret

**Setup-Anleitung:** [STRAVA_INTEGRATION.md](STRAVA_INTEGRATION.md)

### Garmin Connect

Für die Garmin-Integration benötigen Sie:

1. Garmin Developer Program Zugang
2. Genehmigte Garmin Connect API Application
3. Client ID und Client Secret

**Setup-Anleitung:** [GARMIN_INTEGRATION.md](GARMIN_INTEGRATION.md)

### Nike Run Club

Nike-Integration ist in Planung/Entwicklung.

## 🧪 Testing

### Backend Tests

```bash
cd mainstream-backend

# Alle Tests ausführen
./mvnw test

# Spezifische Test-Klasse
./mvnw test -Dtest=UserServiceTest

# Mit Coverage-Report
./mvnw test jacoco:report
```

### Frontend Tests

```bash
cd mainstream-frontend

# Unit Tests
ng test

# Tests mit Coverage
ng test --code-coverage

# E2E Tests
ng e2e
```

## 🚢 Deployment

### Backend Deployment

#### JAR-Datei erstellen

```bash
cd mainstream-backend
./mvnw clean package -DskipTests

# JAR-Datei befindet sich in:
# target/mainstream-backend-1.0.0.jar
```

#### Anwendung starten

```bash
java -jar target/mainstream-backend-1.0.0.jar \
  --spring.datasource.url=jdbc:mariadb://prod-db:3306/mainstream \
  --spring.datasource.password=${DB_PASSWORD} \
  --jwt.secret=${JWT_SECRET}
```

### Frontend Deployment

```bash
cd mainstream-frontend

# Production Build
ng build --configuration production

# Build-Artefakte befinden sich in:
# dist/mainstream-frontend/
```

Deploye die `dist/` Inhalte auf:
- **Nginx**
- **Apache**
- **Firebase Hosting**
- **AWS S3 + CloudFront**
- **Vercel** / **Netlify**

### Docker Deployment (Optional)

**Backend Dockerfile:**

```dockerfile
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

**Frontend Dockerfile:**

```dockerfile
FROM node:18 AS build
WORKDIR /app
COPY package*.json ./
RUN npm install
COPY . .
RUN npm run build --prod

FROM nginx:alpine
COPY --from=build /app/dist/mainstream-frontend /usr/share/nginx/html
EXPOSE 80
```

**docker-compose.yml:**

```yaml
version: '3.8'
services:
  db:
    image: mariadb:10
    environment:
      MYSQL_DATABASE: mainstream
      MYSQL_USER: mainstream
      MYSQL_PASSWORD: ${DB_PASSWORD}
      MYSQL_ROOT_PASSWORD: ${DB_ROOT_PASSWORD}
    volumes:
      - db-data:/var/lib/mysql
    ports:
      - "3306:3306"

  backend:
    build: ./mainstream-backend
    environment:
      SPRING_DATASOURCE_URL: jdbc:mariadb://db:3306/mainstream
      SPRING_DATASOURCE_PASSWORD: ${DB_PASSWORD}
      JWT_SECRET: ${JWT_SECRET}
    ports:
      - "8080:8080"
    depends_on:
      - db

  frontend:
    build: ./mainstream-frontend
    ports:
      - "80:80"
    depends_on:
      - backend

volumes:
  db-data:
```

## 🤝 Mitwirken

Beiträge sind willkommen! Bitte folge diesen Schritten:

1. **Fork** das Repository
2. **Erstelle** einen Feature-Branch (`git checkout -b feature/AmazingFeature`)
3. **Committe** deine Änderungen (`git commit -m 'Add some AmazingFeature'`)
4. **Push** zum Branch (`git push origin feature/AmazingFeature`)
5. **Öffne** einen Pull Request

### Entwicklungsrichtlinien

- Folge den bestehenden Code-Konventionen
- Schreibe Tests für neue Features
- Aktualisiere die Dokumentation
- Halte Commits atomar und aussagekräftig

## 📞 Support

Bei Fragen, Problemen oder Anregungen:

- **Issues:** [GitHub Issues](https://github.com/Blindworks/mainstream/issues)
- **Dokumentation:** Siehe die jeweiligen `.md` Dateien im Repository
- **E-Mail:** support@mainstream.app (falls verfügbar)

## 📄 Lizenz

Dieses Projekt ist unter der [MIT License](LICENSE) lizenziert.

---

## 🗺️ Roadmap

### Geplante Features

- [ ] Mobile Apps (iOS & Android)
- [ ] Soziale Features (Freunde, Activity Feed)
- [ ] Training Plans & Coaching
- [ ] Erweiterte Analytics mit ML
- [ ] Webhook-Support für Echtzeit-Syncs
- [ ] Nike Run Club vollständige Integration
- [ ] Apple Health & Google Fit Integration
- [ ] Intervall-Training Tracking
- [ ] Herzfrequenz-Zonen Analyse
- [ ] Virtual Races & Events

### Version History

- **v1.0.0** - Initial Release
  - Core Run Tracking
  - Strava & Garmin Integration
  - Trophy System
  - Premium Subscriptions

---

**Entwickelt mit ❤️ für die Lauf-Community**
