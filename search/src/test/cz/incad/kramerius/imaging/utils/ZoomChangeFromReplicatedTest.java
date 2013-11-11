/*
 * Copyright (C) 2013 Pavel Stastny
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
package cz.incad.kramerius.imaging.utils;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import junit.framework.Assert;

import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import cz.incad.Kramerius.imaging.utils.ZoomChangeFromReplicated;
import cz.incad.kramerius.utils.XMLUtils;

public class ZoomChangeFromReplicatedTest {

	@Test
	public void testChangeURL() throws ParserConfigurationException, SAXException, IOException {
		
		Document document = XMLUtils.parseDocument(ZoomChangeFromReplicatedTest.class.getResourceAsStream("rels-ext.xml"),true);
		String deepZoomAddress = ZoomChangeFromReplicated.deepZoomAddress(document, "uuid:mypid");
		Assert.assertEquals(deepZoomAddress, "http://kramerius.mzk.cz/search/deepZoom/uuid:mypid");

		
		String zoomifyAddress = ZoomChangeFromReplicated.zoomifyAddress(document, "uuid:mypid");
		Assert.assertEquals(zoomifyAddress, "http://kramerius.mzk.cz/search/zoomify/uuid:mypid");
		
	}
}
