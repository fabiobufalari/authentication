package com.constructionhub.authentication.controller;

import com.constructionhub.authentication.model.Permission;
import com.constructionhub.authentication.service.PermissionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/permissions")
@CrossOrigin(origins = "*") // Permite acesso de qualquer origem — pode ajustar conforme necessário
public class PermissionController {

    private final PermissionService permissionService;

    public PermissionController(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    /**
     * Cria uma nova permissão.
     * @param permission Objeto de permissão
     * @return Permissão criada
     */
    @PostMapping
    public ResponseEntity<Permission> createPermission(@RequestBody Permission permission) {
        Permission savedPermission = permissionService.createPermission(permission);
        return ResponseEntity.ok(savedPermission);
    }

    /**
     * Retorna todas as permissões do sistema.
     * @return Lista de permissões
     */
    @GetMapping
    public ResponseEntity<List<Permission>> getAllPermissions() {
        List<Permission> permissions = permissionService.getAllPermissions();
        return ResponseEntity.ok(permissions);
    }

    /**
     * Busca permissão por nome.
     * @param name Nome da permissão
     * @return Permissão (se encontrada)
     */
    @GetMapping("/search")
    public ResponseEntity<Permission> getPermissionByName(@RequestParam String name) {
        Optional<Permission> permission = permissionService.getPermissionByName(name);
        return permission.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Deleta uma permissão pelo ID.
     * @param id ID da permissão
     * @return Resposta sem conteúdo
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePermission(@PathVariable Long id) {
        permissionService.deletePermission(id);
        return ResponseEntity.noContent().build();
    }
}
