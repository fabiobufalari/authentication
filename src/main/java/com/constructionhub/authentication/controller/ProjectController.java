package com.constructionhub.authentication.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/projects")
@PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
public class ProjectController {

    @GetMapping
    public ResponseEntity<List<String>> getAllProjects() {
        List<String> projects = List.of("Casa do João", "Obra Comercial", "Reforma do Cliente X");
        return ResponseEntity.ok(projects);
    }
}
