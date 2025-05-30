package com.mycompany.myapp.service.mapper;

import com.mycompany.myapp.domain.Reservation;
import com.mycompany.myapp.domain.Resource;
import com.mycompany.myapp.domain.User;
import com.mycompany.myapp.service.dto.ReservationDTO;
import com.mycompany.myapp.service.dto.ResourceDTO;
import com.mycompany.myapp.service.dto.UserDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link Reservation} and its DTO {@link ReservationDTO}.
 */
@Mapper(componentModel = "spring")
public interface ReservationMapper extends EntityMapper<ReservationDTO, Reservation> {
    @Mapping(target = "user", source = "user", qualifiedByName = "userLogin")
    @Mapping(target = "resource", source = "resource", qualifiedByName = "resourceId")
    ReservationDTO toDto(Reservation s);

    @Named("userLogin")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "login", source = "login")
    UserDTO toDtoUserLogin(User user);

    @Named("resourceId")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    ResourceDTO toDtoResourceId(Resource resource);
}
