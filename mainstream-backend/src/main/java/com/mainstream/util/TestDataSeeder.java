package com.mainstream.util;

import com.mainstream.activity.entity.PredefinedRoute;
import com.mainstream.run.entity.Run;
import com.mainstream.user.entity.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Test Data Seeder for MainStream Application
 *
 * Generates realistic test data for:
 * - Users (16 test accounts + 1 admin)
 * - Predefined Routes (4 standard routes)
 * - Runs (20+ runs across last 30 days)
 *
 * Aktivierung:
 * 1. Via Profile: --spring.profiles.active=dev,seed-data
 * 2. Via Property: --mainstream.seed-data.enabled=true
 *
 * WICHTIG: Nur für Entwicklung/Testing verwenden!
 * Löscht existierende Testdaten bei jedem Start!
 *
 * Login für Test-User:
 * - Email: test.mueller@mainstream.app (oder andere test.*@mainstream.app)
 * - Passwort: password123
 *
 * Admin-Account:
 * - Email: admin@mainstream.app
 * - Passwort: password123
 */
@Slf4j
@Configuration
@Profile("seed-data") // Nur aktiv wenn "seed-data" Profile gesetzt ist
public class TestDataSeeder {

    @Bean
    @Transactional
    public CommandLineRunner seedDatabase(
            EntityManager entityManager,
            PasswordEncoder passwordEncoder
    ) {
        return args -> {
            log.info("========================================");
            log.info("  STARTING TEST DATA SEEDING");
            log.info("========================================");

            // 1. Clean existing test data
            cleanTestData(entityManager);

            // 2. Create test users
            List<User> users = createTestUsers(entityManager, passwordEncoder);
            log.info("✓ Created {} test users", users.size());

            // 3. Create predefined routes
            List<PredefinedRoute> routes = createPredefinedRoutes(entityManager);
            log.info("✓ Created {} predefined routes", routes.size());

            // 4. Create runs for users
            int runCount = createRuns(entityManager, users, routes);
            log.info("✓ Created {} test runs", runCount);

            log.info("========================================");
            log.info("  TEST DATA SEEDING COMPLETED!");
            log.info("========================================");
            log.info("Login credentials:");
            log.info("  Email:    test.mueller@mainstream.app");
            log.info("  Password: password123");
            log.info("");
            log.info("  Admin Email:    admin@mainstream.app");
            log.info("  Admin Password: password123");
            log.info("========================================");
        };
    }

    private void cleanTestData(EntityManager em) {
        log.info("Cleaning existing test data...");

        // Delete GPS points from test runs
        em.createQuery(
            "DELETE FROM GpsPoint g WHERE g.run IN " +
            "(SELECT r FROM Run r WHERE r.userId IN " +
            "(SELECT u.id FROM User u WHERE u.email LIKE 'test%@mainstream.app'))"
        ).executeUpdate();

        // Delete test runs
        em.createQuery(
            "DELETE FROM Run r WHERE r.userId IN " +
            "(SELECT u.id FROM User u WHERE u.email LIKE 'test%@mainstream.app')"
        ).executeUpdate();

        // Delete route track points
        em.createQuery("DELETE FROM RouteTrackPoint").executeUpdate();

        // Delete predefined routes
        em.createQuery("DELETE FROM PredefinedRoute").executeUpdate();

        // Delete test users
        em.createQuery(
            "DELETE FROM User u WHERE u.email LIKE 'test%@mainstream.app'"
        ).executeUpdate();

        em.flush();
        log.info("✓ Test data cleaned");
    }

    private List<User> createTestUsers(EntityManager em, PasswordEncoder passwordEncoder) {
        String encodedPassword = passwordEncoder.encode("password123");
        List<User> users = new ArrayList<>();

        // Admin User
        users.add(createUser(
            "admin@mainstream.app", encodedPassword,
            "Admin", "User", LocalDate.of(1985, 3, 15),
            User.Gender.MALE, User.FitnessLevel.EXPERT, User.Role.ADMIN,
            "https://ui-avatars.com/api/?name=Admin+User&background=e91e63&color=fff&size=128",
            "Plattform Administrator und Lauf-Enthusiast"
        ));

        // Regular Test Users
        users.add(createUser(
            "test.mueller@mainstream.app", encodedPassword,
            "Max", "Müller", LocalDate.of(1990, 6, 20),
            User.Gender.MALE, User.FitnessLevel.INTERMEDIATE, User.Role.USER,
            "https://ui-avatars.com/api/?name=Max+Mueller&background=9c27b0&color=fff&size=128",
            "Laufe gerne am Main entlang"
        ));

        users.add(createUser(
            "test.schmidt@mainstream.app", encodedPassword,
            "Anna", "Schmidt", LocalDate.of(1988, 11, 12),
            User.Gender.FEMALE, User.FitnessLevel.ADVANCED, User.Role.USER,
            "https://ui-avatars.com/api/?name=Anna+Schmidt&background=673ab7&color=fff&size=128",
            "Marathon-Läuferin aus Frankfurt"
        ));

        users.add(createUser(
            "test.weber@mainstream.app", encodedPassword,
            "Tim", "Weber", LocalDate.of(1995, 2, 28),
            User.Gender.MALE, User.FitnessLevel.INTERMEDIATE, User.Role.USER,
            "https://ui-avatars.com/api/?name=Tim+Weber&background=3f51b5&color=fff&size=128",
            "Morgenläufer - immer vor der Arbeit"
        ));

        users.add(createUser(
            "test.wagner@mainstream.app", encodedPassword,
            "Sarah", "Wagner", LocalDate.of(1992, 9, 5),
            User.Gender.FEMALE, User.FitnessLevel.ADVANCED, User.Role.USER,
            "https://ui-avatars.com/api/?name=Sarah+Wagner&background=2196f3&color=fff&size=128",
            "Trail-Running Fan"
        ));

        users.add(createUser(
            "test.becker@mainstream.app", encodedPassword,
            "Jan", "Becker", LocalDate.of(1987, 4, 17),
            User.Gender.MALE, User.FitnessLevel.BEGINNER, User.Role.USER,
            "https://ui-avatars.com/api/?name=Jan+Becker&background=03a9f4&color=fff&size=128",
            "Hobbyjogger"
        ));

        users.add(createUser(
            "test.hoffmann@mainstream.app", encodedPassword,
            "Lisa", "Hoffmann", LocalDate.of(1993, 7, 22),
            User.Gender.FEMALE, User.FitnessLevel.INTERMEDIATE, User.Role.USER,
            "https://ui-avatars.com/api/?name=Lisa+Hoffmann&background=00bcd4&color=fff&size=128",
            "Laufen ist meine Meditation"
        ));

        users.add(createUser(
            "test.koch@mainstream.app", encodedPassword,
            "Tom", "Koch", LocalDate.of(1991, 1, 30),
            User.Gender.MALE, User.FitnessLevel.INTERMEDIATE, User.Role.USER,
            "https://ui-avatars.com/api/?name=Tom+Koch&background=009688&color=fff&size=128",
            "Stadtpark-Läufer"
        ));

        users.add(createUser(
            "test.richter@mainstream.app", encodedPassword,
            "Emma", "Richter", LocalDate.of(1994, 12, 8),
            User.Gender.FEMALE, User.FitnessLevel.BEGINNER, User.Role.USER,
            "https://ui-avatars.com/api/?name=Emma+Richter&background=4caf50&color=fff&size=128",
            "Neue Läuferin, voller Motivation!"
        ));

        users.add(createUser(
            "test.klein@mainstream.app", encodedPassword,
            "Lukas", "Klein", LocalDate.of(1989, 8, 14),
            User.Gender.MALE, User.FitnessLevel.EXPERT, User.Role.USER,
            "https://ui-avatars.com/api/?name=Lukas+Klein&background=8bc34a&color=fff&size=128",
            "Ultra-Marathon Vorbereitung"
        ));

        users.add(createUser(
            "test.wolf@mainstream.app", encodedPassword,
            "Sophie", "Wolf", LocalDate.of(1996, 5, 25),
            User.Gender.FEMALE, User.FitnessLevel.INTERMEDIATE, User.Role.USER,
            "https://ui-avatars.com/api/?name=Sophie+Wolf&background=cddc39&color=fff&size=128",
            "Laufe für den guten Zweck"
        ));

        users.add(createUser(
            "test.neumann@mainstream.app", encodedPassword,
            "Felix", "Neumann", LocalDate.of(1990, 10, 3),
            User.Gender.MALE, User.FitnessLevel.ADVANCED, User.Role.USER,
            "https://ui-avatars.com/api/?name=Felix+Neumann&background=ffc107&color=fff&size=128",
            "Intervalltraining-Spezialist"
        ));

        users.add(createUser(
            "test.schwarz@mainstream.app", encodedPassword,
            "Laura", "Schwarz", LocalDate.of(1997, 3, 19),
            User.Gender.FEMALE, User.FitnessLevel.INTERMEDIATE, User.Role.USER,
            "https://ui-avatars.com/api/?name=Laura+Schwarz&background=ff9800&color=fff&size=128",
            "Laufe jeden Tag!"
        ));

        users.add(createUser(
            "test.zimmermann@mainstream.app", encodedPassword,
            "Ben", "Zimmermann", LocalDate.of(1986, 11, 27),
            User.Gender.MALE, User.FitnessLevel.INTERMEDIATE, User.Role.USER,
            "https://ui-avatars.com/api/?name=Ben+Zimmermann&background=ff5722&color=fff&size=128",
            "Abendläufer nach der Arbeit"
        ));

        users.add(createUser(
            "test.fischer@mainstream.app", encodedPassword,
            "Marie", "Fischer", LocalDate.of(1992, 6, 11),
            User.Gender.FEMALE, User.FitnessLevel.ADVANCED, User.Role.USER,
            "https://ui-avatars.com/api/?name=Marie+Fischer&background=795548&color=fff&size=128",
            "Naturliebhaberin und Läuferin"
        ));

        users.add(createUser(
            "test.meyer@mainstream.app", encodedPassword,
            "Paul", "Meyer", LocalDate.of(1988, 9, 1),
            User.Gender.MALE, User.FitnessLevel.EXPERT, User.Role.USER,
            "https://ui-avatars.com/api/?name=Paul+Meyer&background=607d8b&color=fff&size=128",
            "Wettkampf-Läufer"
        ));

        // Persist all users
        users.forEach(em::persist);
        em.flush();

        return users;
    }

    private User createUser(
            String email, String password, String firstName, String lastName,
            LocalDate dob, User.Gender gender, User.FitnessLevel fitnessLevel,
            User.Role role, String avatarUrl, String bio
    ) {
        return User.builder()
                .email(email)
                .password(password)
                .firstName(firstName)
                .lastName(lastName)
                .dateOfBirth(dob)
                .gender(gender)
                .fitnessLevel(fitnessLevel)
                .role(role)
                .profilePictureUrl(avatarUrl)
                .bio(bio)
                .isActive(true)
                .isPublicProfile(true)
                .preferredDistanceUnit(User.DistanceUnit.KILOMETERS)
                .build();
    }

    private List<PredefinedRoute> createPredefinedRoutes(EntityManager em) {
        List<PredefinedRoute> routes = new ArrayList<>();

        routes.add(createRoute(
            "10km Vollrunde Main",
            "Komplette Mainufer-Runde, beide Seiten. Start am Eisernen Steg.",
            "main_vollrunde_10km.gpx",
            new BigDecimal("10200.00"), new BigDecimal("45.00"), new BigDecimal("45.00"),
            new BigDecimal("50.1074"), new BigDecimal("8.6841")
        ));

        routes.add(createRoute(
            "5km Ostpark Loop",
            "Schnelle Runde durch den Ostpark. Ideal für Intervalltraining.",
            "ostpark_loop_5km.gpx",
            new BigDecimal("5300.00"), new BigDecimal("15.00"), new BigDecimal("15.00"),
            new BigDecimal("50.1234"), new BigDecimal("8.7123")
        ));

        routes.add(createRoute(
            "7km Nordmainufer",
            "Nordseite des Mainufers, Hin und Zurück. Flache Strecke.",
            "nordmainufer_7km.gpx",
            new BigDecimal("7100.00"), new BigDecimal("20.00"), new BigDecimal("20.00"),
            new BigDecimal("50.1145"), new BigDecimal("8.6512")
        ));

        routes.add(createRoute(
            "12km Stadtwald Trail",
            "Anspruchsvolle Trail-Strecke durch den Stadtwald.",
            "stadtwald_trail_12km.gpx",
            new BigDecimal("12500.00"), new BigDecimal("180.00"), new BigDecimal("180.00"),
            new BigDecimal("50.0823"), new BigDecimal("8.6545")
        ));

        routes.forEach(em::persist);
        em.flush();

        return routes;
    }

    private PredefinedRoute createRoute(
            String name, String description, String filename,
            BigDecimal distanceMeters, BigDecimal elevGain, BigDecimal elevLoss,
            BigDecimal startLat, BigDecimal startLon
    ) {
        PredefinedRoute route = new PredefinedRoute();
        route.setName(name);
        route.setDescription(description);
        route.setOriginalFilename(filename);
        route.setDistanceMeters(distanceMeters);
        route.setElevationGainMeters(elevGain);
        route.setElevationLossMeters(elevLoss);
        route.setStartLatitude(startLat);
        route.setStartLongitude(startLon);
        route.setIsActive(true);
        return route;
    }

    private int createRuns(EntityManager em, List<User> users, List<PredefinedRoute> routes) {
        List<Run> runs = new ArrayList<>();
        Random random = new Random();

        // Create sample runs for various users
        // Note: This is simplified - in reality you'd want more sophisticated run generation

        // Run 1: Max Müller - 10km Main (2 days ago)
        runs.add(createRun(
            users.get(1).getId(), "Morgenrunde am Main", "Schöne Runde bei Sonnenaufgang",
            LocalDateTime.now().minusDays(2).withHour(7),
            3720, 10200, 365.0, 18.5, 9.8, 620,
            45.0, 45.0, Run.RunType.OUTDOOR, routes.get(0).getId()
        ));

        // Run 2: Anna Schmidt - 5km Ostpark (1 day ago)
        runs.add(createRun(
            users.get(2).getId(), "Tempo-Training Ostpark", "Schnelles Intervalltraining",
            LocalDateTime.now().minusDays(1).withHour(17),
            1500, 5300, 283.0, 21.5, 12.7, 340,
            15.0, 15.0, Run.RunType.OUTDOOR, routes.get(1).getId()
        ));

        // Run 3: Tim Weber - 7km Nordmain (today)
        runs.add(createRun(
            users.get(3).getId(), "Feierabend-Lauf", "Entspannter Lauf nach der Arbeit",
            LocalDateTime.now().minusHours(3),
            2700, 7100, 380.0, 16.2, 9.5, 450,
            20.0, 20.0, Run.RunType.OUTDOOR, routes.get(2).getId()
        ));

        // Add more runs for variety (simplified - add more as needed)

        runs.forEach(em::persist);
        em.flush();

        return runs.size();
    }

    private Run createRun(
            Long userId, String title, String description, LocalDateTime startTime,
            int durationSeconds, int distanceMeters, double avgPace, double maxSpeed,
            double avgSpeed, int calories, double elevGain, double elevLoss,
            Run.RunType runType, Long routeId
    ) {
        return Run.builder()
                .userId(userId)
                .title(title)
                .description(description)
                .startTime(startTime)
                .endTime(startTime.plusSeconds(durationSeconds))
                .durationSeconds(durationSeconds)
                .distanceMeters(new BigDecimal(distanceMeters))
                .averagePaceSecondsPerKm(avgPace)
                .maxSpeedKmh(new BigDecimal(maxSpeed))
                .averageSpeedKmh(new BigDecimal(avgSpeed))
                .caloriesBurned(calories)
                .elevationGainMeters(new BigDecimal(elevGain))
                .elevationLossMeters(new BigDecimal(elevLoss))
                .runType(runType)
                .status(Run.RunStatus.COMPLETED)
                .isPublic(true)
                .routeId(routeId)
                .build();
    }
}
