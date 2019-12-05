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

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import cz.incad.kramerius.FedoraNamespaceContext;

/**
 * Vytahuje autory
 * @author pavels
 */
public class AuthorBuilder extends AbstractBuilder {

    private static final String AUTHOR_CONSTANT = "cre";
    private static final String ALTERNATIVE_AUTHOR_CONSTANT = "aut";
    
    
    public static final String MODS_AUTHOR="mods:author";


//    <mods:name type="personal">
//    <mods:namePart type="family">Kramerius</mods:namePart>
//    <mods:namePart type="given">Václav</mods:namePart>
//    <mods:role>
//        <mods:roleTerm type="code">cre</mods:roleTerm>
//
//        <mods:roleTerm type="text">Author</mods:roleTerm>
//    </mods:role>
//</mods:name>

    @Override
    public void build(Document document, Map<String, List<String>> map, String model) throws XPathExpressionException {

        XPath xpath = getFactory().newXPath();
        xpath.setNamespaceContext(new FedoraNamespaceContext());
        
        XPathExpression expr = xpath.compile("//mods:name[@type='personal']");
        NodeList nlist = (NodeList) expr.evaluate(document, XPathConstants.NODESET);
        for (int i = 0,ll=nlist.getLength(); i < ll; i++) {
            Node item = nlist.item(i);
            XPathExpression subExpr = xpath.compile("mods:role/mods:roleTerm[@type='code']/text()");
            Object textNode = subExpr.evaluate(item, XPathConstants.NODE);
            if ((textNode !=null ) && (textNode instanceof Text)) {
                String role   = ((Text)textNode).getData();
                if (roleIsAuthor(role)) {
                    String givenName = null; String familyName = null;
                    XPathExpression givenNameExpr = xpath.compile("mods:namePart[@type='given']/text()");
                    textNode = givenNameExpr.evaluate(item, XPathConstants.NODE);
                    if (textNode != null) {
                        givenName = ((Text)textNode).getData();
                    }

                    XPathExpression familyNameExpr = xpath.compile("mods:namePart[@type='family']/text()");
                    textNode = familyNameExpr.evaluate(item, XPathConstants.NODE);
                    if (textNode != null) {
                        familyName = ((Text)textNode).getData();
                    }
                    if (givenName == null && familyName == null)
                    {
                        String name = null;
                        XPathExpression nameExpr = xpath.compile ("//mods:name/mods:namePart/text()");
                        textNode = nameExpr.evaluate(item, XPathConstants.NODE);
                        if (textNode != null) {
                            name = ((Text)textNode).getData();
                        }
                        String author = (name != null ? name : "");
                        if (!author.trim().equals("")) {
                            add(MODS_AUTHOR,author,map);
                        }
                    }
                    else {
                        String author = (givenName != null ? givenName : "") +" "+(familyName != null ? familyName : "");
                        if (!author.trim().equals("")) {
                            add(MODS_AUTHOR,author,map);
                        }
                    }

                }
            }
        }
    }


    public boolean roleIsAuthor(String role) {
        return Arrays.asList(AUTHOR_CONSTANT, ALTERNATIVE_AUTHOR_CONSTANT).contains(role);
    }
}
