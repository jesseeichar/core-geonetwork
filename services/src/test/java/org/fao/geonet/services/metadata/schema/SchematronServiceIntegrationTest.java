package org.fao.geonet.services.metadata.schema;

import jeeves.server.context.ServiceContext;
import org.fao.geonet.constants.Params;
import org.fao.geonet.domain.*;
import org.fao.geonet.repository.SchematronCriteriaGroupRepository;
import org.fao.geonet.repository.SchematronCriteriaRepository;
import org.fao.geonet.repository.SchematronRepository;
import org.jdom.Element;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.fao.geonet.domain.Pair.read;
import static org.fao.geonet.services.metadata.schema.SchematronServiceAction.*;
import static org.fao.geonet.services.metadata.schema.SchematronServiceAction.LIST;
import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

/**
 * Created by Jesse on 2/13/14.
 */
public class SchematronServiceIntegrationTest extends AbstractSchematronServiceIntegrationTest {
    @Test
    public void testExecListAll() throws Exception {
        ServiceContext context = createServiceContext();
        loginAsAdmin(context);

        Element result = create(LIST).exec(createParams(), context);

        assertEquals(4, result.getChildren().size());
        for (Object elem : result.getChildren()) {
            assertSchematronInfo((Element) elem);
        }

        result = create(LIST).exec(createParams(), context);

        assertEquals(4, result.getChildren().size());
        for (Object elem : result.getChildren()) {
            assertSchematronInfo((Element) elem);
        }
    }

    @Test
    public void testExecListOne() throws Exception {
        ServiceContext context = createServiceContext();
        loginAsAdmin(context);

        Element params = createParams(
                read(Params.ID, _group1_Name1_SchematronId1.getSchematron().getId())
        );

        Element result = create(LIST).exec(params, context);

        assertEquals(1, result.getChildren().size());
        assertSchematronInfo(result.getChild(GeonetEntity.RECORD_EL_NAME));
    }
    @Test
    public void testExecExists() throws Exception {
        ServiceContext context = createServiceContext();
        loginAsAdmin(context);

        Element params = createParams(
                read(Params.ID, _group1_Name1_SchematronId1.getSchematron().getId())
        );

        Element result = create(EXISTS).exec(params, context);
        assertEquals(Boolean.TRUE.toString(), result.getText());

        params = createParams(
                read(Params.ID, Integer.MAX_VALUE)
        );

        result = create(EXISTS).exec(params, context);
        assertEquals(Boolean.FALSE.toString(), result.getText());
    }
    @Test(expected = UnsupportedOperationException.class)
    public void testAdd() throws Exception {

        // will be implemented in the future.  For now throw exception when method is called.
        ServiceContext context = createServiceContext();
        loginAsAdmin(context);

        create(ADD).exec(createParams(), context);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testEdit() throws Exception {
        ServiceContext context = createServiceContext();
        loginAsAdmin(context);

        // might be be implemented in the future.  For now throw exception when method is called.
        create(EDIT).exec(createParams(), context);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testDelete() throws Exception {
        ServiceContext context = createServiceContext();
        loginAsAdmin(context);

        // might be be implemented in the future.  For now throw exception when method is called.
        create(DELETE).exec(createParams(), context);
    }

    private void assertSchematronInfo(Element child) {
        String id = child.getChildText("id");
        assertNotNull(id);
        String file = child.getChildText("file");
        assertNotNull(file);
        String ruleName = child.getChildText("rulename");
        assertNotNull(ruleName);
        String schemaname = child.getChildText("schemaname");
        assertNotNull(schemaname);

        List<Schematron> schematrons = _applicationContext.getBean(SchematronRepository.class).findAll();

        boolean found = false;
        for (Schematron schematron : schematrons) {
            if (found) {
                break;
            }

            if (schematron.getId() == Integer.parseInt(id)) {
                found = true;
                assertEquals(schematron.getFile(), file);
                assertEquals(schematron.getRuleName(), ruleName);
                assertEquals(schematron.getSchemaName(), schemaname);
            }
        }

        assertTrue(found);
    }

    private SchematronService create(SchematronServiceAction action) throws Exception {
        final SchematronService schematronService = new SchematronService();
        super.init(schematronService, action);
        return schematronService;
    }
}
