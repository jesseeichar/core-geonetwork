package org.fao.geonet.services.metadata.schema;

import com.google.common.collect.Lists;
import jeeves.interfaces.Service;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;
import org.fao.geonet.constants.Params;
import org.fao.geonet.domain.SchematronCriteriaGroup;
import org.fao.geonet.domain.SchematronCriteriaType;
import org.fao.geonet.kernel.SchemaManager;
import org.fao.geonet.repository.SchematronCriteriaGroupRepository;
import org.fao.geonet.repository.specification.SchematronCriteriaGroupSpecs;
import org.fao.geonet.utils.Xml;
import org.jdom.Content;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.springframework.data.jpa.domain.Specification;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * A service for the metadata validation administration UI.
 *
 * Created by Jesse on 2/9/14.
 */
public class SchematronCriteriaTypeService implements Service {

    private static final String EL_TYPE = "type";
    private static final String EL_VALUE = "value";
    private static final String EL_NAME = "name";
    private SchematronService schematronService = new SchematronService();

    @Override
    public void init(String appPath, ServiceConfig params) throws Exception {
    }

    @Override
    public Element exec(Element params, ServiceContext context) throws Exception {
        SchemaManager schemaManager = context.getApplicationContext().getBean(SchemaManager.class);
        SchematronCriteriaGroupRepository criteriaGroupRepo = context.getApplicationContext().getBean(SchematronCriteriaGroupRepository.class);

        Element schematrons = schematronService.exec(params, context);
        Element schemas = new Element("schemas");
        @SuppressWarnings("unchecked")
        List<Element> schematronElements = Lists.newArrayList(schematrons.getChildren());

        for (Element element : schematronElements) {
            String schemaname = element.getChildText("schemaname");
            Element schemaEl = schemas.getChild(schemaname);
            if (schemaEl == null) {
                schemaEl = new Element(schemaname);
                schemaEl.addContent(new Element("name").setText(schemaname));
                addCriteriaTypeDefinition(schemaManager, schemaEl, schemaname);
                schemas.addContent(schemaEl);
            }

            element.setName("schematron");
            schemaEl.addContent(element.detach());

            addSchematronGroupCount(criteriaGroupRepo, element, element.getChildText("id"));
        }

        return schemas;
    }

    private void addSchematronGroupCount(SchematronCriteriaGroupRepository criteriaGroupRepo, Element schemaEl, String id) {
        final Specification<SchematronCriteriaGroup> spec = SchematronCriteriaGroupSpecs.hasSchematronId(Integer.parseInt(id));
        String count = String.valueOf(criteriaGroupRepo.count(spec));
        schemaEl.addContent(new Element("groupCount").setText(count));
    }

    private void addCriteriaTypeDefinition(SchemaManager schemaManager, Element schemaEl, String schemaName) throws IOException, JDOMException {
        File file = new File(schemaManager.getSchemaDir(schemaName), "schematron" + File.separator + "criteria-type.xml");
        if (file.exists()) {
            Element criteriaType = Xml.loadFile(file);
            criteriaType.setName("criteriaTypes");
            criteriaType.addContent(alwaysAcceptCriteriaType());
            criteriaType.addContent(genericXPathCriteriaType());
            schemaEl.addContent(criteriaType);
        }
    }

    private Element genericXPathCriteriaType() {
       return new Element(EL_TYPE).addContent(Arrays.asList(
               new Element(EL_VALUE).setText("@@value@@"),
               new Element(EL_TYPE).setText(SchematronCriteriaType.XPATH.name()),
               new Element(EL_NAME).setText(SchematronCriteriaType.XPATH.name()))
       );
    }

    private Element alwaysAcceptCriteriaType() {
        return new Element(EL_TYPE).addContent(Arrays.asList(
                new Element(EL_TYPE).setText(SchematronCriteriaType.ALWAYS_ACCEPT.name()),
                new Element(EL_NAME).setText(SchematronCriteriaType.ALWAYS_ACCEPT.name()))
        );
    }
}
