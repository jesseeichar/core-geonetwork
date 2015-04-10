package org.fao.geonet.transformer.groovy;

import jeeves.server.context.ServiceContext;
import org.fao.geonet.SystemInfo;
import org.fao.geonet.domain.Metadata;
import org.fao.geonet.transformer.ConfigFile;
import org.fao.geonet.transformer.FormatType;
import org.fao.geonet.transformer.FormatterWidth;
import org.jdom.Element;
import org.springframework.web.context.request.WebRequest;

import java.nio.file.Path;

/**
 * @author Jesse on 4/10/2015.
 */
public class TransformerParams {
    public WebRequest webRequest;
    public ServiceContext context;
    public Path formatDir;
    public Path viewFile;
    public Element metadata;
    public String schema;
    public Path schemaDir;
    public ConfigFile config;
    public String url;
    public Metadata metadataInfo;
    public FormatType formatType;
    public FormatterWidth width;
    public String lang3;

    public String param(String paramName, String defaultVal) {
        String[] values = webRequest.getParameterMap().get(paramName);
        if (values == null) {
            return defaultVal;
        }
        return values[0];
    }

    public String getLocUrl() {
        return url;
    }

    public boolean isDevMode() {
        return context.getBean(SystemInfo.class).isDevMode();
    }

    public TransformerParams copy() {
        TransformerParams transformerParams = new TransformerParams();
        copyDataToCopy(transformerParams);

        return transformerParams;
    }

    protected void copyDataToCopy(TransformerParams transformerParams) {
        transformerParams.config = this.config;
        transformerParams.webRequest = this.webRequest;
        transformerParams.context = this.context;
        transformerParams.schema = this.schema;
        transformerParams.metadata = this.metadata;
        transformerParams.formatDir = this.formatDir;
        transformerParams.formatType = this.formatType;
        transformerParams.url = this.url;
        transformerParams.viewFile = this.viewFile;
        transformerParams.metadataInfo = this.metadataInfo;
        transformerParams.schemaDir = this.schemaDir;
        transformerParams.lang3 = this.lang3;
    }

    public String getResourceUrl() {
        return null;
    }
}
