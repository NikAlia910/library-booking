package gatling.simulations;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;

import io.gatling.javaapi.core.*;
import io.gatling.javaapi.http.*;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.UUID;

/**
 * Comprehensive Gatling simulation for Library Booking API
 * Tests all endpoints with proper authentication and CRUD operations
 */
public class ComprehensiveApiGatlingTest extends Simulation {

    // Configuration
    private static final String BASE_URL = System.getProperty("baseUrl", "http://localhost:8080");
    private static final int CONCURRENT_USERS = Integer.parseInt(System.getProperty("users", "10"));
    private static final int RAMP_DURATION = Integer.parseInt(System.getProperty("rampDuration", "10"));

    // HTTP Protocol Configuration
    private HttpProtocolBuilder httpProtocol = http
        .baseUrl(BASE_URL)
        .acceptHeader("application/json")
        .contentTypeHeader("application/json")
        .userAgentHeader("Gatling Performance Test");

    // Common Headers
    private Map<String, String> jsonHeaders = Map.of("Content-Type", "application/json", "Accept", "application/json");

    private Map<String, String> authenticatedHeaders = Map.of("Content-Type", "application/json", "Accept", "application/json");

    // Helper Methods for Timestamp Generation
    private String getCurrentTimestamp() {
        return Instant.now().truncatedTo(ChronoUnit.MILLIS).toString();
    }

    private String getFutureTimestamp(int hoursFromNow) {
        return Instant.now().plus(Duration.ofHours(hoursFromNow)).truncatedTo(ChronoUnit.MILLIS).toString();
    }

    // ============================================================================
    // AUTHENTICATION HELPER
    // ============================================================================

    private ChainBuilder authenticate() {
        return exec(session -> {
            System.out.println("Starting authentication process...");
            return session;
        }).exec(
            http("POST /api/authenticate - Get JWT Token")
                .post("/api/authenticate")
                .headers(jsonHeaders)
                .body(
                    StringBody(
                        """
                        {
                            "username": "admin",
                            "password": "admin",
                            "rememberMe": false
                        }
                        """
                    )
                )
                .check(status().is(200))
                .check(responseTimeInMillis().lte(2000))
                .check(jsonPath("$.id_token").saveAs("jwt_token"))
        );
    }

    // ============================================================================
    // RESOURCE CRUD OPERATIONS
    // ============================================================================

    private ChainBuilder createResource() {
        return exec(
            http("POST /api/resources - Create Resource")
                .post("/api/resources")
                .header("Authorization", session -> "Bearer " + session.getString("jwt_token"))
                .header("Content-Type", "application/json")
                .body(StringBody("{\"title\":\"Test Resource\",\"resourceType\":\"BOOK\"}"))
                .check(status().in(200, 201))
                .check(responseTimeInMillis().lte(5000))
                .check(jsonPath("$.id").saveAs("resourceId"))
        );
    }

    private ChainBuilder getResource() {
        return exec(
            http("GET /api/resources/{id} - Get Resource")
                .get("/api/resources/#{resourceId}")
                .header("Authorization", session -> "Bearer " + session.getString("jwt_token"))
                .check(status().is(200))
                .check(responseTimeInMillis().lte(1000))
                .check(jsonPath("$.title").exists())
        );
    }

    private ChainBuilder updateResource() {
        return exec(session -> {
            String uniqueId = UUID.randomUUID().toString().substring(0, 8);

            String updateJson = String.format(
                """
                {
                    "id": %s,
                    "title": "Updated Resource %s",
                    "author": "Updated Author",
                    "keywords": "updated testing",
                    "resourceType": "EQUIPMENT"
                }
                """,
                session.getString("resourceId"),
                uniqueId
            );

            return session.set("updateResourcePayload", updateJson);
        }).exec(
            http("PUT /api/resources/{id} - Update Resource")
                .put("/api/resources/#{resourceId}")
                .header("Authorization", session -> "Bearer " + session.getString("jwt_token"))
                .header("Content-Type", "application/json")
                .body(StringBody("${updateResourcePayload}"))
                .check(status().is(200))
                .check(responseTimeInMillis().lte(1000))
                .check(jsonPath("$.title").exists())
        );
    }

    private ChainBuilder deleteResource() {
        return exec(
            http("DELETE /api/resources/{id} - Delete Resource")
                .delete("/api/resources/#{resourceId}")
                .header("Authorization", session -> "Bearer " + session.getString("jwt_token"))
                .check(status().in(200, 204))
                .check(responseTimeInMillis().lte(1000))
        );
    }

    // ============================================================================
    // RESERVATION CRUD OPERATIONS
    // ============================================================================

    private ChainBuilder createReservation() {
        return exec(session -> {
            String uniqueId = UUID.randomUUID().toString().substring(0, 8);
            String reservationDate = getCurrentTimestamp();
            String startTime = getFutureTimestamp(1);
            String endTime = getFutureTimestamp(2);

            // First create a resource to reserve
            String resourceJson = String.format(
                """
                {
                    "title": "Reservable Resource %s",
                    "resourceType": "MEETING_ROOM"
                }
                """,
                uniqueId
            );

            return session
                .set("reservableResourcePayload", resourceJson)
                .set("reservationDate", reservationDate)
                .set("startTime", startTime)
                .set("endTime", endTime)
                .set("reservationUniqueId", uniqueId);
        })
            .exec(
                http("POST /api/resources - Create Resource for Reservation")
                    .post("/api/resources")
                    .header("Authorization", session -> "Bearer " + session.getString("jwt_token"))
                    .header("Content-Type", "application/json")
                    .body(StringBody("${reservableResourcePayload}"))
                    .check(status().in(200, 201))
                    .check(jsonPath("$.id").saveAs("reservableResourceId"))
                    .check(jsonPath("$.title").saveAs("reservableResourceTitle"))
                    .check(jsonPath("$.author").saveAs("reservableResourceAuthor"))
                    .check(jsonPath("$.keywords").saveAs("reservableResourceKeywords"))
                    .check(jsonPath("$.resourceType").saveAs("reservableResourceType"))
            )
            .exec(session -> {
                String reservationJson = String.format(
                    """
                    {
                        "reservationDate": "%s",
                        "startTime": "%s",
                        "endTime": "%s",
                        "user": {"id": 1},
                        "resource": {"id": %s}
                    }
                    """,
                    session.getString("reservationDate"),
                    session.getString("startTime"),
                    session.getString("endTime"),
                    session.getString("reservableResourceId")
                );

                return session.set("reservationPayload", reservationJson);
            })
            .exec(
                http("POST /api/reservations - Create Reservation")
                    .post("/api/reservations")
                    .header("Authorization", session -> "Bearer " + session.getString("jwt_token"))
                    .header("Content-Type", "application/json")
                    .body(StringBody("${reservationPayload}"))
                    .check(status().in(200, 201))
                    .check(responseTimeInMillis().lte(1000))
                    .check(jsonPath("$.id").saveAs("reservationId"))
            );
    }

    private ChainBuilder getReservation() {
        return exec(
            http("GET /api/reservations/{id} - Get Reservation")
                .get("/api/reservations/#{reservationId}")
                .header("Authorization", session -> "Bearer " + session.getString("jwt_token"))
                .check(status().is(200))
                .check(responseTimeInMillis().lte(1000))
                .check(jsonPath("$.reservationDate").exists())
        );
    }

    private ChainBuilder updateReservation() {
        return exec(session -> {
            String newEndTime = getFutureTimestamp(4);

            String updateJson = String.format(
                """
                {
                    "id": %s,
                    "reservationDate": "%s",
                    "startTime": "%s",
                    "endTime": "%s",
                    "reservationId": "RES-%s",
                    "user": {
                        "id": 1,
                        "login": "admin"
                    },
                    "resource": {
                        "id": %s,
                        "title": "%s",
                        "author": "%s",
                        "keywords": "%s",
                        "resourceType": "%s"
                    }
                }
                """,
                session.getString("reservationId"),
                session.getString("reservationDate"),
                session.getString("startTime"),
                newEndTime,
                session.getString("reservationUniqueId"),
                session.getString("reservableResourceId"),
                session.getString("reservableResourceTitle"),
                session.getString("reservableResourceAuthor"),
                session.getString("reservableResourceKeywords"),
                session.getString("reservableResourceType")
            );

            return session.set("updateReservationPayload", updateJson);
        }).exec(
            http("PUT /api/reservations/{id} - Update Reservation")
                .put("/api/reservations/#{reservationId}")
                .header("Authorization", session -> "Bearer " + session.getString("jwt_token"))
                .header("Content-Type", "application/json")
                .body(StringBody("${updateReservationPayload}"))
                .check(status().is(200))
                .check(responseTimeInMillis().lte(1000))
                .check(jsonPath("$.endTime").exists())
        );
    }

    private ChainBuilder deleteReservation() {
        return exec(
            http("DELETE /api/reservations/{id} - Delete Reservation")
                .delete("/api/reservations/#{reservationId}")
                .header("Authorization", session -> "Bearer " + session.getString("jwt_token"))
                .check(status().in(200, 204))
                .check(responseTimeInMillis().lte(1000))
        );
    }

    // ============================================================================
    // ADDITIONAL API ENDPOINT TESTS
    // ============================================================================

    private ChainBuilder testAdditionalEndpoints() {
        return exec(session -> {
            System.out.println("Testing additional API endpoints...");
            return session;
        })
            // List all resources with pagination
            .exec(
                http("GET /api/resources - List Resources")
                    .get("/api/resources?page=0&size=10&sort=id,asc")
                    .header("Authorization", session -> "Bearer " + session.getString("jwt_token"))
                    .check(status().is(200))
                    .check(responseTimeInMillis().lte(1000))
            )
            .pause(1)
            // Search resources by type
            .exec(
                http("GET /api/resources/search/type - Search by Type")
                    .get("/api/resources/search/type?resourceType=BOOK&page=0&size=10")
                    .header("Authorization", session -> "Bearer " + session.getString("jwt_token"))
                    .check(status().is(200))
                    .check(responseTimeInMillis().lte(1000))
            )
            .pause(1)
            // List all reservations
            .exec(
                http("GET /api/reservations - List Reservations")
                    .get("/api/reservations?page=0&size=10&sort=id,asc")
                    .header("Authorization", session -> "Bearer " + session.getString("jwt_token"))
                    .check(status().is(200))
                    .check(responseTimeInMillis().lte(1000))
            )
            .pause(1)
            // Check resource availability
            .exec(session -> {
                String startTime = getFutureTimestamp(5);
                String endTime = getFutureTimestamp(6);
                return session.set("availabilityStartTime", startTime).set("availabilityEndTime", endTime);
            })
            .exec(
                http("GET /api/reservations/availability/{resourceId} - Check Availability")
                    .get("/api/reservations/availability/#{reservableResourceId}")
                    .queryParam("startTime", "#{availabilityStartTime}")
                    .queryParam("endTime", "#{availabilityEndTime}")
                    .header("Authorization", session -> "Bearer " + session.getString("jwt_token"))
                    .check(status().is(200))
                    .check(responseTimeInMillis().lte(1000))
            )
            .pause(1)
            // Get current account
            .exec(
                http("GET /api/account - Get Current Account")
                    .get("/api/account")
                    .header("Authorization", session -> "Bearer " + session.getString("jwt_token"))
                    .check(status().is(200))
                    .check(responseTimeInMillis().lte(1000))
                    .check(jsonPath("$.login").exists())
            )
            .pause(1)
            // List authorities
            .exec(
                http("GET /api/authorities - List Authorities")
                    .get("/api/authorities")
                    .header("Authorization", session -> "Bearer " + session.getString("jwt_token"))
                    .check(status().is(200))
                    .check(responseTimeInMillis().lte(1000))
            )
            .pause(1)
            // List users (public endpoint)
            .exec(
                http("GET /api/users - List Public Users")
                    .get("/api/users?page=0&size=10&sort=id,asc")
                    .header("Authorization", session -> "Bearer " + session.getString("jwt_token"))
                    .check(status().is(200))
                    .check(responseTimeInMillis().lte(1000))
            );
    }

    // ============================================================================
    // SCENARIO COMPOSITION
    // ============================================================================

    private ScenarioBuilder comprehensiveApiScenario = scenario("Comprehensive API Test")
        .exec(authenticate())
        .pause(2)
        // Resource CRUD operations
        .exec(createResource())
        .pause(1)
        .exec(getResource())
        .pause(1)
        .exec(updateResource())
        .pause(1)
        // Reservation CRUD operations
        .exec(createReservation())
        .pause(1)
        .exec(getReservation())
        .pause(1)
        .exec(updateReservation())
        .pause(1)
        // Test additional endpoints
        .exec(testAdditionalEndpoints())
        .pause(1)
        // Cleanup - delete reservation first (due to foreign key constraint)
        .exec(deleteReservation())
        .pause(1)
        .exec(deleteResource())
        .pause(1)
        .exec(session -> {
            System.out.println("Comprehensive API test completed successfully");
            return session;
        });

    // ============================================================================
    // LOAD PATTERN AND ASSERTIONS
    // ============================================================================

    {
        setUp(comprehensiveApiScenario.injectOpen(rampUsers(CONCURRENT_USERS).during(Duration.ofSeconds(RAMP_DURATION))))
            .protocols(httpProtocol)
            .assertions(
                global().successfulRequests().percent().gte(45.0),
                global().responseTime().mean().lte(1000),
                forAll().responseTime().max().lte(5000),
                details("POST /api/authenticate - Get JWT Token").responseTime().percentile(95).lte(2000),
                details("POST /api/resources - Create Resource").responseTime().percentile(95).lte(2000),
                details("POST /api/reservations - Create Reservation").responseTime().percentile(95).lte(2000)
            );
    }
}
