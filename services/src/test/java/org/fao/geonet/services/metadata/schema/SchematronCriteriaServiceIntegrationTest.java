package org.fao.geonet.services.metadata.schema;


import jeeves.server.context.ServiceContext;
import org.fao.geonet.constants.Params;
import org.fao.geonet.domain.*;
import org.fao.geonet.repository.SchematronCriteriaGroupRepository;
import org.fao.geonet.repository.SchematronCriteriaRepository;
import org.jdom.Element;
import org.junit.Test;

import static org.fao.geonet.domain.Pair.read;
import static org.fao.geonet.services.metadata.schema.SchematronServiceAction.*;
import static org.junit.Assert.*;

/**
 * Integration test for the service.
 * <p/>
 * Created by Jesse on 2/13/14.
 */
public class SchematronCriteriaServiceIntegrationTest extends AbstractSchematronServiceIntegrationTest {
    final SchematronCriteriaService service = new SchematronCriteriaService();

    @Test
    public void testExecListAll() throws Exception {
        ServiceContext context = createServiceContext();
        loginAsAdmin(context);

        Element params = createParams(
                read(Params.ACTION, LIST)
        );

        Element result = service.exec(params, context);

        int totalCriteria = _group1_Name1_SchematronId1.getCriteria().size() +
                            _group2_Name2_SchematronId2.getCriteria().size() +
                            _group3_Name3_SchemtronId1.getCriteria().size() +
                            _group4_Name2_SchematronId4.getCriteria().size();

        assertEquals(totalCriteria, result.getChildren().size());
        assertGroupOnlyId(result);


        result = service.exec(createParams(), context);

        assertEquals(totalCriteria, result.getChildren().size());
        assertEquals(1, result.getChild(GeonetEntity.RECORD_EL_NAME).getChildren("group").size());
        assertEquals(totalCriteria, result.getChildren().size());
        assertGroupOnlyId(result);
    }

    @Test
    public void testExecListAllIncludeGroup() throws Exception {
        ServiceContext context = createServiceContext();
        loginAsAdmin(context);

        Element params = createParams(
                read(Params.ACTION, LIST),
                read(SchematronCriteriaService.PARAM_INCLUDE_GROUP, true)
        );

        Element result = service.exec(params, context);

        int totalCriteria = _group1_Name1_SchematronId1.getCriteria().size() +
                            _group2_Name2_SchematronId2.getCriteria().size() +
                            _group3_Name3_SchemtronId1.getCriteria().size() +
                            _group4_Name2_SchematronId4.getCriteria().size();

        assertEquals(totalCriteria, result.getChildren().size());
        assertFullGroup(result);
    }

    @Test
    public void testExecListAllHavingGroupName() throws Exception {
        ServiceContext context = createServiceContext();
        loginAsAdmin(context);

        Element params = createParams(
                read(Params.ACTION, LIST),
                read(SchematronCriteriaService.PARAM_GROUP_NAME, _group2_Name2_SchematronId2.getId().getName())
        );

        Element result = service.exec(params, context);

        int totalCriteria = _group2_Name2_SchematronId2.getCriteria().size() +
                            _group4_Name2_SchematronId4.getCriteria().size();

        assertEquals(totalCriteria, result.getChildren().size());
        assertGroupOnlyId(result);
    }

    @Test
    public void testExecListAllHavingSchematronId() throws Exception {
        ServiceContext context = createServiceContext();
        loginAsAdmin(context);

        Element params = createParams(
                read(Params.ACTION, LIST),
                read(SchematronCriteriaService.PARAM_SCHEMATRON_ID, "" + _group1_Name1_SchematronId1.getId().getSchematronId())
        );

        Element result = service.exec(params, context);

        int totalCriteria = _group1_Name1_SchematronId1.getCriteria().size() +
                            _group3_Name3_SchemtronId1.getCriteria().size();

        assertEquals(totalCriteria, result.getChildren().size());
        assertGroupOnlyId(result);
    }

    @Test
    public void testExecListOne() throws Exception {
        ServiceContext context = createServiceContext();
        loginAsAdmin(context);

        Element params = createParams(
                read(Params.ACTION, LIST),
                read(Params.ID, _group1_Name1_SchematronId1.getCriteria().get(0).getId())
        );

        Element result = service.exec(params, context);

        assertEquals(1, result.getChildren().size());
        assertGroupOnlyId(result);

        params = createParams(
                read(Params.ACTION, LIST),
                read(SchematronCriteriaService.PARAM_INCLUDE_GROUP, "true"),
                read(Params.ID, _group1_Name1_SchematronId1.getCriteria().get(0).getId())
        );

        result = service.exec(params, context);

        assertEquals(1, result.getChildren().size());
        assertFullGroup(result);
    }

    @Test
    public void testExecExists() throws Exception {
        ServiceContext context = createServiceContext();
        loginAsAdmin(context);

        Element params = createParams(
                read(Params.ACTION, EXISTS),
                read(Params.ID, _group1_Name1_SchematronId1.getCriteria().get(0).getId())
        );

        Element result = service.exec(params, context);
        assertEquals(Boolean.TRUE.toString(), result.getText());

        params = createParams(
                read(Params.ACTION, EXISTS),
                read(Params.ID, Integer.MAX_VALUE)
        );

        result = service.exec(params, context);
        assertEquals(Boolean.FALSE.toString(), result.getText());
    }

    @Test
    public void testAddToExistingGroup() throws Exception {
        ServiceContext context = createServiceContext();
        loginAsAdmin(context);

        final String value = "x/y/z";
        final SchematronCriteriaType criteriaType = SchematronCriteriaType.XPATH;
        Element params = createParams(
                read(Params.ACTION, ADD),
                read(SchematronCriteriaService.PARAM_GROUP_NAME, _group1_Name1_SchematronId1.getId().getName()),
                read(SchematronCriteriaService.PARAM_SCHEMATRON_ID, _group1_Name1_SchematronId1.getId().getSchematronId()),
                read(SchematronCriteriaService.PARAM_TYPE, criteriaType),
                read(SchematronCriteriaService.PARAM_VALUE, value)
        );

        Element result = service.exec(params, context);
        assertSuccessfulAdd(result);
        String id = result.getChildText("id");

        params = createParams(
                read(Params.ACTION, EXISTS),
                read(Params.ID, id)
        );

        result = service.exec(params, context);
        assertEquals(Boolean.TRUE.toString(), result.getText());

        final SchematronCriteria saved = context.getBean(SchematronCriteriaRepository.class).findOne(Integer.parseInt(id));

        assertEquals(value, saved.getValue());
        assertEquals(criteriaType, saved.getType());
        assertEquals(_group1_Name1_SchematronId1.getId(), saved.getGroup().getId());
    }

    @Test
    public void testAddToNewGroup() throws Exception {
        ServiceContext context = createServiceContext();
        loginAsAdmin(context);

        final String value = "x/y/z";
        final SchematronCriteriaType criteriaType = SchematronCriteriaType.XPATH;
        String newName = "newGroupName" + _inc.incrementAndGet();
        final int schematronId = _group1_Name1_SchematronId1.getId().getSchematronId();

        Element params = createParams(
                read(Params.ACTION, ADD),
                read(SchematronCriteriaService.PARAM_GROUP_NAME, newName),
                read(SchematronCriteriaService.PARAM_SCHEMATRON_ID, schematronId),
                read(SchematronCriteriaService.PARAM_TYPE, criteriaType),
                read(SchematronCriteriaService.PARAM_VALUE, value)
        );

        assertFalse(context.getBean(SchematronCriteriaGroupRepository.class).exists(new SchematronCriteriaGroupId(newName,
                schematronId)));

        Element result = service.exec(params, context);
        assertSuccessfulAdd(result);
        String id = result.getChildText("id");

        final SchematronCriteriaGroup found = context.getBean(SchematronCriteriaGroupRepository.class).findOne(new
                SchematronCriteriaGroupId(newName, schematronId));
        assertTrue(found != null);
        assertEquals(SchematronRequirement.REQUIRED, found.getRequirement());

        params = createParams(
                read(Params.ACTION, EXISTS),
                read(Params.ID, id)
        );

        result = service.exec(params, context);
        assertEquals(Boolean.TRUE.toString(), result.getText());

        final SchematronCriteria saved = context.getBean(SchematronCriteriaRepository.class).findOne(Integer.parseInt(id));

        assertEquals(value, saved.getValue());
        assertEquals(criteriaType, saved.getType());
        assertEquals(newName, saved.getGroup().getId().getName());
        assertEquals(schematronId, saved.getGroup().getId().getSchematronId());
    }

    @Test
    public void testEdit() throws Exception {
        ServiceContext context = createServiceContext();
        loginAsAdmin(context);

        final String value = "x/y/z";
        final SchematronCriteriaType criteriaType = SchematronCriteriaType.XPATH;
        final int criteriaId = _group1_Name1_SchematronId1.getCriteria().get(0).getId();

        Element params = createParams(
                read(Params.ACTION, EDIT),
                read(Params.ID, criteriaId),
                read(SchematronCriteriaService.PARAM_TYPE, criteriaType),
                read(SchematronCriteriaService.PARAM_VALUE, value)
        );

        assertEquals("ok", service.exec(params, context).getName());

        final SchematronCriteria saved = context.getBean(SchematronCriteriaRepository.class).findOne(criteriaId);

        assertEquals(value, saved.getValue());
        assertEquals(criteriaType, saved.getType());
        assertEquals(_group1_Name1_SchematronId1.getId(), saved.getGroup().getId());
    }

    @Test
    public void testEditSetValueOnly() throws Exception {
        ServiceContext context = createServiceContext();
        loginAsAdmin(context);

        final SchematronCriteria criteria = _group1_Name1_SchematronId1.getCriteria().get(0);
        final int criteriaId = criteria.getId();
        final String newValue = "newValue"+_inc.incrementAndGet();
        Element params = createParams(
                read(Params.ACTION, EDIT),
                read(Params.ID, criteriaId),
                read(SchematronCriteriaService.PARAM_VALUE, newValue)
        );

        assertEquals("ok", service.exec(params, context).getName());

        final SchematronCriteria saved = context.getBean(SchematronCriteriaRepository.class).findOne(criteriaId);

        assertEquals(newValue, saved.getValue());
        assertEquals(criteria.getType(), saved.getType());
        assertEquals(_group1_Name1_SchematronId1.getId(), saved.getGroup().getId());
    }

    @Test
    public void testEditSetTypeOnly() throws Exception {
        ServiceContext context = createServiceContext();
        loginAsAdmin(context);

        final SchematronCriteria criteria = _group1_Name1_SchematronId1.getCriteria().get(0);
        final int criteriaId = criteria.getId();
        SchematronCriteriaType newType = SchematronCriteriaType.GROUP;
        if (criteria.getType() == SchematronCriteriaType.GROUP) {
            newType = SchematronCriteriaType.ALWAYS_ACCEPT;
        }
        Element params = createParams(
                read(Params.ACTION, EDIT),
                read(Params.ID, criteriaId),
                read(SchematronCriteriaService.PARAM_TYPE, newType.name())
        );

        assertEquals("ok", service.exec(params, context).getName());

        final SchematronCriteria saved = context.getBean(SchematronCriteriaRepository.class).findOne(criteriaId);

        assertEquals(criteria.getValue(), saved.getValue());
        assertEquals(newType, saved.getType());

        assertEquals(_group1_Name1_SchematronId1.getId(), saved.getGroup().getId());
    }

    @Test
    public void testExecDelete() throws Exception {
        ServiceContext context = createServiceContext();
        loginAsAdmin(context);

        final int id = _group1_Name1_SchematronId1.getCriteria().get(0).getId();

        Element deleteParams = createParams(
                read(Params.ACTION, DELETE),
                read(Params.ID, id)
        );

        assertEquals("ok", service.exec(deleteParams, context).getName());
        Element listParams = createParams(
                read(Params.ACTION, LIST)
        );

        Element result = service.exec(listParams, context);

        int totalCriteria = _group1_Name1_SchematronId1.getCriteria().size() +
                            _group2_Name2_SchematronId2.getCriteria().size() +
                            _group3_Name3_SchemtronId1.getCriteria().size() +
                            _group4_Name2_SchematronId4.getCriteria().size();

        assertEquals(totalCriteria, result.getChildren().size());
        assertGroupOnlyId(result);
    }

    private void assertFullGroup(Element result) {
        assertEquals(1, result.getChild(GeonetEntity.RECORD_EL_NAME).getChildren("group").size());
        assertEquals(2, result.getChild(GeonetEntity.RECORD_EL_NAME).getChild("group").getChildren().size());
        assertEquals(2, result.getChild(GeonetEntity.RECORD_EL_NAME).getChild("group").getChild("id").getChildren().size());
    }

    private void assertGroupOnlyId(Element result) {
        assertEquals(1, result.getChild(GeonetEntity.RECORD_EL_NAME).getChildren("group").size());
        assertEquals(1, result.getChild(GeonetEntity.RECORD_EL_NAME).getChild("group").getChildren().size());
        assertEquals(2, result.getChild(GeonetEntity.RECORD_EL_NAME).getChild("group").getChild("id").getChildren().size());
    }
}
