package gatling.simulations;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;

import io.gatling.javaapi.core.*;
import io.gatling.javaapi.http.*;
import java.time.Duration;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Comprehensive Performance Test for Library Booking System API
 * Tests all CRUD operations with JWT authentication and realistic data generation
 */
public class ComprehensiveApiGatlingTest extends Simulation {

    // Environment Configuration
    private static final String BASE_URL = Optional.ofNullable(System.getProperty("baseUrl")).orElse("http://localhost:8080");

    // Authentication credentials
    private static final String USERNAME = "admin";
    private static final String PASSWORD = "admin";

    // HTTP Protocol Configuration
    HttpProtocolBuilder httpProtocol = http
        .baseUrl(BASE_URL)
        .acceptHeader("application/json")
        .contentTypeHeader("application/json")
        .acceptEncodingHeader("gzip, deflate")
        .acceptLanguageHeader("en-US,en;q=0.5")
        .connectionHeader("keep-alive")
        .userAgentHeader("Gatling Performance Test")
        .silentResources();

    // Common Headers
    Map<String, String> jsonHeaders = Map.of("Content-Type", "application/json", "Accept", "application/json");
    Map<String, String> authHeaders = Map.of("Authorization", "Bearer #{jwt_token}", "Accept", "application/json");
    Map<String, String> authJsonHeaders = Map.of(
        "Authorization",
        "Bearer #{jwt_token}",
        "Content-Type",
        "application/json",
        "Accept",
        "application/json"
    );

    // ===== AUTHENTICATION HELPER =====

    ChainBuilder authenticateUser() {
        return exec(session -> {
            System.out.println("=== Starting Authentication ===");
            return session;
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
                System.out.println(
                    "Token obtained: " + (token != null ? token.substring(0, Math.min(20, token.length())) + "..." : "null")
                );
                return session;
            })
            .pause(1);
    }

    // ===== RESOURCE CRUD OPERATIONS =====

    ChainBuilder createResource(String namePrefix) {
        return exec(session -> {
            String timestamp = ZonedDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ISO_INSTANT);
            String uniqueId = UUID.randomUUID().toString().substring(0, 8);

            return session
                .set("resource_title", namePrefix + "_" + uniqueId)
                .set("resource_author", "Author_" + uniqueId)
                .set("resource_keywords", "performance,test,gatling," + uniqueId)
                .set(
                    "resource_type",
                    session.getInt("user_index") % 3 == 0 ? "BOOK" : session.getInt("user_index") % 3 == 1 ? "MEETING_ROOM" : "EQUIPMENT"
                )
                .set("timestamp", timestamp);
        })
            .exec(
                http("Create Resource")
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
                System.out.println("Created Resource ID: " + session.getString("resource_id"));
                return session;
            });
    }

    ChainBuilder getResource() {
        return exec(
            http("Get Resource")
                .get("/api/resources/#{resource_id}")
                .headers(authHeaders)
                .check(status().is(200))
                .check(jsonPath("$.id").is("#{resource_id}"))
                .check(jsonPath("$.title").exists())
                .check(responseTimeInMillis().lte(1000))
        );
    }

    ChainBuilder updateResource() {
        return exec(session -> {
            String updatedTitle = session.getString("resource_title") + "_UPDATED";
            return session.set("updated_title", updatedTitle);
        }).exec(
            http("Update Resource")
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
        );
    }

    ChainBuilder searchResources() {
        return exec(
            http("Search Resources by Title")
                .get("/api/resources/search/title?title=#{resource_title}")
                .headers(authHeaders)
                .check(status().is(200))
                .check(responseTimeInMillis().lte(1000))
        )
            .pause(1)
            .exec(
                http("Search Resources by Type")
                    .get("/api/resources/search/type?resourceType=#{resource_type}")
                    .headers(authHeaders)
                    .check(status().is(200))
                    .check(responseTimeInMillis().lte(1000))
            );
    }

    ChainBuilder getAllResources() {
        return exec(
            http("Get All Resources")
                .get("/api/resources?page=0&size=20")
                .headers(authHeaders)
                .check(status().is(200))
                .check(responseTimeInMillis().lte(2000))
        );
    }

    // ===== RESERVATION CRUD OPERATIONS =====

    ChainBuilder createReservation() {
        return exec(session -> {
            ZonedDateTime now = ZonedDateTime.now(ZoneOffset.UTC);
            ZonedDateTime reservationDate = now.plusDays(1);
            ZonedDateTime startTime = reservationDate.withHour(9).withMinute(0).withSecond(0).withNano(0);
            ZonedDateTime endTime = startTime.plusHours(2);
            String uniqueReservationId = "PERF_TEST_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 8);

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
                    .check(bodyString().is("true"))
                    .check(responseTimeInMillis().lte(1000))
            )
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
                System.out.println("Created Reservation ID: " + session.getString("reservation_id"));
                return session;
            });
    }

    ChainBuilder getReservation() {
        return exec(
            http("Get Reservation")
                .get("/api/reservations/#{reservation_id}")
                .headers(authHeaders)
                .check(status().is(200))
                .check(jsonPath("$.id").is("#{reservation_id}"))
                .check(jsonPath("$.reservationId").is("#{reservation_unique_id}"))
                .check(responseTimeInMillis().lte(1000))
        );
    }

    ChainBuilder updateReservation() {
        return exec(session -> {
            ZonedDateTime currentStart = ZonedDateTime.parse(session.getString("reservation_start"));
            ZonedDateTime newEnd = currentStart.plusHours(3); // Extend by 1 hour
            return session.set("new_reservation_end", newEnd.format(DateTimeFormatter.ISO_INSTANT));
        }).exec(
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
                            "endTime": "#{new_reservation_end}",
                            "reservationId": "#{reservation_unique_id}",
                            "resource": {
                                "id": #{resource_id}
                            }
                        }
                        """
                    )
                )
                .check(status().is(200))
                .check(jsonPath("$.endTime").exists())
                .check(responseTimeInMillis().lte(1500))
        );
    }

    ChainBuilder getReservationsByResource() {
        return exec(
            http("Get Reservations by Resource")
                .get("/api/reservations/resource/#{resource_id}")
                .headers(authHeaders)
                .check(status().is(200))
                .check(responseTimeInMillis().lte(1000))
        );
    }

    // ===== CLEANUP OPERATIONS =====

    ChainBuilder deleteReservation() {
        return exec(
            http("Delete Reservation")
                .delete("/api/reservations/#{reservation_id}")
                .headers(authHeaders)
                .check(status().is(204))
                .check(responseTimeInMillis().lte(1000))
        ).exec(session -> {
            System.out.println("Deleted Reservation ID: " + session.getString("reservation_id"));
            return session;
        });
    }

    ChainBuilder deleteResource() {
        return exec(
            http("Delete Resource")
                .delete("/api/resources/#{resource_id}")
                .headers(authHeaders)
                .check(status().is(204))
                .check(responseTimeInMillis().lte(1000))
        ).exec(session -> {
            System.out.println("Deleted Resource ID: " + session.getString("resource_id"));
            return session;
        });
    }

    // ===== MAIN SCENARIOS =====

    ScenarioBuilder fullCrudScenario = scenario("Full CRUD Operations")
        .exec(session -> session.set("user_index", session.userId()))
        .exec(authenticateUser())
        .pause(2)
        // Resource Operations
        .exec(createResource("PERF_RESOURCE"))
        .pause(1, 3)
        .exec(getResource())
        .pause(1)
        .exec(updateResource())
        .pause(1)
        .exec(searchResources())
        .pause(1)
        .exec(getAllResources())
        .pause(2)
        // Reservation Operations
        .exec(createReservation())
        .pause(1, 2)
        .exec(getReservation())
        .pause(1)
        .exec(updateReservation())
        .pause(1)
        .exec(getReservationsByResource())
        .pause(2)
        // Cleanup
        .exec(deleteReservation())
        .pause(1)
        .exec(deleteResource())
        .pause(1)
        .exec(session -> {
            System.out.println("=== Completed CRUD scenario for user " + session.userId() + " ===");
            return session;
        });

    ScenarioBuilder readOnlyScenario = scenario("Read-Only Operations")
        .exec(session -> session.set("user_index", session.userId()))
        .exec(authenticateUser())
        .repeat(3)
        .on(
            exec(getAllResources())
                .pause(2, 5)
                .exec(
                    http("Get All Reservations")
                        .get("/api/reservations?page=0&size=20")
                        .headers(authHeaders)
                        .check(status().is(200))
                        .check(responseTimeInMillis().lte(2000))
                )
                .pause(1, 3)
        );

    // ===== LOAD INJECTION AND SETUP =====

    {
        setUp(
            fullCrudScenario.injectOpen(rampUsers(10).during(Duration.ofSeconds(10))),
            readOnlyScenario.injectOpen(rampUsers(5).during(Duration.ofSeconds(15)))
        )
            .protocols(httpProtocol)
            .assertions(
                global().successfulRequests().percent().gte(95),
                global().responseTime().mean().lte(1000),
                global().responseTime().percentile3().lte(2000),
                forAll().failedRequests().count().lte(10)
            );
    }
}
