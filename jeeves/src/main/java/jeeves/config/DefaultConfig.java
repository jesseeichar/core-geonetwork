package jeeves.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Required;

import jeeves.server.dispatchers.ErrorPage;

public class DefaultConfig {
    String service;
    String startupErrorSrv;
    String language = "eng";
    String contentType = "text/html";
    boolean localized = true;
    
    List<ErrorPage> errorPages;

    public String getService() {
        return service;
    }
    @Required
    public void setService(String service) {
        this.service = service;
    }

    public String getStartupErrorSrv() {
        return startupErrorSrv;
    }

    public void setStartupErrorSrv(String startupErrorSrv) {
        this.startupErrorSrv = startupErrorSrv;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public boolean isLocalized() {
        return localized;
    }

    public void setLocalized(boolean localized) {
        this.localized = localized;
    }

    public List<ErrorPage> getErrorPages() {
        return errorPages;
    }
    @Required
    public void setErrorPages(List<ErrorPage> errorPages) {
        this.errorPages = errorPages;
    }
    
    
}