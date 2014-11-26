package org.fao.geonet.services.metadata.format;

import com.google.common.collect.Sets;
import com.google.common.io.Files;
import org.fao.geonet.Constants;
import org.fao.geonet.kernel.GeonetworkDataDirectory;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Set;

/**
 * Strategy for formatting using an xslt based formatter.
 *
 * <p>
 *     Note: to include files from the formatter dir you can use @@formatterDir@@ and it will be replaced with the
 *     path to the formatter dir.
 * </p>
 *
 * @author Jesse on 10/15/2014.
 */
@Component
public class XsltFormatter {
    @Autowired
    GeonetworkDataDirectory dataDirectory;

    public Element format(FormatterParams fparams) throws Exception {
        String lang = fparams.config.getLang(fparams.context.getLanguage());
        Iterable<SchemaLocalization> localization = fparams.format.getSchemaLocalizations(fparams.context).values();

        Element root = new Element("root");

        root.addContent(new Element("lang").setText(fparams.context.getLanguage()));
        root.addContent(new Element("url").setText(fparams.url));
        root.addContent(new Element("locUrl").setText(fparams.getLocUrl()));

        root.addContent(new Element("resourceUrl").setText(fparams.getResourceUrl()));
        root.addContent(fparams.metadata);
        root.addContent(fparams.format.getPluginLocResources(fparams.context, fparams.formatDir, lang));
        if (fparams.config.loadStrings()) {
            root.addContent(fparams.format.getStrings(fparams.context.getAppPath(), lang));
        }

        Element schemas = new Element("schemas");
        root.addContent(schemas);

        List<String> schemasToLoadList = fparams.config.listOfSchemasToLoad();

        String schemasToLoad = fparams.config.schemasToLoad();
        if (!"none".equalsIgnoreCase(schemasToLoad)) {
            for (SchemaLocalization schemaLocalization : localization) {
                String currentSchema = schemaLocalization.schema.trim();
                if ("all".equalsIgnoreCase(schemasToLoad) || schemasToLoadList.contains(currentSchema.toLowerCase())) {
                    Element schemaEl = new Element(currentSchema);
                    schemas.addContent(schemaEl);

                    schemaEl.addContent((Element) schemaLocalization.getLabels().clone());
                    schemaEl.addContent((Element) schemaLocalization.getCodelists().clone());
                    schemaEl.addContent((Element) schemaLocalization.getStrings().clone());
                }
            }
        }
        if (!"false".equalsIgnoreCase(fparams.param("debug", "false"))) {
            return root;
        }
        Element transformed = Xml.transform(root, fparams.viewFile.getAbsolutePath());

        Element response = new Element("metadata");
        response.addContent(transformed);
        return response;
    }
}
