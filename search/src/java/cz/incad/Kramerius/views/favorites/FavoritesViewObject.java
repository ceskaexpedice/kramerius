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
package cz.incad.Kramerius.views.favorites;

import java.util.ArrayList;
import java.util.List;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;

import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.security.User;
import cz.incad.kramerius.users.UserProfile;
import cz.incad.kramerius.users.UserProfileManager;
import cz.incad.kramerius.utils.conf.KConfiguration;

public class FavoritesViewObject {

    
    
    @Inject
    @Named("securedFedoraAccess")
    FedoraAccess fedoraAccess;

    @Inject
    KConfiguration configuration;
    
    @Inject
    UserProfileManager userProfileManager;
    
    @Inject
    Provider<User> userProvider;

    
    public List<String> getFavorites() {
        User user = this.userProvider.get();
        UserProfile profile = this.userProfileManager.getProfile(user);
        JSONObject jsonData = profile.getJSONData();
        if (jsonData.containsKey("favorites")) {
            JSONArray array = jsonData.getJSONArray("favorites");
            ArrayList<String> alist = new ArrayList<String>();
            for (int i = 0,ll=array.size(); i < ll; i++) {
                alist.add(array.getString(i));
            }
            return alist;
        } else return new ArrayList<String>();
    }
    
}
