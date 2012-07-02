package org.fao.geonet.kernel.search;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import jeeves.config.EnvironmentalConfig;
import jeeves.config.springutil.ReloadableBean;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.util.NumericUtils;
import org.fao.geonet.kernel.search.function.DocumentBoosting;
import org.fao.geonet.kernel.search.function.QueryBoostFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Required;

public class LuceneConfig extends ReloadableBean {

    private Index index;
    private Search search;
    private Analyzer defaultAnalyzer = new GeoNetworkAnalyzer();
    private Map<String, Analyzer> fieldSpecificAnalyzer = Collections.emptyMap();
    private Map<String, Analyzer> fieldSpecificSearchAnalyzer = Collections.emptyMap();
    private Map<String, Float> fieldBoosting = Collections.emptyMap();
    private DocumentBoosting documentBoosting;
    private Set<String> tokenizedFields = Collections.emptySet();
    private Map<String, NumericField> numericFields = Collections.emptyMap();
    
    @Autowired
    public LuceneConfig(EnvironmentalConfig config) {
        super(new File(config.getConfigPath(), "config-lucene.xml"));
    }
    
    @Required
    @Autowired
    public void setIndex(Index index) { this.index = index; }
    @Required
    @Autowired
    public void setSearch(Search search) { this.search = search; }
    
    public Analyzer getDefaultAnalyzer() { return defaultAnalyzer; }
    public void setDefaultAnalyzer(Analyzer defaultAnalyzer) { this.defaultAnalyzer = defaultAnalyzer; }
    
    public Map<String, Analyzer> getFieldSpecificAnalyzer() { return fieldSpecificAnalyzer; }
    public void setFieldSpecificAnalyzer(Map<String, Analyzer> fieldSpecificAnalyzer) { this.fieldSpecificAnalyzer = fieldSpecificAnalyzer; }
    
    public Map<String, Analyzer> getFieldSpecificSearchAnalyzer() { return fieldSpecificSearchAnalyzer; }
    public void setFieldSpecificSearchAnalyzer(Map<String, Analyzer> fieldSpecificSearchAnalyzer) { this.fieldSpecificSearchAnalyzer = fieldSpecificSearchAnalyzer; }
    
    public Map<String, Float> getFieldBoosting() { return fieldBoosting; }
    public void setFieldBoosting(Map<String, Float> fieldBoosting) { this.fieldBoosting = fieldBoosting; }
    
    public DocumentBoosting getDocumentBoosting() { return documentBoosting; }
    public void setBoostDocument(DocumentBoosting boostDocument) { this.documentBoosting = boostDocument; }
    
    public Set<String> getTokenizedFields() { return tokenizedFields; }
    public void setTokenizedFields(Set<String> tokenized) { this.tokenizedFields = tokenized; }
    
    public float getRamBufferSizeMB() { return index.ramBufferSizeMB; }
    public int getMergeFactor() { return index.mergeFactor; }
    public int getLuceneVersion() { return index.luceneVersion; }
    public boolean isTrackDocScores() { return search.trackDocScores; }
    public boolean isTrackMaxScore() { return search.trackMaxScore; }
    public boolean isDocsScoredInOrder() {return search.docsScoredInOrder;}
    public QueryBoostFactory getQueryBoost() { return search.queryBoost; }
    public Map<String, String> getDumpFields() { return search.dumpFields;}

    public Map<String, NumericField> getNumericFieldsMap() { return numericFields; }
    public void setNumericFields(Set<NumericField> numerics) { 
        numericFields = new HashMap<String,NumericField>();
        for (NumericField numeric : numerics) {
            numericFields.put(numeric.getName(), numeric);
        }
    }
    public boolean isTokenizedField(String fieldName) {
        return tokenizedFields.contains(fieldName);
    }
    public boolean isNumericField(String fieldName) {
        return numericFields.containsKey(fieldName);
    }


    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Lucene configuration:\n");
        sb.append(" * Version: " + getLuceneVersion() + "\n");
        sb.append(" * RAMBufferSize: " + getRamBufferSizeMB() + "\n");
        sb.append(" * MergeFactor: " + getMergeFactor() + "\n");
        sb.append(" * Default analyzer: " + classString(getDefaultAnalyzer()) + "\n");
        sb.append(" * Field analyzers: "
                + getFieldSpecificAnalyzer() + "\n");
        sb.append(" * Field search analyzers: "
                + getFieldSpecificSearchAnalyzer() + "\n");
        sb.append(" * Field boost factor: "
                + getFieldBoosting().toString() + "\n");
        sb.append(" * Boost document class: " + classString(getDocumentBoosting()) + "\n");
        sb.append(" * Tokenized fields: " + getTokenizedFields()
                + "\n");
        sb.append(" * Numeric fields: "
                + getNumericFieldsMap().keySet().toString() + "\n");
        sb.append(" * Dump fields: " + getDumpFields()
                + "\n");
        sb.append(" * Search boost query: " + classString(getQueryBoost()) + "\n");
        sb.append(" * Score: \n");
        sb.append("  * trackDocScores: " + isTrackDocScores() + " \n");
        sb.append("  * trackMaxScore: " + isTrackMaxScore() + " \n");
        sb.append("  * docsScoredInOrder: " + isDocsScoredInOrder() + " \n");
        return sb.toString();
    }

    private String classString(Object obj) {
        return obj == null ? "none" : obj.getClass().getName();
    }

    public static class Index {
        private float ramBufferSizeMB;
        private int mergeFactor;
        private int luceneVersion;
        @Required
        public void setRamBufferSizeMB(float ramBufferSizeMB) { this.ramBufferSizeMB = ramBufferSizeMB; }
        @Required
        public void setMergeFactor(int mergeFactor) { this.mergeFactor = mergeFactor; }
        @Required
        public void setLuceneVersion(int luceneVersion) { this.luceneVersion = luceneVersion; }
        
    }
    public static class Search {
        private boolean trackDocScores;
        private boolean trackMaxScore;
        private boolean docsScoredInOrder;
        private QueryBoostFactory queryBoost;
        private Map<String,String> dumpFields = Collections.emptyMap();
        
        @Required
        public void setTrackDocScores(boolean trackDocScores) { this.trackDocScores = trackDocScores; }
        @Required
        public void setTrackMaxScore(boolean trackMaxScore) { this.trackMaxScore = trackMaxScore; }
        @Required
        public void setDocsScoredInOrder(boolean docsScoredInOrder) { this.docsScoredInOrder = docsScoredInOrder; }
        public void setQueryBoost(QueryBoostFactory queryBoost) { this.queryBoost = queryBoost; }
        public void setDumpFields(Map<String, String> dumpFields) { this.dumpFields = dumpFields; }
    }
    public static class NumericField {
        public String name;
        public String type = "int";
        private int precisionStep = NumericUtils.PRECISION_STEP_DEFAULT;

        public String getName() { return name; }
        @Required
        public void setName(String name) { this.name = name; }
        
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public int getPrecisionStep() { return precisionStep; }
        public void setPrecisionStep(int precisionStep) { this.precisionStep = precisionStep; }
        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((name == null) ? 0 : name.hashCode());
            result = prime * result + precisionStep;
            result = prime * result + ((type == null) ? 0 : type.hashCode());
            return result;
        }
        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            NumericField other = (NumericField) obj;
            if (name == null) {
                if (other.name != null)
                    return false;
            } else if (!name.equals(other.name))
                return false;
            if (precisionStep != other.precisionStep)
                return false;
            if (type == null) {
                if (other.type != null)
                    return false;
            } else if (!type.equals(other.type))
                return false;
            return true;
        }
        
    }
}
