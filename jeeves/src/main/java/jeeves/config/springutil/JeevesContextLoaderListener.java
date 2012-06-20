package jeeves.config.springutil;

import java.io.File;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;

import org.springframework.context.Lifecycle;
import org.springframework.web.context.ConfigurableWebApplicationContext;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

public class JeevesContextLoaderListener extends ContextLoaderListener {
	public JeevesContextLoaderListener() {
		System.out.println("Created JeevesContextLoader");
	}
	
	@Override
	protected void customizeContext(ServletContext servletContext,
			ConfigurableWebApplicationContext applicationContext) {
      String appPath = servletContext.getRealPath("/");

      String baseUrl = "";

      try {
          // 2.5 servlet spec or later (eg. tomcat 6 and later)
          baseUrl = servletContext.getContextPath();
      } catch (java.lang.NoSuchMethodError ex) {
          // 2.4 or earlier servlet spec (eg. tomcat 5.5)
          try {
              String resource = servletContext.getResource("/").getPath();
              baseUrl = resource.substring(resource.indexOf('/', 1), resource.length() - 1);
          } catch (java.net.MalformedURLException e) { // unlikely
              baseUrl = servletContext.getServletContextName();
          }
      }

      if (!appPath.endsWith(File.separator))
          appPath += File.separator;

      JeevesApplicationContextLoaderUtil.addEnvConfig(appPath, baseUrl, servletContext, applicationContext);
	}
	
	@Override
	public void contextInitialized(ServletContextEvent event) {
		super.contextInitialized(event);
		Lifecycle context = (Lifecycle) WebApplicationContextUtils.getWebApplicationContext(event.getServletContext());
		context.start();
	}
	@Override
	public void contextDestroyed(ServletContextEvent event) {
		Lifecycle context = (Lifecycle) WebApplicationContextUtils.getWebApplicationContext(event.getServletContext());
		if(context != null) {
			context.stop();
		}
		super.contextDestroyed(event);
	}
}
