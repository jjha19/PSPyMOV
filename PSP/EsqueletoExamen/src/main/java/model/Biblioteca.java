package model;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;
@Data
public class Biblioteca {
    private String nombre;
    private List<Partida> partidas;

    public  Biblioteca(String nombre) {
        this.nombre = nombre;
        partidas = new ArrayList<>();
    }
}
