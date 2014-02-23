package org.fao.geonet.services.metadata.schema;

import com.google.common.collect.Lists;
import jeeves.constants.Jeeves;
import jeeves.interfaces.Service;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;
import org.fao.geonet.Util;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.constants.Params;
import org.fao.geonet.domain.GeonetEntity;
import org.fao.geonet.domain.SchematronCriteriaGroup;
import org.fao.geonet.domain.SchematronCriteriaGroupId;
import org.fao.geonet.domain.SchematronRequirement;
import org.fao.geonet.repository.SchematronCriteriaGroupRepository;
import org.fao.geonet.repository.Updater;
import org.fao.geonet.repository.specification.SchematronCriteriaGroupSpecs;
import org.jdom.Element;
import org.springframework.data.jpa.domain.Specifications;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * Load, edit, delete {@link org.fao.geonet.domain.SchematronCriteriaGroup} entities.
 *
 * Created by Jesse on 2/7/14.
 */
public class SchematronCriteriaGroupService extends AbstractSchematronService {

    static final String PARAM_SCHEMATRON_ID = "schematronId";
    static final String PARAM_GROUP_NAME = "groupName";
    static final String PARAM_INCLUDE_CRITERIA = "includeCriteria";
    static final String PARAM_INCLUDE_SCHEMATRON = "includeSchematron";
    static final String PARAM_REQUIREMENT = "requirement";

    @Override
    protected Element delete(Element params, ServiceContext context) throws Exception {
        String groupName = Util.getParam(params, PARAM_GROUP_NAME);
        String schematronId = Util.getParam(params, PARAM_SCHEMATRON_ID);

        final SchematronCriteriaGroupRepository repository = context.getBean(SchematronCriteriaGroupRepository.class);
        repository.delete(new SchematronCriteriaGroupId(groupName, Integer.parseInt(schematronId)));

        return new Element("ok");
    }

    @Override
    protected Element add(Element params, ServiceContext context) throws Exception {
        String groupName = Util.getParam(params, PARAM_GROUP_NAME);
        Integer schematronId = Integer.parseInt(Util.getParam(params, PARAM_SCHEMATRON_ID));
        String requirement = Util.getParam(params, PARAM_REQUIREMENT, SchematronRequirement.REQUIRED.name());


        final SchematronCriteriaGroup group = new SchematronCriteriaGroup()
                .setId(new SchematronCriteriaGroupId(groupName, schematronId))
                .setRequirement(SchematronRequirement.valueOf(requirement.toUpperCase()));
        context.getBean(SchematronCriteriaGroupRepository.class).saveAndFlush(group);

        return new Element(Jeeves.Elem.RESPONSE).addContent(new Element("status").setText("success"));
    }

    @Override
    protected Element list(Element params, ServiceContext context) throws Exception {
        String groupName = Util.getParam(params, PARAM_GROUP_NAME, null);
        String schematronId = Util.getParam(params, PARAM_SCHEMATRON_ID, null);

        boolean includeCriteriaParamPresent = params.getChild(PARAM_INCLUDE_CRITERIA) != null;
        boolean includeCriteria = Util.getParam(params, PARAM_INCLUDE_CRITERIA, includeCriteriaParamPresent);
        boolean includeSchematronParamPresent = params.getChild(PARAM_INCLUDE_SCHEMATRON) != null;
        boolean includeSchematron = Util.getParam(params, PARAM_INCLUDE_SCHEMATRON, includeSchematronParamPresent);

        final SchematronCriteriaGroupRepository repository = context.getBean(SchematronCriteriaGroupRepository.class);
        Specifications<SchematronCriteriaGroup> spec = null;

        if (schematronId != null) {
            spec = Specifications.where(SchematronCriteriaGroupSpecs.hasSchematronId(Integer.parseInt(schematronId)));
        }

        if (groupName != null) {
            if (spec == null) {
                spec = Specifications.where(SchematronCriteriaGroupSpecs.hasGroupName(groupName));
            } else {
                spec = spec.and(SchematronCriteriaGroupSpecs.hasGroupName(groupName));
            }
        }

        Element groups = repository.findAllAsXml(spec);

        @SuppressWarnings("unchecked")
        final List<Element> records = groups.getChildren(GeonetEntity.RECORD_EL_NAME);
        if (!includeCriteria || !includeSchematron) {
            for (Element child : records) {
                if (!includeCriteria) {
                    @SuppressWarnings("unchecked")
                    final List<Element> criteria = child.getChild("criteria").getChildren();
                    for (Element element : criteria) {
                        element.setContent(element.getChild("id"));
                    }
                }
                if (!includeSchematron) {
                    child.removeChild("schematron");
                }
            }
        }

        return groups;
    }

    @Override
    protected boolean exists(Element params, ServiceContext context) throws Exception {
        String groupName = Util.getParam(params, PARAM_GROUP_NAME);
        int schematronId = Integer.parseInt(Util.getParam(params, PARAM_SCHEMATRON_ID));

        return context.getBean(SchematronCriteriaGroupRepository.class).exists(new SchematronCriteriaGroupId(groupName, schematronId));
    }

    @Override
    protected Element edit(Element params, ServiceContext context) throws Exception {
        final SchematronCriteriaGroupRepository repository = context.getBean(SchematronCriteriaGroupRepository.class);

        String groupName = Util.getParam(params, PARAM_GROUP_NAME);
        int schematronId = Integer.parseInt(Util.getParam(params, PARAM_SCHEMATRON_ID));
        final String requirement = Util.getParam(params, PARAM_REQUIREMENT, null);

        if (requirement != null) {
            repository.update(new SchematronCriteriaGroupId(groupName, schematronId), new Updater<SchematronCriteriaGroup>() {
                @Override
                public void apply(@Nonnull SchematronCriteriaGroup entity) {
                    entity.setRequirement(SchematronRequirement.valueOf(requirement.toUpperCase()));
                }
            });

            // Test that the new requirement is valid
            SchematronRequirement.valueOf(requirement.toUpperCase());

            return new Element("ok");
        }

        return new Element("NoUpdate");

    }
}