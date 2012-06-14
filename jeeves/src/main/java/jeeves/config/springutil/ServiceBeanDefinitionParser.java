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
import org.springframework.beans.factory.support.RootBeanDefinition;
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
        String packageName = element.getAttribute(ConfigFile.Service.Attr.PACKAGE);

        ManagedList<BeanDefinition> classes = new ManagedList<BeanDefinition>();
        NodeList children = element.getElementsByTagName(ConfigFile.Service.Child.SERVICECLASS);
        for(int i = 0; i < children.getLength(); i++) { 
            classes.add(parseServiceClass((Element) children.item(i), packageName));
        }
        if (!classes.isEmpty()) {
            builder.addPropertyValue(ConfigFile.Service.Child.SERVICECLASS, classes);
        }
        
        ManagedList<BeanDefinition> outputs = new ManagedList<BeanDefinition>();
        children = element.getElementsByTagName(ConfigFile.Service.Child.OUTPUT);
        for(int i = 0; i < children.getLength(); i++) { 
            outputs.add(parseServiceOutput((Element) children.item(i), packageName));
        }
        if (!outputs.isEmpty()) {
            builder.addPropertyValue(ConfigFile.Service.Child.OUTPUT, outputs);
        }

        ManagedList<BeanDefinition> errors = new ManagedList<BeanDefinition>();
        children = element.getElementsByTagName(ConfigFile.Service.Child.ERROR);
        for(int i = 0; i < children.getLength(); i++) { 
            errors.add(parseServiceError((Element) children.item(i), packageName));
        }
        if (!errors.isEmpty()) {
            builder.addPropertyValue(ConfigFile.Service.Child.ERROR, errors);
        }
    }

    private BeanDefinition parseServiceClass(Element element, String packageName) {
        BeanDefinitionBuilder builder = newBuilder(ServiceConfigBean.class);
        String serviceNameAtt = ConfigFile.Class.Attr.NAME;
        setServiceName(element, packageName, builder, serviceNameAtt);

        parseParam(element, builder);
        return builder.getBeanDefinition();
    }

    private void setServiceName(Element element, String packageName, BeanDefinitionBuilder builder, String serviceNameAtt) {
        if(element.hasAttribute(serviceNameAtt)) {
            String name = element.getAttribute(serviceNameAtt);
            if(name.startsWith(".")) {
                name = packageName+name;
            }
            builder.addPropertyValue(serviceNameAtt, name);
        } else {
            error(element, serviceNameAtt);
        }
    }

    private void parseParam(Element element, BeanDefinitionBuilder builder) {
        
        NodeList paramsList = element.getElementsByTagName(ConfigFile.Class.Child.PARAM);
        ManagedList<BeanDefinition> params = new ManagedList<BeanDefinition>();
        for(int i = 0; i < paramsList.getLength(); i++) {
            BeanDefinitionBuilder paramBuilder = newBuilder(Param.class);
            Element paramDef = (Element) paramsList.item(i);
         
            addPropertyValue(paramBuilder, paramDef, ConfigFile.Param.Attr.NAME);
            addPropertyValue(paramBuilder, paramDef, ConfigFile.Param.Attr.VALUE);
            params.add(paramBuilder.getBeanDefinition());
        }
        if (!params.isEmpty()) {
            builder.addPropertyValue(ConfigFile.Class.Child.PARAM, params);
        }
    }

    private BeanDefinition parseServiceOutput(Element element, String packageName) {
        BeanDefinitionBuilder builder = newBuilder(OutputPage.class);
        addPropertyValue(builder, element, ConfigFile.Output.Attr.SHEET);
        addPropertyValue(builder, element, ConfigFile.Output.Attr.BLOB);
        addPropertyValue(builder, element, ConfigFile.Output.Attr.CONTENT_TYPE);
        addPropertyValue(builder, element, ConfigFile.Output.Attr.FILE);
        addPropertyValue(builder, element, ConfigFile.Output.Attr.FORWARD);
        addPropertyValue(builder, element, ConfigFile.Output.Attr.TEST);

        parseGuiServices(builder, element, packageName);
        return builder.getBeanDefinition();
    }

    private BeanDefinition parseServiceError(Element element, String packageName) {
        BeanDefinitionBuilder builder = newBuilder(ErrorPage.class);
        
        addPropertyValue(builder, element, ConfigFile.Error.Attr.SHEET);
        addPropertyValue(builder, element, ConfigFile.Error.Attr.STATUS_CODE);
        addPropertyValue(builder, element, ConfigFile.Error.Attr.CONTENT_TYPE);
        addPropertyValue(builder, element, ConfigFile.Error.Attr.TEST);
        
        parseGuiServices(builder, element, packageName);
        
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
            error(element, name);
        }
    }

    private void error(Element element, String name) {
        throw new BeanInitializationException("Property "+name+" of "+element.getLocalName()+" is a required element");
    }
    private void addPropertyValue(BeanDefinitionBuilder builder, Element element, String name, boolean required) {
        addPropertyValue(builder, element, name, name, required);
    }

    private void parseGuiServices(BeanDefinitionBuilder builder, Element element, String packageName) {
        NodeList nodeList = element.getChildNodes();
        ManagedList<BeanDefinition> guiServices = new ManagedList<BeanDefinition>();
        for(int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            if(node instanceof Element) {
                Element elem = (Element) node;
                if (elem.getLocalName().equals(ConfigFile.Output.Child.CALL) ||
                        elem.getLocalName().equals(ConfigFile.Output.Child.XML))
                guiServices.add(parseGuiService(elem, packageName));
            }
        }

        builder.addPropertyValue("guiServices", guiServices);
    }
    private BeanDefinition parseGuiService(Element elem, String packageName) {
        if (ConfigFile.Output.Child.XML.equals(elem.getLocalName())) {
            BeanDefinitionBuilder builder = newBuilder(XmlFile.class);
            addPropertyValue(builder, elem, ConfigFile.Xml.Attr.NAME);
            addPropertyValue(builder, elem, ConfigFile.Xml.Attr.BASE);
            addPropertyValue(builder, elem, ConfigFile.Xml.Attr.FILE);
            addPropertyValue(builder, elem, ConfigFile.Xml.Attr.LANGUAGE);
            addPropertyValue(builder, elem, ConfigFile.Xml.Attr.LOCALIZED);

            return builder.getBeanDefinition();
        }

        if (ConfigFile.Output.Child.CALL.equals(elem.getLocalName())) {
            BeanDefinitionBuilder builder = newBuilder(Call.class);
            setServiceName(elem, packageName, builder, ConfigFile.Call.Attr.CLASS);

            addPropertyValue(builder, elem, ConfigFile.Call.Attr.NAME);
            parseParam(elem, builder);
            return builder.getBeanDefinition();
        }
        throw new IllegalArgumentException("Unknown GUI element : "+ elem.getLocalName());
    }
    private BeanDefinitionBuilder newBuilder(Class<?> beanClass) {
        BeanDefinitionBuilder builder = BeanDefinitionBuilder.rootBeanDefinition(beanClass);
        builder.setAutowireMode(RootBeanDefinition.AUTOWIRE_BY_TYPE);
        return builder;
    }

}
