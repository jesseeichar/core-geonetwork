package org.fao.geonet.services.metadata.format.function;

import com.vividsolutions.jts.util.Assert;
import org.fao.geonet.utils.Xml;
import org.jdom.*;

import javax.annotation.Nonnull;
import java.io.*;
import java.util.Arrays;
import java.util.List;

/**
 * An object representing a xslt function that can be used in any xslt.
 * <p/>
 * Created by Jesse on 3/15/14.
 */
public class FormatterFunction {
    private final String name;
    private final String namespace;
    private final String function;

    public FormatterFunction(@Nonnull String namespace, @Nonnull String name, @Nonnull String function) {
        this.name = name;
        this.namespace = namespace;
        this.function = function;
        checkValues();
    }

    public FormatterFunction(@Nonnull String namespace, @Nonnull String name, @Nonnull Content... function) {
        this(namespace, name, convertToString(Arrays.asList(function)));
    }

    public FormatterFunction(String expectedNamespace, Element functionEl) {
        String[] parts = functionEl.getAttributeValue("name").split(":", 2);
        this.name = parts[1];
        this.namespace = parts[0];
        Assert.equals(this.namespace, expectedNamespace);
        @SuppressWarnings("unchecked")
        List<Content> functionBodyEls = functionEl.getContent();
        this.function = convertToString(functionBodyEls);
        checkValues();
    }

    private void checkValues() {

    }

    public static Namespace getJDomNamespace(String namespace) {
        return Namespace.getNamespace(namespace, "org.fao.geonet.metadata.formatter.function." + namespace);
    }

    private static String convertToString(List<Content> functionBodyEls) {
        StringBuilder functionText = new StringBuilder();
        for (Content functionBodyEl : functionBodyEls) {
            if (functionBodyEl instanceof Element) {
                functionText.append(Xml.getString((Element) functionBodyEl));
            } else if (functionBodyEl instanceof Text) {
                functionText.append(((Text) functionBodyEl).getText());
            } else if (functionBodyEl instanceof Comment) {
                functionText.append("<!-- " + ((Comment) functionBodyEl).getText() + " -->");
            }
        }

        return functionText.toString();
    }

    /**
     * Get the function name.
     *
     * @return the function name.
     */
    @Nonnull
    public String getName() {
        return name;
    }

    /**
     * Get the namespace prefix of the function.
     *
     * @return the namespace prefix of the function.
     */
    @Nonnull
    public String getNamespace() {
        return namespace;
    }

    /**
     * Get the function body of the xslt transform function.
     *
     * @return the function body of the xslt transform function.
     */
    @Nonnull
    public String getFunction() {
        return function;
    }

    /**
     * Create the xslt xml for the function.  This element can be added to a xslt document.
     */
    @Nonnull
    public Element toElement() {
        try {
            final String functionData = "<xsl:function xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\" name=\"" + getQualifiedName()
                                        + "\">" + function + "</xsl:function>";
            return Xml.loadString(functionData, false);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (JDOMException e) {
            throw new RuntimeException(e);
        }
    }

    private String getQualifiedName() {
        return namespace + ":" + name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FormatterFunction that = (FormatterFunction) o;

        if (!function.replaceAll("\\s+", "").equals(that.function.replaceAll("\\s+", ""))) return false;
        if (!name.equals(that.name)) return false;
        if (!namespace.equals(that.namespace)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + namespace.hashCode();
        result = 31 * result + function.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "FormatterFunction{" +
               "namespace='" + namespace + '\'' +
               ", name='" + name + '\'' +
               '}';
    }

    /**
     * Check that this function has the same namespace and name as provided
     *
     * @param namespace the desired namespace
     * @param name      the desired name
     * @return true if this function has the same namespace and name as provided
     */
    public boolean hasId(@Nonnull String namespace, @Nonnull String name) {
        return this.namespace.equals(namespace) && this.name.equals(name);
    }

}
