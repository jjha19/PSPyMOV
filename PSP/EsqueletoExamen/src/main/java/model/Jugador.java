package model;

import lombok.Data;

import java.util.HashMap;

@Data
public class Jugador {
    String nombre;
    int numeroTirada;
    int casillasAvanzadas;
    HashMap<String, Integer> historial;

    public Jugador(String nombre) {
        this.nombre = nombre;
        this.numeroTirada = 0;
        this.casillasAvanzadas = 0;
        this.historial = new HashMap<>();
    }

}
