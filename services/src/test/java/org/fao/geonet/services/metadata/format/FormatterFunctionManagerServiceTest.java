package org.fao.geonet.services.metadata.format;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;
import org.fao.geonet.constants.Params;
import org.fao.geonet.domain.Pair;
import org.fao.geonet.services.AbstractServiceIntegrationTest;
import org.fao.geonet.services.metadata.format.function.FormatterFunction;
import org.fao.geonet.services.metadata.format.function.FormatterFunctionRepository;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;

import static org.fao.geonet.services.metadata.format.FormatterFunctionManagerService.*;
import static org.fao.geonet.services.metadata.format.FormatterFunctionManagerService.FormatterFunctionServiceAction.ADD;
import static org.fao.geonet.services.metadata.format.FormatterFunctionManagerService.FormatterFunctionServiceAction.DELETE;
import static org.fao.geonet.services.metadata.format.FormatterFunctionManagerService.FormatterFunctionServiceAction.LIST;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 *
 * Created by Jesse on 3/15/14.
 */
public class FormatterFunctionManagerServiceTest extends AbstractServiceIntegrationTest {

    @Autowired
    private FormatterFunctionRepository _formatterFunctionRepository;

    @Test
    public void testAdd() throws Exception {
        final ServiceContext context = createServiceContext();
        loginAsAdmin(context);

        String namespace = "testExecNS";
        int countBeforeAdd = _formatterFunctionRepository.findAll().size();
        int countInNamespaceBeforeAdd = _formatterFunctionRepository.findAllByNamespace(namespace).size();
        final String name = "testExecFunc";
        Element params = createParams(
                Pair.read(PARAM_NAMESPACE, namespace),
                Pair.read(PARAM_NAME, name),
                Pair.read(PARAM_FUNCTION, "<div>this is the value of the function</div>")
        );
        assertEquals("ok", createService(ADD).exec(params, context).getName());
        assertEquals(countBeforeAdd + 1, _formatterFunctionRepository.findAll().size());
        assertEquals(countInNamespaceBeforeAdd + 1, _formatterFunctionRepository.findAllByNamespace(namespace).size());
    }

    @Test
    public void testDelete() throws Exception {
        final ServiceContext context = createServiceContext();
        loginAsAdmin(context);

        String namespace = "testExecNS";
        final String name = "testExecFunc";

        int countBeforeAdd = _formatterFunctionRepository.findAll().size();
        int countInNamespaceBeforeAdd = _formatterFunctionRepository.findAllByNamespace(namespace).size();

        _formatterFunctionRepository.save(new FormatterFunction(namespace, name, "<div/>"));
        Element params = createParams(
                Pair.read(PARAM_NAMESPACE, namespace),
                Pair.read(PARAM_NAME, name)
        );
        assertEquals("ok", createService(DELETE).exec(params, context).getName());
        assertEquals(countBeforeAdd, _formatterFunctionRepository.findAll().size());
        assertEquals(countInNamespaceBeforeAdd, _formatterFunctionRepository.findAllByNamespace(namespace).size());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testList() throws Exception {
        final ServiceContext context = createServiceContext();
        loginAsAdmin(context);

        String namespace1 = "ns1";
        final String name1 = "name1";
        final String name1_2 = "name1_2";
        String namespace2 = "ns2";
        final String name2 = "name2";

        final String function1 = "<div/>";
        _formatterFunctionRepository.save(new FormatterFunction(namespace1, name1, function1));
        final String function1_2 = "<html/>";
        _formatterFunctionRepository.save(new FormatterFunction(namespace1, name1_2, function1_2));
        final String function2 = "<span/>";
        _formatterFunctionRepository.save(new FormatterFunction(namespace2, name2, function2));
        Element params = createParams(
                Pair.read(PARAM_NAMESPACE, namespace1),
                Pair.read(PARAM_NAME, name1)
        );
        final Element listResult = createService(LIST).exec(params, context);
        System.out.println(Xml.getJSON(listResult));

        assertEquals("formatterFunctions", listResult.getName());
        assertEquals(2, listResult.getChildren("namespace").size());
        assertEquals(2, listResult.getChildren().size());

        List<String> namespaceNames = Lists.transform(listResult.getChildren(), new Function<Object, String>() {
            @Nullable
            @Override
            public String apply(@Nullable Object input) {
                return ((Element)input).getChildText("name");
            }
        });
        assertTrue(namespaceNames.containsAll(Arrays.asList(namespace1, namespace2)));

        assertEquals(2, Xml.selectNumber(listResult, "count(namespace[name='"+namespace1+"']/functions/function)").intValue());
        assertEquals(1, Xml.selectNumber(listResult, "count(namespace[name='"+namespace2+"']/functions/function)").intValue());

        assertEquals(1, Xml.selectNumber(listResult, "count(namespace[name='"+namespace1+"']/functions/function[name = '"+name1+"'])").intValue());
        assertEquals(1, Xml.selectNumber(listResult, "count(namespace[name='"+namespace1+"']/functions/function[name = '"+name1_2+"'])").intValue());

        assertEqualsIgnoreWhitespace(function1, Xml.selectString(listResult, "namespace[name='" + namespace1 +
                                                                             "']/functions/function[name = '" + name1 + "']/function"));
        assertEqualsIgnoreWhitespace(function1_2, Xml.selectString(listResult, "namespace[name='" + namespace1 +
                                                                               "']/functions/function[name = '" +
                                                                               name1_2 + "']/function"));

        assertEqualsIgnoreWhitespace(function2, Xml.selectString(listResult, "namespace[name='" + namespace2 + "']/functions" +
                                                                             "/function/function"));
        assertEquals(name2, Xml.selectString(listResult, "namespace[name='" + namespace2 + "']/functions/function/name"));

    }

    private void assertEqualsIgnoreWhitespace(String expected, String actual) {
        assertEquals(expected.replaceAll("\\s+", ""), actual.replaceAll("\\s+", ""));
    }

    private FormatterFunctionManagerService createService(FormatterFunctionServiceAction action) throws Exception {
        final FormatterFunctionManagerService service = new FormatterFunctionManagerService();
        final ServiceConfig serviceConfig = new ServiceConfig(Arrays.asList(createServiceConfigParam(Params.ACTION, action.name())));
        service.init(getWebappDir(getClass()), serviceConfig);
        return service;
    }
}
