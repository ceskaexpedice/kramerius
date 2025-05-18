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
package cz.incad.kramerius.security.licenses;

import cz.incad.kramerius.utils.XMLUtils;
import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;

public class RuntimeLicenseTypeTest {


    @Test
    public void testFrontCover_Monograph() throws ParserConfigurationException, IOException, SAXException {
        InputStream is = this.getClass().getResourceAsStream("page_monograph_frontcover.xml");
        Document doc = XMLUtils.parseDocument(is, false);
        Assert.assertTrue(RuntimeLicenseType.COVER_AND_CONTENT_MONOGRAPH_PAGE.accept(doc));
    }

    @Test
    public void testNormalPage_Monograph() throws ParserConfigurationException, IOException, SAXException {
        InputStream is = this.getClass().getResourceAsStream("page_monograph_normalpage.xml");
        Document doc = XMLUtils.parseDocument(is, false);
        Assert.assertFalse(RuntimeLicenseType.COVER_AND_CONTENT_MONOGRAPH_PAGE.accept(doc));
    }

    @Test
    public void testFrontJacket_Monograph() throws ParserConfigurationException, IOException, SAXException {
        InputStream is = this.getClass().getResourceAsStream("page_monograph_frontjacket.xml");
        Document doc = XMLUtils.parseDocument(is, false);
        Assert.assertTrue(RuntimeLicenseType.COVER_AND_CONTENT_MONOGRAPH_PAGE.accept(doc));
    }

    @Test
    public void testFrontTableOfContent_Monograph() throws ParserConfigurationException, IOException, SAXException {
        InputStream is = this.getClass().getResourceAsStream("page_monograph_tableofcontent.xml");
        Document doc = XMLUtils.parseDocument(is, false);
        Assert.assertTrue(RuntimeLicenseType.COVER_AND_CONTENT_MONOGRAPH_PAGE.accept(doc));
    }

    @Test
    public void testFrontTableOfContent_Periodical() throws ParserConfigurationException, IOException, SAXException {
        InputStream is = this.getClass().getResourceAsStream("page_periodical_teableofcontent.xml");
        Document doc = XMLUtils.parseDocument(is, false);
        Assert.assertFalse(RuntimeLicenseType.COVER_AND_CONTENT_MONOGRAPH_PAGE.accept(doc));
    }

}
