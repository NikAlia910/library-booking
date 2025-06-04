package com.mycompany.myapp.service;

import com.mycompany.myapp.domain.Reservation;
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
@Service
@Transactional
public class ReservationService {

    private static final Logger log = LoggerFactory.getLogger(ReservationService.class);

    private static final int MAX_RESERVATIONS_PER_USER = 5;
    private static final int MAX_ADVANCE_BOOKING_DAYS = 30;
    private static final int MIN_ADVANCE_BOOKING_HOURS = 1;
    private static final int MIN_RESERVATION_DURATION_HOURS = 1;
    private static final int MAX_RESERVATION_DURATION_HOURS = 8;

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
        log.debug("Request to save Reservation : {}", reservationDTO);

        // Validate business rules
        validateReservationBusinessRules(reservationDTO);

        Reservation reservation = reservationMapper.toEntity(reservationDTO);
        reservation = reservationRepository.save(reservation);
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
}
