package jeeves.config.springutil;

import java.io.File;
import java.io.IOException;

import javax.servlet.ServletContext;

import jeeves.config.EnvironmentalConfig;
import jeeves.utils.Log;

import org.apache.commons.io.FileUtils;
import org.springframework.context.support.AbstractXmlApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;
import org.springframework.web.context.ConfigurableWebApplicationContext;

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
		Log.info(Log.JEEVES, "since the file 'config-jeeves' does not exist I will attempt to migrate the configuration files to the spring dependency injection framework");
		migrateConfigFiles(configPath);
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
	
	// -------------------------- Migration code -----------------------------
    private static void migrateConfigFiles(String configPath) {
        if (!new File(configPath, "config-jeeves.xml").exists()) {
        	if (new File(configPath, "config.xml").exists()) {
        		Log.info(Log.JEEVES, "since the file 'config-jeeves' does not exist I will attempt to migrate the configuration files to the spring dependency injection framework");
        		try {
        			File backupDir = new File(configPath, "config_backup");
        			backupDir.mkdirs();
        			for (File f : new File(configPath).listFiles()) {
        				FileUtils.copyFile(f, new File(backupDir, f.getName()));
        			}
        		} catch (IOException e) {
        			Log.error(Log.JEEVES, "Attempted to migrate configuration to spring dependency injection configuration and failed:", e);
        		}
        	}
        }

		
	}
	public static void addEnvConfig(String appPath, String baseUrl, ServletContext servletContext, ConfigurableWebApplicationContext springContext ) {
        EnvironmentalConfig envConfig = new EnvironmentalConfig(baseUrl, appPath);
        envConfig.setServletContext(servletContext);

        String configPath = envConfig.getConfigPath();
        migrateConfigFiles(configPath);
        springContext.addBeanFactoryPostProcessor(envConfig);
	}

}
