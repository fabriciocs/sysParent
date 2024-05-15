package com.mycompany.myapp.service.mapper;

import com.mycompany.myapp.domain.Parent;
import com.mycompany.myapp.service.dto.ParentDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link Parent} and its DTO {@link ParentDTO}.
 */
@Mapper(componentModel = "spring")
public interface ParentMapper extends EntityMapper<ParentDTO, Parent> {}
