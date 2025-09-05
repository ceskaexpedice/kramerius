/*
 * Copyright (C) 2025  Inovatika
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
package cz.incad.kramerius.rest.oai.metadata.decorators.impl;

import cz.incad.kramerius.rest.oai.metadata.decorators.MetadataDecorator;
import cz.incad.kramerius.utils.XMLUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * <pre>
 *     Schematron error: A ProvidedCHO must have a dc:subject or dc:type or dct:temporal or dct:spatial.
 * </pre>
 *
 * Adding default type <dc:type>print</dc:type>
 */
public class NoTypeOrSubjectDecorator implements MetadataDecorator {

    @Override
    public Document decorate(Document dc, Document mods) {
        Element subjectOrType = XMLUtils.findElement(dc.getDocumentElement(), (elm)-> {
            return  (elm.getLocalName().equals("subject") || elm.getLocalName().equals("type"));
        });
        if (subjectOrType == null) {
            // <dc:type xmlns:dc="http://purl.org/dc/elements/1.1/">print</dc:type>
            Element dcType = dc.createElementNS("http://purl.org/dc/elements/1.1/", "dc:type");
            dcType.setTextContent("print");
            dc.getDocumentElement().appendChild(dcType);
        }
        return dc;
    }
}
