package jeeves.server.dispatchers;

import java.util.List;

import javax.annotation.PostConstruct;

import org.jdom.Element;
import org.springframework.beans.factory.annotation.Required;

import jeeves.config.EnvironmentalConfig;
import jeeves.constants.Jeeves;
import jeeves.interfaces.Service;
import jeeves.server.context.ServiceContext;

/**
 * Contains the configuration for creating a Service object
 *  
 * @author jeichar
 */
public class ServiceConfigBean {
    private String name;
    private List<Param> params;
    private EnvironmentalConfig envConfig;
    private Service service;
    @Required
    public void setName(String name) {
        this.name = name;
    }
    @Required
    public void setParams(List<Param> params) {
        this.params = params;
    }
    @Required
    public void setEnvConfig(EnvironmentalConfig envConfig) {
        this.envConfig = envConfig;
    }

    @PostConstruct
    public void init() {
        try {
            Class<?> serviceClass = Class.forName(name);
            this.service = (Service) serviceClass.newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Element execute(Element params, ServiceContext context) throws Exception {
        try {
            Element response = service.exec(params, context);

            if (response == null)
                response = new Element(Jeeves.Elem.RESPONSE);

            // --- commit resources and return response

            context.getResourceManager().close();

            return response;
        } catch (Exception e) {
            // --- in case of exception we have to abort all resources

            context.getResourceManager().abort();
            ServiceManager.error("Exception when executing service");
            ServiceManager.error(" (C) Exc : " + e);

            throw e;
        }}
    
    
}
