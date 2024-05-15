package com.mycompany.myapp.service.mapper;

import com.mycompany.myapp.domain.Child;
import com.mycompany.myapp.domain.Driver;
import com.mycompany.myapp.domain.Ride;
import com.mycompany.myapp.service.dto.ChildDTO;
import com.mycompany.myapp.service.dto.DriverDTO;
import com.mycompany.myapp.service.dto.RideDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link Ride} and its DTO {@link RideDTO}.
 */
@Mapper(componentModel = "spring")
public interface RideMapper extends EntityMapper<RideDTO, Ride> {
    @Mapping(target = "child", source = "child", qualifiedByName = "childName")
    @Mapping(target = "driver", source = "driver", qualifiedByName = "driverName")
    RideDTO toDto(Ride s);

    @Named("childName")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "name", source = "name")
    ChildDTO toDtoChildName(Child child);

    @Named("driverName")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "name", source = "name")
    DriverDTO toDtoDriverName(Driver driver);
}
