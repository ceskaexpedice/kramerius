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

import javax.xml.parsers.ParserConfigurationException;

import org.easymock.EasyMock;
import org.junit.Test;
import org.xml.sax.SAXException;

import cz.incad.kramerius.ObjectPidsPath;
import cz.incad.kramerius.impl.FedoraAccessImpl;
import cz.incad.kramerius.impl.SolrAccessImpl;
import cz.incad.kramerius.utils.SolrUtilsTest;
import cz.incad.kramerius.utils.conf.KConfiguration;

public class SolrAccessImplTest {

    @Test
    public void testGetSolrSimplePath() throws IOException, ParserConfigurationException, SAXException {
        // SolrAccess 
        SolrAccessImpl solr = createMockBuilder(SolrAccessImpl.class)
        .addMockedMethod("getSolrDataDocument")
        .createMock();
        
        EasyMock.expect(solr.getSolrDataDocument("uuid:0xAAA")).andReturn(SolrUtilsTest.solrDocument());
        replay(solr);
        
        ObjectPidsPath[] paths = solr.getPath("uuid:0xAAA");
        for (ObjectPidsPath path : paths) {
            System.out.println(path.toString());
            
        }
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
        for (ObjectPidsPath path : paths) {
            path = path.injectRepository();
            System.out.println(path.toString());
        }
    }
}
