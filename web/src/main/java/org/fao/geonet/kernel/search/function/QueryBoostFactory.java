package org.fao.geonet.kernel.search.function;

import org.apache.lucene.search.Query;
import org.apache.lucene.search.function.CustomScoreQuery;

public interface QueryBoostFactory {
    public CustomScoreQuery createBoost(Query query);
}
