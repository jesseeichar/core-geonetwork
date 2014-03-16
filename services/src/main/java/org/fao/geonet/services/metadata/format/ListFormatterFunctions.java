package org.fao.geonet.services.metadata.format;

import jeeves.interfaces.Service;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;
import org.jdom.Element;

/**
 * Lists all the registered formatter functions.
 *
 * Created by Jesse on 3/15/14.
 */
public class ListFormatterFunctions implements Service {
    @Override
    public void init(String appPath, ServiceConfig params) throws Exception {

    }

    @Override
    public Element exec(Element params, ServiceContext context) throws Exception {
        return null;
    }
}
