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

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.logging.Level;

import javax.servlet.http.HttpServletRequest;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.google.inject.Inject;
import com.google.inject.Provider;

import cz.incad.Kramerius.Initializable;
import cz.incad.kramerius.security.User;
import cz.incad.kramerius.users.LoggedUsersSingleton;
import cz.incad.kramerius.users.UserProfile;
import cz.incad.kramerius.users.UserProfileManager;
import cz.incad.kramerius.utils.UTFSort;
import cz.incad.kramerius.utils.conf.KConfiguration;

public class SearchParamsViews implements Initializable {

    public static java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(SearchParamsViews.class.getName());

    public static final String SEARCH_HISTORY = "searchHistory";

    private static final String DA_DO = "da_do";
    private static final String DA_OD = "da_od";

    @Inject
    Provider<HttpServletRequest> requestProvider;

    @Inject
    UserProfileManager userProfileManager;

    @Inject
    Provider<User> userProvider;

    @Inject
    LoggedUsersSingleton loggedUsersSingleton;

    @Override
    public void init() {

        if (this.loggedUsersSingleton.isLoggedUser(this.requestProvider)) {

            String urlString = this.requestProvider.get().getRequestURL().toString();

            Map<String, String> params = this.requestProvider.get().getParameterMap();
            if (params.containsKey("q")) {

                UserProfile profile = this.userProfileManager.getProfile(this.userProvider.get());
                JSONObject jsonData = profile.getJSONData();
                if (!jsonData.containsKey(SEARCH_HISTORY)) {
                    jsonData.put(SEARCH_HISTORY, new JSONArray());
                }
                JSONArray shistory = jsonData.getJSONArray(SEARCH_HISTORY);

                JSONObject searchObj = new JSONObject();
                searchObj.put("url", urlString+"?"+this.requestProvider.get().getQueryString());
                searchObj.put("query", params.get("q"));
                shistory.add(searchObj);

                profile.setJSONData(jsonData);
                this.userProfileManager.saveProfile(this.userProvider.get(), profile);

            }

        }
    }

    public String getSearchResultsRows() {
        return KConfiguration.getInstance().getProperty("search.results.rows", "20");
    }

    public int getYearFrom() throws ParseException {
        Date dfrom = getDateFrom();
        Calendar cal = Calendar.getInstance();
        cal.setTime(dfrom);
        return cal.get(Calendar.YEAR);
    }

    public int getYearUntil() throws ParseException {
        Date dunt = getDateUntil();
        Calendar cal = Calendar.getInstance();
        cal.setTime(dunt);
        return cal.get(Calendar.YEAR);
    }

    public Date getDateFrom() throws ParseException {
        HttpServletRequest request = this.requestProvider.get();
        String parameter = request.getParameter(DA_OD);
        return parameter != null ? getModsDateFormat().parse(parameter) : null;
    }

    public Date getDateUntil() throws ParseException {
        HttpServletRequest request = this.requestProvider.get();
        String parameter = request.getParameter(DA_DO);
        return parameter != null ? getModsDateFormat().parse(parameter) : null;
    }

    public String getDateFromFormatted() throws ParseException {
        return getModsDateFormatForOutput().format(getDateFrom());
    }

    public String getDateUntilFormatted() throws ParseException {
        return getModsDateFormatForOutput().format(getDateUntil());
    }

    public SimpleDateFormat getModsDateFormatForOutput() {
        SimpleDateFormat dfout = new SimpleDateFormat(KConfiguration.getInstance().getProperty("mods.date.format", "yyyy-MM-dd'T'hh:mm:ss.SSS'Z'"));
        return dfout;
    }

    public SimpleDateFormat getModsDateFormat() {
        SimpleDateFormat df = new SimpleDateFormat(KConfiguration.getInstance().getProperty("mods.date.format", "dd.MM.yyyy"));
        return df;
    }

    public String getBrowserTitle() throws IOException {
        HttpServletRequest request = this.requestProvider.get();
        String t = request.getParameter("browse_title");
        UTFSort utf_sort = new UTFSort();
        utf_sort.init();
        String browseTitle = utf_sort.translate(t);
        browseTitle = "\"" + browseTitle + "##" + t + "\"";
        // URI uri = new URI(request.getRequestURI());
        // uri.getQuery();

        return browseTitle;
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

    public static void main(String[] args) throws MalformedURLException, UnsupportedEncodingException {
        String uril[] = { "http://localhost:8080/search/r.jsp?debug=&sort=&q=&issn=&title=Drobn%C5%AFstky&author=&rok=&udc=&ddc=", "http://localhost:8080/search/r.jsp", "http://localhost:8080/search" };

        URL url = new URL(uril[1]);
        System.out.println(url.getFile());
        System.out.println(url.getPath());
        
        JSONObject obj = new JSONObject();
        System.out.println(obj.containsKey("test"));

        /*
         * System.out.println(url.getUserInfo());
         * System.out.println(url.getQuery()); String[] params =
         * url.getQuery().split("&"); for (String par : params) {
         * System.out.println(URLDecoder.decode(par,"UTF-8")); }
         */
        // System.out.println(url.getRef());
    }
}
