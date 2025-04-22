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
package cz.incad.kramerius.utils.solr;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import cz.incad.kramerius.impl.SolrAccessImpl;
import cz.incad.kramerius.solr.SolrFieldsMapping;

import cz.inovatika.monitoring.ApiCallEvent;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.apache.hc.client5.http.HttpResponseException;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.core5.http.HttpEntity;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import cz.incad.kramerius.utils.RESTHelper;
import cz.incad.kramerius.utils.XMLUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;

import static org.apache.http.HttpStatus.SC_OK;

/**
 * Generic solr utility; refactor
 * @see SolrAccessImpl
 * @author pavels
 */
public class SolrUtils   {
    public static final Logger LOGGER = Logger.getLogger(SolrUtils.class.getName());
    //public static final String DNNT_FLAG = "dnnt";
//    /** Handle query */
//    public static final String HANDLE_QUERY="q=handle:";


    /** PID query */
    public static final String UUID_QUERY="q=pid:";
    /** Parent query */
    public static final String PARENT_QUERY="q=own_parent.pid:";


    // factory instance
    static XPathFactory fact =XPathFactory.newInstance();
    
    /** 
     * Conscturcts XPath for disecting pid path 
     * @return Compiled XPath expression
     * @throws XPathExpressionException Cannot compile xpath
     */
    public static XPathExpression pidPathExpr() throws XPathExpressionException {
        XPathExpression pidPathExpr = fact.newXPath().compile("//arr[@name='pid_paths']/str");
        return pidPathExpr;
    }

    public static XPathExpression ownpidPathExpr() throws XPathExpressionException {
        XPathExpression pidPathExpr = fact.newXPath().compile("//str[@name='own_pid_path']");
        return pidPathExpr;
    }



    
//    /** 
//     * Conscturcts XPath for disecting pid path 
//     * @return Compiled XPath expression
//     * @throws XPathExpressionException Cannot compile xpath
//     */
//    public static XPathExpression pidPathExpr() throws XPathExpressionException {
//        XPathExpression pidPathExpr = fact.newXPath().compile("//arr[@name='pid_paths']/str");
//        return pidPathExpr;
//    }

    /**
     * Constructs XPath for disecting PID
     * @return Compiled XPath expression
     * @throws XPathExpressionException Cannot compile xpath
     */
    public static XPathExpression docPidExpr() throws XPathExpressionException {
        XPathExpression pidExpr = fact.newXPath().compile("//str[@name='pid']");
        return pidExpr;
    }

//    public static XPathExpression elmPidExpr() throws XPathExpressionException {
//        XPathExpression pidExpr = fact.newXPath().compile("str[@name='PID']");
//        return pidExpr;
//    }

    /**
     * Constructs XPath for disecting model path
     * @return Compiled XPath expression
     * @throws XPathExpressionException Cannot compile xpath
     */
    public static XPathExpression modelPathExpr() throws XPathExpressionException {
        XPathExpression pathExpr = fact.newXPath().compile("//str[@name='own_model_path']");
        return pathExpr;
    }
    
    /**
     * Constructs XPath for disecting fedora model
     * @return Compiled XPath expression
     * @throws XPathExpressionException Cannot compile xpath
     */
    public static XPathExpression fedoraModelExpr() throws XPathExpressionException {
        XPathExpression fedoraModelExpr = fact.newXPath().compile("//str[@name='model']");
        return fedoraModelExpr;
    }
    
    /**
     * Constructs XPath for disecting parent PID
     * @return Compiled XPath expression
     * @throws XPathExpressionException Cannot compile xpath
     */
    public static XPathExpression parentPidExpr() throws XPathExpressionException {
        XPathExpression pidExpr = fact.newXPath().compile("//arr[@name='own_parent.pid']/str");
        return pidExpr;
    }

    /**
     * Constructs XPath for disecting root PID
     * @return Compiled XPath expression
     * @throws XPathExpressionException Cannot compile xpath
     */
    public static XPathExpression rootPidExpr() throws XPathExpressionException {
        XPathExpression rootExpr = fact.newXPath().compile("//str[@name='root.pid']");
        return rootExpr;
    }

   public static XPathExpression rootTitleExpr() throws XPathExpressionException {
       XPathExpression rootExpr = fact.newXPath().compile("//str[@name='root.title']");
       return rootExpr;
   }

   public static XPathExpression rootModelExpr() throws XPathExpressionException {
       XPathExpression rootExpr = fact.newXPath().compile("//str[@name='root.model']");
       return rootExpr;
   }
   

    
    /**
     * Constructs XPath for disecting date
     * @return Compiled XPath expression
     * @throws XPathExpressionException Cannot compile xpath
     */
    public static XPathExpression dateExpr() throws XPathExpressionException {
        XPathExpression dateExpr = fact.newXPath().compile("//str[@name='date.str']");
        return dateExpr;
    }

    /**
     * Disects pid paths from given parsed solr document
     * @return pid paths
     * @throws XPathExpressionException cannot disect pid paths
     */
    public static List<String> disectPidPaths( Document parseDocument) throws XPathExpressionException {
        synchronized(parseDocument) {
            return paths(parseDocument);
        }
    }

    public static List<String> disectPidPaths( Element element) throws XPathExpressionException {
        synchronized(element) {
            return paths(element);
        }
    }


    public static List<String> disectOwnPidPaths( Document parseDocument) throws XPathExpressionException {
        synchronized(parseDocument) {
            return ownPidpath(parseDocument);
        }
    }

    public static List<String> disectOwnPidPaths( Element element) throws XPathExpressionException {
        synchronized(element) {
            return ownPidpath(element);
        }
    }

    
    private static List<String> paths(Node domn) throws XPathExpressionException {
        List<String> list = new ArrayList<>();
        NodeList paths = (NodeList) pidPathExpr().evaluate(domn, XPathConstants.NODESET);
        if (paths != null) {
            for (int i = 0,ll=paths.getLength(); i < ll; i++) {
                Node n = paths.item(i);
                String text = n.getTextContent();
                list.add(text.trim());
            }
            return list;
        }
        return new ArrayList<>();
    }

    private static List<String> ownPidpath(Node domn) throws XPathExpressionException {
        List<String> list = new ArrayList<>();
        NodeList paths = (NodeList) ownpidPathExpr().evaluate(domn, XPathConstants.NODESET);
        if (paths != null) {
            for (int i = 0,ll=paths.getLength(); i < ll; i++) {
                Node n = paths.item(i);
                String text = n.getTextContent();
                list.add(text.trim());
            }
            return list;
        }
        return new ArrayList<>();
    }
    
    /**
     * Disect pid from given solr document
     * @param parseDocument Parsed solr document
     * @return PID 
     * @throws XPathExpressionException cannot disect pid
     */
    public static String disectPid(Document parseDocument) throws XPathExpressionException {
        synchronized(parseDocument) {
            Node pidNode = (Node) docPidExpr().evaluate(parseDocument, XPathConstants.NODE);
            if (pidNode != null) {
                Element pidElm = (Element) pidNode;
                return pidElm.getTextContent().trim();
            }
            return null;
        }
    }
    
    public static String rootPid(Document parseDocument) throws XPathExpressionException {
        synchronized(parseDocument) {
            Node pidNode = (Node) rootPidExpr().evaluate(parseDocument, XPathConstants.NODE);
            if (pidNode != null) {
                Element pidElm = (Element) pidNode;
                return pidElm.getTextContent().trim();
            }
            return null;
        }
    }
    
    public static String rootTitle(Document parseDocument) throws XPathExpressionException {
        synchronized(parseDocument) {
            Node pidNode = (Node) rootTitleExpr().evaluate(parseDocument, XPathConstants.NODE);
            if (pidNode != null) {
                Element pidElm = (Element) pidNode;
                return pidElm.getTextContent().trim();
            }
            return null;
        }
    }

    public static String rootModel(Document parseDocument) throws XPathExpressionException {
        synchronized(parseDocument) {
            Node pidNode = (Node) rootModelExpr().evaluate(parseDocument, XPathConstants.NODE);
            if (pidNode != null) {
                Element pidElm = (Element) pidNode;
                return pidElm.getTextContent().trim();
            }
            return null;
        }
    }
    
    public static List<String> disectLicenses(Element topElem) {
        synchronized(topElem.getOwnerDocument()) {

            Element licensensesElement = XMLUtils.findElement(topElem, new XMLUtils.ElementsFilter() {
                @Override
                public boolean acceptElement(Element element) {
                    return (element.getNodeName().equals("arr") && element.getAttribute("name") != null && element.getAttribute("name").equals(
                            "licenses"
                    ));
                }
            });

            Element licensesOfAncestorsElement = XMLUtils.findElement(topElem, new XMLUtils.ElementsFilter() {
                @Override
                public boolean acceptElement(Element element) {
                    return (element.getNodeName().equals("arr") && element.getAttribute("name") != null && element.getAttribute("name").equals(
                            "licenses_of_ancestors"
                    ));
                }
            });

            if (licensensesElement != null || licensesOfAncestorsElement != null) {

                List<String> list = new ArrayList<>();
                NodeList childNodes = licensensesElement != null ? licensensesElement.getChildNodes() : licensesOfAncestorsElement.getChildNodes();
                for (int i = 0; i < childNodes.getLength(); i++) {
                    Node item = childNodes.item(i);
                    if (item.getNodeType() == Node.ELEMENT_NODE) {
                        list.add(item.getTextContent().toString());
                    }
                }
                return list;

            } else return new ArrayList<>();
        }

    }




    public static String disectDNNTFlag(Element topElem)  {
        return disectDNNTFlag(topElem, SolrFieldsMapping.getInstance().getDnntFlagField());
    }

    public static String disectDNNTFlag(Element topElem, String flag)  {
        synchronized(topElem.getOwnerDocument()) {
            Element foundElement = XMLUtils.findElement(topElem, new XMLUtils.ElementsFilter() {

                @Override
                public boolean acceptElement(Element element) {
                    return (element.getNodeName().equals("bool") && element.getAttribute("name") != null && element.getAttribute("name").equals(flag));
                }

            });
            if (foundElement != null) {
                return foundElement.getTextContent().trim();
            } else return null;
        }
    }




    public static String disectPid(Element topElem) throws XPathExpressionException {
        synchronized(topElem.getOwnerDocument()) {
            Element foundElement = XMLUtils.findElement(topElem, new XMLUtils.ElementsFilter() {

                @Override
                public boolean acceptElement(Element element) {
                    return (element.getNodeName().equals("str") && element.getAttribute("name") != null && element.getAttribute("name").equals("PID"));
                }
                
            });
            if (foundElement != null) {
                return foundElement.getTextContent().trim();
            } else return null;
        }
    }

    /**
     * Disect models path from given solr document
     * @param parseDocument Parsed solr document
     * @return model paths
     * @throws XPathExpressionException cannot disect models path
     */
    public static List<String> disectModelPaths(Document parseDocument) throws XPathExpressionException {
        synchronized(parseDocument) {
            List<String> list = new ArrayList<String>();
            NodeList pathNodes = (NodeList) modelPathExpr().evaluate(parseDocument, XPathConstants.NODESET);
            if (pathNodes != null) {
                for (int i = 0,ll=pathNodes.getLength(); i < ll; i++) {
                    Node n = pathNodes.item(i);
                    String text = n.getTextContent();
                    list.add(text.trim());
                }
                return list;
            }
            return new ArrayList<String>();
        }
    }


    public static String strValue(Document parsedDocument, String xpath) throws XPathExpressionException {
        synchronized(parsedDocument) {
            XPathExpression compiled = fact.newXPath().compile(xpath);
            String value = (String) compiled.evaluate(parsedDocument, XPathConstants.STRING);
            return value;
        }
    }

    /**
     * Disect fedora model from given solr document
     * @param parseDocument Parsed solr document
     * @return fedora model
     * @throws XPathExpressionException cannot disect fedora model
     */
    public static String disectFedoraModel(Document parseDocument) throws XPathExpressionException {
        synchronized(parseDocument) {
            Node fedoraModelNode = (Node) fedoraModelExpr().evaluate(parseDocument, XPathConstants.NODE);
            if (fedoraModelNode != null) {
                Element fedoraModelElm = (Element) fedoraModelNode;
                return fedoraModelElm.getTextContent().trim();
            }
            return null;
        }
    }



    /**
     * Disect parent PID from given solr document
     * @param parseDocument Parsed solr document
     * @return parent PID
     * @throws XPathExpressionException cannot disect parent PID
     */
    public static String disectParentPid(Document parseDocument) throws XPathExpressionException {
        synchronized(parseDocument) {
            Node parentPidNode = (Node) parentPidExpr().evaluate(parseDocument, XPathConstants.NODE);
            if (parentPidNode != null) {
                Element parentPidElm = (Element) parentPidNode;
                return parentPidElm.getTextContent().trim();
            }
            return null;
        }
    }

    /**
     * Disect root PID from given solr document
     * @param parseDocument Parsed solr document
     * @return root PID
     * @throws XPathExpressionException cannot disect root PID
     */
    public static String disectRootPid(Document parseDocument) throws XPathExpressionException {
        synchronized(parseDocument) {
            Node rootPidNode = (Node) rootPidExpr().evaluate(parseDocument, XPathConstants.NODE);
            if (rootPidNode != null) {
                Element rootPidElm = (Element) rootPidNode;
                return rootPidElm.getTextContent().trim();
            }
            return null;
        }
    }

    /**
     * Disect date from given solr document
     * @param parseDocument Parsed solr document
     * @return date
     * @throws XPathExpressionException cannot disect date
     */
    public static String disectDate(Document parseDocument) throws XPathExpressionException {
        synchronized(parseDocument) {
            Node dateNode = (Node) dateExpr().evaluate(parseDocument, XPathConstants.NODE);
            if (dateNode != null) {
                Element dateElm = (Element) dateNode;
                return dateElm.getTextContent().trim();
            }
            return null;
        }
    }
    public static Document getSolrDataInternalOffset(String query, String offset) throws IOException, ParserConfigurationException, SAXException {
        String solrHost = KConfiguration.getInstance().getSolrHost();
        String uri = solrHost +"/select?" +query+"&start="+offset+"&wt=xml";
        InputStream inputStream = RESTHelper.inputStream(uri, "<no_user>", "<no_pass>");
        Document parseDocument = XMLUtils.parseDocument(inputStream);
        return parseDocument;
    }

    public static Document getSolrDataInternal(String query) throws IOException, ParserConfigurationException, SAXException {
        String solrHost = KConfiguration.getInstance().getSolrHost();
        String uri = solrHost +"/select?" +query+"&wt=xml";
        InputStream inputStream = RESTHelper.inputStream(uri, "<no_user>", "<no_pass>");
        Document parseDocument = XMLUtils.parseDocument(inputStream);
        return parseDocument;
    }

    public static InputStream getSolrDataInternal(CloseableHttpClient client, String query, String format) throws IOException {
        String solrHost = KConfiguration.getInstance().getSolrHost();
        String uri = solrHost +"/select?" +query;
        if (!uri.endsWith("&")) {
            uri = uri + "&wt="+format;
        } else {
        	uri = uri+"wt="+format;
        }
        HttpGet httpGet = new HttpGet(uri);
        try (CloseableHttpResponse response = client.execute(httpGet)) {
            if (response.getCode() == SC_OK) {
                return response.getEntity().getContent();
            } else {
                throw new HttpResponseException(response.getCode(), response.getReasonPhrase());
            }
        }
    }


    public static String escapeQuery(String sourceQuery) {
        char[] chars = sourceQuery.toCharArray();
        StringBuilder builder = new StringBuilder();
        for(int i=0;i<chars.length;i++) {
            char ch = chars[i];
            switch (ch) {
                case '+':
                case '-':
                case '!':
                case  '(':
                case ')':
                case '{':
                case '}':
                case '[':
                case ']':
                case '^':
                case '\'':
                case '~':
                case '*':
                case '?':
                case ':':
                case '\\':
                case '/':
                    builder.append('\\').append(ch);
                break;
                case '&':
                    if (i<chars.length) {
                        ch = chars[i+1];
                        if (ch == '&') {
                            builder.append('\\').append("&&");
                            i++;
                        } else {
                            builder.append('\\').append('&');
                        }
                    } else {
                        builder.append('\\').append(ch);
                    }
                    break;
                case '|':
                    if (i<chars.length) {
                        ch = chars[i+1];
                        if (ch == '|') {
                            builder.append('\\').append("||");
                            i++;
                        } else {
                            builder.append('\\').append('|');
                        }
                    } else {
                        builder.append('\\').append(ch);
                    }

                    break;
                default:
                    builder.append(ch);

            }

        }
        return builder.toString();

    }

    //reads and closes entity's content stream
    public static InputStream readContentAndProvideThroughBufferedStream(HttpEntity entity) throws IOException {
        try (InputStream src = entity.getContent()) {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            IOUtils.copy(src, bos);
            return new ByteArrayInputStream(bos.toByteArray());
        }
    }

    /**
     * @param query for example: q=model%3Amonograph&fl=pid%2Ctitle.search&start=0&sort=created+desc&fq=model%3Aperiodical+OR+model%3Amonograph&rows=24&hl.fragsize=20
     *              i.e. url encoded and without query param wt
     */
    public static InputStream requestWithTermsReturningStream(CloseableHttpClient client, String solrHost, String query, String type) throws IOException {
        String url = String.format("%s/terms?%s&wt=%s", solrHost, query, type);
        HttpGet httpGet = new HttpGet(url);
        try (CloseableHttpResponse response = client.execute(httpGet)) {
            if (response.getCode() == SC_OK) {
                return readContentAndProvideThroughBufferedStream(response.getEntity());
            } else {
                throw new HttpResponseException(response.getCode(), response.getReasonPhrase());
            }
        }
    }

    /**
     * @param query for example: q=model%3Amonograph&fl=pid%2Ctitle.search&start=0&sort=created+desc&fq=model%3Aperiodical+OR+model%3Amonograph&rows=24&hl.fragsize=20
     *              i.e. url encoded and without query param wt
     */
    public static InputStream requestWithSelectReturningStream(CloseableHttpClient client, String solrHost, String query, String type, ApiCallEvent event) throws IOException {
        List<Triple<String, Long, Long>> eventGranularity = event != null ?  event.getGranularTimeSnapshots() : null;
        String url = String.format("%s/select?%s&wt=%s", solrHost, query, type);
        long start = System.currentTimeMillis();
        HttpGet httpGet = new HttpGet(url);
        try (CloseableHttpResponse response = client.execute(httpGet)) {
            if (response.getCode() == SC_OK) {
                long end = System.currentTimeMillis();
                if (eventGranularity != null)  eventGranularity.add(Triple.of("http/solr",start,end));
                return readContentAndProvideThroughBufferedStream(response.getEntity());
            } else {
                throw new HttpResponseException(response.getCode(), response.getReasonPhrase());
            }
        }
    }

    public static String requestWithSelectReturningString(CloseableHttpClient client, String solrHost, String query, String type, ApiCallEvent event) throws IOException {
        List<Triple<String, Long, Long>> eventGranularity = event != null ?  event.getGranularTimeSnapshots() : null;
        long start = System.currentTimeMillis();
        InputStream in = requestWithSelectReturningStream(client, solrHost, query, type,event);
        long end = System.currentTimeMillis();
        if (eventGranularity != null)  eventGranularity.add(Triple.of("http/solr",start,end));
        BufferedReader streamReader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
        StringBuilder responseStrBuilder = new StringBuilder();
        String inputStr;
        while ((inputStr = streamReader.readLine()) != null) {
            responseStrBuilder.append(inputStr);
        }
        return responseStrBuilder.toString();
    }

    public static InputStream schema(CloseableHttpClient client, String hostWithCollection, ApiCallEvent event) throws IOException {
        List<Triple<String, Long, Long>> eventGranularity = event != null ?  event.getGranularTimeSnapshots() : null;
        String url = String.format("%s/schema", hostWithCollection);
        long start = System.currentTimeMillis();
        HttpGet httpGet = new HttpGet(url);
        try (CloseableHttpResponse response = client.execute(httpGet)) {
            if (response.getCode() == SC_OK) {
                long end = System.currentTimeMillis();
                if (eventGranularity != null) eventGranularity.add(Triple.of("http/solr/schema",start,end));
                return readContentAndProvideThroughBufferedStream(response.getEntity());
            } else {
                throw new HttpResponseException(response.getCode(), response.getReasonPhrase());
            }
        }
    }

    public static InputStream fields(CloseableHttpClient client, String hostWithCollection, ApiCallEvent event) throws IOException {
        List<Triple<String, Long, Long>> eventGranularity = event != null ? event.getGranularTimeSnapshots() : null;
        String url = String.format("%s/schema/fields", hostWithCollection);
        long start = System.currentTimeMillis();
        HttpGet httpGet = new HttpGet(url);
        try (CloseableHttpResponse response = client.execute(httpGet)) {
            if (response.getCode() == SC_OK) {
                long end = System.currentTimeMillis();
                if (eventGranularity != null)  eventGranularity.add(Triple.of("http/solr/fields",start,end));
                return readContentAndProvideThroughBufferedStream(response.getEntity());
            } else {
                throw new HttpResponseException(response.getCode(), response.getReasonPhrase());
            }
        }
    }
}
