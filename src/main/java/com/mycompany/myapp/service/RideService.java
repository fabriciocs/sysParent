package com.mycompany.myapp.service;

import com.mycompany.myapp.domain.Ride;
import com.mycompany.myapp.repository.RideRepository;
import com.mycompany.myapp.repository.search.RideSearchRepository;
import com.mycompany.myapp.service.dto.RideDTO;
import com.mycompany.myapp.service.mapper.RideMapper;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link com.mycompany.myapp.domain.Ride}.
 */
@Service
@Transactional
public class RideService {

    private final Logger log = LoggerFactory.getLogger(RideService.class);

    private final RideRepository rideRepository;

    private final RideMapper rideMapper;

    private final RideSearchRepository rideSearchRepository;

    public RideService(RideRepository rideRepository, RideMapper rideMapper, RideSearchRepository rideSearchRepository) {
        this.rideRepository = rideRepository;
        this.rideMapper = rideMapper;
        this.rideSearchRepository = rideSearchRepository;
    }

    /**
     * Save a ride.
     *
     * @param rideDTO the entity to save.
     * @return the persisted entity.
     */
    public RideDTO save(RideDTO rideDTO) {
        log.debug("Request to save Ride : {}", rideDTO);
        Ride ride = rideMapper.toEntity(rideDTO);
        ride = rideRepository.save(ride);
        rideSearchRepository.index(ride);
        return rideMapper.toDto(ride);
    }

    /**
     * Update a ride.
     *
     * @param rideDTO the entity to save.
     * @return the persisted entity.
     */
    public RideDTO update(RideDTO rideDTO) {
        log.debug("Request to update Ride : {}", rideDTO);
        Ride ride = rideMapper.toEntity(rideDTO);
        ride = rideRepository.save(ride);
        rideSearchRepository.index(ride);
        return rideMapper.toDto(ride);
    }

    /**
     * Partially update a ride.
     *
     * @param rideDTO the entity to update partially.
     * @return the persisted entity.
     */
    public Optional<RideDTO> partialUpdate(RideDTO rideDTO) {
        log.debug("Request to partially update Ride : {}", rideDTO);

        return rideRepository
            .findById(rideDTO.getId())
            .map(existingRide -> {
                rideMapper.partialUpdate(existingRide, rideDTO);

                return existingRide;
            })
            .map(rideRepository::save)
            .map(savedRide -> {
                rideSearchRepository.index(savedRide);
                return savedRide;
            })
            .map(rideMapper::toDto);
    }

    /**
     * Get all the rides.
     *
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    public List<RideDTO> findAll() {
        log.debug("Request to get all Rides");
        return rideRepository.findAll().stream().map(rideMapper::toDto).collect(Collectors.toCollection(LinkedList::new));
    }

    /**
     * Get all the rides with eager load of many-to-many relationships.
     *
     * @return the list of entities.
     */
    public Page<RideDTO> findAllWithEagerRelationships(Pageable pageable) {
        return rideRepository.findAllWithEagerRelationships(pageable).map(rideMapper::toDto);
    }

    /**
     * Get one ride by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<RideDTO> findOne(Long id) {
        log.debug("Request to get Ride : {}", id);
        return rideRepository.findOneWithEagerRelationships(id).map(rideMapper::toDto);
    }

    /**
     * Delete the ride by id.
     *
     * @param id the id of the entity.
     */
    public void delete(Long id) {
        log.debug("Request to delete Ride : {}", id);
        rideRepository.deleteById(id);
        rideSearchRepository.deleteFromIndexById(id);
    }

    /**
     * Search for the ride corresponding to the query.
     *
     * @param query the query of the search.
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    public List<RideDTO> search(String query) {
        log.debug("Request to search Rides for query {}", query);
        try {
            return StreamSupport.stream(rideSearchRepository.search(query).spliterator(), false).map(rideMapper::toDto).toList();
        } catch (RuntimeException e) {
            throw e;
        }
    }
}
