package com.constructionhub.authentication.service;

import com.constructionhub.authentication.entity.ClientApplicationEntity;
import com.constructionhub.authentication.entity.UserEntity;
import com.constructionhub.authentication.exception.ApiException;
import com.constructionhub.authentication.repository.ClientAppRepository;
import com.constructionhub.authentication.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.HashSet;
import java.util.List;
import java.util.UUID;

@Service
public class ClientAppService {

    private static final Logger log = LoggerFactory.getLogger(ClientAppService.class);

    private final ClientAppRepository clientAppRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public ClientAppService(ClientAppRepository clientAppRepository,
                            UserRepository userRepository,
                            PasswordEncoder passwordEncoder) {
        this.clientAppRepository = clientAppRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<ClientApplicationEntity> getAllClientApps() {
        log.debug("Fetching all client applications.");
        return clientAppRepository.findAll();
    }

    @Transactional(readOnly = true)
    public ClientApplicationEntity getClientAppById(UUID id) {
        log.debug("Fetching client application by ID: {}", id);
        return clientAppRepository.findById(id)
                .orElseThrow(() -> new ApiException("client.notFoundById", new Object[]{id}, HttpStatus.NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public ClientApplicationEntity getClientAppByClientId(String clientId) {
        log.debug("Fetching client application by client ID: {}", clientId);
        return clientAppRepository.findByClientId(clientId)
                .orElseThrow(() -> new ApiException("client.notFoundByClientId", new Object[]{clientId}, HttpStatus.NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public List<ClientApplicationEntity> getClientAppsByOwnerId(UUID ownerId) {
        log.debug("Fetching client applications by owner user ID: {}", ownerId);
        // Primeiro, verifica se o UserEntity (owner) existe.
        // Isso não é estritamente necessário se o repositório já lida com isso,
        // mas pode fornecer uma mensagem de erro mais clara se o owner não for encontrado.
        if (!userRepository.existsById(ownerId)) {
            throw new ApiException("user.notFound", new Object[]{ownerId}, HttpStatus.NOT_FOUND);
        }
        // A chamada CORRETA para o método do repositório que definimos
        return clientAppRepository.findByOwner_Id(ownerId);
    }


    @Transactional
    public ClientApplicationEntity createClientApp(ClientApplicationEntity clientApp, UUID ownerId) {
        log.info("Creating new client application: {}", clientApp.getApplicationName());
        if (!StringUtils.hasText(clientApp.getApplicationName())) {
             throw new ApiException("client.name.notBlank", null, HttpStatus.BAD_REQUEST);
        }
        if (clientAppRepository.existsByApplicationName(clientApp.getApplicationName())) {
            throw new ApiException("client.nameExists", new Object[]{clientApp.getApplicationName()}, HttpStatus.CONFLICT);
        }

        UserEntity owner = null;
        if (ownerId != null) {
            owner = userRepository.findById(ownerId)
                    .orElseThrow(() -> new ApiException("user.notFound", new Object[]{ownerId}, HttpStatus.NOT_FOUND));
        }

        if (!StringUtils.hasText(clientApp.getClientId())) {
            clientApp.setClientId(UUID.randomUUID().toString());
        } else if (clientAppRepository.existsByClientId(clientApp.getClientId())) {
             throw new ApiException("client.clientIdExists", new Object[]{clientApp.getClientId()}, HttpStatus.CONFLICT);
        }

        if (!StringUtils.hasText(clientApp.getClientSecret())) {
            throw new ApiException("client.secret.notBlank", null, HttpStatus.BAD_REQUEST);
        }
        clientApp.setClientSecret(passwordEncoder.encode(clientApp.getClientSecret()));

        clientApp.setOwner(owner);
        clientApp.setEnabled(true);

        if (clientApp.getScopes() == null) clientApp.setScopes(new HashSet<>());
        if (clientApp.getAuthorizedGrantTypes() == null) clientApp.setAuthorizedGrantTypes(new HashSet<>());
        if (clientApp.getRedirectUris() == null) clientApp.setRedirectUris(new HashSet<>());
        if (clientApp.getAllowedOrigins() == null) clientApp.setAllowedOrigins(new HashSet<>());

        ClientApplicationEntity savedClientApp = clientAppRepository.save(clientApp);
        log.info("Client application '{}' created with ID: {} and Client ID: {}", savedClientApp.getApplicationName(), savedClientApp.getId(), savedClientApp.getClientId());
        return savedClientApp;
    }

    @Transactional
    public ClientApplicationEntity updateClientApp(UUID id, ClientApplicationEntity clientAppDetails) {
        log.info("Updating client application with ID: {}", id);
        ClientApplicationEntity clientApp = clientAppRepository.findById(id)
                .orElseThrow(() -> new ApiException("client.notFoundById", new Object[]{id}, HttpStatus.NOT_FOUND));

        if (StringUtils.hasText(clientAppDetails.getApplicationName()) &&
            !clientApp.getApplicationName().equals(clientAppDetails.getApplicationName())) {
            if (clientAppRepository.existsByApplicationName(clientAppDetails.getApplicationName())) {
                 throw new ApiException("client.nameExists", new Object[]{clientAppDetails.getApplicationName()}, HttpStatus.CONFLICT);
            }
            clientApp.setApplicationName(clientAppDetails.getApplicationName());
        }

        clientApp.setDescription(clientAppDetails.getDescription());
        if (clientAppDetails.getRedirectUris() != null) clientApp.setRedirectUris(new HashSet<>(clientAppDetails.getRedirectUris()));
        if (clientAppDetails.getAllowedOrigins() != null) clientApp.setAllowedOrigins(new HashSet<>(clientAppDetails.getAllowedOrigins()));
        if (clientAppDetails.getScopes() != null) clientApp.setScopes(new HashSet<>(clientAppDetails.getScopes()));
        if (clientAppDetails.getAuthorizedGrantTypes() != null) clientApp.setAuthorizedGrantTypes(new HashSet<>(clientAppDetails.getAuthorizedGrantTypes()));

        clientApp.setEnabled(clientAppDetails.isEnabled());

        ClientApplicationEntity updatedClientApp = clientAppRepository.save(clientApp);
        log.info("Client application ID {} updated successfully.", updatedClientApp.getId());
        return updatedClientApp;
    }

    @Transactional
    public ClientApplicationEntity regenerateClientSecret(UUID id) {
        log.info("Regenerating client secret for client application ID: {}", id);
        ClientApplicationEntity clientApp = clientAppRepository.findById(id)
                .orElseThrow(() -> new ApiException("client.notFoundById", new Object[]{id}, HttpStatus.NOT_FOUND));

        String newSecret = UUID.randomUUID().toString();
        clientApp.setClientSecret(passwordEncoder.encode(newSecret));
        ClientApplicationEntity savedClientApp = clientAppRepository.save(clientApp);
        log.info("Client secret regenerated for client application ID {}.", id);
        // Não logar ou retornar `newSecret` em produção
        return savedClientApp;
    }

    @Transactional
    public void deleteClientApp(UUID id) {
        log.info("Deleting client application with ID: {}", id);
        if (!clientAppRepository.existsById(id)) {
            throw new ApiException("client.notFoundById", new Object[]{id}, HttpStatus.NOT_FOUND);
        }
        clientAppRepository.deleteById(id);
        log.info("Client application ID {} deleted successfully.", id);
    }
}