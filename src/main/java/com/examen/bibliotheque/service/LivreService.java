package com.examen.bibliotheque.service;

import com.examen.bibliotheque.model.Livre;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class LivreService {

    private final Map<Long, Livre> livres = new ConcurrentHashMap<>();
    private final AtomicLong sequence = new AtomicLong();

    public LivreService() {
        ajouter(new Livre(null, "Le Petit Prince", "Antoine de Saint-Exupery", 1943));
        ajouter(new Livre(null, "L'Etranger", "Albert Camus", 1942));
        ajouter(new Livre(null, "Les Miserables", "Victor Hugo", 1862));
    }

    public List<Livre> lister() {
        return new ArrayList<>(livres.values());
    }

    public Optional<Livre> trouver(Long id) {
        return Optional.ofNullable(livres.get(id));
    }

    public final Livre ajouter(Livre livre) {
        long id = sequence.incrementAndGet();
        livre.setId(id);
        livres.put(id, livre);
        return livre;
    }

    public boolean supprimer(Long id) {
        return livres.remove(id) != null;
    }

    /** Emprunte un livre : retourne false s'il est introuvable ou deja emprunte. */
    public boolean emprunter(Long id) {
        Livre livre = livres.get(id);
        if (livre == null || !livre.isDisponible()) {
            return false;
        }
        livre.setDisponible(false);
        return true;
    }

    /** Rend un livre : retourne false s'il est introuvable ou deja disponible. */
    public boolean rendre(Long id) {
        Livre livre = livres.get(id);
        if (livre == null || livre.isDisponible()) {
            return false;
        }
        livre.setDisponible(true);
        return true;
    }
}
