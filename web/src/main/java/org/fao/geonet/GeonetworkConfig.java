package org.fao.geonet;

import jeeves.config.EnvironmentalConfig;

import org.fao.geonet.kernel.csw.CswCatalogConfig;
import org.fao.geonet.kernel.search.LuceneConfig;
import org.fao.geonet.kernel.search.SummaryConfig;
import org.fao.geonet.kernel.search.spatial.SpatialIndexWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.context.Lifecycle;

public class GeonetworkConfig implements Lifecycle {
    // ------------------------------------------------------------------------------
    // -- Injected by spring
    // ------------------------------------------------------------------------------
    private String languageProfilesDir;
    private String licenseDir;
    private String preferredSchema;
    private boolean statLogSpatialObjects;
    private boolean statLogAsynch;
    private String statLuceneTermsExclude = "";
    private int maxWritesInTransaction = SpatialIndexWriter.MAX_WRITES_IN_TRANSACTION;
    private boolean useSubversion;
    private String statusActionsClassName;
    
    private EnvironmentalConfig envConfig;
    private GeonetworkDataDirectory dataDirectories;
    private LuceneConfig luceneConfig;
    private SummaryConfig summaryConfig;
    private CswCatalogConfig cswCatalogConfig;
    // ------------------------------------------------------------------------------
    
    private boolean started;
    
    @Autowired
    public GeonetworkConfig(
            EnvironmentalConfig envConfig, 
            GeonetworkDataDirectory dataDirectories,
            LuceneConfig luceneConfig,
            SummaryConfig summaryConfig,
            CswCatalogConfig cswCatalogConfig) {
        this.envConfig = envConfig;
        this.dataDirectories = dataDirectories;
        this.luceneConfig = luceneConfig;
        this.summaryConfig = summaryConfig;
        this.cswCatalogConfig = cswCatalogConfig;
    }
    
    public String getLanguageProfilesDir() { return languageProfilesDir;}
    @Required
    public void setLanguageProfilesDir(String languageProfilesDir) {this.languageProfilesDir = languageProfilesDir;}
    
    public String getLicenseDir() {return licenseDir;}
    @Required
    public void setLicenseDir(String licenseDir) {this.licenseDir = licenseDir;}
    
    public String getPreferredSchema() {return preferredSchema;}
    @Required
    public void setPreferredSchema(String preferredSchema) {this.preferredSchema = preferredSchema;}

    public boolean isStatLogSpatialObjects() {return statLogSpatialObjects;}
    @Required
    public void setStatLogSpatialObjects(boolean statLogSpatialObjects) {this.statLogSpatialObjects = statLogSpatialObjects;}
    
    public boolean isStatLogAsynch() {return statLogAsynch;}
    @Required
    public void setStatLogAsynch(boolean statLogAsynch) {this.statLogAsynch = statLogAsynch;}
    
    public String getStatLuceneTermsExclude() {return statLuceneTermsExclude;}
    @Required
    public void setStatLuceneTermsExclude(String statLuceneTermsExclude) { this.statLuceneTermsExclude = statLuceneTermsExclude; }
    
    public int getMaxWritesInTransaction() { return maxWritesInTransaction; }
    @Required
    public void setMaxWritesInTransaction(int maxWritesInTransaction) { this.maxWritesInTransaction = maxWritesInTransaction; }
    
    public boolean isUseSubversion() { return useSubversion; }
    @Required
    public void setUseSubversion(boolean useSubversion) { this.useSubversion = useSubversion; }
    
    public String getStatusActionsClassName() { return statusActionsClassName; }
    @Required
    public void setStatusActionsClassName(String statusActionsClassName) { this.statusActionsClassName = statusActionsClassName; }
    
    public EnvironmentalConfig getEnvConfig() { return envConfig; }
    public GeonetworkDataDirectory getDataDirectories() { return dataDirectories; }
    public LuceneConfig getLuceneConfig() { return luceneConfig; }
    public SummaryConfig getSummaryConfig() { return summaryConfig;}
    public CswCatalogConfig getCswCatalogConfig() { return cswCatalogConfig; }

    @Override
    public void start() {
        this.started = true;
    }

    @Override
    public void stop() {
        this.started = false;
        
    }

    @Override
    public boolean isRunning() {
        return started;
    }
}
