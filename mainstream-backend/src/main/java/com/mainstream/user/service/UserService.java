package com.mainstream.user.service;

import com.mainstream.user.dto.UserDto;
import com.mainstream.user.dto.UserRegistrationDto;
import com.mainstream.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface UserService {

    UserDto registerUser(UserRegistrationDto registrationDto);

    Optional<UserDto> findByEmail(String email);

    Optional<UserDto> findById(Long id);

    UserDto updateUser(Long id, UserDto userDto);

    void deactivateUser(Long id);

    void activateUser(Long id);

    Page<UserDto> findAllUsers(Pageable pageable);

    Page<UserDto> findPublicUsers(Pageable pageable);

    Page<UserDto> searchUsers(String searchTerm, Pageable pageable);

    long countActiveUsers();

    boolean existsByEmail(String email);

    UserDto updateRole(Long id, User.Role role);

    Page<UserDto> findUsersByRole(User.Role role, Pageable pageable);
}