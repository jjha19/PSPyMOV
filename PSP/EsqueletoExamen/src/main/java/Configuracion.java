import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.Carrera;

import java.io.IOException;
import java.io.PrintWriter;


// Copiapega
// http://localhost:8080/EsqueletoExamen_war/configuracion?longitud=5

@WebServlet("/configuracion")
public class Configuracion extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        int longitud;
        String contrasena = "hola123";
        try {
            longitud = Integer.parseInt(req.getParameter("longitud"));;
            if (longitud <= 0) {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST,
                        "La longitud tiene que ser positiva");
                return;
            }
        }catch (NumberFormatException e){
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST,
                    "La longitud tiene que ser numerica");
            return;
        }

        Carrera carrera = new Carrera(longitud, contrasena);
        req.getSession().setAttribute("carrera", carrera);
        req.getSession().setAttribute("contrasena", contrasena);

        resp.setContentType("text/html;charset=utf-8");
        resp.setCharacterEncoding("utf-8");
        PrintWriter out = resp.getWriter();
        out.println("<!DOCTYPE html>");
        out.println("<html>");
        out.println("<body>");
        out.println("<h1> Se ha creado una carrera con longitud " + longitud + " y con contraseña " + contrasena + "</h1>");
        out.println("</body>");
        out.println("</html>");
        out.close();
    }
}
