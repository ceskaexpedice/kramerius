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

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import cz.incad.kramerius.SolrAccess;
import cz.incad.kramerius.utils.RESTHelper;
import cz.incad.kramerius.utils.XMLUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;

import static org.apache.http.HttpStatus.SC_OK;

/**
 * Utility helper class for SolrAccess
 * @see SolrAccess
 * @author pavels
 */
public class SolrUtils   {
    
    public static final Logger LOGGER = Logger.getLogger(SolrUtils.class.getName());

    /** PID query */
    public static final String UUID_QUERY="q=PID:";
    /** Handle query */
    public static final String HANDLE_QUERY="q=handle:";
    /** Parent query */
    public static final String PARENT_QUERY="q=parent_pid:";
    
    // factory instance
    static XPathFactory fact =XPathFactory.newInstance();
    
    /** 
     * Conscturcts XPath for disecting pid path 
     * @return Compiled XPath expression
     * @throws XPathExpressionException Cannot compile xpath
     */
    public static XPathExpression pidPathExpr() throws XPathExpressionException {
        XPathExpression pidPathExpr = fact.newXPath().compile("//arr[@name='pid_path']/str");
        return pidPathExpr;
    }

    /**
     * Constructs XPath for disecting PID
     * @return Compiled XPath expression
     * @throws XPathExpressionException Cannot compile xpath
     */
    public static XPathExpression docPidExpr() throws XPathExpressionException {
        XPathExpression pidExpr = fact.newXPath().compile("//str[@name='PID']");
        return pidExpr;
    }

    public static XPathExpression elmPidExpr() throws XPathExpressionException {
        XPathExpression pidExpr = fact.newXPath().compile("str[@name='PID']");
        return pidExpr;
    }

    /**
     * Constructs XPath for disecting model path
     * @return Compiled XPath expression
     * @throws XPathExpressionException Cannot compile xpath
     */
    public static XPathExpression modelPathExpr() throws XPathExpressionException {
        XPathExpression pathExpr = fact.newXPath().compile("//arr[@name='model_path']/str");
        return pathExpr;
    }
    
    /**
     * Constructs XPath for disecting fedora model
     * @return Compiled XPath expression
     * @throws XPathExpressionException Cannot compile xpath
     */
    public static XPathExpression fedoraModelExpr() throws XPathExpressionException {
        XPathExpression fedoraModelExpr = fact.newXPath().compile("//str[@name='fedora.model']");
        return fedoraModelExpr;
    }
    
    /**
     * Constructs XPath for disecting parent PID
     * @return Compiled XPath expression
     * @throws XPathExpressionException Cannot compile xpath
     */
    public static XPathExpression parentPidExpr() throws XPathExpressionException {
        XPathExpression pidExpr = fact.newXPath().compile("//arr[@name='parent_pid']/str");
        return pidExpr;
    }
    
    /**
     * Constructs XPath for disecting date
     * @return Compiled XPath expression
     * @throws XPathExpressionException Cannot compile xpath
     */
    public static XPathExpression dateExpr() throws XPathExpressionException {
        XPathExpression dateExpr = fact.newXPath().compile("//str[@name='datum_str']");
        return dateExpr;
    }
    
    /**
     * Disects pid paths from given parsed solr document
     * @return pid paths
     * @throws XPathExpressionException cannot disect pid paths
     */
    public static List<String> disectPidPaths( Document parseDocument) throws XPathExpressionException {
        synchronized(parseDocument) {
            List<String> list = new ArrayList<String>();
            NodeList paths = (org.w3c.dom.NodeList) pidPathExpr().evaluate(parseDocument, XPathConstants.NODESET);
            if (paths != null) {
                for (int i = 0,ll=paths.getLength(); i < ll; i++) {
                    Node n = paths.item(i);
                    String text = n.getTextContent();
                    list.add(text.trim());
                }
                return list;
            }
            return new ArrayList<String>();
        }
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
        String uri = solrHost +"/select?" +query+"&start="+offset;
        InputStream inputStream = RESTHelper.inputStream(uri, "<no_user>", "<no_pass>");
        Document parseDocument = XMLUtils.parseDocument(inputStream);
        return parseDocument;
    }

    public static Document getSolrDataInternal(String query) throws IOException, ParserConfigurationException, SAXException {
        String solrHost = KConfiguration.getInstance().getSolrHost();
        String uri = solrHost +"/select?" +query;
        InputStream inputStream = RESTHelper.inputStream(uri, "<no_user>", "<no_pass>");
        Document parseDocument = XMLUtils.parseDocument(inputStream);
        return parseDocument;
    }

    public static InputStream getSolrDataInternal(String query, String format) throws IOException {
        String solrHost = KConfiguration.getInstance().getSolrHost();
        String uri = solrHost +"/select?" +query;
        if (!uri.endsWith("&")) {
            uri = uri + "&wt="+format;
        } else {
        	uri = uri+"wt="+format;
        }
        HttpGet httpGet = new HttpGet(uri);
        CloseableHttpClient client = HttpClients.createDefault();
        HttpResponse response = client.execute(httpGet);
        if (response.getStatusLine().getStatusCode() == SC_OK) {
            return response.getEntity().getContent();
        } else {
            throw new HttpResponseException(response.getStatusLine().getStatusCode(), response.getStatusLine().getReasonPhrase());
        }

    }

    public static InputStream getSolrTermsInternal(String query, String format) throws IOException, ParserConfigurationException, SAXException {
        String solrHost = KConfiguration.getInstance().getSolrHost();
        String uri = solrHost +"/terms?" +query;
        if (!uri.endsWith("&")) {
            uri = uri + "&wt="+format;
        } else {
        	uri = uri+"wt="+format;
        }
        InputStream inputStream = RESTHelper.inputStream(uri, "<no_user>", "<no_pass>");
        return inputStream;
    }
    
    
    
    

    
}
