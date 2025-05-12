package com.constructionhub.authentication.service;

import com.constructionhub.authentication.dto.UserDTO;
import com.constructionhub.authentication.entity.RoleEntity;
import com.constructionhub.authentication.entity.UserEntity;
import com.constructionhub.authentication.exception.ApiException;
import com.constructionhub.authentication.repository.RoleRepository;
import com.constructionhub.authentication.repository.UserRepository;
import org.slf4j.Logger; // Adicionar Logger
import org.slf4j.LoggerFactory; // Adicionar LoggerFactory
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

    private static final Logger log = LoggerFactory.getLogger(UserService.class); // Logger

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

    @Transactional(readOnly = true)
    public Page<UserDTO> getAllUsers(Pageable pageable) {
        log.debug("Fetching all users, page: {}, size: {}", pageable.getPageNumber(), pageable.getPageSize());
        return userRepository.findAll(pageable)
                .map(this::mapToDto);
    }

    @Transactional(readOnly = true)
    public UserDTO getUserById(UUID id) {
        log.debug("Fetching user by ID: {}", id);
        UserEntity userEntity = userRepository.findById(id)
                .orElseThrow(() -> new ApiException("user.notFound", new Object[]{id}, HttpStatus.NOT_FOUND));
        return mapToDto(userEntity);
    }

    @Transactional(readOnly = true)
    public UserDTO getUserByUsername(String username) {
        log.debug("Fetching user by username: {}", username);
        UserEntity userEntity = userRepository.findByUsername(username)
                .orElseThrow(() -> new ApiException("user.notFoundByUsername", new Object[]{username}, HttpStatus.NOT_FOUND));
        return mapToDto(userEntity);
    }

    @Transactional
    public UserDTO updateUser(UUID id, UserDTO userDto) {
        log.info("Updating user with ID: {}", id);
        UserEntity userEntity = userRepository.findById(id)
                .orElseThrow(() -> new ApiException("user.notFound", new Object[]{id}, HttpStatus.NOT_FOUND));

        if (userDto.getFirstName() != null) {
            userEntity.setFirstName(userDto.getFirstName());
        }
        if (userDto.getLastName() != null) {
            userEntity.setLastName(userDto.getLastName());
        }
        // Para campos booleanos, o getter é isAlgumaCoisa() e o setter é setAlgumaCoisa()
        if (userDto.getIsEnabled() != null) {
            userEntity.setEnabled(userDto.getIsEnabled()); // <<< CORRIGIDO de setIsEnabled para setEnabled
        }

        UserEntity updatedUser = userRepository.save(userEntity);
        log.info("User ID {} updated successfully.", updatedUser.getId());
        return mapToDto(updatedUser);
    }

    @Transactional
    public void deleteUser(UUID id) {
        log.info("Deleting user with ID: {}", id);
        if (!userRepository.existsById(id)) {
            throw new ApiException("user.notFound", new Object[]{id}, HttpStatus.NOT_FOUND);
        }
        userRepository.deleteById(id);
        log.info("User ID {} deleted successfully.", id);
    }

    @Transactional
    public UserDTO addRoleToUser(UUID userId, String roleName) {
        log.info("Adding role '{}' to user ID: {}", roleName, userId);
        UserEntity userEntity = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException("user.notFound", new Object[]{userId}, HttpStatus.NOT_FOUND));

        RoleEntity roleEntity = roleRepository.findByName(roleName)
                .orElseThrow(() -> new ApiException("role.notFound", new Object[]{roleName}, HttpStatus.NOT_FOUND));

        // O getter para o campo "roles" em UserEntity é "getRoles()"
        boolean added = userEntity.getRoles().add(roleEntity); // <<< CORRIGIDO de getRoleEntities para getRoles
        if (added) {
            UserEntity updatedUser = userRepository.save(userEntity);
            log.info("Role '{}' added to user ID {}.", roleName, userId);
            return mapToDto(updatedUser);
        } else {
            log.info("Role '{}' was already assigned to user ID {}. No changes made.", roleName, userId);
            return mapToDto(userEntity); // Retorna o DTO do usuário sem salvar novamente se a role já existia
        }
    }

    @Transactional
    public UserDTO removeRoleFromUser(UUID userId, String roleName) {
        log.info("Removing role '{}' from user ID: {}", roleName, userId);
        UserEntity userEntity = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException("user.notFound", new Object[]{userId}, HttpStatus.NOT_FOUND));

        // Não é necessário buscar a RoleEntity aqui se você está apenas removendo pelo nome.
        // O getter para o campo "roles" em UserEntity é "getRoles()"
        boolean removed = userEntity.getRoles().removeIf(r -> r.getName().equals(roleName)); // <<< CORRIGIDO de getRoleEntities para getRoles

        if (removed) {
            UserEntity updatedUser = userRepository.save(userEntity);
            log.info("Role '{}' removed from user ID {}.", roleName, userId);
            return mapToDto(updatedUser);
        } else {
            log.info("Role '{}' was not assigned to user ID {}. No changes made.", roleName, userId);
            return mapToDto(userEntity); // Retorna o DTO do usuário sem salvar novamente
        }
    }

    @Transactional
    public void changePassword(UUID userId, String currentPassword, String newPassword) {
        log.info("Attempting to change password for user ID: {}", userId);
        UserEntity userEntity = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException("user.notFound", new Object[]{userId}, HttpStatus.NOT_FOUND));

        if (!passwordEncoder.matches(currentPassword, userEntity.getPassword())) {
            log.warn("Password change failed for user ID {}: Current password does not match.", userId);
            throw new ApiException("auth.invalidPassword", null, HttpStatus.BAD_REQUEST);
        }

        if (currentPassword.equals(newPassword)) {
            log.warn("Password change failed for user ID {}: New password is the same as the current password.", userId);
            throw new ApiException("auth.newPasswordSameAsOld", null, HttpStatus.BAD_REQUEST);
        }
        // Adicionar validação de complexidade para newPassword se necessário

        userEntity.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(userEntity);
        log.info("Password changed successfully for user ID: {}", userId);
    }

    private UserDTO mapToDto(UserEntity userEntity) {
        return UserDTO.builder()
                .id(userEntity.getId())
                .username(userEntity.getUsername())
                .email(userEntity.getEmail())
                .firstName(userEntity.getFirstName())
                .lastName(userEntity.getLastName())
                // O getter para o campo "enabled" (boolean) é "isEnabled()"
                .isEnabled(userEntity.isEnabled()) // <<< CORRIGIDO de getIsEnabled para isEnabled (padrão Lombok para boolean)
                // O getter para o campo "roles" é "getRoles()"
                .roles(userEntity.getRoles().stream() // <<< CORRIGIDO de getRoleEntities para getRoles
                        .map(RoleEntity::getName)
                        .collect(Collectors.toSet()))
                .createdAt(userEntity.getCreatedAt())
                .updatedAt(userEntity.getUpdatedAt())
                .build();
    }
}