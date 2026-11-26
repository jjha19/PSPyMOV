package org.example.primerspring;

import org.example.primerspring.domain.Cosita;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/primer")
public class Primero {

    @RequestMapping("/uno")
    public Cosita mostrarPagina(Model model) {
        return new Cosita("Pepe", 1);
    }

    @RequestMapping
    public List<Cosita> mostrarCositas() {
        return List.of(new Cosita("Pepe", 1), new Cosita("María", 2), new Cosita("Juan", 3));
    }

    @RequestMapping("/{id}")
    public Cosita mostrarCositaPorId(@PathVariable String id) {
        return new Cosita("Cosita con ID: " + id, Integer.parseInt(id));
    }

}
