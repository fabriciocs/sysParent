package com.mycompany.myapp.web.rest;

import static com.mycompany.myapp.domain.ChildAsserts.*;
import static com.mycompany.myapp.web.rest.TestUtil.createUpdateProxyForBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mycompany.myapp.IntegrationTest;
import com.mycompany.myapp.domain.Child;
import com.mycompany.myapp.domain.Parent;
import com.mycompany.myapp.repository.ChildRepository;
import com.mycompany.myapp.repository.search.ChildSearchRepository;
import com.mycompany.myapp.service.ChildService;
import com.mycompany.myapp.service.dto.ChildDTO;
import com.mycompany.myapp.service.mapper.ChildMapper;
import jakarta.persistence.EntityManager;
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
 * Integration tests for the {@link ChildResource} REST controller.
 */
@IntegrationTest
@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc
@WithMockUser
class ChildResourceIT {

    private static final String DEFAULT_NAME = "AAAAAAAAAA";
    private static final String UPDATED_NAME = "BBBBBBBBBB";

    private static final Integer DEFAULT_AGE = 0;
    private static final Integer UPDATED_AGE = 1;

    private static final String DEFAULT_SCHOOL_NAME = "AAAAAAAAAA";
    private static final String UPDATED_SCHOOL_NAME = "BBBBBBBBBB";

    private static final String ENTITY_API_URL = "/api/children";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";
    private static final String ENTITY_SEARCH_API_URL = "/api/children/_search";

    private static Random random = new Random();
    private static AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private ObjectMapper om;

    @Autowired
    private ChildRepository childRepository;

    @Mock
    private ChildRepository childRepositoryMock;

    @Autowired
    private ChildMapper childMapper;

    @Mock
    private ChildService childServiceMock;

    @Autowired
    private ChildSearchRepository childSearchRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restChildMockMvc;

    private Child child;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Child createEntity(EntityManager em) {
        Child child = new Child().name(DEFAULT_NAME).age(DEFAULT_AGE).schoolName(DEFAULT_SCHOOL_NAME);
        // Add required entity
        Parent parent;
        if (TestUtil.findAll(em, Parent.class).isEmpty()) {
            parent = ParentResourceIT.createEntity(em);
            em.persist(parent);
            em.flush();
        } else {
            parent = TestUtil.findAll(em, Parent.class).get(0);
        }
        child.setParent(parent);
        return child;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Child createUpdatedEntity(EntityManager em) {
        Child child = new Child().name(UPDATED_NAME).age(UPDATED_AGE).schoolName(UPDATED_SCHOOL_NAME);
        // Add required entity
        Parent parent;
        if (TestUtil.findAll(em, Parent.class).isEmpty()) {
            parent = ParentResourceIT.createUpdatedEntity(em);
            em.persist(parent);
            em.flush();
        } else {
            parent = TestUtil.findAll(em, Parent.class).get(0);
        }
        child.setParent(parent);
        return child;
    }

    @AfterEach
    public void cleanupElasticSearchRepository() {
        childSearchRepository.deleteAll();
        assertThat(childSearchRepository.count()).isEqualTo(0);
    }

    @BeforeEach
    public void initTest() {
        child = createEntity(em);
    }

    @Test
    @Transactional
    void createChild() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(childSearchRepository.findAll());
        // Create the Child
        ChildDTO childDTO = childMapper.toDto(child);
        var returnedChildDTO = om.readValue(
            restChildMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(childDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            ChildDTO.class
        );

        // Validate the Child in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedChild = childMapper.toEntity(returnedChildDTO);
        assertChildUpdatableFieldsEquals(returnedChild, getPersistedChild(returnedChild));

        await()
            .atMost(5, TimeUnit.SECONDS)
            .untilAsserted(() -> {
                int searchDatabaseSizeAfter = IterableUtil.sizeOf(childSearchRepository.findAll());
                assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore + 1);
            });
    }

    @Test
    @Transactional
    void createChildWithExistingId() throws Exception {
        // Create the Child with an existing ID
        child.setId(1L);
        ChildDTO childDTO = childMapper.toDto(child);

        long databaseSizeBeforeCreate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(childSearchRepository.findAll());

        // An entity with an existing ID cannot be created, so this API call must fail
        restChildMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(childDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Child in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(childSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void checkNameIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(childSearchRepository.findAll());
        // set the field null
        child.setName(null);

        // Create the Child, which fails.
        ChildDTO childDTO = childMapper.toDto(child);

        restChildMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(childDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);

        int searchDatabaseSizeAfter = IterableUtil.sizeOf(childSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void checkSchoolNameIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(childSearchRepository.findAll());
        // set the field null
        child.setSchoolName(null);

        // Create the Child, which fails.
        ChildDTO childDTO = childMapper.toDto(child);

        restChildMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(childDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);

        int searchDatabaseSizeAfter = IterableUtil.sizeOf(childSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void getAllChildren() throws Exception {
        // Initialize the database
        childRepository.saveAndFlush(child);

        // Get all the childList
        restChildMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(child.getId().intValue())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME)))
            .andExpect(jsonPath("$.[*].age").value(hasItem(DEFAULT_AGE)))
            .andExpect(jsonPath("$.[*].schoolName").value(hasItem(DEFAULT_SCHOOL_NAME)));
    }

    @SuppressWarnings({ "unchecked" })
    void getAllChildrenWithEagerRelationshipsIsEnabled() throws Exception {
        when(childServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restChildMockMvc.perform(get(ENTITY_API_URL + "?eagerload=true")).andExpect(status().isOk());

        verify(childServiceMock, times(1)).findAllWithEagerRelationships(any());
    }

    @SuppressWarnings({ "unchecked" })
    void getAllChildrenWithEagerRelationshipsIsNotEnabled() throws Exception {
        when(childServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restChildMockMvc.perform(get(ENTITY_API_URL + "?eagerload=false")).andExpect(status().isOk());
        verify(childRepositoryMock, times(1)).findAll(any(Pageable.class));
    }

    @Test
    @Transactional
    void getChild() throws Exception {
        // Initialize the database
        childRepository.saveAndFlush(child);

        // Get the child
        restChildMockMvc
            .perform(get(ENTITY_API_URL_ID, child.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(child.getId().intValue()))
            .andExpect(jsonPath("$.name").value(DEFAULT_NAME))
            .andExpect(jsonPath("$.age").value(DEFAULT_AGE))
            .andExpect(jsonPath("$.schoolName").value(DEFAULT_SCHOOL_NAME));
    }

    @Test
    @Transactional
    void getNonExistingChild() throws Exception {
        // Get the child
        restChildMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingChild() throws Exception {
        // Initialize the database
        childRepository.saveAndFlush(child);

        long databaseSizeBeforeUpdate = getRepositoryCount();
        childSearchRepository.save(child);
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(childSearchRepository.findAll());

        // Update the child
        Child updatedChild = childRepository.findById(child.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedChild are not directly saved in db
        em.detach(updatedChild);
        updatedChild.name(UPDATED_NAME).age(UPDATED_AGE).schoolName(UPDATED_SCHOOL_NAME);
        ChildDTO childDTO = childMapper.toDto(updatedChild);

        restChildMockMvc
            .perform(
                put(ENTITY_API_URL_ID, childDTO.getId()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(childDTO))
            )
            .andExpect(status().isOk());

        // Validate the Child in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedChildToMatchAllProperties(updatedChild);

        await()
            .atMost(5, TimeUnit.SECONDS)
            .untilAsserted(() -> {
                int searchDatabaseSizeAfter = IterableUtil.sizeOf(childSearchRepository.findAll());
                assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
                List<Child> childSearchList = Streamable.of(childSearchRepository.findAll()).toList();
                Child testChildSearch = childSearchList.get(searchDatabaseSizeAfter - 1);

                assertChildAllPropertiesEquals(testChildSearch, updatedChild);
            });
    }

    @Test
    @Transactional
    void putNonExistingChild() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(childSearchRepository.findAll());
        child.setId(longCount.incrementAndGet());

        // Create the Child
        ChildDTO childDTO = childMapper.toDto(child);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restChildMockMvc
            .perform(
                put(ENTITY_API_URL_ID, childDTO.getId()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(childDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Child in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(childSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void putWithIdMismatchChild() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(childSearchRepository.findAll());
        child.setId(longCount.incrementAndGet());

        // Create the Child
        ChildDTO childDTO = childMapper.toDto(child);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restChildMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(childDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Child in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(childSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamChild() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(childSearchRepository.findAll());
        child.setId(longCount.incrementAndGet());

        // Create the Child
        ChildDTO childDTO = childMapper.toDto(child);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restChildMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(childDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Child in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(childSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void partialUpdateChildWithPatch() throws Exception {
        // Initialize the database
        childRepository.saveAndFlush(child);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the child using partial update
        Child partialUpdatedChild = new Child();
        partialUpdatedChild.setId(child.getId());

        partialUpdatedChild.schoolName(UPDATED_SCHOOL_NAME);

        restChildMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedChild.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedChild))
            )
            .andExpect(status().isOk());

        // Validate the Child in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertChildUpdatableFieldsEquals(createUpdateProxyForBean(partialUpdatedChild, child), getPersistedChild(child));
    }

    @Test
    @Transactional
    void fullUpdateChildWithPatch() throws Exception {
        // Initialize the database
        childRepository.saveAndFlush(child);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the child using partial update
        Child partialUpdatedChild = new Child();
        partialUpdatedChild.setId(child.getId());

        partialUpdatedChild.name(UPDATED_NAME).age(UPDATED_AGE).schoolName(UPDATED_SCHOOL_NAME);

        restChildMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedChild.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedChild))
            )
            .andExpect(status().isOk());

        // Validate the Child in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertChildUpdatableFieldsEquals(partialUpdatedChild, getPersistedChild(partialUpdatedChild));
    }

    @Test
    @Transactional
    void patchNonExistingChild() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(childSearchRepository.findAll());
        child.setId(longCount.incrementAndGet());

        // Create the Child
        ChildDTO childDTO = childMapper.toDto(child);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restChildMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, childDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(childDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Child in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(childSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void patchWithIdMismatchChild() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(childSearchRepository.findAll());
        child.setId(longCount.incrementAndGet());

        // Create the Child
        ChildDTO childDTO = childMapper.toDto(child);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restChildMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(childDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Child in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(childSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamChild() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(childSearchRepository.findAll());
        child.setId(longCount.incrementAndGet());

        // Create the Child
        ChildDTO childDTO = childMapper.toDto(child);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restChildMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(childDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Child in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(childSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void deleteChild() throws Exception {
        // Initialize the database
        childRepository.saveAndFlush(child);
        childRepository.save(child);
        childSearchRepository.save(child);

        long databaseSizeBeforeDelete = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(childSearchRepository.findAll());
        assertThat(searchDatabaseSizeBefore).isEqualTo(databaseSizeBeforeDelete);

        // Delete the child
        restChildMockMvc
            .perform(delete(ENTITY_API_URL_ID, child.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(childSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore - 1);
    }

    @Test
    @Transactional
    void searchChild() throws Exception {
        // Initialize the database
        child = childRepository.saveAndFlush(child);
        childSearchRepository.save(child);

        // Search the child
        restChildMockMvc
            .perform(get(ENTITY_SEARCH_API_URL + "?query=id:" + child.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(child.getId().intValue())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME)))
            .andExpect(jsonPath("$.[*].age").value(hasItem(DEFAULT_AGE)))
            .andExpect(jsonPath("$.[*].schoolName").value(hasItem(DEFAULT_SCHOOL_NAME)));
    }

    protected long getRepositoryCount() {
        return childRepository.count();
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

    protected Child getPersistedChild(Child child) {
        return childRepository.findById(child.getId()).orElseThrow();
    }

    protected void assertPersistedChildToMatchAllProperties(Child expectedChild) {
        assertChildAllPropertiesEquals(expectedChild, getPersistedChild(expectedChild));
    }

    protected void assertPersistedChildToMatchUpdatableProperties(Child expectedChild) {
        assertChildAllUpdatablePropertiesEquals(expectedChild, getPersistedChild(expectedChild));
    }
}
