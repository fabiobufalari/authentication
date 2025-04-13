package com.constructionhub.authentication.service;


import com.constructionhub.authentication.entity.ClientApplication;
import com.constructionhub.authentication.entity.User;
import com.constructionhub.authentication.exception.ApiException;
import com.constructionhub.authentication.repository.ClientAppRepository;
import com.constructionhub.authentication.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ClientAppService {

    private final ClientAppRepository clientAppRepository;
    private final UserRepository userRepository;

    public ClientAppService(ClientAppRepository clientAppRepository, UserRepository userRepository) {
        this.clientAppRepository = clientAppRepository;
        this.userRepository = userRepository;
    }

    public List<ClientApplication> getAllClientApps() {
        return clientAppRepository.findAll();
    }

    public ClientApplication getClientAppById(UUID id) {
        return clientAppRepository.findById(id)
                .orElseThrow(() -> new ApiException("client.notFound", null, HttpStatus.NOT_FOUND));
    }

    public ClientApplication getClientAppByClientId(String clientId) {
        return clientAppRepository.findByClientId(clientId)
                .orElseThrow(() -> new ApiException("client.notFound", null, HttpStatus.NOT_FOUND));
    }

    public List<ClientApplication> getClientAppsByOwnerId(UUID ownerId) {
        return clientAppRepository.findByOwnerId(ownerId);
    }

    @Transactional
    public ClientApplication createClientApp(ClientApplication clientApp, UUID ownerId) {
        // Verificar se já existe aplicação com esse nome
        if (clientAppRepository.existsByName(clientApp.getName())) {
            throw new ApiException("client.nameExists", null, HttpStatus.CONFLICT);
        }

        // Verificar se o usuário existe
        User owner = null;
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
    public ClientApplication updateClientApp(UUID id, ClientApplication clientAppDetails) {
        ClientApplication clientApp = clientAppRepository.findById(id)
                .orElseThrow(() -> new ApiException("client.notFound", null, HttpStatus.NOT_FOUND));

        // Atualizar informações básicas
        clientApp.setDescription(clientAppDetails.getDescription());
        clientApp.setRedirectUris(clientAppDetails.getRedirectUris());
        clientApp.setAllowedOrigins(clientAppDetails.getAllowedOrigins());
        clientApp.setIsEnabled(clientAppDetails.getIsEnabled());

        return clientAppRepository.save(clientApp);
    }

    @Transactional
    public ClientApplication regenerateClientSecret(UUID id) {
        ClientApplication clientApp = clientAppRepository.findById(id)
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