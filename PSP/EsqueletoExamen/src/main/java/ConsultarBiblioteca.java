import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.Biblioteca;

import java.io.IOException;
import java.io.PrintWriter;

@WebServlet("/biblioteca")
public class ConsultarBiblioteca extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        Biblioteca biblioteca = (Biblioteca) req.getSession().getAttribute("biblioteca");
        if (biblioteca == null) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST,
                    "No hay biblioteca configurada");
            return;
        }

        String nombre = biblioteca.getNombre();
        int capacidad = biblioteca.getCapacidad();
        int libros = biblioteca.getLibros().size();


        resp.setContentType("text/html;charset=utf-8");
        resp.setCharacterEncoding("utf-8");
        PrintWriter out = resp.getWriter();
        out.println("<!DOCTYPE html>");
        out.println("<html>");
        out.println("<body>");
        out.println("<h1> Existe una biblioteca con nombre " + nombre + " con capacidad para " + capacidad + " y tiene " + libros + " libros</h1>");
        out.println("</body>");
        out.println("</html>");
        out.close();
    }
}
