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
package cz.incad.kramerius.users.impl;

import net.sf.json.JSONObject;
import cz.incad.kramerius.users.UserProfile;

public class UserProfileImpl implements UserProfile {

    private String rawData="{}";
    
    public UserProfileImpl() {
        super();
    }

    public UserProfileImpl(String rawData) {
        super();
        this.rawData = rawData;
    }

    @Override
    public String getRawData() {
        return this.rawData;
    }

    @Override
    public JSONObject getJSONData() {
        return JSONObject.fromObject(this.rawData);
    }

    @Override
    public void setJSONData(JSONObject jsonObject) {
        this.rawData = jsonObject.toString();
    }

    
    public static void main(String[] args) {
        //JSONObject obj = new JSONObject();
        JSONObject object = JSONObject.fromObject("{test:'abc',testa:['a','b']}");
        System.out.println(object);
    }
}
