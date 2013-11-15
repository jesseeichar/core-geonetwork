package jeeves.config.springutil;

import jeeves.server.sources.http.HttpServiceRequest;
import jeeves.server.sources.http.JeevesServlet;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.web.context.WebApplicationContext;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: Jesse
 * Date: 11/13/13
 * Time: 5:15 PM
 */
public class DelegatingFilterProxy extends org.springframework.web.filter.DelegatingFilterProxy {
    private final static InheritableThreadLocal<String> applicationContextAttributeKey = new InheritableThreadLocal<String>();
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        if (request instanceof HttpServletRequest) {
            HttpServletRequest httpRequest = (HttpServletRequest) request;
            final String nodeName = httpRequest.getServletPath().substring(1);
            applicationContextAttributeKey.set(JeevesServlet.USER_SESSION_ATTRIBUTE_KEY + nodeName);
        super.doFilter(request, response, filterChain);
        } else {
            response.getWriter().write(request.getClass().getName() + " is not a supported type of request");
        }
    }

    @Override
    public String getContextAttribute() {
        return applicationContextAttributeKey.get();
    }

    public static ConfigurableApplicationContext getApplicationContextAttributeKey(ServletContext servletContext) {
        final Object applicationContext = servletContext.getAttribute(applicationContextAttributeKey.get());
        return (ConfigurableApplicationContext) applicationContext;
    }

}