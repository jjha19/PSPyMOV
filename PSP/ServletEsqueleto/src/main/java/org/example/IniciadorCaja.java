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

    @WebServlet("/iniciador")
    public class IniciadorCaja extends HttpServlet {

        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            var webExchange = JakartaServletWebApplication.buildApplication(getServletContext())
                    .buildExchange(req, resp);
            WebContext ctx = new WebContext(webExchange);

            //Copiapega

            try {
                int combinacionNum = Integer.parseInt(req.getParameter("combinacion"));
            }catch (NumberFormatException e){
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST,
                        "La combinacion tiene que ser numerica");
            }

            String[] combinacion = req.getParameter("combinacion").split("");
            if (combinacion.length !=3 ) {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST,
                        "La combinacion tiene que ser de 3 numeros");
                return;
            }



            req.getSession().setAttribute("primerNumero", combinacion[0]);
            req.getSession().setAttribute("segundoNumero", combinacion[1]);
            req.getSession().setAttribute("tercerNumero", combinacion[2]);
            ctx.setVariable("iniciado", "La caja ha sido inicializada");


            resp.setContentType(ThymeleafConstants.CONTENT_TYPE);
            ((TemplateEngine) getServletContext().getAttribute(ThymeleafConstants.TEMPLATE_ENGINE_ATTR))
                    .process(UrlConstants.TEMPLATE_INICIADOR, ctx, resp.getWriter());

        }
    }
