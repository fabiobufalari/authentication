package com.constructionhub.authentication.repository;


import com.constructionhub.authentication.entity.ClientApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ClientAppRepository extends JpaRepository<ClientApplication, UUID> {

    Optional<ClientApplication> findByClientId(String clientId);
    
    List<ClientApplication> findByOwnerId(UUID ownerId);
    
    boolean existsByName(String name);
    
    boolean existsByClientId(String clientId);
}