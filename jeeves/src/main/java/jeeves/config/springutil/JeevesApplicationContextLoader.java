package jeeves.config.springutil;

import java.io.File;

import javax.servlet.ServletContext;

import jeeves.config.EnvironmentalConfig;

import org.springframework.context.support.AbstractXmlApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

public class JeevesApplicationContextLoader {

    public static AbstractXmlApplicationContext load(String baseUrl, String appPath, String... configurationFiles) {
        return load(baseUrl, appPath, null, configurationFiles);
    }
    public static AbstractXmlApplicationContext load(String baseUrl, String appPath, ServletContext servletContext, String... configurationFiles) {
        EnvironmentalConfig envConfig = new EnvironmentalConfig(baseUrl, appPath);
        envConfig.setServletContext(servletContext);

        FileSystemXmlApplicationContext springContext = new FileSystemXmlApplicationContext(configurationFiles, false);
        springContext.setValidating(false);

        springContext.addBeanFactoryPostProcessor(envConfig);
        springContext.refresh();

        return springContext;
    }
    
    public static AbstractXmlApplicationContext loadDefaults(String appPath, String baseUrl, ServletContext context) {
        EnvironmentalConfig envConfig = new EnvironmentalConfig(baseUrl, appPath);
        envConfig.setServletContext(context);

        String configPath = envConfig.getConfigPath();
        String[] contexts = { 
                new File(configPath, "config-jeeves.xml").toURI().toString(),
                new File(configPath, "config.xml").toURI().toString() 
        };
        FileSystemXmlApplicationContext springContext = new FileSystemXmlApplicationContext(contexts, false);
        springContext.setValidating(false);
        springContext.addBeanFactoryPostProcessor(envConfig);
        springContext.refresh();

        return springContext;
    }
    public static AbstractXmlApplicationContext loadDefaults(String appPath, String baseUrl, String configPath) {
        return load(baseUrl, appPath, new File(configPath, "config-jeeves.xml").getAbsolutePath(), new File(configPath, "config.xml").getAbsolutePath());
    }
}
