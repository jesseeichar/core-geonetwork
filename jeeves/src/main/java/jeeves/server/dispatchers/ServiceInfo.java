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

package jeeves.server.dispatchers;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import jeeves.config.EnvironmentalConfig;
import jeeves.constants.Jeeves;
import jeeves.server.context.ServiceContext;
import jeeves.utils.Xml;

import org.jdom.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Required;

//=============================================================================

/** A container class for a service. It collect the method and the filter
  */

public class ServiceInfo
{
	private String  match;
	private String  sheet;
	private boolean cache = false;
	private String name;

	private List<ServiceConfigBean> servicesConfigBeans = Collections.emptyList();
	private List<OutputPage> outputs = Collections.emptyList();
	private List<ErrorPage> errors = Collections.emptyList();
    private EnvironmentalConfig envConfig;

	//--------------------------------------------------------------------------
	//---
	//--- API methods
	//---
	//--------------------------------------------------------------------------
    @Autowired
    public void setEnvConfig(EnvironmentalConfig envConfig) {
        this.envConfig = envConfig;
    }
    public String getName() {
        return name;
    }
    @Required
    public void setName(String name) {
        this.name = name;
    }
    public void setServiceClass(List<ServiceConfigBean> configs) {
        this.servicesConfigBeans = configs;
    }
    public void setOutput(List<OutputPage> pages) {
        this.outputs = pages;
    }
    public void setError(List<ErrorPage> pages) {
        this.errors = pages;
    }
	public void setMatch(String match)
	{
		if (match != null && match.trim().equals(""))
			match = null;

		this.match = match;
	}

	//--------------------------------------------------------------------------

	public void setSheet(String sheet)
	{
		if (sheet != null && sheet.trim().equals(""))
			sheet = null;

		this.sheet = sheet;
	}

	//--------------------------------------------------------------------------

	public void setCache(String cache)
	{
		this.cache = "yes".equals(cache);
	}

	//--------------------------------------------------------------------------

	public boolean isCacheSet() { return cache; }

	//--------------------------------------------------------------------------

	public Element execServices(Element params, ServiceContext context) throws Exception
	{
		if (params == null)
			params = new Element(Jeeves.Elem.REQUEST);

		//--- transform input request using a given stylesheet

		params = transformInput(params);

		if (servicesConfigBeans.size() == 0)
		{
			params.setName(Jeeves.Elem.RESPONSE);
			return params;
		}

		Element response = params;

		for(ServiceConfigBean service : servicesConfigBeans) {
			response = service.execute(response, context);
		}

		//--- we must detach the element from its parent because the output dispatcher
		//--- links it to the root element
		//--- note that caching is not allowed in any case

		response.detach();

		return response;
	}

	//--------------------------------------------------------------------------

	public OutputPage findOutputPage(Element response) throws Exception {
		for (OutputPage page : outputs) {
			if (page.matches(response))
				return page;
		}

		return null;
	}

	//--------------------------------------------------------------------------

	public ErrorPage findErrorPage(String id) {
		for (ErrorPage page : errors) {
			if (page.matches(id))
				return page;
		}

		return null;
	}

	//---------------------------------------------------------------------------
	/** Returns true if the service input has elements that match this page
	  */

	public boolean matches(Element request) throws Exception
	{
		if (match == null)
			return true;
		else
			return jeeves.utils.Xml.selectBoolean(request, match);
	}

	//---------------------------------------------------------------------------
	//---
	//--- Private methods
	//---
	//---------------------------------------------------------------------------

	private Element transformInput(Element request) throws Exception
	{
		if (sheet == null)
			return request;

		String styleSheet = envConfig.getAppPath() + Jeeves.Path.XSL + sheet;

		ServiceManager.info("Transforming input with stylesheet : " +styleSheet);

		try
		{
			Element result = Xml.transform(request, styleSheet);
			ServiceManager.info("End of input transformation");

			return result;
		}
		catch(Exception e)
		{
			ServiceManager.error("Exception during transformation");
			ServiceManager.error("  (C) message is : "+ e.getMessage());

			Throwable t = e;

			while(t.getCause() != null)
			{
				ServiceManager.error("  (C) message is : "+ t.getMessage());
				t = t.getCause();
			}

			throw e;
		}
	}

}

//=============================================================================

