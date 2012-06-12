package jeeves.config.springutil;

import org.jdom.Content;
import org.jdom.Element;

import jeeves.interfaces.Service;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;

public class TestService implements Service {

    private ServiceConfig params;
    private Element requestParams;
    private String appPath;

    @Override
    public void init(String appPath, ServiceConfig params) throws Exception {
        this.params = params;
        this.appPath = appPath;
    }

    @Override
    public Element exec(Element params, ServiceContext context) throws Exception {
        this.requestParams = params;
        return new Element("result").addContent((Content) params.clone());
    }

    public Element getRequestParams() {
        return requestParams;
    }
    public ServiceConfig getParams() {
        return params;
    }
    public String getAppPath() {
        return appPath;
    }
}
