package org.fao.geonet.services.metadata.format.function;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.io.Files;
import org.apache.commons.io.IOUtils;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.kernel.GeonetworkDataDirectory;
import org.fao.geonet.utils.IO;
import org.fao.geonet.utils.Xml;
import org.jdom.Document;
import org.jdom.Element;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

/**
 * Manages loading and writing functions to files.
 *
 * Created by Jesse on 3/15/14.
 */
public class FileFormatterFunctionRepository implements FormatterFunctionRepository {
    public static final String FUNCTION_DIRECTORY = "formatter-shared-functions";

    @Autowired
    GeonetworkDataDirectory _dataDirectory;

    @Override
    @Nonnull
    public Set<String> findAllNamespaces() throws IOException {
        final String[] functions = getFunctionDir().list();
        TreeSet<String> sortedFunctions = new TreeSet<String>();
        if (functions != null) {
            sortedFunctions.addAll(Lists.transform(Arrays.asList(functions), new Function<String, String>() {
                @Nullable
                @Override
                public String apply(@Nonnull String input) {
                    return Files.getNameWithoutExtension(input);
                }
            }));
        }
        return sortedFunctions;
    }


    @Override
    @Nonnull
    public Set<FormatterFunction> findAll() throws IOException {
        final Set<FormatterFunction> functions = new TreeSet<FormatterFunction>(COMPARATOR);
        final File[] functionFiles = getFunctionDir().listFiles();
        if (functionFiles != null) {
            for (File functionFile : functionFiles) {
                loadFunctionFromFile(functions, functionFile);
            }
        }
        return functions;
    }

    @Override
    @Nonnull
    public Set<FormatterFunction> findAllByNamespace(@Nonnull String namespace) throws IOException {
        return loadFunctionFromFile(new TreeSet<FormatterFunction>(COMPARATOR), getFunctionFile(namespace));
    }

    @Override
    public void save(@Nonnull FormatterFunction newFunction) throws IOException {
        final String namespace = newFunction.getNamespace();
        final Set<FormatterFunction> allByNamespace = findAllByNamespace(namespace);

        allByNamespace.add(newFunction);
        writeFunctionsToFile(namespace, allByNamespace);
    }

    @Override
    public void delete(@Nonnull String namespace, @Nonnull String name) throws IOException {
        final Set<FormatterFunction> functions = findAllByNamespace(namespace);
        Iterator<FormatterFunction> allInNamespaceIter = functions.iterator();
        while (allInNamespaceIter.hasNext()) {
            FormatterFunction next = allInNamespaceIter.next();
            if (next.hasId(namespace, name)) {
                allInNamespaceIter.remove();
            }
        }
        if (functions.isEmpty()) {
            IO.delete(getFunctionFile(namespace), true, Geonet.FORMATTER);
        } else {
            writeFunctionsToFile(namespace, functions);
        }
    }

    private void writeFunctionsToFile(String namespace, Collection<FormatterFunction> allByNamespace) throws IOException {
        Element root = new Element(EL_STYLESHEET, XSL_NAMESPACE).setAttribute(ATT_VERSION, "2.0");
        root.addNamespaceDeclaration(FormatterFunction.getJDomNamespace(namespace));
        for (FormatterFunction formatterFunction : allByNamespace) {
            root.addContent(formatterFunction.toElement());
        }

        Document xslt = new Document(root);
        FileOutputStream outputStream = null;
        BufferedOutputStream bufferedOutputStream = null;
        try {
            outputStream = new FileOutputStream(getFunctionFile(namespace));
            bufferedOutputStream = new BufferedOutputStream(outputStream);
            Xml.writeResponse(xslt, bufferedOutputStream);
        } finally {
            IOUtils.closeQuietly(bufferedOutputStream);
            IOUtils.closeQuietly(outputStream);
        }
    }

    private Set<FormatterFunction> loadFunctionFromFile(Set<FormatterFunction> functions, File functionFile) {
        if (!functionFile.exists()) {
            return functions;
        }
        try {
            final Element functionXml = Xml.loadFile(functionFile);
            @SuppressWarnings("unchecked")
            final List<Element> functionsEls = functionXml.getChildren();
            final String namespace = Files.getNameWithoutExtension(functionFile.getName());
            for (Element functionEl : functionsEls) {
                functions.add(new FormatterFunction(namespace, functionEl));
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return functions;
    }

    private File getFunctionFile(String namespace) throws IOException {
        return new File(getFunctionDir(), namespace+".xsl");
    }

    private File getFunctionDir() throws IOException {
        final File dir = new File(_dataDirectory.getMetadataFormatterDir(), FUNCTION_DIRECTORY);
        IO.mkdirs(dir, "Metadata Formatter Function Directory");
        return dir;
    }
}
