package org.fao.geonet.services.metadata.format;

import jeeves.server.context.ServiceContext;
import org.fao.geonet.constants.Params;
import org.fao.geonet.services.AbstractServiceIntegrationTest;
import org.jdom.Element;
import org.junit.Test;

import static org.fao.geonet.domain.Pair.read;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Test Formatter framework.
 *
 * Created by Jesse on 3/15/14.
 */
public class FormatIntegrationTest extends AbstractServiceIntegrationTest {
    @Test
    public void formatFixedLanguageFormatter() throws Exception {
        final ServiceContext serviceContext = createServiceContext();
        loginAsAdmin(serviceContext);
        String formatterId = FormatterTestUtils.registerFormatter(this, serviceContext, "fixed_locale.zip");

        int metadataId = importMetadata(this, serviceContext);

        assertNotNull(formatMetadata(serviceContext, formatterId, metadataId));

        serviceContext.setLanguage("eng");
        assertTranslation("eng", serviceContext, formatterId, metadataId, true);
        serviceContext.setLanguage("fre");
        assertTranslation("eng", serviceContext, formatterId, metadataId, true);
    }
    @Test
    public void formatLocalizedFolderLanguageFormatter() throws Exception {
        final ServiceContext serviceContext = createServiceContext();
        loginAsAdmin(serviceContext);
        String formatterId = FormatterTestUtils.registerFormatter(this, serviceContext, "localized_folder.zip");

        int metadataId = importMetadata(this, serviceContext);

        assertNotNull(formatMetadata(serviceContext, formatterId, metadataId));

        serviceContext.setLanguage("eng");
        assertTranslation("eng", serviceContext, formatterId, metadataId, true);
        serviceContext.setLanguage("fre");
        assertTranslation("fre", serviceContext, formatterId, metadataId, true);
    }

    @Test
    public void formatLocalizedFlatZipLanguageFormatter() throws Exception {
        final ServiceContext serviceContext = createServiceContext();
        loginAsAdmin(serviceContext);
        String formatterId = FormatterTestUtils.registerFormatter(this, serviceContext, "localized_flatzip.zip");

        int metadataId = importMetadata(this, serviceContext);

        assertNotNull(formatMetadata(serviceContext, formatterId, metadataId));

        serviceContext.setLanguage("eng");
        assertTranslation("eng", serviceContext, formatterId, metadataId, true);
        serviceContext.setLanguage("fre");
        assertTranslation("fre", serviceContext, formatterId, metadataId, true);
    }
    @Test
    public void formatSingleFileFormatter() throws Exception {
        final ServiceContext serviceContext = createServiceContext();
        loginAsAdmin(serviceContext);
        String formatterId = FormatterTestUtils.registerFormatter(this, serviceContext, "single_file.xsl");

        int metadataId = importMetadata(this, serviceContext);

        assertNotNull(formatMetadata(serviceContext, formatterId, metadataId));

        serviceContext.setLanguage("eng");
        assertTranslation("eng", serviceContext, formatterId, metadataId, false);
        serviceContext.setLanguage("fre");
        assertTranslation("fre", serviceContext, formatterId, metadataId, false);
    }

    private Element formatMetadata(ServiceContext serviceContext, String formatterId, int metadataId) throws Exception {
        final Format format = new Format();
        Element params = createParams(
                read("xsl", formatterId),
                read(Params.ID, metadataId)
        );
        return format.exec(params, serviceContext);
    }

    private void assertTranslation(String langCode, ServiceContext serviceContext, String formatterId, int metadataId, boolean
            testBundledTranslations) throws Exception {
        Element formattedMetadata = formatMetadata(serviceContext, formatterId, metadataId);

        String fromFormatterLocStringsTranslation;
        String fromGuiStringsTranslation;
        if (langCode.equals("eng")){
            fromFormatterLocStringsTranslation = "String";
            fromGuiStringsTranslation = "Home";
        } else if (langCode.equals("fre")){
            fromFormatterLocStringsTranslation = "Chaine";
            fromGuiStringsTranslation = "Accueil";
        } else {
            throw new IllegalArgumentException(langCode);
        }

        assertEqualsText(fromGuiStringsTranslation, formattedMetadata, "*//s1/@v");
        if (testBundledTranslations) {
            assertEqualsText(fromFormatterLocStringsTranslation, formattedMetadata, "*//s2/@v");
        }
    }

}
