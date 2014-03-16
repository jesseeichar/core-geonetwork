package org.fao.geonet.services.metadata.format;

import jeeves.server.context.ServiceContext;
import org.fao.geonet.AbstractCoreIntegrationTest;
import org.fao.geonet.domain.Pair;
import org.jdom.Element;
import org.junit.Test;

/**
 *
 * Created by Jesse on 3/15/14.
 */
public class AddFormatterFunctionTest extends AbstractCoreIntegrationTest {
    @Test
    public void testExec() throws Exception {
        final ServiceContext context = createServiceContext();
        loginAsAdmin(context);

        Element params = createParams(
                Pair.read(AddFormatterFunction.PARAM_NAMESPACE, "test"),
                Pair.read(AddFormatterFunction.PARAM_NAME, "name"),
                Pair.read(AddFormatterFunction.PARAM_FUNCTION, "<html>")
        );
        new AddFormatterFunction().exec(params, context);
    }
}
