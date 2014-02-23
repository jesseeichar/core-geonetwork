package org.fao.geonet.services.metadata.schema;

import static org.fao.geonet.services.metadata.schema.SchematronServiceAction.*;
import static org.junit.Assert.*;

import jeeves.server.context.ServiceContext;
import org.fao.geonet.constants.Params;
import org.fao.geonet.domain.GeonetEntity;
import org.fao.geonet.domain.SchematronRequirement;
import org.jdom.Element;
import org.junit.Test;

import static org.fao.geonet.domain.Pair.read;

/**
 * Test SchematronCriteriaGroupServiceIntegration.
 * Created by Jesse on 2/12/14.
 */
public class SchematronCriteriaGroupServiceIntegrationTest extends AbstractSchematronServiceIntegrationTest {

    @Test
    public void testExecListAll_ExcludeCriteriaAndSchematron() throws Exception {
        final SchematronCriteriaGroupService service = new SchematronCriteriaGroupService();

        ServiceContext context = createServiceContext();
        loginAsAdmin(context);

        Element params = createParams(
                read(Params.ACTION, LIST)
        );

        Element result = service.exec(params, context);
        assertEquals(4, result.getChildren().size());

        assertEquals(0, result.getChild(GeonetEntity.RECORD_EL_NAME).getChildren("schematron").size());
        assertCriteriaOnlyId(result);


        result = service.exec(createParams(), context);

        assertEquals(4, result.getChildren().size());

        assertEquals(0, result.getChild(GeonetEntity.RECORD_EL_NAME).getChildren("schematron").size());
        assertCriteriaOnlyId(result);
    }

    @Test
    public void testExecListAll_IncludeCriteria() throws Exception {
        final SchematronCriteriaGroupService service = new SchematronCriteriaGroupService();

        ServiceContext context = createServiceContext();
        loginAsAdmin(context);

        Element params = createParams(
                read(Params.ACTION, LIST),
                read(SchematronCriteriaGroupService.PARAM_INCLUDE_CRITERIA, Boolean.TRUE)
        );

        Element result = service.exec(params, context);
        assertEquals(4, result.getChildren().size());

        assertEquals(0, result.getChild(GeonetEntity.RECORD_EL_NAME).getChildren("schematron").size());
        assertFullCriteria(result);
    }

    @Test
    public void testExecListAll_IncludeSchematron() throws Exception {
        final SchematronCriteriaGroupService service = new SchematronCriteriaGroupService();

        ServiceContext context = createServiceContext();
        loginAsAdmin(context);

        Element params = createParams(
                read(Params.ACTION, LIST),
                read(SchematronCriteriaGroupService.PARAM_INCLUDE_SCHEMATRON, Boolean.TRUE)
        );

        Element result = service.exec(params, context);
        assertEquals(4, result.getChildren().size());

        assertEquals(1, result.getChild(GeonetEntity.RECORD_EL_NAME).getChildren("schematron").size());
        assertCriteriaOnlyId(result);
    }

    @Test
    public void testExecListAll_IncludeSchematronAndCriteria() throws Exception {
        final SchematronCriteriaGroupService service = new SchematronCriteriaGroupService();

        ServiceContext context = createServiceContext();
        loginAsAdmin(context);

        Element params = createParams(
                read(Params.ACTION, LIST),
                read(SchematronCriteriaGroupService.PARAM_INCLUDE_SCHEMATRON, Boolean.TRUE),
                read(SchematronCriteriaGroupService.PARAM_INCLUDE_CRITERIA, Boolean.TRUE)
        );

        Element result = service.exec(params, context);
        assertEquals(4, result.getChildren().size());

        assertEquals(1, result.getChild(GeonetEntity.RECORD_EL_NAME).getChildren("schematron").size());
        assertFullCriteria(result);
    }

    @Test
    public void testExecListOwnedBySchematron() throws Exception {
        final SchematronCriteriaGroupService service = new SchematronCriteriaGroupService();

        ServiceContext context = createServiceContext();
        loginAsAdmin(context);

        Element params = createParams(
                read(Params.ACTION, LIST),
                read(SchematronCriteriaGroupService.PARAM_SCHEMATRON_ID, _group1_Name1_SchematronId1.getId().getSchematronId()),
                read(SchematronCriteriaGroupService.PARAM_INCLUDE_SCHEMATRON, Boolean.TRUE)
        );

        Element result = service.exec(params, context);
        assertEquals(2, result.getChildren().size());

        assertEquals(1, result.getChild(GeonetEntity.RECORD_EL_NAME).getChildren("schematron").size());
        assertCriteriaOnlyId(result);

        assertGroupNames(result, _group1_Name1_SchematronId1, _group3_Name3_SchemtronId1);
        assertSchematronIds(result, _group1_Name1_SchematronId1, _group3_Name3_SchemtronId1);
        assertNotSchematronIds(result, _group2_Name2_SchematronId2, _group4_Name2_SchematronId4);
    }

    @Test
    public void testExecListHasGroupName() throws Exception {
        final SchematronCriteriaGroupService service = new SchematronCriteriaGroupService();

        ServiceContext context = createServiceContext();
        loginAsAdmin(context);

        Element params = createParams(
                read(Params.ACTION, LIST),
                read(SchematronCriteriaGroupService.PARAM_GROUP_NAME, _group2_Name2_SchematronId2.getId().getName()),
                read(SchematronCriteriaGroupService.PARAM_INCLUDE_SCHEMATRON, Boolean.TRUE)
        );

        Element result = service.exec(params, context);
        assertEquals(2, result.getChildren().size());

        assertEquals(1, result.getChild(GeonetEntity.RECORD_EL_NAME).getChildren("schematron").size());
        assertCriteriaOnlyId(result);

        assertGroupNames(result, _group2_Name2_SchematronId2, _group4_Name2_SchematronId4);
        assertNotGroupNames(result, _group1_Name1_SchematronId1, _group3_Name3_SchemtronId1);
        assertSchematronIds(result, _group2_Name2_SchematronId2, _group4_Name2_SchematronId4);
    }

    @Test
    public void testExecListOne() throws Exception {
        final SchematronCriteriaGroupService service = new SchematronCriteriaGroupService();

        ServiceContext context = createServiceContext();
        loginAsAdmin(context);

        Element params = createParams(
                read(Params.ACTION, LIST),
                read(SchematronCriteriaGroupService.PARAM_GROUP_NAME, _group2_Name2_SchematronId2.getId().getName()),
                read(SchematronCriteriaGroupService.PARAM_SCHEMATRON_ID, _group2_Name2_SchematronId2.getId().getSchematronId())
        );

        Element result = service.exec(params, context);
        assertEquals(1, result.getChildren().size());

        assertGroupNames(result, _group2_Name2_SchematronId2);
        assertSchematronIds(result, _group2_Name2_SchematronId2);
        assertNotGroupNames(result, _group1_Name1_SchematronId1, _group3_Name3_SchemtronId1);
        assertNotSchematronIds(result, _group1_Name1_SchematronId1, _group4_Name2_SchematronId4);
    }
    @Test
    public void testExecExists() throws Exception {
        final SchematronCriteriaGroupService service = new SchematronCriteriaGroupService();

        ServiceContext context = createServiceContext();
        loginAsAdmin(context);

        Element params = createParams(
                read(Params.ACTION, EXISTS),
                read(SchematronCriteriaGroupService.PARAM_GROUP_NAME, _group2_Name2_SchematronId2.getId().getName()),
                read(SchematronCriteriaGroupService.PARAM_SCHEMATRON_ID, _group2_Name2_SchematronId2.getId().getSchematronId())
        );

        Element result = service.exec(params, context);
        assertEquals(Boolean.TRUE.toString(), result.getText());

        params = createParams(
                read(Params.ACTION, EXISTS),
                read(SchematronCriteriaGroupService.PARAM_GROUP_NAME, Integer.MAX_VALUE),
                read(SchematronCriteriaGroupService.PARAM_SCHEMATRON_ID, _group2_Name2_SchematronId2.getId().getSchematronId())
        );

        result = service.exec(params, context);
        assertEquals(Boolean.FALSE.toString(), result.getText());

    }

    @Test
    public void testExecDelete() throws Exception {
        final SchematronCriteriaGroupService service = new SchematronCriteriaGroupService();

        ServiceContext context = createServiceContext();
        loginAsAdmin(context);

        Element deleteParams = createParams(
                read(Params.ACTION, DELETE),
                read(SchematronCriteriaGroupService.PARAM_GROUP_NAME, _group1_Name1_SchematronId1.getId().getName()),
                read(SchematronCriteriaGroupService.PARAM_SCHEMATRON_ID, _group1_Name1_SchematronId1.getId().getSchematronId())
        );

        Element result = service.exec(deleteParams, context);
        assertEquals("ok", result.getName());

        Element listParams = createParams(
                read(Params.ACTION, LIST)
        );

        result = service.exec(listParams, context);
        assertEquals(3, result.getChildren().size());

        assertGroupNames(result, _group2_Name2_SchematronId2, _group3_Name3_SchemtronId1, _group4_Name2_SchematronId4);
        assertNotGroupNames(result, _group1_Name1_SchematronId1);
        assertSchematronIds(result, _group2_Name2_SchematronId2, _group3_Name3_SchemtronId1, _group4_Name2_SchematronId4);
    }

    @Test
    public void testExecAdd() throws Exception {
        final SchematronCriteriaGroupService service = new SchematronCriteriaGroupService();

        ServiceContext context = createServiceContext();
        loginAsAdmin(context);

        final String newName = "NewName" + _inc.incrementAndGet();
        final String requirement = SchematronRequirement.REPORT_ONLY.name();
        final int schematronId = _group1_Name1_SchematronId1.getId().getSchematronId();
        Element deleteParams = createParams(
                read(Params.ACTION, ADD),
                read(SchematronCriteriaGroupService.PARAM_GROUP_NAME, newName),
                read(SchematronCriteriaGroupService.PARAM_SCHEMATRON_ID, schematronId),
                read(SchematronCriteriaGroupService.PARAM_REQUIREMENT, requirement)
                );

        Element result = service.exec(deleteParams, context);
        assertSuccessfulAdd(result);

        Element listParams = createParams(
                read(Params.ACTION, LIST)
        );

        result = service.exec(listParams, context);
        assertEquals(5, result.getChildren().size());

        listParams = createParams(
                read(Params.ACTION, LIST),
                read(SchematronCriteriaGroupService.PARAM_GROUP_NAME, newName),
                read(SchematronCriteriaGroupService.PARAM_SCHEMATRON_ID, schematronId)
        );

        result = service.exec(listParams, context);

        final Element idEl = result.getChild(GeonetEntity.RECORD_EL_NAME).getChild("id");
        assertEquals(newName, idEl.getChildText("name"));
        assertEquals("" + schematronId, idEl.getChildText("schematronid"));
        assertEquals(requirement, result.getChild(GeonetEntity.RECORD_EL_NAME).getChildText("requirement"));
    }

    @Test
    public void testExecEditRequirement() throws Exception {
        final SchematronCriteriaGroupService service = new SchematronCriteriaGroupService();

        ServiceContext context = createServiceContext();
        loginAsAdmin(context);

        SchematronRequirement newRequirement = SchematronRequirement.DISABLED;
        if (_group1_Name1_SchematronId1.getRequirement() == SchematronRequirement.DISABLED) {
            newRequirement = SchematronRequirement.REQUIRED;
        }
        Element deleteParams = createParams(
                read(Params.ACTION, EDIT),
                read(SchematronCriteriaGroupService.PARAM_GROUP_NAME, _group1_Name1_SchematronId1.getId().getName()),
                read(SchematronCriteriaGroupService.PARAM_SCHEMATRON_ID, _group1_Name1_SchematronId1.getId().getSchematronId()),
                read(SchematronCriteriaGroupService.PARAM_REQUIREMENT, newRequirement)
        );

        Element result = service.exec(deleteParams, context);
        assertEquals("ok", result.getName());

        Element listParams = createParams(
                read(Params.ACTION, LIST),
                read(SchematronCriteriaGroupService.PARAM_GROUP_NAME, _group1_Name1_SchematronId1.getId().getName()),
                read(SchematronCriteriaGroupService.PARAM_SCHEMATRON_ID, _group1_Name1_SchematronId1.getId().getSchematronId())
        );

        result = service.exec(listParams, context);
        assertEquals(1, result.getChildren().size());

        assertEquals(newRequirement.name(), result.getChild(GeonetEntity.RECORD_EL_NAME).getChildText("requirement"));

    }

    private void assertCriteriaOnlyId(Element result) {
        assertEquals(1, result.getChild(GeonetEntity.RECORD_EL_NAME).getChildren("criteria").size());
        assertTrue(0 < result.getChild(GeonetEntity.RECORD_EL_NAME).getChild("criteria").getChildren().size());
        assertEquals(1, result.getChild(GeonetEntity.RECORD_EL_NAME).getChild("criteria").getChild("criteria").getChildren().size());
        assertNotNull(result.getChild(GeonetEntity.RECORD_EL_NAME).getChild("criteria").getChild("criteria").getChild("id"));
    }


    private void assertFullCriteria(Element result) {
        assertEquals(1, result.getChild(GeonetEntity.RECORD_EL_NAME).getChildren("criteria").size());
        assertTrue(0 < result.getChild(GeonetEntity.RECORD_EL_NAME).getChild("criteria").getChildren().size());
        assertNotNull(result.getChild(GeonetEntity.RECORD_EL_NAME).getChild("criteria").getChild("criteria").getChild("id"));
        assertNotNull(result.getChild(GeonetEntity.RECORD_EL_NAME).getChild("criteria").getChild("criteria").getChild("type"));
        assertNotNull(result.getChild(GeonetEntity.RECORD_EL_NAME).getChild("criteria").getChild("criteria").getChild("value"));
    }
}
