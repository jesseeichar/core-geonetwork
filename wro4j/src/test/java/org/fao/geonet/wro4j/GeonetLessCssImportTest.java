package org.fao.geonet.wro4j;

import com.google.common.io.Resources;
import org.junit.Test;
import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.ResourceType;
import ro.isdc.wro.model.resource.locator.UriLocator;
import ro.isdc.wro.model.resource.locator.UrlUriLocator;
import ro.isdc.wro.model.resource.locator.factory.UriLocatorFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertEquals;

public class GeonetLessCssImportTest {
    @Test
    public void testNoDuplicateImports() throws Exception {
        final URL startFile = GeonetLessCssImportTest.class.getResource("/import-test-basic/start.less");
        Path path = Paths.get(startFile.toURI()).getParent().toAbsolutePath();
        final String lessFile = Resources.toString(startFile, Charset.forName("UTF-8")).replace("{{abs}}", path.toUri().toString());
        Resource resource = Resource.create(startFile.toURI().toString(), ResourceType.CSS);
        StringWriter writer = new StringWriter();
        try (Reader reader = new StringReader(lessFile)) {
            createLessImportCompiler().process(resource, reader, writer);
        }

        final String css = writer.toString();
        assertEquals(css, 1, count(css, "@import url(\"not-included.less\");"));
        assertEquals(css, 1, count(css, "div.file1 {}"));
        assertEquals(css, 1, count(css, "div.file2 {}"));
        assertEquals(css, 1, count(css, "div.file3 {}"));
        assertEquals(css, 1, count(css, "@impact 1234"));
        assertEquals(css, 1, count(css, "@importName 11;"));
        assertEquals(css, 1, count(css, "div.file4 {}"));
        assertEquals(css, 0, count(css, "div.not-included {}"));

        assertOrdering(css, "@import url(\"not-included.less\");", "div.file1 {}", "@impact 1234", "@importName 11;",
                "div.file2 {}", "div.file3 {}", "div.file4 {}");
    }

    @Test
    public void testImportOption() throws Exception {
        final URL startFile = GeonetLessCssImportTest.class.getResource("/import-test-options/start.less");
        Path path = Paths.get(startFile.toURI()).getParent().toAbsolutePath();
        final String lessFile = Resources.toString(startFile, Charset.forName("UTF-8")).replace("{{abs}}", path.toUri().toString());
        Resource resource = Resource.create(startFile.toURI().toString(), ResourceType.CSS);
        StringWriter writer = new StringWriter();
        try (Reader reader = new StringReader(lessFile)) {
            createLessImportCompiler().process(resource, reader, writer);
        }

        final String css = writer.toString();
        assertEquals(css, 2, count(css, "div.multi {}"));
        assertEquals(css, 1, count(css, "div.once {}"));

        assertOrdering(css, "div.multi {}", "div.once {}");
    }

    public GeonetLessCssImport createLessImportCompiler() throws IllegalAccessException, NoSuchFieldException {
        final GeonetLessCssImport processor = new GeonetLessCssImport();
        final Field field = GeonetLessCssImport.class.getDeclaredField("uriLocatorFactory");
        field.setAccessible(true);
        field.set(processor, new UriLocatorFactory() {
            private final UrlUriLocator locator = new UrlUriLocator();

            @Override
            public InputStream locate(String uri) throws IOException {
                return locator.locate(uri);
            }

            @Override
            public UriLocator getInstance(String uri) {
                return locator;
            }
        });
        return processor;
    }

    private void assertOrdering(String css, String... fragments) {
        int lastIndex = -1;
        int index;
        for (int i = 0; i < fragments.length; i++) {
            index = css.indexOf(fragments[i]);
            if (index <= lastIndex) {
                throw new AssertionError("An incorrect order was found.  " + fragments[i] + " was before " + fragments[i - 1]);
            }
        }
    }

    private int count(String css, String fragment) {
        int index = css.indexOf(fragment);
        int count = 0;
        while(index > -1) {
            count ++;
            index = css.indexOf(fragment, index + 1);
        }

        return count;
    }
}