/*
 * Copyright (C) 2012 Martin Řehánek <rehan at mzk.cz>
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package cz.incad.kramerius.audio;

import java.util.Iterator;
import javax.xml.namespace.NamespaceContext;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

/**
 *
 * @author Martin Řehánek <rehan at mzk.cz>
 */
public class XpathEvaluator {

    private final XPath xpath;

    public XpathEvaluator() {
        XPathFactory factory = XPathFactory.newInstance();
        NamespaceContext context = namespaceContext();
        xpath = factory.newXPath();
        xpath.setNamespaceContext(context);
    }

    private NamespaceContext namespaceContext() {
        return new NamespaceContext() {
            @Override
            public String getNamespaceURI(String prefix) {
                if (prefix == null) {
                    throw new NullPointerException("Null prefix");
                } else if ("rdf".equals(prefix)) {
                    return "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
                } else if ("rel".equals(prefix)) {
                    return "http://www.nsdl.org/ontologies/relationships#";
                } else if ("model".equals(prefix)) {
                    return "info:fedora/fedora-system:def/model#";
                } else if ("dc".equals(prefix)) {
                    return "http://purl.org/dc/elements/1.1/";
                } else if ("fedora-access".equals(prefix)) {
                    return "http://www.fedora.info/definitions/1/0/access/";
                } else if ("fedora-management".equals(prefix)) {
                    return "http://www.fedora.info/definitions/1/0/management/";
                } else {
                    return "";
                }
            }

            // This method isn't necessary for XPath processing.
            @Override
            public String getPrefix(String uri) {
                throw new UnsupportedOperationException();
            }

            // This method isn't necessary for XPath processing either.
            @Override
            public Iterator getPrefixes(String uri) {
                throw new UnsupportedOperationException();
            }
        };
    }

    public XPathExpression createExpression(String expression) throws XPathExpressionException {
        return xpath.compile(expression);
    }
}
