package org.fao.geonet.services.metadata.schema;

import jeeves.server.context.ServiceContext;
import net.arnx.jsonic.JSON;
import org.fao.geonet.domain.Schematron;
import org.fao.geonet.kernel.GeonetworkDataDirectory;
import org.fao.geonet.kernel.SchemaManager;
import org.fao.geonet.repository.SchematronRepository;
import org.fao.geonet.services.AbstractServiceIntegrationTest;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.junit.Test;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;

import static org.junit.Assert.*;

/**
 * Test for the json service.
 *
 * Created by Jesse on 2/14/14.
 */
public class SchematronCriteriaTypeServiceTest extends AbstractServiceIntegrationTest {
    public static final class ErrorContainer {
        ArrayList<String> errorList = new ArrayList<String>();

        public void add(String report) {
            errorList.add(report);
        }
    }

    public static final class SchematronResultContainer {
        public String name;
        public String id;
        public String schemaname;

        public void assertValidity() {
            assertNotNull(name);
            assertNotNull(id);
            assertNotNull(schemaname);
        }
    }

    public static final class CriteriaTypeResultContainer {
        public String name;
        public String type;
        public String value;
        public String allowArbitraryValue;

        public void assertValidity() {
            assertNotNull(name);
            assertNotNull(type);
            assertNotNull(value);
            assertNotNull(allowArbitraryValue);
        }
    }

    public static final class CriteriaTypeServiceResultContainer {
        public String url;
        public String cacheable;
        public String records;
        public String label;
        public String value;

        public void assertValidity() {
            assertNotNull(url);
            assertNotNull(cacheable);
            assertNotNull(records);
            assertNotNull(label);
            assertNotNull(value);
        }
    }

    @Test
    public void testExec() throws Exception {
        addSchematron("iso19139");
        addSchematron("iso19115");

        ServiceContext context = createServiceContext();
        loginAsAdmin(context);

        Element params = createParams();

        final SchematronCriteriaTypeService service = new SchematronCriteriaTypeService();
        Element result = service.exec(params, context);

        String json = Xml.getJSON(result);
        new JSON().parse(json);
        // no exception ??? good

        assertEqualsText("iso19139", result, "iso19139/name");
        String resultAsString = Xml.getString(result);
        assertEquals(resultAsString, 2, Xml.selectNumber(result, "count(iso19139/criteriaTypes/type)").intValue());
        assertEqualsText("XPATH", result, "iso19139/criteriaTypes/type[1]/type");
        assertEqualsText("Keyword", result, "iso19139/criteriaTypes/type[1]/name");
        assertEquals(resultAsString, 1, Xml.selectNumber(result, "count(iso19139/criteriaTypes/type[1]/service)").intValue());
        assertTrue(resultAsString, 0 < Xml.selectNumber(result, "count(iso19139/schematron)").intValue());
        assertEquals(resultAsString, 1, Xml.selectNumber(result, "count(iso19139/schematron[1]/file)").intValue());
        assertEquals(resultAsString, 1, Xml.selectNumber(result, "count(iso19139/schematron[1]/id)").intValue());
        assertEquals(resultAsString, 1, Xml.selectNumber(result, "count(iso19139/schematron[1]/rulename)").intValue());
        assertTrue(resultAsString, 0 < Xml.selectNumber(result, "count(iso19139/schematron[1]/label)").intValue());
        assertEquals(resultAsString, 1, Xml.selectNumber(result, "count(iso19139/schematron[1]/schemaname)").intValue());
        assertEquals(resultAsString, 1, Xml.selectNumber(result, "count(iso19139/schematron[1]/groupCount)").intValue());
    }

    private void addSchematron(String schemaName) {
        final SchemaManager schemaManager = _applicationContext.getBean(SchemaManager.class);
        final SchematronRepository repo = _applicationContext.getBean(SchematronRepository.class);
        Schematron schematron = new Schematron();
        schematron.setSchemaName(schemaName);
        schematron.setFile(schemaManager.getSchemaDir(schemaName) + File.separator + "schematron" + File.separator
                           + "schematron-rules-geonetwork.sch");
        repo.save(schematron);
    }
}
