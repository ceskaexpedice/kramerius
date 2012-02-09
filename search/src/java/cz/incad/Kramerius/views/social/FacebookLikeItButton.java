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
    
    
    
    
    public String getLocale() {
        Locale locale = this.localeProvider.get();
        String country = locale.getCountry();
        String language = locale.getLanguage();
        if (!emptyString(country) && (!emptyString(language))) {
            return language+"_"+country;
        } else return DEFAULT_LOCALE_STRING;
    }
    

    public String getAuthors() {
        try {
            String pid = getPID();
            PIDParser pidParser = new PIDParser(pid);
            pidParser.objectPid();
            Document rootDCDocument = getRootDCDocument(pid, pidParser.isDatastreamPid() ? pidParser.getDataStream() : null);
            String[] creators = DCUtils.creatorsFromDC(rootDCDocument);
            if (creators.length > 0) {
                StringBuilder builder = new StringBuilder();
                for (int i = 0; i < creators.length; i++) {
                    if (i > 0) {
                        builder.append(", ");
                    }
                    builder.append(creators[i]);
                }
            }
            return "";
        } catch (LexerException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(),e);
            return "";
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(),e);
            return "";
        }
    }
    
    
    
    @Override
    public boolean isButtonEnabled() {
        boolean fbEnabled = configuration.getConfiguration().getBoolean("facebook.likeit");
        return fbEnabled && (this.isItemPage() || isHomePage());
    }
}
