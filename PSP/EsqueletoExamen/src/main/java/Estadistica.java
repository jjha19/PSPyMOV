import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.Carrera;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

@WebServlet("/estadistica")
public class Estadistica extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Carrera carrera = (Carrera) req.getSession().getAttribute("carrera");
        if (carrera == null) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST,
                    "No hay carrera configurada, ergo, no hay participantes para mostrar");
            return;
        }


        List<String> estadistica = new ArrayList<>();
        carrera.getJugadores().forEach(jugador -> {
            estadistica.add("\n-*" + jugador.getNombre() + jugador.getHistorial().toString());
        });

        resp.setContentType("text/html;charset=utf-8");
        resp.setCharacterEncoding("utf-8");
        PrintWriter out = resp.getWriter();
        out.println("<!DOCTYPE html>");
        out.println("<html>");
        out.println("<body>");

        out.println("<h1>ESTADISTICAS</h1>");
        out.println("<h2>" + estadistica.toString() + "</h2>");
        out.println("</body>");
        out.println("</html>");
        out.close();


    }
}
