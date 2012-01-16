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
import cz.incad.kramerius.pdf.utils.TitlesUtils;
import cz.incad.kramerius.service.ResourceBundleService;
import cz.incad.kramerius.utils.ApplicationURL;
import cz.incad.kramerius.utils.DCUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;
import cz.incad.kramerius.utils.pid.LexerException;
import cz.incad.kramerius.utils.pid.PIDParser;

public class FacebookLikeItButton extends AbstractSocialButton {
    
    public static final String DEFAULT_LOCALE_STRING="cs_CZ";
    
    @Inject
    Provider<Locale> localeProvider;

    @Inject
    KConfiguration configuration;
    
    @Inject
    @Named("securedFedoraAccess")
    FedoraAccess fedoraAccess;
    
    @Inject
    SolrAccess solrAccess;
    
    @Inject
    LocalesProvider localesProvider;
    
    @Inject
    ResourceBundleService bundleService;

    private Document solrData;
    private Document dcData;
    
    public String getMetadataType() {
        if (isItemPage()) {
            return "book";
        } else {
            return "product";
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
    
    public String getLocale() {
        Locale locale = this.localeProvider.get();
        String country = locale.getCountry();
        String language = locale.getLanguage();
        if (!emptyString(country) && (!emptyString(language))) {
            return language+"_"+country;
        } else return DEFAULT_LOCALE_STRING;
    }

    
    public String getRootTitle() {
        try {
            String pid = getPID();
            PIDParser pidParser = new PIDParser(pid);
            pidParser.objectPid();
            ObjectPidsPath[] path = this.solrAccess.getPath(pidParser.isDatastreamPid() ? pidParser.getDataStream() : null, getSolrDocument());
            if (path.length > 0 ) {
                String root = path[0].getRoot();
                return DCUtils.titleFromDC(fedoraAccess.getDC(root));
            } else {
                return DCUtils.titleFromDC(fedoraAccess.getDC(pid));
            }
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

    public String getDescription() throws IOException {
        if (isItemPage()) {
            return getDescriptionForItem();
        } else {
            ResourceBundle resBundle = bundleService.getResourceBundle("labels", localesProvider.get());
            return resBundle.getString("application.about");
        }
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
    
    public String getPID() {
        String pid = this.requestProvider.get().getParameter("pid");
        return pid;
    }

    public String getApplicationTitle()  {
        try {
            Locale locale = localeProvider.get();
            ResourceBundle resbundle = bundleService.getResourceBundle("labels", locale);
            return resbundle.getString("application.title");
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(),e);
            return "";
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
    
    @Override
    public boolean isButtonEnabled() {
        boolean fbEnabled = configuration.getConfiguration().getBoolean("facebook.likeit");
        return fbEnabled && (this.isItemPage() || isHomePage());
    }
}
