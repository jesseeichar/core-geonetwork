package org.fao.geonet.kernel.search;

import java.util.Set;

/**
 * An analyzer that takes into account multiple languages and therefore
 * needs to have certain properties set.
 * 
 * @author jeichar
 */
public interface LocalizedAnalyzer {
    public void setStopwords(Set<String> stopwords);
}
