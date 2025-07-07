package com.mycompany.myapp.gatling;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;

import io.gatling.javaapi.core.*;
import io.gatling.javaapi.http.*;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public class AllApiSimulation extends Simulation {

    // Base URL (can be overridden by -DbaseUrl)
    private static final String BASE_URL = System.getProperty("baseUrl", "http://localhost:8080");

    // Helper to get current ISO timestamp
    private static String nowIso() {
        return ZonedDateTime.now().format(DateTimeFormatter.ISO_INSTANT);
    }

    // Helper to generate a unique string
    private static String unique(String prefix) {
        return prefix + "-" + UUID.randomUUID();
    }

    // HTTP protocol config
    HttpProtocolBuilder httpProtocol = http.baseUrl(BASE_URL).acceptHeader("application/json").contentTypeHeader("application/json");

    // Authenticate and extract JWT
    ChainBuilder authenticate = exec(
        http("Authenticate")
            .post("/api/authenticate")
            .body(StringBody("{ \"username\": \"admin\", \"password\": \"admin\" }"))
            .check(status().is(200))
            .check(jsonPath("$.id_token").saveAs("jwt_token"))
            .check(responseTimeInMillis().lt(1000))
    ).exec(session -> {
        System.out.println("[DEBUG] JWT Token: " + session.getString("jwt_token"));
        return session;
    });

    // Helper to add Authorization header and print debug info
    private static HttpRequestActionBuilder withAuth(HttpRequestActionBuilder req) {
        return req.header("Authorization", session -> {
            String token = session.getString("jwt_token");
            System.out.println("[DEBUG] Using Authorization: Bearer " + token);
            return "Bearer " + token;
        });
    }

    // Resource CRUD scenario
    ChainBuilder resourceCrud = exec(authenticate)
        .exec(session -> session.set("resource_title", unique("Book")))
        .exec(
            withAuth(
                http("Create Resource")
                    .post("/api/resources")
                    .body(
                        StringBody(session ->
                            String.format(
                                "{ \"title\": \"%s\", \"author\": \"Author %s\", \"keywords\": \"gatling,test\", \"resourceType\": \"BOOK\" }",
                                session.getString("resource_title"),
                                session.getString("resource_title")
                            )
                        )
                    )
                    .check(status().is(201))
                    .check(jsonPath("$.id").saveAs("resource_id"))
                    .check(responseTimeInMillis().lt(1000))
            )
        )
        .exec(session -> {
            System.out.println("[DEBUG] Created Resource ID: " + session.get("resource_id"));
            return session;
        })
        .exec(
            withAuth(
                http("Get Resource").get("/api/resources/${resource_id}").check(status().is(200)).check(responseTimeInMillis().lt(1000))
            )
        )
        .exec(
            withAuth(
                http("Update Resource")
                    .put("/api/resources/${resource_id}")
                    .body(
                        StringBody(session ->
                            String.format(
                                "{ \"id\": %s, \"title\": \"%s-updated\", \"author\": \"Author Updated\", \"keywords\": \"gatling,updated\", \"resourceType\": \"BOOK\" }",
                                session.get("resource_id"),
                                session.getString("resource_title")
                            )
                        )
                    )
                    .check(status().is(200))
                    .check(responseTimeInMillis().lt(1000))
            )
        )
        .exec(
            withAuth(
                http("Delete Resource")
                    .delete("/api/resources/${resource_id}")
                    .check(status().in(200, 204))
                    .check(responseTimeInMillis().lt(1000))
            )
        );

    // Authority CRUD scenario
    ChainBuilder authorityCrud = exec(authenticate)
        .exec(session -> session.set("authority_name", unique("ROLE_TEST")))
        .exec(
            withAuth(
                http("Create Authority")
                    .post("/api/authorities")
                    .body(StringBody(session -> String.format("{\"name\":\"%s\"}", session.getString("authority_name"))))
                    .check(status().is(201))
                    .check(responseTimeInMillis().lt(1000))
            )
        )
        .exec(
            withAuth(
                http("Get Authority")
                    .get("/api/authorities/${authority_name}")
                    .check(status().is(200))
                    .check(responseTimeInMillis().lt(1000))
            )
        )
        .exec(
            withAuth(
                http("Delete Authority")
                    .delete("/api/authorities/${authority_name}")
                    .check(status().in(200, 204))
                    .check(responseTimeInMillis().lt(1000))
            )
        );

    // User CRUD scenario
    ChainBuilder userCrud = exec(authenticate)
        .exec(session -> session.set("user_login", unique("user")))
        .exec(
            withAuth(
                http("Create User")
                    .post("/api/admin/users")
                    .body(
                        StringBody(session ->
                            String.format(
                                "{ \"login\": \"%s\", \"firstName\": \"Test\", \"lastName\": \"User\", \"email\": \"%s@example.com\", \"activated\": true, \"langKey\": \"en\", \"authorities\": [\"ROLE_USER\"] }",
                                session.getString("user_login"),
                                session.getString("user_login")
                            )
                        )
                    )
                    .check(status().is(201))
                    .check(responseTimeInMillis().lt(1000))
            )
        )
        .exec(
            withAuth(http("Get User").get("/api/admin/users/${user_login}").check(status().is(200)).check(responseTimeInMillis().lt(1000)))
        )
        .exec(
            withAuth(
                http("Update User")
                    .put("/api/admin/users/${user_login}")
                    .body(
                        StringBody(session ->
                            String.format(
                                "{ \"login\": \"%s\", \"firstName\": \"Updated\", \"lastName\": \"User\", \"email\": \"%s-updated@example.com\", \"activated\": true, \"langKey\": \"en\", \"authorities\": [\"ROLE_USER\"] }",
                                session.getString("user_login"),
                                session.getString("user_login")
                            )
                        )
                    )
                    .check(status().is(200))
                    .check(responseTimeInMillis().lt(1000))
            )
        )
        .exec(
            withAuth(
                http("Delete User")
                    .delete("/api/admin/users/${user_login}")
                    .check(status().in(200, 204))
                    .check(responseTimeInMillis().lt(1000))
            )
        );

    // Reservation CRUD scenario (requires a user and resource)
    ChainBuilder reservationCrud = exec(authenticate)
        .exec(session ->
            session
                .set("reservation_id", unique("res"))
                .set("reservation_date", nowIso())
                .set("start_time", nowIso())
                .set("end_time", nowIso())
                .set("user_id", 1)
                .set("user_login", "admin")
                .set("resource_id", 1)
        )
        .exec(
            withAuth(
                http("Create Reservation")
                    .post("/api/reservations")
                    .body(
                        StringBody(session ->
                            String.format(
                                "{ \"reservationDate\": \"%s\", \"startTime\": \"%s\", \"endTime\": \"%s\", \"reservationId\": \"%s\", \"user\": {\"id\": %s, \"login\": \"%s\"}, \"resource\": {\"id\": %s} }",
                                session.getString("reservation_date"),
                                session.getString("start_time"),
                                session.getString("end_time"),
                                session.getString("reservation_id"),
                                session.get("user_id"),
                                session.getString("user_login"),
                                session.get("resource_id")
                            )
                        )
                    )
                    .check(status().is(201))
                    .check(jsonPath("$.id").saveAs("reservation_entity_id"))
                    .check(responseTimeInMillis().lt(1000))
            )
        )
        .exec(session -> {
            System.out.println("[DEBUG] Created Reservation ID: " + session.get("reservation_entity_id"));
            return session;
        })
        .exec(
            withAuth(
                http("Get Reservation")
                    .get("/api/reservations/${reservation_entity_id}")
                    .check(status().is(200))
                    .check(responseTimeInMillis().lt(1000))
            )
        )
        .exec(
            withAuth(
                http("Update Reservation")
                    .put("/api/reservations/${reservation_entity_id}")
                    .body(
                        StringBody(session ->
                            String.format(
                                "{ \"id\": %s, \"reservationDate\": \"%s\", \"startTime\": \"%s\", \"endTime\": \"%s\", \"reservationId\": \"%s-updated\", \"user\": {\"id\": %s, \"login\": \"%s\"}, \"resource\": {\"id\": %s} }",
                                session.get("reservation_entity_id"),
                                session.getString("reservation_date"),
                                session.getString("start_time"),
                                session.getString("end_time"),
                                session.getString("reservation_id"),
                                session.get("user_id"),
                                session.getString("user_login"),
                                session.get("resource_id")
                            )
                        )
                    )
                    .check(status().is(200))
                    .check(responseTimeInMillis().lt(1000))
            )
        )
        .exec(
            withAuth(
                http("Delete Reservation")
                    .delete("/api/reservations/${reservation_entity_id}")
                    .check(status().in(200, 204))
                    .check(responseTimeInMillis().lt(1000))
            )
        );

    // Scenario: Authenticate, then run all CRUDs
    ScenarioBuilder scn = scenario("All API CRUD Simulation").exec(resourceCrud).exec(authorityCrud).exec(userCrud).exec(reservationCrud);

    {
        setUp(scn.injectOpen(rampUsers(10).during(Duration.ofSeconds(10)))).protocols(httpProtocol);
    }
}
