package com.mycompany.myapp.web.rest;

import static com.mycompany.myapp.domain.ReservationAsserts.*;
import static com.mycompany.myapp.web.rest.TestUtil.createUpdateProxyForBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mycompany.myapp.IntegrationTest;
import com.mycompany.myapp.domain.Reservation;
import com.mycompany.myapp.domain.Resource;
import com.mycompany.myapp.domain.User;
import com.mycompany.myapp.repository.ReservationRepository;
import com.mycompany.myapp.repository.UserRepository;
import com.mycompany.myapp.service.ReservationService;
import com.mycompany.myapp.service.dto.ReservationDTO;
import com.mycompany.myapp.service.mapper.ReservationMapper;
import jakarta.persistence.EntityManager;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration tests for the {@link ReservationResource} REST controller.
 */
@IntegrationTest
@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc
@WithMockUser
class ReservationResourceIT {

    private static final Instant DEFAULT_RESERVATION_DATE = Instant.now().plus(1, ChronoUnit.DAYS).truncatedTo(ChronoUnit.MILLIS);
    private static final Instant UPDATED_RESERVATION_DATE = Instant.now().plus(2, ChronoUnit.DAYS).truncatedTo(ChronoUnit.MILLIS);

    private static final Instant DEFAULT_START_TIME = Instant.now().plus(1, ChronoUnit.DAYS).truncatedTo(ChronoUnit.MILLIS);
    private static final Instant UPDATED_START_TIME = Instant.now().plus(2, ChronoUnit.DAYS).truncatedTo(ChronoUnit.MILLIS);

    private static final Instant DEFAULT_END_TIME = Instant.now()
        .plus(1, ChronoUnit.DAYS)
        .plus(2, ChronoUnit.HOURS)
        .truncatedTo(ChronoUnit.MILLIS);
    private static final Instant UPDATED_END_TIME = Instant.now()
        .plus(2, ChronoUnit.DAYS)
        .plus(2, ChronoUnit.HOURS)
        .truncatedTo(ChronoUnit.MILLIS);

    private static final String DEFAULT_RESERVATION_ID = "AAAAAAAAAA";
    private static final String UPDATED_RESERVATION_ID = "BBBBBBBBBB";

    private static final String ENTITY_API_URL = "/api/reservations";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private ObjectMapper om;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private UserRepository userRepository;

    @Mock
    private ReservationRepository reservationRepositoryMock;

    @Autowired
    private ReservationMapper reservationMapper;

    @Mock
    private ReservationService reservationServiceMock;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restReservationMockMvc;

    private Reservation reservation;

    private Reservation insertedReservation;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Reservation createEntity(EntityManager em) {
        Reservation reservation = new Reservation()
            .reservationDate(DEFAULT_RESERVATION_DATE)
            .startTime(DEFAULT_START_TIME)
            .endTime(DEFAULT_END_TIME)
            .reservationId(DEFAULT_RESERVATION_ID);
        // Add required entity
        User user = UserResourceIT.createEntity();
        em.persist(user);
        em.flush();
        reservation.setUser(user);
        // Add required entity
        Resource resource;
        if (TestUtil.findAll(em, Resource.class).isEmpty()) {
            resource = ResourceResourceIT.createEntity();
            em.persist(resource);
            em.flush();
        } else {
            resource = TestUtil.findAll(em, Resource.class).get(0);
        }
        reservation.setResource(resource);
        return reservation;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Reservation createUpdatedEntity(EntityManager em) {
        Reservation updatedReservation = new Reservation()
            .reservationDate(UPDATED_RESERVATION_DATE)
            .startTime(UPDATED_START_TIME)
            .endTime(UPDATED_END_TIME)
            .reservationId(UPDATED_RESERVATION_ID);
        // Add required entity
        User user = UserResourceIT.createEntity();
        em.persist(user);
        em.flush();
        updatedReservation.setUser(user);
        // Add required entity
        Resource resource;
        if (TestUtil.findAll(em, Resource.class).isEmpty()) {
            resource = ResourceResourceIT.createUpdatedEntity();
            em.persist(resource);
            em.flush();
        } else {
            resource = TestUtil.findAll(em, Resource.class).get(0);
        }
        updatedReservation.setResource(resource);
        return updatedReservation;
    }

    @BeforeEach
    void initTest() {
        reservation = createEntity(em);
    }

    @AfterEach
    void cleanup() {
        if (insertedReservation != null) {
            reservationRepository.delete(insertedReservation);
            insertedReservation = null;
        }
    }

    @Test
    @Transactional
    void createReservation() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the Reservation
        ReservationDTO reservationDTO = reservationMapper.toDto(reservation);
        var returnedReservationDTO = om.readValue(
            restReservationMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(reservationDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            ReservationDTO.class
        );

        // Validate the Reservation in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedReservation = reservationMapper.toEntity(returnedReservationDTO);
        assertReservationUpdatableFieldsEquals(returnedReservation, getPersistedReservation(returnedReservation));

        insertedReservation = returnedReservation;
    }

    @Test
    @Transactional
    void createReservationWithExistingId() throws Exception {
        // Create the Reservation with an existing ID
        reservation.setId(1L);
        ReservationDTO reservationDTO = reservationMapper.toDto(reservation);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restReservationMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(reservationDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Reservation in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkReservationDateIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        reservation.setReservationDate(null);

        // Create the Reservation, which fails.
        ReservationDTO reservationDTO = reservationMapper.toDto(reservation);

        restReservationMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(reservationDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkStartTimeIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        reservation.setStartTime(null);

        // Create the Reservation, which fails.
        ReservationDTO reservationDTO = reservationMapper.toDto(reservation);

        restReservationMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(reservationDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkEndTimeIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        reservation.setEndTime(null);

        // Create the Reservation, which fails.
        ReservationDTO reservationDTO = reservationMapper.toDto(reservation);

        restReservationMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(reservationDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkReservationIdIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        reservation.setReservationId(null);

        // Create the Reservation, which fails.
        ReservationDTO reservationDTO = reservationMapper.toDto(reservation);

        restReservationMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(reservationDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllReservations() throws Exception {
        // Initialize the database
        insertedReservation = reservationRepository.saveAndFlush(reservation);

        // Get all the reservationList
        restReservationMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(reservation.getId().intValue())))
            .andExpect(jsonPath("$.[*].reservationDate").value(hasItem(DEFAULT_RESERVATION_DATE.toString())))
            .andExpect(jsonPath("$.[*].startTime").value(hasItem(DEFAULT_START_TIME.toString())))
            .andExpect(jsonPath("$.[*].endTime").value(hasItem(DEFAULT_END_TIME.toString())))
            .andExpect(jsonPath("$.[*].reservationId").value(hasItem(DEFAULT_RESERVATION_ID)));
    }

    @SuppressWarnings({ "unchecked" })
    void getAllReservationsWithEagerRelationshipsIsEnabled() throws Exception {
        when(reservationServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restReservationMockMvc.perform(get(ENTITY_API_URL + "?eagerload=true")).andExpect(status().isOk());

        verify(reservationServiceMock, times(1)).findAllWithEagerRelationships(any());
    }

    @SuppressWarnings({ "unchecked" })
    void getAllReservationsWithEagerRelationshipsIsNotEnabled() throws Exception {
        when(reservationServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restReservationMockMvc.perform(get(ENTITY_API_URL + "?eagerload=false")).andExpect(status().isOk());
        verify(reservationRepositoryMock, times(1)).findAll(any(Pageable.class));
    }

    @Test
    @Transactional
    void getReservation() throws Exception {
        // Initialize the database
        insertedReservation = reservationRepository.saveAndFlush(reservation);

        // Get the reservation
        restReservationMockMvc
            .perform(get(ENTITY_API_URL_ID, reservation.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(reservation.getId().intValue()))
            .andExpect(jsonPath("$.reservationDate").value(DEFAULT_RESERVATION_DATE.toString()))
            .andExpect(jsonPath("$.startTime").value(DEFAULT_START_TIME.toString()))
            .andExpect(jsonPath("$.endTime").value(DEFAULT_END_TIME.toString()))
            .andExpect(jsonPath("$.reservationId").value(DEFAULT_RESERVATION_ID));
    }

    @Test
    @Transactional
    void getNonExistingReservation() throws Exception {
        // Get the reservation
        restReservationMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingReservation() throws Exception {
        // Initialize the database
        insertedReservation = reservationRepository.saveAndFlush(reservation);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the reservation
        Reservation updatedReservation = reservationRepository.findById(reservation.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedReservation are not directly saved in db
        em.detach(updatedReservation);
        updatedReservation
            .reservationDate(UPDATED_RESERVATION_DATE)
            .startTime(UPDATED_START_TIME)
            .endTime(UPDATED_END_TIME)
            .reservationId(UPDATED_RESERVATION_ID);
        ReservationDTO reservationDTO = reservationMapper.toDto(updatedReservation);

        restReservationMockMvc
            .perform(
                put(ENTITY_API_URL_ID, reservationDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(reservationDTO))
            )
            .andExpect(status().isOk());

        // Validate the Reservation in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedReservationToMatchAllProperties(updatedReservation);
    }

    @Test
    @Transactional
    void putNonExistingReservation() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        reservation.setId(longCount.incrementAndGet());

        // Create the Reservation
        ReservationDTO reservationDTO = reservationMapper.toDto(reservation);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restReservationMockMvc
            .perform(
                put(ENTITY_API_URL_ID, reservationDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(reservationDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Reservation in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchReservation() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        reservation.setId(longCount.incrementAndGet());

        // Create the Reservation
        ReservationDTO reservationDTO = reservationMapper.toDto(reservation);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restReservationMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(reservationDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Reservation in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamReservation() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        reservation.setId(longCount.incrementAndGet());

        // Create the Reservation
        ReservationDTO reservationDTO = reservationMapper.toDto(reservation);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restReservationMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(reservationDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Reservation in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateReservationWithPatch() throws Exception {
        // Initialize the database
        insertedReservation = reservationRepository.saveAndFlush(reservation);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the reservation using partial update
        Reservation partialUpdatedReservation = new Reservation();
        partialUpdatedReservation.setId(reservation.getId());

        partialUpdatedReservation.startTime(UPDATED_START_TIME).reservationId(UPDATED_RESERVATION_ID);

        restReservationMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedReservation.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedReservation))
            )
            .andExpect(status().isOk());

        // Validate the Reservation in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertReservationUpdatableFieldsEquals(
            createUpdateProxyForBean(partialUpdatedReservation, reservation),
            getPersistedReservation(reservation)
        );
    }

    @Test
    @Transactional
    void fullUpdateReservationWithPatch() throws Exception {
        // Initialize the database
        insertedReservation = reservationRepository.saveAndFlush(reservation);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the reservation using partial update
        Reservation partialUpdatedReservation = new Reservation();
        partialUpdatedReservation.setId(reservation.getId());

        partialUpdatedReservation
            .reservationDate(UPDATED_RESERVATION_DATE)
            .startTime(UPDATED_START_TIME)
            .endTime(UPDATED_END_TIME)
            .reservationId(UPDATED_RESERVATION_ID);

        restReservationMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedReservation.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedReservation))
            )
            .andExpect(status().isOk());

        // Validate the Reservation in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertReservationUpdatableFieldsEquals(partialUpdatedReservation, getPersistedReservation(partialUpdatedReservation));
    }

    @Test
    @Transactional
    void patchNonExistingReservation() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        reservation.setId(longCount.incrementAndGet());

        // Create the Reservation
        ReservationDTO reservationDTO = reservationMapper.toDto(reservation);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restReservationMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, reservationDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(reservationDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Reservation in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchReservation() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        reservation.setId(longCount.incrementAndGet());

        // Create the Reservation
        ReservationDTO reservationDTO = reservationMapper.toDto(reservation);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restReservationMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(reservationDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Reservation in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamReservation() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        reservation.setId(longCount.incrementAndGet());

        // Create the Reservation
        ReservationDTO reservationDTO = reservationMapper.toDto(reservation);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restReservationMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(reservationDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Reservation in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteReservation() throws Exception {
        // Initialize the database
        insertedReservation = reservationRepository.saveAndFlush(reservation);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the reservation
        restReservationMockMvc
            .perform(delete(ENTITY_API_URL_ID, reservation.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return reservationRepository.count();
    }

    protected void assertIncrementedRepositoryCount(long countBefore) {
        assertThat(countBefore + 1).isEqualTo(getRepositoryCount());
    }

    protected void assertDecrementedRepositoryCount(long countBefore) {
        assertThat(countBefore - 1).isEqualTo(getRepositoryCount());
    }

    protected void assertSameRepositoryCount(long countBefore) {
        assertThat(countBefore).isEqualTo(getRepositoryCount());
    }

    protected Reservation getPersistedReservation(Reservation reservation) {
        return reservationRepository.findById(reservation.getId()).orElseThrow();
    }

    protected void assertPersistedReservationToMatchAllProperties(Reservation expectedReservation) {
        assertReservationAllPropertiesEquals(expectedReservation, getPersistedReservation(expectedReservation));
    }

    protected void assertPersistedReservationToMatchUpdatableProperties(Reservation expectedReservation) {
        assertReservationAllUpdatablePropertiesEquals(expectedReservation, getPersistedReservation(expectedReservation));
    }
}
