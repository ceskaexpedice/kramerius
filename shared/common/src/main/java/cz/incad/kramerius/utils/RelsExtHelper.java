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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.lang3.tuple.Pair;
import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.FedoraNamespaceContext;
import cz.incad.kramerius.FedoraNamespaces;
import cz.incad.kramerius.KrameriusModels;
import cz.incad.kramerius.repository.KrameriusRepositoryApi;
import cz.incad.kramerius.repository.KrameriusRepositoryApi.KnownRelations;
import cz.incad.kramerius.utils.database.SQLFilter.Tripple;
import cz.incad.kramerius.utils.pid.LexerException;
import cz.incad.kramerius.utils.pid.PIDParser;

public class RelsExtHelper {

    public static final Logger LOGGER = Logger.getLogger(RelsExtHelper.class.getName());

    
    private RelsExtHelper() {}
    
    
    public static Element getRELSEXTFromGivenFOXML(Element foxmlElement) {
        List<Element> elms = XMLUtils.getElementsRecursive(foxmlElement, (elm)->{
            if (elm.getLocalName().equals("datastream")) {
                String id = elm.getAttribute("ID");
                return id.equals(FedoraUtils.RELS_EXT_STREAM);
            } return false;
        });
        if (elms.size() == 1) {
            return elms.get(0);
        } else return null;
    }
    

    /** Get model 
     * @throws LexerException */
    public static String getModel(Element relsExt) throws XPathExpressionException, LexerException {
        //<hasModel xmlns="
        Element foundElement = XMLUtils.findElement(relsExt, "hasModel", FedoraNamespaces.FEDORA_MODELS_URI);
        if (foundElement != null) {
            String sform = foundElement.getAttributeNS(FedoraNamespaces.RDF_NAMESPACE_URI, "resource");
            PIDParser pidParser = new PIDParser(sform);
            pidParser.disseminationURI();
            return pidParser.getObjectId();
        } else {
            throw new IllegalArgumentException("cannot find model of given document");
        }
    }
    

    
    public static Element getRDFDescriptionElement(Element relsExt) throws XPathExpressionException, LexerException {
        Element foundElement = XMLUtils.findElement(relsExt, "Description", FedoraNamespaces.RDF_NAMESPACE_URI);
        return foundElement;
    }
    
    
    

    
    /** Returns replicatedFrom url from given  RELS-EXT element*/
    public static String getReplicatedFromUrl(String uuid, FedoraAccess fedoraAccess) throws IOException, XPathExpressionException {
        Document relsExt = fedoraAccess.getRelsExt(uuid);
        return getReplicatedFromUrl(relsExt);
    }

    /** Returns replicatedFrom url from given  RELS-EXT element */
    private static String getReplicatedFromUrl(Document relsExt) throws XPathExpressionException {
        XPathFactory xpfactory = XPathFactory.newInstance();
        XPath xpath = xpfactory.newXPath();
        xpath.setNamespaceContext(new FedoraNamespaceContext());
        XPathExpression expr = xpath.compile("//kramerius:replicatedFrom/text()");
        Object tiles = expr.evaluate(relsExt, XPathConstants.NODE);
        if (tiles != null) return ((Text) tiles).getData();
        else return null;
    }

    /** Returns replicatedFrom url from given  RELS-EXT element */
    public static String getRelsExtTilesUrl(String uuid, FedoraAccess fedoraAccess) throws IOException, XPathExpressionException {
        Document relsExt = fedoraAccess.getRelsExt(uuid);
        return getRelsExtTilesUrl(relsExt.getDocumentElement());
    }


    /** Returns tiles url  from given RELS-EXT element */
    public static String getRelsExtTilesUrl(Element reslExtDoc) throws IOException, XPathExpressionException {
        XPathFactory xpfactory = XPathFactory.newInstance();
        XPath xpath = xpfactory.newXPath();
        xpath.setNamespaceContext(new FedoraNamespaceContext());
        XPathExpression expr = xpath.compile("//kramerius:tiles-url/text()");
        Object tiles = expr.evaluate(reslExtDoc, XPathConstants.NODE);
        if (tiles != null) {
            String data = ((Text) tiles).getData();
            return data != null ? data.trim() : null;
        }
        else return null;
    }

    public static String getRelsExtTilesUrl(Document reslExtDoc) throws IOException, XPathExpressionException {
        return getRelsExtTilesUrl(reslExtDoc.getDocumentElement());
    }

    public static String getDonator(Document reslExtDoc) throws IOException, XPathExpressionException {
        return getDonator(reslExtDoc.getDocumentElement());
    }
    
    /** Returns donator label  from given RELS-EXT element */
    public static String getDonator(Element reslExtDoc) throws IOException, XPathExpressionException {
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
    
    
    public static List<String> getLicenses(Element relsExt) throws XPathExpressionException, LexerException {
        List<Element> elms = XMLUtils.getElementsRecursive(relsExt, (elm)->{
            return (elm.getLocalName().equals("license"));
        });
        List<String> collect = elms.stream().map(Element::getTextContent).collect(Collectors.toList());
        return collect;
    }    

    public static List<String> getContainsLicenses(Element relsExt) throws XPathExpressionException, LexerException {
        List<Element> elms = XMLUtils.getElementsRecursive(relsExt, (elm)->{
            return (elm.getLocalName().equals("containsLicense"));
        });
        List<String> collect = elms.stream().map(Element::getTextContent).collect(Collectors.toList());
        return collect;
    }    

    
    public static List<Pair<String,String>> getRelations(Element relsExt) {

        //    private static Set<KrameriusModels> EXCLUDE_RELATIONS = EnumSet.of(
//      KrameriusModels.DONATOR, KrameriusModels.INTERNALPART);
      //KrameriusModels.DONATOR, KrameriusModels.INTERNALPART, KrameriusModels.PAGE);

        
        List<Pair<String,String>> pairs = new ArrayList<>();
        List<String> names = Arrays.stream(KrameriusRepositoryApi.KnownRelations.values()).map(KrameriusRepositoryApi.KnownRelations::toString).collect(Collectors.toList());
        List<Element> elms = XMLUtils.getElementsRecursive(relsExt,  new XMLUtils.ElementsFilter() {

            @Override
            public boolean acceptElement(Element element) {
                String namespaceUri = element.getNamespaceURI();
                if (namespaceUri.equals(FedoraNamespaces.KRAMERIUS_URI)) {
                    String nodeName = element.getLocalName();
                    return names.contains(nodeName);
                }
                return false;
            }
            
        });
        
        
        elms.stream().forEach(elm-> {
          try {
            String attrVal = elm.getAttributeNS(FedoraNamespaces.RDF_NAMESPACE_URI, "resource");
              PIDParser pidParser = new PIDParser(attrVal);
              pidParser.disseminationURI();
              String objectPid = pidParser.getObjectPid();
              pairs.add(Pair.of(elm.getLocalName(), objectPid));
            } catch (DOMException | LexerException e) {
                LOGGER.log(Level.SEVERE,e.getMessage(),e);
            }
        });
        
        return pairs;
    }
    
    

    public synchronized static void addRDFLiteral(Element relsExt, String license, String elmName)
            throws XPathExpressionException, LexerException {
        Element rdfDescriptionElement = getRDFDescriptionElement(relsExt);
        if (rdfDescriptionElement != null) {
            Document document = rdfDescriptionElement.getOwnerDocument();
            Element containsLicense = document.createElementNS(FedoraNamespaces.ONTOLOGY_RELATIONSHIP_NAMESPACE_URI, elmName);
            containsLicense.setTextContent(license);
            rdfDescriptionElement.appendChild(containsLicense);
        }
    }
    

}
