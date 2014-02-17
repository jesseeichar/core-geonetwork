package org.fao.geonet.kernel.search.index;

import org.apache.lucene.facet.taxonomy.TaxonomyWriter;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.TrackingIndexWriter;

import java.io.IOException;

public interface Function {
    public void apply(TaxonomyWriter taxonomyWriter, TrackingIndexWriter indexWriter) throws CorruptIndexException, IOException;
}
