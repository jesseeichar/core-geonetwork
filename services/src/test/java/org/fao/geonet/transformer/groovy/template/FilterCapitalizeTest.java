package org.fao.geonet.transformer.groovy.template;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class FilterCapitalizeTest {

    @Test
    public void testProcess() throws Exception {
        assertEquals("It Is A Title", new FilterCapitalize().process(null, "it is a title"));
    }
}