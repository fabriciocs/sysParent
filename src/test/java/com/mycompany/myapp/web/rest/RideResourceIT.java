package com.mycompany.myapp.web.rest;

import static com.mycompany.myapp.domain.RideAsserts.*;
import static com.mycompany.myapp.web.rest.TestUtil.createUpdateProxyForBean;
import static com.mycompany.myapp.web.rest.TestUtil.sameInstant;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mycompany.myapp.IntegrationTest;
import com.mycompany.myapp.domain.Child;
import com.mycompany.myapp.domain.Driver;
import com.mycompany.myapp.domain.Ride;
import com.mycompany.myapp.domain.enumeration.RideStatus;
import com.mycompany.myapp.repository.RideRepository;
import com.mycompany.myapp.repository.search.RideSearchRepository;
import com.mycompany.myapp.service.RideService;
import com.mycompany.myapp.service.dto.RideDTO;
import com.mycompany.myapp.service.mapper.RideMapper;
import jakarta.persistence.EntityManager;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import org.assertj.core.util.IterableUtil;
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
import org.springframework.data.util.Streamable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration tests for the {@link RideResource} REST controller.
 */
@IntegrationTest
@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc
@WithMockUser
class RideResourceIT {

    private static final ZonedDateTime DEFAULT_SCHEDULED_TIME = ZonedDateTime.ofInstant(Instant.ofEpochMilli(0L), ZoneOffset.UTC);
    private static final ZonedDateTime UPDATED_SCHEDULED_TIME = ZonedDateTime.now(ZoneId.systemDefault()).withNano(0);

    private static final RideStatus DEFAULT_STATUS = RideStatus.SCHEDULED;
    private static final RideStatus UPDATED_STATUS = RideStatus.IN_PROGRESS;

    private static final String DEFAULT_PICKUP_ADDRESS = "AAAAAAAAAA";
    private static final String UPDATED_PICKUP_ADDRESS = "BBBBBBBBBB";

    private static final String DEFAULT_DROPOFF_ADDRESS = "AAAAAAAAAA";
    private static final String UPDATED_DROPOFF_ADDRESS = "BBBBBBBBBB";

    private static final String ENTITY_API_URL = "/api/rides";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";
    private static final String ENTITY_SEARCH_API_URL = "/api/rides/_search";

    private static Random random = new Random();
    private static AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private ObjectMapper om;

    @Autowired
    private RideRepository rideRepository;

    @Mock
    private RideRepository rideRepositoryMock;

    @Autowired
    private RideMapper rideMapper;

    @Mock
    private RideService rideServiceMock;

    @Autowired
    private RideSearchRepository rideSearchRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restRideMockMvc;

    private Ride ride;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Ride createEntity(EntityManager em) {
        Ride ride = new Ride()
            .scheduledTime(DEFAULT_SCHEDULED_TIME)
            .status(DEFAULT_STATUS)
            .pickupAddress(DEFAULT_PICKUP_ADDRESS)
            .dropoffAddress(DEFAULT_DROPOFF_ADDRESS);
        // Add required entity
        Child child;
        if (TestUtil.findAll(em, Child.class).isEmpty()) {
            child = ChildResourceIT.createEntity(em);
            em.persist(child);
            em.flush();
        } else {
            child = TestUtil.findAll(em, Child.class).get(0);
        }
        ride.setChild(child);
        // Add required entity
        Driver driver;
        if (TestUtil.findAll(em, Driver.class).isEmpty()) {
            driver = DriverResourceIT.createEntity(em);
            em.persist(driver);
            em.flush();
        } else {
            driver = TestUtil.findAll(em, Driver.class).get(0);
        }
        ride.setDriver(driver);
        return ride;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Ride createUpdatedEntity(EntityManager em) {
        Ride ride = new Ride()
            .scheduledTime(UPDATED_SCHEDULED_TIME)
            .status(UPDATED_STATUS)
            .pickupAddress(UPDATED_PICKUP_ADDRESS)
            .dropoffAddress(UPDATED_DROPOFF_ADDRESS);
        // Add required entity
        Child child;
        if (TestUtil.findAll(em, Child.class).isEmpty()) {
            child = ChildResourceIT.createUpdatedEntity(em);
            em.persist(child);
            em.flush();
        } else {
            child = TestUtil.findAll(em, Child.class).get(0);
        }
        ride.setChild(child);
        // Add required entity
        Driver driver;
        if (TestUtil.findAll(em, Driver.class).isEmpty()) {
            driver = DriverResourceIT.createUpdatedEntity(em);
            em.persist(driver);
            em.flush();
        } else {
            driver = TestUtil.findAll(em, Driver.class).get(0);
        }
        ride.setDriver(driver);
        return ride;
    }

    @AfterEach
    public void cleanupElasticSearchRepository() {
        rideSearchRepository.deleteAll();
        assertThat(rideSearchRepository.count()).isEqualTo(0);
    }

    @BeforeEach
    public void initTest() {
        ride = createEntity(em);
    }

    @Test
    @Transactional
    void createRide() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(rideSearchRepository.findAll());
        // Create the Ride
        RideDTO rideDTO = rideMapper.toDto(ride);
        var returnedRideDTO = om.readValue(
            restRideMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(rideDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            RideDTO.class
        );

        // Validate the Ride in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedRide = rideMapper.toEntity(returnedRideDTO);
        assertRideUpdatableFieldsEquals(returnedRide, getPersistedRide(returnedRide));

        await()
            .atMost(5, TimeUnit.SECONDS)
            .untilAsserted(() -> {
                int searchDatabaseSizeAfter = IterableUtil.sizeOf(rideSearchRepository.findAll());
                assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore + 1);
            });
    }

    @Test
    @Transactional
    void createRideWithExistingId() throws Exception {
        // Create the Ride with an existing ID
        ride.setId(1L);
        RideDTO rideDTO = rideMapper.toDto(ride);

        long databaseSizeBeforeCreate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(rideSearchRepository.findAll());

        // An entity with an existing ID cannot be created, so this API call must fail
        restRideMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(rideDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Ride in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(rideSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void checkScheduledTimeIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(rideSearchRepository.findAll());
        // set the field null
        ride.setScheduledTime(null);

        // Create the Ride, which fails.
        RideDTO rideDTO = rideMapper.toDto(ride);

        restRideMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(rideDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);

        int searchDatabaseSizeAfter = IterableUtil.sizeOf(rideSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void checkStatusIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(rideSearchRepository.findAll());
        // set the field null
        ride.setStatus(null);

        // Create the Ride, which fails.
        RideDTO rideDTO = rideMapper.toDto(ride);

        restRideMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(rideDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);

        int searchDatabaseSizeAfter = IterableUtil.sizeOf(rideSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void checkPickupAddressIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(rideSearchRepository.findAll());
        // set the field null
        ride.setPickupAddress(null);

        // Create the Ride, which fails.
        RideDTO rideDTO = rideMapper.toDto(ride);

        restRideMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(rideDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);

        int searchDatabaseSizeAfter = IterableUtil.sizeOf(rideSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void checkDropoffAddressIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(rideSearchRepository.findAll());
        // set the field null
        ride.setDropoffAddress(null);

        // Create the Ride, which fails.
        RideDTO rideDTO = rideMapper.toDto(ride);

        restRideMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(rideDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);

        int searchDatabaseSizeAfter = IterableUtil.sizeOf(rideSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void getAllRides() throws Exception {
        // Initialize the database
        rideRepository.saveAndFlush(ride);

        // Get all the rideList
        restRideMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(ride.getId().intValue())))
            .andExpect(jsonPath("$.[*].scheduledTime").value(hasItem(sameInstant(DEFAULT_SCHEDULED_TIME))))
            .andExpect(jsonPath("$.[*].status").value(hasItem(DEFAULT_STATUS.toString())))
            .andExpect(jsonPath("$.[*].pickupAddress").value(hasItem(DEFAULT_PICKUP_ADDRESS)))
            .andExpect(jsonPath("$.[*].dropoffAddress").value(hasItem(DEFAULT_DROPOFF_ADDRESS)));
    }

    @SuppressWarnings({ "unchecked" })
    void getAllRidesWithEagerRelationshipsIsEnabled() throws Exception {
        when(rideServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restRideMockMvc.perform(get(ENTITY_API_URL + "?eagerload=true")).andExpect(status().isOk());

        verify(rideServiceMock, times(1)).findAllWithEagerRelationships(any());
    }

    @SuppressWarnings({ "unchecked" })
    void getAllRidesWithEagerRelationshipsIsNotEnabled() throws Exception {
        when(rideServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restRideMockMvc.perform(get(ENTITY_API_URL + "?eagerload=false")).andExpect(status().isOk());
        verify(rideRepositoryMock, times(1)).findAll(any(Pageable.class));
    }

    @Test
    @Transactional
    void getRide() throws Exception {
        // Initialize the database
        rideRepository.saveAndFlush(ride);

        // Get the ride
        restRideMockMvc
            .perform(get(ENTITY_API_URL_ID, ride.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(ride.getId().intValue()))
            .andExpect(jsonPath("$.scheduledTime").value(sameInstant(DEFAULT_SCHEDULED_TIME)))
            .andExpect(jsonPath("$.status").value(DEFAULT_STATUS.toString()))
            .andExpect(jsonPath("$.pickupAddress").value(DEFAULT_PICKUP_ADDRESS))
            .andExpect(jsonPath("$.dropoffAddress").value(DEFAULT_DROPOFF_ADDRESS));
    }

    @Test
    @Transactional
    void getNonExistingRide() throws Exception {
        // Get the ride
        restRideMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingRide() throws Exception {
        // Initialize the database
        rideRepository.saveAndFlush(ride);

        long databaseSizeBeforeUpdate = getRepositoryCount();
        rideSearchRepository.save(ride);
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(rideSearchRepository.findAll());

        // Update the ride
        Ride updatedRide = rideRepository.findById(ride.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedRide are not directly saved in db
        em.detach(updatedRide);
        updatedRide
            .scheduledTime(UPDATED_SCHEDULED_TIME)
            .status(UPDATED_STATUS)
            .pickupAddress(UPDATED_PICKUP_ADDRESS)
            .dropoffAddress(UPDATED_DROPOFF_ADDRESS);
        RideDTO rideDTO = rideMapper.toDto(updatedRide);

        restRideMockMvc
            .perform(put(ENTITY_API_URL_ID, rideDTO.getId()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(rideDTO)))
            .andExpect(status().isOk());

        // Validate the Ride in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedRideToMatchAllProperties(updatedRide);

        await()
            .atMost(5, TimeUnit.SECONDS)
            .untilAsserted(() -> {
                int searchDatabaseSizeAfter = IterableUtil.sizeOf(rideSearchRepository.findAll());
                assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
                List<Ride> rideSearchList = Streamable.of(rideSearchRepository.findAll()).toList();
                Ride testRideSearch = rideSearchList.get(searchDatabaseSizeAfter - 1);

                assertRideAllPropertiesEquals(testRideSearch, updatedRide);
            });
    }

    @Test
    @Transactional
    void putNonExistingRide() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(rideSearchRepository.findAll());
        ride.setId(longCount.incrementAndGet());

        // Create the Ride
        RideDTO rideDTO = rideMapper.toDto(ride);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restRideMockMvc
            .perform(put(ENTITY_API_URL_ID, rideDTO.getId()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(rideDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Ride in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(rideSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void putWithIdMismatchRide() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(rideSearchRepository.findAll());
        ride.setId(longCount.incrementAndGet());

        // Create the Ride
        RideDTO rideDTO = rideMapper.toDto(ride);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restRideMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(rideDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Ride in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(rideSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamRide() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(rideSearchRepository.findAll());
        ride.setId(longCount.incrementAndGet());

        // Create the Ride
        RideDTO rideDTO = rideMapper.toDto(ride);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restRideMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(rideDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Ride in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(rideSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void partialUpdateRideWithPatch() throws Exception {
        // Initialize the database
        rideRepository.saveAndFlush(ride);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the ride using partial update
        Ride partialUpdatedRide = new Ride();
        partialUpdatedRide.setId(ride.getId());

        partialUpdatedRide.dropoffAddress(UPDATED_DROPOFF_ADDRESS);

        restRideMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedRide.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedRide))
            )
            .andExpect(status().isOk());

        // Validate the Ride in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertRideUpdatableFieldsEquals(createUpdateProxyForBean(partialUpdatedRide, ride), getPersistedRide(ride));
    }

    @Test
    @Transactional
    void fullUpdateRideWithPatch() throws Exception {
        // Initialize the database
        rideRepository.saveAndFlush(ride);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the ride using partial update
        Ride partialUpdatedRide = new Ride();
        partialUpdatedRide.setId(ride.getId());

        partialUpdatedRide
            .scheduledTime(UPDATED_SCHEDULED_TIME)
            .status(UPDATED_STATUS)
            .pickupAddress(UPDATED_PICKUP_ADDRESS)
            .dropoffAddress(UPDATED_DROPOFF_ADDRESS);

        restRideMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedRide.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedRide))
            )
            .andExpect(status().isOk());

        // Validate the Ride in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertRideUpdatableFieldsEquals(partialUpdatedRide, getPersistedRide(partialUpdatedRide));
    }

    @Test
    @Transactional
    void patchNonExistingRide() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(rideSearchRepository.findAll());
        ride.setId(longCount.incrementAndGet());

        // Create the Ride
        RideDTO rideDTO = rideMapper.toDto(ride);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restRideMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, rideDTO.getId()).contentType("application/merge-patch+json").content(om.writeValueAsBytes(rideDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Ride in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(rideSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void patchWithIdMismatchRide() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(rideSearchRepository.findAll());
        ride.setId(longCount.incrementAndGet());

        // Create the Ride
        RideDTO rideDTO = rideMapper.toDto(ride);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restRideMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(rideDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Ride in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(rideSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamRide() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(rideSearchRepository.findAll());
        ride.setId(longCount.incrementAndGet());

        // Create the Ride
        RideDTO rideDTO = rideMapper.toDto(ride);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restRideMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(rideDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Ride in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(rideSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void deleteRide() throws Exception {
        // Initialize the database
        rideRepository.saveAndFlush(ride);
        rideRepository.save(ride);
        rideSearchRepository.save(ride);

        long databaseSizeBeforeDelete = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(rideSearchRepository.findAll());
        assertThat(searchDatabaseSizeBefore).isEqualTo(databaseSizeBeforeDelete);

        // Delete the ride
        restRideMockMvc
            .perform(delete(ENTITY_API_URL_ID, ride.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(rideSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore - 1);
    }

    @Test
    @Transactional
    void searchRide() throws Exception {
        // Initialize the database
        ride = rideRepository.saveAndFlush(ride);
        rideSearchRepository.save(ride);

        // Search the ride
        restRideMockMvc
            .perform(get(ENTITY_SEARCH_API_URL + "?query=id:" + ride.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(ride.getId().intValue())))
            .andExpect(jsonPath("$.[*].scheduledTime").value(hasItem(sameInstant(DEFAULT_SCHEDULED_TIME))))
            .andExpect(jsonPath("$.[*].status").value(hasItem(DEFAULT_STATUS.toString())))
            .andExpect(jsonPath("$.[*].pickupAddress").value(hasItem(DEFAULT_PICKUP_ADDRESS)))
            .andExpect(jsonPath("$.[*].dropoffAddress").value(hasItem(DEFAULT_DROPOFF_ADDRESS)));
    }

    protected long getRepositoryCount() {
        return rideRepository.count();
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

    protected Ride getPersistedRide(Ride ride) {
        return rideRepository.findById(ride.getId()).orElseThrow();
    }

    protected void assertPersistedRideToMatchAllProperties(Ride expectedRide) {
        assertRideAllPropertiesEquals(expectedRide, getPersistedRide(expectedRide));
    }

    protected void assertPersistedRideToMatchUpdatableProperties(Ride expectedRide) {
        assertRideAllUpdatablePropertiesEquals(expectedRide, getPersistedRide(expectedRide));
    }
}
