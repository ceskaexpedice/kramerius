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
package cz.incad.kramerius.fedora.impl;

import static junit.framework.Assert.assertEquals;
import static org.easymock.EasyMock.createMockBuilder;
import static org.easymock.EasyMock.replay;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import cz.incad.kramerius.ObjectPidsPath;
import cz.incad.kramerius.impl.FedoraAccessImpl;
import cz.incad.kramerius.impl.SolrAccessImpl;
import cz.incad.kramerius.utils.SolrUtilsTest;
import cz.incad.kramerius.utils.conf.KConfiguration;

public class SolrAccessImplTest {

    @Test
    public void testGetSolrPDFPath() throws IOException, ParserConfigurationException, SAXException {
    	List<String> expecting = new ArrayList<String>() {{
            add("uuid:81c10f62-21bf-11e3-a6ac-001b63bd97ba");
            add("uuid:81c15d83-21bf-11e3-a6ac-001b63bd97ba");
            add("uuid:81c15d83-21bf-11e3-a6ac-001b63bd97ba/@1");
        }};

    	SolrAccessImpl solr = createMockBuilder(SolrAccessImpl.class)
        		.addMockedMethod("getSolrDataDocument")
        		.createMock();

        EasyMock.expect(solr.getSolrDataDocument("uuid:0xAAA")).andReturn(SolrUtilsTest.solrPDFDocument());
        replay(solr);
        
        ObjectPidsPath[] paths = solr.getPath("uuid:0xAAA");

        Assert.assertTrue(paths.length == 1);
        String[] real = paths[0].getPathFromRootToLeaf();
        Assert.assertEquals(expecting, Arrays.asList(real));
    }
	
	
    @Test
    public void testGetSolrSimplePath() throws IOException, ParserConfigurationException, SAXException {
        // SolrAccess 
        SolrAccessImpl solr = createMockBuilder(SolrAccessImpl.class)
        		.addMockedMethod("getSolrDataDocument")
        		.createMock();
        
        EasyMock.expect(solr.getSolrDataDocument("uuid:0xAAA")).andReturn(SolrUtilsTest.solrDocument());
        replay(solr);
        
        ObjectPidsPath[] paths = solr.getPath("uuid:0xAAA");
        Assert.assertTrue(paths.length == 2);

        ObjectPidsPath p1 = paths[0];
        Assert.assertTrue(p1.getLength() == 3);

        Assert.assertTrue(p1.getNodeFromRootToLeaf(0).equals("uuid:cd2b2ad0-62d4-11dd-ac0e-000d606f5dc6"));
        Assert.assertTrue(p1.getNodeFromRootToLeaf(1).equals("uuid:5fe0b160-62d5-11dd-bdc7-000d606f5dc6"));
        Assert.assertTrue(p1.getNodeFromRootToLeaf(2).equals("uuid:28286e70-64a6-11dd-981a-000d606f5dc6"));

        
        ObjectPidsPath p2 = paths[1];
        Assert.assertTrue(p2.getLength() == 4);

        Assert.assertTrue(p2.getNodeFromRootToLeaf(0).equals("uuid:cd2b2ad0-62d4-11dd-ac0e-000d606f5dc6"));
        Assert.assertTrue(p2.getNodeFromRootToLeaf(1).equals("uuid:5fe0b160-62d5-11dd-bdc7-000d606f5dc6"));
        Assert.assertTrue(p2.getNodeFromRootToLeaf(2).equals("uuid:f0da6570-8f3b-11dd-b796-000d606f5dc6"));
        Assert.assertTrue(p2.getNodeFromRootToLeaf(3).equals("uuid:28286e70-64a6-11dd-981a-000d606f5dc6"));

    }
    
    @Test
    public void testGetSolrPathWithStream() throws IOException, ParserConfigurationException, SAXException {
        // SolrAccess 
        SolrAccessImpl solr = createMockBuilder(SolrAccessImpl.class)
        .addMockedMethod("getSolrDataDocument")
        .createMock();
        
        EasyMock.expect(solr.getSolrDataDocument("uuid:0xAAA")).andReturn(SolrUtilsTest.solrDocument());
        replay(solr);
        
        ObjectPidsPath[] paths = solr.getPath("uuid:0xAAA/DC");
        for (int i = 0; i < paths.length; i++) {
        	paths[i] = paths[i].injectRepository();
		}

        Assert.assertTrue(paths.length == 2);

        ObjectPidsPath p1 = paths[0];
        Assert.assertTrue(p1.getLength() == 4);

        Assert.assertTrue(p1.getNodeFromRootToLeaf(0).equals("uuid:1"));
        Assert.assertTrue(p1.getNodeFromRootToLeaf(1).equals("uuid:cd2b2ad0-62d4-11dd-ac0e-000d606f5dc6/DC"));
        Assert.assertTrue(p1.getNodeFromRootToLeaf(2).equals("uuid:5fe0b160-62d5-11dd-bdc7-000d606f5dc6/DC"));
        Assert.assertTrue(p1.getNodeFromRootToLeaf(3).equals("uuid:28286e70-64a6-11dd-981a-000d606f5dc6/DC"));


        
        ObjectPidsPath p2 = paths[1];
        Assert.assertTrue(p2.getLength() == 5);

        Assert.assertTrue(p2.getNodeFromRootToLeaf(0).equals("uuid:1"));
        Assert.assertTrue(p2.getNodeFromRootToLeaf(1).equals("uuid:cd2b2ad0-62d4-11dd-ac0e-000d606f5dc6/DC"));
        Assert.assertTrue(p2.getNodeFromRootToLeaf(2).equals("uuid:5fe0b160-62d5-11dd-bdc7-000d606f5dc6/DC"));
        Assert.assertTrue(p2.getNodeFromRootToLeaf(3).equals("uuid:f0da6570-8f3b-11dd-b796-000d606f5dc6/DC"));
        Assert.assertTrue(p2.getNodeFromRootToLeaf(4).equals("uuid:28286e70-64a6-11dd-981a-000d606f5dc6/DC"));

    }
}
