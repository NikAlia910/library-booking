package com.mycompany.myapp.service.mapper;

import com.mycompany.myapp.domain.Resource;
import com.mycompany.myapp.service.dto.ResourceDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link Resource} and its DTO {@link ResourceDTO}.
 */
@Mapper(componentModel = "spring")
public interface ResourceMapper extends EntityMapper<ResourceDTO, Resource> {}
