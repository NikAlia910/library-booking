package gatling.simulations;

import static io.gatling.javaapi.core.CoreDsl.StringBody;
import static io.gatling.javaapi.core.CoreDsl.exec;
import static io.gatling.javaapi.core.CoreDsl.rampUsers;
import static io.gatling.javaapi.core.CoreDsl.scenario;
import static io.gatling.javaapi.http.HttpDsl.header;
import static io.gatling.javaapi.http.HttpDsl.headerRegex;
import static io.gatling.javaapi.http.HttpDsl.http;
import static io.gatling.javaapi.http.HttpDsl.status;

import io.gatling.javaapi.core.ChainBuilder;
import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Simulation;
import io.gatling.javaapi.http.HttpProtocolBuilder;
import java.time.Duration;
import java.util.Map;
import java.util.Optional;

/**
 * Performance test for the Resource entity.
 *
 * @see <a href="https://github.com/jhipster/generator-jhipster/tree/v8.10.0/generators/gatling#logging-tips">Logging tips</a>
 */
public class ResourceGatlingTest extends Simulation {

    private String baseURL = System.getProperty("baseURL", "http://localhost:8080");

    HttpProtocolBuilder httpConf = http
        .baseUrl(baseURL)
        .inferHtmlResources()
        .acceptHeader("*/*")
        .acceptEncodingHeader("gzip, deflate")
        .acceptLanguageHeader("fr,fr-fr;q=0.8,en-us;q=0.5,en;q=0.3")
        .connectionHeader("keep-alive")
        .userAgentHeader("Mozilla/5.0 (Macintosh; Intel Mac OS X 10.10; rv:33.0) Gecko/20100101 Firefox/33.0")
        .silentResources(); // Silence all resources like css or css so they don't clutter the results

    private Map<CharSequence, String> headersHttp = Map.of("Accept", "application/json");

    private Map<CharSequence, String> headersHttpAuthentication = Map.of("Content-Type", "application/json", "Accept", "application/json");

    // Static JWT token - replace this with a real token from your login
    private static final String STATIC_JWT_TOKEN =
        "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJhZG1pbiIsImV4cCI6MTc1MDE0OTcyNiwiYXV0aCI6IlJPTEVfQURNSU4gUk9MRV9VU0VSIiwiaWF0IjoxNzUwMDYzMzI2fQ.Gh05Qu2kK37Y3yuqbL7v0-B_ESPleVtlW6w80ikq7dAX1zA_RS6bfU2yrbCtRNVJSl2752mTfMuXv2eY5PCRsA";

    private Map<CharSequence, String> headersHttpWithAuth = Map.of(
        "Accept",
        "application/json",
        "Authorization",
        "Bearer " + STATIC_JWT_TOKEN
    );

    ChainBuilder scn = exec(http("First unauthenticated request").get("/api/account").headers(headersHttp).check(status().is(401)))
        .exitHereIfFailed()
        .pause(10)
        .exec(
            http("Authentication")
                .post("/api/authenticate")
                .headers(headersHttpAuthentication)
                .body(StringBody("{\"username\":\"admin\", \"password\":\"admin\"}"))
                .asJson()
                .check(status().is(200))
        )
        .exitHereIfFailed()
        .pause(2)
        .exec(http("Authenticated request").get("/api/account").headers(headersHttpWithAuth).check(status().is(200)))
        .pause(10)
        .repeat(2)
        .on(
            exec(http("Get all resources").get("/api/resources").headers(headersHttpWithAuth).check(status().is(200)))
                .pause(10)
                .exec(
                    http("Create new resource")
                        .post("/api/resources")
                        .headers(headersHttpAuthentication)
                        .header("Authorization", "Bearer " + STATIC_JWT_TOKEN)
                        .body(
                            StringBody(
                                "{" +
                                "\"title\":\"Performance Test Book\"," +
                                "\"author\":\"Test Author\"," +
                                "\"keywords\":\"performance,testing,book\"," +
                                "\"resourceType\":\"BOOK\"" +
                                "}"
                            )
                        )
                        .asJson()
                        .check(status().is(201))
                )
        );

    ScenarioBuilder users = scenario("Test the Resource entity").exec(scn);

    {
        setUp(
            users.injectOpen(rampUsers(Integer.getInteger("users", 100)).during(Duration.ofMinutes(Integer.getInteger("ramp", 1))))
        ).protocols(httpConf);
    }
}
