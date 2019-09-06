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

public class PeriodicalVolumeNumberBuilder extends AbstractBuilder {

    private static final String APPLICABLE_MODEL = "periodicalvolume";

    public static final String MODS_VOLUMENUMBER="mods:volumeNumber";
    public static final String MODS_DATE="mods:periodicalVolumeDate";

    
//    <mods:modsCollection xmlns:mods="http://www.loc.gov/mods/v3">
//    <mods:mods version="3.3">
//        <mods:identifier type="urn">b2f18fb0-91f6-11dc-9f72-000d606f5dc6</mods:identifier>

//    <mods:part>
//    <mods:detail type="volume">
//        <mods:number>81</mods:number>
//    </mods:detail>
//    <mods:date>1941</mods:date>
//    <mods:text>nekvalitní a poškozená předloha; č. 1 večerní
//vydání není k dispozici; č. 2 ranní vydání není k
//dispozici; č. 120 večerní vydání není k dispozici; č.
//121 ranní vydání není k dispozici; od č. 134 ranní
//vydání není k dispozici;</mods:text>
//</mods:part>

    @Override
    public void build(Document document, Map<String, List<String>> map, String model) throws XPathExpressionException {
        XPath xpath = getFactory().newXPath();
        xpath.setNamespaceContext(new FedoraNamespaceContext());
        
        XPathExpression expr = xpath.compile("//mods:part/mods:detail[@type='volume']/mods:number/text()");
        Node node = (Node) expr.evaluate(document, XPathConstants.NODE);
        if (node == null){
            expr = xpath.compile("//mods:titleInfo/mods:partNumber/text()");
            node = (Node) expr.evaluate(document, XPathConstants.NODE);
        }

        if (node != null) {
            add(MODS_VOLUMENUMBER, ((Text)node).getData(), map);
            if (APPLICABLE_MODEL.equals(model)) {
                add(DEFAULT_TITLE, ((Text)node).getData(), map);
            }

        }        
        
        if (APPLICABLE_MODEL.equals(model)) {
            expr = xpath.compile("//mods:part/mods:date/text()");
            node = (Node) expr.evaluate(document, XPathConstants.NODE);
            if (node != null) {
                add(MODS_DATE, ((Text)node).getData(), map);
            }        
        }
    }

    
}
