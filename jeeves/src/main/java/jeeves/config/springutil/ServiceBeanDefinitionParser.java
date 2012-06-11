package jeeves.config.springutil;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import jeeves.constants.ConfigFile;
import jeeves.server.dispatchers.ErrorPage;
import jeeves.server.dispatchers.OutputPage;
import jeeves.server.dispatchers.Param;
import jeeves.server.dispatchers.ServiceConfigBean;
import jeeves.server.dispatchers.ServiceInfo;
import jeeves.server.dispatchers.guiservices.Call;
import jeeves.server.dispatchers.guiservices.XmlFile;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class ServiceBeanDefinitionParser extends AbstractSingleBeanDefinitionParser {
    @Override
    protected Class<?> getBeanClass(Element element) {
        return ServiceInfo.class;
    }

    @Override
    protected void doParse(Element element, BeanDefinitionBuilder builder) {
        if (element.hasAttribute(ConfigFile.Service.Attr.NAME)) {
            addPropertyValue(builder, element, ConfigFile.Service.Attr.NAME);
        } else {
            builder.addPropertyValue(ConfigFile.Service.Attr.NAME, element.getAttribute("id"));
        }
        addPropertyValue(builder, element, ConfigFile.Service.Attr.NAME);
        addPropertyValue(builder, element, ConfigFile.Service.Attr.MATCH);
        addPropertyValue(builder, element, ConfigFile.Service.Attr.SHEET);
        addPropertyValue(builder, element, ConfigFile.Service.Attr.CACHE);

        List<BeanDefinition> classes = new LinkedList<BeanDefinition>();
        NodeList children = element.getElementsByTagName(ConfigFile.Service.Child.CLASS);
        for(int i = 0; i < children.getLength(); i++) { 
            classes.add(parseServiceClass((Element) children.item(i)));
        }
        builder.addPropertyValue("serviceConfig", classes);
        
        List<BeanDefinition> outputs = new LinkedList<BeanDefinition>();
        children = element.getElementsByTagName(ConfigFile.Service.Child.OUTPUT);
        for(int i = 0; i < children.getLength(); i++) { 
            outputs.add(parseServiceOutput((Element) children.item(i)));
        }
        builder.addPropertyValue(ConfigFile.Service.Child.OUTPUT, outputs);

        List<BeanDefinition> errors = new LinkedList<BeanDefinition>();
        children = element.getElementsByTagName(ConfigFile.Service.Child.ERROR);
        for(int i = 0; i < children.getLength(); i++) { 
            errors.add(parseServiceError(element));
        }
        builder.addPropertyValue(ConfigFile.Service.Child.ERROR, outputs);
    }

    private BeanDefinition parseServiceClass(Element element) {
        BeanDefinitionBuilder builder = BeanDefinitionBuilder.rootBeanDefinition(ServiceConfigBean.class);
        addPropertyValue(builder, element, ConfigFile.Class.Attr.NAME);
        
        NodeList paramsList = element.getElementsByTagName(ConfigFile.Class.Child.PARAM);
        List<BeanDefinition> params = new ArrayList<BeanDefinition>();
        for(int i = 0; i < paramsList.getLength(); i++) {
            BeanDefinitionBuilder paramBuilder = BeanDefinitionBuilder.rootBeanDefinition(Param.class);
            Element paramDef = (Element) paramsList.item(i);
            
            addPropertyValue(paramBuilder, paramDef, ConfigFile.Param.Attr.NAME);
            addPropertyValue(paramBuilder, paramDef, ConfigFile.Param.Attr.VALUE);
        }
        builder.addPropertyValue(ConfigFile.Class.Child.PARAM, params);
        return builder.getBeanDefinition();
    }

    private BeanDefinition parseServiceOutput(Element element) {
        BeanDefinitionBuilder builder = BeanDefinitionBuilder.rootBeanDefinition(OutputPage.class);
        addPropertyValue(builder, element, ConfigFile.Output.Attr.SHEET);
        addPropertyValue(builder, element, ConfigFile.Output.Attr.BLOB);
        addPropertyValue(builder, element, ConfigFile.Output.Attr.CONTENT_TYPE);
        addPropertyValue(builder, element, ConfigFile.Output.Attr.FILE);
        addPropertyValue(builder, element, ConfigFile.Output.Attr.FORWARD);
        addPropertyValue(builder, element, ConfigFile.Output.Attr.TEST);
        
        NodeList paramsList = element.getElementsByTagName(ConfigFile.Class.Child.PARAM);
        List<BeanDefinition> params = new ArrayList<BeanDefinition>();
        for(int i = 0; i < paramsList.getLength(); i++) {
            Element paramDef = (Element) paramsList.item(i);
            
            params.add(parseGuiService(paramDef));
        }
        builder.addPropertyValue("guiServices", params);
        return builder.getBeanDefinition();
    }

    private BeanDefinition parseServiceError(Element element) {
        BeanDefinitionBuilder builder = BeanDefinitionBuilder.rootBeanDefinition(ErrorPage.class);
        addPropertyValue(builder, element, ConfigFile.Error.Attr.SHEET);
        addPropertyValue(builder, element, ConfigFile.Error.Attr.STATUS_CODE);
        addPropertyValue(builder, element, ConfigFile.Error.Attr.CONTENT_TYPE);
        
        NodeList paramsList = element.getElementsByTagName(ConfigFile.Class.Child.PARAM);
        List<BeanDefinition> params = new ArrayList<BeanDefinition>();
        for(int i = 0; i < paramsList.getLength(); i++) {
            Element paramDef = (Element) paramsList.item(i);
            
            params.add(parseGuiService(paramDef));
        }
        builder.addPropertyValue("guiServices", params);
        return builder.getBeanDefinition();
        
    }
    
    private void addPropertyValue(BeanDefinitionBuilder builder, Element element, String name) {
        if (element.hasAttribute(name)) {
            String value = element.getAttribute(name);
            builder.addPropertyValue(name, value);
        }

    }
    private BeanDefinition parseGuiService(Element elem) {
        if (ConfigFile.Output.Child.XML.equals(elem.getLocalName())) {
            BeanDefinitionBuilder builder = BeanDefinitionBuilder.rootBeanDefinition(XmlFile.class);
            addPropertyValue(builder, elem, ConfigFile.Xml.Attr.NAME);
            addPropertyValue(builder, elem, ConfigFile.Xml.Attr.BASE);
            addPropertyValue(builder, elem, ConfigFile.Xml.Attr.FILE);
            addPropertyValue(builder, elem, ConfigFile.Xml.Attr.LANGUAGE);
            addPropertyValue(builder, elem, ConfigFile.Xml.Attr.LOCALIZED);

            return builder.getBeanDefinition();
        }

        if (ConfigFile.Output.Child.CALL.equals(elem.getLocalName())) {
            BeanDefinitionBuilder builder = BeanDefinitionBuilder.rootBeanDefinition(Call.class);
            addPropertyValue(builder, elem, ConfigFile.Xml.Attr.NAME);
            addPropertyValue(builder, elem, ConfigFile.Xml.Attr.BASE);
            addPropertyValue(builder, elem, ConfigFile.Xml.Attr.FILE);
            addPropertyValue(builder, elem, ConfigFile.Xml.Attr.LANGUAGE);
            addPropertyValue(builder, elem, ConfigFile.Xml.Attr.LOCALIZED);

            return builder.getBeanDefinition();
        }
        throw new IllegalArgumentException("Unknown GUI element : "+ elem.getLocalName());
    }
}
