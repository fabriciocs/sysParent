package com.mycompany.myapp.service.mapper;

import com.mycompany.myapp.domain.Child;
import com.mycompany.myapp.domain.Parent;
import com.mycompany.myapp.service.dto.ChildDTO;
import com.mycompany.myapp.service.dto.ParentDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link Child} and its DTO {@link ChildDTO}.
 */
@Mapper(componentModel = "spring")
public interface ChildMapper extends EntityMapper<ChildDTO, Child> {
    @Mapping(target = "parent", source = "parent", qualifiedByName = "parentName")
    ChildDTO toDto(Child s);

    @Named("parentName")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "name", source = "name")
    ParentDTO toDtoParentName(Parent parent);
}
