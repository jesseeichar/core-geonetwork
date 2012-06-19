//==============================================================================
//===
//===   JeevesEngine
//===
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

package jeeves.server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.annotation.PreDestroy;
import javax.servlet.ServletContext;
import javax.xml.transform.TransformerConfigurationException;

import jeeves.config.DefaultConfig;
import jeeves.config.EnvironmentalConfig;
import jeeves.config.GeneralConfig;
import jeeves.constants.ConfigFile;
import jeeves.interfaces.ApplicationHandler;
import jeeves.monitor.MonitorManager;
import jeeves.server.dispatchers.ServiceManager;
import jeeves.server.resources.ResourceManager;
import jeeves.server.sources.ServiceRequest;
import jeeves.utils.Log;
import jeeves.utils.TransformerFactoryFactory;
import jeeves.utils.Util;

import org.apache.log4j.PropertyConfigurator;
import org.jdom.Element;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

//=============================================================================

/** This is the main class. It handles http connections and inits the system
  */

public class JeevesEngine implements ApplicationContextAware
{
	private static final String TRANSFORMER_PATH = "/WEB-INF/classes/META-INF/services/javax.xml.transform.TransformerFactory";
	
	private EnvironmentalConfig envConfig;
	private GeneralConfig generalConfig;
	private DefaultConfig defaultConfig;

	private ResourceManager resourceManager;
	private MonitorManager monitorManager;
	private ServiceManager  serviceMan;
	
	// TODO spring migration
	private ScheduleManager scheduleMan = new ScheduleManager();

	private Collection<ApplicationHandler> appHandlers;
    
    // default for testing
    ResourceManager getResourceManager() {
        return resourceManager;
    }
    
    @Autowired
    public JeevesEngine(
            EnvironmentalConfig envConfig,
            GeneralConfig generalConfig, 
            DefaultConfig defaultConfig, 
            ResourceManager resourceManager,
            MonitorManager monitorManager,
            ServiceManager serviceManager) throws TransformerConfigurationException, IOException {
        this.envConfig = envConfig;
        this.monitorManager = monitorManager;
        this.resourceManager = resourceManager;
        this.serviceMan = serviceManager;
        this.generalConfig = generalConfig;
        this.defaultConfig = defaultConfig;
        
        File log4jConfigFile = new File(envConfig.getConfigPath(), "log4j.cfg");
        if(log4jConfigFile.exists()) {
        	PropertyConfigurator.configure(log4jConfigFile.getPath());
        }
        setupXSLTTransformerFactory();
    }

    /**
     * Looks up the implementation of XSLT factory defined in META-INF/services/javax.xml.transform.TransformerFactory and instantiates
     * that implementation. This way, a conflicting setting in System Properties is overridden for this application only.
     *
     * @throws IOException
     * @throws TransformerConfigurationException
     */
    private void setupXSLTTransformerFactory() throws IOException, TransformerConfigurationException {
    	
        ServletContext servlet = envConfig.getServletContext();
        
    	InputStream in = null;
    	// In debug mode, Jeeves may load a different file
    	// Load javax.xml.transform.TransformerFactory from application path instead
    	if(servlet != null) {
    		in = servlet.getResourceAsStream(TRANSFORMER_PATH);
    	}
    	if(in == null){
    		File f = new File(envConfig.getAppPath() + TRANSFORMER_PATH);
    		if(f.exists()) {
    		    in = new FileInputStream(f);
    		}
    	}
    	
    	if(in == null) {
    	    warning("xsl Transformer not found");
    	    return;
    	}
        try {
            
            if(in != null) {
                BufferedReader br = new BufferedReader(new InputStreamReader(in));
                String line;
                while ((line = br.readLine()) != null)   {
                    if(line == null || line.length() == 0) {
                        warning("Malformed definition of XSLT transformer (in: META-INF/services/javax.xml.transform.TransformerFactory).");
                    }
                    TransformerFactoryFactory.init(line);
                    break;
                }
                in.close();
            }
        }
        catch(IOException x) {
        	String msg = "Definition of XSLT transformer not found (tried: " + new File(envConfig.getAppPath() + TRANSFORMER_PATH).getCanonicalPath() + ")";
        	if(servlet != null) {
        		msg += " and servlet.getServletContext().getResourceAsStream("+TRANSFORMER_PATH+")";
        	}
        	warning(msg);
            error(x.getMessage());
            x.printStackTrace();
        }
        finally {
            if(in != null) {
                in.close();
            }
        }
    }

	//---------------------------------------------------------------------------
	//---
	//--- 'services' element
	//---
	//---------------------------------------------------------------------------

	/** Setup services found in the services tag (config.xml)
	  */

	//---------------------------------------------------------------------------
	//---
	//--- 'schedules' element
	//---
	//---------------------------------------------------------------------------

	/** Setup schedules found in the 'schedules' element (config.xml)
	  */

	@SuppressWarnings("unchecked")
	private void initSchedules(Element schedules) throws Exception
	{
		info("Initializing schedules...");

		//--- get schedules root package
		String pack = schedules.getAttributeValue(ConfigFile.Schedules.Attr.PACKAGE);

		// --- scan schedules elements
		for (Element schedule : (List<Element>) schedules
				.getChildren(ConfigFile.Schedules.Child.SCHEDULE)) {
			String name = schedule
					.getAttributeValue(ConfigFile.Schedule.Attr.NAME);

			info("   Adding schedule : " + name);

			try {
				scheduleMan.addSchedule(pack, schedule);
			} catch (Exception e) {
				error("Raised exception while registering schedule. Skipped.");
				error("   Schedule  : " + name);
				error("   Package   : " + pack);
				error("   Exception : " + e);
				error("   Message   : " + e.getMessage());
				error("   Stack     : " + Util.getStackTrace(e));
			}
		}
	}

    //---------------------------------------------------------------------------
    //---
    //--- 'schedules' element
    //---
    //---------------------------------------------------------------------------

    /** Setup schedules found in the 'schedules' element (config.xml)
     */
	//---------------------------------------------------------------------------
	//---
	//--- Destroy
	//---
	//---------------------------------------------------------------------------
	@PreDestroy
	public void destroy()
	{
		try
		{
			info("=== Stopping system ========================================");

			info("Shutting down monitor manager...");
			monitorManager.shutdown();

			info("Stopping schedule manager...");
			scheduleMan.exit();

			info("=== System stopped ========================================");
		}
		catch (Exception e)
		{
			error("Raised exception during destroy");
			error("  Exception : " +e);
			error("  Message   : " +e.getMessage());
			error("  Stack     : " +Util.getStackTrace(e));
		}
	}

	//---------------------------------------------------------------------------
	//---
	//--- API methods
	//---
	//---------------------------------------------------------------------------

	public String getUploadDir() { return generalConfig.getUploadDir(); }

	//---------------------------------------------------------------------------

  public int getMaxUploadSize() { return generalConfig.getMaxUploadSize(); }

  //---------------------------------------------------------------------------

	public void dispatch(ServiceRequest srvReq, UserSession session)
	{
		if (srvReq.getService() == null || srvReq.getService().length() == 0)
			srvReq.setService(defaultConfig.getService());

		if (srvReq.getLanguage() == null || srvReq.getLanguage().length() == 0)
			srvReq.setLanguage(defaultConfig.getLanguage());

		srvReq.setDebug(srvReq.hasDebug() && generalConfig.isDebug());

		//--- if we have a startup error (ie. exception during startup) then
		//--- override with the startupErrorSrv service (if defined)
		String startupErrorSrv = defaultConfig.getStartupErrorService();
        if (serviceMan.isStartupError() && !startupErrorSrv.equals("") 
				&& !srvReq.getService().contains(startupErrorSrv))
			srvReq.setService(startupErrorSrv);

		//--- normal dispatch pipeline

		serviceMan.dispatch(srvReq, session);
	}

	//---------------------------------------------------------------------------
	//---
	//--- Other private methods
	//---
	//---------------------------------------------------------------------------

	private void info   (String message) { Log.info   (Log.ENGINE, message); }
	private void warning(String message) { Log.warning(Log.ENGINE, message); }
	private void error  (String message) { Log.error  (Log.ENGINE, message); }

	@Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		appHandlers = applicationContext.getBeansOfType(ApplicationHandler.class).values();
    }
}

//=============================================================================


