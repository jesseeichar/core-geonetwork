/**
 * 
 */
package org.fao.geonet.maintenance.service;

import org.jdom.Element;

import jeeves.interfaces.Service;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;

/**
 * Create a report of all the reported issues found during the maintenance.
 *
 * @author Jesse
 */
public class Report implements Service {

    @Override
    public void init(String appPath, ServiceConfig params) throws Exception {
        
    }

    @Override
    public Element exec(Element params, ServiceContext context) throws Exception {
        // TODO Auto-generated method stub
        return null;
    }

}
