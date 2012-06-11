//=============================================================================
//===	Copyright (C) 2001-2005 Food and Agriculture Organization of the
//===	United Nations (FAO-UN), United Nations World Food Programme (WFP)
//===	and United Nations Environment Programme (UNEP)
//===
//===	This library is free software; you can redistribute it and/or
//===	modify it under the terms of the GNU Lesser General Public
//===	License as published by the Free Software Foundation; either
//===	version 2.1 of the License, or (at your option) any later version.
//===
//===	This library is distributed in the hope that it will be useful,
//===	but WITHOUT ANY WARRANTY; without even the implied warranty of
//===	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
//===	Lesser General Public License for more details.
//===
//===	You should have received a copy of the GNU Lesser General Public
//===	License along with this library; if not, write to the Free Software
//===	Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
//===
//===	Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
//===	Rome - Italy. email: GeoNetwork@fao.org
//==============================================================================

package jeeves.server.resources;

import java.util.HashMap;
import java.util.Map;

import jeeves.server.dispatchers.ServiceManager;
import jeeves.utils.Log;
import jeeves.utils.Util;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

//=============================================================================

/** This class contains all resource providers present into the config file.
  * on demand
  */

public class ProviderManager implements ApplicationContextAware
{

	private Map<String, ResourceProvider> hmProviders;

	//--------------------------------------------------------------------------
	//---
	//--- API methods
	//---
	//--------------------------------------------------------------------------

	
	private void error(String message) {
        Log.error(Log.RESOURCES, message);
        
    }
	//--------------------------------------------------------------------------

	public void end()
	{
		for (ResourceProvider provider : hmProviders.values()) {
		    Log.info(Log.RESOURCES, "   Stopping provider : " + provider.getClass().getName());
		    try {
                provider.end();
		    } catch (Exception e) {
		        Log.error(Log.RESOURCES, "Failure while stopping provider: " + provider.getClass().getName());
		    }
        }
	}

	//--------------------------------------------------------------------------

	public ResourceProvider getProvider(String name)
	{
		return hmProviders.get(name);
	}

	//--------------------------------------------------------------------------

	public Iterable<ResourceProvider> getProviders()
	{
		return hmProviders.values();
	}
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        ServiceManager serviceManager = applicationContext.getBean(ServiceManager.class);
        this.hmProviders = applicationContext.getBeansOfType(ResourceProvider.class);
        
        boolean resourceFound = false;
        Log.info(Log.RESOURCES, "Initializing resources...");

        for (Map.Entry<String, ResourceProvider> entry : this.hmProviders.entrySet()) {
            String name = entry.getKey();
            ResourceProvider provider = entry.getValue();
            try {
                // ensure the provider can be opened
                Object resource = provider.open();
                provider.close(resource);
                hmProviders.put(name, provider);
            } catch (Exception e) {
                Map<String, String> errorReport = new HashMap<String, String>();
                String eS = "Raised exception while initializing resource " + name + ". ----  Skipped ---- ";
                error(eS);
                errorReport.put("Error", eS);
                error("   Resource  : " + name);
                errorReport.put("Resource", name);
                error("   Provider  : " + provider);
                errorReport.put("Provider", provider.getClass().getName());
                error("   Exception : " + e);
                errorReport.put("Exception", e.toString());
                error("   Message   : " + e.getMessage());
                errorReport.put("Message", e.getMessage());
                error("   Stack     : " + Util.getStackTrace(e));
                errorReport.put("Stack", Util.getStackTrace(e));
                error(errorReport.toString());
                serviceManager.setStartupErrors(errorReport);
            }
        }
        if (!resourceFound) {
            Map<String, String> errorReport = new HashMap<String, String>();
            errorReport.put("Error", "No database resources found to initialize");
            error(errorReport.toString());
            serviceManager.setStartupErrors(errorReport);
        }

        
    }

	//--------------------------------------------------------------------------

	
}

//=============================================================================

