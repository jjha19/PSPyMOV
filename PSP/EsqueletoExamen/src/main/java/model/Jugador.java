package model;

import lombok.Data;


@Data
public class Jugador {
    String nombre;
    int partidasGanadas;


    public Jugador(String nombre) {
        this.nombre = nombre;
    }

}
