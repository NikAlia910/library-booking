package com.mycompany.myapp.web.rest;

import com.mycompany.myapp.repository.ReservationRepository;
import com.mycompany.myapp.service.ReservationService;
import com.mycompany.myapp.service.dto.ReservationDTO;
import com.mycompany.myapp.web.rest.errors.BadRequestAlertException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.PaginationUtil;
import tech.jhipster.web.util.ResponseUtil;

/**
 * REST controller for managing {@link com.mycompany.myapp.domain.Reservation}.
 */
@RestController
@RequestMapping("/api/reservations")
public class ReservationResource {

    private static final Logger LOG = LoggerFactory.getLogger(ReservationResource.class);

    private static final String ENTITY_NAME = "reservation";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final ReservationService reservationService;

    private final ReservationRepository reservationRepository;

    public ReservationResource(ReservationService reservationService, ReservationRepository reservationRepository) {
        this.reservationService = reservationService;
        this.reservationRepository = reservationRepository;
    }

    /**
     * {@code POST  /reservations} : Create a new reservation.
     *
     * @param reservationDTO the reservationDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new reservationDTO, or with status {@code 400 (Bad Request)} if the reservation has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<ReservationDTO> createReservation(@Valid @RequestBody ReservationDTO reservationDTO) throws URISyntaxException {
        LOG.debug("REST request to save Reservation : {}", reservationDTO);
        if (reservationDTO.getId() != null) {
            throw new BadRequestAlertException("A new reservation cannot already have an ID", ENTITY_NAME, "idexists");
        }

        try {
            reservationDTO = reservationService.save(reservationDTO);
            return ResponseEntity.created(new URI("/api/reservations/" + reservationDTO.getId()))
                .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, reservationDTO.getId().toString()))
                .body(reservationDTO);
        } catch (IllegalArgumentException e) {
            LOG.warn("Business rule violation during reservation creation: {}", e.getMessage());
            throw new BadRequestAlertException(e.getMessage(), ENTITY_NAME, "businessrule");
        }
    }

    /**
     * {@code PUT  /reservations/:id} : Updates an existing reservation.
     *
     * @param id the id of the reservationDTO to save.
     * @param reservationDTO the reservationDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated reservationDTO,
     * or with status {@code 400 (Bad Request)} if the reservationDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the reservationDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public ResponseEntity<ReservationDTO> updateReservation(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody ReservationDTO reservationDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to update Reservation : {}, {}", id, reservationDTO);
        if (reservationDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, reservationDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!reservationRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        try {
            reservationDTO = reservationService.update(reservationDTO);
            return ResponseEntity.ok()
                .headers(HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, reservationDTO.getId().toString()))
                .body(reservationDTO);
        } catch (IllegalArgumentException e) {
            LOG.warn("Business rule violation during reservation update: {}", e.getMessage());
            throw new BadRequestAlertException(e.getMessage(), ENTITY_NAME, "businessrule");
        }
    }

    /**
     * {@code PATCH  /reservations/:id} : Partial updates given fields of an existing reservation, field will ignore if it is null
     *
     * @param id the id of the reservationDTO to save.
     * @param reservationDTO the reservationDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated reservationDTO,
     * or with status {@code 400 (Bad Request)} if the reservationDTO is not valid,
     * or with status {@code 404 (Not Found)} if the reservationDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the reservationDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<ReservationDTO> partialUpdateReservation(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody ReservationDTO reservationDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update Reservation partially : {}, {}", id, reservationDTO);
        if (reservationDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, reservationDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!reservationRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<ReservationDTO> result = reservationService.partialUpdate(reservationDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, reservationDTO.getId().toString())
        );
    }

    /**
     * {@code GET  /reservations} : get all the reservations.
     *
     * @param pageable the pagination information.
     * @param eagerload flag to eager load entities from relationships (This is applicable for many-to-many).
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of reservations in body.
     */
    @GetMapping("")
    public ResponseEntity<List<ReservationDTO>> getAllReservations(
        @org.springdoc.core.annotations.ParameterObject Pageable pageable,
        @RequestParam(name = "eagerload", required = false, defaultValue = "true") boolean eagerload
    ) {
        LOG.debug("REST request to get a page of Reservations");
        Page<ReservationDTO> page;
        if (eagerload) {
            page = reservationService.findAllWithEagerRelationships(pageable);
        } else {
            page = reservationService.findAll(pageable);
        }
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /reservations/resource/{resourceId}} : get reservations by resource.
     *
     * @param resourceId the resource ID.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of reservations in body.
     */
    @GetMapping("/resource/{resourceId}")
    public ResponseEntity<List<ReservationDTO>> getReservationsByResource(@PathVariable("resourceId") Long resourceId) {
        LOG.debug("REST request to get Reservations by resource ID: {}", resourceId);
        List<ReservationDTO> reservations = reservationService.findByResourceId(resourceId);
        return ResponseEntity.ok().body(reservations);
    }

    /**
     * {@code GET  /reservations/user/{userId}/active} : get active reservations for a user.
     *
     * @param userId the user ID.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of active reservations in body.
     */
    @GetMapping("/user/{userId}/active")
    public ResponseEntity<List<ReservationDTO>> getActiveReservationsByUser(@PathVariable("userId") Long userId) {
        LOG.debug("REST request to get active Reservations by user ID: {}", userId);
        List<ReservationDTO> reservations = reservationService.findActiveReservationsByUserId(userId);
        return ResponseEntity.ok().body(reservations);
    }

    /**
     * {@code GET  /reservations/availability/{resourceId}} : check resource availability.
     *
     * @param resourceId the resource ID.
     * @param startTime the start time (ISO format).
     * @param endTime the end time (ISO format).
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and availability status in body.
     */
    @GetMapping("/availability/{resourceId}")
    public ResponseEntity<Boolean> checkResourceAvailability(
        @PathVariable("resourceId") Long resourceId,
        @RequestParam String startTime,
        @RequestParam String endTime
    ) {
        LOG.debug("REST request to check availability for resource {} from {} to {}", resourceId, startTime, endTime);
        try {
            Instant start = Instant.parse(startTime);
            Instant end = Instant.parse(endTime);
            boolean isAvailable = reservationService.isResourceAvailable(resourceId, start, end);
            return ResponseEntity.ok().body(isAvailable);
        } catch (Exception e) {
            LOG.error("Error parsing time parameters", e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * {@code GET  /reservations/:id} : get the "id" reservation.
     *
     * @param id the id of the reservationDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the reservationDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ReservationDTO> getReservation(@PathVariable("id") Long id) {
        LOG.debug("REST request to get Reservation : {}", id);
        Optional<ReservationDTO> reservationDTO = reservationService.findOne(id);
        return ResponseUtil.wrapOrNotFound(reservationDTO);
    }

    /**
     * {@code DELETE  /reservations/:id} : delete the "id" reservation.
     *
     * @param id the id of the reservationDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReservation(@PathVariable("id") Long id) {
        LOG.debug("REST request to delete Reservation : {}", id);
        reservationService.delete(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, false, ENTITY_NAME, id.toString()))
            .build();
    }
}
