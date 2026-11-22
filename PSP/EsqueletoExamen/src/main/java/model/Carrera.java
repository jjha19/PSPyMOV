package model;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data

public class Carrera {
    int longitud;
    List<Jugador> jugadores;
    String contrasena;
    boolean iniciada;
    int ronda;

    public Carrera(int longitud, String contrasena) {
        this.longitud = longitud;
        this.jugadores = new ArrayList<>();
        this.contrasena = contrasena;
        this.iniciada = false;
        this.ronda = 0;
    }

    public Jugador consultarJugador(String nombre){
        for (Jugador jugador : jugadores) {
            if (jugador.getNombre().equals(nombre)) {
                return jugador;
            }
        }
        return null;
    }

    public boolean todosTiraron(){
        int rondaActual = jugadores.getFirst().getNumeroTirada();
        for (Jugador jugador : jugadores) {
            if (!(rondaActual == jugador.getNumeroTirada())){
                return false;
            }
        }
        return true;
    }

    public String mostrarJugadores() {
        String listado = "";
        jugadores.forEach(jugador -> listado.concat(jugador.getNombre() + " ha avanzado " + jugador.getCasillasAvanzadas()+ " casillas\n"));
        return listado;
    }
}
