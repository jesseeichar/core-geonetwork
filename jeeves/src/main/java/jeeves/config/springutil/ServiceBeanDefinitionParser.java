package jeeves.config.springutil;


import jeeves.constants.ConfigFile;
import jeeves.server.dispatchers.ErrorPage;
import jeeves.server.dispatchers.OutputPage;
import jeeves.server.dispatchers.Param;
import jeeves.server.dispatchers.ServiceConfigBean;
import jeeves.server.dispatchers.ServiceInfo;
import jeeves.server.dispatchers.guiservices.Call;
import jeeves.server.dispatchers.guiservices.XmlFile;

import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.ManagedList;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
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
        addPropertyValue(builder, element, ConfigFile.Service.Attr.MATCH);
        addPropertyValue(builder, element, ConfigFile.Service.Attr.SHEET);
        addPropertyValue(builder, element, ConfigFile.Service.Attr.CACHE);

        ManagedList<BeanDefinition> classes = new ManagedList<BeanDefinition>();
        NodeList children = element.getElementsByTagName(ConfigFile.Service.Child.SERVICECLASS);
        for(int i = 0; i < children.getLength(); i++) { 
            classes.add(parseServiceClass((Element) children.item(i)));
        }
        if (!classes.isEmpty()) {
            builder.addPropertyValue(ConfigFile.Service.Child.SERVICECLASS, classes);
        }
        
        ManagedList<BeanDefinition> outputs = new ManagedList<BeanDefinition>();
        children = element.getElementsByTagName(ConfigFile.Service.Child.OUTPUT);
        for(int i = 0; i < children.getLength(); i++) { 
            outputs.add(parseServiceOutput((Element) children.item(i)));
        }
        if (!outputs.isEmpty()) {
            builder.addPropertyValue(ConfigFile.Service.Child.OUTPUT, outputs);
        }

        ManagedList<BeanDefinition> errors = new ManagedList<BeanDefinition>();
        children = element.getElementsByTagName(ConfigFile.Service.Child.ERROR);
        for(int i = 0; i < children.getLength(); i++) { 
            errors.add(parseServiceError(element));
        }
        if (!errors.isEmpty()) {
            builder.addPropertyValue(ConfigFile.Service.Child.ERROR, errors);
        }
    }

    private BeanDefinition parseServiceClass(Element element) {
        BeanDefinitionBuilder builder = BeanDefinitionBuilder.rootBeanDefinition(ServiceConfigBean.class);
        addPropertyValue(builder, element, ConfigFile.Class.Attr.NAME, true);
        
        parseParam(element, builder);
        return builder.getBeanDefinition();
    }

    private void parseParam(Element element, BeanDefinitionBuilder builder) {
        NodeList paramsList = element.getElementsByTagName(ConfigFile.Class.Child.PARAM);
        ManagedList<BeanDefinition> params = new ManagedList<BeanDefinition>();
        for(int i = 0; i < paramsList.getLength(); i++) {
            BeanDefinitionBuilder paramBuilder = BeanDefinitionBuilder.rootBeanDefinition(Param.class);
            Element paramDef = (Element) paramsList.item(i);
            
            addPropertyValue(paramBuilder, paramDef, ConfigFile.Param.Attr.NAME);
            addPropertyValue(paramBuilder, paramDef, ConfigFile.Param.Attr.VALUE);
        }
        if (!params.isEmpty()) {
            builder.addPropertyValue(ConfigFile.Class.Child.PARAM, params);
        }
    }

    private BeanDefinition parseServiceOutput(Element element) {
        BeanDefinitionBuilder builder = BeanDefinitionBuilder.rootBeanDefinition(OutputPage.class);
        addPropertyValue(builder, element, ConfigFile.Output.Attr.SHEET);
        addPropertyValue(builder, element, ConfigFile.Output.Attr.BLOB);
        addPropertyValue(builder, element, ConfigFile.Output.Attr.CONTENT_TYPE);
        addPropertyValue(builder, element, ConfigFile.Output.Attr.FILE);
        addPropertyValue(builder, element, ConfigFile.Output.Attr.FORWARD);
        addPropertyValue(builder, element, ConfigFile.Output.Attr.TEST);

        parseGuiServices(builder, element);
        return builder.getBeanDefinition();
    }

    private BeanDefinition parseServiceError(Element element) {
        BeanDefinitionBuilder builder = BeanDefinitionBuilder.rootBeanDefinition(ErrorPage.class);
        
        addPropertyValue(builder, element, ConfigFile.Error.Attr.SHEET);
        addPropertyValue(builder, element, ConfigFile.Error.Attr.STATUS_CODE);
        addPropertyValue(builder, element, ConfigFile.Error.Attr.CONTENT_TYPE);
        parseGuiServices(builder, element);
        
        return builder.getBeanDefinition();
        
    }
    
    private void addPropertyValue(BeanDefinitionBuilder builder, Element element, String name) {
        addPropertyValue(builder, element, name, false);
    }
    private void addPropertyValue(BeanDefinitionBuilder builder, Element element, String name, String propName, boolean required) {
        
        if (element.hasAttribute(name)) {
            String value = element.getAttribute(name);
            builder.addPropertyValue(propName, value);
        } else if (required) {
            throw new BeanInitializationException("Property "+name+" of "+element.getLocalName()+" is a required element");
        }
    }
    private void addPropertyValue(BeanDefinitionBuilder builder, Element element, String name, boolean required) {
        addPropertyValue(builder, element, name, name, required);
    }

    private void parseGuiServices(BeanDefinitionBuilder builder, Element element) {
        NodeList nodeList = element.getChildNodes();
        ManagedList<BeanDefinition> guiServices = new ManagedList<BeanDefinition>();
        for(int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            if(node instanceof Element) {
                Element elem = (Element) node;
                if (elem.getLocalName().equals(ConfigFile.Output.Child.CALL) ||
                        elem.getLocalName().equals(ConfigFile.Output.Child.XML))
                guiServices.add(parseGuiService(elem));
            }
        }

        builder.addPropertyValue("guiServices", guiServices);
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
            addPropertyValue(builder, elem, ConfigFile.Call.Attr.NAME);
            addPropertyValue(builder, elem, ConfigFile.Call.Attr.CLASS);
            parseParam(elem, builder);
            return builder.getBeanDefinition();
        }
        throw new IllegalArgumentException("Unknown GUI element : "+ elem.getLocalName());
    }
}
