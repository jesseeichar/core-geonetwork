package org.fao.geonet.kernel.schema;

import org.fao.geonet.domain.Pair;
import org.fao.geonet.utils.Xml;
import org.jdom.Content;
import org.jdom.Element;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.fao.geonet.constants.Geonet.Namespaces.GCO;
import static org.fao.geonet.constants.Geonet.Namespaces.GMD;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Test the inspire schematron.
 *
 * Created by Jesse on 1/31/14.
 */
public class StrictInspireTest extends AbstractInspireTest {
    protected File schematronXsl;
    protected Element inspire_schematron;

    @Before
    public void before() {
        super.before();
        String schematronFile = "iso19139/schematron/schematron-rules-inspire-strict.disabled.sch";
        Pair<Element,File> compiledResult = compileSchematron(new File(SCHEMA_PLUGINS, schematronFile));
        inspire_schematron = compiledResult.one();
        schematronXsl = compiledResult.two();
    }

    protected File getSchematronXsl() {
        return schematronXsl;
    }

    @Test
    public void testConformityEngLanguage_GerConformityString() throws Exception {
        final Element testMetadata = Xml.loadStream(AbstractInspireTest.class.getResourceAsStream(INSPIRE_VALID_ISO19139_XML));

        Map<String, String> languageMap = new HashMap<String, String>();
        languageMap.put("En", "eng");
        languageMap.put("Fr", "fre");
        languageMap.put("It", "ita");
        languageMap.put("Es", "spa");
        languageMap.put("Fi", "fin");
        languageMap.put("Nl", "dut");

        for (Map.Entry<String, String> lang : languageMap.entrySet()) {
            testMetadata.getChild("language", GMD).getChild("CharacterString", GCO).setText(lang.getValue());

            Element results = Xml.transform(testMetadata, getSchematronXsl().getPath(), PARAMS);
            assertEquals(1, countFailures(results));

            Element failure = (Element) results.getDescendants(FAILURE_FILTER).next();

            assertTrue(failure.getAttributeValue("test"), failure.getAttributeValue("test").contains("$has"+lang.getKey()+"Title"));
        }
    }


    @Test
    public void testMissingConformityDate() throws Exception {
        final Element testMetadata = Xml.loadStream(AbstractInspireTest.class.getResourceAsStream(INSPIRE_VALID_ISO19139_XML));

        final String xpath = "gmd:dataQualityInfo/*/gmd:report/*/gmd:result/*/gmd:specification/*/gmd:date";
        List<Content> pass = (List<Content>) Xml.selectNodes(testMetadata, xpath, NAMESPACES);

        for (Content content : pass) {
            content.detach();
        }

        Element results = Xml.transform(testMetadata, getSchematronXsl().getPath(), PARAMS);
        assertEquals(1, countFailures(results));
    }

    @Test
    public void testMissingConformityTitle() throws Exception {
        final Element testMetadata = Xml.loadStream(AbstractInspireTest.class.getResourceAsStream(INSPIRE_VALID_ISO19139_XML));

        final String xpath = "gmd:dataQualityInfo/*/gmd:report/*/gmd:result/*/gmd:specification/*/gmd:title";
        List<Content> pass = (List<Content>) Xml.selectNodes(testMetadata, xpath, NAMESPACES);

        for (Content content : pass) {
            content.detach();
        }

        Element results = Xml.transform(testMetadata, getSchematronXsl().getPath(), PARAMS);
        assertEquals(2, countFailures(results));
    }
}
