/*
 * Copyright (C) 2010 Pavel Stastny
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package cz.incad.Kramerius.views.social;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.logging.Level;

import javax.servlet.http.HttpServletRequest;

import org.w3c.dom.Document;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;

import cz.incad.Kramerius.backend.guice.LocalesProvider;
import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.ObjectPidsPath;
import cz.incad.kramerius.SolrAccess;
import cz.incad.kramerius.service.ResourceBundleService;
import cz.incad.kramerius.utils.ApplicationURL;
import cz.incad.kramerius.utils.DCUtils;
import cz.incad.kramerius.utils.StringUtils;
import cz.incad.kramerius.utils.pid.LexerException;
import cz.incad.kramerius.utils.pid.PIDParser;
import cz.incad.utils.IKeys;

public abstract class AbstractSocialButton {

    public static final String DEFAULT_LOCALE_STRING="cs_CZ";

    static java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(AbstractSocialButton.class.getName());
    
    private static final String I_JSP = "i.jsp";
    private static final String S_JSP = "s.jsp";
    private static final String SEARCH_JSP = "search.jsp";
    
    @Inject
    Provider<HttpServletRequest> requestProvider;

    @Inject
    Provider<Locale> localeProvider;

    @Inject
    ResourceBundleService bundleService;

    @Inject
    @Named("securedFedoraAccess")
    FedoraAccess fedoraAccess;
    
    @Inject
    SolrAccess solrAccess;


    private Document rootDcData;
    private Document solrData;
    private Document dcData;

    
    public abstract boolean isButtonEnabled();

    public boolean emptyString(String str) {
        return (str == null) || (str.trim().equals(""));
    }
    
    public boolean isHomePage() {
        String contextPath = ApplicationURL.applicationContextPath(this.requestProvider.get());
        String requestedURL = this.requestProvider.get().getRequestURL().toString();
        int indexOfContextPath = requestedURL.indexOf(contextPath);
        String queryString = this.requestProvider.get().getQueryString();
        if (emptyString(queryString)) {
            String stringAfterContext = requestedURL.substring(indexOfContextPath+contextPath.length());
            return stringAfterContext.equals("/") || stringAfterContext.equals("/search.jsp");
        } else return false;
    }
    
    public boolean isItemPage() {
        try {
            HttpServletRequest request = this.requestProvider.get();
            String requestedURL = request.getRequestURL().toString();
            URL url = new URL(requestedURL);
            String furl = StringUtils.minus(requestedURL, "?"+url.getQuery());
            return furl.endsWith(I_JSP);
        } catch (MalformedURLException e) {
            LOGGER.log(Level.SEVERE,e.getMessage(),e);
        }
        return false;
    }
    
    public boolean isSearchPage() {
        try {
            HttpServletRequest request = this.requestProvider.get();
            String requestedURL = request.getRequestURL().toString();
            URL url = new URL(requestedURL);
            String furl = StringUtils.minus(requestedURL, "?"+url.getQuery());
            return furl.endsWith(S_JSP);
        } catch (MalformedURLException e) {
            LOGGER.log(Level.SEVERE,e.getMessage(),e);
        }
        return false;
    }

    
    public String getShareURL() {
        try {
            HttpServletRequest request = this.requestProvider.get();
            if (isItemPage()) {
                String pidParameter = request.getParameter(IKeys.PID_PARAMETER);
                String encoded = URLEncoder.encode(pidParameter, "UTF-8");
                return ApplicationURL.applicationURL(request)+"/handle/"+encoded;
            } else {
                String requestedURL = request.getRequestURL().toString();
                String query = request.getQueryString();
                String returnedShareURL = requestedURL;
                if (!emptyString(query)) {
                    returnedShareURL = requestedURL+"?"+query;
                    if ((request.getParameter("language") != null) && (!request.getParameter("language").trim().equals(""))) {
                        returnedShareURL = requestedURL +"&language="+localeProvider.get().getLanguage();
                    }
                }
                return returnedShareURL;
            }
        } catch (UnsupportedEncodingException e) {
            LOGGER.log(Level.SEVERE,e.getMessage(), e);
            return "#";
        }
    }

    public String getPidParam(HttpServletRequest request) {
        String pid = request.getParameter("pid");
        return pid;
    }

    public String getMetadataType() {
        if (isItemPage()) {
            return "book";
        } else {
            return "product";
        }
    }
    
    public String getApplicationTitle()  {
        try {
            Locale locale = this.localeProvider.get();
            ResourceBundle resbundle = bundleService.getResourceBundle("labels", locale);
            return resbundle.getString("application.title");
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(),e);
            return "";
        }
    }


    public String getMetadataImage() {
        HttpServletRequest request = this.requestProvider.get();
        String applUrl = ApplicationURL.applicationURL(request);
        String pid = request.getParameter("pid");
        if (isItemPage()) {
            return applUrl+"/img?uuid="+pid+"&stream=IMG_THUMB";
        } else {
            return applUrl+"/img/logo.png";
        }
    }

    public String getDescription() throws IOException {
        if (isItemPage()) {
            return getDescriptionForItem();
        } else {
            ResourceBundle resBundle = bundleService.getResourceBundle("labels", this.localeProvider.get());
            return resBundle.getString("application.about");
        }
    }
    

    Document getSolrDocument() throws IOException {
        if (solrData == null) {
            this.solrData = solrAccess.getSolrDataDocument(getPID());
        }
        return this.solrData;
    }
    
    Document getDCDocument() throws IOException {
        if (this.dcData == null) {
            this.dcData = this.fedoraAccess.getDC(getPID());
        }
        return this.dcData;
    }

    public String getDescriptionForItem() throws IOException {
        List<String> list = new ArrayList<String>();
        Document dcDocument = getDCDocument();
        ResourceBundle resBundle = this.bundleService.getResourceBundle("labels", this.localeProvider.get());
        
        String modelFromDC = DCUtils.modelFromDC(dcDocument);
        if (modelFromDC != null) {
            String key = "fedora.model."+modelFromDC;
            if (resBundle.containsKey(key)) {
                list.add(resBundle.getString(key));
            } else { list.add(key); }
        }
        
        String titleFromDC = DCUtils.titleFromDC(dcDocument);
        if (titleFromDC != null) list.add(titleFromDC);
        
        if ("periodical".equals(modelFromDC)) {
            String[] pubs = DCUtils.publishersFromDC(dcDocument);
            if (pubs != null) list.addAll(Arrays.asList(pubs));
        } else {
            String[] crets = DCUtils.creatorsFromDC(dcDocument);
            if (crets != null) list.addAll(Arrays.asList(crets));
        }
        
        String date = DCUtils.dateFromDC(dcDocument);
        if (date != null) list.add(date);
        
        if (!list.isEmpty()) {
            StringBuilder builder = new StringBuilder();
            for (int i = 0, ll = list.size(); i < ll; i++) {
                String st = list.get(i);
                if (i>0) {
                    builder.append(", ");
                }
                builder.append(st);
            }
            return builder.toString();
        }
        else return "";
    }

    public Document getRootDCDocument(String pid, String datastreamName) throws IOException {
        if (rootDcData == null) {
            ObjectPidsPath[] path = this.solrAccess.getPath(datastreamName, getSolrDocument());
            if (path.length > 0 ) {
                String root = path[0].getRoot();
                rootDcData = fedoraAccess.getDC(root);
            } else {
                rootDcData = fedoraAccess.getDC(pid);
            }
        }
        return rootDcData;
    }

    
    public String getPID() {
        String pid = this.requestProvider.get().getParameter("pid");
        return pid;
    }

    public String getRootTitle() {
        try {
            String pid = getPID();
            PIDParser pidParser = new PIDParser(pid);
            pidParser.objectPid();
            Document document = getRootDCDocument(pid, pidParser.isDatastreamPid() ? pidParser.getDataStream() : null);
            return DCUtils.titleFromDC(document);
        } catch (LexerException e) {
            LOGGER.log(Level.SEVERE,e.getMessage(),e);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE,e.getMessage(),e);
        }
        
        return null;
    }

    
    public String getTitle() {
        if (this.isItemPage()) {
            return getRootTitle();
        } else {
            return getApplicationTitle();
        }
    }


}
