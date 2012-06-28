package org.fao.geonet;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.SQLException;

import javax.servlet.ServletContext;

import jeeves.interfaces.Logger;
import jeeves.resources.dbms.Dbms;
import jeeves.utils.Log;

import org.fao.geonet.constants.Geonet;
import org.fao.geonet.resources.Resources;

public final class Logo {
	private static Logger        		logger = Log.createLogger(Geonet.GEONETWORK);
	private Logo(){}

	/**
	 * Creates a default site logo, only if the logo image doesn't exists
	 *
	 * @param nodeUuid
	 * @param servletContext
	 * @param appPath
	 */
	public static void createSiteLogo(String nodeUuid, ServletContext servletContext, String appPath) {
	    try {
	        String logosDir = Resources.locateLogosDir(servletContext, appPath);
	        File logo = new File(logosDir, nodeUuid +".gif");
	        if (!logo.exists()) {
	            FileOutputStream os = new FileOutputStream(logo);
	            try {
	                os.write(Resources.loadImage(servletContext, appPath, "logos/dummy.gif", new byte[0]));
	                logger.info("      Setting catalogue logo for current node identified by: " + nodeUuid);
	            } finally {
	                os.close();
	            }
	        }
	    } catch (Exception e) {
	        logger.error("      Error when setting the logo: " + e.getMessage());
	    }
	}

	/**
		 * Copy the default dummy logo to the logo folder based on uuid
	     *
	     * @param servletContext
	     * @param dbms
	* @param nodeUuid
	* @param appPath
	* @throws FileNotFoundException
		 * @throws IOException
		 * @throws SQLException
		 */
		public static void initLogo(ServletContext servletContext, Dbms dbms, String nodeUuid, String appPath) {
			createSiteLogo(nodeUuid, servletContext, appPath);
			
			try {
				dbms.execute("UPDATE Settings SET value=? WHERE name='siteId'", nodeUuid);
			} catch (SQLException e) {
				logger.error("      Error when setting siteId values: " + e.getMessage());
			}
		}
}
