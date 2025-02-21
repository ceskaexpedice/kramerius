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
package cz.incad.kramerius.pdf.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.ceskaexpedice.akubra.AkubraRepository;
import org.ceskaexpedice.akubra.core.repository.KnownDatastreams;
import org.ceskaexpedice.akubra.core.repository.RepositoryNamespaceContext;
import org.ceskaexpedice.akubra.core.repository.RepositoryNamespaces;
import org.ceskaexpedice.akubra.utils.DomUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


public class ModsUtils {

    static final XPathFactory FACTORY = XPathFactory.newInstance();

    
    
//    public static BiblioMods biblioMods(String pid, FedoraAccess fa) throws IOException {
//        BiblioMods mods = new BiblioMods(fa.getKrameriusModelName(pid));
//        Document modsDocument = fa.getBiblioMods(pid);
//        NodeList nlist = modsDocument.getElementsByTagNameNS(FedoraNamespaces.BIBILO_MODS_URI, "mods");
//        
//        for (int i = 0, ll = nlist.getLength(); i < ll; i++) {
//            Node item = nlist.item(i);
//            if (item.getNodeType() == Node.ELEMENT_NODE) {
//                mods.init((Element) item);
//            }
//        }
//        return mods;
//    }
    

    public static Map<String, String> getTitleInfo(String pid, AkubraRepository akubraRepository) throws XPathExpressionException, IOException {
        Map<String, String> map = new HashMap<String, String>();
        InputStream inputStream = akubraRepository.getDatastreamContent(pid, KnownDatastreams.BIBLIO_MODS.toString());
        Document biblioMods = DomUtils.streamToDocument(inputStream);
        XPath xpath = FACTORY.newXPath();
        xpath.setNamespaceContext(new RepositoryNamespaceContext());
        XPathExpression expr = xpath.compile("//mods:titleInfo/mods:title");
        NodeList set = (NodeList) expr.evaluate(biblioMods, XPathConstants.NODESET);
        for (int i = 0,ll=set.getLength(); i < ll; i++) {
            Node node = set.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element elm = (Element) node;
                if (elm.hasAttributeNS(RepositoryNamespaces.BIBILO_MODS_URI, "type")) {
                    String type = elm.getAttributeNS(RepositoryNamespaces.BIBILO_MODS_URI,"type");
                    map.put(type, elm.getTextContent().trim());
                } else {
                    if (!map.containsKey("default")) {
                        map.put("default", elm.getTextContent().trim());
                    }
                }
            }
        }
        return map;
    }
    
    
    public static List<String> languagesFromMods(org.w3c.dom.Document mods) throws XPathExpressionException, IOException {
        ArrayList<String> languages  = new ArrayList<String>();
        XPath xpath = FACTORY.newXPath();
        xpath.setNamespaceContext(new RepositoryNamespaceContext());
        XPathExpression expr = xpath.compile("//mods:language/mods:languageTerm");
        NodeList set = (NodeList) expr.evaluate(mods, XPathConstants.NODESET);
        for (int i = 0,ll=set.getLength(); i < ll; i++) {
            Node languageNode = set.item(i);
            if (languageNode.getNodeType() == Node.ELEMENT_NODE) {
                String languageText = languageNode.getTextContent();
                languages.add(languageText);
            }
        }
        return languages;
    }


    public static Map<String, List<String>> identifiersFromMods(org.w3c.dom.Document mods, String ... expectedTypes) throws XPathExpressionException, IOException {
        List<String> collectedExpectedTypes = Arrays.stream(expectedTypes).collect(Collectors.toList());

        Map<String, List<String>> map = new HashMap<>();
        XPath xpath = FACTORY.newXPath();
        xpath.setNamespaceContext(new RepositoryNamespaceContext());
        XPathExpression expr = xpath.compile("//mods:identifier");

        NodeList set = (NodeList) expr.evaluate(mods, XPathConstants.NODESET);
        for (int i = 0,ll=set.getLength(); i < ll; i++) {
            Node identNode = set.item(i);
            if (identNode.getNodeType() == Node.ELEMENT_NODE) {
                String type = ((Element) identNode).getAttribute("type");
                boolean add = collectedExpectedTypes.isEmpty() || collectedExpectedTypes.contains(type);
                if (add) {
                    if (!map.containsKey(type)) { map.put(type, new ArrayList<>());  }
                    map.get(type).add(identNode.getTextContent());
                }
            }
        }
        return map;
    }
    
    
    
}
