package com.constructionhub.authentication.entity;

import com.constructionhub.authentication.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;


@Component
public class DataLoader implements CommandLineRunner {

    @Autowired
    private RoleRepository roleRepository;

    @Override
    public void run(String... args) {
        // Verifica se a role "ROLE_USER" existe. Se não, cria.
        if (roleRepository.findByName("ROLE_USER").isEmpty()) {
            RoleEntity userRole = new RoleEntity();
            userRole.setName("ROLE_USER");
            userRole.setDescription("Usuário padrão");
            roleRepository.save(userRole);
        }

        // Verifica se a role "ROLE_ADMIN" existe. Se não, cria.
        if (roleRepository.findByName("ROLE_ADMIN").isEmpty()) {
            RoleEntity adminRole = new RoleEntity();
            adminRole.setName("ROLE_ADMIN");
            adminRole.setDescription("Administrador");
            roleRepository.save(adminRole);
        }
    }
}