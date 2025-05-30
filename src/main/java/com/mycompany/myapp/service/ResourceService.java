package com.mycompany.myapp.service;

import com.mycompany.myapp.domain.Resource;
import com.mycompany.myapp.repository.ResourceRepository;
import com.mycompany.myapp.service.dto.ResourceDTO;
import com.mycompany.myapp.service.mapper.ResourceMapper;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link com.mycompany.myapp.domain.Resource}.
 */
@Service
@Transactional
public class ResourceService {

    private static final Logger LOG = LoggerFactory.getLogger(ResourceService.class);

    private final ResourceRepository resourceRepository;

    private final ResourceMapper resourceMapper;

    public ResourceService(ResourceRepository resourceRepository, ResourceMapper resourceMapper) {
        this.resourceRepository = resourceRepository;
        this.resourceMapper = resourceMapper;
    }

    /**
     * Save a resource.
     *
     * @param resourceDTO the entity to save.
     * @return the persisted entity.
     */
    public ResourceDTO save(ResourceDTO resourceDTO) {
        LOG.debug("Request to save Resource : {}", resourceDTO);
        Resource resource = resourceMapper.toEntity(resourceDTO);
        resource = resourceRepository.save(resource);
        return resourceMapper.toDto(resource);
    }

    /**
     * Update a resource.
     *
     * @param resourceDTO the entity to save.
     * @return the persisted entity.
     */
    public ResourceDTO update(ResourceDTO resourceDTO) {
        LOG.debug("Request to update Resource : {}", resourceDTO);
        Resource resource = resourceMapper.toEntity(resourceDTO);
        resource = resourceRepository.save(resource);
        return resourceMapper.toDto(resource);
    }

    /**
     * Partially update a resource.
     *
     * @param resourceDTO the entity to update partially.
     * @return the persisted entity.
     */
    public Optional<ResourceDTO> partialUpdate(ResourceDTO resourceDTO) {
        LOG.debug("Request to partially update Resource : {}", resourceDTO);

        return resourceRepository
            .findById(resourceDTO.getId())
            .map(existingResource -> {
                resourceMapper.partialUpdate(existingResource, resourceDTO);

                return existingResource;
            })
            .map(resourceRepository::save)
            .map(resourceMapper::toDto);
    }

    /**
     * Get all the resources.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    public Page<ResourceDTO> findAll(Pageable pageable) {
        LOG.debug("Request to get all Resources");
        return resourceRepository.findAll(pageable).map(resourceMapper::toDto);
    }

    /**
     * Get one resource by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<ResourceDTO> findOne(Long id) {
        LOG.debug("Request to get Resource : {}", id);
        return resourceRepository.findById(id).map(resourceMapper::toDto);
    }

    /**
     * Delete the resource by id.
     *
     * @param id the id of the entity.
     */
    public void delete(Long id) {
        LOG.debug("Request to delete Resource : {}", id);
        resourceRepository.deleteById(id);
    }
}
