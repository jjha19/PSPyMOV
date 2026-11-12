package org.example;

import config.UrlConstants;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;

@WebServlet(UrlConstants.URL_BORRAR_JUGADOR)
public class BorrarJugadorServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String jugador = req.getParameter("jugador");
        if (jugador == null || jugador.trim().isEmpty()) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Falta parametro jugador");
            return;
        }

        jugador = jugador.trim();
        int eliminadas = 0;
        boolean eliminado;

        synchronized (TorneoStore.LOCK) {
            eliminado = TorneoStore.jugadoresSimbolo.remove(jugador) != null;
            if (!eliminado) {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Jugador no existe en el torneo");
                return;
            }

            for (int i = TorneoStore.partidas.size() - 1; i >= 0; i--) {
                TorneoStore.Partida p = TorneoStore.partidas.get(i);
                if (p.jugador1.equals(jugador) || p.jugador2.equals(jugador)) {
                    TorneoStore.partidas.remove(i);
                    eliminadas++;
                }
            }

            if (TorneoStore.partidaActual != null && (TorneoStore.partidaActual.jugador1.equals(jugador) || TorneoStore.partidaActual.jugador2.equals(jugador))) {
                TorneoStore.partidaActual = null;
            }

            TorneoStore.pasoPodio = 0;
        }

        resp.setContentType("text/html;charset=utf-8");
        resp.setCharacterEncoding("utf-8");
        PrintWriter out = resp.getWriter();
        out.println("<html><body>");
        out.println("<h1>Jugador eliminado</h1>");
        out.println("<p>Jugador: " + TorneoStore.escape(jugador) + "</p>");
        out.println("<p>Partidas eliminadas: " + eliminadas + "</p>");
        out.println("</body></html>");
    }
}
