/*
 * Copyright (C) 2012 Pavel Stastny
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
package cz.incad.kramerius.document.model.utils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import javax.xml.parsers.ParserConfigurationException;

import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.ObjectPidsPath;
import cz.incad.kramerius.SolrAccess;
import cz.incad.kramerius.document.model.DCConent;
import cz.incad.kramerius.utils.XMLUtils;


public class DCContentUtilsTest {

    private final int MAX = 5500;

    @Test
    public void testCache() throws ParserConfigurationException, SAXException, IOException {
        InputStream is = DCContentUtilsTest.class.getClassLoader().getResourceAsStream("cz/incad/kramerius/fedora/res/0eaa6730-9068-11dd-97de-000d606f5dc6.dc.xml");
        Document dcDocument = XMLUtils.parseDocument(is,true);
        
        List<String> pids = generatePIDS();
        
        FedoraAccess fa = EasyMock.createMock(FedoraAccess.class);
        for (int i = 0; i < MAX; i++) {
            EasyMock.expect(fa.getDC(pids.get(i))).andReturn(dcDocument).anyTimes();
        }
        
        
        SolrAccess sa = EasyMock.createMock(SolrAccess.class);
        for (int i = 0; i < MAX; i++) {
            ObjectPidsPath pidPath = new ObjectPidsPath(pids.get(i)).injectRepository();
            EasyMock.expect(sa.getPath(pids.get(i))).andReturn(new ObjectPidsPath[] {pidPath}).anyTimes();
        }
        
        
        
        EasyMock.replay(fa,sa);
        
        for (int i = 0; i < pids.size(); i++) {
            String pid = pids.get(i);
            DCContentUtils.getDCS(fa, sa, Arrays.asList(pid));
        }
        
        List<String> tailed = tail(DCContentUtils._CACHE_SIZE,pids);
        Assert.assertTrue(DCContentUtils._CACHE.size() == DCContentUtils._CACHE_SIZE);
        Assert.assertTrue(DCContentUtils._CACHE_HISTORY.size() == DCContentUtils._CACHE_SIZE);
        for (int i = 0; i < tailed.size(); i++) {
            Assert.assertTrue(DCContentUtils._CACHE_HISTORY.get(i).equals(tailed.get(i)));
        }
    }

    public List<String> tail(int number, List<String> orig) {
        int offset = orig.size() - number;
        if (offset > 0) {
            return orig.subList(offset, orig.size());
        } else return orig;
    }
    public List<String> generatePIDS() {
        List<String> pids = new ArrayList<String>();
        for (int i = 0; i < MAX; i++) {
            String p = "uuid:"+UUID.randomUUID().toString();
            pids.add(p);
        }
        return pids;
    }
}
