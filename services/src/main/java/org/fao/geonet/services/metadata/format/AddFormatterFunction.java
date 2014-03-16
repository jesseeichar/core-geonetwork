package org.fao.geonet.services.metadata.format;

import jeeves.interfaces.Service;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;
import org.jdom.Element;

/**
 * Add a formatter widget library.  A widget library is an xslt file containing only functions.  The libraries are not permitted to
 * have anything other function declarations.
 * 
 * Created by Jesse on 3/15/14.
 */
public class AddFormatterFunction implements Service {
    private static final String FUNCTION_DIR = "shared-functions";
    public static final String PARAM_NAMESPACE = "namespace";
    public static final String PARAM_NAME = "name";
    public static final String PARAM_FUNCTION = "function";

    @Override
    public void init(String appPath, ServiceConfig params) throws Exception {

    }

    @Override
    public Element exec(Element params, ServiceContext context) throws Exception {
        return null;
    }
}
