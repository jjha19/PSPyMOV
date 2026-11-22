package org.example;

import config.UrlConstants;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

@WebServlet(UrlConstants.URL_ESTADISTICAS)
public class EstadisticasServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String jugador = req.getParameter("jugador");
        String partidaTexto = req.getParameter("partida");

        resp.setContentType("text/html;charset=utf-8");
        resp.setCharacterEncoding("utf-8");
        PrintWriter out = resp.getWriter();

        out.println("<html><body>");
        out.println("<h1>Estadisticas</h1>");

        synchronized (TorneoStore.LOCK) {
            if (partidaTexto != null && !partidaTexto.trim().isEmpty()) {
                int id;
                try {
                    id = Integer.parseInt(partidaTexto.trim());
                } catch (NumberFormatException e) {
                    resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "partida debe ser numero");
                    return;
                }
                TorneoStore.Partida encontrada = null;
                for (TorneoStore.Partida p : TorneoStore.partidas) {
                    if (p.id == id) {
                        encontrada = p;
                        break;
                    }
                }
                if (encontrada == null) {
                    out.println("<p>No existe la partida</p>");
                } else {
                    out.println("<h2>Historial partida " + encontrada.id + "</h2>");
                    for (int i = 0; i < encontrada.movimientos.size(); i++) {
                        TorneoStore.Movimiento m = encontrada.movimientos.get(i);
                        out.println("<p>" + (i + 1) + ". " + TorneoStore.escape(m.jugador) + " (" + TorneoStore.escape(m.simbolo) + ") -> " + m.posicion + "</p>");
                    }
                }
                out.println("</body></html>");
                return;
            }

            if (jugador != null && !jugador.trim().isEmpty()) {
                String j = jugador.trim();
                out.println("<h2>Partidas de " + TorneoStore.escape(j) + "</h2>");
                for (TorneoStore.Partida p : TorneoStore.partidas) {
                    if (p.jugador1.equals(j) || p.jugador2.equals(j)) {
                        String simbolo = p.jugador1.equals(j) ? p.simbolo1 : p.simbolo2;
                        String rival = p.jugador1.equals(j) ? p.jugador2 : p.jugador1;
                        String resultado;
                        if (!p.finalizada || p.ganador == null) {
                            resultado = p.finalizada ? "Empate" : "En curso";
                        } else if (p.ganador.equals(j)) {
                            resultado = "Ganada";
                        } else {
                            resultado = "Perdida";
                        }
                        out.println("<p>Partida " + p.id + " | simbolo: " + TorneoStore.escape(simbolo) + " | " + resultado + " contra " + TorneoStore.escape(rival) + "</p>");
                    }
                }
                out.println("</body></html>");
                return;
            }

            out.println("<h2>Victorias por jugador</h2>");
            for (Map.Entry<String, Integer> e : TorneoStore.victoriasPorJugador().entrySet()) {
                out.println("<p>" + TorneoStore.escape(e.getKey()) + ": " + e.getValue() + "</p>");
            }
        }

        out.println("</body></html>");
    }
}
