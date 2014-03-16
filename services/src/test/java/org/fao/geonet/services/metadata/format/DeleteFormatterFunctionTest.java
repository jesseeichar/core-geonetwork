package org.fao.geonet.services.metadata.format;

import jeeves.server.context.ServiceContext;
import org.fao.geonet.domain.Pair;
import org.fao.geonet.services.AbstractServiceIntegrationTest;
import org.fao.geonet.services.metadata.format.function.FormatterFunction;
import org.fao.geonet.services.metadata.format.function.FormatterFunctionRepository;
import org.jdom.Element;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.Assert.assertEquals;

/**
 * Test deleting a formatter function.
 *
 * Created by Jesse on 3/15/14.
 */
public class DeleteFormatterFunctionTest extends AbstractServiceIntegrationTest {

    @Autowired
    private FormatterFunctionRepository _formatterFunctionRepository;

    @Test
    public void testExec() throws Exception {
        final ServiceContext context = createServiceContext();
        loginAsAdmin(context);

        String namespace = "testExecNS";
        final String name = "testExecFunc";

        int countBeforeAdd = _formatterFunctionRepository.findAll().size();
        int countInNamespaceBeforeAdd = _formatterFunctionRepository.findAllByNamespace(namespace).size();

        _formatterFunctionRepository.save(new FormatterFunction(namespace, name, "<div/>"));
        Element params = createParams(
                Pair.read(AddFormatterFunction.PARAM_NAMESPACE, namespace),
                Pair.read(AddFormatterFunction.PARAM_NAME, name)
        );
        assertEquals("ok", new DeleteFormatterFunction().exec(params, context).getName());
        assertEquals(countBeforeAdd, _formatterFunctionRepository.findAll().size());
        assertEquals(countInNamespaceBeforeAdd, _formatterFunctionRepository.findAllByNamespace(namespace).size());
    }
}
