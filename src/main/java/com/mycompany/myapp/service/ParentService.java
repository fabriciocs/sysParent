package com.mycompany.myapp.service;

import com.mycompany.myapp.domain.Parent;
import com.mycompany.myapp.repository.ParentRepository;
import com.mycompany.myapp.repository.search.ParentSearchRepository;
import com.mycompany.myapp.service.dto.ParentDTO;
import com.mycompany.myapp.service.mapper.ParentMapper;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link com.mycompany.myapp.domain.Parent}.
 */
@Service
@Transactional
public class ParentService {

    private final Logger log = LoggerFactory.getLogger(ParentService.class);

    private final ParentRepository parentRepository;

    private final ParentMapper parentMapper;

    private final ParentSearchRepository parentSearchRepository;

    public ParentService(ParentRepository parentRepository, ParentMapper parentMapper, ParentSearchRepository parentSearchRepository) {
        this.parentRepository = parentRepository;
        this.parentMapper = parentMapper;
        this.parentSearchRepository = parentSearchRepository;
    }

    /**
     * Save a parent.
     *
     * @param parentDTO the entity to save.
     * @return the persisted entity.
     */
    public ParentDTO save(ParentDTO parentDTO) {
        log.debug("Request to save Parent : {}", parentDTO);
        Parent parent = parentMapper.toEntity(parentDTO);
        parent = parentRepository.save(parent);
        parentSearchRepository.index(parent);
        return parentMapper.toDto(parent);
    }

    /**
     * Update a parent.
     *
     * @param parentDTO the entity to save.
     * @return the persisted entity.
     */
    public ParentDTO update(ParentDTO parentDTO) {
        log.debug("Request to update Parent : {}", parentDTO);
        Parent parent = parentMapper.toEntity(parentDTO);
        parent = parentRepository.save(parent);
        parentSearchRepository.index(parent);
        return parentMapper.toDto(parent);
    }

    /**
     * Partially update a parent.
     *
     * @param parentDTO the entity to update partially.
     * @return the persisted entity.
     */
    public Optional<ParentDTO> partialUpdate(ParentDTO parentDTO) {
        log.debug("Request to partially update Parent : {}", parentDTO);

        return parentRepository
            .findById(parentDTO.getId())
            .map(existingParent -> {
                parentMapper.partialUpdate(existingParent, parentDTO);

                return existingParent;
            })
            .map(parentRepository::save)
            .map(savedParent -> {
                parentSearchRepository.index(savedParent);
                return savedParent;
            })
            .map(parentMapper::toDto);
    }

    /**
     * Get all the parents.
     *
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    public List<ParentDTO> findAll() {
        log.debug("Request to get all Parents");
        return parentRepository.findAll().stream().map(parentMapper::toDto).collect(Collectors.toCollection(LinkedList::new));
    }

    /**
     * Get one parent by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<ParentDTO> findOne(Long id) {
        log.debug("Request to get Parent : {}", id);
        return parentRepository.findById(id).map(parentMapper::toDto);
    }

    /**
     * Delete the parent by id.
     *
     * @param id the id of the entity.
     */
    public void delete(Long id) {
        log.debug("Request to delete Parent : {}", id);
        parentRepository.deleteById(id);
        parentSearchRepository.deleteFromIndexById(id);
    }

    /**
     * Search for the parent corresponding to the query.
     *
     * @param query the query of the search.
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    public List<ParentDTO> search(String query) {
        log.debug("Request to search Parents for query {}", query);
        try {
            return StreamSupport.stream(parentSearchRepository.search(query).spliterator(), false).map(parentMapper::toDto).toList();
        } catch (RuntimeException e) {
            throw e;
        }
    }
}
