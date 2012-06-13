package org.fao.geonet.kernel.search;

import java.util.List;

public class SummaryConfig {
    List<Item> hits;
    List<Item> titles;
    List<Item> results;
    List<Item> results_with_summary;

    public List<Item> getHits() { return hits; }
    public void setHits(List<Item> hits) { this.hits = hits; }
    public List<Item> getTitles() { return titles; }
    public void setTitles(List<Item> titles) { this.titles = titles; }
    public List<Item> getResults() { return results; }
    public void setResults(List<Item> results) { this.results = results; }
    public List<Item> getResults_with_summary() { return results_with_summary; }
    public void setResults_with_summary(List<Item> results_with_summary) { this.results_with_summary = results_with_summary;}

    public static class Item {
        String name;
        String plural;
        String indexKey;
        String order;
        String type;
        int max;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getPlural() { return plural; }
        public void setPlural(String plural) { this.plural = plural; }
        public String getIndexKey() { return indexKey; }
        public void setIndexKey(String indexKey) { this.indexKey = indexKey; }
        public String getOrder() { return order; }
        public void setOrder(String order) { this.order = order; }
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public int getMax() { return max; }
        public void setMax(int max) { this.max = max; }
    }
}
