import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.Biblioteca;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.web.servlet.JakartaServletWebApplication;

import java.io.IOException;
import java.io.PrintWriter;

@WebServlet("/config")
public class Config extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        var webExchange = JakartaServletWebApplication.buildApplication(getServletContext())
                .buildExchange(req, resp);
        WebContext ctx = new WebContext(webExchange);

        String nombre = req.getParameter("nombre");
        int capacidad;
        try {
            capacidad = Integer.parseInt(req.getParameter("capacidad"));;
            if (capacidad <= 0) {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST,
                        "La capacidad tiene que ser positiva");
                return;
            }
        }catch (NumberFormatException e){
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST,
                    "La capacidad tiene que ser numerica");
            return;
        }

        Biblioteca biblioteca = new Biblioteca(nombre, capacidad);
        req.getSession().setAttribute("biblioteca", biblioteca);

        resp.setContentType("text/html;charset=utf-8");
        resp.setCharacterEncoding("utf-8");
        PrintWriter out = resp.getWriter();
        out.println("<!DOCTYPE html>");
        out.println("<html>");
        out.println("<body>");
        out.println("<h1> Se ha creado una biblioteca con nombre " + nombre + " y capacidad " + capacidad + "</h1>");
        out.println("</body>");
        out.println("</html>");
        out.close();
    }
}
