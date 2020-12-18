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
package cz.incad.kramerius.rest.apiNew.client.v60;

import com.google.inject.Inject;
import cz.incad.kramerius.SolrAccess;
import cz.incad.kramerius.rest.api.k5.client.JSONDecorator;
import cz.incad.kramerius.rest.api.k5.client.JSONDecoratorsAggregate;
import cz.incad.kramerius.rest.apiNew.exceptions.BadRequestException;
import cz.incad.kramerius.rest.apiNew.exceptions.InternalErrorException;
import cz.incad.kramerius.utils.IOUtils;
import cz.incad.kramerius.utils.XMLUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;
import org.apache.commons.lang.StringUtils;
import org.apache.http.client.HttpResponseException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.inject.Named;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.*;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.*;
import java.net.URLEncoder;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.apache.http.HttpStatus.SC_BAD_REQUEST;

//TODO: pouklizet, probrat endpointy (terms?), prejmenovate endpoint metody

@Path("/client/v6.0/search")
public class SearchResource {

    private static Logger LOGGER = Logger.getLogger(SearchResource.class.getName());
    private static final String[] FILTERED_FIELDS = {"text_ocr"}; //see api.solr.filtered for old index

    @Inject
    @Named("new-index")
    private SolrAccess solrAccess;

    @Inject
    private JSONDecoratorsAggregate jsonDecoratorAggregates;

    @GET
    @Produces({MediaType.APPLICATION_XML + ";charset=utf-8"})
    public Response selectXML(@Context UriInfo uriInfo, @QueryParam("wt") String wt) {
        if ("json".equals(wt)) {
            return Response.ok().type(MediaType.APPLICATION_JSON + ";charset=utf-8")
                    .entity(getEntityJSON(uriInfo).toString()).build();
        } else {
            return Response.ok().entity(getEntityXML(uriInfo).toString()).build();
        }
    }

    private String getEntityXML(UriInfo uriInfo) {
        try {
            MultivaluedMap<String, String> queryParameters = uriInfo.getQueryParameters();
            StringBuilder builder = new StringBuilder();
            Set<String> keys = queryParameters.keySet();
            for (String k : keys) {
                for (String v : queryParameters.get(k)) {
                    if (k.equals("fl")) {
                        checkFieldSettings(v);
                    }
                    String value = URLEncoder.encode(v, "UTF-8");
                    value = checkHighlightValues(k, value);
                    builder.append(k + "=" + value);
                    builder.append("&");
                }
            }
            InputStream istream = this.solrAccess.request(builder.toString(), "xml");

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            IOUtils.copyStreams(istream, bos);
            String rawString = new String(bos.toByteArray(), "UTF-8");

            String uri = UriBuilder.fromResource(SearchResource.class).path("").build().toString();
            Document domObject = changeXMLResult(rawString, uri);

            StringWriter strWriter = new StringWriter();
            XMLUtils.print(domObject, strWriter);

            return strWriter.toString();
        } catch (HttpResponseException e) {
            if (e.getStatusCode() == SC_BAD_REQUEST) {
                LOGGER.log(Level.INFO, "SOLR Bad Request: " + uriInfo.getRequestUri());
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

    private void checkFieldSettings(String value) {
        List<String> filters = Arrays.asList(FILTERED_FIELDS);
        String[] vals = value.split(",");
        for (String v : vals) {
            // remove field alias
            v = StringUtils.substringAfterLast(v, ":");
            if (filters.contains(v)) throw new BadRequestException("requesting filtering field");
        }
    }

    private String checkHighlightValues(String v, String value) {
        if (v.equals("hl.fragsize")) {
            try {
                int confVal = KConfiguration.getInstance().getConfiguration().getInt("api.search.highlight.defaultfragsize", 20);
                int maxVal = KConfiguration.getInstance().getConfiguration().getInt("api.search.highlight.maxfragsize", 120);
                int val = Integer.parseInt(value);
                if (val == 0) {
                    val = confVal;
                } else if (val > maxVal) {
                    val = confVal;
                }
                return "" + val;
            } catch (NumberFormatException e) {
                LOGGER.log(Level.SEVERE, e.getMessage(), e);
                return value;
            }
        } else {
            return value;
        }
    }

    @GET
    @Produces({MediaType.APPLICATION_JSON + ";charset=utf-8"})
    public Response selectJSON(@Context UriInfo uriInfo, @QueryParam("wt") String wt) {
        if ("xml".equals(wt)) {
            return Response.ok().type(MediaType.APPLICATION_XML + ";charset=utf-8")
                    .entity(getEntityXML(uriInfo).toString()).build();
        } else {
            return Response.ok().entity(getEntityJSON(uriInfo).toString()).build();
        }
    }

    private String getEntityJSON(UriInfo uriInfo) {
        try {

            MultivaluedMap<String, String> queryParameters = uriInfo
                    .getQueryParameters();
            StringBuilder builder = new StringBuilder();
            Set<String> keys = queryParameters.keySet();
            for (String k : keys) {
                for (String v : queryParameters.get(k)) {
                    if (k.equals("fl")) {
                        checkFieldSettings(v);
                    }
                    String value = URLEncoder.encode(v, "UTF-8");
                    value = checkHighlightValues(k, value);
                    builder.append(k + "=" + value);
                    builder.append("&");
                }
            }
            InputStream istream = this.solrAccess.request(builder.toString(),
                    "json");

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            IOUtils.copyStreams(istream, bos);
            String rawString = new String(bos.toByteArray(), "UTF-8");

            String uri = UriBuilder.fromResource(SearchResource.class).path("")
                    .build().toString();
            JSONObject jsonObject = changeJSONResult(rawString, uri, this.jsonDecoratorAggregates.getDecorators());

            return jsonObject.toString();
        } catch (HttpResponseException e) {
            if (e.getStatusCode() == SC_BAD_REQUEST) {
                LOGGER.log(Level.INFO, "SOLR Bad Request: " + uriInfo.getRequestUri());
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

    /**
     * Change result in xml result
     *
     * @param rawString XML result
     * @param context   Calling context
     * @return Changed result
     * @throws ParserConfigurationException Parser error
     * @throws SAXException                 Parse error
     * @throws IOException                  IO error
     */
    private Document changeXMLResult(String rawString, String context)
            throws ParserConfigurationException, SAXException, IOException {
        Document doc = XMLUtils.parseDocument(new StringReader(rawString));
        List<Element> elms = XMLUtils.getElementsRecursive(doc.getDocumentElement(), new XMLUtils.ElementsFilter() {
            @Override
            public boolean acceptElement(Element element) {
                return (element.getNodeName().equals("doc"));
            }
        });
        for (Element docE : elms) {
            filterOutFieldsFromDOM(docE);
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

    private JSONObject changeJSONResult(String rawString, String context, List<JSONDecorator> decs) throws UnsupportedEncodingException, JSONException {

        //List<JSONDecorator> decs = this.jsonDecoratorAggregates.getDecorators();
        List<JSONArray> docsArrays = new ArrayList<JSONArray>();

        JSONObject resultJSONObject = new JSONObject(rawString);
        Stack<JSONObject> prcStack = new Stack<JSONObject>();
        prcStack.push(resultJSONObject);
        while (!prcStack.isEmpty()) {
            JSONObject popped = prcStack.pop();
            //Iterator keys = popped.keys();
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
                // decorators
                applyDecorators(context, decs, docJSON);
            }
        }
        return resultJSONObject;
    }

    private void applyDecorators(String context, List<JSONDecorator> decs, JSONObject docJSON) throws JSONException {
        // decorators
        Map<String, Object> runtimeCtx = new HashMap<String, Object>();
        for (JSONDecorator d : decs) {
            d.before(runtimeCtx);
        }
        for (JSONDecorator jsonDec : decs) {
            boolean canApply = jsonDec.apply(docJSON, context);
            if (canApply) {
                jsonDec.decorate(docJSON, runtimeCtx);
            }
        }
        for (JSONDecorator d : decs) {
            d.after();
        }
    }

    private void filterOutFieldsFromJSON(JSONObject jsonObj) {
        for (String filteredFieldName : FILTERED_FIELDS) {
            if (jsonObj.has(filteredFieldName)) {
                jsonObj.remove(filteredFieldName);
            }
        }
    }

    @GET
    @Path("terms")
    @Produces({MediaType.APPLICATION_XML + ";charset=utf-8"})
    public Response termsXML(@Context UriInfo uriInfo) {
        try {
            MultivaluedMap<String, String> queryParameters = uriInfo.getQueryParameters();
            StringBuilder builder = new StringBuilder();
            Set<String> keys = queryParameters.keySet();
            for (String k : keys) {
                for (String v : queryParameters.get(k)) {
                    builder.append(k + "=" + URLEncoder.encode(v, "UTF-8"));
                    builder.append("&");
                }
            }
            InputStream istream = this.solrAccess.terms(builder.toString(),
                    "xml");
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            IOUtils.copyStreams(istream, bos);

            String rawString = new String(bos.toByteArray(), "UTF-8");
            String uri = UriBuilder.fromResource(SearchResource.class)
                    .path("terms").build().toString();
            // Document domObject = changeXMLResult(rawString, uri);
            //
            // StringWriter strWriter = new StringWriter();
            // XMLUtils.print(domObject, strWriter);

            return Response.ok().entity(rawString).build();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, e.getMessage());
            throw new InternalErrorException(e.getMessage());
        }
    }

    @GET
    @Path("terms")
    @Produces({MediaType.APPLICATION_JSON + ";charset=utf-8"})
    public Response termsJSON(@Context UriInfo uriInfo) {
        try {
            MultivaluedMap<String, String> queryParameters = uriInfo.getQueryParameters();
            StringBuilder builder = new StringBuilder();
            Set<String> keys = queryParameters.keySet();
            for (String k : keys) {
                for (String v : queryParameters.get(k)) {
                    builder.append(k + "=" + URLEncoder.encode(v, "UTF-8"));
                    builder.append("&");
                }
            }
            InputStream istream = this.solrAccess.terms(builder.toString(),
                    "json");
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            IOUtils.copyStreams(istream, bos);

            String rawString = new String(bos.toByteArray(), "UTF-8");

            return Response.ok().entity(rawString).build();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, e.getMessage());
            throw new InternalErrorException(e.getMessage());
        }
    }

}
