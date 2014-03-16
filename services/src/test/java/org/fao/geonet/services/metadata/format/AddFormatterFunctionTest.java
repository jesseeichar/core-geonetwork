package org.fao.geonet.services.metadata.format;

import jeeves.server.context.ServiceContext;
import org.fao.geonet.domain.Pair;
import org.fao.geonet.services.AbstractServiceIntegrationTest;
import org.fao.geonet.services.metadata.format.function.FormatterFunctionRepository;
import org.jdom.Element;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.Assert.assertEquals;

/**
 *
 * Created by Jesse on 3/15/14.
 */
public class AddFormatterFunctionTest extends AbstractServiceIntegrationTest {

    @Autowired
    private FormatterFunctionRepository _formatterFunctionRepository;

    @Test
    public void testExec() throws Exception {
        final ServiceContext context = createServiceContext();
        loginAsAdmin(context);

        String namespace = "testExecNS";
        int countBeforeAdd = _formatterFunctionRepository.findAll().size();
        int countInNamespaceBeforeAdd = _formatterFunctionRepository.findAllByNamespace(namespace).size();
        final String name = "testExecFunc";
        Element params = createParams(
                Pair.read(AddFormatterFunction.PARAM_NAMESPACE, namespace),
                Pair.read(AddFormatterFunction.PARAM_NAME, name),
                Pair.read(AddFormatterFunction.PARAM_FUNCTION, "<div>this is the value of the function</div>")
        );
        assertEquals("ok", new AddFormatterFunction().exec(params, context).getName());
        assertEquals(countBeforeAdd + 1, _formatterFunctionRepository.findAll().size());
        assertEquals(countInNamespaceBeforeAdd + 1, _formatterFunctionRepository.findAllByNamespace(namespace).size());
    }
}
