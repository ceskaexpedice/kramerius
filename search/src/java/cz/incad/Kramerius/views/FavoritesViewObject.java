/*
 * Copyright (C) 2012 Pavel Stastny
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
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Level;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import antlr.RecognitionException;
import antlr.TokenStreamException;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;

import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.Initializable;
import cz.incad.kramerius.SolrAccess;
import cz.incad.kramerius.document.model.DCConent;
import cz.incad.kramerius.document.model.utils.DCContentUtils;
import cz.incad.kramerius.document.model.utils.DescriptionUtils;
import cz.incad.kramerius.security.User;
import cz.incad.kramerius.service.ResourceBundleService;
import cz.incad.kramerius.users.UserProfile;
import cz.incad.kramerius.users.UserProfileManager;

public class FavoritesViewObject extends AbstractViewObject implements Initializable {

    static java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(FavoritesViewObject.class.getName());
    
    @Inject
    UserProfileManager userProfileManager;
    
    @Inject
    Provider<User> userProvider;
    
    @Inject
    ResourceBundleService resourceBundleService;
    
    @Inject
    Locale locale;
    
    @Inject
    SolrAccess solrAccess;

    @Inject
    @Named("securedFedoraAccess")
    FedoraAccess fedoraAccess;
    
    private List<RadioItem> items = new ArrayList<FavoritesViewObject.RadioItem>();
    
    @Override
    public void init() {
        try {
            List params = getDisplayingPids();
            for (int i = 0,ll=params.size(); i < ll; i++) {
                String pid = params.get(i).toString();
                String id = pid.toString().replace(":", "_");
                List<String> favorites = getFavorites();
                items.add(new RadioItem(pid, i+"_"+id, favorites.contains(pid)));
            }
        } catch (RecognitionException e) {
            LOGGER.log(Level.SEVERE,e.getMessage(),e);
        } catch (TokenStreamException e) {
            LOGGER.log(Level.SEVERE,e.getMessage(),e);
        } catch (JSONException e) {
            LOGGER.log(Level.SEVERE,e.getMessage(),e);
        }
    }


    public List getDisplayingPids() throws RecognitionException, TokenStreamException, JSONException {
        List pidsFromParam = getPidsParams();
        if (!pidsFromParam.isEmpty()) return pidsFromParam;
        else return getFavorites();
    }


    public List<String> getFavorites() throws JSONException {
        List<String> selected = new ArrayList<String>();
        UserProfile profile = this.userProfileManager.getProfile(userProvider.get());

        JSONObject jsonData = profile.getJSONData();
        if (jsonData.has("favorites")) {
            Object obj = jsonData.get("favorites");
            JSONArray jsonArray = jsonData.getJSONArray("favorites");
            for (int j = 0,lj=jsonArray.length(); j < lj; j++) {
                Object pidObj = jsonArray.get(j);
                selected.add(pidObj.toString());
            }
        }
        return selected;
    }

    
    public String getHeader() {
        return "Zmena oblibenych";
    }
    
//    public String getDescription() {
//        return "Upravte "
//    }
    
    public List<RadioItem> getItems() {
        return this.items;
    }
    
    
    public class RadioItem {

        protected String pid;
        protected String id;
        protected boolean checked = false;

        public RadioItem(String pid, String id, boolean checked) {
            super();
            this.pid = pid;
            this.id = id;
            this.checked = checked;
        }


        
        public Map<String, List<DCConent>> getDCS() throws IOException {
            return DCContentUtils.getDCS(fedoraAccess, solrAccess, Arrays.asList(getPid()));
        }

        public boolean isDescriptionDefined() throws IOException {
            String[] descs = getDescriptions();
            return descs.length > 0 ;
        }

        public String[] getDescriptions() throws IOException {
            return DescriptionUtils.getDescriptions(FavoritesViewObject.this.resourceBundleService.getResourceBundle("labels", locale), getDCS(), Arrays.asList(getPid()));
        }        

        public boolean isChecked() {
            return this.checked;
        }

        public void setChecked(boolean checked) {
            this.checked = checked;
        }

        public String getCheckedAttribute() {
            return this.checked ? " checked='checked' " : "";
        }

        public String getId() {
            return id;
        }

        public String getPid() {
            return pid;
        }
        

        public String getName() throws IOException {
            ResourceBundle bundle = resourceBundleService.getResourceBundle("labels", locale);
            StringBuilder builder = new StringBuilder();
            List<DCConent> contents = getDCS().get(this.pid);
            for (int i = 0,ll=contents.size(); i < ll; i++) {
                DCConent dcConent = contents.get(i);
                String model = dcConent.getType();
                String i18n = null;
                String resBundleKey = "fedora.model."+model;
                if (bundle.containsKey(resBundleKey)) {
                    i18n = bundle.getString(model);
                } else {
                    i18n = model;
                }
                if (i > 0) builder.append(" | ");
                builder.append(i18n).append(":").append(dcConent.getTitle());
            }

            return builder.toString();
        }

    }
}
