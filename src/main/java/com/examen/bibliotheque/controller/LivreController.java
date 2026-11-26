package com.examen.bibliotheque.controller;

import com.examen.bibliotheque.model.Livre;
import com.examen.bibliotheque.service.LivreService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/livres")
public class LivreController {

    private final LivreService service;

    public LivreController(LivreService service) {
        this.service = service;
    }

    @GetMapping
    public List<Livre> lister() {
        return service.lister();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Livre> trouver(@PathVariable Long id) {
        return service.trouver(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Livre> ajouter(@Valid @RequestBody Livre livre) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.ajouter(livre));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> supprimer(@PathVariable Long id) {
        return service.supprimer(id)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }

    @PostMapping("/{id}/emprunter")
    public ResponseEntity<Void> emprunter(@PathVariable Long id) {
        return service.emprunter(id)
                ? ResponseEntity.ok().build()
                : ResponseEntity.status(HttpStatus.CONFLICT).build();
    }

    @PostMapping("/{id}/rendre")
    public ResponseEntity<Void> rendre(@PathVariable Long id) {
        return service.rendre(id)
                ? ResponseEntity.ok().build()
                : ResponseEntity.status(HttpStatus.CONFLICT).build();
    }
}
