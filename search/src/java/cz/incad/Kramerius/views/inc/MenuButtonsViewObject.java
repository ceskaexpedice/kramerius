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
package cz.incad.Kramerius.views.inc;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import javax.servlet.http.HttpServletRequest;

import com.google.inject.Inject;
import com.google.inject.Provider;

import cz.incad.kramerius.auth.thirdparty.shibb.utils.ShibbolethUtils;
import cz.incad.kramerius.utils.ApplicationURL;
import cz.incad.kramerius.utils.conf.KConfiguration;

public class MenuButtonsViewObject {

    @Inject
    Provider<HttpServletRequest> requestProvider;

    @Inject
    KConfiguration kConfiguration;

    String[] getConfigredItems() {
        String[] langs = kConfiguration.getPropertyList("interface.languages");

        return langs;
    }
    
    
    public String getQueryString() {
        HttpServletRequest request = this.requestProvider.get();
        if (request.getQueryString() != null) return request.getQueryString();
        else return "";
    }

    public boolean isUnderShibbolethSession() {
        HttpServletRequest req = this.requestProvider.get();
        return ShibbolethUtils.isUnderShibbolethSession(req);
    }
    
    public boolean getShibbLogoutEnabled() {
        HttpServletRequest req = this.requestProvider.get();
        if (ShibbolethUtils.isUnderShibbolethSession(req)) {
            return KConfiguration.getInstance().getConfiguration().getBoolean("security.shib.logout.enabled",false);
        }
        return true;
    }


    public String getShibbLogout() {
        HttpServletRequest req = this.requestProvider.get();
        if (ShibbolethUtils.isUnderShibbolethSession(req)) {
            String property = KConfiguration.getInstance().getProperty("security.shib.logout");
            return property;
        }
        return null;
    }
    
    
    public List<LanguageItem> getLanguageItems() {
        String[] items = getConfigredItems();
        List<LanguageItem> links = new ArrayList<LanguageItem>();
        

        StringBuffer buffer = new StringBuffer();
        String queryString = getQueryString();
        StringTokenizer tokenizer = new StringTokenizer(queryString,"&");
        while(tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken();
            if (!token.trim().startsWith("language")) {
                if (buffer.length() > 0) {
                    buffer.append("&");
                }
                buffer.append(token);
            }
        }
        
        for (int i = 0; i < items.length; i++) {
            String name = items[i];
            String link =  "?language="+ items[++i] + "&" + buffer.toString() ;
            LanguageItem itm = new LanguageItem(link, name, items[i]);
            links.add(itm);
        }
        return links;
    
    
    }

    
    public static class LanguageItem {
        
        private String link;
        private String name;
        private String key;
        
        private LanguageItem(String link, String name, String key) {
            super();
            this.link = link;
            this.name = name;
            this.key = key;
        }
        
        public String getLink() {
            return link;
        }
        
        public String getName() {
            return name;
        }
        
        public String getKey(){
            return this.key;
        }
    }
}
