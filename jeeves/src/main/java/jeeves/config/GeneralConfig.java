package jeeves.config;

import org.springframework.beans.factory.annotation.Required;


public class GeneralConfig {
    String uploadDir;
    int maxUploadSize = 50;
    boolean debug = true;
    
    public String getUploadDir() {
        return uploadDir;
    }
    @Required
    public void setUploadDir(String uploadDir) {
        this.uploadDir = uploadDir;
    }
    public int getMaxUploadSize() {
        return maxUploadSize;
    }
    public void setMaxUploadSize(int maxUploadSize) {
        this.maxUploadSize = maxUploadSize;
    }
    public boolean isDebug() {
        return debug;
    }
    public void setDebug(boolean debug) {
        this.debug = debug;
    }
}
