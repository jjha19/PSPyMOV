import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.Biblioteca;

import java.io.IOException;

@WebServlet("/estadisticas")
public class Estadisticas extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Biblioteca biblioteca =  (Biblioteca) req.getSession().getAttribute("biblioteca");
        if (biblioteca == null) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST,
                    "No se ha configurado la biblioteca");
            return;
        }

//        biblioteca.getPartidas().stream().forEach(partida -> {
//            if (partida.getGanador())
//        })
    }
}
