package com.mycompany.myapp.service;

import com.mycompany.myapp.domain.Reservation;
import com.mycompany.myapp.domain.enumeration.ResourceType;
import com.mycompany.myapp.repository.ReservationRepository;
import com.mycompany.myapp.service.dto.ReservationDTO;
import com.mycompany.myapp.service.mapper.ReservationMapper;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link com.mycompany.myapp.domain.Reservation}.
 */
/**
 * Service Implementation for managing {@link com.mycompany.myapp.domain.Reservation}.
 */
@Service
@Transactional
public class ReservationService {

    private static final Logger log = LoggerFactory.getLogger(ReservationService.class);

    private static final int MAX_RESERVATIONS_PER_USER = 5;
    private static final int MAX_ADVANCE_BOOKING_DAYS = 30;
    private static final int MIN_ADVANCE_BOOKING_HOURS = 1;
    private static final int MIN_RESERVATION_DURATION_HOURS = 1;
    private static final int MAX_RESERVATION_DURATION_HOURS = 2;
    private static final int MAX_MEETING_ROOM_HOURS_PER_24H = 2;

    private final ReservationRepository reservationRepository;

    private final ReservationMapper reservationMapper;

    private final DateTimeService dateTimeService;

    public ReservationService(
        ReservationRepository reservationRepository,
        ReservationMapper reservationMapper,
        DateTimeService dateTimeService
    ) {
        this.reservationRepository = reservationRepository;
        this.reservationMapper = reservationMapper;
        this.dateTimeService = dateTimeService;
    }

    /**
     * Save a reservation with business rule validation.
     *
     * @param reservationDTO the entity to save.
     * @return the persisted entity.
     */
    public ReservationDTO save(ReservationDTO reservationDTO) {
        log.error("🚨🚨🚨 SAVE METHOD CALLED - THIS SHOULD APPEAR IN LOGS 🚨🚨🚨");
        log.debug("Request to save Reservation : {}", reservationDTO);
        log.info("=== RESERVATION VALIDATION STARTING ===");
        log.info(
            "Reservation details: User={}, Resource={}, StartTime={}, EndTime={}, Duration={} hours",
            reservationDTO.getUser() != null ? reservationDTO.getUser().getId() : "null",
            reservationDTO.getResource() != null
                ? reservationDTO.getResource().getId() + " (" + reservationDTO.getResource().getResourceType() + ")"
                : "null",
            reservationDTO.getStartTime(),
            reservationDTO.getEndTime(),
            reservationDTO.getStartTime() != null && reservationDTO.getEndTime() != null
                ? Duration.between(reservationDTO.getStartTime(), reservationDTO.getEndTime()).toMinutes() / 60.0
                : "unknown"
        );

        // Validate business rules
        try {
            validateReservationBusinessRules(reservationDTO);
            log.info("=== RESERVATION VALIDATION PASSED ===");
        } catch (Exception e) {
            log.error("=== RESERVATION VALIDATION FAILED: {} ===", e.getMessage());
            throw e;
        }

        Reservation reservation = reservationMapper.toEntity(reservationDTO);
        reservation = reservationRepository.save(reservation);
        log.info("=== RESERVATION SAVED SUCCESSFULLY ===");
        return reservationMapper.toDto(reservation);
    }

    /**
     * Update a reservation.
     *
     * @param reservationDTO the entity to save.
     * @return the persisted entity.
     */
    public ReservationDTO update(ReservationDTO reservationDTO) {
        log.debug("Request to update Reservation : {}", reservationDTO);

        // Validate business rules for updates
        validateReservationBusinessRules(reservationDTO);

        Reservation reservation = reservationMapper.toEntity(reservationDTO);
        reservation = reservationRepository.save(reservation);
        return reservationMapper.toDto(reservation);
    }

    /**
     * Partially update a reservation.
     *
     * @param reservationDTO the entity to update partially.
     * @return the persisted entity.
     */
    public Optional<ReservationDTO> partialUpdate(ReservationDTO reservationDTO) {
        log.debug("Request to partially update Reservation : {}", reservationDTO);

        return reservationRepository
            .findById(reservationDTO.getId())
            .map(existingReservation -> {
                reservationMapper.partialUpdate(existingReservation, reservationDTO);

                return existingReservation;
            })
            .map(reservationRepository::save)
            .map(reservationMapper::toDto);
    }

    /**
     * Get all the reservations.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    public Page<ReservationDTO> findAll(Pageable pageable) {
        log.debug("Request to get all Reservations");
        return reservationRepository.findAll(pageable).map(reservationMapper::toDto);
    }

    /**
     * Get all the reservations with eager load of many-to-many relationships.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    public Page<ReservationDTO> findAllWithEagerRelationships(Pageable pageable) {
        return reservationRepository.findAllWithEagerRelationships(pageable).map(reservationMapper::toDto);
    }

    /**
     * Get one reservation by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<ReservationDTO> findOne(Long id) {
        log.debug("Request to get Reservation : {}", id);
        return reservationRepository.findOneWithEagerRelationships(id).map(reservationMapper::toDto);
    }

    /**
     * Delete the reservation by id.
     *
     * @param id the id of the entity.
     */
    public void delete(Long id) {
        log.debug("Request to delete Reservation : {}", id);
        reservationRepository.deleteById(id);
    }

    /**
     * Get reservations by resource ID.
     *
     * @param resourceId the resource ID.
     * @return the list of reservations.
     */
    @Transactional(readOnly = true)
    public List<ReservationDTO> findByResourceId(Long resourceId) {
        log.debug("Request to get Reservations by resource ID : {}", resourceId);
        return reservationRepository.findByResourceIdOrderByStartTime(resourceId).stream().map(reservationMapper::toDto).toList();
    }

    /**
     * Get active reservations for a user.
     *
     * @param userId the user ID.
     * @return the list of active reservations.
     */
    @Transactional(readOnly = true)
    public List<ReservationDTO> findActiveReservationsByUserId(Long userId) {
        log.debug("Request to get active Reservations by user ID : {}", userId);
        return reservationRepository
            .findActiveReservationsByUserId(userId, dateTimeService.getCurrentInstant())
            .stream()
            .map(reservationMapper::toDto)
            .toList();
    }

    /**
     * Check if a resource is available for a specific time slot.
     *
     * @param resourceId the resource ID.
     * @param startTime the start time.
     * @param endTime the end time.
     * @return true if available, false otherwise.
     */
    @Transactional(readOnly = true)
    public boolean isResourceAvailable(Long resourceId, Instant startTime, Instant endTime) {
        log.debug("Request to check availability for resource {} from {} to {}", resourceId, startTime, endTime);
        return reservationRepository.isResourceAvailable(resourceId, startTime, endTime);
    }

    /**
     * Validate business rules for reservations.
     *
     * @param reservationDTO the reservation to validate.
     */
    private void validateReservationBusinessRules(ReservationDTO reservationDTO) {
        // 1. Validate time constraints
        validateTimeConstraints(reservationDTO);

        // 2. Validate maximum reservations per user
        validateMaxReservationsPerUser(reservationDTO);

        // 3. Validate no overlapping reservations (check this before advance booking to get proper error messages)
        validateNoOverlappingReservations(reservationDTO);

        // 4. Validate advance booking window (including 1-hour advance rule)
        validateAdvanceBookingWindow(reservationDTO);

        // 5. Validate meeting room 2-hour limit within 24 hours
        validateMeetingRoomTimeLimit(reservationDTO);
    }

    private void validateTimeConstraints(ReservationDTO reservationDTO) {
        if (reservationDTO.getStartTime() == null || reservationDTO.getEndTime() == null) {
            throw new IllegalArgumentException("Start time and end time must be provided");
        }

        if (!reservationDTO.getEndTime().isAfter(reservationDTO.getStartTime())) {
            throw new IllegalArgumentException("End time must be after start time");
        }

        long durationHours = Duration.between(reservationDTO.getStartTime(), reservationDTO.getEndTime()).toHours();

        if (durationHours < MIN_RESERVATION_DURATION_HOURS) {
            throw new IllegalArgumentException("Minimum reservation duration is " + MIN_RESERVATION_DURATION_HOURS + " hour");
        }

        if (durationHours > MAX_RESERVATION_DURATION_HOURS) {
            throw new IllegalArgumentException("Maximum reservation duration is " + MAX_RESERVATION_DURATION_HOURS + " hours");
        }
    }

    private void validateMaxReservationsPerUser(ReservationDTO reservationDTO) {
        if (reservationDTO.getUser() != null && reservationDTO.getUser().getId() != null) {
            long activeReservationsCount = reservationRepository.countActiveReservationsByUserId(
                reservationDTO.getUser().getId(),
                dateTimeService.getCurrentInstant()
            );

            // Don't count the current reservation if it's an update
            if (reservationDTO.getId() != null) {
                // This is an update, so we don't increment the count
            } else if (activeReservationsCount >= MAX_RESERVATIONS_PER_USER) {
                throw new IllegalArgumentException(
                    "Maximum reservation limit of " + MAX_RESERVATIONS_PER_USER + " active reservations reached"
                );
            }
        }
    }

    private void validateAdvanceBookingWindow(ReservationDTO reservationDTO) {
        if (reservationDTO.getReservationDate() != null) {
            LocalDate reservationDate = reservationDTO.getReservationDate().atZone(ZoneOffset.UTC).toLocalDate();
            LocalDate currentDate = dateTimeService.getCurrentDate();

            long daysBetween = ChronoUnit.DAYS.between(currentDate, reservationDate);

            if (daysBetween > MAX_ADVANCE_BOOKING_DAYS) {
                throw new IllegalArgumentException(
                    "Reservations cannot be made more than " + MAX_ADVANCE_BOOKING_DAYS + " days in advance"
                );
            }
        }

        // Validate that reservations must be made at least 1 hour in advance
        if (reservationDTO.getStartTime() != null) {
            Instant currentTime = dateTimeService.getCurrentInstant();
            long hoursUntilReservation = Duration.between(currentTime, reservationDTO.getStartTime()).toHours();

            if (hoursUntilReservation < MIN_ADVANCE_BOOKING_HOURS) {
                throw new IllegalArgumentException("Reservations must be made at least " + MIN_ADVANCE_BOOKING_HOURS + " hour in advance");
            }
        }
    }

    private void validateNoOverlappingReservations(ReservationDTO reservationDTO) {
        if (reservationDTO.getResource() != null && reservationDTO.getResource().getId() != null) {
            List<Reservation> overlappingReservations = reservationRepository.findOverlappingReservations(
                reservationDTO.getResource().getId(),
                reservationDTO.getStartTime(),
                reservationDTO.getEndTime()
            );

            // Filter out the current reservation if it's an update
            if (reservationDTO.getId() != null) {
                overlappingReservations = overlappingReservations.stream().filter(r -> !r.getId().equals(reservationDTO.getId())).toList();
            }

            if (!overlappingReservations.isEmpty()) {
                throw new IllegalArgumentException("Selected time slot overlaps with an existing reservation");
            }
        }
    }

    private void validateMeetingRoomTimeLimit(ReservationDTO reservationDTO) {
        log.debug("Validating meeting room time limit for reservation: {}", reservationDTO);

        // Only apply this rule to meeting room reservations
        if (
            reservationDTO.getResource() != null &&
            reservationDTO.getResource().getResourceType() == ResourceType.MEETING_ROOM &&
            reservationDTO.getUser() != null &&
            reservationDTO.getUser().getId() != null &&
            reservationDTO.getStartTime() != null &&
            reservationDTO.getEndTime() != null
        ) {
            log.debug("Meeting room reservation detected for user {} - applying 2-hour limit check", reservationDTO.getUser().getId());

            // Calculate the duration of this reservation
            long newReservationMinutes = Duration.between(reservationDTO.getStartTime(), reservationDTO.getEndTime()).toMinutes();
            double newReservationHours = newReservationMinutes / 60.0;

            log.debug("New reservation duration: {} hours", newReservationHours);

            // First, check if this single reservation exceeds 2 hours
            if (newReservationHours > MAX_MEETING_ROOM_HOURS_PER_24H) {
                log.warn("Single meeting room reservation exceeds {} hours: {} hours", MAX_MEETING_ROOM_HOURS_PER_24H, newReservationHours);
                throw new IllegalArgumentException(
                    String.format(
                        "Meeting room cannot be reserved for more than %d hours within a 24-hour period. " +
                        "This reservation is %.1f hours.",
                        MAX_MEETING_ROOM_HOURS_PER_24H,
                        newReservationHours
                    )
                );
            }

            // Now check for conflicts with existing reservations within 24-hour windows
            // We need to check any 24-hour period that could include this reservation
            Instant checkStart = reservationDTO.getStartTime().minus(24, ChronoUnit.HOURS);
            Instant checkEnd = reservationDTO.getEndTime().plus(24, ChronoUnit.HOURS);

            log.debug("Checking for existing meeting room reservations from {} to {}", checkStart, checkEnd);

            // Find all meeting room reservations for this user in the extended period
            List<Reservation> existingReservations = reservationRepository.findMeetingRoomReservationsForUserInPeriod(
                reservationDTO.getUser().getId(),
                checkStart,
                checkEnd
            );

            log.debug("Found {} existing meeting room reservations in the extended period", existingReservations.size());

            // Filter out the current reservation if it's an update
            if (reservationDTO.getId() != null) {
                existingReservations = existingReservations.stream().filter(r -> !r.getId().equals(reservationDTO.getId())).toList();
                log.debug("After filtering out current reservation, {} reservations remain", existingReservations.size());
            }

            // Check if this reservation would violate the 2-hour limit in any 24-hour window
            // We'll check starting from 24 hours before the new reservation to 24 hours after
            Instant windowStart = reservationDTO.getStartTime().minus(24, ChronoUnit.HOURS);
            Instant finalCheck = reservationDTO.getEndTime();

            while (windowStart.isBefore(finalCheck) || windowStart.equals(finalCheck)) {
                Instant windowEnd = windowStart.plus(24, ChronoUnit.HOURS);

                // Calculate total meeting room time for this user in this 24-hour window
                long totalMinutesInWindow = 0;

                // Add time from existing reservations that overlap with this window
                for (Reservation existing : existingReservations) {
                    if (existing.getStartTime().isBefore(windowEnd) && existing.getEndTime().isAfter(windowStart)) {
                        Instant overlapStart = existing.getStartTime().isAfter(windowStart) ? existing.getStartTime() : windowStart;
                        Instant overlapEnd = existing.getEndTime().isBefore(windowEnd) ? existing.getEndTime() : windowEnd;
                        totalMinutesInWindow += Duration.between(overlapStart, overlapEnd).toMinutes();
                    }
                }

                // Add time from the new reservation that overlaps with this window
                if (reservationDTO.getStartTime().isBefore(windowEnd) && reservationDTO.getEndTime().isAfter(windowStart)) {
                    Instant overlapStart = reservationDTO.getStartTime().isAfter(windowStart) ? reservationDTO.getStartTime() : windowStart;
                    Instant overlapEnd = reservationDTO.getEndTime().isBefore(windowEnd) ? reservationDTO.getEndTime() : windowEnd;
                    totalMinutesInWindow += Duration.between(overlapStart, overlapEnd).toMinutes();
                }

                double totalHoursInWindow = totalMinutesInWindow / 60.0;

                log.debug("Window {} to {}: Total hours = {}", windowStart, windowEnd, totalHoursInWindow);

                if (totalHoursInWindow > MAX_MEETING_ROOM_HOURS_PER_24H) {
                    log.warn(
                        "Meeting room time limit exceeded in window {} to {}! Total hours: {}",
                        windowStart,
                        windowEnd,
                        totalHoursInWindow
                    );

                    throw new IllegalArgumentException(
                        String.format(
                            "Meeting room cannot be reserved for more than %d hours within a 24-hour period. " +
                            "This reservation would result in %.1f hours of meeting room usage between %s and %s.",
                            MAX_MEETING_ROOM_HOURS_PER_24H,
                            totalHoursInWindow,
                            windowStart.toString(),
                            windowEnd.toString()
                        )
                    );
                }

                // Move window forward by 1 hour for the next check
                windowStart = windowStart.plus(1, ChronoUnit.HOURS);
            }

            log.debug("Meeting room time limit validation passed");
        } else {
            log.debug("Skipping meeting room validation - not a meeting room or missing required data");
            if (reservationDTO.getResource() != null) {
                log.debug("Resource type: {}", reservationDTO.getResource().getResourceType());
            }
        }
    }
}
