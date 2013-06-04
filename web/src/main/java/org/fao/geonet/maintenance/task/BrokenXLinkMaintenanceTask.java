package org.fao.geonet.maintenance.task;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import jeeves.resources.dbms.Dbms;
import jeeves.server.context.ServiceContext;

import org.apache.commons.httpclient.Cookie;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpState;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.methods.GetMethod;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.kernel.setting.SettingInfo;
import org.fao.geonet.lib.Lib;
import org.fao.geonet.maintenance.MaintenanceIssueDescription;
import org.fao.geonet.maintenance.MaintenanceReport;
import org.fao.geonet.maintenance.MaintenanceReport.Category;
import org.fao.geonet.maintenance.MaintenanceReport.Severity;
import org.fao.geonet.maintenance.MaintenanceReporting;
import org.fao.geonet.maintenance.MaintenanceTask;
import org.jdom.Content;
import org.jdom.Element;
import org.jdom.Text;
import org.springframework.stereotype.Component;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

@Component
public class BrokenXLinkMaintenanceTask implements MaintenanceTask {
    private static final class ElementToIdTransformFunction implements Function<Element, String> {
        @Override
        @Nullable
        public String apply(@Nonnull Element input) {
            return input.getChildText("id");
        }
    }

    private static Pattern LINK_PATTERN = Pattern.compile("(https://|http://)[^\\s<>]*\\w");

    private static final int LOAD_IDS = 0;
    private static final int LOAD_METADATA = 1;
    private static final int FIND_XLINKS = 2;
    private static final int TEST_XLINKS = 3;
    private List<String> _ids;
    private String _currentMetadataId;
    private Element _metadata;

    private Map<String, String> _links = new HashMap<String, String>();

    @Override
    public int performTaskStep(int state, ServiceContext serviceContext, @Nonnull MaintenanceReporting reporting) throws 
            Exception {
        switch (state) {
        case LOAD_IDS:
            loadIds(serviceContext);
            return LOAD_METADATA;
        case LOAD_METADATA:
            loadMetadata(serviceContext);
            return FIND_XLINKS;
        case FIND_XLINKS:
            findXLinks(serviceContext, reporting);
            return TEST_XLINKS;
        case TEST_XLINKS:
            return testXLinks(serviceContext, reporting);
        default:
            return 0;
        }
    }

    private int testXLinks(ServiceContext serviceContext, MaintenanceReporting reporting) throws MalformedURLException, InterruptedException {
        String url = _links.keySet().iterator().next();
        String xpath = _links.remove(url);
        try {
            new URL (url);
        } catch (MalformedURLException e) {
            String errorDescription = "Found some text cases where there appears to be a URL but an error occurs when creating the URL: "+xpath;
            String errorName = "String that appears to contain a URL is invalid";
            reportError(serviceContext, errorName, errorDescription, url, xpath, reporting);
        }
        try {
            HttpClient client = new HttpClient();
            Lib.net.setupProxy(serviceContext, client);

            GetMethod method = new GetMethod(url);
            Cookie cookie = new Cookie();
            HttpState state = new HttpState();
            state.addCookie(cookie);
            client.setState(state);
            client.getParams().setCookiePolicy(CookiePolicy.BROWSER_COMPATIBILITY);
            int responseCode = client.executeMethod(method);
            
            if (responseCode == 400 || responseCode > 403) {
                reportError(serviceContext, "Response Code was "+responseCode, url, xpath, reporting);
            }
        } catch (MalformedURLException e) {
            throw e;
        } catch (Exception e) {
            reportError(serviceContext, e.getMessage(), url, xpath, reporting);
        }
        return 0;
    }

    private void reportError(ServiceContext context, String errorName, String url, String xpath, MaintenanceReporting reporting) throws MalformedURLException, InterruptedException {
        String errorDesc = "A url cannot be accessed in metadata "+_currentMetadataId+" xpath:"+xpath+". Error is: "+errorName;
        reportError(context, errorName, errorDesc , url, xpath, reporting);
    }

    private void reportError(ServiceContext context, String errorName, String errorDesc, String url, String xpath, MaintenanceReporting reporting) throws MalformedURLException, InterruptedException {
        MaintenanceIssueDescription description = new MaintenanceIssueDescription("eng", errorName, errorDesc);
        MaintenanceReport report = new MaintenanceReport(this,_currentMetadataId+":"+xpath, Category.METADATA_ERROR, Severity.TRIVIAL, description);
        SettingInfo info = new SettingInfo(context);
        String rootURL = info.getSiteUrl();
        URL fixLink = new URL(rootURL+context.getAppPath()+"/srv/"+context.getLanguage()+"/metadata.edit?id="+_currentMetadataId);
        report.setFixLinks(fixLink);
        reporting.report(report);
    }

    private void findXLinks(ServiceContext serviceContext, MaintenanceReporting reporting) {
        @SuppressWarnings("unchecked")
        Iterator<Content> allContent = _metadata.getDescendants();
        
        while (allContent.hasNext()) {
            Content content = allContent.next();
            if (content != null && content instanceof Text) {
                String text = ((Text) content).getText();
                Matcher matcher = LINK_PATTERN.matcher(text);
                while (matcher.find()) {
                    String urlText = matcher.group();
                    StringBuilder xpath = new StringBuilder();
                    getXPath(content.getParentElement(), xpath);
                    _links.put(urlText, xpath.toString());
                }
            }
        }
        
    }

    private void getXPath(Element content, StringBuilder xpath) {
        if (content != null) {
            getXPath(content.getParentElement(), xpath);
            if (xpath.length() > 0) {
                xpath.append("/");
            }
            xpath.append(content.getNamespacePrefix()).append(':').append(content.getName());
            xpath.append('[');

            @SuppressWarnings("unchecked")
            List<Element> siblingsAndSelf = content.getParentElement().getChildren(content.getName(), content.getNamespace());
            xpath.append(siblingsAndSelf.indexOf(content) + 1);
            xpath.append(']');
        }
    }

    private void loadMetadata(ServiceContext serviceContext) throws Exception {
        Dbms dbms = (Dbms) serviceContext.getResourceManager().openDirect(Geonet.Res.MAIN_DB);
        _currentMetadataId = _ids.remove(_ids.size() - 1);
        GeonetContext geonetContext = (GeonetContext) serviceContext.getHandlerContext(Geonet.CONTEXT_NAME);
        _metadata = geonetContext.getDataManager().getMetadata(dbms, _currentMetadataId);
    }

    private void loadIds(ServiceContext serviceContext) throws Exception {

        Dbms dbms = (Dbms) serviceContext.getResourceManager().openDirect(Geonet.Res.MAIN_DB);
        
        @SuppressWarnings("unchecked")
        List<Element> ids = dbms.select("SELECT id from Metadata").getChildren();
        List<String> transformedIds = Lists.transform(ids, new ElementToIdTransformFunction());
        this._ids = new ArrayList<String>(transformedIds);
    }


}
