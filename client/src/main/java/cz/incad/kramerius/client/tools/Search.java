/*
 * Copyright (C) 2013 Alberto Hernandez
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
package cz.incad.kramerius.client.tools;

import cz.incad.kramerius.client.RESTHelper;
import cz.incad.kramerius.utils.conf.KConfiguration;
import cz.incad.utils.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.httpclient.util.URIUtil;
import org.apache.velocity.tools.config.DefaultKey;
import org.apache.velocity.tools.view.ViewToolContext;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author alberto
 */
@DefaultKey("search")
public class Search {

    public static final Logger LOGGER = Logger.getLogger(Search.class.getName());

    private HttpServletRequest req;
    private I18NTool i18n;
    private String host;
    private String apipoint;
    private IndexConfig fieldsConfig;

    private String facets;
    private String otherParams = "";
    private final String groupedParams = "&group.field=root_pid&group.type=normal&group.threshold=1"
            + "&group.facet=false&group=true&group.truncate=true&group.ngroups=true";
    private final String hlParams = "&hl=true&hl.fl=text_ocr&hl.mergeContiguous=true&hl.snippets=2";

    public void configure(Map props) {
        try {
            req = (HttpServletRequest) props.get("request");
            ViewToolContext vc = (ViewToolContext) props.get("velocityContext");

            host = KConfiguration.getInstance().getConfiguration().getString("k4.host");
            apipoint = KConfiguration.getInstance().getConfiguration().getString("api.point");
            fieldsConfig = IndexConfig.getInstance();

            facets = "&facet.mincount=1";
            JSONArray fs = fieldsConfig.getJSON().getJSONArray("facets");
            for (int i = 0; i < fs.length(); i++) {
                facets += "&facet.field=" + fs.getString(i);
            }

            JSONObject others = fieldsConfig.getJSON().getJSONObject("otherParams");
            Iterator keys = others.keys();
            while (keys.hasNext()) {
                String key = (String) keys.next();
                Object val = others.get(key);
                otherParams += "&" + key + "=" + val;
//                if (val instanceof Integer) {
//                    query.set(key, (Integer) val);
//                } else if (val instanceof String) {
//                    query.set(key, (String) val);
//                } else if (val instanceof Boolean) {
//                    query.set(key, (Boolean) val);
//                }

            }
//            facets = "&facet.mincount=1&facet.field=" + 
//                    fieldsConfig.getMappedField("model_path") + 
//                    "&facet.field=keywords&facet.field=collection&facet.field=dostupnost";

        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        } catch (JSONException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        }
    }
    
    private boolean isHome(){
        String p = req.getParameter("page");
        LOGGER.log(Level.INFO, "page {0}", p);
        return (p==null || "home".equals(p));
        
    }

    public String getMappings() {
        return fieldsConfig.getMappings();
    }

    private String getJSON(String url) throws IOException {

        LOGGER.log(Level.INFO, "requesting url {0}", url);
        InputStream inputStream = RESTHelper.inputStream(url, "application/json", this.req, new HashMap<String, String>());
        StringWriter sw = new StringWriter();
        org.apache.commons.io.IOUtils.copy(inputStream, sw, "UTF-8");
        return sw.toString();
    }

    public JSONArray getDaJSON() {
        try {
            String url = apipoint + "/search"
                    + "?q=*:*&rows=0&facet=true&facet.field=rok&facet.mincount=1&f.rok.facet.sort=false&f.rok.facet.limit=-1"
                    + "&group=true&group.main=true&group.truncate=true&group.ngroups=true&group.field=root_pid&group.format=simple";
            return new JSONObject(getJSON(url))
                    .getJSONObject("facet_counts")
                    .getJSONObject("facet_fields").getJSONArray("rok");
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
            return null;
        } catch (JSONException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
            return null;
        }
    }

    public JSONObject getResults() {
        if(isHome()){
            return getHome();
        }else if (isFilterByType() || !KConfiguration.getInstance().getConfiguration().getBoolean("search.query.collapsed", true)) {
            return getUngrouped();
        } else {
            return getGrouped();
        }
    }
    

    public JSONObject getHome() {
        try {
            String url = apipoint + "/search" 
                    + "?q=*:*&wt=json&rows=0&facet=true&facet.mincount=1" 
                    + "&facet.field=model_path&facet.field=collection&facet.field=dostupnost";
            return new JSONObject(getJSON(url));
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
            return null;
        } catch (JSONException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
            return null;
        }
    }


    public JSONObject getUngrouped() {
        try {

            String q = req.getParameter("q");
            if (q == null || q.equals("")) {
                q = "*:*";
            } else {
                q = URIUtil.encodeQuery(q + getBoost(q), "UTF-8");
            }
            String url = apipoint + "/search" + "?q=" + q + "&wt=json&facet=true"
                    + getStart()
                    + getCollectionFilter()
                    + facets
                    + getSort()
                    + getFilters()
                    + otherParams
                    + hlParams;
            return new JSONObject(getJSON(url));
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
            return null;
        } catch (JSONException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
            return null;
        }
    }

    public JSONObject getGrouped() {
        try {

            String q = req.getParameter("q");
            if (q == null || q.equals("")) {
                q = "*:*";
            } else {
                q = URIUtil.encodeQuery(q + getBoost(q), "UTF-8");
            }

            String url = apipoint + "/search" + "?q=" + q + "&wt=json&facet=true&fl=score,*"
                    + getStart()
                    + getCollectionFilter()
                    + facets
                    + getSort()
                    + getFilters()
                    + groupedParams
                    + otherParams
                    + hlParams;
            return new JSONObject(getJSON(url));
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
            return null;
        } catch (JSONException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
            return null;
        }
    }

    public JSONObject getYear(String fq, String year, String offset, String rows) {
        try {
            String fl = fieldsConfig.getMappedField("title") + ","
                    + fieldsConfig.getMappedField("autor") + ",PID,dostupnost";
            String url = apipoint + "/search" + "?fl=" + fl + "&fq="
                    + URLEncoder.encode(fq, "UTF-8") + "&q=rok:" + year + "&start=" + offset + "&rows=" + rows;
            return new JSONObject(getJSON(url));
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
            return null;
        } catch (JSONException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
            return null;
        }
    }

    public JSONArray getDaJSON(String fq) {
        try {
            String url = apipoint + "/search" + "?q=*:*&fq=" + URLEncoder.encode(fq, "UTF-8") + "&rows=0&facet=true&facet.field=rok&facet.mincount=1&f.rok.facet.sort=false&f.rok.facet.limit=-1";
            return new JSONObject(getJSON(url))
                    .getJSONObject("facet_counts")
                    .getJSONObject("facet_fields").getJSONArray("rok");
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
            return null;
        } catch (JSONException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
            return null;
        }
    }

    private String advFilter(String param) throws UnsupportedEncodingException {
        return advFilter(param, param);
    }

    private String advFilter(String param, String field) throws UnsupportedEncodingException {
        String[] p = req.getParameterValues(param);
        if (p != null) {
            if ("rok".equals(param)) {
                return "&fq=" + field + ":" + URLEncoder.encode(p[0], "UTF-8");
            } else if ("dostupnost".equals(param) && p[0].equals("none")) {
                return "&fq=-dostupnost:" + URLEncoder.encode("['' TO *]", "UTF-8");
            } else {
                String fq = "";
                for (String p1 : p) {
                    fq += "&fq=" + field + ":" + URLEncoder.encode(StringUtils.escapeQueryChars(p1), "UTF-8");
                }
                return fq;
            }
        }
        return "";
    }

    public String getAdvFilter(String param) throws UnsupportedEncodingException {
        return advFilter(param, getFieldFromParam(param));
    }

    public String getFieldFromParam(String param) {

        if ("title".equals(param)) {
            return fieldsConfig.getMappedField("title");
        } else if ("author".equals(param)) {
            return fieldsConfig.getMappedField("autor");
        } else if ("fedora_model".equals(param)) {
            return fieldsConfig.getMappedField("fedora_model");
        } else if ("udc".equals(param)) {
            return "mdt";
        } else if ("ddc".equals(param)) {
            return "ddt";
        } else {
            return param;
        }
    }

    public String getAdvSearch() throws UnsupportedEncodingException {

        StringBuilder res = new StringBuilder();
        res.append(advFilter("title", fieldsConfig.getMappedField("title")));
        res.append(advFilter("author", fieldsConfig.getMappedField("autor")));
        res.append(advFilter("udc", "mdt"));
        res.append(advFilter("ddc", "ddt"));
        res.append(advFilter("rok"));
        res.append(advFilter("keywords"));
        res.append(advFilter(fieldsConfig.getMappedField("fedora_model")));
        res.append(advFilter("collection"));
        res.append(advFilter("dostupnost", "dostupnost"));
        String p = req.getParameter("issn");
        if (p != null && !p.equals("")) {
            res.append("&fq=issn:").append(p).append("%20OR%20").append(fieldsConfig.getMappedField("dc_identifier")).append(":").append(p);
        }
        String root_model = req.getParameter("typ_titulu");
        if (root_model != null && !root_model.equals("")) {
            res.append("&fq=model_path:").append(root_model).append("*");
        }
        return res.toString();

    }

    private void usedFilter(Map<String, String[]> map, String param) {
        String[] p = req.getParameterValues(param);
        if (p != null && !p.equals("")) {
            map.put(param, p);
        }
    }

    private void usedFilter(Map<String, String[]> map, String param, String field) {
        String[] p = req.getParameterValues(param);
        if (p != null) {
            map.put(param, p);
        }
    }

    public Map<String, String[]> getUsedFilters() {
        Map<String, String[]> map = new HashMap<String, String[]>();
        usedFilter(map, "title", fieldsConfig.getMappedField("title"));
        usedFilter(map, "author", fieldsConfig.getMappedField("autor"));
        usedFilter(map, "udc", "mdt");
        usedFilter(map, "ddc", "ddt");
        usedFilter(map, "typ_titulu");
        usedFilter(map, "rok");
        usedFilter(map, "keywords");
        usedFilter(map, fieldsConfig.getMappedField("fedora_model"));
        usedFilter(map, "collection");
        usedFilter(map, "dostupnost", "dostupnost");
        usedFilter(map, "issn");
        return map;
    }

    private String getBoost(String q) {
        String ret = "";
        ret = "&defType=edismax&qf=text+text_lemmatized+text_lemmatized_ascii^0.2+"
                + fieldsConfig.getMappedField("title") + "^4.0+"
                + "title_lemmatized^4.0+"
                + "title_lemmatized_ascii+"
                + fieldsConfig.getMappedField("autor") + "^1.5&bq=(level:0)^4.5"
                + "&bq=" + fieldsConfig.getMappedField("dostupnost") + ":\"public\"^1.2"
                + "&pf=text^10";
        return ret;
    }

    private String getFilters() throws UnsupportedEncodingException {
        String res = getAdvSearch();
        String[] fqs = req.getParameterValues("fq");
        if (fqs != null) {
            for (String fq : fqs) {
                res += "&fq=" + fq;
            }
        }
        String browse_title = req.getParameter("browse_title");
        if (browse_title != null && !browse_title.equals("")) {
            res = "fq=search_title:" + browse_title;
        }
        return res;
    }

    private String getCollectionFilter() {
        String[] cols = req.getParameterValues("collection");
        if (cols != null) {
            String fq = "";
            for(String col:cols){
                fq += "&fq=collection:\"" + StringUtils.escapeQueryChars(col) + "\"";
            }
            return fq;
        }
        return "";
    }

    private String getStart() throws UnsupportedEncodingException {
        String start = req.getParameter("start");
        if (start == null || start.equals("")) {
            start = "0";
        }
        String rows = req.getParameter("rows");
        if (rows == null || rows.equals("")) {
            rows = "40";
        }
        return "&start=" + start + "&rows=" + rows;
    }

    private boolean isFilterByType() {
        String[] fqs = req.getParameterValues("fq");
        if (fqs == null) {
            return false;
        }
        for (String fq : fqs) {
            if (fq.equals("document_type") || fq.equals(fieldsConfig.getMappedField("fedora_model"))) {
                return true;
            }
        }
        return false;
    }

    private String getSort() throws UnsupportedEncodingException {
        String res;
        String sort = req.getParameter("sort");
        String asis = req.getParameter("asis");
        String q = req.getParameter("q");
        boolean filterByType = isFilterByType();
        boolean fieldedSearch = false;
        if (sort != null && !sort.equals("") && asis != null) {
            res = sort;
        } else if (sort != null && !sort.equals("") && filterByType) {
            res = sort;
        } else if (sort != null && !sort.equals("")) {
            res = "level asc, " + sort;
        } else if ((q == null || q.equals("")) && filterByType) {
            res = "title_sort asc";
        } else if (q != null && !q.equals("") && filterByType) {
            res = "score desc, title_sort asc";
        } else if (fieldedSearch) {
            res = "level asc, title_sort asc, score desc";
        } else if (q == null || q.equals("")) {
            res = "level asc, title_sort asc, score desc";
        } else {
            res = "score desc";
        }

        return "&sort=" + URLEncoder.encode(res, "UTF-8");
    }
}
