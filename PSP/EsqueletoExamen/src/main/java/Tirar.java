import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.Carrera;
import model.Jugador;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Random;

@WebServlet("/tirar")
public class Tirar extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Carrera carrera = (Carrera) req.getSession().getAttribute("carrera");
        if (carrera == null) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST,
                    "No hay carrera configurada");
            return;
        }
        if (!carrera.isIniciada()) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST,
                    "No se ha iniciado la carrera todavia");
            return;
        }
        if (carrera.getRonda() == carrera.getLongitud()){
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST,
                    "Ya se terminó la carrera. No se puede seguir tirando");
            return;
        }

        String nombreJugador = req.getParameter("nombre");
        if (nombreJugador == null || nombreJugador.isEmpty()) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST,
                    "Debes poner un jugador para que tire");
            return;
        }
        Jugador jugador = carrera.consultarJugador(nombreJugador);

        if (jugador == null) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST,
                    "No hay jugadores con ese nombre");
            return;
        }



        if (!(jugador.getNumeroTirada() == carrera.getRonda())) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST,
                    "No es tu turno");
            return;
        }


        Random rd = new Random();
        int numerito = rd.nextInt(1, 6);
        jugador.setNumeroTirada(jugador.getNumeroTirada() + 1);
        jugador.setCasillasAvanzadas(numerito);
        jugador.getHistorial().put(("Ronda " + carrera.getRonda()), numerito);

        if (carrera.todosTiraron()) {
            carrera.setRonda(carrera.getRonda() + 1);
        }



        String distanciaJugadores = carrera.mostrarJugadores();

        resp.setContentType("text/html;charset=utf-8");
        resp.setCharacterEncoding("utf-8");
        PrintWriter out = resp.getWriter();
        out.println("<!DOCTYPE html>");
        out.println("<html>");
        out.println("<body>");
        out.println("<h1> El jugador " + nombreJugador + " avanza " + numerito + " casillas</h1>");
        out.println("<h2>"+distanciaJugadores+"</h2>");
        out.println("</body>");
        out.println("</html>");
        out.close();
    }
}
