package org.example;

import config.UrlConstants;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;

@WebServlet(UrlConstants.URL_JUGAR)
public class JugarServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String jugador = req.getParameter("jugador");
        String posicionTexto = req.getParameter("posicion");

        if (jugador == null || posicionTexto == null) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Faltan parametros: jugador, posicion");
            return;
        }

        jugador = jugador.trim();
        if (jugador.isEmpty()) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Jugador vacio");
            return;
        }

        int posicion;
        try {
            posicion = Integer.parseInt(posicionTexto);
        } catch (NumberFormatException e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Posicion invalida");
            return;
        }

        if (posicion < 1 || posicion > 9) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Posicion fuera de rango (1..9)");
            return;
        }

        String mensaje;
        TorneoStore.Partida partida;

        synchronized (TorneoStore.LOCK) {
            partida = TorneoStore.partidaActual;
            if (partida == null) {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "No hay partida activa");
                return;
            }
            if (partida.finalizada) {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "La partida ya esta finalizada");
                return;
            }
            if (!partida.jugador1.equals(jugador) && !partida.jugador2.equals(jugador)) {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Jugador no pertenece a la partida activa");
                return;
            }
            if (!partida.turnoJugador.equals(jugador)) {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "No es tu turno");
                return;
            }

            int idx = posicion - 1;
            if (!TorneoStore.VACIO.equals(partida.tablero[idx])) {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Esa casilla ya esta ocupada");
                return;
            }

            String simbolo = TorneoStore.getSimboloJugador(partida, jugador);
            partida.tablero[idx] = simbolo;
            partida.movimientos.add(new TorneoStore.Movimiento(jugador, simbolo, posicion));

            if (TorneoStore.hayGanador(partida.tablero, simbolo)) {
                partida.finalizada = true;
                partida.ganador = jugador;
                partida.perdedor = partida.jugador1.equals(jugador) ? partida.jugador2 : partida.jugador1;
                mensaje = "Ganador: " + partida.ganador + " en " + partida.movimientos.size() + " movimientos";
                TorneoStore.partidaActual = null;
            } else if (TorneoStore.tableroLleno(partida.tablero)) {
                partida.finalizada = true;
                mensaje = "Empate en " + partida.movimientos.size() + " movimientos";
                TorneoStore.partidaActual = null;
            } else {
                partida.turnoJugador = partida.jugador1.equals(jugador) ? partida.jugador2 : partida.jugador1;
                mensaje = "Movimiento correcto. Turno de " + partida.turnoJugador;
            }
        }

        resp.setContentType("text/html;charset=utf-8");
        resp.setCharacterEncoding("utf-8");
        PrintWriter out = resp.getWriter();

        out.println("<html><body>");
        out.println("<h1>Estado de partida</h1>");
        out.println("<p>Partida: " + partida.id + "</p>");
        out.println("<p>" + TorneoStore.escape(mensaje) + "</p>");
        out.println(TorneoStore.pintarTablero(partida.tablero));
        out.println("</body></html>");
    }
}
