package com.constructionhub.authentication.controller;

import com.constructionhub.authentication.model.Role;
import com.constructionhub.authentication.service.RoleService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/roles")
@CrossOrigin(origins = "*") // Libera o acesso de outros domínios — ajuste conforme sua necessidade
public class RoleController {

    private final RoleService roleService;

    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    /**
     * Cria uma nova role.
     * Create a new role.
     */
    @PostMapping
    public ResponseEntity<Role> createRole(@RequestBody Role role) {
        Role created = roleService.createRole(role);
        return ResponseEntity.ok(created);
    }

    /**
     * Lista todas as roles existentes.
     * List all roles.
     */
    @GetMapping
    public ResponseEntity<List<Role>> getAllRoles() {
        List<Role> roles = roleService.getAllRoles();
        return ResponseEntity.ok(roles);
    }

    /**
     * Busca uma role pelo ID.
     * Get role by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Role> getRoleById(@PathVariable Long id) {
        return roleService.getRoleById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Deleta uma role pelo ID.
     * Delete role by ID.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRole(@PathVariable Long id) {
        roleService.deleteRole(id);
        return ResponseEntity.noContent().build();
    }
}
