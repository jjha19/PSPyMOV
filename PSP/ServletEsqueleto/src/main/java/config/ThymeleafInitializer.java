package config;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.templateresolver.WebApplicationTemplateResolver;
import org.thymeleaf.web.servlet.JakartaServletWebApplication;

import java.net.MalformedURLException;

@WebListener
public class ThymeleafInitializer implements ServletContextListener {



    @Override
    public void contextInitialized(ServletContextEvent sce) {
        ServletContext servletContext = sce.getServletContext();
        JakartaServletWebApplication application = JakartaServletWebApplication.buildApplication(servletContext);

        WebApplicationTemplateResolver templateResolver = new WebApplicationTemplateResolver(application);
        templateResolver.setPrefix(UrlConstants.TEMPLATE_PREFIX);
        templateResolver.setSuffix(UrlConstants.TEMPLATE_SUFFIX);
        templateResolver.setTemplateMode(ThymeleafConstants.TEMPLATE_MODE);
        templateResolver.setCharacterEncoding(ThymeleafConstants.CHARACTER_ENCODING);

        TemplateEngine templateEngine = new TemplateEngine();
        templateEngine.setTemplateResolver(templateResolver);

        System.out.println("Context path: " +
                servletContext.getContextPath());

        try {
            System.out.println("Template exists: " +
                    servletContext.getResource(
                            "/WEB-INF/templates/iniciador.html"
                    ));
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }

        servletContext.setAttribute(ThymeleafConstants.TEMPLATE_ENGINE_ATTR, templateEngine);
    }
}
