package org.fao.geonet.kernel.csw;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.fao.geonet.csw.common.Csw;
import org.jdom.Namespace;
import org.springframework.beans.factory.annotation.Required;

public class CswCatalogConfig {
    private GetCapabilities capabilities;
    private GetDomain domain;
    private GetRecords records;
    private DescribeRecord describeRecord;
    
    public GetCapabilities getCapabilities() { return capabilities; }
    public void setCapabilities(GetCapabilities capabilities) { this.capabilities = capabilities; }
    public GetDomain getDomain() { return domain; }
    public void setDomain(GetDomain domain) { this.domain = domain; }
    public GetRecords getRecords() { return records; }
    public void setRecords(GetRecords records) { this.records = records; }
    public DescribeRecord getDescribeRecord() { return describeRecord; }
    public void setDescribeRecord(DescribeRecord describeRecord) { this.describeRecord = describeRecord; }
    
    public static class GetCapabilities {
        private int numberOfKeywords = 10;
        private int maxNumberOfRecordsForKeywords = Integer.MAX_VALUE;
        
        public int getNumberOfKeywords() { return numberOfKeywords; }
        public void setNumberOfKeywords(int numberOfKeywords) { this.numberOfKeywords = numberOfKeywords; }
        
        public int getMaxNumberOfRecordsForKeywords() { return maxNumberOfRecordsForKeywords; }
        public void setMaxNumberOfRecordsForKeywords(int maxNumberOfRecordsForKeywords) { this.maxNumberOfRecordsForKeywords = maxNumberOfRecordsForKeywords; }
    }

    public static class GetDomain {
        private int maxNumberOfRecordsForPropertyNames = Integer.MAX_VALUE;

        public int getMaxNumberOfRecordsForPropertyNames() { return maxNumberOfRecordsForPropertyNames; }
        public void setMaxNumberOfRecordsForPropertyNames(int maxNumberOfRecordsForPropertyNames) { this.maxNumberOfRecordsForPropertyNames = maxNumberOfRecordsForPropertyNames;}
    }

    public static class GetRecords {
        private Map<String, String> fieldMapping = new HashMap<String, String>();
        private Map<String, Map<String, String>> fieldMappingXPath = new HashMap<String, Map<String, String>>();
        private Set<String> isoQueryables = new HashSet<String>();
        private Set<String> additionalQueryables = new HashSet<String>();
        private Set<String> constraintLanguage = new HashSet<String>();
        private Set<String> outputFormat = new HashSet<String>();
        private Set<String> getRecordsOutputSchema = new HashSet<String>();
        private Set<String> getRecordsTypenames = new HashSet<String>();
        private Set<String> getRecordsRangeFields = new HashSet<String>();
        
        public Set<String> getTypeMapping(String type) {
            if (type.equals(Csw.ISO_QUERYABLES))
                return isoQueryables;
            else
                return additionalQueryables;
        }
        public Map<String, String> getFieldMapping() { return fieldMapping; }
        public Map<String, Map<String, String>> getFieldMappingXPath() { return fieldMappingXPath; }
        public Set<String> getIsoQueryables() { return isoQueryables; }
        public Set<String> getAdditionalQueryables() { return additionalQueryables; }
        public Set<String> getGetRecordsConstraintLanguage() { return constraintLanguage; }
        public Set<String> getGetRecordsOutputFormat() { return outputFormat; }
        public Set<String> getGetRecordsOutputSchema() { return getRecordsOutputSchema; }
        public Set<String> getGetRecordsTypenames() { return getRecordsTypenames; }
        public Set<String> getGetRecordsRangeFields() { return getRecordsRangeFields; }
        public void setOutputFormats(Set<String> outputFormat) { this.outputFormat = outputFormat;}
        public void setConstraintLanguages(Set<String> constraintLanguage) { this.constraintLanguage = constraintLanguage; }
        public void setTypeNames(Set<TypeName> typeNames) {
            for (TypeName typeName : typeNames) {
                getRecordsOutputSchema.add(typeName.namespace);
                getRecordsTypenames.add(typeName.prefix + ":" + typeName.name);
            }
        }
        
        public void setParameters(List<GetRecordParameter> params) {
            for (GetRecordParameter param : params) {
                fieldMapping.put(param.name.toLowerCase(), param.field);
                if (param.range) {
                    getRecordsRangeFields.add(param.field);
                }
                if (param.type.equals(Csw.ISO_QUERYABLES)) {
                    isoQueryables.add(param.name);
                } else {
                    additionalQueryables.add(param.name);
                }
                
                Map<String, String> xpathMap = new HashMap<String, String>();
                for (XPath xpath : param.xpaths) {
                    xpathMap.put(xpath.schema, xpath.path);
                }
                fieldMappingXPath.put(param.name.toLowerCase(), xpathMap);
            }
        }

    }
    public static class GetRecordParameter {
        String name;
        String field;
        String type = Csw.ISO_QUERYABLES;
        boolean range = false;
        List<XPath> xpaths = Collections.emptyList();
        
        @Required
        public void setName(String name) { this.name = name; }
        @Required
        public void setField(String field) { this.field = field; }
        public void setType(String type) { this.type = type; }
        public void setRange(boolean range) { this.range = range; }
        public void setXpaths(List<XPath> xpaths) { this.xpaths = xpaths; }
        /**
         * Only can be used is {@link #setXpaths(List)} is NOT used.
         * It is a short cut for defining a XPath and setting the schema.  
         * To be used in conjunction with {@link #setXpathPath(String)}
         */
        public void setXpathSchema(String schema) {
            XPath xpath;
            
            if(xpaths == null) {
                xpath = new XPath();
                xpaths = Collections.singletonList(xpath);
            } else {
                xpath = xpaths.get(0);
            }
            xpath.setSchema(schema);
        }
        /**
         * Only can be used is {@link #setXpaths(List)} is NOT used.
         * It is a short cut for defining a XPath and setting the path.  
         * To be used in conjunction with {@link #setXpathSchema(String)}
         */
        public void setXpathPath(String path) { 
            XPath xpath;
            
            if(xpaths == null) {
                xpath = new XPath();
                xpaths = Collections.singletonList(xpath);
            } else {
                xpath = xpaths.get(0);
            }
            xpath.setPath(path);
        }
        
    }
    public static class XPath {
        String path;
        String schema;
        public void setPath(String path) { this.path = path; }
        public void setSchema(String schema) { this.schema = schema; }
    }
    public static class DescribeRecord {
        private HashMap<String, String> typenames = new HashMap<String, String>();
        private Set<Namespace> namespaces = new HashSet<Namespace>();
        private Set<String> outputFormats = new HashSet<String>();
        
        public HashMap<String, String> getDescribeRecordTypenames() { return typenames; }
        public Set<Namespace> getDescribeRecordNamespaces() { return namespaces; }
        public Set<String> getDescribeRecordOutputFormat() { return outputFormats; }
        public void setOutputFormats(Set<String> describeRecordOutputFormat) {
            this.outputFormats = describeRecordOutputFormat;
        }
        public void setTypeNames(List<TypeName> typeNames) {
            for (TypeName typeName : typeNames) {
                typenames.put(typeName.prefix+":"+typeName.name, typeName.schema);
                namespaces.add(Namespace.getNamespace(typeName.prefix, typeName.namespace));
            }
        }
    }
    public static class TypeName {
        private String namespace;
        private String prefix;
        private String schema;
        private String name;

        public String getNamespace() { return namespace; }
        public void setNamespace(String namespace) { this.namespace = namespace; }
        public String getPrefix() { return prefix; }
        public void setPrefix(String prefix) { this.prefix = prefix; }
        public String getSchema() { return schema; }
        public void setSchema(String schema) { this.schema = schema; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
    }
}
