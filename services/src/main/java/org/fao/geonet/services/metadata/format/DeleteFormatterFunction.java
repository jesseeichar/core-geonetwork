package org.fao.geonet.services.metadata.format;

import static org.fao.geonet.services.metadata.format.AddFormatterFunction.PARAM_NAME;
import static org.fao.geonet.services.metadata.format.AddFormatterFunction.PARAM_NAMESPACE;
import jeeves.interfaces.Service;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;
import org.fao.geonet.Util;
import org.fao.geonet.services.metadata.format.function.FormatterFunctionRepository;
import org.jdom.Element;

/**
 * Add a formatter widget library.  A widget library is an xslt file containing only functions.  The libraries are not permitted to
 * have anything other function declarations.
 * 
 * Created by Jesse on 3/15/14.
 */
public class DeleteFormatterFunction implements Service {

    @Override
    public void init(String appPath, ServiceConfig params) throws Exception {

    }

    @Override
    public Element exec(Element params, ServiceContext context) throws Exception {
        String namespace = Util.getParam(params, PARAM_NAMESPACE);
        String name = Util.getParam(params, PARAM_NAME);

        context.getBean(FormatterFunctionRepository.class).delete(namespace, name);

        return new Element("ok");
    }
}
