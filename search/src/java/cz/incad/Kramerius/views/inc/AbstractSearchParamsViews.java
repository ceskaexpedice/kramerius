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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONException;
import org.json.JSONObject;

import com.google.inject.Inject;
import com.google.inject.Provider;

import cz.incad.kramerius.security.User;
import cz.incad.kramerius.users.LoggedUsersSingleton;
import cz.incad.kramerius.users.UserProfile;
import cz.incad.kramerius.users.UserProfileManager;
import cz.incad.kramerius.utils.UTFSort;
import cz.incad.kramerius.utils.conf.KConfiguration;

public class AbstractSearchParamsViews {

    public static final String DA_DO = "da_do";
    public static final String DA_OD = "da_od";
    @Inject
    protected Provider<HttpServletRequest> requestProvider;
    @Inject
    protected UserProfileManager userProfileManager;
    @Inject
    protected Provider<User> userProvider;
    @Inject
    protected LoggedUsersSingleton loggedUsersSingleton;
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
    
    public String getEscapedIssn(){
        return getEscapedParameter("issn");
    }
    
    public String getEscapedTitle(){
        return getEscapedParameter("title");
    }
    
    public String getEscapedAuthor(){
        return getEscapedParameter("author");
    }
    
    public String getEscapedRok(){
        return getEscapedParameter("rok");
    }
    
    public String getEscapedUdc(){
        return getEscapedParameter("udc");
    }
    
    public String getEscapedDdc(){
        return getEscapedParameter("ddc");
    }
    
    public String getEscapedKeywords(){
        return getEscapedParameter("keywords");
    }
        
    public String getEscapedParameter(String param){
        HttpServletRequest request = this.requestProvider.get();
        String escaped_q = request.getParameter(param);
        escaped_q = escaped_q.replaceAll(" ", "+");
        // * is not escaped, as we want wildcard search
        String escapeChars ="[\\\\+\\-\\!\\(\\)\\:\\^\\]\\{\\}\\~\\?]";
        escaped_q = escaped_q.replaceAll(escapeChars, "\\\\$0");
        return escaped_q;
    }
    public String getEscapedQuery(){
        HttpServletRequest request = this.requestProvider.get();
        String escaped_q = request.getParameter("q");
        // * is not escaped, as we want wildcard search
        String escapeChars ="[\\\\+\\-\\!\\(\\)\\:\\^\\]\\{\\}\\~\\?]";
        escaped_q = escaped_q.replaceAll(escapeChars, "\\\\$0");
        return escaped_q;
    }
    public String getSearchResultsRows() {
        return KConfiguration.getInstance().getProperty("search.results.rows", "20");
    }
    public String getSortingFromProfile() throws JSONException {
        UserProfile profile = this.userProfileManager.getProfile(this.userProvider.get());
        JSONObject jsonData = profile.getJSONData();
        JSONObject results = (JSONObject) jsonData.get("results");
        if (results != null) {
            String str = (String) results.get("sorting");
            if (str != null) {
                if (str.equals("title")) {
                    String dir = (String) jsonData.get("sorting_dir");
                    if (dir != null)  return "title_sort "+dir;
                    else return "title_sort asc";
                } else {
                    return "level asc, score desc";
                }
            } else return null;
        } else return null;
    }

}
