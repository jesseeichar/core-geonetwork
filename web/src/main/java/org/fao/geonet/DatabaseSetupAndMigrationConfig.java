package org.fao.geonet;

import java.util.Collections;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.servlet.ServletContext;

import jeeves.resources.dbms.Dbms;
import jeeves.utils.Log;

import org.fao.geonet.constants.Geonet;
import org.fao.geonet.lib.Lib;

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
        String fileType = "file";
        public void setFileType(String fileType) { this.fileType = fileType; }
        public void setPath(String path) { this.path = path; }
        public void setFilePrefix(String filePrefix) { this.filePrefix = filePrefix; }
		@Override
		public String toString() {
			return "DbConfigFile ["
					+ (path != null ? "path=" + path + ", " : "")
					+ (filePrefix != null ? "filePrefix=" + filePrefix : "")
					+ "]";
		}
        
    }
    public static class Version {
        List<DbConfigFile> file;
        int version;
        public void setFile(List<DbConfigFile> file) { this.file = file; }
        public void setVersion(int version) { this.version = version; }
		@Override
		public String toString() {
			return "Version [" + (file != null ? "file=" + file + ", " : "")
					+ "version=" + version + "]";
		}
        
    }
	public void createDatabase(ServletContext servletContext, Dbms dbms,
			String path) throws Exception {
		for(DbConfigFile file : create) {
		    String filePath = path + file.path;
		    Log.info(Geonet.GEONETWORK, "         - SQL create file:" + filePath + " prefix:" + file.filePrefix + " ...");
            // Do we need to remove object before creating the database ?
			Lib.db.removeObjects(servletContext, dbms, path, filePath, file.filePrefix);
			Lib.db.createSchema(servletContext, dbms, path, filePath, file.filePrefix);
		}
		
	}
	public void addData(ServletContext servletContext, Dbms dbms, String path) throws Exception {
        for(DbConfigFile file : data) {
            String filePath = path + file.path;
            Log.info(Geonet.GEONETWORK, "         - SQL data file:" + filePath + " prefix:" + file.filePrefix + " ...");
            Lib.db.insertData(servletContext, dbms, path, filePath, file.filePrefix);
        }
	}
}
