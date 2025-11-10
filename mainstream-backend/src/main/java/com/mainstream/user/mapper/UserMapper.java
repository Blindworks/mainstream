package com.mainstream.user.mapper;

import com.mainstream.user.dto.UserDto;
import com.mainstream.user.dto.UserRegistrationDto;
import com.mainstream.user.entity.User;
import org.mapstruct.*;

import java.util.List;

@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface UserMapper {

    @Mapping(target = "fullName", expression = "java(user.getFullName())")
    UserDto toDto(User user);

    List<UserDto> toDtoList(List<User> users);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "password", ignore = true) // Will be set separately after encoding
    @Mapping(target = "isActive", constant = "true")
    @Mapping(target = "role", constant = "USER")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    User toEntity(UserRegistrationDto registrationDto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "email", ignore = true) // Email should not be updated
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "role", ignore = true) // Role should not be updated via this method
    @Mapping(target = "isActive", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "city", source = "userDto.city")
    void updateEntityFromDto(@MappingTarget User user, UserDto userDto);
}