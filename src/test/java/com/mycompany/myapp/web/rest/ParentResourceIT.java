package com.mycompany.myapp.web.rest;

import static com.mycompany.myapp.domain.ParentAsserts.*;
import static com.mycompany.myapp.web.rest.TestUtil.createUpdateProxyForBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mycompany.myapp.IntegrationTest;
import com.mycompany.myapp.domain.Parent;
import com.mycompany.myapp.repository.ParentRepository;
import com.mycompany.myapp.repository.search.ParentSearchRepository;
import com.mycompany.myapp.service.dto.ParentDTO;
import com.mycompany.myapp.service.mapper.ParentMapper;
import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import org.assertj.core.util.IterableUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.data.util.Streamable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration tests for the {@link ParentResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class ParentResourceIT {

    private static final String DEFAULT_NAME = "AAAAAAAAAA";
    private static final String UPDATED_NAME = "BBBBBBBBBB";

    private static final String DEFAULT_PHONE = "2860809066";
    private static final String UPDATED_PHONE = "0762472413";

    private static final String DEFAULT_EMAIL = "AAAAAAAAAA";
    private static final String UPDATED_EMAIL = "BBBBBBBBBB";

    private static final String DEFAULT_ADDRESS = "AAAAAAAAAA";
    private static final String UPDATED_ADDRESS = "BBBBBBBBBB";

    private static final String ENTITY_API_URL = "/api/parents";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";
    private static final String ENTITY_SEARCH_API_URL = "/api/parents/_search";

    private static Random random = new Random();
    private static AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private ObjectMapper om;

    @Autowired
    private ParentRepository parentRepository;

    @Autowired
    private ParentMapper parentMapper;

    @Autowired
    private ParentSearchRepository parentSearchRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restParentMockMvc;

    private Parent parent;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Parent createEntity(EntityManager em) {
        Parent parent = new Parent().name(DEFAULT_NAME).phone(DEFAULT_PHONE).email(DEFAULT_EMAIL).address(DEFAULT_ADDRESS);
        return parent;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Parent createUpdatedEntity(EntityManager em) {
        Parent parent = new Parent().name(UPDATED_NAME).phone(UPDATED_PHONE).email(UPDATED_EMAIL).address(UPDATED_ADDRESS);
        return parent;
    }

    @AfterEach
    public void cleanupElasticSearchRepository() {
        parentSearchRepository.deleteAll();
        assertThat(parentSearchRepository.count()).isEqualTo(0);
    }

    @BeforeEach
    public void initTest() {
        parent = createEntity(em);
    }

    @Test
    @Transactional
    void createParent() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(parentSearchRepository.findAll());
        // Create the Parent
        ParentDTO parentDTO = parentMapper.toDto(parent);
        var returnedParentDTO = om.readValue(
            restParentMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(parentDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            ParentDTO.class
        );

        // Validate the Parent in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedParent = parentMapper.toEntity(returnedParentDTO);
        assertParentUpdatableFieldsEquals(returnedParent, getPersistedParent(returnedParent));

        await()
            .atMost(5, TimeUnit.SECONDS)
            .untilAsserted(() -> {
                int searchDatabaseSizeAfter = IterableUtil.sizeOf(parentSearchRepository.findAll());
                assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore + 1);
            });
    }

    @Test
    @Transactional
    void createParentWithExistingId() throws Exception {
        // Create the Parent with an existing ID
        parent.setId(1L);
        ParentDTO parentDTO = parentMapper.toDto(parent);

        long databaseSizeBeforeCreate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(parentSearchRepository.findAll());

        // An entity with an existing ID cannot be created, so this API call must fail
        restParentMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(parentDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Parent in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(parentSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void checkNameIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(parentSearchRepository.findAll());
        // set the field null
        parent.setName(null);

        // Create the Parent, which fails.
        ParentDTO parentDTO = parentMapper.toDto(parent);

        restParentMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(parentDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);

        int searchDatabaseSizeAfter = IterableUtil.sizeOf(parentSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void checkPhoneIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(parentSearchRepository.findAll());
        // set the field null
        parent.setPhone(null);

        // Create the Parent, which fails.
        ParentDTO parentDTO = parentMapper.toDto(parent);

        restParentMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(parentDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);

        int searchDatabaseSizeAfter = IterableUtil.sizeOf(parentSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void checkEmailIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(parentSearchRepository.findAll());
        // set the field null
        parent.setEmail(null);

        // Create the Parent, which fails.
        ParentDTO parentDTO = parentMapper.toDto(parent);

        restParentMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(parentDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);

        int searchDatabaseSizeAfter = IterableUtil.sizeOf(parentSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void getAllParents() throws Exception {
        // Initialize the database
        parentRepository.saveAndFlush(parent);

        // Get all the parentList
        restParentMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(parent.getId().intValue())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME)))
            .andExpect(jsonPath("$.[*].phone").value(hasItem(DEFAULT_PHONE)))
            .andExpect(jsonPath("$.[*].email").value(hasItem(DEFAULT_EMAIL)))
            .andExpect(jsonPath("$.[*].address").value(hasItem(DEFAULT_ADDRESS)));
    }

    @Test
    @Transactional
    void getParent() throws Exception {
        // Initialize the database
        parentRepository.saveAndFlush(parent);

        // Get the parent
        restParentMockMvc
            .perform(get(ENTITY_API_URL_ID, parent.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(parent.getId().intValue()))
            .andExpect(jsonPath("$.name").value(DEFAULT_NAME))
            .andExpect(jsonPath("$.phone").value(DEFAULT_PHONE))
            .andExpect(jsonPath("$.email").value(DEFAULT_EMAIL))
            .andExpect(jsonPath("$.address").value(DEFAULT_ADDRESS));
    }

    @Test
    @Transactional
    void getNonExistingParent() throws Exception {
        // Get the parent
        restParentMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingParent() throws Exception {
        // Initialize the database
        parentRepository.saveAndFlush(parent);

        long databaseSizeBeforeUpdate = getRepositoryCount();
        parentSearchRepository.save(parent);
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(parentSearchRepository.findAll());

        // Update the parent
        Parent updatedParent = parentRepository.findById(parent.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedParent are not directly saved in db
        em.detach(updatedParent);
        updatedParent.name(UPDATED_NAME).phone(UPDATED_PHONE).email(UPDATED_EMAIL).address(UPDATED_ADDRESS);
        ParentDTO parentDTO = parentMapper.toDto(updatedParent);

        restParentMockMvc
            .perform(
                put(ENTITY_API_URL_ID, parentDTO.getId()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(parentDTO))
            )
            .andExpect(status().isOk());

        // Validate the Parent in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedParentToMatchAllProperties(updatedParent);

        await()
            .atMost(5, TimeUnit.SECONDS)
            .untilAsserted(() -> {
                int searchDatabaseSizeAfter = IterableUtil.sizeOf(parentSearchRepository.findAll());
                assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
                List<Parent> parentSearchList = Streamable.of(parentSearchRepository.findAll()).toList();
                Parent testParentSearch = parentSearchList.get(searchDatabaseSizeAfter - 1);

                assertParentAllPropertiesEquals(testParentSearch, updatedParent);
            });
    }

    @Test
    @Transactional
    void putNonExistingParent() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(parentSearchRepository.findAll());
        parent.setId(longCount.incrementAndGet());

        // Create the Parent
        ParentDTO parentDTO = parentMapper.toDto(parent);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restParentMockMvc
            .perform(
                put(ENTITY_API_URL_ID, parentDTO.getId()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(parentDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Parent in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(parentSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void putWithIdMismatchParent() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(parentSearchRepository.findAll());
        parent.setId(longCount.incrementAndGet());

        // Create the Parent
        ParentDTO parentDTO = parentMapper.toDto(parent);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restParentMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(parentDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Parent in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(parentSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamParent() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(parentSearchRepository.findAll());
        parent.setId(longCount.incrementAndGet());

        // Create the Parent
        ParentDTO parentDTO = parentMapper.toDto(parent);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restParentMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(parentDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Parent in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(parentSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void partialUpdateParentWithPatch() throws Exception {
        // Initialize the database
        parentRepository.saveAndFlush(parent);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the parent using partial update
        Parent partialUpdatedParent = new Parent();
        partialUpdatedParent.setId(parent.getId());

        partialUpdatedParent.phone(UPDATED_PHONE).email(UPDATED_EMAIL);

        restParentMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedParent.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedParent))
            )
            .andExpect(status().isOk());

        // Validate the Parent in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertParentUpdatableFieldsEquals(createUpdateProxyForBean(partialUpdatedParent, parent), getPersistedParent(parent));
    }

    @Test
    @Transactional
    void fullUpdateParentWithPatch() throws Exception {
        // Initialize the database
        parentRepository.saveAndFlush(parent);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the parent using partial update
        Parent partialUpdatedParent = new Parent();
        partialUpdatedParent.setId(parent.getId());

        partialUpdatedParent.name(UPDATED_NAME).phone(UPDATED_PHONE).email(UPDATED_EMAIL).address(UPDATED_ADDRESS);

        restParentMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedParent.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedParent))
            )
            .andExpect(status().isOk());

        // Validate the Parent in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertParentUpdatableFieldsEquals(partialUpdatedParent, getPersistedParent(partialUpdatedParent));
    }

    @Test
    @Transactional
    void patchNonExistingParent() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(parentSearchRepository.findAll());
        parent.setId(longCount.incrementAndGet());

        // Create the Parent
        ParentDTO parentDTO = parentMapper.toDto(parent);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restParentMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, parentDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(parentDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Parent in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(parentSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void patchWithIdMismatchParent() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(parentSearchRepository.findAll());
        parent.setId(longCount.incrementAndGet());

        // Create the Parent
        ParentDTO parentDTO = parentMapper.toDto(parent);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restParentMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(parentDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Parent in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(parentSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamParent() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(parentSearchRepository.findAll());
        parent.setId(longCount.incrementAndGet());

        // Create the Parent
        ParentDTO parentDTO = parentMapper.toDto(parent);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restParentMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(parentDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Parent in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(parentSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void deleteParent() throws Exception {
        // Initialize the database
        parentRepository.saveAndFlush(parent);
        parentRepository.save(parent);
        parentSearchRepository.save(parent);

        long databaseSizeBeforeDelete = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(parentSearchRepository.findAll());
        assertThat(searchDatabaseSizeBefore).isEqualTo(databaseSizeBeforeDelete);

        // Delete the parent
        restParentMockMvc
            .perform(delete(ENTITY_API_URL_ID, parent.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(parentSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore - 1);
    }

    @Test
    @Transactional
    void searchParent() throws Exception {
        // Initialize the database
        parent = parentRepository.saveAndFlush(parent);
        parentSearchRepository.save(parent);

        // Search the parent
        restParentMockMvc
            .perform(get(ENTITY_SEARCH_API_URL + "?query=id:" + parent.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(parent.getId().intValue())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME)))
            .andExpect(jsonPath("$.[*].phone").value(hasItem(DEFAULT_PHONE)))
            .andExpect(jsonPath("$.[*].email").value(hasItem(DEFAULT_EMAIL)))
            .andExpect(jsonPath("$.[*].address").value(hasItem(DEFAULT_ADDRESS)));
    }

    protected long getRepositoryCount() {
        return parentRepository.count();
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

    protected Parent getPersistedParent(Parent parent) {
        return parentRepository.findById(parent.getId()).orElseThrow();
    }

    protected void assertPersistedParentToMatchAllProperties(Parent expectedParent) {
        assertParentAllPropertiesEquals(expectedParent, getPersistedParent(expectedParent));
    }

    protected void assertPersistedParentToMatchUpdatableProperties(Parent expectedParent) {
        assertParentAllUpdatablePropertiesEquals(expectedParent, getPersistedParent(expectedParent));
    }
}
