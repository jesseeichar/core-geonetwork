package org.fao.geonet.services.metadata.format;

import org.fao.geonet.constants.Params;
import org.fao.geonet.transformer.groovy.TransformerParams;

/**
 * @author Jesse on 10/15/2014.
 */
public class FormatterParams extends TransformerParams {
    public Format format;
    public boolean formatterInSchemaPlugin;

    @Override
    public String getResourceUrl() {
        String xslid = param("xsl", null);
        String schemaParam = "";

        if (formatterInSchemaPlugin) {
            schemaParam = Params.SCHEMA + "=" + schema + "&";
        }

        return getLocUrl() + "/md.formatter.resource?" + schemaParam +
                             Params.ID + "=" + xslid + "&" + Params.FNAME + "=";
    }

    @Override
    public TransformerParams copy() {
        final FormatterParams formatterParams = new FormatterParams();
        copyDataToCopy(formatterParams);
        formatterParams.format = this.format;
        formatterParams.formatterInSchemaPlugin = this.formatterInSchemaPlugin;
        return formatterParams;
    }
}
