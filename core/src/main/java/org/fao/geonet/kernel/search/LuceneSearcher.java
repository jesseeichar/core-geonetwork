package org.fao.geonet.kernel.search;

import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;
import org.jdom.Document;
import org.jdom.Element;

import java.util.List;

/**
 * @author Jesse on 2/16/2015.
 */
public class LuceneSearcher extends MetaSearcher {
    public static String getMetadataFromIndex(String language, String metadataUuid, String fieldname) {
        return null; // TODO SOLR
    }

    public static String getMetadataFromIndexById(String language, String metadataId, String fieldname) {
        return null;// TODO SOLR
    }

    @Override
    public void search(ServiceContext srvContext, Element request, ServiceConfig config) throws Exception {
        // TODO SOLR
    }

    @Override
    public Element present(ServiceContext srvContext, Element request, ServiceConfig config) throws Exception {
        return null;            // TODO SOLR
    }

    @Override
    public List<Document> presentDocuments(ServiceContext srvContext, Element request, ServiceConfig config) throws Exception {
        return null;// TODO SOLR
    }

    @Override
    public int getSize() {
        return 0;// TODO SOLR
    }

    @Override
    public Element getSummary() throws Exception {
        return null;// TODO SOLR
    }

    @Override
    public void close() {
        // TODO SOLR
    }
}
