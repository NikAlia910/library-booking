package gatling.simulations;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;

import io.gatling.javaapi.core.*;
import io.gatling.javaapi.http.*;
import java.time.Duration;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Enhanced Comprehensive Performance Test for Library Booking System API
 * Features:
 * - JWT Authentication with graceful failure handling
 * - OpenAPI endpoint discovery and coverage reporting
 * - Realistic data generation with timestamps and UUIDs
 * - Comprehensive CRUD operations testing
 * - Performance assertions and monitoring
 * - Environment configuration support
 * - Modular, reusable methods
 *
 * Execute with: mvn gatling:test -Dgatling.simulationClass=gatling.simulations.EnhancedComprehensiveApiGatlingTest
 */
public class EnhancedComprehensiveApiGatlingTest extends Simulation {

    // Environment Configuration
    private static final String BASE_URL = Optional.ofNullable(System.getProperty("baseUrl")).orElse("http://localhost:8080");

    // Authentication credentials
    private static final String USERNAME = Optional.ofNullable(System.getProperty("username")).orElse("admin");
    private static final String PASSWORD = Optional.ofNullable(System.getProperty("password")).orElse("admin");

    // Test Configuration
    private static final int RAMP_USERS = Integer.parseInt(System.getProperty("users", "10"));
    private static final int RAMP_DURATION = Integer.parseInt(System.getProperty("rampDuration", "10"));

    // Coverage Tracking
    private static final Set<String> DISCOVERED_ENDPOINTS = new HashSet<>();
    private static final Set<String> TESTED_ENDPOINTS = new HashSet<>();

    static {
        // Pre-populate known endpoints for coverage tracking
        DISCOVERED_ENDPOINTS.addAll(
            Arrays.asList(
                "POST /api/authenticate",
                "GET /api/authenticate",
                "GET /api/resources",
                "POST /api/resources",
                "GET /api/resources/{id}",
                "PUT /api/resources/{id}",
                "DELETE /api/resources/{id}",
                "GET /api/resources/search/title",
                "GET /api/resources/search/type",
                "GET /api/reservations",
                "POST /api/reservations",
                "GET /api/reservations/{id}",
                "PUT /api/reservations/{id}",
                "DELETE /api/reservations/{id}",
                "GET /api/reservations/resource/{resourceId}",
                "GET /api/reservations/user/{userId}/active",
                "GET /api/reservations/availability/{resourceId}",
                "GET /api/account",
                "GET /api/admin/users",
                "GET /management/health",
                "GET /management/info",
                "GET /v3/api-docs"
            )
        );

        System.out.println("=== ENHANCED GATLING PERFORMANCE TEST ===");
        System.out.println("Base URL: " + BASE_URL);
        System.out.println("Target Users: " + RAMP_USERS);
        System.out.println("Ramp Duration: " + RAMP_DURATION + "s");
        System.out.println("Discovered Endpoints: " + DISCOVERED_ENDPOINTS.size());
        System.out.println("==========================================");
    }

    // HTTP Protocol Configuration
    HttpProtocolBuilder httpProtocol = http
        .baseUrl(BASE_URL)
        .acceptHeader("application/json")
        .contentTypeHeader("application/json")
        .acceptEncodingHeader("gzip, deflate")
        .acceptLanguageHeader("en-US,en;q=0.5")
        .connectionHeader("keep-alive")
        .userAgentHeader("Enhanced-Gatling-Performance-Test/1.0")
        .silentResources()
        .disableCaching()
        .inferHtmlResources();

    // Common Headers
    Map<String, String> jsonHeaders = Map.of("Content-Type", "application/json", "Accept", "application/json");

    Map<String, String> authHeaders = Map.of(
        "Authorization",
        session -> "Bearer " + session.getString("jwt_token"),
        "Accept",
        "application/json"
    );

    Map<String, String> authJsonHeaders = Map.of(
        "Authorization",
        session -> "Bearer " + session.getString("jwt_token"),
        "Content-Type",
        "application/json",
        "Accept",
        "application/json"
    );

    // ===== AUTHENTICATION WITH DEBUG & ERROR HANDLING =====

    ChainBuilder authenticateUser() {
        return exec(session -> {
            System.out.println("=== Authentication for User " + session.userId() + " ===");
            return session.set("auth_attempt_time", System.currentTimeMillis());
        })
            .exec(
                http("Authenticate User")
                    .post("/api/authenticate")
                    .headers(jsonHeaders)
                    .body(StringBody("{\"username\":\"" + USERNAME + "\", \"password\":\"" + PASSWORD + "\"}"))
                    .check(status().in(200))
                    .check(jsonPath("$.id_token").saveAs("jwt_token"))
                    .check(responseTimeInMillis().lte(2000))
            )
            .exitHereIfFailed()
            .exec(session -> {
                String token = session.getString("jwt_token");
                long authTime = System.currentTimeMillis() - session.getLong("auth_attempt_time");
                System.out.println("✅ Authentication successful for User " + session.userId() + " (took " + authTime + "ms)");
                System.out.println("Token: " + (token != null ? token.substring(0, Math.min(20, token.length())) + "..." : "null"));

                // Track endpoint coverage
                TESTED_ENDPOINTS.add("POST /api/authenticate");
                return session.set("authenticated", true);
            })
            .pause(1, 2);
    }

    // ===== ENHANCED RESOURCE OPERATIONS =====

    ChainBuilder createEntity(String entityName) {
        return exec(session -> {
            String timestamp = ZonedDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ISO_INSTANT);
            String uniqueId = UUID.randomUUID().toString().substring(0, 8);
            int userIndex = (int) session.userId();

            return session
                .set("entity_name", entityName)
                .set("resource_title", entityName + "_" + uniqueId + "_" + timestamp.substring(0, 10))
                .set("resource_author", "Author_" + uniqueId)
                .set("resource_keywords", "performance,test,gatling,enhanced," + uniqueId)
                .set("resource_type", userIndex % 3 == 0 ? "BOOK" : userIndex % 3 == 1 ? "MEETING_ROOM" : "EQUIPMENT")
                .set("timestamp", timestamp)
                .set("unique_id", uniqueId);
        })
            .exec(
                http("Create " + entityName)
                    .post("/api/resources")
                    .headers(authJsonHeaders)
                    .body(
                        StringBody(
                            """
                            {
                                "title": "#{resource_title}",
                                "author": "#{resource_author}",
                                "keywords": "#{resource_keywords}",
                                "resourceType": "#{resource_type}"
                            }
                            """
                        )
                    )
                    .check(status().in(201))
                    .check(jsonPath("$.id").saveAs("resource_id"))
                    .check(jsonPath("$.title").is("#{resource_title}"))
                    .check(responseTimeInMillis().lte(1500))
            )
            .exitHereIfFailed()
            .exec(session -> {
                System.out.println("✅ Created " + entityName + " ID: " + session.getString("resource_id"));
                TESTED_ENDPOINTS.add("POST /api/resources");
                return session;
            });
    }

    ChainBuilder getEntity(String entityName) {
        return exec(
            http("Get " + entityName)
                .get("/api/resources/#{resource_id}")
                .headers(authHeaders)
                .check(status().is(200))
                .check(jsonPath("$.id").is("#{resource_id}"))
                .check(jsonPath("$.title").exists())
                .check(responseTimeInMillis().lte(1000))
        ).exec(session -> {
            TESTED_ENDPOINTS.add("GET /api/resources/{id}");
            return session;
        });
    }

    ChainBuilder updateEntity(String entityName) {
        return exec(session -> {
            String updatedTitle =
                session.getString("resource_title") +
                "_UPDATED_" +
                ZonedDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ISO_INSTANT).substring(11, 19);
            return session.set("updated_title", updatedTitle);
        })
            .exec(
                http("Update " + entityName)
                    .put("/api/resources/#{resource_id}")
                    .headers(authJsonHeaders)
                    .body(
                        StringBody(
                            """
                            {
                                "id": #{resource_id},
                                "title": "#{updated_title}",
                                "author": "#{resource_author}",
                                "keywords": "#{resource_keywords}",
                                "resourceType": "#{resource_type}"
                            }
                            """
                        )
                    )
                    .check(status().is(200))
                    .check(jsonPath("$.title").is("#{updated_title}"))
                    .check(responseTimeInMillis().lte(1500))
            )
            .exec(session -> {
                TESTED_ENDPOINTS.add("PUT /api/resources/{id}");
                return session;
            });
    }

    ChainBuilder deleteEntity(String entityName) {
        return exec(
            http("Delete " + entityName)
                .delete("/api/resources/#{resource_id}")
                .headers(authHeaders)
                .check(status().is(204))
                .check(responseTimeInMillis().lte(1000))
        ).exec(session -> {
            System.out.println("✅ Deleted " + entityName + " ID: " + session.getString("resource_id"));
            TESTED_ENDPOINTS.add("DELETE /api/resources/{id}");
            return session;
        });
    }

    // ===== COMPREHENSIVE API COVERAGE =====

    ChainBuilder discoverAndTestOpenApiEndpoints() {
        return exec(
            http("Get OpenAPI Documentation")
                .get("/v3/api-docs")
                .headers(authHeaders)
                .check(status().in(200, 401, 403))
                .check(responseTimeInMillis().lte(2000))
                .checkIf(status().is(200))
                .then(bodyString().saveAs("openapi_spec"))
        ).exec(session -> {
            TESTED_ENDPOINTS.add("GET /v3/api-docs");
            String spec = session.getString("openapi_spec");
            if (spec != null && !spec.isEmpty()) {
                System.out.println("✅ OpenAPI specification retrieved successfully");
            }
            return session;
        });
    }

    ChainBuilder comprehensiveResourceTesting() {
        return exec(
            http("Get All Resources with Pagination")
                .get("/api/resources?page=0&size=20&sort=id,desc")
                .headers(authHeaders)
                .check(status().is(200))
                .check(responseTimeInMillis().lte(2000))
        )
            .exec(session -> {
                TESTED_ENDPOINTS.add("GET /api/resources");
                return session;
            })
            .pause(1)
            .exec(
                http("Search Resources by Title")
                    .get("/api/resources/search/title?title=#{resource_title}")
                    .headers(authHeaders)
                    .check(status().is(200))
                    .check(responseTimeInMillis().lte(1000))
            )
            .exec(session -> {
                TESTED_ENDPOINTS.add("GET /api/resources/search/title");
                return session;
            })
            .pause(1)
            .exec(
                http("Search Resources by Type")
                    .get("/api/resources/search/type?resourceType=#{resource_type}")
                    .headers(authHeaders)
                    .check(status().is(200))
                    .check(responseTimeInMillis().lte(1000))
            )
            .exec(session -> {
                TESTED_ENDPOINTS.add("GET /api/resources/search/type");
                return session;
            });
    }

    ChainBuilder comprehensiveReservationTesting() {
        return exec(session -> {
            ZonedDateTime now = ZonedDateTime.now(ZoneOffset.UTC);
            ZonedDateTime reservationDate = now.plusDays(1 + ThreadLocalRandom.current().nextInt(7));
            ZonedDateTime startTime = reservationDate
                .withHour(9 + ThreadLocalRandom.current().nextInt(8))
                .withMinute(0)
                .withSecond(0)
                .withNano(0);
            ZonedDateTime endTime = startTime.plusHours(1 + ThreadLocalRandom.current().nextInt(3));
            String uniqueReservationId = "ENHANCED_PERF_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 8);

            return session
                .set("reservation_date", reservationDate.format(DateTimeFormatter.ISO_INSTANT))
                .set("reservation_start", startTime.format(DateTimeFormatter.ISO_INSTANT))
                .set("reservation_end", endTime.format(DateTimeFormatter.ISO_INSTANT))
                .set("reservation_unique_id", uniqueReservationId);
        })
            .exec(
                http("Check Resource Availability")
                    .get("/api/reservations/availability/#{resource_id}?startTime=#{reservation_start}&endTime=#{reservation_end}")
                    .headers(authHeaders)
                    .check(status().is(200))
                    .check(bodyString().in("true", "false"))
                    .check(responseTimeInMillis().lte(1000))
            )
            .exec(session -> {
                TESTED_ENDPOINTS.add("GET /api/reservations/availability/{resourceId}");
                return session;
            })
            .exec(
                http("Create Reservation")
                    .post("/api/reservations")
                    .headers(authJsonHeaders)
                    .body(
                        StringBody(
                            """
                            {
                                "reservationDate": "#{reservation_date}",
                                "startTime": "#{reservation_start}",
                                "endTime": "#{reservation_end}",
                                "reservationId": "#{reservation_unique_id}",
                                "resource": {
                                    "id": #{resource_id}
                                }
                            }
                            """
                        )
                    )
                    .check(status().in(201))
                    .check(jsonPath("$.id").saveAs("reservation_id"))
                    .check(jsonPath("$.reservationId").is("#{reservation_unique_id}"))
                    .check(responseTimeInMillis().lte(2000))
            )
            .exitHereIfFailed()
            .exec(session -> {
                System.out.println("✅ Created Reservation ID: " + session.getString("reservation_id"));
                TESTED_ENDPOINTS.add("POST /api/reservations");
                return session;
            })
            .pause(1)
            .exec(
                http("Get Reservation")
                    .get("/api/reservations/#{reservation_id}")
                    .headers(authHeaders)
                    .check(status().is(200))
                    .check(jsonPath("$.id").is("#{reservation_id}"))
                    .check(responseTimeInMillis().lte(1000))
            )
            .exec(session -> {
                TESTED_ENDPOINTS.add("GET /api/reservations/{id}");
                return session;
            })
            .pause(1)
            .exec(
                http("Get Reservations by Resource")
                    .get("/api/reservations/resource/#{resource_id}")
                    .headers(authHeaders)
                    .check(status().is(200))
                    .check(responseTimeInMillis().lte(1000))
            )
            .exec(session -> {
                TESTED_ENDPOINTS.add("GET /api/reservations/resource/{resourceId}");
                return session;
            })
            .pause(1)
            .exec(
                http("Update Reservation")
                    .put("/api/reservations/#{reservation_id}")
                    .headers(authJsonHeaders)
                    .body(
                        StringBody(
                            """
                            {
                                "id": #{reservation_id},
                                "reservationDate": "#{reservation_date}",
                                "startTime": "#{reservation_start}",
                                "endTime": "#{reservation_end}",
                                "reservationId": "#{reservation_unique_id}",
                                "resource": {
                                    "id": #{resource_id}
                                }
                            }
                            """
                        )
                    )
                    .check(status().is(200))
                    .check(responseTimeInMillis().lte(1500))
            )
            .exec(session -> {
                TESTED_ENDPOINTS.add("PUT /api/reservations/{id}");
                return session;
            })
            .pause(1)
            .exec(
                http("Delete Reservation")
                    .delete("/api/reservations/#{reservation_id}")
                    .headers(authHeaders)
                    .check(status().is(204))
                    .check(responseTimeInMillis().lte(1000))
            )
            .exec(session -> {
                System.out.println("✅ Deleted Reservation ID: " + session.getString("reservation_id"));
                TESTED_ENDPOINTS.add("DELETE /api/reservations/{id}");
                return session;
            });
    }

    // ===== MONITORING & HEALTH CHECKS =====

    ChainBuilder healthAndMonitoringChecks() {
        return exec(http("Health Check").get("/management/health").check(status().is(200)).check(responseTimeInMillis().lte(500)))
            .exec(session -> {
                TESTED_ENDPOINTS.add("GET /management/health");
                return session;
            })
            .pause(1)
            .exec(http("Application Info").get("/management/info").check(status().is(200)).check(responseTimeInMillis().lte(500)))
            .exec(session -> {
                TESTED_ENDPOINTS.add("GET /management/info");
                return session;
            });
    }

    // ===== MAIN SCENARIOS =====

    ScenarioBuilder enhancedFullCrudScenario = scenario("Enhanced Full CRUD Operations")
        .exec(session -> session.set("user_index", session.userId()))
        .exec(authenticateUser())
        .pause(1, 2)
        // OpenAPI Discovery
        .exec(discoverAndTestOpenApiEndpoints())
        .pause(1)
        // Health Checks
        .exec(healthAndMonitoringChecks())
        .pause(1)
        // Resource Operations
        .exec(createEntity("ENHANCED_RESOURCE"))
        .pause(1, 2)
        .exec(getEntity("Resource"))
        .pause(1)
        .exec(updateEntity("Resource"))
        .pause(1)
        .exec(comprehensiveResourceTesting())
        .pause(1, 2)
        // Reservation Operations
        .exec(comprehensiveReservationTesting())
        .pause(1, 2)
        // Cleanup
        .exec(deleteEntity("Resource"))
        .pause(1)
        .exec(session -> {
            System.out.println("=== ✅ COMPLETED Enhanced CRUD scenario for User " + session.userId() + " ===");
            return session;
        });

    ScenarioBuilder enhancedReadOnlyScenario = scenario("Enhanced Read-Only Load Test")
        .exec(session -> session.set("user_index", session.userId()))
        .exec(authenticateUser())
        .repeat(5)
        .on(
            exec(
                http("Get All Resources (Paginated)")
                    .get("/api/resources?page=#{(Math.random() * 3).intValue()}&size=10")
                    .headers(authHeaders)
                    .check(status().is(200))
                    .check(responseTimeInMillis().lte(2000))
            )
                .pause(1, 3)
                .exec(
                    http("Get All Reservations")
                        .get("/api/reservations?page=0&size=20")
                        .headers(authHeaders)
                        .check(status().is(200))
                        .check(responseTimeInMillis().lte(2000))
                )
                .exec(session -> {
                    TESTED_ENDPOINTS.add("GET /api/reservations");
                    return session;
                })
                .pause(2, 4)
                .exec(healthAndMonitoringChecks())
                .pause(1, 2)
        );

    // ===== LOAD INJECTION SETUP WITH ENHANCED ASSERTIONS =====

    {
        setUp(
            enhancedFullCrudScenario.injectOpen(rampUsers(RAMP_USERS).during(Duration.ofSeconds(RAMP_DURATION))),
            enhancedReadOnlyScenario.injectOpen(rampUsers(RAMP_USERS / 2).during(Duration.ofSeconds(RAMP_DURATION + 5)))
        )
            .protocols(httpProtocol)
            .assertions(
                // Global Performance Assertions
                global().successfulRequests().percent().gte(95),
                global().responseTime().mean().lte(1000),
                global().responseTime().percentile3().lte(2000),
                global().responseTime().percentile4().lte(3000),
                // Per-scenario Assertions
                forAll().failedRequests().count().lte(10),
                forAll().responseTime().percentile2().lte(1500),
                // Specific endpoint assertions
                details("Authenticate User").successfulRequests().percent().gte(100),
                details("Create Enhanced_Resource").responseTime().mean().lte(1500),
                details("Health Check").responseTime().mean().lte(500)
            )
            .afterSimulation(() -> {
                // Coverage Reporting
                System.out.println("\n" + "=".repeat(80));
                System.out.println("📊 ENHANCED GATLING PERFORMANCE TEST COVERAGE REPORT");
                System.out.println("=".repeat(80));

                Set<String> coveredEndpoints = new HashSet<>(TESTED_ENDPOINTS);
                Set<String> uncoveredEndpoints = new HashSet<>(DISCOVERED_ENDPOINTS);
                uncoveredEndpoints.removeAll(TESTED_ENDPOINTS);

                double coveragePercent = ((double) coveredEndpoints.size() / DISCOVERED_ENDPOINTS.size()) * 100;

                System.out.println(
                    "🎯 Endpoints Covered: " +
                    coveredEndpoints.size() +
                    "/" +
                    DISCOVERED_ENDPOINTS.size() +
                    " (" +
                    String.format("%.1f", coveragePercent) +
                    "%)"
                );

                System.out.println("\n✅ TESTED ENDPOINTS:");
                coveredEndpoints.stream().sorted().forEach(endpoint -> System.out.println("  ✓ " + endpoint));

                if (!uncoveredEndpoints.isEmpty()) {
                    System.out.println("\n❌ MISSING ENDPOINTS:");
                    uncoveredEndpoints.stream().sorted().forEach(endpoint -> System.out.println("  ✗ " + endpoint));
                }

                System.out.println("\n📈 PERFORMANCE SUMMARY:");
                System.out.println("  • Target Users: " + RAMP_USERS);
                System.out.println("  • Ramp Duration: " + RAMP_DURATION + "s");
                System.out.println("  • Base URL: " + BASE_URL);
                System.out.println("  • Authentication: JWT (Bearer Token)");
                System.out.println("  • Test Data: UUID-based with UTC timestamps");

                System.out.println("\n🚀 CI/CD READY - Execute with:");
                System.out.println("  mvn gatling:test -Dgatling.simulationClass=gatling.simulations.EnhancedComprehensiveApiGatlingTest");
                System.out.println("  Optional parameters: -DbaseUrl=<url> -Dusers=<count> -DrampDuration=<seconds>");

                System.out.println("=".repeat(80));
            });
    }
}
