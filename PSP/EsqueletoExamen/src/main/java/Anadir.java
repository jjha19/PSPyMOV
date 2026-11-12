import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.Biblioteca;

import java.io.IOException;
import java.io.PrintWriter;

@WebServlet("/anadir")
public class Anadir extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        Biblioteca biblioteca = (Biblioteca) req.getSession().getAttribute("biblioteca");
        if (biblioteca == null) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST,
                    "No hay biblioteca configurada");
            return;
        }

        if (biblioteca.getLibros().size() >= biblioteca.getCapacidad()) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST,
                    "La biblioteca está llena");
            return;
        }

        String titulo = req.getParameter("titulo");
        if (titulo == null || titulo.isEmpty()) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST,
                    "El título es obligatorio");
            return;
        }

        String autor = req.getParameter("autor");
        if (autor == null || autor.isEmpty()) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST,
                    "El autor es obligatorio");
            return;
        }

        biblioteca.anadirLibro(titulo, autor);

        resp.setContentType("text/html;charset=utf-8");
        resp.setCharacterEncoding("utf-8");
        PrintWriter out = resp.getWriter();
        out.println("<!DOCTYPE html>");
        out.println("<html>");
        out.println("<body>");
        out.println("<h1> Se ha añadido un libro a la biblioteca con titulo " + titulo + " y autor " + autor + "</h1>");
        out.println("</body>");
        out.println("</html>");
        out.close();


    }
}
