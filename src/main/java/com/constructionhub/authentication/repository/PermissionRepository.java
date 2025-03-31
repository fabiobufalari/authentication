package com.constructionhub.authentication.repository;

import com.constructionhub.authentication.model.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

// Marca explicitamente como componente de repositório para clareza e boas práticas
@Repository
public interface PermissionRepository extends JpaRepository<Permission, Long> {

    // Busca uma permissão pelo nome (caso sensível)
    Optional<Permission> findByName(String name);
}
