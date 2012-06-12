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

package jeeves.server.dispatchers.guiservices;

import java.util.Collections;
import java.util.List;

import javax.annotation.PostConstruct;

import jeeves.config.EnvironmentalConfig;
import jeeves.constants.ConfigFile;
import jeeves.constants.Jeeves;
import jeeves.interfaces.Service;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;
import jeeves.server.dispatchers.Param;
import jeeves.server.dispatchers.ServiceConfigBean;
import jeeves.utils.Util;
import org.jdom.Element;
import org.springframework.beans.factory.annotation.Autowired;

//=============================================================================

public class Call implements GuiService {
    private String name;
    private String serviceClass;
    private List<Param> params = Collections.emptyList();
    private Service serviceObj;
    EnvironmentalConfig envConfig;

    // ---------------------------------------------------------------------------
    // ---
    // --- Init
    // ---
    // --------------------------------------------------------------------------

    public void setName(String name) {
        this.name = name;
    }

    public void setServiceClass(String serviceClass) {
        this.serviceClass = serviceClass;
    }

    public void setParam(List<Param> params) {
        this.params = params;
    }

    @PostConstruct
    public void init() throws Exception {
        params.add(new Param(Jeeves.Text.GUI_SERVICE, "yes"));
        serviceObj = ServiceConfigBean.createService(serviceClass, params, envConfig);
    }

    // ---------------------------------------------------------------------------
    // ---
    // --- Exec
    // ---
    // --------------------------------------------------------------------------

    public Element exec(Element response, ServiceContext context) throws Exception {
        try {
            // --- invoke the method and obtain a jdom result

            response = serviceObj.exec(response, context);

            context.getResourceManager().close();

            if (response != null)
                response.setName(name);

            return response;
        } catch (Exception e) {
            // --- in case of exception we have to abort all resources

            context.getResourceManager().abort();

            throw e;
        }
    }
}

// =============================================================================

