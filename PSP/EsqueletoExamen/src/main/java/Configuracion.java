import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.Biblioteca;
import model.Jugador;
import model.Partida;

import java.io.IOException;
import java.io.PrintWriter;


// Copiapega
// http://localhost:8080/EsqueletoExamen_war/configuracion?biblioteca=quevedo&palabra=hola&jugador1=pepe&jugador2=juan

@WebServlet("/configuracion")
public class Configuracion extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        String nombreBiblioteca;
        String palabra;
        String contrasena = "hola123";
        String nombreJugador1;
        String nombreJugador2;

        nombreBiblioteca = req.getParameter("biblioteca");
        palabra = req.getParameter("palabra");
        nombreJugador1 = req.getParameter("jugador1");
        nombreJugador2 = req.getParameter("jugador2");

        if (palabra.contains(" ") || palabra.contains("1")
                || palabra.contains("2") || palabra.contains("3")
                || palabra.contains("4") || palabra.contains("5")
                || palabra.contains("6") || palabra.contains("7")
                || palabra.contains("8") || palabra.contains("9")) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST,
                    "La palabra no debe contener ni espacios ni numeros.");
        }
        Jugador jugador1 = new Jugador(nombreJugador1);
        Jugador jugador2 = new Jugador(nombreJugador2);

        Partida partida = new Partida(palabra, contrasena, jugador1, jugador2);
        Biblioteca biblioteca = new Biblioteca(nombreBiblioteca);

        req.getSession().setAttribute("partida", partida);
        req.getSession().setAttribute("contrasena", contrasena);
        req.getSession().setAttribute("biblioteca", biblioteca);

        resp.setContentType("text/html;charset=utf-8");
        resp.setCharacterEncoding("utf-8");
        PrintWriter out = resp.getWriter();
        out.println("<!DOCTYPE html>");
        out.println("<html>");
        out.println("<body>");
        out.println("<h1> Se ha creado una partida con la palabra " + palabra + " y con contraseña " + contrasena + "</h1>");
        out.println("<h2>" + jugador1.getNombre() + " contra " + jugador2.getNombre() + "</h2>");
        out.println("</body>");
        out.println("</html>");
        out.close();
    }
}
