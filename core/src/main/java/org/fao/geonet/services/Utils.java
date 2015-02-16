package org.fao.geonet.services;

import jeeves.server.context.ServiceContext;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.Util;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.constants.Params;
import org.fao.geonet.exceptions.MissingParameterEx;
import org.fao.geonet.kernel.DataManager;
import org.jdom.Element;

import java.io.IOException;

public class Utils {

	/**
	 * Search for a UUID or an internal identifier parameter and return an
	 * internal identifier using default UUID and identifier parameter names
	 * (ie. uuid and id).
	 * 
	 * @param params
	 *            The params to search ids in
	 * @param context
	 *            The service context
	 * @param uuidParamName		UUID parameter name
	 * @param uuidParamName		Id parameter name
	 *  
	 * @return
	 * @throws Exception
	 */
	public static String getIdentifierFromParameters(Element params,
			ServiceContext context, String uuidParamName, String idParamName)
			throws Exception {

		// the metadata ID
		String id;
		GeonetContext gc = (GeonetContext) context
				.getHandlerContext(Geonet.CONTEXT_NAME);
		DataManager dm = gc.getBean(DataManager.class);

		id = lookupByFileId(params,gc);
		if(id==null) {
    		// does the request contain a UUID ?
    		try {
    			String uuid = Util.getParam(params, uuidParamName);
    			// lookup ID by UUID
    			id = dm.getMetadataId(uuid);
    		}
            catch (MissingParameterEx x) {
    			// request does not contain UUID; use ID from request
    			try {
    				id = Util.getParam(params, idParamName);
    			} catch (MissingParameterEx xx) {
    				// request does not contain ID
    				// give up
    				throw new Exception("Request must contain a UUID ("
    						+ uuidParamName + ") or an ID (" + idParamName + ")");
    			}
    		}
		}
		return id;
	}

	private static String lookupByFileId(Element params, GeonetContext gc) throws Exception {
	    String fileId = Util.getParam(params, "fileIdentifier", null);
	    if(fileId == null) {
	        return null;
	    }

	    return lookupMetadataIdFromFileId(gc, fileId);
    }

    public static String lookupMetadataIdFromFileId(GeonetContext gc, String fileId) throws IOException,
            InterruptedException {
//        TODO SOLR
//        TermQuery query = new TermQuery(new Term("fileId", fileId));
//
//        SearchManager searchManager = gc.getBean(SearchManager.class);
//
//        IndexAndTaxonomy indexAndTaxonomy = searchManager.getIndexReader(null, -1);
//		GeonetworkMultiReader reader = indexAndTaxonomy.indexReader;
//
//        try {
//            IndexSearcher searcher = new IndexSearcher(reader);
//            TopDocs tdocs = searcher.search(query, 1);
//
//            if (tdocs.totalHits > 0) {
//
//                Set<String> id = Collections.singleton("_id");
//                Document element = reader.document(tdocs.scoreDocs[0].doc, id);
//                return element.get("_id");
//            }
//
            return null;
//        } finally {
//            searchManager.releaseIndexReader(indexAndTaxonomy);
//        }
    }

    /**
	 * Search for a UUID or an internal identifier parameter and return an
	 * internal identifier using default UUID and identifier parameter names
	 * (ie. uuid and id).
	 *
	 * @param params
	 *            The params to search ids in
	 * @param context
	 *            The service context
	 * @return
	 * @throws Exception
	 */
	public static String getIdentifierFromParameters(Element params,
			ServiceContext context) throws Exception {
		return getIdentifierFromParameters(params, context, Params.UUID, Params.ID);
	}

}
