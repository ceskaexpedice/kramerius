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
package cz.incad.kramerius.imaging;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import cz.incad.Kramerius.imaging.utils.FileNameUtils;
import cz.incad.kramerius.utils.XMLUtils;

import junit.framework.TestCase;

public class FileNameTest extends TestCase {

    public void testDisectFileName() throws ParserConfigurationException, SAXException, IOException, XPathExpressionException {
        InputStream stream = this.getClass().getResourceAsStream("rels-ext-page.xml");
        Document document = XMLUtils.parseDocument(stream,true);
        String fileName = FileNameUtils.disectFileNameFromRelsExt(document);
        assertEquals("4011400001.djvu", fileName);
    }
}
