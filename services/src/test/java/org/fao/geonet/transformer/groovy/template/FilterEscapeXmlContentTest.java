package org.fao.geonet.transformer.groovy.template;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class FilterEscapeXmlContentTest {

    @Test
    public void testProcess() throws Exception {
        assertEquals("&gt;&amp;&lt;", new FilterEscapeXmlContent().process(null, ">&<"));
    }
}