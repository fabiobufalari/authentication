package com.constructionhub.authentication.service;

import com.constructionhub.authentication.dto.UserDTO;
import com.constructionhub.authentication.entity.RoleEntity;
import com.constructionhub.authentication.entity.UserEntity;
import com.constructionhub.authentication.exception.ApiException;
import com.constructionhub.authentication.repository.RoleRepository;
import com.constructionhub.authentication.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    public Page<UserDTO> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable)
                .map(this::mapToDto);
    }

    public UserDTO getUserById(UUID id) {
        UserEntity userEntity = userRepository.findById(id)
                .orElseThrow(() -> new ApiException("user.notFound", null, HttpStatus.NOT_FOUND));
        return mapToDto(userEntity);
    }

    public UserDTO getUserByUsername(String username) {
        UserEntity userEntity = userRepository.findByUsername(username)
                .orElseThrow(() -> new ApiException("user.notFound", null, HttpStatus.NOT_FOUND));
        return mapToDto(userEntity);
    }

    @Transactional
    public UserDTO updateUser(UUID id, UserDTO userDto) {
        UserEntity userEntity = userRepository.findById(id)
                .orElseThrow(() -> new ApiException("user.notFound", null, HttpStatus.NOT_FOUND));

        // Atualizar informações básicas
        if (userDto.getFirstName() != null) {
            userEntity.setFirstName(userDto.getFirstName());
        }
        if (userDto.getLastName() != null) {
            userEntity.setLastName(userDto.getLastName());
        }
        if (userDto.getIsEnabled() != null) {
            userEntity.setIsEnabled(userDto.getIsEnabled());
        }

        // Salvar e retornar
        return mapToDto(userRepository.save(userEntity));
    }

    @Transactional
    public void deleteUser(UUID id) {
        if (!userRepository.existsById(id)) {
            throw new ApiException("user.notFound", null, HttpStatus.NOT_FOUND);
        }
        userRepository.deleteById(id);
    }

    @Transactional
    public UserDTO addRoleToUser(UUID userId, String roleName) {
        UserEntity userEntity = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException("user.notFound", null, HttpStatus.NOT_FOUND));

        RoleEntity roleEntity = roleRepository.findByName(roleName)
                .orElseThrow(() -> new ApiException("role.notFound", null, HttpStatus.NOT_FOUND));

        userEntity.getRoleEntities().add(roleEntity);
        return mapToDto(userRepository.save(userEntity));
    }

    @Transactional
    public UserDTO removeRoleFromUser(UUID userId, String roleName) {
        UserEntity userEntity = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException("user.notFound", null, HttpStatus.NOT_FOUND));

        RoleEntity roleEntity = roleRepository.findByName(roleName)
                .orElseThrow(() -> new ApiException("role.notFound", null, HttpStatus.NOT_FOUND));

        userEntity.getRoleEntities().removeIf(r -> r.getName().equals(roleName));
        return mapToDto(userRepository.save(userEntity));
    }

    @Transactional
    public void changePassword(UUID userId, String currentPassword, String newPassword) {
        UserEntity userEntity = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException("user.notFound", null, HttpStatus.NOT_FOUND));

        // Verificar senha atual
        if (!passwordEncoder.matches(currentPassword, userEntity.getPassword())) {
            throw new ApiException("auth.invalidPassword", null, HttpStatus.BAD_REQUEST);
        }

        // Atualizar senha
        userEntity.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(userEntity);
    }

    private UserDTO mapToDto(UserEntity userEntity) {
        return UserDTO.builder()
                .id(userEntity.getId())
                .username(userEntity.getUsername())
                .email(userEntity.getEmail())
                .firstName(userEntity.getFirstName())
                .lastName(userEntity.getLastName())
                .isEnabled(userEntity.getIsEnabled())
                .roles(userEntity.getRoleEntities().stream()
                        .map(RoleEntity::getName)
                        .collect(Collectors.toSet()))
                .createdAt(userEntity.getCreatedAt())
                .updatedAt(userEntity.getUpdatedAt())
                .build();
    }
}