package org.example;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

public final class TorneoStore {

    private TorneoStore() {
    }

    public static final Object LOCK = new Object();
    public static final String VACIO = "-";

    public static final Map<String, String> jugadoresSimbolo = new LinkedHashMap<>();
    public static final List<Partida> partidas = new ArrayList<>();
    public static Partida partidaActual = null;
    public static int siguienteId = 1;
    public static int pasoPodio = 0;

    public static final class Movimiento {
        public final String jugador;
        public final String simbolo;
        public final int posicion;

        public Movimiento(String jugador, String simbolo, int posicion) {
            this.jugador = jugador;
            this.simbolo = simbolo;
            this.posicion = posicion;
        }
    }

    public static final class Partida {
        public final int id;
        public final String jugador1;
        public final String jugador2;
        public final String simbolo1;
        public final String simbolo2;
        public final String[] tablero;
        public final List<Movimiento> movimientos;
        public String turnoJugador;
        public boolean finalizada;
        public String ganador;
        public String perdedor;

        public Partida(int id, String jugador1, String jugador2, String simbolo1, String simbolo2, String turnoJugador) {
            this.id = id;
            this.jugador1 = jugador1;
            this.jugador2 = jugador2;
            this.simbolo1 = simbolo1;
            this.simbolo2 = simbolo2;
            this.turnoJugador = turnoJugador;
            this.tablero = new String[]{VACIO, VACIO, VACIO, VACIO, VACIO, VACIO, VACIO, VACIO, VACIO};
            this.movimientos = new ArrayList<>();
            this.finalizada = false;
        }
    }

    public static String getSimboloJugador(Partida partida, String jugador) {
        return partida.jugador1.equals(jugador) ? partida.simbolo1 : partida.simbolo2;
    }

    public static boolean hayGanador(String[] t, String s) {
        return (t[0].equals(s) && t[1].equals(s) && t[2].equals(s))
                || (t[3].equals(s) && t[4].equals(s) && t[5].equals(s))
                || (t[6].equals(s) && t[7].equals(s) && t[8].equals(s))
                || (t[0].equals(s) && t[3].equals(s) && t[6].equals(s))
                || (t[1].equals(s) && t[4].equals(s) && t[7].equals(s))
                || (t[2].equals(s) && t[5].equals(s) && t[8].equals(s))
                || (t[0].equals(s) && t[4].equals(s) && t[8].equals(s))
                || (t[2].equals(s) && t[4].equals(s) && t[6].equals(s));
    }

    public static boolean tableroLleno(String[] t) {
        for (String casilla : t) {
            if (VACIO.equals(casilla)) {
                return false;
            }
        }
        return true;
    }

    public static String pintarTablero(String[] t) {
        return "<pre>"
                + t[0] + " " + t[1] + " " + t[2] + "\n"
                + t[3] + " " + t[4] + " " + t[5] + "\n"
                + t[6] + " " + t[7] + " " + t[8]
                + "</pre>";
    }

    public static String escape(String text) {
        if (text == null) {
            return "";
        }
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }

    public static String elegirTurnoInicial(String j1, String j2) {
        return new Random().nextBoolean() ? j1 : j2;
    }

    public static Map<String, Integer> victoriasPorJugador() {
        Map<String, Integer> victorias = new HashMap<>();
        for (String jugador : jugadoresSimbolo.keySet()) {
            victorias.put(jugador, 0);
        }
        for (Partida partida : partidas) {
            if (partida.ganador != null && victorias.containsKey(partida.ganador)) {
                victorias.put(partida.ganador, victorias.get(partida.ganador) + 1);
            }
        }
        return victorias;
    }

    public static List<Map.Entry<String, Integer>> rankingActual() {
        return victoriasPorJugador().entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue(Comparator.reverseOrder())
                        .thenComparing(Map.Entry::getKey))
                .collect(Collectors.toList());
    }
}
