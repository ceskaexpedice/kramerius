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
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

import cz.incad.kramerius.FedoraNamespaceContext;

public class TitleBuilder extends AbstractBuilder {

    public static final String MODS_TITLE="mods:title";
    public static final String MODS_SUBTITLE="mods:subTitle";
    public static final String MODS_PARTNAME="mods:partName";
    
    public static final String MODS_ALTERNATIVE_TITLE="mods:alternativetitle";
    public static final String MODS_ALTERNATIVE_SUBTITLE="mods:alternativeSubtitle";
    public static final String MODS_ALTERNATIVE_PARTNAME="mods:partName";
    
//    <mods:modsCollection xmlns:mods="http://www.loc.gov/mods/v3">
//    <mods:mods version="3.3">
//        <mods:titleInfo>
//            <mods:title>Národní listy</mods:title>
//            <mods:subTitle></mods:subTitle>
//            <mods:partName></mods:partName>
//        </mods:titleInfo>
    
//    <mods:titleInfo type="alternative">
//    <mods:title>Kniha zlatá, anebo, Nový Zvěstovatel všeho
//dobrého a užitečného pro Národ Slovenský</mods:title>
//</mods:titleInfo>

    
    @Override
    public void build(Document document, Map<String, List<String>> map, String model) throws XPathExpressionException {
        XPath xpath = getFactory().newXPath();
        xpath.setNamespaceContext(new FedoraNamespaceContext());
        
        XPathExpression expr = xpath.compile("//mods:titleInfo/mods:title/text()");
        Node node = (Node) expr.evaluate(document, XPathConstants.NODE);
        if (node != null) {
            add(MODS_TITLE, ((Text)node).getData(), map);
            add(DEFAULT_TITLE,((Text)node).getData(),map);
        }

        expr = xpath.compile("//mods:titleInfo/mods:subTitle/text()");
        node = (Node) expr.evaluate(document,XPathConstants.NODE);
        if (node != null) {
            add(MODS_SUBTITLE, ((Text)node).getData(), map);
        }

        expr = xpath.compile("//mods:titleInfo/mods:partName/text()");
        node = (Node) expr.evaluate(document,XPathConstants.NODE);
        if (node != null) {
            add(MODS_PARTNAME, ((Text)node).getData(), map);
        }
        
        
        expr = xpath.compile("//mods:titleInfo[@type='alternative']/mods:title/text()");
        node = (Node) expr.evaluate(document, XPathConstants.NODE);
        if (node != null) {
            add(MODS_ALTERNATIVE_TITLE, ((Text)node).getData(), map);
        }

        expr = xpath.compile("//mods:titleInfo[@type='alternative']/mods:subTitle/text()");
        node = (Node) expr.evaluate(document,XPathConstants.NODE);
        if (node != null) {
            add(MODS_ALTERNATIVE_SUBTITLE, ((Text)node).getData(), map);
        }

        expr = xpath.compile("//mods:titleInfo[@type='alternative']/mods:partName/text()");
        node = (Node) expr.evaluate(document,XPathConstants.NODE);
        if (node != null) {
            add(MODS_ALTERNATIVE_PARTNAME, ((Text)node).getData(), map);
        }
    }
}
