package org.fao.geonet.services.metadata.format;

import org.jdom.JDOMException;

import java.io.IOException;

/**
 * This is for backwards compatibility.  Just delegates to the moved class.
 *
 * @author Jesse on 11/28/2014.
 */
public class SchemaLocalizations {

    public static org.fao.geonet.transformer.SchemaLocalizations create(String schema) throws IOException, JDOMException {
        return org.fao.geonet.transformer.SchemaLocalizations.create(schema);
    }
}
