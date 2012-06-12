package jeeves.server;

import org.jdom.Element;

import jeeves.interfaces.Service;
import jeeves.server.context.ServiceContext;

public class TestService implements Service {

    private String result;

    @Override
    public void init(String appPath, ServiceConfig params) throws Exception {
        result = params.getValue("result", "No result param set");
    }

    @Override
    public Element exec(Element params, ServiceContext context) throws Exception {
        return new Element("result").setText(this.result);
    }

}
