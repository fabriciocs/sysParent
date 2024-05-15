package com.mycompany.myapp.web.rest;

import com.mycompany.myapp.repository.RideRepository;
import com.mycompany.myapp.service.RideService;
import com.mycompany.myapp.service.dto.RideDTO;
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
 * REST controller for managing {@link com.mycompany.myapp.domain.Ride}.
 */
@RestController
@RequestMapping("/api/rides")
public class RideResource {

    private final Logger log = LoggerFactory.getLogger(RideResource.class);

    private static final String ENTITY_NAME = "ride";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final RideService rideService;

    private final RideRepository rideRepository;

    public RideResource(RideService rideService, RideRepository rideRepository) {
        this.rideService = rideService;
        this.rideRepository = rideRepository;
    }

    /**
     * {@code POST  /rides} : Create a new ride.
     *
     * @param rideDTO the rideDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new rideDTO, or with status {@code 400 (Bad Request)} if the ride has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<RideDTO> createRide(@Valid @RequestBody RideDTO rideDTO) throws URISyntaxException {
        log.debug("REST request to save Ride : {}", rideDTO);
        if (rideDTO.getId() != null) {
            throw new BadRequestAlertException("A new ride cannot already have an ID", ENTITY_NAME, "idexists");
        }
        rideDTO = rideService.save(rideDTO);
        return ResponseEntity.created(new URI("/api/rides/" + rideDTO.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, rideDTO.getId().toString()))
            .body(rideDTO);
    }

    /**
     * {@code PUT  /rides/:id} : Updates an existing ride.
     *
     * @param id the id of the rideDTO to save.
     * @param rideDTO the rideDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated rideDTO,
     * or with status {@code 400 (Bad Request)} if the rideDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the rideDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public ResponseEntity<RideDTO> updateRide(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody RideDTO rideDTO
    ) throws URISyntaxException {
        log.debug("REST request to update Ride : {}, {}", id, rideDTO);
        if (rideDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, rideDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!rideRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        rideDTO = rideService.update(rideDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, rideDTO.getId().toString()))
            .body(rideDTO);
    }

    /**
     * {@code PATCH  /rides/:id} : Partial updates given fields of an existing ride, field will ignore if it is null
     *
     * @param id the id of the rideDTO to save.
     * @param rideDTO the rideDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated rideDTO,
     * or with status {@code 400 (Bad Request)} if the rideDTO is not valid,
     * or with status {@code 404 (Not Found)} if the rideDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the rideDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<RideDTO> partialUpdateRide(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody RideDTO rideDTO
    ) throws URISyntaxException {
        log.debug("REST request to partial update Ride partially : {}, {}", id, rideDTO);
        if (rideDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, rideDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!rideRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<RideDTO> result = rideService.partialUpdate(rideDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, rideDTO.getId().toString())
        );
    }

    /**
     * {@code GET  /rides} : get all the rides.
     *
     * @param eagerload flag to eager load entities from relationships (This is applicable for many-to-many).
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of rides in body.
     */
    @GetMapping("")
    public List<RideDTO> getAllRides(@RequestParam(name = "eagerload", required = false, defaultValue = "true") boolean eagerload) {
        log.debug("REST request to get all Rides");
        return rideService.findAll();
    }

    /**
     * {@code GET  /rides/:id} : get the "id" ride.
     *
     * @param id the id of the rideDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the rideDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<RideDTO> getRide(@PathVariable("id") Long id) {
        log.debug("REST request to get Ride : {}", id);
        Optional<RideDTO> rideDTO = rideService.findOne(id);
        return ResponseUtil.wrapOrNotFound(rideDTO);
    }

    /**
     * {@code DELETE  /rides/:id} : delete the "id" ride.
     *
     * @param id the id of the rideDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRide(@PathVariable("id") Long id) {
        log.debug("REST request to delete Ride : {}", id);
        rideService.delete(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()))
            .build();
    }

    /**
     * {@code SEARCH  /rides/_search?query=:query} : search for the ride corresponding
     * to the query.
     *
     * @param query the query of the ride search.
     * @return the result of the search.
     */
    @GetMapping("/_search")
    public List<RideDTO> searchRides(@RequestParam("query") String query) {
        log.debug("REST request to search Rides for query {}", query);
        try {
            return rideService.search(query);
        } catch (RuntimeException e) {
            throw ElasticsearchExceptionMapper.mapException(e);
        }
    }
}
