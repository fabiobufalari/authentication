package com.constructionhub.authentication.repository;


import com.constructionhub.authentication.entity.ClientApplicationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ClientAppRepository extends JpaRepository<ClientApplicationEntity, UUID> {

    Optional<ClientApplicationEntity> findByClientId(String clientId);
    
    List<ClientApplicationEntity> findByOwnerId(UUID ownerId);
    
    boolean existsByName(String name);
    
    boolean existsByClientId(String clientId);
}