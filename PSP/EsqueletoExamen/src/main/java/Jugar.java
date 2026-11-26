import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.Biblioteca;
import model.Partida;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;

//  Copiapega
//  http://localhost:8080/EsqueletoExamen_war/jugar?adivinanza=hos&nombre=pepe

@WebServlet("/jugar")
public class Jugar extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String adivinanza = req.getParameter("adivinanza");
        String jugador = req.getParameter("nombre");
        Biblioteca biblioteca = (Biblioteca) req.getSession().getAttribute("biblioteca");
        Partida partida = (Partida) req.getSession().getAttribute("partida");

        if (biblioteca == null) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST,
                    "No se ha configurado la biblioteca");
            return;
        }
        if (partida == null || partida.getContrasena() == null) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST,
                    "No se ha iniciado una partida");
            return;
        }
        if (partida.isFinalizado()){
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST,
                    "La partida ha terminado");
            return;

        }
        if (jugador == null || jugador.isEmpty()) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST,
                    "Quien juega?");
            return;
        }


        if ((partida.getJugador1().getNombre().equals(jugador) && !partida.isTurnoJug1()) ||
                (partida.getJugador2().getNombre().equals(jugador) && partida.isTurnoJug1())) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST,
                    "No es tu turno");
            return;
        }

        if (adivinanza == null || adivinanza.equals("")) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST,
                    "Tienes que adivinar");
            return;
        }


        if (adivinanza.length() > 1) {
            if (!adivinanza.equalsIgnoreCase(partida.getPalabra())) {
                partida.setErrores(partida.getErrores() + 1);
            } else {
                terminarPartidaGanador(partida);
                partida.setAdivinado(null);
            }
        } else {
            if (partida.getPalabra().contains(adivinanza)) {
                for (int i = 0; i < partida.getPalabra().length(); i++) {
                    if (adivinanza.charAt(0) == partida.getPalabra().charAt(i)) {
                        partida.getAdivinado()[i] = adivinanza;
                    }
                }
            } else {
                partida.setErrores(partida.getErrores() + 1);
            }
        }

        //Cambia el turno
        partida.setTurnoJug1(!partida.isTurnoJug1());


        if (!Arrays.toString(partida.getAdivinado()).contains("_")) {
            terminarPartidaGanador(partida);
        }


        //lo vuelvo a setear a true pq creo que es mas rentable volver a setear que comprobar en 2 if distintos
        if (partida.isFinalizado() || partida.getErrores() >= 6) {
            partida.setFinalizado(true);
            biblioteca.getPartidas().add(partida);
        }
        partida.setRondas(partida.getRondas() + 1);


        resp.setContentType("text/html;charset=utf-8");
        resp.setCharacterEncoding("utf-8");
        PrintWriter out = resp.getWriter();
        out.println("<!DOCTYPE html>");
        out.println("<html>");
        out.println("<body>");
        out.println("<h1>Biblioteca " + biblioteca.getNombre() + "</h1>");
        out.println("<h2> " + Arrays.toString(partida.getAdivinado()) + "</h2>");
        out.println("<h3>Llevan " + partida.getErrores() + " errores </h3>");
        out.println("<p>" + pintarAhorcado(partida.getErrores()) + "</p>");
        out.println("</body>");
        out.println("</html>");
        out.close();
    }

    private void terminarPartidaGanador(Partida partida) {
        if (partida.isTurnoJug1()) {
            partida.getJugador1().setPartidasGanadas(partida.getJugador1().getPartidasGanadas() + 1);
            partida.setGanador(partida.getJugador1().getNombre());
        } else {
            partida.getJugador2().setPartidasGanadas(partida.getJugador2().getPartidasGanadas() + 1);
            partida.setGanador(partida.getJugador2().getNombre());
        }
        partida.setFinalizado(true);
    }


    public String pintarAhorcado(int errores) {
        switch (errores) {
            case 0:
                return """
                        |---------\n
                        |\n
                        |\n
                        |\n
                        |\n
                        """;
            case 1:
                return """
                        |---------
                        |        |
                        |
                        |
                        |
                        """;
            case 2:
                return """
                        |---------
                        |        |
                        |        O
                        |
                        |
                        """;
            case 3:
                return """
                        |---------
                        |        |
                        |        O
                        |        !
                        |
                        """;
            case 4:
                return """
                        |---------
                        |        |
                        |        O
                        |        !
                        |       /\
                        """;
            case 5:
                return """
                        |---------
                        |        |
                        |        O
                        |       /!\
                        |       /\
                        """;
            case 6:
                return """
                        |---------
                        |        |
                        |        X
                        |       /!\
                        |       /\
                        """;

        }
        return "";
    }
}
