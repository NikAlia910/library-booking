package com.mycompany.myapp.cucumber.stepdefs;

import static org.assertj.core.api.Assertions.assertThat;

import com.mycompany.myapp.domain.Reservation;
import com.mycompany.myapp.domain.Resource;
import com.mycompany.myapp.domain.User;
import com.mycompany.myapp.domain.enumeration.ResourceType;
import com.mycompany.myapp.repository.ReservationRepository;
import com.mycompany.myapp.repository.ResourceRepository;
import com.mycompany.myapp.repository.UserRepository;
import com.mycompany.myapp.service.ReservationService;
import com.mycompany.myapp.service.ResourceService;
import com.mycompany.myapp.service.dto.ReservationDTO;
import com.mycompany.myapp.service.dto.ResourceDTO;
import com.mycompany.myapp.service.dto.UserDTO;
import com.mycompany.myapp.service.mapper.ReservationMapper;
import com.mycompany.myapp.service.mapper.ResourceMapper;
import com.mycompany.myapp.service.mapper.UserMapper;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.Before;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;

public class ReservationBusinessRulesStepDefs extends StepDefs {

    @Autowired
    private ResourceService resourceService;

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private ResourceRepository resourceRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ResourceMapper resourceMapper;

    @Autowired
    private ReservationMapper reservationMapper;

    @Autowired
    private UserMapper userMapper;

    private User currentPatron;
    private LocalDate currentDate;
    private boolean lastReservationAccepted;
    private String lastErrorMessage;
    private List<ReservationDTO> multipleReservations;

    @Before
    public void setup() {
        // Clean up test data before each scenario
        reservationRepository.deleteAll();
        resourceRepository.deleteAll();
        // Clean up test users to avoid constraint violations
        userRepository
            .findAll()
            .stream()
            .filter(user -> user.getEmail() != null && user.getEmail().contains("@example.com"))
            .forEach(user -> userRepository.delete(user));

        // Reset state
        currentPatron = null;
        currentDate = LocalDate.now();
        lastReservationAccepted = false;
        lastErrorMessage = null;
        multipleReservations = new ArrayList<>();
    }

    @Given("the library system is operational")
    public void the_library_system_is_operational() {
        // Verify basic system components are available
        assertThat(resourceService).isNotNull();
        assertThat(reservationService).isNotNull();
        assertThat(resourceRepository).isNotNull();
        assertThat(reservationRepository).isNotNull();
    }

    @Given("the following resources are available:")
    public void the_following_resources_are_available(DataTable dataTable) {
        List<Map<String, String>> resources = dataTable.asMaps(String.class, String.class);

        for (Map<String, String> resourceData : resources) {
            ResourceDTO resourceDTO = new ResourceDTO();
            resourceDTO.setTitle(resourceData.get("title"));
            resourceDTO.setResourceType(ResourceType.valueOf(resourceData.get("resourceType")));

            String author = resourceData.get("author");
            if (!"N/A".equals(author)) {
                resourceDTO.setAuthor(author);
            }

            // Call actual service to create resource
            resourceService.save(resourceDTO);
        }
    }

    @Given("a patron {string} exists")
    public void a_patron_exists(String email) {
        String uniqueId = RandomStringUtils.insecure().nextAlphabetic(5);
        User patron = new User();
        patron.setLogin(email.split("@")[0] + uniqueId);
        patron.setPassword(RandomStringUtils.insecure().nextAlphanumeric(60));
        patron.setEmail(uniqueId + email); // Make email unique by prepending unique ID
        patron.setActivated(true);
        patron.setFirstName("Test");
        patron.setLastName("Patron");

        currentPatron = userRepository.save(patron);
    }

    @Given("the patron has exactly {int} active reservations")
    public void the_patron_has_exactly_active_reservations(int count) {
        // Create the specified number of active reservations for the current patron
        for (int i = 0; i < count; i++) {
            Resource testResource = createTestResource("Test Resource " + i);
            Reservation reservation = createTestReservation(testResource, currentPatron);
            reservationRepository.save(reservation);
        }

        // Verify the count using actual repository
        // Note: Business logic should implement a method to count active reservations per user
        List<Reservation> allReservations = reservationRepository.findAll();
        long userReservationCount = allReservations.stream().filter(r -> r.getUser().getId().equals(currentPatron.getId())).count();

        assertThat(userReservationCount).isEqualTo(count);
    }

    @Given("today is {string}")
    public void today_is(String date) {
        currentDate = LocalDate.parse(date);
        // This would need to be implemented in business logic to support date mocking
    }

    @Given("{string} is reserved from {string} to {string}")
    public void resource_is_reserved_from_to(String resourceTitle, String startDateTime, String endDateTime) {
        Resource resource = findResourceByTitle(resourceTitle);

        Instant startTime = parseDateTime(startDateTime);
        Instant endTime = parseDateTime(endDateTime);

        User existingUser = createAnotherUser();

        Reservation existingReservation = createTestReservation(resource, existingUser);
        existingReservation.setStartTime(startTime);
        existingReservation.setEndTime(endTime);
        existingReservation.setReservationDate(startTime);

        reservationRepository.save(existingReservation);
    }

    @When("the patron attempts to make a 6th reservation for {string}")
    public void the_patron_attempts_to_make_6th_reservation_for(String resourceTitle) {
        attemptReservation(resourceTitle, "2024-02-15 10:00", "2024-02-15 12:00");
    }

    @When("the patron attempts to make a reservation for {string}")
    public void the_patron_attempts_to_make_reservation_for(String resourceTitle) {
        attemptReservation(resourceTitle, "2024-02-15 10:00", "2024-02-15 12:00");
    }

    @When("the patron attempts to reserve {string} for {string}")
    public void the_patron_attempts_to_reserve_for_date(String resourceTitle, String date) {
        attemptReservation(resourceTitle, date + " 10:00", date + " 12:00");
    }

    @When("the patron attempts to reserve {string} from {string} to {string}")
    public void the_patron_attempts_to_reserve_from_to(String resourceTitle, String startDateTime, String endDateTime) {
        attemptReservation(resourceTitle, startDateTime, endDateTime);
    }

    @When("the patron attempts to reserve {string} with end time before start time")
    public void the_patron_attempts_to_reserve_with_invalid_times(String resourceTitle) {
        // Intentionally create invalid reservation with end time before start time
        attemptReservation(resourceTitle, "2024-02-15 12:00", "2024-02-15 10:00");
    }

    @When("the patron makes multiple reservations")
    public void the_patron_makes_multiple_reservations() {
        // Create multiple reservations to test unique ID generation
        for (int i = 0; i < 3; i++) {
            try {
                Resource resource = createTestResource("Multi Resource " + i);

                ReservationDTO reservationDTO = new ReservationDTO();
                reservationDTO.setReservationDate(Instant.now().plus(i, ChronoUnit.DAYS));
                reservationDTO.setStartTime(Instant.now().plus(i, ChronoUnit.DAYS));
                reservationDTO.setEndTime(Instant.now().plus(i, ChronoUnit.DAYS).plus(2, ChronoUnit.HOURS));
                reservationDTO.setReservationId(UUID.randomUUID().toString());

                UserDTO userDTO = userMapper.userToUserDTO(currentPatron);
                ResourceDTO resourceDTO = resourceMapper.toDto(resource);
                reservationDTO.setUser(userDTO);
                reservationDTO.setResource(resourceDTO);

                // Call actual service
                ReservationDTO result = reservationService.save(reservationDTO);
                multipleReservations.add(result);
            } catch (Exception e) {
                // Continue with other reservations even if one fails
            }
        }
    }

    @Then("the reservation should be rejected")
    public void the_reservation_should_be_rejected() {
        assertThat(lastReservationAccepted).isFalse();
    }

    @Then("the reservation should be accepted")
    public void the_reservation_should_be_accepted() {
        assertThat(lastReservationAccepted).isTrue();
    }

    @Then("the system should return error {string}")
    public void the_system_should_return_error(String expectedError) {
        assertThat(lastErrorMessage).contains(expectedError);
    }

    @Then("the patron should now have {int} active reservations")
    public void the_patron_should_now_have_active_reservations(int expectedCount) {
        // Verify the updated count using actual repository
        List<Reservation> allReservations = reservationRepository.findAll();
        long userReservationCount = allReservations.stream().filter(r -> r.getUser().getId().equals(currentPatron.getId())).count();

        assertThat(userReservationCount).isEqualTo(expectedCount);
    }

    @Then("each reservation should have a unique reservation ID")
    public void each_reservation_should_have_unique_reservation_id() {
        assertThat(multipleReservations).isNotEmpty();

        Set<String> reservationIds = new HashSet<>();
        for (ReservationDTO reservation : multipleReservations) {
            assertThat(reservation.getReservationId()).isNotNull();
            reservationIds.add(reservation.getReservationId());
        }

        // All reservation IDs should be unique
        assertThat(reservationIds).hasSize(multipleReservations.size());
    }

    @Then("no two reservations should share the same ID")
    public void no_two_reservations_should_share_same_id() {
        // This is verified in the previous step
        each_reservation_should_have_unique_reservation_id();
    }

    // Helper methods
    private void attemptReservation(String resourceTitle, String startDateTime, String endDateTime) {
        try {
            Resource resource = findResourceByTitle(resourceTitle);

            Instant startTime = parseDateTime(startDateTime);
            Instant endTime = parseDateTime(endDateTime);

            ReservationDTO reservationDTO = new ReservationDTO();
            reservationDTO.setReservationDate(startTime);
            reservationDTO.setStartTime(startTime);
            reservationDTO.setEndTime(endTime);
            reservationDTO.setReservationId(UUID.randomUUID().toString());

            UserDTO userDTO = userMapper.userToUserDTO(currentPatron);
            ResourceDTO resourceDTO = resourceMapper.toDto(resource);
            reservationDTO.setUser(userDTO);
            reservationDTO.setResource(resourceDTO);

            // Call actual service - this will fail until business logic is implemented
            reservationService.save(reservationDTO);
            lastReservationAccepted = true;
            lastErrorMessage = null;
        } catch (Exception e) {
            lastReservationAccepted = false;
            lastErrorMessage = e.getMessage();
        }
    }

    private Resource findResourceByTitle(String title) {
        return resourceRepository
            .findAll()
            .stream()
            .filter(r -> r.getTitle().equals(title))
            .findFirst()
            .orElseThrow(() -> new RuntimeException("Resource not found: " + title));
    }

    private Resource createTestResource(String title) {
        Resource resource = new Resource();
        resource.setTitle(title);
        resource.setResourceType(ResourceType.BOOK);
        resource.setAuthor("Test Author");
        resource.setKeywords("test");
        return resourceRepository.save(resource);
    }

    private Reservation createTestReservation(Resource resource, User user) {
        Reservation reservation = new Reservation();
        reservation.setResource(resource);
        reservation.setUser(user);
        reservation.setReservationId(UUID.randomUUID().toString());
        reservation.setReservationDate(Instant.now());
        reservation.setStartTime(Instant.now());
        reservation.setEndTime(Instant.now().plusSeconds(3600)); // 1 hour later
        return reservation;
    }

    private User createAnotherUser() {
        String uniqueId = RandomStringUtils.insecure().nextAlphabetic(8);
        User user = new User();
        user.setLogin("another" + uniqueId);
        user.setPassword(RandomStringUtils.insecure().nextAlphanumeric(60));
        user.setActivated(true);
        user.setEmail("another" + uniqueId + "@example.com");
        user.setFirstName("Another");
        user.setLastName("User");
        return userRepository.save(user);
    }

    private Instant parseDateTime(String dateTimeString) {
        // Parse date-time strings like "2024-02-15 10:00"
        LocalDateTime dateTime = LocalDateTime.parse(dateTimeString, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        return dateTime.toInstant(ZoneOffset.UTC);
    }
}
