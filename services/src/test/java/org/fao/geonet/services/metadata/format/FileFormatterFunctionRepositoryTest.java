package org.fao.geonet.services.metadata.format;

import org.fao.geonet.kernel.GeonetworkDataDirectory;
import org.jdom.Element;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import static org.fao.geonet.services.metadata.format.FormatterFunctionRepository.XSL_NAMESPACE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Test {@link org.fao.geonet.services.metadata.format.FormatterFunctionRepository}.
 *
 * Created by Jesse on 3/16/14.
 */
public class FileFormatterFunctionRepositoryTest {
    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void testFindAllNamespaces() throws Exception {
        FileFormatterFunctionRepository repo = getRepo();
        assertEquals(0, repo.findAllNamespaces().size());

        repo.save(new FormatterFunction("ns1", "name1", new Element("result")));
        repo.save(new FormatterFunction("ns2", "name2", new Element("result2")));

        final List<String> allNamespaces = repo.findAllNamespaces();
        assertEquals(2, allNamespaces.size());
        assertTrue(allNamespaces.containsAll(Arrays.asList("ns1","ns1")));
    }

    @Test
    public void testFindAll() throws Exception {
        FileFormatterFunctionRepository repo = getRepo();
        assertEquals(0, repo.findAll().size());
        final FormatterFunction function1 = new FormatterFunction("ns1", "name1", new Element("result1"));
        repo.save(function1);
        final FormatterFunction function2 = new FormatterFunction("ns1", "name1_2", new Element("result1_2"));
        repo.save(function2);
        final FormatterFunction function3 = new FormatterFunction("ns2", "name2", new Element("result2"));
        repo.save(function3);

        final List<FormatterFunction> allFunctions = repo.findAll();
        assertEquals(3, allFunctions.size());
        assertTrue(allFunctions.containsAll(Arrays.asList(function1, function2, function3)));
    }

    @Test
    public void testFindAllByNamespace() throws Exception {
        FileFormatterFunctionRepository repo = getRepo();
        final FormatterFunction function1 = new FormatterFunction("ns1", "name1", new Element("result1"));
        repo.save(function1);
        final FormatterFunction function2 = new FormatterFunction("ns1", "name1_2", new Element("result1_2"));
        repo.save(function2);
        final FormatterFunction function3 = new FormatterFunction("ns2", "name2", new Element("result2"));
        repo.save(function3);

        List<FormatterFunction> allFunctions = repo.findAllByNamespace("ns1");
        assertEquals(2, allFunctions.size());
        assertTrue(allFunctions.containsAll(Arrays.asList(function1, function2)));

        allFunctions = repo.findAllByNamespace("ns2");
        assertEquals(1, allFunctions.size());
        assertTrue(allFunctions.contains(function3));

    }

    @Test
    public void testDelete() throws Exception {
        FileFormatterFunctionRepository repo = getRepo();
        final FormatterFunction function1 = new FormatterFunction("ns1", "name1", new Element("result1"));
        repo.save(function1);
        final FormatterFunction function2 = new FormatterFunction("ns1", "name1_2", new Element("result1_2"));
        repo.save(function2);

        repo.delete(function1.getNamespace(), function1.getName());

        List<FormatterFunction> allFunctions = repo.findAll();
        assertEquals(1, allFunctions.size());
        assertTrue(allFunctions.containsAll(Arrays.asList(function2)));

        repo.delete(function2.getNamespace(), function2.getName());

        allFunctions = repo.findAll();
        assertEquals(0, allFunctions.size());

        assertEquals(0, new File(temporaryFolder.getRoot(), FileFormatterFunctionRepository.FUNCTION_DIRECTORY).list().length);

    }

    @Test(expected = IllegalArgumentException.class)
    public void testDisallowIllegalVariableReferenceInAttribute() throws Exception {
        FileFormatterFunctionRepository repo = getRepo();
        final FormatterFunction function1 = new FormatterFunction("ns1", "name1",
                new Element("result1").setAttribute("name","{$nameParam}"));
        repo.save(function1);
    }
    @Test(expected = IllegalArgumentException.class)
    public void testDisallowIllegalVariableReferenceInValueOf() throws Exception {
        FileFormatterFunctionRepository repo = getRepo();

        final FormatterFunction function = new FormatterFunction("ns1", "name1",
                new Element("result1").addContent(new Element("value-of", XSL_NAMESPACE).setAttribute("select", "$nameParam")));
        repo.save(function);
    }

    @Test
    public void testAllowParamReference() throws Exception {
        FileFormatterFunctionRepository repo = getRepo();
        final FormatterFunction function1 = new FormatterFunction("ns1", "name1",
                new Element("nameParam", XSL_NAMESPACE),
                new Element("result1").setAttribute("name","{$nameParam}"));
        repo.save(function1);

        final FormatterFunction function2 = new FormatterFunction("ns1", "name1",
                new Element("nameParam", XSL_NAMESPACE),
                new Element("result1").addContent(new Element("value-of", XSL_NAMESPACE).setAttribute("select", "$nameParam")));
        repo.save(function2);


    }
    private FileFormatterFunctionRepository getRepo() {
        GeonetworkDataDirectory dataDir = Mockito.mock(GeonetworkDataDirectory.class);
        Mockito.when(dataDir.getMetadataFormatterDir()).thenReturn(temporaryFolder.getRoot());
        final FileFormatterFunctionRepository repository = new FileFormatterFunctionRepository();
        repository._dataDirectory = dataDir;
        return repository;
    }
}
