package com.mycompany.myapp.service;

import com.mycompany.myapp.domain.Resource;
import com.mycompany.myapp.domain.enumeration.ResourceType;
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
     * Get all the resources with eager load of many-to-many relationships.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    public Page<ResourceDTO> findAllWithEagerRelationships(Pageable pageable) {
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

    /**
     * Search resources by title.
     *
     * @param title the title to search for.
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    public Page<ResourceDTO> findByTitleContaining(String title, Pageable pageable) {
        LOG.debug("Request to search Resources by title : {}", title);
        return resourceRepository.findByTitleContainingIgnoreCase(title, pageable).map(resourceMapper::toDto);
    }

    /**
     * Search resources by author.
     *
     * @param author the author to search for.
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    public Page<ResourceDTO> findByAuthorContaining(String author, Pageable pageable) {
        LOG.debug("Request to search Resources by author : {}", author);
        return resourceRepository.findByAuthorContainingIgnoreCase(author, pageable).map(resourceMapper::toDto);
    }

    /**
     * Search resources by keywords.
     *
     * @param keywords the keywords to search for.
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    public Page<ResourceDTO> findByKeywordsContaining(String keywords, Pageable pageable) {
        LOG.debug("Request to search Resources by keywords : {}", keywords);
        return resourceRepository.findByKeywordsContainingIgnoreCase(keywords, pageable).map(resourceMapper::toDto);
    }

    /**
     * Search resources by resource type.
     *
     * @param resourceType the resource type to search for.
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    public Page<ResourceDTO> findByResourceType(ResourceType resourceType, Pageable pageable) {
        LOG.debug("Request to search Resources by resource type : {}", resourceType);
        return resourceRepository.findByResourceType(resourceType, pageable).map(resourceMapper::toDto);
    }

    /**
     * Advanced search combining multiple criteria.
     *
     * @param title the title to search for.
     * @param author the author to search for.
     * @param keywords the keywords to search for.
     * @param resourceType the resource type to search for.
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    public Page<ResourceDTO> searchByCriteria(String title, String author, String keywords, ResourceType resourceType, Pageable pageable) {
        LOG.debug(
            "Request to search Resources by criteria - title: {}, author: {}, keywords: {}, type: {}",
            title,
            author,
            keywords,
            resourceType
        );
        return resourceRepository.findByCriteria(title, author, keywords, resourceType, pageable).map(resourceMapper::toDto);
    }
}
