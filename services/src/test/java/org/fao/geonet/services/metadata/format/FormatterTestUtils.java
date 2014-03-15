package org.fao.geonet.services.metadata.format;

import jeeves.server.context.ServiceContext;
import org.fao.geonet.AbstractCoreIntegrationTest;
import org.fao.geonet.constants.Params;
import org.fao.geonet.utils.BinaryFile;
import org.jdom.Element;

import java.io.File;

import static org.fao.geonet.domain.Pair.read;

/**
 * Some utility methods.
 *
 * Created by Jesse on 3/15/14.
 */
public class FormatterTestUtils {
    static String registerFormatter(AbstractCoreIntegrationTest test, ServiceContext context, String formatterName) throws Exception {
        final Register register = new Register();
        final File zipFile = new File(AbstractCoreIntegrationTest.getClassFile(test.getClass()).getParentFile(), formatterName);
        BinaryFile.copy(zipFile, new File(context.getUploadDir(), zipFile.getName()));
        Element params = test.createParams(read(Params.FNAME, zipFile.getName()));
        return register.exec(params, context).getChild("id").getAttributeValue("id");
    }

    static int findCountOfHarvesters(AbstractCoreIntegrationTest test, ServiceContext context) throws Exception {
        return new ListFormatters().exec(test.createParams(), context).getChildren().size();
    }
}
