package org.fao.geonet.transformer;

import org.fao.geonet.AbstractCoreIntegrationTest;
import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.utils.IO;
import org.jdom.Element;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;

import java.nio.file.Path;

/**
 * @author Jesse on 4/10/2015.
 */
public class TransformTest extends AbstractCoreIntegrationTest {
    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Test
    public void testXmlGroovy() throws Exception {
        ApplicationContextHolder.set(applicationContext);
        Path path = IO.toPath(TransformTest.class.getResource("select_title.groovy").toURI());
        final Element xml = getSampleMetadataXml();
        Element result = Transform.get().xml(xml, path);
        System.out.println(result);
    }
    @Test
    public void testXmlXslt() throws Exception {
        ApplicationContextHolder.set(applicationContext);
        Path path = IO.toPath(TransformTest.class.getResource("select_title.xsl").toURI());
        final Element xml = getSampleMetadataXml();
        Element result = Transform.get().xml(xml, path);
        System.out.println(result);
    }
}