//=============================================================================
//===	Copyright (C) 2001-2007 Food and Agriculture Organization of the
//===	United Nations (FAO-UN), United Nations World Food Programme (WFP)
//===	and United Nations Environment Programme (UNEP)
//===
//===	This program is free software; you can redistribute it and/or modify
//===	it under the terms of the GNU General Public License as published by
//===	the Free Software Foundation; either version 2 of the License, or (at
//===	your option) any later version.
//===
//===	This program is distributed in the hope that it will be useful, but
//===	WITHOUT ANY WARRANTY; without even the implied warranty of
//===	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
//===	General Public License for more details.
//===
//===	You should have received a copy of the GNU General Public License
//===	along with this program; if not, write to the Free Software
//===	Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
//===
//===	Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
//===	Rome - Italy. email: geonetwork@osgeo.org
//==============================================================================

package org.fao.geonet;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.nio.charset.Charset;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.servlet.ServletContext;

import jeeves.JeevesJCS;
import jeeves.JeevesProxyInfo;
import jeeves.config.EnvironmentalConfig;
import jeeves.constants.Jeeves;
import jeeves.interfaces.ApplicationHandler;
import jeeves.interfaces.Logger;
import jeeves.resources.dbms.Dbms;
import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;
import jeeves.server.dispatchers.ServiceManager;
import jeeves.utils.Log;
import jeeves.utils.ProxyInfo;
import jeeves.utils.Util;
import jeeves.utils.XmlResolver;
import jeeves.xlink.Processor;

import org.fao.geonet.constants.Geonet;
import org.fao.geonet.kernel.AccessManager;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.SchemaManager;
import org.fao.geonet.kernel.SvnManager;
import org.fao.geonet.kernel.ThesaurusManager;
import org.fao.geonet.kernel.XmlSerializer;
import org.fao.geonet.kernel.XmlSerializerDb;
import org.fao.geonet.kernel.XmlSerializerSvn;
import org.fao.geonet.kernel.csw.CatalogDispatcher;
import org.fao.geonet.kernel.csw.CswHarvesterResponseExecutionService;
import org.fao.geonet.kernel.harvest.HarvestManager;
import org.fao.geonet.kernel.oaipmh.OaiPmhDispatcher;
import org.fao.geonet.kernel.search.LuceneConfig;
import org.fao.geonet.kernel.search.SearchManager;
import org.fao.geonet.kernel.setting.SettingInfo;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.languages.IsoLanguagesMapper;
import org.fao.geonet.languages.LanguageDetector;
import org.fao.geonet.notifier.MetadataNotifierControl;
import org.fao.geonet.notifier.MetadataNotifierManager;
import org.fao.geonet.services.util.z3950.Repositories;
import org.fao.geonet.services.util.z3950.Server;
import org.fao.geonet.util.ThreadPool;
import org.fao.geonet.util.ThreadUtils;
import org.geotools.data.DataStore;
import org.geotools.data.shapefile.indexed.IndexType;
import org.geotools.data.shapefile.indexed.IndexedShapefileDataStore;
import org.geotools.feature.AttributeTypeBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.referencing.CRS;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.vividsolutions.jts.geom.MultiPolygon;

/**
 * This is the main class, it handles http connections and inits the system.
  */
public class Geonetwork implements ApplicationHandler {

    private static final String       SPATIAL_INDEX_FILENAME    = "spatialindex";
    private static final String       IDS_ATTRIBUTE_NAME        = "id";

	private Logger        		logger;
    //----------------------------------------------
    // -- Injected by spring in future
    //----------------------------------------------
	private SearchManager 		searchMan;
	private HarvestManager 		harvestMan;
	private ThesaurusManager 	thesaurusMan;
	private MetadataNotifierControl metadataNotifierControl;
	private ThreadPool        threadPool;
	//----------------------------------------------

	//----------------------------------------------
    // -- NOT injected by spring
    //----------------------------------------------
	private String   FS         = File.separator;
	
    GeonetContext geonetworkContext = new GeonetContext();
    //----------------------------------------------

    //----------------------------------------------
    // -- Injected by spring
    //----------------------------------------------
    private EnvironmentalConfig envConfig;
    private GeonetworkConfig config;
    private ServiceManager serviceMan;
    private SchemaManager schemaManager;
    private InitializedDbms initdbms;
    private SettingManager settingManager;
    private OaiPmhDispatcher oaipmhDispatcher;
    //----------------------------------------------

	@Autowired
	public Geonetwork(EnvironmentalConfig envConfig, GeonetworkConfig config, 
	        ServiceManager serviceMan, 
	        SchemaManager schemaManager,
	        InitializedDbms initdbms,
	        SettingManager settingMan,
	        OaiPmhDispatcher oaipmhDis) {
        this.config = config;
        this.envConfig = envConfig;
        this.serviceMan = serviceMan;
        this.schemaManager = schemaManager;
        this.initdbms = initdbms;
        this.settingManager = settingMan;
        this.oaipmhDispatcher = oaipmhDis;
    }
	
	
	
	//---------------------------------------------------------------------------
	//---
	//--- GetContextName
	//---
	//---------------------------------------------------------------------------

	@Override
	public String getContextName() { return Geonet.CONTEXT_NAME; }
	@Override
	public Object getContext() throws Exception {
	    return geonetworkContext;
	}

	/**
     * Inits the engine, loading all needed data.
	  */
	@PostConstruct
	public void init() throws Exception {
	    ServiceContext context = serviceMan.createServiceContext(getContextName());
		logger = Log.createLogger(Geonet.GEONETWORK);

		String appPath = envConfig.getAppPath();
		
        ServletContext servletContext = envConfig.getServletContext();
        
		// Init configuration directory
		GeonetworkDataDirectory dataDirectory = config.getDataDirectories();
		
		// Get config handler properties
		String systemDataDir = dataDirectory.getSystemDataDir();
		String thesauriDir = dataDirectory.getCodelistDir();
		String luceneDir =  dataDirectory.getLuceneDir();
		logger.info("Data directory: " + systemDataDir);

		setProps(appPath);

		// Status actions class - load it
		Class<?> statusActionsClass = Class.forName(config.getStatusActionsClassName());

		JeevesJCS.setConfigFilename(appPath + "WEB-INF/classes/cache.ccf");

		// force caches to be config'd so shutdown hook works correctly
		@SuppressWarnings("unused")
		JeevesJCS jcsDummy = JeevesJCS.getInstance(Processor.XLINK_JCS);
		jcsDummy = JeevesJCS.getInstance(XmlResolver.XMLRESOLVER_JCS);

		Dbms dbms = initdbms.getDbms();
		boolean created = initdbms.isCreated();

		//------------------------------------------------------------------------
		//--- initialize thread pool 

		logger.info("  - Thread Pool...");

		threadPool = new ThreadPool();

		//--- initialize ThreadUtils with setting manager and rm props
		ThreadUtils.init(context.getResourceManager().getProps(Geonet.Res.MAIN_DB),
		              	 settingManager); 


		//------------------------------------------------------------------------
		//--- initialize Z39.50

		logger.info("  - Z39.50...");

		boolean z3950Enable    = settingManager.getValueAsBool("system/z3950/enable", false);
		String  z3950port      = settingManager.getValue("system/z3950/port");
		String  host           = settingManager.getValue(Geonet.Settings.SERVER_HOST);

		// null means not initialized
		ApplicationContext app_context = null;

		// build Z3950 repositories file first from template
		URL url = getClass().getClassLoader().getResource(Geonet.File.JZKITCONFIG_TEMPLATE);

		if (Repositories.build(url, context)) {
			logger.info("     Repositories file built from template.");

			try {
				app_context = new  ClassPathXmlApplicationContext( Geonet.File.JZKITAPPLICATIONCONTEXT );

				// to have access to the GN context in spring-managed objects
				ContextContainer cc = (ContextContainer)app_context.getBean("ContextGateway");
				cc.setSrvctx(context);

				if (!z3950Enable)
					logger.info("     Server is Disabled.");
				else
				{
					logger.info("     Server is Enabled.");
		
					UserSession session = new UserSession();
					session.authenticate(null, "z39.50", "", "", "Guest", "");
					context.setUserSession(session);
					context.setIpAddress("127.0.0.1");
					Server.init(host, z3950port, appPath, context, app_context);
				}	
			} catch (Exception e) {
				logger.error("     Repositories file init FAILED - Z3950 server disabled and Z3950 client services (remote search, harvesting) may not work. Error is:" + e.getMessage());
				e.printStackTrace();
			}
			
		} else {
			logger.error("     Repositories file builder FAILED - Z3950 server disabled and Z3950 client services (remote search, harvesting) may not work.");
		}


		//------------------------------------------------------------------------
		//--- initialize search and editing

		logger.info("  - Search...");

		logger.info("  - Log spatial object: " + config.isStatLogSpatialObjects());
		logger.info("  - Log in asynch mode: " + config.isStatLogAsynch());
        
		LuceneConfig lc = this.config.getLuceneConfig();
        logger.info("  - Lucene configuration is:");
        logger.info(lc.toString());
       
		DataStore dataStore = context.getResourceManager().getDataStore(Geonet.Res.MAIN_DB);
		if (dataStore == null) {
			dataStore = createShapefileDatastore(luceneDir);
		}

		//--- no datastore for spatial indexing means that we can't continue
		if (dataStore == null) {
			throw new IllegalArgumentException("GeoTools datastore creation failed - check logs for more info/exceptions");
		}

		searchMan = new SearchManager(appPath, config, 
				dataStore, 
				new SettingInfo(settingManager), schemaManager, servletContext);

		//------------------------------------------------------------------------
		//--- extract intranet ip/mask and initialize AccessManager

		logger.info("  - Access manager...");

		AccessManager accessMan = new AccessManager(dbms, settingManager);

		//------------------------------------------------------------------------
		//--- get edit params and initialize DataManager

		logger.info("  - Xml serializer and Data manager...");

		SvnManager svnManager = null;
		XmlSerializer xmlSerializer = null;
		if (config.isUseSubversion()) {
			svnManager = new SvnManager(context, settingManager, dataDirectory.getSubversionPath(), dbms, created);
			xmlSerializer = new XmlSerializerSvn(settingManager, svnManager);
		} else {
			xmlSerializer = new XmlSerializerDb(settingManager);
		}

		DataManager dataMan = new DataManager(context, svnManager, xmlSerializer, schemaManager, searchMan, accessMan, dbms, settingManager, config);


        /**
         * Initialize iso languages mapper
         */
        IsoLanguagesMapper.getInstance().init(dbms);
        
        /**
         * Initialize language detector
         */
        LanguageDetector.init(appPath + config.getLanguageProfilesDir(), context, dataMan);

		//------------------------------------------------------------------------
		//--- Initialize thesaurus

		logger.info("  - Thesaurus...");

		thesaurusMan = ThesaurusManager.getInstance(appPath, dataMan, context.getResourceManager(), thesauriDir);

		//------------------------------------------------------------------------
		//--- initialize harvesting subsystem

		logger.info("  - Harvest manager...");

		harvestMan = new HarvestManager(context, settingManager, dataMan);
		dataMan.setHarvestManager(harvestMan);

		//------------------------------------------------------------------------
		//--- initialize catalogue services for the web

		logger.info("  - Catalogue services for the web...");

		CatalogDispatcher catalogDis = new CatalogDispatcher(config);

        //------------------------------------------------------------------------
		//--- initialize metadata notifier subsystem
        MetadataNotifierManager metadataNotifierMan = new MetadataNotifierManager(dataMan);

        logger.info("  - Metadata notifier ...");

		//------------------------------------------------------------------------
		//--- return application context

		geonetworkContext.accessMan   = accessMan;
		geonetworkContext.dataMan     = dataMan;
		geonetworkContext.searchMan   = searchMan;
		geonetworkContext.schemaMan   = schemaManager;
		geonetworkContext.config      = config;
		geonetworkContext.catalogDis  = catalogDis;
		geonetworkContext.settingMan  = settingManager;
		geonetworkContext.harvestMan  = harvestMan;
		geonetworkContext.thesaurusMan= thesaurusMan;
		geonetworkContext.oaipmhDis   = oaipmhDispatcher;
		geonetworkContext.app_context = app_context;
        geonetworkContext.metadataNotifierMan = metadataNotifierMan;
		geonetworkContext.threadPool  = threadPool;
		geonetworkContext.xmlSerializer  = xmlSerializer;
		geonetworkContext.svnManager  = svnManager;
		geonetworkContext.statusActionsClass = statusActionsClass;

		logger.info("Site ID is : " + geonetworkContext.getSiteId());

        // Creates a default site logo, only if the logo image doesn't exists
        // This can happen if the application has been updated with a new version preserving the database and
        // images/logos folder is not copied from old application 
        Logo.createSiteLogo(geonetworkContext.getSiteId(), servletContext, context.getAppPath());

        // Notify unregistered metadata at startup. Needed, for example, when the user enables the notifier config
        // to notify the existing metadata in database
        // TODO: Fix DataManager.getUnregisteredMetadata and uncomment next lines
        metadataNotifierControl = new MetadataNotifierControl(context, geonetworkContext);
        metadataNotifierControl.runOnce();

		//--- load proxy information from settings into Jeeves for observers such
		//--- as jeeves.utils.XmlResolver to use
		ProxyInfo pi = JeevesProxyInfo.getInstance();
		boolean useProxy = settingManager.getValueAsBool("system/proxy/use", false);
		if (useProxy) {
			String  proxyHost      = settingManager.getValue("system/proxy/host");
			String  proxyPort      = settingManager.getValue("system/proxy/port");
			String  username       = settingManager.getValue("system/proxy/username");
			String  password       = settingManager.getValue("system/proxy/password");
			pi.setProxyInfo(proxyHost, new Integer(proxyPort), username, password);
		}

	}

	/**
	 * Set system properties to those required
	 * @param path webapp path
	 */
	private void setProps(String path) {

		String webapp = path + "WEB-INF" + FS;

		//--- Set jeeves.xml.catalog.files property
		//--- this is critical to schema support so must be set correctly
		String catalogProp = System.getProperty(Jeeves.XML_CATALOG_FILES);
		if (catalogProp == null) catalogProp = "";
		if (!catalogProp.equals("")) {
			logger.info("Overriding "+Jeeves.XML_CATALOG_FILES+" property (was set to "+catalogProp+")");
		} 
		catalogProp = webapp + "oasis-catalog.xml;" + envConfig.getConfigPath() + File.separator + "schemaplugin-uri-catalog.xml";
		System.setProperty(Jeeves.XML_CATALOG_FILES, catalogProp);
		logger.info(Jeeves.XML_CATALOG_FILES+" property set to "+catalogProp);

		//--- Set mime-mappings
		String mimeProp = System.getProperty("mime-mappings");
		if (mimeProp == null) mimeProp = "";
		if (!mimeProp.equals("")) {
			logger.info("Overriding mime-mappings property (was set to "+mimeProp+")");
		} 
		mimeProp = webapp + "mime-types.properties";
		System.setProperty("mime-mappings", mimeProp);
		logger.info("mime-mappings property set to "+mimeProp);

	}
		
		
	//---------------------------------------------------------------------------
	//---
	//--- Stop
	//---
	//---------------------------------------------------------------------------
	@PreDestroy
	public void stop() {
		logger.info("Stopping geonetwork...");
		
        logger.info("shutting down CSW HarvestResponse executionService");
        CswHarvesterResponseExecutionService.getExecutionService().shutdownNow();		

		//------------------------------------------------------------------------
		//--- end search
		logger.info("  - search...");

		try
		{
			searchMan.end();
		}
		catch (Exception e)
		{
			logger.error("Raised exception while stopping search");
			logger.error("  Exception : " +e);
			logger.error("  Message   : " +e.getMessage());
			logger.error("  Stack     : " +Util.getStackTrace(e));
		}

		
		logger.info("  - ThreadPool ...");
		threadPool.shutDown();
		
		logger.info("  - MetadataNotifier ...");
		try {
			metadataNotifierControl.shutDown();
		} catch (Exception e) {
			logger.error("Raised exception while stopping metadatanotifier");
			logger.error("  Exception : " +e);
			logger.error("  Message   : " +e.getMessage());
			logger.error("  Stack     : " +Util.getStackTrace(e));
		}

			
		logger.info("  - Harvest Manager...");
		harvestMan.shutdown();

		logger.info("  - Z39.50...");
		Server.end();
	}

	//---------------------------------------------------------------------------

	private DataStore createShapefileDatastore(String indexDir) throws Exception {

		File file = new File(indexDir + "/" + SPATIAL_INDEX_FILENAME + ".shp");
		file.getParentFile().mkdirs();
		if (!file.exists()) {
			logger.info("Creating shapefile "+file.getAbsolutePath());
		} else {
			logger.info("Using shapefile "+file.getAbsolutePath());
		}
		IndexedShapefileDataStore ids = new IndexedShapefileDataStore(file.toURI().toURL(), new URI("http://geonetwork.org"), true, true, IndexType.QIX, Charset.defaultCharset());
		CoordinateReferenceSystem crs = CRS.decode("EPSG:4326");

		if (crs != null) {
			ids.forceSchemaCRS(crs);
		}

		if (!file.exists()) {
			SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
			AttributeDescriptor geomDescriptor = new AttributeTypeBuilder().crs(DefaultGeographicCRS.WGS84).binding(MultiPolygon.class).buildDescriptor("the_geom");
			builder.setName(SPATIAL_INDEX_FILENAME);
			builder.add(geomDescriptor);
			builder.add(IDS_ATTRIBUTE_NAME, String.class);
			ids.createSchema(builder.buildFeatureType());
		}	

		logger.info("NOTE: Using shapefile for spatial index, this can be slow for larger catalogs");
		return ids;
	}
}
