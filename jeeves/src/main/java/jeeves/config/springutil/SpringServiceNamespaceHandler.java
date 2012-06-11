package jeeves.config.springutil;

import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

public class SpringServiceNamespaceHandler extends NamespaceHandlerSupport {

    @Override
    public void init() {
        registerBeanDefinitionParser("service", new ServiceBeanDefinitionParser());
    }

}
