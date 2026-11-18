import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.Carrera;
import model.Jugador;

import java.io.IOException;
import java.io.PrintWriter;

// Copiapega
// http://localhost:8080/EsqueletoExamen_war/anadirJugador?nombre=juan

@WebServlet("/anadirJugador")
public class AnadirJugador extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Carrera carrera = (Carrera) req.getSession().getAttribute("carrera");
        if (carrera == null) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST,
                    "No hay carrera configurada");
            return;
        }

        if (carrera.isIniciada()){
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST,
                    "Una vez iniciada la carrera no se pueden añadir participantes");
            return;
        }

        String nombreJugador = req.getParameter("nombre");
        if (nombreJugador == null || nombreJugador.isEmpty()) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST,
                    "Debes poner un nombre para el participante");
            return;
        }
        Jugador jugador = new Jugador(nombreJugador);




        carrera.getJugadores().add(jugador);
        resp.setContentType("text/html;charset=utf-8");
        resp.setCharacterEncoding("utf-8");
        PrintWriter out = resp.getWriter();
        out.println("<!DOCTYPE html>");
        out.println("<html>");
        out.println("<body>");
        out.println("<h1> Se ha añadido el jugador " + nombreJugador + " a la carrera</h1>");
        out.println("</body>");
        out.println("</html>");
        out.close();


    }
}
