package org.fao.geonet.services.metadata.format;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.xml.JSONTypes;
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
import static org.fao.geonet.services.metadata.format.FormatterFunctionManagerService.FormatterFunctionServiceAction.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Created by Jesse on 3/15/14.
 */
public class FormatterFunctionManagerServiceTest extends AbstractServiceIntegrationTest {

    @Autowired
    private FormatterFunctionRepository _formatterFunctionRepository;

    @Test
    public void testSet() throws Exception {
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
        final FormatterFunctionManagerService service = createService(SET);
        assertEquals("ok", service.exec(params, context).getName());
        assertEquals(countBeforeAdd + 1, _formatterFunctionRepository.findAll().size());
        assertEquals(countInNamespaceBeforeAdd + 1, _formatterFunctionRepository.findAllByNamespace(namespace).size());

        assertEquals("ok", service.exec(params, context).getName());
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
        final String jsonString = Xml.getJSON(listResult);
        final JSONObject json = JSONObject.fromObject(jsonString);
        assertNotNull(json.getJSONObject(namespace1));
        assertNotNull(json.getJSONObject(namespace2));
        assertEquals(2, json.getJSONObject(namespace1).getJSONArray("formatterFunction").size());
        assertEquals(1, json.getJSONObject(namespace2).getJSONArray("formatterFunction").size());

        assertEquals("formatterFunctions", listResult.getName());
        assertEquals(2, listResult.getChildren().size());

        List<String> namespaceNames = Lists.transform(listResult.getChildren(), new Function<Object, String>() {
            @Nullable
            @Override
            public String apply(@Nullable Object input) {
                return ((Element) input).getName();
            }
        });
        assertTrue(namespaceNames.containsAll(Arrays.asList(namespace1, namespace2)));

        assertEquals(2, Xml.selectNumber(listResult, "count(" + namespace1 + "/formatterFunction)").intValue());
        assertEquals(JSONTypes.ARRAY, Xml.selectString(listResult, namespace1 + "/formatterFunction/@json_class"));
        assertEquals(2, Xml.selectNumber(listResult, "count(" + namespace1 + "[name = '"+namespace1+"']/formatterFunction)").intValue());
        assertEquals(1, Xml.selectNumber(listResult, "count(" + namespace2 + "/formatterFunction)").intValue());

        assertEquals(1, Xml.selectNumber(listResult, "count(" + namespace1 + "/formatterFunction[name = '" + name1 + "'])").intValue());
        assertEquals(1, Xml.selectNumber(listResult, "count(" + namespace1 + "/formatterFunction[name = '" + name1_2 + "'])").intValue
                ());

        assertEqualsIgnoreWhitespace(function1, Xml.selectString(listResult, namespace1 +
                                                                             "/formatterFunction[name = '" + name1 + "']/functionBody"));
        assertEqualsIgnoreWhitespace(function1_2, Xml.selectString(listResult, namespace1 +
                                                                               "/formatterFunction[name = '" + name1_2 + "']/functionBody"));

        assertEqualsIgnoreWhitespace(function2, Xml.selectString(listResult, namespace2 + "/formatterFunction/functionBody"));
        assertEquals(name2, Xml.selectString(listResult, namespace2 + "/formatterFunction/name"));

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
