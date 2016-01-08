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
package cz.incad.kramerius.rest.api.k5.client.item.decorators;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.easymock.EasyMock;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.utils.XMLUtils;

public class ReplicatedFromDecorateTest {


	@Test
	public void testDecorate() throws ParserConfigurationException, SAXException, IOException, JSONException {
		URL res = CollectionsDecoratorTest.class.getResource("rels-ext.xml");
		Document document = XMLUtils.parseDocument(res.openStream(), true);
		
        FedoraAccess fa = EasyMock.createMock(FedoraAccess.class);
        EasyMock.expect(fa.getRelsExt("uuid:c32e0540-3e38-11e2-8227-5ef3fc9bb22f")).andReturn(document).anyTimes();
        
        EasyMock.replay(fa);
        
        ReplicatedFromDecorator rep = new ReplicatedFromDecorator();
        rep.fedoraAccess = fa;

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("pid", "uuid:c32e0540-3e38-11e2-8227-5ef3fc9bb22f");
    	Map<String, Object> runtimeContext = new HashMap<String, Object>();	
    	rep.decorate(jsonObject, runtimeContext);

    	Assert.assertTrue(jsonObject.has("replicatedFrom"));
    	Assert.assertTrue(jsonObject.getJSONArray("replicatedFrom").length() == 2);
	}
}
