package jeeves.server;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

import javax.servlet.ServletContext;

import jeeves.config.EnvironmentalConfig;
import jeeves.server.sources.ServiceRequest;
import jeeves.server.sources.ServiceRequest.InputMethod;
import jeeves.server.sources.ServiceRequest.OutputMethod;
import jeeves.utils.Xml;

import org.junit.Before;
import org.junit.Test;
import org.springframework.context.support.FileSystemXmlApplicationContext;

public class JeevesEngineTest {

    private final String appPath = JeevesEngineTest.class.getResource(".").getFile();
    private final String baseUrl = "/test";
    private EnvironmentalConfig envConfig;
    
    @Before
    public void createConfig() {
        envConfig = new EnvironmentalConfig(baseUrl, appPath);
    }

    private FileSystemXmlApplicationContext createContext(String... extraContextFileUrls) {
        return createContext(envConfig, extraContextFileUrls);
    }
    private FileSystemXmlApplicationContext createContext(EnvironmentalConfig envConfig, String... extraContextFileUrls) {
        ArrayList<String> contexts = new ArrayList<String>();
        String configFile = JeevesEngineTest.class.getResource("config-jeeves.xml").toExternalForm();
        contexts.add(configFile);
        if (extraContextFileUrls != null) {
            for (String string : extraContextFileUrls) {
                contexts.add(string);
            }
        }

        FileSystemXmlApplicationContext springContext = new FileSystemXmlApplicationContext(contexts.toArray(new String[contexts.size()]),false);
        springContext.setValidating(false);
        springContext.addBeanFactoryPostProcessor(envConfig);
        springContext.refresh();
        return springContext;
    }

    @Test
    public void testInit() throws Exception {
        FileSystemXmlApplicationContext springContext = createContext();

        JeevesEngine engine = springContext.getBean(JeevesEngine.class);
        assertNotNull(engine.getResourceManager());
        assertNotNull(engine.getResourceManager().getProviderManager());
        assertNotNull(engine.getResourceManager().getProviderManager().getProviders().iterator().hasNext());
    }

    @Test
    public void testDestroy() throws Exception {
        String destroyContextFile = JeevesEngineTest.class.getResource("config-destroy-provider.xml").toExternalForm();
        FileSystemXmlApplicationContext springContext = createContext(destroyContextFile);
        TestResourceProvider testDb = springContext.getBean(TestResourceProvider.class);
        JeevesEngine engine = springContext.getBean(JeevesEngine.class);
        
        reset(testDb.spy);
        Object result = new Object();
        when(testDb.spy.open()).thenReturn(result);
        Object openedResource = engine.getResourceManager().open("test-db");
        assertSame(result, openedResource);
        springContext.close();

        verify(testDb.spy).open();
        verify(testDb.spy).end();
    }

    @Test
    public void testServletContext() throws Exception {
        ServletContext mockContext = mock(ServletContext.class);
        
        EnvironmentalConfig envConfigWithContext = new EnvironmentalConfig(baseUrl, appPath);
        envConfigWithContext.setServletContext(mockContext);
        FileSystemXmlApplicationContext springContext = createContext(envConfigWithContext);
        
        JeevesEngine engine = springContext.getBean(JeevesEngine.class);
        verify(mockContext, atLeastOnce()).getAttribute(anyString());
    }
    
    @Test
    public void testDispatch() throws Exception {
        FileSystemXmlApplicationContext springContext = createContext();
        springContext.start();
        JeevesEngine engine = springContext.getBean(JeevesEngine.class);
        
        ServiceRequest srvReq = new ServiceRequest();
        srvReq.setAddress("localhost:8080");
        srvReq.setLanguage("eng");
        srvReq.setInputMethod(InputMethod.GET);
        srvReq.setOutputMethod(OutputMethod.XML);
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        srvReq.setOutputStream(os);
        srvReq.setService("test.service");
        UserSession session = new UserSession();

        engine.dispatch(srvReq, session);
        
        String data = new String(os.toByteArray());
        assertEquals("OK", Xml.loadString(data, false).getText());
    }

}
