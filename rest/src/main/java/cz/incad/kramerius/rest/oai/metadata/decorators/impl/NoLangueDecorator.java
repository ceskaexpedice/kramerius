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

import cz.incad.kramerius.rest.oai.metadata.decorators.DublinCoreDecorator;
import cz.incad.kramerius.utils.XMLUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * <pre>
 * Schematron error: Within a ProvidedCHO context, dc:language is mandatory when edm:type has the value 'TEXT'.
 * </pre>
 *
 * Adding default language <dc:lang>und</dc:lang>
 */
public class NoLangueDecorator implements DublinCoreDecorator {

    public static final String UNDEFINED_LANGUAGE = "und";

    @Override
    public Document decorate(Document dc) {
        Element languageElm = XMLUtils.findElement(dc.getDocumentElement(), (elm)-> {
            return  (elm.getLocalName().equals("language"));
        });
        if (languageElm == null) {
            // <dc:language>und</dc:language>
            Element dcLang = dc.createElementNS("http://purl.org/dc/elements/1.1/", "dc:language");
            dcLang.setTextContent(UNDEFINED_LANGUAGE);
            dc.getDocumentElement().appendChild(dcLang);
        }
        return dc;
    }
}
