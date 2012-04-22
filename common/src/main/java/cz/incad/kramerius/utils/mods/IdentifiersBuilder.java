/*
 * Copyright (C) 2012 Pavel Stastny
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
package cz.incad.kramerius.utils.mods;

import java.util.List;
import java.util.Map;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

import cz.incad.kramerius.FedoraNamespaceContext;

/**
 * Vytahuje vsechny identifikatory
 * @author pavels
 */
public class IdentifiersBuilder extends AbstractBuilder {
    
    public static final String MODS_ISSN="mods:issn";
    public static final String MODS_ISBN="mods:isbn";
    public static final String MODS_SICI="mods:sici";
    public static final String MODS_CODEN="mods:coden";

//    <mods:modsCollection xmlns:mods="http://www.loc.gov/mods/v3">
//    <mods:mods version="3.3">
//        <mods:identifier type="urn">ae876087-435d-11dd-b505-00145e5790ea</mods:identifier>
//        <mods:identifier type="sici"></mods:identifier>
//        <mods:identifier type="issn">1214-1240</mods:identifier>
//        <mods:identifier type="coden"></mods:identifier>

    @Override
    public void build(Document document, Map<String, List<String>> map, String model) throws XPathExpressionException {
        XPath xpath = getFactory().newXPath();
        xpath.setNamespaceContext(new FedoraNamespaceContext());
        
        XPathExpression expr = xpath.compile("//mods:identifier[@type='issn']/text()");
        Node node = (Node) expr.evaluate(document, XPathConstants.NODE);
        if (node != null) {
            String data = ((Text)node).getData();
            if (data != null && (!data.trim().equals(""))) {
                add(MODS_ISSN, data, map);
            }
        }        

        expr = xpath.compile("//mods:identifier[@type='isbn']/text()");
        node = (Node) expr.evaluate(document, XPathConstants.NODE);
        if (node != null) {
            String data = ((Text)node).getData();
            if (data != null && (!data.trim().equals(""))) {
                add(MODS_ISBN, data, map);
            }
        }        

        expr = xpath.compile("//mods:identifier[@type='sici']/text()");
        node = (Node) expr.evaluate(document, XPathConstants.NODE);
        if (node != null) {
            String data = ((Text)node).getData();
            if (data != null && (!data.trim().equals(""))) {
                add(MODS_SICI, data, map);
            }
        }        

        expr = xpath.compile("//mods:identifier[@type='coden']/text()");
        node = (Node) expr.evaluate(document, XPathConstants.NODE);
        if (node != null) {
            String data = ((Text)node).getData();
            if (data != null && (!data.trim().equals(""))) {
                add(MODS_CODEN, data, map);
            }
        }        
    }
}
