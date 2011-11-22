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
package cz.incad.Kramerius.views.inc.results;

import net.sf.json.JSONObject;

import com.google.inject.Inject;
import com.google.inject.Provider;

import cz.incad.kramerius.security.User;
import cz.incad.kramerius.users.UserProfile;
import cz.incad.kramerius.users.UserProfileManager;

public class HeadView {

    @Inject
    Provider<User> userProvider;
    
    @Inject
    UserProfileManager userProfileManager;

    
    public int getProfileColumns() {
        User user = this.userProvider.get();
        UserProfile profile = this.userProfileManager.getProfile(user);
        JSONObject jsonData = profile.getJSONData();
    
        JSONObject results = (JSONObject) jsonData.get("results");
        if (results != null) {
            int retval = results.getInt("columns");
            return retval;
        } else return 2; // default
    }
}
