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
package cz.incad.kramerius.rest.api.k5.client.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import cz.incad.kramerius.utils.XMLUtils;

public class SolrUtilsTest {

	@Test
	public void testSolr() throws ParserConfigurationException, SAXException, IOException {
		InputStream is = SolrUtilsTest.class.getResourceAsStream("select.xml");
		Document document = XMLUtils.parseDocument(is);
		Element topElm = XMLUtils.findElement(document.getDocumentElement(),"result");
		List<Element> elements = XMLUtils.getElements(topElm);
		List<String> arr = SOLRUtils.array(elements.get(0),"collection", String.class);
		Assert.assertTrue(arr.size() == 1);
	}

}
