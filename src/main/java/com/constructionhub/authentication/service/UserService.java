package com.constructionhub.authentication.service;

import com.constructionhub.authentication.dto.UserDto;
import com.constructionhub.authentication.entity.Role;
import com.constructionhub.authentication.entity.User;
import com.constructionhub.authentication.exception.ApiException;
import com.constructionhub.authentication.repository.RoleRepository;
import com.constructionhub.authentication.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(
            UserRepository userRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Page<UserDto> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable)
                .map(this::mapToDto);
    }

    public UserDto getUserById(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ApiException("user.notFound", null, HttpStatus.NOT_FOUND));
        return mapToDto(user);
    }

    public UserDto getUserByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ApiException("user.notFound", null, HttpStatus.NOT_FOUND));
        return mapToDto(user);
    }

    @Transactional
    public UserDto updateUser(UUID id, UserDto userDto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ApiException("user.notFound", null, HttpStatus.NOT_FOUND));

        // Atualizar informações básicas
        if (userDto.getFirstName() != null) {
            user.setFirstName(userDto.getFirstName());
        }
        if (userDto.getLastName() != null) {
            user.setLastName(userDto.getLastName());
        }
        if (userDto.getIsEnabled() != null) {
            user.setIsEnabled(userDto.getIsEnabled());
        }

        // Salvar e retornar
        return mapToDto(userRepository.save(user));
    }

    @Transactional
    public void deleteUser(UUID id) {
        if (!userRepository.existsById(id)) {
            throw new ApiException("user.notFound", null, HttpStatus.NOT_FOUND);
        }
        userRepository.deleteById(id);
    }

    @Transactional
    public UserDto addRoleToUser(UUID userId, String roleName) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException("user.notFound", null, HttpStatus.NOT_FOUND));

        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new ApiException("role.notFound", null, HttpStatus.NOT_FOUND));

        user.getRoles().add(role);
        return mapToDto(userRepository.save(user));
    }

    @Transactional
    public UserDto removeRoleFromUser(UUID userId, String roleName) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException("user.notFound", null, HttpStatus.NOT_FOUND));

        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new ApiException("role.notFound", null, HttpStatus.NOT_FOUND));

        user.getRoles().removeIf(r -> r.getName().equals(roleName));
        return mapToDto(userRepository.save(user));
    }

    @Transactional
    public void changePassword(UUID userId, String currentPassword, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException("user.notFound", null, HttpStatus.NOT_FOUND));

        // Verificar senha atual
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new ApiException("auth.invalidPassword", null, HttpStatus.BAD_REQUEST);
        }

        // Atualizar senha
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    private UserDto mapToDto(User user) {
        return UserDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .isEnabled(user.getIsEnabled())
                .roles(user.getRoles().stream()
                        .map(Role::getName)
                        .collect(Collectors.toSet()))
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}