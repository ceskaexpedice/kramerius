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
package cz.incad.kramerius.rest.oai;

import cz.incad.kramerius.rest.oai.metadata.utils.OAICDKUtils;
import cz.incad.kramerius.utils.XMLUtils;
import org.junit.Assert;
import org.junit.Test;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;

public class OAICDKUtilsTest {

    @Test
    public void testCDKDataProvider() throws ParserConfigurationException, IOException, SAXException {
        InputStream is = this.getClass().getResourceAsStream("oai.cdk.solr.xml");
        Assert.assertNotNull(is);
        String metadataProvider = OAICDKUtils.findMetadataProvider(XMLUtils.parseDocument(is));
        Assert.assertEquals("knav", metadataProvider);
    }
}
