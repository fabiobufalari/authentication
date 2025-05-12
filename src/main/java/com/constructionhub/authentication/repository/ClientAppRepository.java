package com.constructionhub.authentication.repository;// em com.constructionhub.authentication.repository.ClientAppRepository.java

import com.constructionhub.authentication.entity.ClientApplicationEntity;
// import com.constructionhub.authentication.entity.UserEntity; // Removido se não usar findByOwner(UserEntity owner)
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ClientAppRepository extends JpaRepository<ClientApplicationEntity, UUID> {

    Optional<ClientApplicationEntity> findByClientId(String clientId);

    // Método para buscar pelo ID do UserEntity referenciado no campo 'owner'
    List<ClientApplicationEntity> findByOwner_Id(UUID ownerId);
    // Se você quisesse buscar pelo objeto UserEntity completo:
    // List<ClientApplicationEntity> findByOwner(UserEntity owner);

    boolean existsByApplicationName(String applicationName);

    boolean existsByClientId(String clientId);
}