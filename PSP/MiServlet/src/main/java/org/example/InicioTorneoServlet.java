package org.example;

import config.UrlConstants;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;

@WebServlet(UrlConstants.URL_INICIO_TORNEO)
public class InicioTorneoServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String jugador1 = req.getParameter("jugador1");
        String jugador2 = req.getParameter("jugador2");
        String simbolo1 = req.getParameter("simbolo1");
        String simbolo2 = req.getParameter("simbolo2");

        if (jugador1 == null || jugador2 == null || simbolo1 == null || simbolo2 == null) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Faltan parametros: jugador1, jugador2, simbolo1, simbolo2");
            return;
        }

        jugador1 = jugador1.trim();
        jugador2 = jugador2.trim();
        simbolo1 = simbolo1.trim();
        simbolo2 = simbolo2.trim();

        if (jugador1.isEmpty() || jugador2.isEmpty() || simbolo1.isEmpty() || simbolo2.isEmpty()) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "No se permiten valores vacios");
            return;
        }

        if (jugador1.equalsIgnoreCase(jugador2)) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Los jugadores deben ser distintos");
            return;
        }

        if (simbolo1.equals(simbolo2)) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Los simbolos deben ser distintos");
            return;
        }

        synchronized (TorneoStore.LOCK) {
            TorneoStore.jugadoresSimbolo.put(jugador1, simbolo1);
            TorneoStore.jugadoresSimbolo.put(jugador2, simbolo2);

            int id = TorneoStore.siguienteId++;
            String turnoInicial = TorneoStore.elegirTurnoInicial(jugador1, jugador2);
            TorneoStore.partidaActual = new TorneoStore.Partida(id, jugador1, jugador2, simbolo1, simbolo2, turnoInicial);
            TorneoStore.partidas.add(TorneoStore.partidaActual);
        }

        resp.setContentType("text/html;charset=utf-8");
        resp.setCharacterEncoding("utf-8");
        PrintWriter out = resp.getWriter();

        TorneoStore.Partida partida;
        synchronized (TorneoStore.LOCK) {
            partida = TorneoStore.partidaActual;
        }

        out.println("<html><body>");
        out.println("<h1>Torneo iniciado</h1>");
        out.println("<p>Partida: " + partida.id + "</p>");
        out.println("<p>" + TorneoStore.escape(partida.jugador1) + " usa " + TorneoStore.escape(partida.simbolo1) + "</p>");
        out.println("<p>" + TorneoStore.escape(partida.jugador2) + " usa " + TorneoStore.escape(partida.simbolo2) + "</p>");
        out.println("<p>Empieza: " + TorneoStore.escape(partida.turnoJugador) + "</p>");
        out.println("<h3>Tablero</h3>");
        out.println(TorneoStore.pintarTablero(partida.tablero));
        out.println("</body></html>");
    }
}
