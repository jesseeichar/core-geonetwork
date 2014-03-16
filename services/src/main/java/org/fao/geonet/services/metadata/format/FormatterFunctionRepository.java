package org.fao.geonet.services.metadata.format;

import org.jdom.Namespace;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.List;

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
    List<String> findAllNamespaces() throws IOException;
    @Nonnull
    List<FormatterFunction> findAll() throws IOException;
    @Nonnull
    List<FormatterFunction> findAllByNamespace(@Nonnull String namespace) throws IOException;
    void save(@Nonnull FormatterFunction newFunction) throws IOException;
    void delete(@Nonnull String namespace, @Nonnull String name) throws IOException;
}
