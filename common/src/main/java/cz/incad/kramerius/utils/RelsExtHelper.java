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
package cz.incad.kramerius.utils;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.FedoraNamespaceContext;
import cz.incad.kramerius.FedoraNamespaces;
import cz.incad.kramerius.utils.pid.LexerException;
import cz.incad.kramerius.utils.pid.PIDParser;

public class RelsExtHelper {

    public static final Logger LOGGER = Logger.getLogger(RelsExtHelper.class.getName());

    
    
    public static String getReplicatedFromUrl(String uuid, FedoraAccess fedoraAccess) throws IOException, XPathExpressionException {
        Document relsExt = fedoraAccess.getRelsExt(uuid);
        XPathFactory xpfactory = XPathFactory.newInstance();
        XPath xpath = xpfactory.newXPath();
        xpath.setNamespaceContext(new FedoraNamespaceContext());
        XPathExpression expr = xpath.compile("//kramerius:replicatedFrom/text()");
        Object tiles = expr.evaluate(relsExt, XPathConstants.NODE);
        if (tiles != null) return ((Text) tiles).getData();
        else return null;
    }

    public static String getRelsExtTilesUrl(String uuid, FedoraAccess fedoraAccess) throws IOException, XPathExpressionException {
        Document relsExt = fedoraAccess.getRelsExt(uuid);
        XPathFactory xpfactory = XPathFactory.newInstance();
        XPath xpath = xpfactory.newXPath();
        xpath.setNamespaceContext(new FedoraNamespaceContext());
        XPathExpression expr = xpath.compile("//kramerius:tiles-url/text()");
        Object tiles = expr.evaluate(relsExt, XPathConstants.NODE);
        if (tiles != null) return ((Text) tiles).getData();
        else return null;
    }

    public static String getRelsExtTilesUrl(Document reslExtDoc) throws IOException, XPathExpressionException {
        XPathFactory xpfactory = XPathFactory.newInstance();
        XPath xpath = xpfactory.newXPath();
        xpath.setNamespaceContext(new FedoraNamespaceContext());
        XPathExpression expr = xpath.compile("//kramerius:tiles-url/text()");
        Object tiles = expr.evaluate(reslExtDoc, XPathConstants.NODE);
        if (tiles != null) return ((Text) tiles).getData();
        else return null;
    }

    public static String getDonator(Document reslExtDoc) throws IOException, XPathExpressionException {
        XPathFactory xpfactory = XPathFactory.newInstance();
        XPath xpath = xpfactory.newXPath();
        xpath.setNamespaceContext(new FedoraNamespaceContext());
        XPathExpression expr = xpath.compile("//kramerius:hasDonator");
        Object donator = expr.evaluate(reslExtDoc, XPathConstants.NODE);
        if (donator != null) {
            Element elm =  (Element) donator;
            Attr ref = elm.getAttributeNodeNS(FedoraNamespaces.RDF_NAMESPACE_URI, "resource");
            if (ref != null) {
                try {
                    PIDParser pidParser = new PIDParser(ref.getValue());
                    pidParser.disseminationURI();
                    return pidParser.getObjectPid();
                } catch (LexerException e) {
                    LOGGER.log(Level.SEVERE,e.getMessage(),e);
                    return null;
                }
            } else return null;
        } else return null;
    }


    public static final String CACHE_RELS_EXT_LITERAL = "kramerius4://deepZoomCache";
    
    
}
