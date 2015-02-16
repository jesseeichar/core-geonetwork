package org.fao.geonet.kernel.search.log;

import jeeves.server.context.ServiceContext;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.statistic.SearchRequestParam;
import org.fao.geonet.utils.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * A class to log Lucene search queries (context, search parameters and search
 * results); this class is built with the GeonetContext object. 
 * If the settings has true for the value: searchStats/enable, log operations 
 * will be performed, otherwise nothing will be done and a notice-level log 
 * will be made
 * 
 * @author nicolas Ribot
 * 
 */
public class SearcherLogger {

	private ServiceContext srvContext;
	private boolean isEnabled;

    /**
     * Constructor.
     *
     * @param srvContext
     * @param logSpatial
     * @param luceneTermsList
     */
	public SearcherLogger(ServiceContext srvContext, boolean logSpatial, String luceneTermsList) {
		this.srvContext = srvContext;


		if (srvContext == null) { // todo: handle exception/errors
			Log.warning(Geonet.SEARCH_LOGGER, "null serviceContext object. will not be able to log queries...");
			this.isEnabled = false;
		}
        else {
			this.isEnabled = true;
		}

        if(Log.isDebugEnabled(Geonet.SEARCH_LOGGER)) {
            Log.debug(Geonet.SEARCH_LOGGER, new StringBuilder().append("SearcherLogger created. Spatial object logging ? ")
                .append(logSpatial).append(". lucene terms to exclude from log: ")
                .append(luceneTermsList).toString());
        }
	}

    /**
     * TODO javadoc.
     *
     * @param query
     * @param numHits
     * @param sort
     * @param geomFilterWKT
     * @param guiService
     */
	public void logSearch(Query query, int numHits, Sort sort, String geomFilterWKT, String guiService) {
		if (!isEnabled) {
			return;
		}
		try{
            if(Log.isDebugEnabled(Geonet.SEARCH_LOGGER))
                Log.debug(Geonet.SEARCH_LOGGER,"Opening dbms...");

    		if (query == null) {
                if(Log.isDebugEnabled(Geonet.SEARCH_LOGGER))
                    Log.debug(Geonet.SEARCH_LOGGER, "Null Query object. cannot log search operation");
    			return;
    		}
    	
    		QueryRequest queryRequest = new QueryRequest(srvContext.getIpAddress(), (new java.util.Date()).getTime());
            List<SearchRequestParam>queryInfos = extractQueryTerms(query);
    		// type is also set when doing this.
    		queryRequest.setQueryInfos(queryInfos);
    		queryRequest.setHits(numHits);
    		queryRequest.setService(this.srvContext.getService());
    		queryRequest.setLanguage(this.srvContext.getLanguage());
    		queryRequest.setLuceneQuery(query.toString());
    		// sortBy, spatial filter ?
    		if (sort != null) queryRequest.setSortBy(concatSortFields(sort.getSort()));
    		// todo: use filter to extract geom from
    		queryRequest.setSpatialFilter(geomFilterWKT);
    		// sets the simple type through this call...
    		queryRequest.isSimpleQuery();
    		queryRequest.setAutoGeneratedQuery(guiService.equals("yes"));
    		
    		if (!queryRequest.storeToDb(srvContext)) {
    			Log.warning(Geonet.SEARCH_LOGGER, "unable to log query into database...");
    		} else {
                if(Log.isDebugEnabled(Geonet.SEARCH_LOGGER))
                    Log.debug(Geonet.SEARCH_LOGGER, "Query saved to database");
    		}
    		
    		// debug only
    		/*
    		for (QueryInfo q : queryInfos) {
    			if (q != null) {
    				System.out.println(q.toString());
    			} else {
    				System.out.println("null queryInfo object");
    			}
    		}
    		*/
		} catch (Exception e) {
            // I dont want the log to cause an exception and hide the real problem.
		    Log.error(Geonet.SEARCH_LOGGER, "Error logging search: "+e.getMessage());
            e.printStackTrace(); //fixme should be removed after control.
		}
	}
	
	/**
     * Returns a dictionary containing field/text for the given query.
	 *  
	 * @param query The query to process to extract terms and 
	 * @return a Hashtable whose key is the field and the value is the text
	 */
	protected List<SearchRequestParam> extractQueryTerms(Query query) {
		if (query == null) {
			return null;
		}
        List<SearchRequestParam> result = new ArrayList<SearchRequestParam>();

		BooleanClause[] clauses;

		if (query instanceof BooleanQuery) {
			BooleanQuery bq = (BooleanQuery) query;
			clauses = bq.getClauses();
			
			for (BooleanClause clause : clauses) {
				result.addAll(extractQueryTerms(clause.getQuery()));
			}
		} else {
            // TODO SOLR
//            Optional<SearchRequestParam> info = LuceneQueryParamType.createRequestParam(query);
//            if (info.isPresent()) {
//                result.add(info.get());
//            } else {
//                Log.warning(Geonet.SEARCH_LOGGER, "unknown queryInfo type: " + query.getClass().getName());
//            }
		}

		return result;
	}
	
	/**
	 * Concatenates the given terms' text into a single String, with the given separator.
	 * 
	 * @param terms the set of terms to concatenate
	 * @param separator the separator to use to separate text elements (use ',' if sep is null)
	 * @return  a string containing all this terms' texts concatenated
	 */
	private String concatTermsText(Term[] terms, String separator) {
		if (terms == null || separator == null) {
            return null;
        }

        StringBuilder sb = new StringBuilder();
		for (Term t : terms) {
			sb.append(t.text()).append(separator);
		}
		if (sb.length() > 0) {
			sb.deleteCharAt(sb.length() - 1);
		}
		return sb.toString();
	}
	
	/**
	 * Concatenates the given terms' fields into a single String, with the given separator.
	 * 
	 * @param terms the set of terms to concatenate
	 * @param separator the separator to use to separate text elements  (use ',' if sep is null)
	 * @return a string containing all this terms' fields concatenated
	 */
	private String concatTermsField(Term[] terms, String separator) {
		if (terms == null || separator == null) return null;

        StringBuilder sb = new StringBuilder();
		for (Term t : terms) {
			sb.append(t.field()).append(separator);
		}
		if (sb.length() > 0) {
			sb.deleteCharAt(sb.length()-1);
		}
		return sb.toString();
	}
	
	/**
	 * Concatenates the given sortFields into a single string of the form.
	 * The concatenation will lead to a string:
	 * <field> ASC|DESC,<field> ASC|DESC,...
	 * where <field> is the sort field name and ASC means ascending sort; DESC means descending sort
	 * (reverse sort)
     *
	 * @param sortFields the array of fields to concatenate
	 * @return a string containing fields concatenated.
	 */
	private String concatSortFields(SortField[] sortFields) {
		if (sortFields == null) {
			return null;
		}
		StringBuilder sb = new StringBuilder();
		for (SortField sf : sortFields) {
			if (sf != null) {
				sb.append(sf.getField()).append(" ").append(sf.getReverse() ? "DESC" : "ASC").append(",");
			}
		}
		if (sb.length() > 0) {
			sb.deleteCharAt(sb.length()-1);
		}
		return sb.toString();
	}
}