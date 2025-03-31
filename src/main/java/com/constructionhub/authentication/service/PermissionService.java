package com.constructionhub.authentication.service;

import com.constructionhub.authentication.model.Permission;
import com.constructionhub.authentication.repository.PermissionRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PermissionService {

    private final PermissionRepository permissionRepository;

    public PermissionService(PermissionRepository permissionRepository) {
        this.permissionRepository = permissionRepository;
    }

    /**
     * Cria uma nova permissão.
     * @param permission Objeto de permissão a ser salvo
     * @return Permissão criada
     */
    public Permission createPermission(Permission permission) {
        return permissionRepository.save(permission);
    }

    /**
     * Busca uma permissão pelo nome.
     * @param name Nome da permissão
     * @return Optional contendo a permissão, se encontrada
     */
    public Optional<Permission> getPermissionByName(String name) {
        return permissionRepository.findByName(name);
    }

    /**
     * Lista todas as permissões do sistema.
     * @return Lista de permissões
     */
    public List<Permission> getAllPermissions() {
        return permissionRepository.findAll();
    }

    /**
     * Deleta uma permissão pelo ID.
     * @param id ID da permissão
     */
    public void deletePermission(Long id) {
        permissionRepository.deleteById(id);
    }
}
