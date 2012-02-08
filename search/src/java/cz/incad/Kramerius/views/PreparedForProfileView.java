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
import java.util.Arrays;
import java.util.HashMap;
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

    //"results":{"sorting_dir":"desc","columns":"2","sorting":"title"},
    //"client_locale":"cs"}"
    
    protected List<String> KEYS = new ArrayList<String>(Arrays.asList(new String[]{"sorting","sorting_dir","columns","client_locale"}));
    protected HashMap<String, String> DEFAULT_VALS = new HashMap<String, String>(); {
        DEFAULT_VALS.put("sorting","title");
        DEFAULT_VALS.put("sorting_dir","desc");
        DEFAULT_VALS.put("columns","2");
    };
    
    
    @Inject
    Provider<HttpServletRequest> requestProvider;

    @Inject
    Provider<Locale> localesProvider;

    @Inject
    ResourceBundleService resourceBundleService;
    

    
    
    public PreparedForProfileView() {
        super();
        
    }


    public List<ProfileCandidateItem> getProfileCandidateItems() throws IOException {
        
        Locale locale = this.localesProvider.get();
        ResourceBundle bundle = this.resourceBundleService.getResourceBundle("labels", locale);
        
        this.DEFAULT_VALS.put("client_locale", locale.getLanguage());
        
        HttpSession session = this.requestProvider.get().getSession();
        Map<String, String> preparedProperties = ProfilePrepareUtils.getPreparedProperties(session);
        if (preparedProperties == null) preparedProperties = new HashMap<String, String>();

        List<ProfileCandidateItem> items = new ArrayList<PreparedForProfileView.ProfileCandidateItem>();
        for (String key : KEYS) {
            boolean flag = preparedProperties.containsKey(key);
            String value = flag ? preparedProperties.get(key) : DEFAULT_VALS.get(key);
            ProfileCandidateItem item = item(bundle, key, value);
            item.setChecked(flag);
            items.add(item);
        }
        return items;
    }


    public ProfileCandidateItem item(ResourceBundle bundle,  String key, String value) {
        ProfileCandidateItem candidateItem = new ProfileCandidateItem(key, value);
        
        if (bundle.containsKey("userprofile.forsave.value."+value)) {
            candidateItem.setLocalizedValue(bundle.getString("userprofile.forsave.value."+value));
        }
        if (bundle.containsKey("userprofile.forsave."+key)) {
            candidateItem.setLocalizedKey(bundle.getString("userprofile.forsave."+key));
        }       
        
        return candidateItem;
    }
    
    
    public static class ProfileCandidateItem {
        
        private String key;
        private String value;

        private String localizedKey;
        private String localizedValue;
    
        private boolean checked = false;
        
        
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

        public boolean isChecked() {
            return checked;
        }

        public void setChecked(boolean checked) {
            this.checked = checked;
        }
        
    }
    
}
