/*
 * Copyright (C) 2013 Pavel Stastny
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
package cz.incad.kramerius.rest.apiNew.client.v70;

import com.google.inject.Inject;
import cz.incad.kramerius.SolrAccess;
import cz.incad.kramerius.rest.api.k5.client.JSONDecorator;
import cz.incad.kramerius.rest.apiNew.client.v70.filter.ProxyFilter;
import cz.incad.kramerius.rest.apiNew.client.v70.redirection.DeleteTriggerSupport;
import cz.incad.kramerius.rest.apiNew.exceptions.BadRequestException;
import cz.incad.kramerius.rest.apiNew.exceptions.InternalErrorException;
import cz.incad.kramerius.utils.conf.KConfiguration;
import cz.inovatika.monitoring.APICallMonitor;
import cz.inovatika.monitoring.ApiCallEvent;
import cz.incad.kramerius.security.licenses.License;
import cz.incad.kramerius.security.licenses.LicensesManager;
import cz.incad.kramerius.security.licenses.LicensesManagerException;
import cz.incad.kramerius.security.User;
import cz.incad.kramerius.solr.SolrKeys;
import cz.incad.kramerius.utils.XMLUtils;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.apache.http.client.HttpResponseException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.inject.Named;
import javax.inject.Provider;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.*;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static org.apache.http.HttpStatus.SC_BAD_REQUEST;

//TODO: odstranit /terms?, pokud ponechat, tak možná omezit limity, povolená pole (kvůli omezení těžení)

@Path("/client/v7.0/search")
public class SearchResource {

    private static Logger LOGGER = Logger.getLogger(SearchResource.class.getName());
    private static final String[] FILTERED_FIELDS = {"text_ocr"}; //see api.solr.filtered for old index
    
    private static final String[] CONTROLLED_SIZE_FIELDS = {"text_ocr"}; //see api.solr.filtered for old index

    private static final int DEFAULT_FRAG_SIZE = 20; //see api.search.highlight.defaultfragsize for old index
    private static final int MAX_FRAG_SIZE = 120; //see api.search.highlight.maxfragsize for old index
    
    @Inject
    private LicensesManager licensesManager;
    
    @Inject
    @Named("new-index")
    private SolrAccess solrAccess;


    @Inject
    ProxyFilter proxyFilter;
    
    @javax.inject.Inject
    Provider<User> userProvider;

    @Inject
    APICallMonitor apiCallMonitor;



    @GET
    public Response get(@Context UriInfo uriInfo, @Context HttpHeaders headers, @QueryParam("wt") String wt) {

        MultivaluedMap<String, String> queryParameters = uriInfo.getQueryParameters();
        String queryString = queryParameters.entrySet().stream()
                .flatMap(entry -> entry.getValue().stream()
                        .map(value -> {
                            try {
                                return entry.getKey() + "=" + URLEncoder.encode(value, StandardCharsets.UTF_8.toString());
                            } catch (Exception e) {
                                LOGGER.log(Level.WARNING, e.getMessage());
                                return "";
                            }
                        }))
                .collect(Collectors.joining("&"));

        ApiCallEvent event = this.apiCallMonitor.start("/client/v7.0/search", "/client/v7.0/search", queryString, "GET");
        try {
            if ("json".equals(wt)) {
                return Response.ok().type(MediaType.APPLICATION_JSON + ";charset=utf-8").entity(buildSearchResponseJson(uriInfo,event)).build();
            } else if ("xml".equals(wt)) {
                return Response.ok().type(MediaType.APPLICATION_XML + ";charset=utf-8").entity(buildSearchResponseXml(uriInfo, event)).build();
            } else { //format not specified in query param "wt"
                boolean preferXmlAccordingToHeaderAccept = false;
                List<String> headerAcceptValues = headers.getRequestHeader("Accept");
                if (headerAcceptValues != null) { //can be null instead of empty list in some implementations
                    for (String headerValue : headerAcceptValues) {
                        if ("application/xml".equals(headerValue) || "text/xml".equals(headerValue)) {
                            preferXmlAccordingToHeaderAccept = true;
                            break;
                        }
                    }
                }
                if (preferXmlAccordingToHeaderAccept) { //header Accept contains "application/xml" or "text/xml"
                    return Response.ok().type(MediaType.APPLICATION_XML + ";charset=utf-8").entity(buildSearchResponseXml(uriInfo, event)).build();
                } else { //default format: json
                    return Response.ok().type(MediaType.APPLICATION_JSON + ";charset=utf-8").entity(buildSearchResponseJson(uriInfo, event)).build();
                }
            }
        } catch (WebApplicationException e) {
            throw e;
        } catch (Throwable e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new InternalErrorException(e.getMessage());
        } finally {
            if (event != null) {
                this.apiCallMonitor.stop(event, userProvider.get().getLoginname());
            }
        }
    }

    private String buildSearchResponseJson(UriInfo uriInfo, ApiCallEvent event) {

        AtomicReference<String> queryRef = new AtomicReference<>();
        try {
            // identify pid query and response multiple
            String solrQuery = buildSearchSolrQueryString(uriInfo);
            queryRef.set(solrQuery);
            String solrResponseJson = this.solrAccess.requestWithSelectReturningString(solrQuery, "json", event);
            String uri = UriBuilder.fromResource(SearchResource.class).path("").build().toString();
            JSONObject jsonObject = buildJsonFromRawSolrResponse(solrResponseJson, uri, new ArrayList<>(), event);

            JSONObject response = jsonObject.optJSONObject("responseHeader");
            if (response != null) {
                int qtime = response.getInt("QTime");
                List<Triple<String, Long, Long>> granularTimeSnapshots = event != null ? event.getGranularTimeSnapshots() : null;
                if (granularTimeSnapshots != null) {
                    granularTimeSnapshots.add(Triple.of("solr/qtime", event.getStartTime(), event.getStartTime() + qtime));
                }
            }


            return jsonObject.toString();
        } catch (HttpResponseException e) {
            if (e.getStatusCode() == SC_BAD_REQUEST) {
                String reasonPhrase = e.getReasonPhrase();
                
                String message = String.format("Bad Request (api request = %s,\n solr request %s)", uriInfo.getRequestUri(), queryRef.get());
                LOGGER.log(Level.SEVERE, message);
                LOGGER.log(Level.SEVERE, String.format("Reason phrase %s", reasonPhrase));
                
                throw new BadRequestException(e.getMessage());
            } else {
                LOGGER.log(Level.INFO, e.getMessage(), e);
                throw new InternalErrorException(e.getMessage());
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new InternalErrorException(e.getMessage());
        } catch (JSONException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new InternalErrorException(e.getMessage());
        }
    }

    private String buildSearchResponseXml(UriInfo uriInfo, ApiCallEvent event) {
        AtomicReference<String> queryRef = new AtomicReference<>();
        try {
            String solrQuery = buildSearchSolrQueryString(uriInfo);
            queryRef.set(solrQuery);

            String solrResponseXml = this.solrAccess.requestWithSelectReturningString(solrQuery, "xml", event);
            Document domObject = buildXmlFromRawSolrResponse(solrResponseXml, event);
            StringWriter strWriter = new StringWriter();
            XMLUtils.print(domObject, strWriter);
            return strWriter.toString();
        } catch (HttpResponseException e) {
            if (e.getStatusCode() == SC_BAD_REQUEST) {

                String message = String.format("Bad Request (api request = %s,\n solr request %s)", uriInfo.getRequestUri(), queryRef.get());
                LOGGER.log(Level.SEVERE, message);

                throw new BadRequestException(e.getMessage());
            } else {
                LOGGER.log(Level.INFO, e.getMessage(), e);
                throw new InternalErrorException(e.getMessage());
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new InternalErrorException(e.getMessage());
        } catch (TransformerException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new InternalErrorException(e.getMessage());
        } catch (ParserConfigurationException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new InternalErrorException(e.getMessage());
        } catch (SAXException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new InternalErrorException(e.getMessage());
        }
    }

    private String buildSearchSolrQueryString(UriInfo uriInfo) throws UnsupportedEncodingException {
        boolean cdkServerMode = isOnCDKSide();
        MultivaluedMap<String, String> queryParameters = uriInfo.getQueryParameters();

        int snippets = -1;
        int fragsize = -1;
        boolean fqFound = false;

        StringBuilder builder = new StringBuilder();
        Set<String> keys = queryParameters.keySet();
        
        for (String k : keys) {
            for (final String v : queryParameters.get(k)) {
                String value = v;
                if (k.equals("fl")) {
                    checkFlValueDoesNotContainFilteredField(value);
                }
                if (k.equals("fq")) {
                    if (cdkServerMode)  value = this.proxyFilter.enhancedFilter(value);
                }
                
                if (k.equals("hl.fragsize")) {
                    value = validateHighlightFragsize(value).toString();
                    fragsize = Integer.parseInt( value );
                }
                if (k.equals("hl.snippets")) {
                    value = validateHighlightSnippets(value).toString();
                    snippets = Integer.parseInt( value );
                }
                if (k.equals("hl.snippets")) {
                    value = validateHighlightSnippets(value).toString();
                    snippets = Integer.parseInt( value );
                }

                builder.append(k).append("=").append(URLEncoder.encode(value, "UTF-8"));
                builder.append("&");
            }
        }
        if (snippets>-1 && fragsize >-1) {
            validateHLCombination(fragsize, snippets);
        }
        
        if (!fqFound) {
            if (cdkServerMode) {
                builder.append("&");
                builder.append("fq=");
                String newFilter = proxyFilter.newFilter();
                if (newFilter != null) builder.append(URLEncoder.encode(proxyFilter.newFilter(), "UTF-8"));
            }
        }
        if (cdkServerMode) {
            String eFT = this.proxyFilter.enhanceFacetsTerms();
            if (eFT != null) {
                builder.append("&facet.excludeTerms="+eFT);
            }
        }
        return builder.toString();
    }

    private static boolean isOnCDKSide() {
        return KConfiguration.getInstance().getConfiguration().getBoolean("cdk.server.mode");
    }

    private void checkFlValueDoesNotContainFilteredField(String comaSeparatedValues) {
        List<String> filters = Arrays.asList(FILTERED_FIELDS);
        String[] vals = comaSeparatedValues.split(",");
        for (String v : vals) {
            // remove field alias
            v = StringUtils.substringAfterLast(v, ":");
            if (filters.contains(v)) {
                throw new BadRequestException("requesting filtered field");
            }
        }
    }


    private Integer validateHighlightFragsize(String value) {
        try {
            Integer hlFragSize = Integer.valueOf(value);
            if (hlFragSize > SolrKeys.MAX_HL_FRAGSIZE) {
                throw new BadRequestException(String.format("The value of the parameter hl.fragsize is too large (%d). The maximum allowed value is %d.", hlFragSize, SolrKeys.MAX_HL_FRAGSIZE));
            }
            if (hlFragSize == 0) {
                throw new BadRequestException("The value of the parameter hl.fragsize cannot be 0");
            }
            return hlFragSize ;
        } catch (NumberFormatException e) {
            throw new BadRequestException(e.getMessage());
        }
    }

    private Integer validateHighlightSnippets(String value) {
        try {
            Integer hlSnippets = Integer.valueOf(value);
            if (hlSnippets > SolrKeys.MAX_HL_SNIPPETS) {
                throw new BadRequestException(String.format("The value of the parameter hl.snippet is too large (%d). The maximum allowed value is %d.", hlSnippets, SolrKeys.MAX_HL_SNIPPETS));
            }
            return hlSnippets;
        } catch (NumberFormatException e) {
            throw new BadRequestException(e.getMessage());
        }
    }
    
    
    private void validateHLCombination(int  fragsize, int snippets) {
        int combination = fragsize * snippets;
        if (combination > SolrKeys.MAX_HL_COMBINATION) {
            throw new BadRequestException(String.format("The combination of the parameters hl.snippet and hl.fragsize is too high (%d*%d). The maximum allowed value is %d.", snippets,fragsize, SolrKeys.MAX_HL_COMBINATION));
        }
    }
    /**
     * Build XML document from SOLR response as a raw String
     *
     * @param rawString SOLR response
     */
    private Document buildXmlFromRawSolrResponse(String rawString, ApiCallEvent event) throws ParserConfigurationException, SAXException, IOException {
        long start = System.currentTimeMillis();
        List<Triple<String, Long, Long>> triples = event != null ? event.getGranularTimeSnapshots() : null;
        Document doc = XMLUtils.parseDocument(new StringReader(rawString));
        List<Element> elms = XMLUtils.getElementsRecursive(doc.getDocumentElement(), new XMLUtils.ElementsFilter() {
            @Override
            public boolean acceptElement(Element element) {
                return (element.getNodeName().equals("doc"));
            }
        });
        for (Element docE : elms) {
            filterOutFieldsFromDOM(docE);
            if (isOnCDKSide())  this.proxyFilter.filterValue(docE,event);
        }
        if (triples != null) {
            long stop = System.currentTimeMillis();
            triples.add(Triple.of("manipulation/xml", start,stop));
        }
        return doc;
    }

    private void filterOutFieldsFromDOM(Element docE) {
        for (String filteredFieldName : FILTERED_FIELDS) {
            Element found = findSolrElement(docE, filteredFieldName);
            if (found != null) {
                Node parentNode = found.getParentNode();
                Node removed = parentNode.removeChild(found);
            }
        }
    }

    private Element findSolrElement(Element docE, final String name) {
        Element found = XMLUtils.findElement(docE,
                new XMLUtils.ElementsFilter() {
                    @Override
                    public boolean acceptElement(Element element) {
                        return (element.hasAttribute("name") && element.getAttribute("name").equals(name));
                    }
                });
        return found;
    }

    /**
     * Build JSON document from SOLR response as a raw String
     *
     * @param rawString SOLR response
     */
    private JSONObject buildJsonFromRawSolrResponse(String rawString, String context, List<JSONDecorator> decs, ApiCallEvent event) throws UnsupportedEncodingException, JSONException {
        List<Triple<String, Long, Long>> triples = event != null ? event.getGranularTimeSnapshots() : null;
        long start = System.currentTimeMillis();

        List<String> sortedLicenses = new ArrayList<>();
        try {
            List<License> allLicenses = this.licensesManager.getAllLicenses();
            sortedLicenses =  allLicenses.stream().map(License::getName).collect(Collectors.toList());
        } catch (LicensesManagerException e) {
            LOGGER.log(Level.SEVERE,e.getMessage(),e);
        }
        
        List<JSONArray> docsArrays = new ArrayList<JSONArray>();
        JSONObject resultJSONObject = new JSONObject(rawString);
        Stack<JSONObject> prcStack = new Stack<JSONObject>();
        prcStack.push(resultJSONObject);
        while (!prcStack.isEmpty()) {
            JSONObject popped = prcStack.pop();
            for (Iterator keys = popped.keys(); keys.hasNext(); ) {
                Object kobj = (Object) keys.next();
                String key = (String) kobj;
                Object obj = popped.get(key);
                boolean docsKey = key.equals("docs");
                if (docsKey && (obj instanceof JSONArray)) {
                    docsArrays.add((JSONArray) obj);
                }
                if (obj instanceof JSONObject) {
                    prcStack.push((JSONObject) obj);
                }
                if (obj instanceof JSONArray) {
                    JSONArray arr = (JSONArray) obj;
                    for (int i = 0, ll = arr.length(); i < ll; i++) {
                        Object arrObj = arr.get(i);
                        if (arrObj instanceof JSONObject) {
                            prcStack.push((JSONObject) arrObj);
                        }
                    }
                }
            }
        }

        for (JSONArray docs : docsArrays) {
            for (int i = 0, ll = docs.length(); i < ll; i++) {
                JSONObject docJSON = (JSONObject) docs.get(i);
                // fiter protected fields
                filterOutFieldsFromJSON(docJSON);
                if (sortedLicenses.size() > 0) {
                    List<String> keys = Arrays.asList("licenses_of_ancestors","licenses","contains_licenses");
                    for (String key : keys) {
                        if (docJSON.has(key)) {
                            JSONArray licArray = docJSON.getJSONArray(key);
                            List<String> notSortedSubLicenses = toStringList(licArray);
                            List<String> sortedSubLicenses = resortLicenses(sortedLicenses, notSortedSubLicenses);
                            docJSON.put(key, toJSONArray(sortedSubLicenses));
                        }
                    }
                }
                if (isOnCDKSide()) {
                    this.proxyFilter.filterValue(docJSON, event);
                }
            }
        }


        if (triples != null) {
            long stop = System.currentTimeMillis();
            triples.add(Triple.of("manipulation/json", start, stop));
        }
        return resultJSONObject;
    }
    
    private List<String> resortLicenses(List<String> sortedLicenses, List<String> notSortedLicenses) {
        List<String> sortedNotSorted = new ArrayList<>();
        for (String license : sortedLicenses) {
            if (notSortedLicenses.contains(license)) {
                sortedNotSorted.add(license);
            }
        }
        return sortedNotSorted;
    }
    private List<String> toStringList(JSONArray licArray) {
        List<String> licArrayValues = new ArrayList<>();
        for (int j = 0; j < licArray.length(); j++) { licArrayValues.add(licArray.getString(j)); }
        return licArrayValues;
    }

    private JSONArray toJSONArray(List<String> stringList) {
        JSONArray jsonArray = new JSONArray();
        for (String str : stringList) {
            jsonArray.put(str);
        }
        return jsonArray;
    }
    

    private void filterOutFieldsFromJSON(JSONObject jsonObj) {
        for (String filteredFieldName : FILTERED_FIELDS) {
            if (jsonObj.has(filteredFieldName)) {
                jsonObj.remove(filteredFieldName);
            }
        }
        
    }

    @GET
    @Path("/terms")
    public Response getTerms(@Context UriInfo uriInfo, @QueryParam("wt") String wt) {
        try {
            if ("json".equals(wt)) {
                return Response.ok().type(MediaType.APPLICATION_JSON + ";charset=utf-8").entity(buildTermsResponseJson(uriInfo)).build();
            } else if ("xml".equals(wt)) {
                return Response.ok().type(MediaType.APPLICATION_XML + ";charset=utf-8").entity(buildTermsResponseXml(uriInfo)).build();
            } else { //json is default
                return Response.ok().type(MediaType.APPLICATION_JSON + ";charset=utf-8").entity(buildTermsResponseJson(uriInfo)).build();
            }
        } catch (WebApplicationException e) {
            throw e;
        } catch (Throwable e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new InternalErrorException(e.getMessage());
        }
    }

    private String buildTermsResponseJson(UriInfo uriInfo) {
        try {
            InputStream istream = this.solrAccess.requestWithTerms(buildTermsSolrQueryString(uriInfo), "json");
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            IOUtils.copy(istream, bos);
            String rawString = new String(bos.toByteArray(), "UTF-8");
            return rawString;
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, e.getMessage());
            throw new InternalErrorException(e.getMessage());
        }
    }

    private String buildTermsResponseXml(UriInfo uriInfo) {
        try {
            InputStream istream = this.solrAccess.requestWithTerms(buildTermsSolrQueryString(uriInfo), "xml");
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            IOUtils.copy(istream, bos);
            String rawString = new String(bos.toByteArray(), "UTF-8");
            return rawString;
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, e.getMessage());
            throw new InternalErrorException(e.getMessage());
        }
    }

    private String buildTermsSolrQueryString(UriInfo uriInfo) throws UnsupportedEncodingException {
        MultivaluedMap<String, String> queryParameters = uriInfo.getQueryParameters();
        StringBuilder builder = new StringBuilder();
        Set<String> keys = queryParameters.keySet();
        for (String key : keys) {
            for (String value : queryParameters.get(key)) {
                builder.append(key).append("=").append(URLEncoder.encode(value, "UTF-8"));
                builder.append("&");
            }
        }
        return builder.toString();
    }

}
