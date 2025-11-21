# 🏃‍♂️ MainStream

**MainStream** is an inclusive running platform designed for everyday runners — not elite athletes.  
Instead of promoting higher–faster–further thinking or toxic ranking systems, MainStream focuses on consistency, achievable challenges, exploration, and sustainable motivation.

The core idea: **Running should feel accessible, enjoyable, and pressure-free — something that fits into your life, not something that dominates it.**

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://openjdk.java.net/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.3-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Angular](https://img.shields.io/badge/Angular-20-red.svg)](https://angular.io/)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

---

## 📋 Table of Contents

- [Overview](#overview)
- [Features](#features)
- [Tech Stack](#tech-stack)
- [Project Structure](#project-structure)
- [Requirements](#requirements)
- [Installation](#installation)
- [Configuration](#configuration)
- [Database Setup](#database-setup)
- [Development](#development)
- [API Documentation](#api-documentation)
- [Integrations](#integrations)
- [Testing](#testing)
- [Deployment](#deployment)
- [Contributing](#contributing)
- [Support](#support)
- [License](#license)

---

## 🎯 Overview

MainStream is a full-stack running application built for people who want to integrate running into their everyday lives — **without judgment, pressure, or comparison to others**.

Unlike traditional fitness apps that revolve around leaderboards, pace rankings, and elite performance metrics, MainStream embraces:

- **Consistency over competition**  
- **Exploration over speed**  
- **Healthy habits over performance pressure**  
- **Motivation without toxicity**

Instead of fighting for the top of a leaderboard, you unlock waypoints, complete time-based missions, maintain weekly streaks, and celebrate simply showing up.

### Core Philosophy

- No global leaderboards  
- No toxic rankings  
- No pace shaming  
- No elitist competition loops  
- Fully optional social features  
- Achievements that reward presence, not performance  

MainStream speaks to the *mainstream* — beginners, returners, casual joggers, lunch-break runners, after-work movers, and anyone who wants running to feel approachable.

---

## 🌟 Highlights

- 🗺️ **GPS-based activity tracking** with interactive maps (Leaflet)
- 🎯 **Consistency-focused challenges** (weekly streaks, time-of-day runs, waypoint missions)
- 🏅 **Achievement system that rewards habit-building**, not personal records
- 👥 **Optional and non-toxic community features** — no public pace comparisons
- 📁 **FIT & GPX file imports** for users bringing their existing running history
- 🔗 **Strava & Garmin optional integrations** (sync if you want, ignore if you don't)
- 🔔 **Positive, pressure-free motivation tools** (streak indicators, reminders, goal progress)

---

## ✨ Features

### 🏃 Activity Management
- Manual run creation
- FIT and GPX file uploads
- Basic metrics calculation (distance, duration, elevation)
- GPS path visualization on the map
- Weather and elevation profiles

> Clear insights without competitive angles.

---

### 📈 Statistics & Insights
- Personal dashboard with weekly/monthly/yearly views
- **Consistency analytics** (e.g., days active per week)
- Streak tracking (habit-based)
- Route history and waypoint progress
- Optional calorie estimates

> Analytics that help you understand your habits — not judge them.

---

### 🏆 Achievements & Trophies (Non-Toxic Gamification)
Achievements reward:
- Weekly or monthly consistency
- Returning after breaks
- Exploring new routes and waypoints
- Running at specific times (sunrise run, evening routine)
- Completing low-pressure missions

> A system built around encouragement, not comparison.

---

### 🗺️ Routes, Waypoints & Exploration
- Community-curated routes  
- Waypoint missions: unlock specific map locations by running there  
- Private & public routes  
- “Discovery”-focused running experience  

> Running becomes exploration, not performance.

---

### 🟦 Challenges (Redefined)
MainStream challenges do **not** use leaderboards or competition metrics.  
Instead, they focus on:

- Run X times per week  
- Hit a certain number of waypoints  
- Build a multi-week habit streak  
- Run at defined times of day  
- Seasonal or theme-based challenge sets  

> Challenges that help runners stay active — not stressed.

---

### 🔗 Integrations (Optional)
- **Strava** (OAuth & activity sync)
- **Garmin Connect**
- **Nike Run Club** (planned)
- Automatic token lifecycle management

> Integrations enhance the app — they don't define it.

---

### 💎 Premium Features
Premium is for convenience and support, never performance advantage.

Includes:
- Extended consistency charts
- Unlimited file uploads
- Additional themed challenges
- Ad-free experience
- Supporter badge

---

### 👤 User Management
- Secure JWT authentication
- Profile settings & privacy controls
- Account deletion & data export

---

## 🛠️ Tech Stack

### Backend

| Technology | Version | Purpose |
|------------|---------|---------|
| **Java** | 21 | Programming Language |
| **Spring Boot** | 3.3.3 | Application Framework |
| **Spring Security** | 6.x | Authentication & Authorization |
| **Spring Data JPA** | 3.x | Data Access & ORM |
| **MariaDB** | 10.x+ | Database |
| **Liquibase** | 4.x | Database Migrations |
| **JWT** | 0.12.3 | Token-based Authentication |
| **MapStruct** | 1.5.5 | DTO Mapping |
| **Lombok** | - | Code Generation |
| **Maven** | 3.x | Build Tool & Dependency Management |

#### Specialized Libraries

- **Garmin FIT SDK** (21.176.0) - FIT file parsing
- **JPX** (3.1.0) - GPX file processing
- **Spring Boot Mail** - Email delivery
- **Spring Boot Actuator** - Monitoring & Health Checks

### Frontend

| Technology | Version | Purpose |
|------------|---------|---------|
| **Angular** | 19 | Frontend Framework |
| **Angular Material** | 19.2.19 | UI Components |
| **TypeScript** | 5.6.2 | Programming Language |
| **RxJS** | 7.8.0 | Reactive Programming |
| **Leaflet** | 1.9.4 | Map Visualization |
| **Transloco** | 8.1.0 | Internationalization (i18n) |

### DevOps & Tools

- **Git** - Version Control
- **Docker** (optional) - Containerization
- **npm** - Frontend Package Manager
- **Jasmine & Karma** - Testing

## 📁 Project Structure

```
mainstream/
├── mainstream-backend/          # Spring Boot Backend
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/mainstream/
│   │   │   │   ├── activity/       # Activity/Run Management
│   │   │   │   ├── competition/    # Competitions
│   │   │   │   ├── dashboard/      # Dashboard & Statistics
│   │   │   │   ├── email/          # Email Service
│   │   │   │   ├── fitfile/        # FIT/GPX File Processing
│   │   │   │   ├── garmin/         # Garmin Integration
│   │   │   │   ├── strava/         # Strava Integration
│   │   │   │   ├── nike/           # Nike Integration
│   │   │   │   ├── subscription/   # Premium Subscriptions
│   │   │   │   ├── user/           # User Management
│   │   │   │   ├── security/       # Security & JWT
│   │   │   │   └── config/         # Configuration
│   │   │   └── resources/
│   │   │       ├── db/             # Liquibase Migrations & Seeds
│   │   │       ├── templates/      # Email Templates
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
├── seed-database.sh             # Database Seeding Script
├── DATABASE_SEEDING.md          # Seeding Guide
├── STRAVA_INTEGRATION.md        # Strava Setup
├── GARMIN_INTEGRATION.md        # Garmin Setup
└── README.md                    # This File
```

## 📋 Prerequisites

### Software Requirements

- **Java Development Kit (JDK)** 21 or higher
- **Node.js** 18+ and npm
- **MariaDB** 10.x or higher (or MySQL 8.x)
- **Maven** 3.8+ (or use the included Maven Wrapper)
- **Git** for version control

### Optional

- **Docker** & Docker Compose for containerized setup
- **Postman** or similar for API testing

## 🚀 Installation

### 1. Clone Repository

```bash
git clone https://github.com/Blindworks/mainstream.git
cd mainstream
```

### 2. Setup Database

```bash
# Start MariaDB/MySQL Server (or use Docker)
sudo systemctl start mariadb

# Create database and user
mysql -u root -p
```

```sql
CREATE DATABASE mainstream CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'mainstream'@'localhost' IDENTIFIED BY 'YourSecurePassword';
GRANT ALL PRIVILEGES ON mainstream.* TO 'mainstream'@'localhost';
FLUSH PRIVILEGES;
EXIT;
```

### 3. Configure Backend

```bash
cd mainstream-backend
```

Create or edit `src/main/resources/application.properties`:

```properties
# Database Configuration
spring.datasource.url=jdbc:mariadb://localhost:3306/mainstream
spring.datasource.username=mainstream
spring.datasource.password=YourSecurePassword
spring.jpa.hibernate.ddl-auto=update

# JWT Configuration
jwt.secret=YourSuperSecureJWTSecretHere
jwt.expiration=86400000

# Email Configuration (optional)
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=your-email@gmail.com
spring.mail.password=your-app-password
```

### 4. Start Backend

```bash
# With Maven Wrapper (recommended)
./mvnw clean install
./mvnw spring-boot:run

# Or with installed Maven
mvn clean install
mvn spring-boot:run
```

Backend now runs on `http://localhost:8080`

### 5. Configure and Start Frontend

```bash
cd ../mainstream-frontend

# Install dependencies
npm install

# Start development server
npm start
# or
ng serve
```

Frontend now runs on `http://localhost:4200`

## ⚙️ Configuration

### Environment Variables

For sensitive data, it's recommended to use environment variables:

```bash
# Backend
export DB_PASSWORD=YourSecurePassword
export JWT_SECRET=YourSuperSecureJWTSecret
export STRAVA_CLIENT_ID=YourStravaClientID
export STRAVA_CLIENT_SECRET=YourStravaClientSecret
export GARMIN_CLIENT_ID=YourGarminClientID
export GARMIN_CLIENT_SECRET=YourGarminClientSecret

# Start application
./mvnw spring-boot:run
```

### application.properties Overview

```properties
# Server
server.port=8080

# Database
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

# Email
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

## 🗄️ Database Setup

### Automatic Migrations (Liquibase)

Liquibase automatically executes all database migrations on application startup. Migrations are located in:

```
mainstream-backend/src/main/resources/db/changelog/
```

### Import Test Data

For development purposes, you can populate the database with test data:

```bash
# Simple Bash script (recommended)
./seed-database.sh

# Or manually with MySQL
mysql -u mainstream -p mainstream < mainstream-backend/src/main/resources/db/seed-testdata.sql
```

**Test users after seeding:**
- Admin: `admin@mainstream.app` / `password123`
- User: `test.mueller@mainstream.app` / `password123`

For more details see [DATABASE_SEEDING.md](DATABASE_SEEDING.md)

## 💻 Development

### Backend Development

```bash
cd mainstream-backend

# Run tests
./mvnw test

# With auto-reload (Spring Boot DevTools)
./mvnw spring-boot:run

# Build for production
./mvnw clean package
```

### Frontend Development

```bash
cd mainstream-frontend

# Development server with auto-reload
ng serve

# Run tests
ng test

# E2E tests
ng e2e

# Production build
ng build --configuration production
```

### Code Quality

**Backend:**
- Follow Spring Boot Best Practices
- Use Lombok for boilerplate reduction
- MapStruct for DTO mappings
- Write unit and integration tests

**Frontend:**
- Follow Angular Style Guide
- Use Angular Material components
- Reactive programming with RxJS
- TypeScript Strict Mode enabled

## 📚 API Documentation

### Authentication

All protected endpoints require a JWT token in the header:

```
Authorization: Bearer <jwt-token>
```

Alternatively, `X-User-Id` is also supported for legacy purposes.

### Main Endpoints

#### Authentication

```
POST   /api/users/register          - Registration
POST   /api/users/login             - Login
POST   /api/users/logout            - Logout
GET    /api/users/me                - Current user
```

#### Activities/Runs

```
GET    /api/runs                    - Get all runs
GET    /api/runs/{id}               - Get single run
POST   /api/runs                    - Create run
PUT    /api/runs/{id}               - Update run
DELETE /api/runs/{id}               - Delete run
GET    /api/runs/{id}/gps           - Get GPS data
```

#### File Upload

```
POST   /api/fit/upload              - Upload FIT file
POST   /api/gpx/upload              - Upload GPX file
```

#### Strava Integration

```
GET    /api/strava/auth-url         - Get OAuth URL
POST   /api/strava/connect          - Connect account
DELETE /api/strava/disconnect       - Disconnect account
POST   /api/strava/sync             - Sync activities
GET    /api/strava/status           - Connection status
```

#### Garmin Integration

```
GET    /api/garmin/auth-url         - Get OAuth URL
POST   /api/garmin/connect          - Connect account
DELETE /api/garmin/disconnect       - Disconnect account
POST   /api/garmin/sync             - Sync activities
GET    /api/garmin/status           - Connection status
```

#### Competitions

```
GET    /api/competitions            - All competitions
GET    /api/competitions/{id}       - Competition details
POST   /api/competitions/{id}/join  - Join competition
GET    /api/competitions/{id}/leaderboard - Leaderboard
```

#### Dashboard

```
GET    /api/dashboard/stats         - Personal statistics
GET    /api/dashboard/recent        - Recent activities
GET    /api/dashboard/achievements  - Achievements
```

Complete API documentation (Swagger/OpenAPI) available at:
```
http://localhost:8080/swagger-ui.html (planned)
```

## 🔗 Integrations

### Strava

For Strava integration you need:

1. Strava Developer Account
2. Registered Strava Application
3. Client ID and Client Secret

**Setup Guide:** [STRAVA_INTEGRATION.md](STRAVA_INTEGRATION.md)

### Garmin Connect

For Garmin integration you need:

1. Garmin Developer Program access
2. Approved Garmin Connect API Application
3. Client ID and Client Secret

**Setup Guide:** [GARMIN_INTEGRATION.md](GARMIN_INTEGRATION.md)

### Nike Run Club

Nike integration is planned/in development.

## 🧪 Testing

### Backend Tests

```bash
cd mainstream-backend

# Run all tests
./mvnw test

# Specific test class
./mvnw test -Dtest=UserServiceTest

# With coverage report
./mvnw test jacoco:report
```

### Frontend Tests

```bash
cd mainstream-frontend

# Unit tests
ng test

# Tests with coverage
ng test --code-coverage

# E2E tests
ng e2e
```

## 🚢 Deployment

### Backend Deployment

#### Create JAR File

```bash
cd mainstream-backend
./mvnw clean package -DskipTests

# JAR file is located at:
# target/mainstream-backend-1.0.0.jar
```

#### Start Application

```bash
java -jar target/mainstream-backend-1.0.0.jar \
  --spring.datasource.url=jdbc:mariadb://prod-db:3306/mainstream \
  --spring.datasource.password=${DB_PASSWORD} \
  --jwt.secret=${JWT_SECRET}
```

### Frontend Deployment

```bash
cd mainstream-frontend

# Production build
ng build --configuration production

# Build artifacts are located at:
# dist/mainstream-frontend/
```

Deploy the `dist/` contents to:
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

## 🤝 Contributing

Contributions are welcome! Please follow these steps:

1. **Fork** the repository
2. **Create** a feature branch (`git checkout -b feature/AmazingFeature`)
3. **Commit** your changes (`git commit -m 'Add some AmazingFeature'`)
4. **Push** to the branch (`git push origin feature/AmazingFeature`)
5. **Open** a Pull Request

### Development Guidelines

- Follow existing code conventions
- Write tests for new features
- Update documentation
- Keep commits atomic and meaningful

## 📞 Support

For questions, issues, or suggestions:

- **Issues:** [GitHub Issues](https://github.com/Blindworks/mainstream/issues)
- **Documentation:** See the respective `.md` files in the repository
- **Email:** support@mainstream.app (if available)

## 📄 License

This project is licensed under the [MIT License](LICENSE).

---

## 🗺️ Roadmap

### Planned Features

- [ ] Mobile Apps (iOS & Android)
- [ ] Social Features (Friends, Activity Feed)
- [ ] Training Plans & Coaching
- [ ] Advanced Analytics with ML
- [ ] Webhook Support for Real-time Syncs
- [ ] Nike Run Club Full Integration
- [ ] Apple Health & Google Fit Integration
- [ ] Interval Training Tracking
- [ ] Heart Rate Zone Analysis
- [ ] Virtual Races & Events

### Version History

- **v1.0.0** - Initial Release
  - Core Run Tracking
  - Strava & Garmin Integration
  - Trophy System
  - Premium Subscriptions

---

**Built with ❤️ for the Running Community**
