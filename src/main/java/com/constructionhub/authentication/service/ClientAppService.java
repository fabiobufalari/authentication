package com.constructionhub.authentication.service;


import com.constructionhub.authentication.entity.ClientApplicationEntity;
import com.constructionhub.authentication.entity.UserEntity;
import com.constructionhub.authentication.exception.ApiException;
import com.constructionhub.authentication.repository.ClientAppRepository;
import com.constructionhub.authentication.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class ClientAppService {

    private final ClientAppRepository clientAppRepository;
    private final UserRepository userRepository;

    public ClientAppService(ClientAppRepository clientAppRepository, UserRepository userRepository) {
        this.clientAppRepository = clientAppRepository;
        this.userRepository = userRepository;
    }

    public List<ClientApplicationEntity> getAllClientApps() {
        return clientAppRepository.findAll();
    }

    public ClientApplicationEntity getClientAppById(UUID id) {
        return clientAppRepository.findById(id)
                .orElseThrow(() -> new ApiException("client.notFound", null, HttpStatus.NOT_FOUND));
    }

    public ClientApplicationEntity getClientAppByClientId(String clientId) {
        return clientAppRepository.findByClientId(clientId)
                .orElseThrow(() -> new ApiException("client.notFound", null, HttpStatus.NOT_FOUND));
    }

    public List<ClientApplicationEntity> getClientAppsByOwnerId(UUID ownerId) {
        return clientAppRepository.findByOwnerId(ownerId);
    }

    @Transactional
    public ClientApplicationEntity createClientApp(ClientApplicationEntity clientApp, UUID ownerId) {
        // Verificar se já existe aplicação com esse nome
        if (clientAppRepository.existsByName(clientApp.getName())) {
            throw new ApiException("client.nameExists", null, HttpStatus.CONFLICT);
        }

        // Verificar se o usuário existe
        UserEntity owner = null;
        if (ownerId != null) {
            owner = userRepository.findById(ownerId)
                    .orElseThrow(() -> new ApiException("user.notFound", null, HttpStatus.NOT_FOUND));
        }

        // Gerar client_id e client_secret
        clientApp.setClientId(UUID.randomUUID().toString());
        clientApp.setClientSecret(UUID.randomUUID().toString());
        clientApp.setOwner(owner);
        clientApp.setIsEnabled(true);

        return clientAppRepository.save(clientApp);
    }

    @Transactional
    public ClientApplicationEntity updateClientApp(UUID id, ClientApplicationEntity clientAppDetails) {
        ClientApplicationEntity clientApp = clientAppRepository.findById(id)
                .orElseThrow(() -> new ApiException("client.notFound", null, HttpStatus.NOT_FOUND));

        // Atualizar informações básicas
        clientApp.setDescription(clientAppDetails.getDescription());
        clientApp.setRedirectUris(clientAppDetails.getRedirectUris());
        clientApp.setAllowedOrigins(clientAppDetails.getAllowedOrigins());
        clientApp.setIsEnabled(clientAppDetails.getIsEnabled());

        return clientAppRepository.save(clientApp);
    }

    @Transactional
    public ClientApplicationEntity regenerateClientSecret(UUID id) {
        ClientApplicationEntity clientApp = clientAppRepository.findById(id)
                .orElseThrow(() -> new ApiException("client.notFound", null, HttpStatus.NOT_FOUND));

        clientApp.setClientSecret(UUID.randomUUID().toString());
        return clientAppRepository.save(clientApp);
    }

    @Transactional
    public void deleteClientApp(UUID id) {
        if (!clientAppRepository.existsById(id)) {
            throw new ApiException("client.notFound", null, HttpStatus.NOT_FOUND);
        }
        clientAppRepository.deleteById(id);
    }
}