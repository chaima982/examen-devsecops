package com.examen.bibliotheque;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.actuate.observability.AutoConfigureObservability;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureObservability
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class LivreControllerTest {

    @Autowired
    private MockMvc mvc;

    @Test
    void accueil() throws Exception {
        mvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Bibliotheque")));
    }

    @Test
    void listerLivres() throws Exception {
        mvc.perform(get("/api/livres"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3));
    }

    @Test
    void trouverLivre() throws Exception {
        mvc.perform(get("/api/livres/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titre").value("Le Petit Prince"));
    }

    @Test
    void livreIntrouvable() throws Exception {
        mvc.perform(get("/api/livres/999")).andExpect(status().isNotFound());
        mvc.perform(delete("/api/livres/999")).andExpect(status().isNotFound());
    }

    @Test
    void supprimerLivre() throws Exception {
        mvc.perform(delete("/api/livres/3")).andExpect(status().isNoContent());
    }

    @Test
    void ajouterLivreValide() throws Exception {
        mvc.perform(post("/api/livres")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"titre\":\"Candide\",\"auteur\":\"Voltaire\",\"annee\":1759}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists());
    }

    @Test
    void ajouterLivreInvalideRetourne400() throws Exception {
        mvc.perform(post("/api/livres")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"titre\":\"\",\"auteur\":\"\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void emprunterEtRendre() throws Exception {
        mvc.perform(post("/api/livres/2/emprunter")).andExpect(status().isOk());
        mvc.perform(post("/api/livres/2/emprunter")).andExpect(status().isConflict());
        mvc.perform(post("/api/livres/2/rendre")).andExpect(status().isOk());
        mvc.perform(post("/api/livres/2/rendre")).andExpect(status().isConflict());
    }

    @Test
    void endpointsMonitoringExposes() throws Exception {
        mvc.perform(get("/actuator/health")).andExpect(status().isOk());
        mvc.perform(get("/actuator/prometheus")).andExpect(status().isOk());
    }
}
