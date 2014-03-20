package org.fao.geonet.services.metadata.format.function;

import org.jdom.Namespace;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

/**
 * An API for accessing Formatters and related objects.
 *
 * Created by Jesse on 3/15/14.
 */
public interface FormatterFunctionRepository {
    Namespace XSL_NAMESPACE = Namespace.getNamespace("xsl", "http://www.w3.org/1999/XSL/Transform");
    String ATT_VERSION = "version";
    String EL_STYLESHEET = "stylesheet";
    String EL_PARAM = "param";

    @Nonnull
    Set<String> findAllNamespaces() throws IOException;
    @Nonnull
    Set<FormatterFunction> findAll() throws IOException;
    @Nonnull
    Set<FormatterFunction> findAllByNamespace(@Nonnull String namespace) throws IOException;
    void save(@Nonnull FormatterFunction newFunction) throws IOException;
    void delete(@Nonnull String namespace, @Nonnull String name) throws IOException;

    /**
     * A comparator for sorting and matching {@link org.fao.geonet.services.metadata.format.function.FormatterFunction}s returned
     * in the sets of this API.
     */
    Comparator<FormatterFunction> COMPARATOR = new Comparator<FormatterFunction>() {

        @Override
        public int compare(FormatterFunction o1, FormatterFunction o2) {
            int namespaceComparison = o1.getNamespace().compareTo(o2.getNamespace());
            if (namespaceComparison != 0) {
                return namespaceComparison;
            }
            return o1.getName().compareTo(o2.getName());
        }
    };
}
