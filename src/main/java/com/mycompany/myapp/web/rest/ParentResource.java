package com.mycompany.myapp.web.rest;

import com.mycompany.myapp.repository.ParentRepository;
import com.mycompany.myapp.service.ParentService;
import com.mycompany.myapp.service.dto.ParentDTO;
import com.mycompany.myapp.web.rest.errors.BadRequestAlertException;
import com.mycompany.myapp.web.rest.errors.ElasticsearchExceptionMapper;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.ResponseUtil;

/**
 * REST controller for managing {@link com.mycompany.myapp.domain.Parent}.
 */
@RestController
@RequestMapping("/api/parents")
public class ParentResource {

    private final Logger log = LoggerFactory.getLogger(ParentResource.class);

    private static final String ENTITY_NAME = "parent";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final ParentService parentService;

    private final ParentRepository parentRepository;

    public ParentResource(ParentService parentService, ParentRepository parentRepository) {
        this.parentService = parentService;
        this.parentRepository = parentRepository;
    }

    /**
     * {@code POST  /parents} : Create a new parent.
     *
     * @param parentDTO the parentDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new parentDTO, or with status {@code 400 (Bad Request)} if the parent has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<ParentDTO> createParent(@Valid @RequestBody ParentDTO parentDTO) throws URISyntaxException {
        log.debug("REST request to save Parent : {}", parentDTO);
        if (parentDTO.getId() != null) {
            throw new BadRequestAlertException("A new parent cannot already have an ID", ENTITY_NAME, "idexists");
        }
        parentDTO = parentService.save(parentDTO);
        return ResponseEntity.created(new URI("/api/parents/" + parentDTO.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, parentDTO.getId().toString()))
            .body(parentDTO);
    }

    /**
     * {@code PUT  /parents/:id} : Updates an existing parent.
     *
     * @param id the id of the parentDTO to save.
     * @param parentDTO the parentDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated parentDTO,
     * or with status {@code 400 (Bad Request)} if the parentDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the parentDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public ResponseEntity<ParentDTO> updateParent(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody ParentDTO parentDTO
    ) throws URISyntaxException {
        log.debug("REST request to update Parent : {}, {}", id, parentDTO);
        if (parentDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, parentDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!parentRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        parentDTO = parentService.update(parentDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, parentDTO.getId().toString()))
            .body(parentDTO);
    }

    /**
     * {@code PATCH  /parents/:id} : Partial updates given fields of an existing parent, field will ignore if it is null
     *
     * @param id the id of the parentDTO to save.
     * @param parentDTO the parentDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated parentDTO,
     * or with status {@code 400 (Bad Request)} if the parentDTO is not valid,
     * or with status {@code 404 (Not Found)} if the parentDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the parentDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<ParentDTO> partialUpdateParent(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody ParentDTO parentDTO
    ) throws URISyntaxException {
        log.debug("REST request to partial update Parent partially : {}, {}", id, parentDTO);
        if (parentDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, parentDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!parentRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<ParentDTO> result = parentService.partialUpdate(parentDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, parentDTO.getId().toString())
        );
    }

    /**
     * {@code GET  /parents} : get all the parents.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of parents in body.
     */
    @GetMapping("")
    public List<ParentDTO> getAllParents() {
        log.debug("REST request to get all Parents");
        return parentService.findAll();
    }

    /**
     * {@code GET  /parents/:id} : get the "id" parent.
     *
     * @param id the id of the parentDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the parentDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ParentDTO> getParent(@PathVariable("id") Long id) {
        log.debug("REST request to get Parent : {}", id);
        Optional<ParentDTO> parentDTO = parentService.findOne(id);
        return ResponseUtil.wrapOrNotFound(parentDTO);
    }

    /**
     * {@code DELETE  /parents/:id} : delete the "id" parent.
     *
     * @param id the id of the parentDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteParent(@PathVariable("id") Long id) {
        log.debug("REST request to delete Parent : {}", id);
        parentService.delete(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()))
            .build();
    }

    /**
     * {@code SEARCH  /parents/_search?query=:query} : search for the parent corresponding
     * to the query.
     *
     * @param query the query of the parent search.
     * @return the result of the search.
     */
    @GetMapping("/_search")
    public List<ParentDTO> searchParents(@RequestParam("query") String query) {
        log.debug("REST request to search Parents for query {}", query);
        try {
            return parentService.search(query);
        } catch (RuntimeException e) {
            throw ElasticsearchExceptionMapper.mapException(e);
        }
    }
}
