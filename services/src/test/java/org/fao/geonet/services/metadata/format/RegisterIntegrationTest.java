package org.fao.geonet.services.metadata.format;

import jeeves.server.context.ServiceContext;
import org.fao.geonet.constants.Params;
import org.fao.geonet.services.AbstractServiceIntegrationTest;
import org.fao.geonet.utils.BinaryFile;
import org.jdom.Element;
import org.junit.Test;

import java.io.File;

import static org.fao.geonet.domain.Pair.read;
import static org.junit.Assert.assertEquals;

/**
 * Test Register formatter
 * Created by Jesse on 3/15/14.
 */
public class RegisterIntegrationTest extends AbstractServiceIntegrationTest {
    @Test
    public void testRegisterZip() throws Exception {
        final ServiceContext context = createServiceContext();
        loginAsAdmin(context);

        final String formatterName = "fixed_locale.zip";

        int numberOfFormattersBefore = FormatterTestUtils.findCountOfHarvesters(this, context);
        final String id = FormatterTestUtils.registerFormatter(this, context, formatterName);

        assertEquals("fixed_locale", id);

        assertEquals(numberOfFormattersBefore + 1, FormatterTestUtils.findCountOfHarvesters(this, context));
    }

    @Test
    public void testRegisterSingleFileWithId() throws Exception {
        final ServiceContext context = createServiceContext();
        loginAsAdmin(context);

        int numberOfFormattersBefore = FormatterTestUtils.findCountOfHarvesters(this, context);
        final Register register = new Register();
        final File formatterFile = new File(getClassFile(RegisterIntegrationTest.class).getParentFile(), "single_file.xsl");
        BinaryFile.copy(formatterFile, new File(context.getUploadDir(), formatterFile.getName()));

        final String metadataFormatterId = "testRegisterSingleFileWithId";
        Element params = createParams(
                read(Params.FNAME, formatterFile.getName()),
                read(Params.ID, metadataFormatterId)
        );
        final Element result = register.exec(params, context);

        assertEquals(metadataFormatterId, result.getChild("id").getAttributeValue("id"));

        assertEquals(numberOfFormattersBefore + 1, FormatterTestUtils.findCountOfHarvesters(this, context));
    }

    @Test
    public void testRegisterSingleFile() throws Exception {
        final ServiceContext context = createServiceContext();
        loginAsAdmin(context);

        int numberOfFormattersBefore = FormatterTestUtils.findCountOfHarvesters(this, context);
        final String id = FormatterTestUtils.registerFormatter(this, context, "single_file.xsl");

        assertEquals("single_file", id);

        assertEquals(numberOfFormattersBefore + 1, FormatterTestUtils.findCountOfHarvesters(this, context));

    }

}
