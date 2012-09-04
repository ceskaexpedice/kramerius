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

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import cz.incad.kramerius.SolrAccess;
import cz.incad.kramerius.utils.RESTHelper;
import cz.incad.kramerius.utils.XMLUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;

/**
 * Utility helper class for SolrAccess
 * @see SolrAccess
 * @author pavels
 */
public class SolrUtils   {

    /** PID query */
    public static final String UUID_QUERY="q=PID:";
    /** Handle query */
    public static final String HANDLE_QUERY="q=handle:";
    
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
    public static XPathExpression pidExpr() throws XPathExpressionException {
        XPathExpression pidExpr = fact.newXPath().compile("//str[@name='PID']");
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
     * Disects pid paths from given parsed solr document
     * @return pid paths
     * @throws XPathExpressionException cannot disect pid paths
     */
    public static List<String> disectPidPaths( Document parseDocument) throws XPathExpressionException {
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
    
    /**
     * Disect pid from given solr document
     * @param parseDocument Parsed solr document
     * @return PID 
     * @throws XPathExpressionException cannot disect pid
     */
    public static String disectPid(Document parseDocument) throws XPathExpressionException {
        Node pidNode = (Node) pidExpr().evaluate(parseDocument, XPathConstants.NODE);
        if (pidNode != null) {
            Element pidElm = (Element) pidNode;
            return pidElm.getTextContent().trim();
        }
        return null;
    }

    /**
     * Disect models path from given solr document
     * @param parseDocument Parsed solr document
     * @return model paths
     * @throws XPathExpressionException cannot disect models path
     */
    public static List<String> disectModelPaths(Document parseDocument) throws XPathExpressionException {
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


    public static Document getSolrDataInternal(String query) throws IOException, ParserConfigurationException, SAXException {
        String solrHost = KConfiguration.getInstance().getSolrHost();
        String uri = solrHost +"/select/?" +query;
        InputStream inputStream = RESTHelper.inputStream(uri, "<no_user>", "<no_pass>");
        Document parseDocument = XMLUtils.parseDocument(inputStream);
        return parseDocument;
    }


}
