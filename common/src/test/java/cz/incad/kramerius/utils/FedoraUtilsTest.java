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
package cz.incad.kramerius.utils;

import java.io.IOException;
import java.net.URL;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import cz.incad.kramerius.utils.conf.KConfiguration;

import junit.framework.TestCase;

public class FedoraUtilsTest {

    @Test
    public void fedoraUtilsExternalStream() throws ParserConfigurationException, SAXException, IOException, XPathExpressionException {
        Document extDoc = XMLUtils.parseDocument(this.getClass().getResourceAsStream("dsProfileExternal.xml"));
        boolean extDocBool = FedoraUtils.isFedoraExternalStream(KConfiguration.getInstance(), extDoc);
        TestCase.assertTrue("expect external stream", extDocBool);
        
        Document intDoc = XMLUtils.parseDocument(this.getClass().getResourceAsStream("dsProfileInternal.xml"));
        boolean intDocBool = FedoraUtils.isFedoraExternalStream(KConfiguration.getInstance(), intDoc);
        TestCase.assertFalse("expect internal stream", intDocBool);
    }
    
    @Test
    public void fedoraUtilsURL() throws ParserConfigurationException, SAXException, IOException, XPathExpressionException {
        Document extDoc = XMLUtils.parseDocument(this.getClass().getResourceAsStream("dsProfileExternal.xml"));
        String location = FedoraUtils.getLocation(KConfiguration.getInstance(), extDoc);
        TestCase.assertNotNull(location);
        URL url = new URL(location);
        TestCase.assertEquals("/img.jp2",url.getFile());
    }
    
}
