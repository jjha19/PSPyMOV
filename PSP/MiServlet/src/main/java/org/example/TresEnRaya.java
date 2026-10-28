package org.example;

import config.ThymeleafConstants;
import config.UrlConstants;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.web.servlet.JakartaServletWebApplication;

import java.io.IOException;

@WebServlet("/tres")
public class TresEnRaya extends HttpServlet {

    public static final String BaseTablero = "-";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        var webExchange = JakartaServletWebApplication.buildApplication(getServletContext())
                .buildExchange(req, resp);
        WebContext ctx = new WebContext(webExchange);

        String coordenada = req.getParameter("coordenada");
        String marca = req.getParameter("marca");
        boolean turnoEquis = true;

        if (req.getSession().getAttribute("TurnoEquis") == null) {
            req.getSession().setAttribute("TurnoEquis", true);
        } else {
            turnoEquis = (boolean) req.getSession().getAttribute("TurnoEquis");
        }

        if (coordenada == null || marca == null) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST,
                    "Faltan parámetros");
            return;
        }

        int coord;

        try {
            coord = Integer.parseInt(coordenada);
        } catch (NumberFormatException e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST,
                    "La coordenada debe ser un número entre 1 y 9");
            return;
        }

        if (coord < 1 || coord > 9
                || (!marca.equals("X") && !marca.equals("O"))) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST,
                    "Media pila rey que tienen que ser o X u O");
            return;
        }

        String[] tablero = (String[]) req.getSession().getAttribute("Tablero");
        if (tablero == null) {
            tablero = new String[]{BaseTablero, BaseTablero, BaseTablero, BaseTablero, BaseTablero, BaseTablero, BaseTablero, BaseTablero, BaseTablero};
            req.getSession().setAttribute("Tablero", tablero);
        }
        if ((turnoEquis && !marca.equals("X"))
                || (!turnoEquis && !marca.equals("O"))) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST,
                    "No es tu turno");
            return;
        }
        if (tablero[Integer.parseInt(coordenada) - 1].equals(BaseTablero)) {
            tablero[Integer.parseInt(coordenada) - 1] = marca;
        }else {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST,
                    "En esa coordenada ya hay una marquita");
            return;
        }




        //Pinta el tablero
        String fila1 = tablero[0] + " " + tablero[1] + " " + tablero[2];
        String fila2 = tablero[3] + " " + tablero[4] + " " + tablero[5];
        String fila3 = tablero[6] + " " + tablero[7] + " " + tablero[8];
        ctx.setVariable("fila1", fila1);
        ctx.setVariable("fila2", fila2);
        ctx.setVariable("fila3", fila3);

        //cambia el turno
        turnoEquis = !turnoEquis;
        req.getSession().setAttribute("TurnoEquis", turnoEquis);


        resp.setContentType(ThymeleafConstants.CONTENT_TYPE);
        ((TemplateEngine) getServletContext().getAttribute(ThymeleafConstants.TEMPLATE_ENGINE_ATTR))
                .process(UrlConstants.TEMPLATE_TRES_EN_RAYA, ctx, resp.getWriter());
    }
}
