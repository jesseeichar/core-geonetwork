package jeeves.config;

import java.lang.reflect.Field;

import javax.annotation.PostConstruct;
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
    private String configPath;
    private boolean frozen = false;
    
    public EnvironmentalConfig() {
        // to allow spring to configure
    }
    public EnvironmentalConfig(String baseUrl, String appPath, String configPath) {
        super();
        this.baseUrl = baseUrl;
        this.appPath = appPath;
        this.configPath = configPath;
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
        checkState();
        this.servletContext = servletContext;
    }
    private void checkState() {
        if(frozen) {
            throw new IllegalStateException("Config has been frozen, you should not change it");
        }
    }
    /**
     * base name of the webapp like /geonetwork
     */
    public String getBaseUrl() {
        return baseUrl;
    }
    @Required
    public void setBaseUrl(String baseUrl) {
        checkState();

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
        checkState();
        this.appPath = appPath;
    }
    /**
     * Path to directory containing the configuration files
     */
    public String getConfigPath() {
        return configPath;
    }
    @Required
    public void setConfigPath(String configPath) {
        checkState();
        this.configPath = configPath;
    }
    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        // no action
    }
    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        GenericBeanDefinition beanDefinition = new GenericBeanDefinition();
        beanDefinition.setBeanClass(getClass());
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
    @PostConstruct
    public EnvironmentalConfig freeze() {
        frozen  = true;
        return this;
    }
}
