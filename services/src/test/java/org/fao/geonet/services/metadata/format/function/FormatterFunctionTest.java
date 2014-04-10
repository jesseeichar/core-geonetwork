package org.fao.geonet.services.metadata.format.function;

import org.fao.geonet.services.metadata.format.function.FormatterFunction;
import org.fao.geonet.utils.Xml;
import org.jdom.Comment;
import org.jdom.Element;
import org.jdom.Text;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Test constructors and methods of {@link org.fao.geonet.services.metadata.format.function.FormatterFunction}
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
    public void testLegalName() throws Exception {
        assertIllegalName("f/f");
        assertIllegalName("f;f");
        assertIllegalName("f#f");
        assertIllegalName("f%f");
        assertIllegalName("f%\nf");
        assertIllegalName("f%\tf");
        assertIllegalName("f% f");
        assertIllegalName("1asd");
        assertIllegalName("-asd");
        assertIllegalName("_asd");

        // if the following don't throw exception then it is considered a pass
        new FormatterFunction("ns1", "asjdflkas", "body");
        new FormatterFunction("ns1", "asdfj1123", "body");
        new FormatterFunction("ns1", "Ads123_djasf-fkj", "body");

    }

    private void assertIllegalName(String illegalName) {
        try {
            new FormatterFunction("ns1", illegalName, "data");
            fail("Excepted error");
        } catch (IllegalArgumentException e) {
            // good
        }
    }
}
