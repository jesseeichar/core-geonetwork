package org.fao.geonet.services.metadata.format;

import jeeves.server.context.ServiceContext;
import org.fao.geonet.exceptions.BadParameterEx;
import org.fao.geonet.services.AbstractServiceIntegrationTest;
import org.fao.geonet.services.metadata.format.function.FileFormatterFunctionRepository;
import org.junit.Test;

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

    @Test(expected = BadParameterEx.class)
    public void testDupId() throws Exception {
        final ServiceContext context = createServiceContext();
        loginAsAdmin(context);

        final String formatterName = "fixed_locale.zip";

        FormatterTestUtils.registerFormatter(this, context, formatterName, FileFormatterFunctionRepository.FUNCTION_DIRECTORY);
    }

    @Test
    public void testRegisterSingleFileWithId() throws Exception {
        final ServiceContext context = createServiceContext();
        loginAsAdmin(context);

        int numberOfFormattersBefore = FormatterTestUtils.findCountOfHarvesters(this, context);

        final String metadataFormatterId = "testRegisterSingleFileWithId";

        String id = FormatterTestUtils.registerFormatter(this, context, "single_file.xsl", metadataFormatterId);
        assertEquals(metadataFormatterId, id);

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
