package org.fao.geonet.services.metadata.format;

import org.fao.geonet.utils.Xml;
import org.jdom.Comment;
import org.jdom.Element;
import org.jdom.Text;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Test constructors and methods of {@link org.fao.geonet.services.metadata.format.FormatterFunction}
 * Created by Jesse on 3/16/14.
 */
public class FormatterFunctionTest {
    @Test
    public void testCommentsInFunction() throws Exception {
        final String comment = "comment";
        final FormatterFunction formatterFunction = new FormatterFunction("ns", "name", new Comment(comment));
        assertEquals("<!-- "+comment+" -->", formatterFunction.getFunction());
    }
    @Test
    public void testTextInFunction() throws Exception {
        final String text = "text";
        final FormatterFunction formatterFunction = new FormatterFunction("ns", "name", new Text(text));
        assertEquals(text, formatterFunction.getFunction());
    }
    @Test
    public void testElementInFunction() throws Exception {
        final Element element = new Element("el").setText("text");
        final FormatterFunction formatterFunction = new FormatterFunction("ns", "name", element);
        assertEquals(Xml.getString(element), formatterFunction.getFunction());
    }

    @Test
    public void testValidity() throws Exception {

        final Element element = new Element("el").setText("text");
        final FormatterFunction formatterFunction = new FormatterFunction("ns", "name", element);

        assertEquals(element, formatterFunction.testValidity());
    }
}
