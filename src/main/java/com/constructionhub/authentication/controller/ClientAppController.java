package com.constructionhub.authentication.controller;


import com.constructionhub.authentication.entity.ClientApplicationEntity;
import com.constructionhub.authentication.service.ClientAppService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/clients")
@Tag(name = "Client Applications", description = "API para gerenciamento de aplicações cliente")
@SecurityRequirement(name = "bearerAuth")
public class ClientAppController {

    private final ClientAppService clientAppService;

    public ClientAppController(ClientAppService clientAppService) {
        this.clientAppService = clientAppService;
    }

    @GetMapping
    @Operation(summary = "Listar aplicações cliente", description = "Retorna todas as aplicações cliente")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ClientApplicationEntity>> getAllClientApps() {
        return ResponseEntity.ok(clientAppService.getAllClientApps());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obter aplicação cliente por ID", description = "Retorna uma aplicação cliente pelo ID")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ClientApplicationEntity> getClientAppById(@PathVariable UUID id) {
        return ResponseEntity.ok(clientAppService.getClientAppById(id));
    }

    @GetMapping("/client-id/{clientId}")
    @Operation(summary = "Obter aplicação cliente por Client ID", description = "Retorna uma aplicação cliente pelo Client ID")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ClientApplicationEntity> getClientAppByClientId(@PathVariable String clientId) {
        return ResponseEntity.ok(clientAppService.getClientAppByClientId(clientId));
    }

    @GetMapping("/owner/{ownerId}")
    @Operation(summary = "Listar aplicações cliente por proprietário", description = "Retorna todas as aplicações cliente de um proprietário")
    @PreAuthorize("hasRole('ADMIN') or authentication.principal.id == #ownerId")
    public ResponseEntity<List<ClientApplicationEntity>> getClientAppsByOwnerId(@PathVariable UUID ownerId) {
        return ResponseEntity.ok(clientAppService.getClientAppsByOwnerId(ownerId));
    }

    @PostMapping
    @Operation(summary = "Criar aplicação cliente", description = "Cria uma nova aplicação cliente")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ClientApplicationEntity> createClientApp(
            @Valid @RequestBody ClientApplicationEntity clientApp,
            @RequestParam(required = false) UUID ownerId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(clientAppService.createClientApp(clientApp, ownerId));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar aplicação cliente", description = "Atualiza uma aplicação cliente existente")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ClientApplicationEntity> updateClientApp(
            @PathVariable UUID id,
            @Valid @RequestBody ClientApplicationEntity clientApp) {
        return ResponseEntity.ok(clientAppService.updateClientApp(id, clientApp));
    }

    @PostMapping("/{id}/regenerate-secret")
    @Operation(summary = "Regenerar Client Secret", description = "Regenera o client secret de uma aplicação cliente")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ClientApplicationEntity> regenerateClientSecret(@PathVariable UUID id) {
        return ResponseEntity.ok(clientAppService.regenerateClientSecret(id));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir aplicação cliente", description = "Exclui uma aplicação cliente pelo ID")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteClientApp(@PathVariable UUID id) {
        clientAppService.deleteClientApp(id);
        return ResponseEntity.noContent().build();
    }
}