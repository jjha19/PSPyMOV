import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.Biblioteca;

import java.io.IOException;
import java.io.PrintWriter;


@WebServlet("/consultar")
public class ConsultarLibro extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        Biblioteca biblioteca = (Biblioteca) req.getSession().getAttribute("biblioteca");
        if (biblioteca == null) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST,
                    "No hay biblioteca configurada");
            return;
        }
        String titulo = req.getParameter("titulo");
        if (titulo == null || titulo.isEmpty()) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST,
                    "El título es obligatorio");
            return;
        }

        String autor = biblioteca.consultarLibro(titulo);

        if (autor == null || autor.isEmpty()) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND,
                    "El libro no se encuentra en la biblioteca");
            return;
        }

        resp.setContentType("text/html;charset=utf-8");
        resp.setCharacterEncoding("utf-8");
        PrintWriter out = resp.getWriter();
        out.println("<!DOCTYPE html>");
        out.println("<html>");
        out.println("<body>");
        out.println("<h1> Se ha consultado el libro " + titulo + " y el autor es " + autor + "</h1>");
        out.println("</body>");
        out.println("</html>");
        out.close();
    }
}
