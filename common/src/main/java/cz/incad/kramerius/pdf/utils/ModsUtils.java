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
import java.util.HashMap;
import java.util.Map;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.FedoraNamespaceContext;
import cz.incad.kramerius.FedoraNamespaces;
import cz.incad.kramerius.utils.XMLUtils;
import java.util.ArrayList;
import javax.swing.JOptionPane;

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
    

    public static Map<String, String> getTitleInfo(String pid, FedoraAccess fedoraAccess) throws XPathExpressionException, IOException {
        Map<String, String> map = new HashMap<String, String>();
        Document biblioMods = fedoraAccess.getBiblioMods(pid);
        XPath xpath = FACTORY.newXPath();
        xpath.setNamespaceContext(new FedoraNamespaceContext());
        XPathExpression expr = xpath.compile("//mods:titleInfo/mods:title");
        NodeList set = (NodeList) expr.evaluate(biblioMods, XPathConstants.NODESET);
        for (int i = 0,ll=set.getLength(); i < ll; i++) {
            Node node = set.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element elm = (Element) node;
                if (elm.hasAttributeNS(FedoraNamespaces.BIBILO_MODS_URI, "type")) {
                    String type = elm.getAttributeNS(FedoraNamespaces.BIBILO_MODS_URI,"type");
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
    
    
    public static ArrayList<String> languagesFromMods(org.w3c.dom.Document mods) throws XPathExpressionException, IOException {
        ArrayList<String> languages  = new ArrayList<String>();
        XPath xpath = FACTORY.newXPath();
        xpath.setNamespaceContext(new FedoraNamespaceContext());
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
    
    
    
    
}
