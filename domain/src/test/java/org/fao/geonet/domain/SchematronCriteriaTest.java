package org.fao.geonet.domain;

import static org.junit.Assert.*;
import org.fao.geonet.repository.SchematronCriteriaGroupRepositoryTest;
import org.fao.geonet.repository.SchematronCriteriaRepositoryTest;
import org.jdom.Element;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Test schematron criteria class
 * Created by Jesse on 2/28/14.
 */
public class SchematronCriteriaTest {

    private AtomicInteger _inc = new AtomicInteger();

    @Test
    public void testAsXml() throws Exception {
        final SchematronCriteria criteria = SchematronCriteriaGroupRepositoryTest.newSchematronCriteria(_inc);
        criteria.setValue("");
        final Element xml = criteria.asXml();
        assertEquals("''", xml.getChildText("value"));

    }
}
