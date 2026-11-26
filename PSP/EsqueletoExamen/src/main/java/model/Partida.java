package model;

import lombok.Data;

@Data
public class Partida {
    String palabra;
    String[] adivinado;
    String contrasena;
    Jugador jugador1;
    Jugador jugador2;
    boolean turnoJug1;
    int errores;
    boolean finalizado;
    int rondas;
    String ganador;

    public Partida(String palabra, String contrasena,  Jugador jugador1, Jugador jugador2) {
        this.palabra = palabra;
        this.adivinado = new String[palabra.length()];
        for (int i = 0; i < palabra.length(); i++) {
            adivinado[i] = "_";
        }
        this.contrasena = contrasena;
        this.jugador1 = jugador1;
        this.jugador2 = jugador2;
        this.errores = 0;
        this.finalizado = false;
        this.rondas = 0;
        this.ganador = "";

    }

}
