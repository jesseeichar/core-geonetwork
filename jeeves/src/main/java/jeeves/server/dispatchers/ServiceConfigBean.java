package jeeves.server.dispatchers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.PostConstruct;

import org.jdom.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Required;

import jeeves.config.EnvironmentalConfig;
import jeeves.constants.Jeeves;
import jeeves.interfaces.Service;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;

/**
 * Contains the configuration for creating a Service object
 *  
 * @author jeichar
 */
public class ServiceConfigBean {
    private String name;
    private List<Param> param = Collections.emptyList();
    private EnvironmentalConfig envConfig;
    private Service serviceObj;
    @Required
    public void setName(String name) {
        this.name = name;
    }
    public void setParam(List<Param> param) {
        this.param = param;
    }
    @Autowired
    public void setEnvConfig(EnvironmentalConfig envConfig) {
        this.envConfig = envConfig;
    }

    @PostConstruct
    public void init() {
        this.serviceObj = createService(name, param, envConfig);
    }

    public static Service createService(String className, List<Param> param, EnvironmentalConfig envConfig) {
        try {
            Class<?> serviceClass = Class.forName(className);
            Service service = (Service) serviceClass.newInstance();
            List<Element> el = new ArrayList<Element>();
            for (Param p : param) {
                el.add(p.toElem());
            }
            service.init(envConfig.getAppPath(), new ServiceConfig(el));
            return service;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }
    public Element execute(Element params, ServiceContext context) throws Exception {
        try {
            Element response = serviceObj.exec(params, context);

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
