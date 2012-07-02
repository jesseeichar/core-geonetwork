package org.fao.geonet;

import java.io.File;
import java.sql.SQLException;
import java.util.UUID;

import javax.servlet.ServletContext;

import jeeves.config.EnvironmentalConfig;
import jeeves.constants.Jeeves;
import jeeves.interfaces.Logger;
import jeeves.resources.dbms.Dbms;
import jeeves.server.resources.ResourceManager;
import jeeves.utils.Log;
import jeeves.utils.Util;

import org.fao.geonet.DatabaseSetupAndMigrationConfig.DbConfigFile;
import org.fao.geonet.DatabaseSetupAndMigrationConfig.Version;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.lib.DatabaseType;
import org.fao.geonet.lib.Lib;
import org.fao.geonet.lib.ServerLib;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.Lifecycle;

/**
 * This class initialize the environment required for several of the beans used in Geonetwork.
 * 
 * Some of its functionalities are:
 * 
 * <ul>
 * <li>Create database tables if necessary</li>
 * <li>Migrate data to new database version</li>
 * <li>Set the Xml Resolver catalog system property</li>
 * </ul>
 * 
 * In addition to performing system initialization the initializer also opens a DBMS instance
 * for other beans to use during setup.  However once the spring lifecycle event "start" is
 * fired the DBMS instance is closed and is no longer usable.
 *  
 * @author jeichar
 *
 */
public class GeonetworkInitializer implements Lifecycle{
	private static Logger        		logger = Log.createLogger(Log.DBMS);

	private Dbms dbms;
	private boolean running = false;
	private boolean created = false;

    //----------------------------------------------
    // -- Injected by spring
    //----------------------------------------------
	private final DatabaseSetupAndMigrationConfig dbConfiguration;
	private final EnvironmentalConfig envConfig;
	private final ResourceManager resourceManager;
    //----------------------------------------------

	@Autowired
	public GeonetworkInitializer(
			DatabaseSetupAndMigrationConfig dbConfiguration,
			EnvironmentalConfig envConfig,
			ResourceManager resourceManager
			) throws Exception {
		this.dbConfiguration = dbConfiguration;
		this.envConfig = envConfig;
		this.resourceManager = resourceManager;
		ServerLib sl = new ServerLib(envConfig.getServletContext(), envConfig.getAppPath());
		String version = sl.getVersion();
		String subVersion = sl.getSubVersion();

		logger.info("Initializing GeoNetwork " + version +  "." + subVersion +  " ...");

		initDatabase();
		migrateDatabase(version, subVersion);
		
		setProps(envConfig.getAppPath());
	}
	
	public Dbms getDbms() { return dbms; }
	public boolean isCreated() { return created; }

	/**
	 * Set system properties to those required
	 * @param path webapp path
	 */
	private void setProps(String path) {

		String webapp = path + "WEB-INF" + File.separator;

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
		
    /**
     * Parses a version number removing extra "-*" element and returning an integer. "2.7.0-SNAPSHOT" is returned as 270.
     * 
     * @param number The version number to parse
     * @return The version number as an integer
     * @throws Exception
     */
    private int parseVersionNumber(String number) throws Exception {
        // Remove extra "-SNAPSHOT" info which may be in version number
        int dashIdx = number.indexOf("-");
        if (dashIdx != -1) {
            number = number.substring(0, number.indexOf("-"));
        }
        return Integer.valueOf(number.replaceAll("\\.", ""));
    }
    
	/**
	 * Database initialization. If no table in current database
	 * create the GeoNetwork database. If an existing GeoNetwork database 
	 * exists, try to migrate the content.
	 * 
	 * @param context
	 * @return Pair with Dbms channel and Boolean set to true if db created
	 * @throws Exception
	 */
	private void initDatabase() throws Exception {
		try {
			dbms = (Dbms) this.resourceManager.open(Geonet.Res.MAIN_DB);
		} catch (Exception e) {
			logger.error("    Failed to open database connection, Check config.xml db file configuration.");
			logger.error(Util.getStackTrace(e));
			throw new IllegalArgumentException("No database connection");
		}
		String appPath = envConfig.getAppPath();

		String dbURL = dbms.getURL();
		logger.info("  - Database connection on " + dbURL + " ...");

        ServletContext servletContext = envConfig.getServletContext();

		// Create db if empty
		if (!Lib.db.touch(dbms)) {
			logger.info("      " + dbURL + " is an empty database (Metadata table not found).");

			dbConfiguration.createDatabase(servletContext, dbms, appPath);
			dbConfiguration.addData(servletContext, dbms, appPath);
	        dbms.commit();
            
			// Copy logo
			String uuid = UUID.randomUUID().toString();
			Logo.initLogo(servletContext, dbms, uuid, envConfig.getAppPath());
			this.created = true;
		} else {
			logger.info("      Found an existing GeoNetwork database.");
		}

	}

	/**
	 * Checks if current database is running same version as the web application.
	 * If not, apply migration SQL script :
	 *  resources/sql/migration/{version}-to-{version}-{dbtype}.sql.
	 * eg. 2.4.3-to-2.5.0-default.sql
     */
	private void migrateDatabase(String webappVersion, String webappSubVersion) throws SQLException {
		logger.info("  - Migration ...");
		String appPath = envConfig.getAppPath();
		SettingManager settingsManager = new SettingManager(this);
		// Get db version and subversion
		String dbVersion = settingsManager.getValue("system/platform/version");
		String dbSubVersion = settingsManager.getValue("system/platform/subVersion");
		
		// Migrate db if needed
		logger.info("      Webapp   version:" + webappVersion + " subversion:" + webappSubVersion);
		logger.info("      Database version:" + dbVersion + " subversion:" + dbSubVersion);
		if (dbVersion == null || webappVersion == null) {
			logger.warning("      Database does not contain any version information. Check that the database is a GeoNetwork database with data." + 
							"      Migration step aborted.");
			return;
		}
		
		int from = 0, to = 0;

		try {
		    from = parseVersionNumber(dbVersion);
		    to = parseVersionNumber(webappVersion);
		} catch(Exception e) {
		    logger.warning("      Error parsing version numbers: " + e.getMessage());
            e.printStackTrace();
		}
		
		if (from == to
				//&& subVersion.equals(dbSubVersion) Check only on version number
		) {
			logger.info("      Webapp version = Database version, no migration task to apply.");
		} else {
			boolean anyMigrationAction = false;
			boolean anyMigrationError = false;
			
            try {
            	new UpdateHarvesterIdsTask().update(settingsManager,dbms);
            } catch (Exception e) {
                logger.info("          Errors occurs during SQL migration file: " + e.getMessage());
                e.printStackTrace();
                anyMigrationError = true;
            }

			// Migrating from 2.0 to 2.5 could be done 2.0 -> 2.3 -> 2.4 -> 2.5
			String dbType = DatabaseType.lookup(dbms).toString();
			logger.debug("      Migrating from " + from + " to " + to + " (dbtype:" + dbType + ")...");
			
		    logger.info("      Loading SQL migration step configuration from config-db.xml ...");
            for(Version version : dbConfiguration.migrate) {
                int versionNumber = version.version;
                if (versionNumber > from && versionNumber <= to) {
                    logger.info("       - running tasks for " + versionNumber + "...");
                    for(DbConfigFile file : version.file) {
						String filePath = appPath + file.path;
                        String filePrefix = file.filePrefix;
                        anyMigrationAction = true;
                        logger.info("         - SQL migration file:" + filePath + " prefix:" + filePrefix + " ...");
                        try {
                            Lib.db.insertData(envConfig.getServletContext(), dbms, appPath, filePath, filePrefix);
                        } catch (Exception e) {
                            logger.info("          Errors occurs during SQL migration file: " + e.getMessage());
                            e.printStackTrace();
                            anyMigrationError = true;
                        }
                    }
                }
            }
			
    		
			// Update the logo 
			String siteId = settingsManager.getValue("system/site/siteId");
			Logo.initLogo(envConfig.getServletContext(), dbms, siteId, appPath);
			
			// TODO : Maybe a force rebuild index is required in such situation.
			
			if (anyMigrationAction && !anyMigrationError) {
			    logger.info("      Successfull migration.\n" +
                        "      Catalogue administrator still need to update the catalogue\n" +
                        "      logo and data directory in order to complete the migration process.\n" +
                        "      Lucene index rebuild is also recommended after migration."
			            );
			}
			
			if (!anyMigrationAction) {
                logger.warning("      No migration task found between webapp and database version.\n" +
                        "      The system may be unstable or may failed to start if you try to run \n" +
                        "      the current GeoNetwork " + webappVersion + " with an older database (ie. " + dbVersion + "\n" +
                        "      ). Try to run the migration task manually on the current database\n" +
                        "      before starting the application or start with a new empty database.\n" +
                        "      Sample SQL scripts for migration could be found in WEB-INF/sql/migrate folder.\n"
                        );
                
            }
			
			if (anyMigrationError) {
                logger.warning("      Error occurs during migration. Check the log file for more details.");
            }
			// TODO : Maybe some migration stuff has to be done in Java ?
		}
	}

	@Override
	public void start() {
		try {
			resourceManager.close(Geonet.Res.MAIN_DB, dbms);
			running = true;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void stop() {
		running = false;
	}

	@Override
	public boolean isRunning() {
		// do nothing
		return running;
	}

	
}
