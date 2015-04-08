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
package cz.incad.kramerius.rest.api.k5.client.search;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.bouncycastle.jce.provider.JCEBlockCipher.DES;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import com.google.inject.Inject;

import cz.incad.kramerius.SolrAccess;
import cz.incad.kramerius.rest.api.exceptions.BadRequestException;
import cz.incad.kramerius.rest.api.exceptions.GenericApplicationException;
import cz.incad.kramerius.rest.api.k5.client.JSONDecorator;
import cz.incad.kramerius.rest.api.k5.client.JSONDecoratorsAggregate;
import cz.incad.kramerius.rest.api.k5.client.item.ItemResource;
import cz.incad.kramerius.rest.api.k5.client.utils.PIDSupport;
import cz.incad.kramerius.rest.api.k5.client.utils.SOLRUtils;
import cz.incad.kramerius.utils.IOUtils;
import cz.incad.kramerius.utils.XMLUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;

import java.net.URLEncoder;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

@Path("/v5.0/search")
public class SearchResource {

    public static Logger LOGGER = Logger.getLogger(SearchResource.class
            .getName());

    @Inject
    SolrAccess solrAccess;

    @Inject
    JSONDecoratorsAggregate jsonDecoratorAggregates;

    @GET
    @Produces({ MediaType.APPLICATION_XML + ";charset=utf-8" })
    public Response selectXML(@Context UriInfo uriInfo) {
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
                    "xml");

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            IOUtils.copyStreams(istream, bos);
            String rawString = new String(bos.toByteArray(), "UTF-8");

            String uri = UriBuilder.fromResource(SearchResource.class).path("")
                    .build().toString();
            Document domObject = changeXMLResult(rawString, uri);

            StringWriter strWriter = new StringWriter();
            XMLUtils.print(domObject, strWriter);

            return Response.ok().entity(strWriter.toString()).build();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new GenericApplicationException(e.getMessage());
        } catch (TransformerException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new GenericApplicationException(e.getMessage());
        } catch (ParserConfigurationException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new GenericApplicationException(e.getMessage());
        } catch (SAXException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new GenericApplicationException(e.getMessage());
        }
    }

    private void checkFieldSettings(String value) {
        List<String> filters = Arrays.asList(KConfiguration.getInstance().getAPISolrFilter());
        String[] vals = value.split(",");
        for (String v : vals) {
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
                return ""+val;
            } catch (NumberFormatException e) {
                LOGGER.log(Level.SEVERE, e.getMessage(), e);
                return value;
            }
        } else {
            return value;
        }
    }

    @GET
    @Produces({ MediaType.APPLICATION_JSON + ";charset=utf-8" })
    public Response selectJSON(@Context UriInfo uriInfo) {
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
            
            return Response.ok().entity(jsonObject.toString()).build();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new GenericApplicationException(e.getMessage());
        }
    }

    /**
     * Change result in xml result
     * 
     * @param rawString
     *            XML result
     * @param context
     *            Calling context
     * @return Changed result
     * @throws ParserConfigurationException
     *             Parser error
     * @throws SAXException
     *             Parse error
     * @throws IOException
     *             IO error
     */
    public static Document changeXMLResult(String rawString, String context)
            throws ParserConfigurationException, SAXException, IOException {
        Document doc = XMLUtils.parseDocument(new StringReader(rawString));
        List<Element> elms = XMLUtils.getElementsRecursive(doc.getDocumentElement(),new XMLUtils.ElementsFilter() {
                @Override
                public boolean acceptElement(Element element) {
                    return (element.getNodeName().equals("doc"));
                }
        });
        for (Element docE : elms) {
            changeMasterPidInDOM(docE);
            filterFieldsInDOM(docE);
            replacePidsInDOM(docE);
        }
        return doc;
    }

    public static void replacePidsInDOM(Element docE) {
        String[] apiReplace = KConfiguration.getInstance().getAPIPIDReplace();
        for (String k : apiReplace) {
            if (k.equals("PID"))
                continue; // already replaced
            Element foundElm = findSolrElement(docE, k);
            if (foundElm != null) {

                if (foundElm.getNodeName().equals("str")) {
                    String value = SOLRUtils.value(foundElm.getTextContent(),
                            String.class);
                    if (value != null && (value.indexOf("/@") > 0)) {
                        value = value.replace("/@", "@");
                        foundElm.setTextContent(value);
                    }
                } else if (foundElm.getNodeName().equals("arr")) {
                    List<String> array = SOLRUtils.array(docE, k, String.class);
                    List<String> newArray = new ArrayList<String>();
                    for (String value : array) {
                        value = value.replace("/@", "@");
                        newArray.add(value);
                    }

                    docE.removeChild(foundElm);
                    Element newArrElm = SOLRUtils.arr(
                            foundElm.getOwnerDocument(), k, newArray);
                    docE.appendChild(newArrElm);
                } else {
                    LOGGER.warning("skipping object type '"
                            + foundElm.getNodeName() + "'");
                }

            }
        }

    }

    public static void filterFieldsInDOM(Element docE) {
        // filter
        String[] filters = KConfiguration.getInstance().getAPISolrFilter();
        for (final String name : filters) {
            Element found = findSolrElement(docE, name);
            if (found != null) {
                Node parentNode = found.getParentNode();
                Node removed = parentNode.removeChild(found);
            }
        }
    }

    public static Element findSolrElement(Element docE, final String name) {
        Element found = XMLUtils.findElement(docE,
                new XMLUtils.ElementsFilter() {
                    @Override
                    public boolean acceptElement(Element element) {
                        return (element.hasAttribute("name") && element
                                .getAttribute("name").equals(name));
                    }
                });
        return found;
    }

    public static JSONObject changeJSONResult(String rawString, String context, List<JSONDecorator> decs)
            throws UnsupportedEncodingException {

        //List<JSONDecorator> decs = this.jsonDecoratorAggregates.getDecorators();
        List<JSONArray> docsArrays = new ArrayList<JSONArray>();
        
        JSONObject resultJSONObject = JSONObject.fromObject(rawString);
        Stack<JSONObject> prcStack = new Stack<JSONObject>();
        prcStack.push(resultJSONObject);
        while(!prcStack.isEmpty()) {
            JSONObject popped = prcStack.pop();
            Set keys = popped.keySet();
            for (Object kobj : keys) {
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
                    for (Object arrObj : arr) {
                        if (arrObj instanceof JSONObject) {
                            prcStack.push((JSONObject) arrObj);
                        }
                    }
                }
            }
        }

        for (JSONArray docs : docsArrays) {
            for (Object obj : docs) {
                JSONObject docJSON = (JSONObject) obj;
                // check master pid
                changeMasterPidInJSON(docJSON);

                // fiter protected fields
                filterFieldsInJSON(docJSON);

                // decorators
                decorators(context, decs, docJSON);

                // replace pids
                replacePidsInJSON(docJSON);
            }
        }
        return resultJSONObject;
    }

    public static void decorators(String context, List<JSONDecorator> decs,
            JSONObject docJSON) {
        // decorators
        Map<String, Object> runtimeCtx = new HashMap<String, Object>();
        for (JSONDecorator d : decs) {
            d.before(runtimeCtx);
        }
        for (JSONDecorator jsonDec : decs) {
            if (jsonDec.apply(docJSON, context)) {
                jsonDec.decorate(docJSON, runtimeCtx);
            }
        }
        for (JSONDecorator d : decs) {
            d.after();
        }
    }

    public static void replacePidsInJSON(JSONObject jsonObj) {
        // repair results
        String[] apiReplace = KConfiguration.getInstance().getAPIPIDReplace();
        for (String k : apiReplace) {
            if (k.equals("PID"))
                continue; // already replaced
            if (jsonObj.containsKey(k)) {
                Object object = jsonObj.get(k);
                if (object instanceof String) {
                    String s = jsonObj.getString(k);
                    if (s.indexOf("/@") > 0) {
                        s.replace("/@", "@");
                        jsonObj.put(k, s);
                    }
                } else if (object instanceof JSONArray) {
                    JSONArray jsonArr = (JSONArray) object;
                    JSONArray newJSONArray = new JSONArray();
                    int size = jsonArr.size();
                    for (int i = 0; i < size; i++) {
                        Object sObj = jsonArr.get(i);
                        if (sObj instanceof String) {
                            String s = (String) sObj;
                            s = s.replace("/@", "@");
                            newJSONArray.add(s);

                        } else {
                            LOGGER.warning("skipping object type '"
                                    + sObj.getClass().getName() + "'");
                        }
                    }
                    jsonObj.put(k, newJSONArray);
                } else {
                    LOGGER.warning("skipping object type '"
                            + object.getClass().getName() + "'");
                }
            }
        }
    }

    public static void filterFieldsInJSON(JSONObject jsonObj) {
        // filter
        String[] filters = KConfiguration.getInstance().getAPISolrFilter();
        for (String filterKey : filters) {
            if (jsonObj.containsKey(filterKey)) {
                jsonObj.remove(filterKey);
            }
        }
    }

    public static void changeMasterPidInJSON(JSONObject jsonObj) {
        if (jsonObj.containsKey("PID")) {
            // pid contains '/' char
            String pid = jsonObj.getString("PID");
            if (pid.contains("/")) {
                pid = pid.replace("/", "");
                jsonObj.put("PID", pid);
            }

        }
    }

    public static void changeMasterPidInDOM(Element docElem) {
        // <str name="PID">uuid:2ad31d65-50ca-11e1-916e-001b63bd97ba</str>
        Element elm = XMLUtils.findElement(docElem,
                new XMLUtils.ElementsFilter() {

                    @Override
                    public boolean acceptElement(Element element) {
                        if (element.getNodeName().equals("str")) {
                            if (element.hasAttribute("name")
                                    && (element.getAttribute("name")
                                            .equals("PID"))) {
                                return true;
                            }
                        }
                        return false;
                    }
                });
        if (elm != null) {
            String pid = elm.getTextContent();
            if (pid.contains("/")) {
                pid = pid.replace("/", "");
                elm.setTextContent(pid);
            }
        }
    }

    @GET
    @Path("terms")
    @Produces({ MediaType.APPLICATION_XML + ";charset=utf-8" })
    public Response termsXML(@Context UriInfo uriInfo) {
        try {
            MultivaluedMap<String, String> queryParameters = uriInfo
                    .getQueryParameters();
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
            throw new GenericApplicationException(e.getMessage());
        }

    }

    @GET
    @Path("terms")
    @Produces({ MediaType.APPLICATION_JSON + ";charset=utf-8" })
    public Response termsJSON(@Context UriInfo uriInfo) {
        try {
            MultivaluedMap<String, String> queryParameters = uriInfo
                    .getQueryParameters();
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
            throw new GenericApplicationException(e.getMessage());
        }
    }

}
