package org.fao.geonet;

import java.util.Collections;
import java.util.List;

import javax.annotation.PostConstruct;

public class DatabaseSetupAndMigrationConfig {
    List<DbConfigFile> create = Collections.emptyList();
    List<DbConfigFile> data = Collections.emptyList();
    List<Version> migrate = Collections.emptyList();
    private String basedir = "";
    
    public void setBasedir(String basedir) { this.basedir = basedir; }
    public void setCreate(List<DbConfigFile> create) { this.create = create; }
    public void setData(List<DbConfigFile> data) { this.data = data; }
    public void setMigrate(List<Version> migrate) { this.migrate = migrate; }
    
    @PostConstruct
    public void init() {
        updatePaths(create);
        updatePaths(data);
        for (Version version : migrate) {
            updatePaths(version.file);
        }
    }
    private void updatePaths(List<DbConfigFile> list) {
        for (DbConfigFile file : list) {
            file.path = basedir + file.path;
        }
    }
    public static class DbConfigFile {
        String path;
        String filePrefix;
        public void setPath(String path) { this.path = path; }
        public void setFilePrefix(String filePrefix) { this.filePrefix = filePrefix; }
    }
    public static class Version {
        List<DbConfigFile> file;
        int version;
        public void setFile(List<DbConfigFile> file) { this.file = file; }
        public void setVersion(int version) { this.version = version; }
        
    }
}
