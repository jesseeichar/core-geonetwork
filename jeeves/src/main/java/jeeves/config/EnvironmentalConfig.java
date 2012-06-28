package jeeves.config;

import java.io.File;
import java.lang.reflect.Field;

import javax.servlet.ServletContext;

import org.springframework.beans.BeansException;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.GenericBeanDefinition;

/**
 * Represents the configuration options that has to be set at startup time by the bootstrapping system
 * (JeevesServlet, Jeevelet)
 * 
 * @author jeichar
 */
public class EnvironmentalConfig implements BeanDefinitionRegistryPostProcessor {

    private ServletContext servletContext;
    private String baseUrl;
    private String appPath;
    private volatile String configPath;
    
    public EnvironmentalConfig() {
        // to allow spring to configure
    }
    public EnvironmentalConfig(ServletContext context) {
    	this.servletContext = context;
    	this.baseUrl = context.getContextPath();
    	this.appPath = context.getRealPath(".")+"/";
    }
    public EnvironmentalConfig(String baseUrl, String appPath) {
        super();
        this.baseUrl = baseUrl;
        this.appPath = appPath;
    }
    /**
     * get Optional servlet context.  May be null
     */
    public ServletContext getServletContext() {
        return servletContext;
    }
    /**
     * Set servlet context, should only be configurator
     */
    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
    }
    /**
     * base name of the webapp like /geonetwork
     */
    public String getBaseUrl() {
        return baseUrl;
    }
    @Required
    public void setBaseUrl(String baseUrl) {
        if (!baseUrl.startsWith("/") && baseUrl.length() != 0) {
            baseUrl = "/"+ baseUrl;
        }

        this.baseUrl = baseUrl;
    }
    /**
     * Path to the webapp resources
     */
    public String getAppPath() {
        return appPath;
    }
    @Required
    public void setAppPath(String appPath) {
        this.appPath = appPath;
    }
    /**
     * Path to directory containing the configuration files
     */
    public String getConfigPath() {
        if (configPath == null) {
            synchronized (this) {
                if (configPath == null) {
                    this.configPath = appPath + "WEB-INF" + File.separator;
                    if (servletContext != null) {
                        String key = getWebappName() + ".config.dir";

                        if (servletContext.getInitParameter(key) != null) {
                            configPath = servletContext.getInitParameter(key);
                        } else if (System.getProperty(key) != null) {
                            configPath = System.getProperty(key);
                        }
                    }
                }
            }
        }
        return configPath;
    }
    @Required
    public void setConfigPath(String configPath) {
        this.configPath = configPath;
    }
    public String getWebappName() { return baseUrl.substring(1); }
    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        // no action
    }
    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        GenericBeanDefinition beanDefinition = new GenericBeanDefinition();
        beanDefinition.setBeanClass(EnvironmentalConfig.class);
        MutablePropertyValues propertyValues = new MutablePropertyValues();
        for(Field field: getClass().getDeclaredFields()) {
            try {
                field.setAccessible(true);
                if(!field.getName().equals("frozen")) {
                    propertyValues.addPropertyValue(field.getName(), field.get(this));
                }
            } catch (Exception e) {
                throw new BeanInitializationException("Unable to create EnvironmentalConfig Bean due to:"+e.getMessage(), e);
            }
        }
        beanDefinition.setPropertyValues(propertyValues);
        registry.registerBeanDefinition("envConfig", beanDefinition);
    }
}
