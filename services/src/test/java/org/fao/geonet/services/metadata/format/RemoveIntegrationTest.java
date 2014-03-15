package org.fao.geonet.services.metadata.format;

import jeeves.server.context.ServiceContext;
import junit.framework.TestCase;
import org.fao.geonet.constants.Params;
import org.fao.geonet.domain.Pair;
import org.fao.geonet.services.AbstractServiceIntegrationTest;
import org.jdom.Element;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Test formatter's remove method.
 *
 * Created by Jesse on 3/15/14.
 */
public class RemoveIntegrationTest extends AbstractServiceIntegrationTest {
    @Test
    public void testRegisterThenRemoveFormatter() throws Exception {
        final ServiceContext context = createServiceContext();
        loginAsAdmin(context);

        int numberOfFormattersBefore = FormatterTestUtils.findCountOfHarvesters(this, context);
        final String id = FormatterTestUtils.registerFormatter(this, context, "single_file.xsl");

        assertEquals(numberOfFormattersBefore + 1, FormatterTestUtils.findCountOfHarvesters(this, context));

        Element params = createParams(Pair.read(Params.ID, id));
        new Remove().exec(params, context);

        assertEquals(numberOfFormattersBefore, FormatterTestUtils.findCountOfHarvesters(this, context));
    }
}
