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

public class ArticleTitleBuilder extends TitleBuilder {
    
    public static final String APPLICABLE_MODEL="article";
    
    public static final String MODS_TITLE="mods:articletitle";
    

    
    @Override
    public void build(Document document, Map<String, List<String>> map, String model) throws XPathExpressionException {
        if (APPLICABLE_MODEL.equals(model)) {
            XPath xpath = getFactory().newXPath();
            xpath.setNamespaceContext(new FedoraNamespaceContext());
            
            XPathExpression expr = xpath.compile("//mods:titleInfo/mods:title/text()");
            Node node = (Node) expr.evaluate(document, XPathConstants.NODE);
            if (node != null) {
                add(MODS_TITLE, ((Text)node).getData(), map);
                add(DEFAULT_TITLE,((Text)node).getData(),map);
            }
        }

    }

}
