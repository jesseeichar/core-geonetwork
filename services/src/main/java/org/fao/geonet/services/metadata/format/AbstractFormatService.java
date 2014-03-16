package org.fao.geonet.services.metadata.format;

import org.fao.geonet.exceptions.BadParameterEx;
import jeeves.interfaces.Service;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.kernel.GeonetworkDataDirectory;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.services.Utils;
import org.jdom.Element;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.regex.Pattern;

/**
 * Common constants and methods for Metadata formatter classes
 * 
 * @author jeichar
 */
abstract class AbstractFormatService implements Service {
    protected static final Pattern ID_XSL_REGEX = Pattern.compile("[\\w0-9\\-_]+");
    protected static final String VIEW_XSL_FILENAME = "view.xsl";

    public void init(String appPath, ServiceConfig params) throws Exception {}

    protected void checkLegalId(String paramName, String xslid) throws BadParameterEx {
        if (FileFormatterFunctionRepository.FUNCTION_DIRECTORY.equalsIgnoreCase(xslid)) {
            throw new BadParameterEx(paramName, xslid);
        }
        if(!ID_XSL_REGEX.matcher(xslid).matches()) {
            throw new BadParameterEx(paramName, xslid+" is not a legal ID: only the following are permitted in the id:"+ID_XSL_REGEX);
        }
    }
	protected String getMetadataSchema(Element params, ServiceContext context)
			throws Exception {
		String metadataId = Utils.getIdentifierFromParameters(params, context);
    	GeonetContext gc = (GeonetContext) context
				.getHandlerContext(Geonet.CONTEXT_NAME);
		DataManager dm = gc.getBean(DataManager.class);
        return dm.getMetadataSchema(metadataId);
	}
    protected static boolean containsFile(File container, File desiredFile) throws IOException {
        String canonicalDesired = desiredFile.getCanonicalPath();
        String canonicalContainer = container.getCanonicalPath();
        return canonicalDesired.startsWith(canonicalContainer);
    }
    protected File getAndVerifyFormatDir(GeonetworkDataDirectory dataDirectory, String paramName, String xslid) throws BadParameterEx, IOException {
        if (xslid == null) {
            throw new BadParameterEx(paramName, "missing "+paramName+" param");
        }
        
        checkLegalId(paramName, xslid);
        File formatDir = new File(dataDirectory.getMetadataFormatterDir(), xslid);
        
        if(!formatDir.exists()) {
            throw new BadParameterEx(paramName, "Format bundle "+xslid+" does not exist");
        }
        
        if(!formatDir.isDirectory()) {
            throw new BadParameterEx(paramName, "Format bundle "+xslid+" is not a directory");
        }
        
        if(!new File(formatDir, VIEW_XSL_FILENAME).exists()) {
            throw new BadParameterEx(paramName, "Format bundle "+xslid+" is not a valid format bundle because it does not have a "+VIEW_XSL_FILENAME+" file");
        }
        
        if (!containsFile(dataDirectory.getMetadataFormatterDir(), formatDir)) {
            throw new BadParameterEx(paramName, "Format bundle "+xslid+" is not a format bundle id because it does not reference a file contained within the userXslDir");
        }
        return formatDir;
    }

    protected static class FormatterFilter implements FileFilter {
        @Override
        public boolean accept(File file) {
            return file.isDirectory() && new File(file, VIEW_XSL_FILENAME).exists();
        }
    }
    
}
