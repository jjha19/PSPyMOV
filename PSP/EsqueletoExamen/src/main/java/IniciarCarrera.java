import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.Carrera;

import java.io.IOException;
import java.io.PrintWriter;


// Copiapega
// http://localhost:8080/EsqueletoExamen_war/iniciar?contrasena=hola123

@WebServlet("/iniciar")
public class IniciarCarrera extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        Carrera carrera = (Carrera) req.getSession().getAttribute("carrera");
        if (carrera == null) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST,
                    "No hay carrera configurada");
            return;
        }

        String contrasena = req.getParameter("contrasena");
        if (contrasena ==null || contrasena.isEmpty()) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST,
                    "Debes poner una contraseña");
            return;
        }

        if (! req.getParameter("contrasena").equalsIgnoreCase(carrera.getContrasena())){
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST,
                    "Contraseña incorrecta");
            return;
        }

        carrera.setIniciada(true);

        resp.setContentType("text/html;charset=utf-8");
        resp.setCharacterEncoding("utf-8");
        PrintWriter out = resp.getWriter();
        out.println("<!DOCTYPE html>");
        out.println("<html>");
        out.println("<body>");
        out.println("<h1> Se ha inicializado la carrera con " +carrera.getJugadores().size()+ " jugadores </h1>");
        out.println("</body>");
        out.println("</html>");
        out.close();
    }
}
