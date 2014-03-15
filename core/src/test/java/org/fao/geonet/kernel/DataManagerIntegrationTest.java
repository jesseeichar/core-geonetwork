package org.fao.geonet.kernel;

import static org.junit.Assert.*;
import static org.springframework.data.jpa.domain.Specifications.where;

import com.google.common.base.Optional;
import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;
import org.fao.geonet.AbstractCoreIntegrationTest;
import org.fao.geonet.domain.*;
import org.fao.geonet.kernel.search.IndexAndTaxonomy;
import org.fao.geonet.kernel.search.SearchManager;
import org.fao.geonet.repository.*;
import org.fao.geonet.repository.specification.MetadataSpecs;
import org.jdom.Element;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;

import javax.annotation.Nonnull;
import java.util.UUID;

/**
 * Tests for the DataManager.
 * <p/>
 * User: Jesse
 * Date: 10/24/13
 * Time: 5:30 PM
 */
public class DataManagerIntegrationTest extends AbstractCoreIntegrationTest {
    @Autowired
    DataManager _dataManager;
    @Autowired
    MetadataRepository _metadataRepository;

    @Test
    public void testDeleteMetadata() throws Exception {
        int count = (int) _metadataRepository.count();
        final ServiceContext serviceContext = createServiceContext();
        loginAsAdmin(serviceContext);
        final UserSession userSession = serviceContext.getUserSession();
        final String mdId = _dataManager.insertMetadata(serviceContext, "iso19193", new Element("MD_Metadata"), "uuid",
                userSession.getUserIdAsInt(),
                "" + ReservedGroup.all.getId(), "sourceid", "n", "doctype", null, new ISODate().getDateAndTime(), new ISODate().getDateAndTime(),
                false, false);

        assertEquals(count + 1, _metadataRepository.count());

        _dataManager.deleteMetadata(serviceContext, mdId);

        assertEquals(count, _metadataRepository.count());
    }

    @Test
    public void testCreateMetadataWithTemplateMetadata() throws Exception {
        final ServiceContext serviceContext = createServiceContext();
        loginAsAdmin(serviceContext);

        final User principal = serviceContext.getUserSession().getPrincipal();

        final GroupRepository bean = serviceContext.getBean(GroupRepository.class);
        Group group = bean.findAll().get(0);

        MetadataCategory category = serviceContext.getBean(MetadataCategoryRepository.class).findAll().get(0);

        final SourceRepository sourceRepository = serviceContext.getBean(SourceRepository.class);
        Source source = sourceRepository.save(new Source().setLocal(true).setName("GN").setUuid("sourceuuid"));

        final Element sampleMetadataXml = super.getSampleMetadataXml();
        final Metadata metadata = new Metadata();
        metadata.setDataAndFixCR(sampleMetadataXml)
            .setUuid(UUID.randomUUID().toString());
        metadata.getCategories().add(category);
        metadata.getDataInfo().setSchemaId("iso19139");
        metadata.getSourceInfo().setSourceId(source.getUuid());

        final Metadata templateMd = _metadataRepository.save(metadata);
        final String newMetadataId = _dataManager.createMetadata(serviceContext, "" + metadata.getId(), "" + group.getId(), source.getUuid(),
                principal.getId(), templateMd.getUuid(), MetadataType.METADATA.codeString, true);

        Metadata newMetadata = _metadataRepository.findOne(newMetadataId);
        assertEquals(1, newMetadata.getCategories().size());
        assertEquals(category, newMetadata.getCategories().iterator().next());
        assertEqualsText(metadata.getUuid(), newMetadata.getXmlData(false), "gmd:parentIdentifier/gco:CharacterString");

    }

    @Test
    public void testSetStatus() throws Exception {
        final ServiceContext serviceContext = createServiceContext();
        loginAsAdmin(serviceContext);

        final int metadataId = importMetadata(this, serviceContext);

        final MetadataStatus status = _dataManager.getStatus(metadataId);

        assertEquals(null, status);

        final ISODate changeDate = new ISODate();
        final String changeMessage = "Set to draft";
        _dataManager.setStatus(serviceContext, metadataId, 0, changeDate, changeMessage);

        final MetadataStatus loadedStatus = _dataManager.getStatus(metadataId);

        assertEquals(changeDate, loadedStatus.getId().getChangeDate());
        assertEquals(changeMessage, loadedStatus.getChangeMessage());
        assertEquals(0, loadedStatus.getStatusValue().getId());
        assertEquals(metadataId, loadedStatus.getId().getMetadataId());
        assertEquals(0, loadedStatus.getId().getStatusId());
        assertEquals(serviceContext.getUserSession().getUserIdAsInt(), loadedStatus.getId().getUserId());
    }

    @Test
    public void testSetHarvesterData() throws Exception {
        final ServiceContext serviceContext = createServiceContext();
        loginAsAdmin(serviceContext);

        final int metadataId = importMetadata(this, serviceContext);

        doSetHarvesterDataTest(_metadataRepository, _dataManager, metadataId);
    }

    static void doSetHarvesterDataTest(MetadataRepository metadataRepository, DataManager dataManager, int metadataId) throws Exception {
        Metadata metadata = metadataRepository.findOne(metadataId);

        assertNull(metadata.getHarvestInfo().getUuid());
        assertNull(metadata.getHarvestInfo().getUri());
        assertFalse(metadata.getHarvestInfo().isHarvested());

        final String harvesterUuid = "harvesterUuid";
        dataManager.setHarvestedExt(metadataId, harvesterUuid);
        metadata = metadataRepository.findOne(metadataId);
        assertEquals(harvesterUuid, metadata.getHarvestInfo().getUuid());
        assertTrue(metadata.getHarvestInfo().isHarvested());
        assertNull(metadata.getHarvestInfo().getUri());


        final String newSource = "newSource";
        // check that another update doesn't break the last setting
        // there used to a bug where this was the case because entity manager wasn't being flushed
        metadataRepository.update(metadataId, new Updater<Metadata>() {
            @Override
            public void apply(@Nonnull Metadata entity) {
                entity.getSourceInfo().setSourceId(newSource);
            }
        });

        assertEquals(newSource, metadata.getSourceInfo().getSourceId());
        assertEquals(harvesterUuid, metadata.getHarvestInfo().getUuid());
        assertTrue(metadata.getHarvestInfo().isHarvested());
        assertNull(metadata.getHarvestInfo().getUri());

        final String harvesterUuid2 = "harvesterUuid2";
        final String harvesterUri = "harvesterUri";
        dataManager.setHarvestedExt(metadataId, harvesterUuid2, Optional.of(harvesterUri));
        metadata = metadataRepository.findOne(metadataId);
        assertEquals(harvesterUuid2, metadata.getHarvestInfo().getUuid());
        assertTrue(metadata.getHarvestInfo().isHarvested());
        assertEquals(harvesterUri, metadata.getHarvestInfo().getUri());

        dataManager.setHarvestedExt(metadataId, null);
        metadata = metadataRepository.findOne(metadataId);
        assertNull(metadata.getHarvestInfo().getUuid());
        assertNull(metadata.getHarvestInfo().getUri());
        assertFalse(metadata.getHarvestInfo().isHarvested());
    }

    @Test
    public void testDeleteBatchMetadata() throws Exception {
        ServiceContext context = createServiceContext();
        loginAsAdmin(context);

        int md1 = importMetadata(this, context);
        int md2 = importMetadata(this, context);

        final SearchManager searchManager = context.getBean(SearchManager.class);
        IndexAndTaxonomy indexReader = searchManager.getNewIndexReader("eng");

        assertEquals(2, indexReader.indexReader.numDocs());
        assertEquals(2, _metadataRepository.count());

        indexReader.indexReader.releaseToNRTManager();

        Specification<Metadata> spec = where(MetadataSpecs.hasMetadataId(md1)).or(MetadataSpecs.hasMetadataId(md2));
        _dataManager.batchDeleteMetadataAndUpdateIndex(spec);

        assertEquals(0, _metadataRepository.count());

        indexReader = searchManager.getNewIndexReader("eng");
        assertEquals(0, indexReader.indexReader.numDocs());
        indexReader.indexReader.releaseToNRTManager();
    }

}
