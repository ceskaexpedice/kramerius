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

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.logging.Level;

import javax.servlet.http.HttpSession;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cz.incad.Kramerius.users.ProfilePrepareUtils;
import cz.incad.kramerius.Initializable;
import cz.incad.kramerius.users.UserProfile;
import cz.incad.kramerius.utils.conf.KConfiguration;
import cz.incad.kramerius.utils.json.JSONUtils;

public class SearchParamsHTMLView extends AbstractSearchParamsViews implements Initializable {

    public static java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(SearchParamsHTMLView.class.getName());

    public static final String SEARCH_HISTORY = "searchHistory";

    public static final String FROM_PROFILE ="fromProfile";
    public static final String FOR_PROFILE ="forProfile";
    

    public static enum ForProfileEnum {
        SEARCHING("search"),
        SORTING_BY_TITLE("sortbytitle"),
        SORTING_BY_RANK("sortbyrank"),
        DATEAXIS("dateaxis"), FACET("facet");

        private String paramVal;

        private ForProfileEnum(String paramVal) {
            this.paramVal = paramVal;
        }
        
        public String getParamVal() {
            return paramVal;
        }
        
        public static ForProfileEnum findEnum(String paramVal) {
            ForProfileEnum[] values = ForProfileEnum.values();
            for (ForProfileEnum fEnum : values) {
                if (fEnum.getParamVal().equals(paramVal)) {
                    return fEnum;
                }
            }
            return null;
        }
    }
    
    @Override
    public void init() {
        //title_sort
        
        try {
            if (this.loggedUsersSingleton.isLoggedUser(this.requestProvider)) {

                String urlString = this.requestProvider.get().getRequestURL().toString();

                Map params = this.requestProvider.get().getParameterMap();
                if (params.containsKey(FOR_PROFILE) && (!params.containsKey(FROM_PROFILE))) {
                    String[] vals = this.requestProvider.get().getParameterValues(FOR_PROFILE);
                    if (vals.length > 0) {
                        ForProfileEnum en = ForProfileEnum.findEnum(vals[0]);
                        if (en != null) {
                            switch (en) {
                            case SEARCHING:
                                saveSearchIntoProfile();
                                break;
                            case DATEAXIS:
                                saveSearchIntoProfile();
                                break;
                            case FACET:
                                saveSearchIntoProfile();
                                break;
                            case SORTING_BY_RANK: 
                                //saveSortingType("rank",  null);
                                prepareSortingType(this.requestProvider.get().getSession(), "rank", null);
                                break;
                            case SORTING_BY_TITLE: 
                                String dir = this.requestProvider.get().getParameter("forProfile_sorting_dir");
                                //saveSortingType("title",dir);
                                prepareSortingType(this.requestProvider.get().getSession(), "title", dir);
                                break;
                            default:
                                break;
                            }
                        }
                    }
                }
            }
        } catch (MalformedURLException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(),e);
        }
    }

    
    private void prepareSortingType(HttpSession session, String sorting, String dir) {
        ProfilePrepareUtils.prepareProperty(session, "sorting", sorting);
        if (dir != null) {
            ProfilePrepareUtils.prepareProperty(session, "sorting_dir", dir);
        }
    }


    
    private void saveSortingType(String sorting, String dir) throws JSONException {
        UserProfile profile = this.userProfileManager.getProfile(this.userProvider.get());
        if (profile!=null) {
            JSONObject jsonData = profile.getJSONData();
            JSONObject results = (JSONObject) jsonData.get("results");
            if (results == null) {
                results = new JSONObject();
                jsonData.put("results", results);
            }
            results.put("sorting", sorting);
            if (dir != null) {
                results.put("sorting_dir", dir);
            } else {
                results.remove("sorting_dir");
            }
            profile.setJSONData(jsonData);
            this.userProfileManager.saveProfile(this.userProvider.get(), profile);
        }
    }



    public static enum SearchingSingleParams {
        issn, title, da_od, da_do, author, rok, udc, ddc, keywords, onlyPublic,q, browse_title;

        public static boolean contains(String val) {
            SearchingSingleParams[] vals = values();
            for (SearchingSingleParams par : vals) {
                if (par.name().equals(val)) return true;
            }
            return false;
        }
    }
    
    public static enum SearchingMultipleParams {
        fq;

        public static boolean contains(String val) {
            SearchingMultipleParams[] vals = values();
            for (SearchingMultipleParams par : vals) {
                if (par.name().equals(val)) return true;
            }
            return false;
        }
    }
    
    private void saveSearchIntoProfile() throws MalformedURLException {
        
        Map<String,String> singleParams = new HashMap<String, String>();
        
        Enumeration enumNames = this.requestProvider.get().getParameterNames();
        while(enumNames.hasMoreElements()) {
            String nm = (String) enumNames.nextElement();
            if (SearchingSingleParams.contains(nm)) {
                String[] vals = this.requestProvider.get().getParameterValues(nm);
                if (vals.length > 0) {
                    singleParams.put(nm, vals[0]);
                }
            }
        }
        
        LOGGER.fine("single params :"+singleParams);
        
        Map<String, List<String>> multiParams = new HashMap<String, List<String>>();
        enumNames = this.requestProvider.get().getParameterNames();
        while(enumNames.hasMoreElements()) {
            String nm = (String) enumNames.nextElement();
            if (SearchingMultipleParams.contains(nm)) {
                String[] vals = this.requestProvider.get().getParameterValues(nm);
                List<String> list = new ArrayList<String>();
                if (vals.length > 0) {
                    for (String val : vals) list.add(val);
                    multiParams.put(nm, list);
                }
            }
        }
        
        LOGGER.fine("multi params :"+multiParams);
        
        if (singleParams.size() > 0 || multiParams.size() > 0) {
            
            UserProfile profile = this.userProfileManager.getProfile(this.userProvider.get());
            JSONObject jsonData = profile.getJSONData();
            
            if (!jsonData.has(SEARCH_HISTORY)) {
                jsonData.put(SEARCH_HISTORY, new JSONArray());
            }
            
            JSONArray shistory = jsonData.getJSONArray(SEARCH_HISTORY);
            
            // remove one item
            int maxItems = KConfiguration.getInstance().getConfiguration().getInt("usersprofile.searchhistory.maxitems",10);
            if (shistory.length()== maxItems) {
                shistory.remove(0);
            }
            
            JSONObject searchObj = new JSONObject();
            String urlString = this.requestProvider.get().getRequestURL().toString();
            String url = urlString+"?"+this.requestProvider.get().getQueryString();
            searchObj.put("url", JSONUtils.escapeQuotes(url));
            
            String rss = urlString.substring(0,urlString.length()-"r.jsp".length())+"r-rss.jsp?"+this.requestProvider.get().getQueryString();
            searchObj.put("rss", JSONUtils.escapeQuotes(rss));

            Set<String> singleKeySet = singleParams.keySet();
            for (String key : singleKeySet) {
                String val = JSONUtils.escapeQuotes(JSONUtils.cutQuotes(singleParams.get(key)));
                searchObj.put(key, val);
            }
            
            Set<String> multipleKeySet = multiParams.keySet();
            for (String key : multipleKeySet) {
                List<String> vals = multiParams.get(key);
                JSONArray arr = new JSONArray();
                for (String rwal : vals) {
                    arr.put(rwal);
                }
                searchObj.put(key,arr);
            }
            
            if (!findQuery(JSONUtils.escapeQuotes(rss),JSONUtils.escapeQuotes(url),shistory)) {
                shistory.put(searchObj);
                profile.setJSONData(jsonData);
                this.userProfileManager.saveProfile(this.userProvider.get(), profile);
                
            }
            
        }
    }


    private boolean findQuery(String rss, String url,JSONArray shistory) throws JSONException {
        for(int i=0,ll=shistory.length();i<ll;i++) {
            JSONObject sobj = (JSONObject) shistory.get(i);
            if (sobj.get("url").equals(url)) {
                return true;
            }
            if (sobj.get("rss")!= null && sobj.get("rss").equals(rss)) {
                return true;
            }
        }
        return false;
    }




    public Map<String, String> getParams(String queryString) throws UnsupportedEncodingException, MalformedURLException {
        Map<String, String> map = new HashMap<String, String>();
        if (queryString != null) {
            StringTokenizer tokenizer = new StringTokenizer(queryString, "&");
            while (tokenizer.hasMoreTokens()) {
                String token = tokenizer.nextToken();
                if (token.contains("=")) {
                    String[] splitted = token.split("=");
                    if (splitted.length == 2) {
                        map.put(splitted[0], splitted[1]);
                    } else {
                        map.put(splitted[0], "");
                    }
                }
            }
        }
        return map;
    }

    /*
     * public static String getResource(URL url) { String file = url.getFile();
     * String query = url.getQuery(); if (query != null) { String substr =
     * file.substring(0, file.length() - query.length()-1); return substr; }
     * else return null; }
     */

}
