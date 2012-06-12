package jeeves.config.springutil;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import jeeves.config.EnvironmentalConfig;
import jeeves.constants.ConfigFile;
import jeeves.constants.Jeeves;
import jeeves.server.dispatchers.ErrorPage;
import jeeves.server.dispatchers.OutputPage;
import jeeves.server.dispatchers.Param;
import jeeves.server.dispatchers.ServiceConfigBean;
import jeeves.server.dispatchers.ServiceInfo;
import jeeves.server.dispatchers.guiservices.Call;
import jeeves.server.dispatchers.guiservices.GuiService;
import jeeves.server.dispatchers.guiservices.XmlFile;

import org.jdom.Element;
import org.junit.Test;
import org.springframework.context.support.FileSystemXmlApplicationContext;
import org.springframework.test.util.ReflectionTestUtils;

public class ServiceBeanDefinitionParserTest {
    private final String appPath = ServiceBeanDefinitionParserTest.class.getResource(".").getFile();
    private final String configPath = appPath;
    private final String baseUrl = "/test";
    private final EnvironmentalConfig envConfig = new EnvironmentalConfig(baseUrl, appPath, configPath).freeze();

    @SuppressWarnings("unchecked")
    @Test
    public void test() throws Exception {
        String configFile = ServiceBeanDefinitionParserTest.class.getResource("full-service-config.xml").toExternalForm();
        FileSystemXmlApplicationContext springContext = new FileSystemXmlApplicationContext(new String[]{configFile},false);
        springContext.setValidating(false);
        springContext.addBeanFactoryPostProcessor(envConfig);
        springContext.refresh();

        Map<String, ServiceInfo> serviceInfos = springContext.getBeansOfType(ServiceInfo.class);
        
        assertEquals(1, serviceInfos.size());
        Entry<String, ServiceInfo> infoEntry = serviceInfos.entrySet().iterator().next();
        assertEquals("test.service", infoEntry.getKey());
        
        ServiceInfo info = infoEntry.getValue();
        List<ServiceConfigBean> beans = (List<ServiceConfigBean>) ReflectionTestUtils.getField(info, "servicesConfigBeans");
        TestService service = (TestService) ReflectionTestUtils.getField(beans.get(0), "serviceObj");
        assertEquals("test.service", info.getName());
        assertTrue(info.matches(new Element("request").addContent(new Element("service"))));
        assertFalse(info.isCacheSet());
        
        assertEquals(appPath, service.getAppPath());
        assertEquals("v1", service.getParams().getMandatoryValue("p1"));
        assertEquals("v2", service.getParams().getMandatoryValue("p2"));
        
        Element params = new Element("request").addContent("rp1").setText("rv1");
        info.execServices(params , null);
        assertEquals("transformed", service.getRequestParams().getName());
        assertEquals("rv1", service.getRequestParams().getChildText("rp1"));
        
        // Test error page configuration
        assertEquals(1,((Collection<ErrorPage>) ReflectionTestUtils.getField(info, "errors")).size());
        ErrorPage errorPage = info.findErrorPage("500");
        assertNotNull(errorPage);
        assertNull(info.findErrorPage("403"));
        assertEquals("text", errorPage.getContentType());
        assertEquals(500, errorPage.getStatusCode());
        assertEquals("error-transform.xsl", errorPage.getStyleSheet());
        assertEquals("/xxx", errorPage.getTestCondition());
        List<GuiService> errorGuiServices = (List<GuiService>) ReflectionTestUtils.getField(errorPage, "guiServices"); 
        assertEquals(2, errorGuiServices.size());
        
        Call errorCall = (Call) errorGuiServices.get(0);
        assertEquals("errorService", ReflectionTestUtils.getField(errorCall, "name"));
        assertEquals(TestService.class.getName(), ReflectionTestUtils.getField(errorCall, "serviceClass"));
        assertEquals(TestService.class, ReflectionTestUtils.getField(errorCall, "serviceObj").getClass());
        List<Param> errorParams =  (List<Param>) ReflectionTestUtils.getField(errorCall, "params");
        assertTrue(errorParams.contains(new Param(Jeeves.Text.GUI_SERVICE, "yes")));
        assertTrue(errorParams.contains(new Param("ep1", "ev1")));
        assertEquals(2, errorParams.size());
        
        XmlFile errorXml = (XmlFile) errorGuiServices.get(1);
        assertEquals("error.xml", ReflectionTestUtils.getField(errorXml, ConfigFile.Xml.Attr.FILE));
        assertEquals("sources", ReflectionTestUtils.getField(errorXml, ConfigFile.Xml.Attr.NAME));
        assertEquals("erloc", ReflectionTestUtils.getField(errorXml, ConfigFile.Xml.Attr.BASE));
        assertEquals("eng", ReflectionTestUtils.getField(errorXml, ConfigFile.Xml.Attr.LANGUAGE));
        assertEquals(false, ReflectionTestUtils.getField(errorXml, ConfigFile.Xml.Attr.LOCALIZED));
        
        // Test output page configuration
        assertEquals(1,((Collection<ErrorPage>) ReflectionTestUtils.getField(info, "outputs")).size());
        OutputPage outputPage = info.findOutputPage(new Element("a"));
        assertNotNull(outputPage);
        assertNull(info.findOutputPage(new Element("xx")));
        assertEquals("application/pdf", outputPage.getContentType());
        assertEquals("output-transform.xsl", outputPage.getStyleSheet());
        assertEquals("a", outputPage.getTestCondition());
        assertEquals("service3", outputPage.getForward());
        assertFalse(outputPage.isBLOB());
        assertTrue(outputPage.isFile());

        List<GuiService> outputGuiServices = (List<GuiService>) ReflectionTestUtils.getField(outputPage, "guiServices"); 
        assertEquals(2, outputGuiServices.size());
        Call outputCall = (Call) outputGuiServices.get(1);
        assertEquals("outputService", ReflectionTestUtils.getField(outputCall, "name"));
        assertEquals(TestService.class.getName(), ReflectionTestUtils.getField(outputCall, "serviceClass"));
        assertEquals(TestService.class, ReflectionTestUtils.getField(outputCall, "serviceObj").getClass());
        List<Param> outputParams =  (List<Param>) ReflectionTestUtils.getField(outputCall, "params");
        assertTrue(outputParams.contains(new Param(Jeeves.Text.GUI_SERVICE, "yes")));
        assertTrue(outputParams.contains(new Param("op1", "ov1")));
        assertEquals(2, outputParams.size());

        XmlFile outputXml = (XmlFile) outputGuiServices.get(0);
        assertEquals("output.xml", ReflectionTestUtils.getField(outputXml, ConfigFile.Xml.Attr.FILE));
        assertEquals("sources", ReflectionTestUtils.getField(outputXml, ConfigFile.Xml.Attr.NAME));
        assertEquals("loc", ReflectionTestUtils.getField(outputXml, ConfigFile.Xml.Attr.BASE));
        assertEquals("fre", ReflectionTestUtils.getField(outputXml, ConfigFile.Xml.Attr.LANGUAGE));
        assertEquals(true, ReflectionTestUtils.getField(outputXml, ConfigFile.Xml.Attr.LOCALIZED));

    }

}
