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
package cz.incad.Kramerius.views;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;


import com.google.inject.Inject;
import com.google.inject.Provider;

import cz.incad.Kramerius.users.ProfilePrepareUtils;
import cz.incad.kramerius.service.ResourceBundleService;

public class PreparedForProfileView {

    @Inject
    Provider<HttpServletRequest> requestProvider;

    @Inject
    Provider<Locale> localesProvider;

    @Inject
    ResourceBundleService resourceBundleService;
    
    public List<ProfileCandidateItem> getProfileCandidateItems() throws IOException {
        Locale locale = this.localesProvider.get();
        ResourceBundle bundle = this.resourceBundleService.getResourceBundle("labels", locale);
        
        HttpSession session = this.requestProvider.get().getSession();
        Map<String, String> preparedProperties = ProfilePrepareUtils.getPreparedProperties(session);
        List<ProfileCandidateItem> items = new ArrayList<PreparedForProfileView.ProfileCandidateItem>();
        if (preparedProperties != null) {
            Set<String> keySet = preparedProperties.keySet();
            for (String key : keySet) {
                ProfileCandidateItem candidateItem = new ProfileCandidateItem(key, preparedProperties.get(key));
                String val = preparedProperties.get(key);
                if (bundle.containsKey("userprofile.forsave.value."+val)) {
                    candidateItem.setLocalizedValue(bundle.getString("userprofile.forsave.value."+val));
                }
                if (bundle.containsKey("userprofile.forsave."+key)) {
                    candidateItem.setLocalizedKey(bundle.getString("userprofile.forsave."+key));
                }                
                items.add(candidateItem);
            }
        }
        return items;
    }
    
    
    public static class ProfileCandidateItem {
        
        private String key;
        private String value;

        private String localizedKey;
        private String localizedValue;
        
        
        private ProfileCandidateItem(String key, String value) {
            super();
            this.key = key;
            this.value = value;
        }

        public String getKey() {
            return key;
        }
        
        public String getValue() {
            return value;
        }
        
        public void setLocalizedKey(String localizedKey) {
            this.localizedKey = localizedKey;
        }
        
        public String getLocalizedKey() {
            if (this.localizedKey == null) return this.key;
            return localizedKey;
        }
        
        public void setLocalizedValue(String localizedValue) {
            this.localizedValue = localizedValue;
        }
        public String getLocalizedValue() {
            if (this.localizedValue == null) return this.value;
            return localizedValue;
        }
    }
    
}
