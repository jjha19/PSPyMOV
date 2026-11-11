package org.example;

import config.ThymeleafConstants;
import config.UrlConstants;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.web.servlet.JakartaServletWebApplication;

import java.io.IOException;

@WebServlet("/abrir")
public class AbrirCaja extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        var webExchange = JakartaServletWebApplication.buildApplication(getServletContext())
                .buildExchange(req, resp);
        WebContext ctx = new WebContext(webExchange);


        if (req.getSession().getAttribute("primerNumero") == null){
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST,
                    "No hay caja fuerte inicializada que abrir");
            return;
        }
        String numero = req.getParameter("numero");

        String contadorComb = (String) req.getSession().getAttribute("contador");
        if(contadorComb == null){
            contadorComb = "0";
        }
        if (contadorComb.equals("0")){
            if (numero.equals(req.getSession().getAttribute("primerNumero"))){
                contadorComb = "1";
            }else contadorComb = "0";
        }
        if (contadorComb.equals("1")){
            if (numero.equals(req.getSession().getAttribute("segundoNumero"))){
                contadorComb = "2";
            }else contadorComb = "0";
        }
        if (contadorComb.equals("2")){
            if (numero.equals(req.getSession().getAttribute("segundoNumero"))){
                contadorComb = "3";
            }else contadorComb = "0";
        }
        req.setAttribute("contador", contadorComb);
        req.setAttribute("abierta", true);
        ctx.setVariable("abriendo", "Estás intentando abrir la caja fuerte");



        resp.setContentType(ThymeleafConstants.CONTENT_TYPE);
        ((TemplateEngine) getServletContext().getAttribute(ThymeleafConstants.TEMPLATE_ENGINE_ATTR))
                .process(UrlConstants.TEMPLATE_ABRIR, ctx, resp.getWriter());

    }
}
