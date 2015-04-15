package org.fao.geonet.transformer;

import jeeves.server.context.ServiceContext;
import jeeves.server.dispatchers.ServiceManager;
import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.domain.Metadata;
import org.fao.geonet.transformer.groovy.TransformerParams;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.ServletWebRequest;

import java.nio.file.Path;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Utility class for applying transformations to XML documents.
 *
 * @author Jesse on 4/10/2015.
 */
@Component
public class Transform {

    @Autowired
    private GroovyTransformer groovyTransformer;
    @Autowired
    private ServiceManager serviceManager;
    @Autowired
    private ConfigurableApplicationContext applicationContext;

    public static Transform get() {
        final ServiceContext context = ServiceContext.get();
        if (context != null) {
            return context.getBean(Transform.class);
        }
        final ConfigurableApplicationContext applicationContext = ApplicationContextHolder.get();
        if (applicationContext != null) {
            return applicationContext.getBean(Transform.class);
        }
        throw new IllegalStateException("No ServiceContext or ApplicationContext in ThreadLocals");
    }

    public Element xml(Element xml, Path transformPath) throws Exception {
        Object result = apply(xml, transformPath, FormatType.xml, FormatterWidth._100);
        if (result instanceof StringBuilder) {
            StringBuilder stringBuilder = (StringBuilder) result;
            return Xml.loadString(stringBuilder.toString(), false);
        } else {
            return (Element) result;
        }
    }

    public Object apply(Element xml, Path transformPath, FormatType formatType, FormatterWidth formatterWidth) throws Exception {
        if (transformPath.getFileName().toString().toLowerCase().endsWith(".groovy")) {
            ServiceContext context = ServiceContext.get();
            if (context == null) {
                context = serviceManager.createServiceContext(transformPath.getFileName().toString(), applicationContext);
            }
            TransformerParams params = new TransformerParams();
            params.context = context;
            params.config = new ConfigFile(transformPath.getParent(), false, null);
            params.formatDir = null;
            params.formatType = formatType;
            params.lang3 = context.getLanguage();
            params.metadata = xml;
            params.metadataInfo = new Metadata();
            params.schema = null;
            params.viewFile = transformPath;
            params.width = formatterWidth;
            try {
                final HttpServletRequest servletRequest = context.getBean(HttpServletRequest.class);
                final HttpServletResponse response = context.getBean(HttpServletResponse.class);
                params.webRequest = new ServletWebRequest(servletRequest, response);
            } catch (NoSuchBeanDefinitionException e) {
                // ignore
            }
            return groovyTransformer.format(params);
        } else {
            return Xml.transform(xml, transformPath);
        }
    }
}
