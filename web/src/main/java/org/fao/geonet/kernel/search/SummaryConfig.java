package org.fao.geonet.kernel.search;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import jeeves.utils.Log;
import jeeves.utils.Xml;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.FieldSelector;
import org.apache.lucene.document.FieldSelectorResult;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.ScoreDoc;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.kernel.search.SummaryComparator.SortOption;
import org.fao.geonet.kernel.search.SummaryComparator.Type;
import org.jdom.Element;
import org.springframework.beans.factory.BeanInitializationException;

public class SummaryConfig {
    Map<String, Map<String, Item>> configurations = new HashMap<String, Map<String, Item>>();
    Element typeConf;

    public void setConfigurations(Map<String, List<Item>> configurations) {
        for (Map.Entry<String, List<Item>> entry : configurations.entrySet()) {
            Map<String, Item> map = new HashMap<String, Item>();
            for (Item item : map.values()) {
                map.put(item.name, item);
            }
            this.configurations.put(entry.getKey(), map);
        }
    }
    public void setTypeConf(String typeConf) {
        try {
            this.typeConf = Xml.loadString(typeConf, false);
        } catch (Exception e) {
            throw new BeanInitializationException("The typeConf property of could not be parsed as XML");
        }
    }

    public static class Item {
        String name;
        String plural;
        String indexKey;
        String order;
        String type;
        int max;
        public void setName(String name) { this.name = name; }
        public void setPlural(String plural) { this.plural = plural; }
        public void setIndexKey(String indexKey) { this.indexKey = indexKey; }
        public void setOrder(String order) { this.order = order; }
        public void setType(String type) { this.type = type; }
        public void setMax(int max) { this.max = max; }
    }

    public Element buildSummaryMaps(Element elSummary, String resultType, String langCode, IndexReader reader,
            ScoreDoc[] scoreDocs) throws Exception {
        elSummary.setAttribute("hitsusedforsummary", scoreDocs.length + "");
        final Map<String, Map<String, Integer>> summaryMaps = prepareSummaryMaps(resultType);
        FieldSelector keySelector = new FieldSelector() {
            private static final long serialVersionUID = 1L;
            public final FieldSelectorResult accept(String name) {
                if (summaryMaps.get(name) != null)
                    return FieldSelectorResult.LOAD;
                else
                    return FieldSelectorResult.NO_LOAD;
            }
        };

        for (ScoreDoc sdoc : scoreDocs) {
            Document doc = null;
            try {
                doc = reader.document(sdoc.doc, keySelector);
            } catch (Exception e) {
                Log.error(Geonet.SEARCH_ENGINE, e.getMessage() + " Caused Failure to get document " + sdoc.doc);
                e.printStackTrace();
            }

            for (String key : summaryMaps.keySet()) {
                Map<String, Integer> summary = summaryMaps.get(key);
                String hits[] = doc.getValues(key);
                if (hits != null) {
                    for (String info : hits) {
                        Integer catCount = summary.get(info);
                        if (catCount == null) {
                            catCount = 1;
                        } else {
                            catCount = catCount + 1;
                        }
                        summary.put(info, catCount);
                    }
                }
            }
        }

        return addSortedSummaryKeys(elSummary, langCode, summaryMaps, resultType);
    }

    private Map<String, Map<String, Integer>> prepareSummaryMaps(String resultType) throws Exception {
        Map<String, Map<String, Integer>> summaryMaps = new HashMap<String, Map<String, Integer>>();
        Map<String, Item> map = configurations.get(resultType);
        if(map != null) {
			for (String key : map.keySet()) {
	            summaryMaps.put(key, new HashMap<String, Integer>());
	        }
        }
        return summaryMaps;
    }

    private SummaryComparator getSummaryComparator(String langCode, Item item) throws Exception {
        SortOption sortOption = SortOption.parse(item.order);
        
        return new SummaryComparator(sortOption, Type.parse(item.type), langCode, typeConf);
    }

    private Element addSortedSummaryKeys(Element elSummary, String langCode, Map<String, Map<String, Integer>> summaryMaps,
            String resultType) throws Exception {

        for (String indexKey : summaryMaps.keySet()) {
            Item item = configurations.get(resultType).get(indexKey);
            Element rootElem = new Element(item.plural);
            // sort according to frequency
            SummaryComparator summaryComparator = getSummaryComparator(langCode, item);
            Map<String, Integer> summary = summaryMaps.get(indexKey);
            if (Log.isDebugEnabled(Geonet.SEARCH_ENGINE))
                Log.debug(Geonet.SEARCH_ENGINE, "Sorting " + summary.size() + " according to frequency of " + indexKey);

            TreeSet<Map.Entry<String, Integer>> sortedSummary = new TreeSet<Map.Entry<String, Integer>>(summaryComparator);
            sortedSummary.addAll(summary.entrySet());

            int nKeys = 0;
            for (Object aSortedSummary : sortedSummary) {
                if (++nKeys > item.max) {
                    break;
                }

                @SuppressWarnings("rawtypes")
                Map.Entry me = (Map.Entry) aSortedSummary;
                String keyword = (String) me.getKey();
                Integer keyCount = (Integer) me.getValue();

                Element childElem = new Element(item.name);
                childElem.setAttribute("count", keyCount.toString());
                childElem.setAttribute("name", keyword);
                childElem.setAttribute("indexKey", indexKey);

                rootElem.addContent(childElem);
            }
            elSummary.addContent(rootElem);
        }

        return elSummary;
    }
}
