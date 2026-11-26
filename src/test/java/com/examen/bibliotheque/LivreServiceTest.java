package com.examen.bibliotheque;

import com.examen.bibliotheque.model.Livre;
import com.examen.bibliotheque.service.LivreService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LivreServiceTest {

    private LivreService service;

    @BeforeEach
    void setUp() {
        service = new LivreService();
    }

    @Test
    void listeContientLesLivresInitiaux() {
        assertEquals(3, service.lister().size());
    }

    @Test
    void ajouterAttribueUnId() {
        Livre livre = service.ajouter(new Livre(null, "Germinal", "Emile Zola", 1885));
        assertNotNull(livre.getId());
        assertTrue(service.trouver(livre.getId()).isPresent());
    }

    @Test
    void supprimerLivre() {
        assertTrue(service.supprimer(1L));
        assertFalse(service.supprimer(1L));
    }

    @Test
    void emprunterPuisRendre() {
        assertTrue(service.emprunter(1L));
        assertFalse(service.emprunter(1L));
        assertTrue(service.rendre(1L));
        assertFalse(service.rendre(1L));
    }

    @Test
    void emprunterLivreInexistant() {
        assertFalse(service.emprunter(999L));
        assertFalse(service.rendre(999L));
    }
}
