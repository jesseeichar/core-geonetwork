package org.fao.geonet.kernel.search.function;

import org.apache.lucene.search.Query;

public interface QueryBoostFactory {
    public Query createBoost(Query query) throws Exception;
}
