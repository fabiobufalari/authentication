package com.constructionhub.authentication.controller;


import com.constructionhub.authentication.dto.UserDto;
import com.constructionhub.authentication.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/users")
@Tag(name = "User Management", description = "API para gerenciamento de usuários")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    @Operation(summary = "Listar usuários", description = "Retorna todos os usuários paginados")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<UserDto>> getAllUsers(Pageable pageable) {
        return ResponseEntity.ok(userService.getAllUsers(pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obter usuário por ID", description = "Retorna um usuário pelo ID")
    @PreAuthorize("hasRole('ADMIN') or authentication.principal.username == @userService.getUserById(#id).username")
    public ResponseEntity<UserDto> getUserById(@PathVariable UUID id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @GetMapping("/username/{username}")
    @Operation(summary = "Obter usuário por username", description = "Retorna um usuário pelo username")
    @PreAuthorize("hasRole('ADMIN') or authentication.principal.username == #username")
    public ResponseEntity<UserDto> getUserByUsername(@PathVariable String username) {
        return ResponseEntity.ok(userService.getUserByUsername(username));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar usuário", description = "Atualiza as informações de um usuário")
    @PreAuthorize("hasRole('ADMIN') or authentication.principal.username == @userService.getUserById(#id).username")
    public ResponseEntity<UserDto> updateUser(@PathVariable UUID id, @Valid @RequestBody UserDto userDto) {
        return ResponseEntity.ok(userService.updateUser(id, userDto));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir usuário", description = "Exclui um usuário pelo ID")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable UUID id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/roles/{role}")
    @Operation(summary = "Adicionar role ao usuário", description = "Adiciona uma role a um usuário")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDto> addRoleToUser(@PathVariable UUID id, @PathVariable String role) {
        return ResponseEntity.ok(userService.addRoleToUser(id, role));
    }

    @DeleteMapping("/{id}/roles/{role}")
    @Operation(summary = "Remover role do usuário", description = "Remove uma role de um usuário")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDto> removeRoleFromUser(@PathVariable UUID id, @PathVariable String role) {
        return ResponseEntity.ok(userService.removeRoleFromUser(id, role));
    }

    @PutMapping("/{id}/password")
    @Operation(summary = "Alterar senha", description = "Altera a senha de um usuário")
    @PreAuthorize("hasRole('ADMIN') or authentication.principal.username == @userService.getUserById(#id).username")
    public ResponseEntity<Void> changePassword(
            @PathVariable UUID id,
            @RequestParam String currentPassword,
            @RequestParam String newPassword) {
        userService.changePassword(id, currentPassword, newPassword);
        return ResponseEntity.noContent().build();
    }
}