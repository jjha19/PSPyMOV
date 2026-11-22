package org.example;

import config.UrlConstants;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;

@WebServlet(UrlConstants.URL_FIN_TORNEO)
public class FinTorneoServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String etiqueta;
        int indiceRanking;
        List<Map.Entry<String, Integer>> ranking;

        synchronized (TorneoStore.LOCK) {
            ranking = TorneoStore.rankingActual();
            int paso = TorneoStore.pasoPodio % 3;
            if (paso == 0) {
                etiqueta = "Tercero";
                indiceRanking = 2;
            } else if (paso == 1) {
                etiqueta = "Segundo";
                indiceRanking = 1;
            } else {
                etiqueta = "Campeon";
                indiceRanking = 0;
            }
            TorneoStore.pasoPodio++;
        }

        resp.setContentType("text/html;charset=utf-8");
        resp.setCharacterEncoding("utf-8");
        PrintWriter out = resp.getWriter();
        out.println("<html><body>");
        out.println("<h1>Fin de torneo</h1>");

        if (ranking.size() <= indiceRanking) {
            out.println("<p>" + etiqueta + ": no disponible</p>");
        } else {
            Map.Entry<String, Integer> entrada = ranking.get(indiceRanking);
            out.println("<p>" + etiqueta + ": " + TorneoStore.escape(entrada.getKey()) + " con " + entrada.getValue() + " victorias</p>");
        }

        out.println("</body></html>");
    }
}
