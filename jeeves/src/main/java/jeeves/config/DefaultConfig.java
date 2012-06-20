package jeeves.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Required;

import jeeves.server.dispatchers.ErrorPage;
import jeeves.server.dispatchers.guiservices.Call;
import jeeves.server.dispatchers.guiservices.GuiService;
import jeeves.server.dispatchers.guiservices.XmlFile;

public class DefaultConfig {
    private String service;
    private String startupErrorService;
    private String language = "eng";
    private String contentType = "text/html";
    private boolean localized = true;
    
    private List<ErrorPage> errorPages;
    private List<GuiService> guiServices;

    public String getService() {
        return service;
    }
    @Required
    public void setService(String service) {
        this.service = service;
    }
    public String getStartupErrorService() {
        return startupErrorService;
    }

    @Required
    public void setStartupErrorService(String startupErrorService) {
        this.startupErrorService = startupErrorService;
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
    public void setGuiServices(List<GuiService> guiServices) {
		this.guiServices = guiServices;
	}
    public void setCall(List<Call> calls) {
        this.guiServices.addAll(calls);
    }
    public void setXml(List<XmlFile> xml) {
        this.guiServices.addAll(xml);
    }
}