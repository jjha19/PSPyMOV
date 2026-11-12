package model;

import lombok.Data;

import java.util.List;

@Data
public class Biblioteca {
    String nombre;
    int capacidad;
    List<Libro> libros;

    public Biblioteca(String nombre, int capacidad) {
        this.nombre = nombre;
        this.capacidad = capacidad;
        this.libros = new java.util.ArrayList<>();
    }

    public void anadirLibro(String titulo, String autor) {
        libros.add(new Libro(titulo, autor));
    }

    public String consultarLibro(String titulo) {
        for (Libro libro : libros) {
            if (libro.getTitulo().equalsIgnoreCase(titulo)) {
                return libro.getAutor();
            }
        }
        return null;
    }
}
