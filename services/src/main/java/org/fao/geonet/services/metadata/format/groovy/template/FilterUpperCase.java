package org.fao.geonet.services.metadata.format.groovy.template;

/**
 * Uppercase the string.
 *
 * @author Jesse on 12/19/2014.
 */
public class FilterUpperCase implements TextContentFilter {

    @Override
    public String process(TRenderContext context, String rawValue) {
        return rawValue.toUpperCase();
    }
}
