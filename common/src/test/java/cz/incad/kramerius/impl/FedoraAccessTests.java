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
package cz.incad.kramerius.impl;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import com.google.inject.Binding;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.name.Names;

import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.ProcessSubtreeException;
import cz.incad.kramerius.TreeNodeProcessor;
import cz.incad.kramerius.impl.FedoraAccessImpl;
import cz.incad.kramerius.service.ResourceBundleServiceModule;
import cz.incad.kramerius.utils.XMLUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;
import junit.framework.TestCase;

public class FedoraAccessTests {

    @Test
    public void fedoraVersion() throws XPathExpressionException, ParserConfigurationException, SAXException, IOException {
        FedoraAccessImpl fedoraAccessImpl = new FedoraAccessImpl(null);
        String versionImpl = fedoraAccessImpl.disectFedoraVersionFromStream(FedoraAccessTests.class.getResourceAsStream("res/describe_3_3"));
        TestCase.assertTrue(versionImpl.equals("3.3"));
        
        String version34 = fedoraAccessImpl.disectFedoraVersionFromStream(FedoraAccessTests.class.getResourceAsStream("res/describe_3_4"));
        TestCase.assertTrue(version34.equals("3.4.2"));
    }
    
    @Test
    public void fedoraDataStreamsTest() throws XPathExpressionException, ParserConfigurationException, SAXException, IOException {
        InputStream datastreams33 = FedoraAccessTests.class.getResourceAsStream("res/datastreams_3_3");
        FedoraAccessImpl fedoraAccessImpl = new FedoraAccessImpl(KConfiguration.getInstance());
        TestCase.assertTrue(fedoraAccessImpl.disectDatastreamInListOfDatastreams(XMLUtils.parseDocument(datastreams33,true), "RELS-EXT",fedoraAccessImpl.disectFedoraVersionFromStream(FedoraAccessTests.class.getResourceAsStream("res/describe_3_3"))));

        InputStream datastreams34 = FedoraAccessTests.class.getResourceAsStream("res/datastreams_3_4");
        TestCase.assertTrue(fedoraAccessImpl.disectDatastreamInListOfDatastreams(XMLUtils.parseDocument(datastreams34,true), "IMG_FULL",fedoraAccessImpl.disectFedoraVersionFromStream(FedoraAccessTests.class.getResourceAsStream("res/describe_3_4"))));
    }
    

    @Test
    public void fedoraMimetype() throws XPathExpressionException, ParserConfigurationException, SAXException, IOException {
        InputStream profile33 = FedoraAccessTests.class.getResourceAsStream("res/dsprofile_3_3");
        FedoraAccessImpl fedoraAccessImpl = new FedoraAccessImpl(KConfiguration.getInstance());
        String mimeType33 = fedoraAccessImpl.disectMimetypeFromProfile(XMLUtils.parseDocument(profile33,true), fedoraAccessImpl.disectFedoraVersionFromStream(FedoraAccessTests.class.getResourceAsStream("res/describe_3_3")));
        TestCase.assertTrue(mimeType33.equals("image/vnd.djvu"));

        InputStream profile34 = FedoraAccessTests.class.getResourceAsStream("res/dsprofile_3_4");
        String mimeType34 = fedoraAccessImpl.disectMimetypeFromProfile(XMLUtils.parseDocument(profile34,true), fedoraAccessImpl.disectFedoraVersionFromStream(FedoraAccessTests.class.getResourceAsStream("res/describe_3_4")));
        TestCase.assertTrue(mimeType34.equals("image/vnd.djvu"));
    }

    
    // how to test tree processing ?
//    public void testProcessSubTree() throws IOException, ProcessSubtreeException {
//        Injector injector = Guice.createInjector(new FedoraAccessModule());
//        FedoraAccess instance = injector.getInstance(Key.get(FedoraAccess.class, Names.named("testFA")));
//        assertNotNull(instance);
//        Document relsExt = instance.getRelsExt("0eaa6730-9068-11dd-97de-000d606f5dc6");
//        assertNotNull(relsExt);
//        instance.processSubtree("uuid:0eaa6730-9068-11dd-97de-000d606f5dc6", new TreeNodeProcessor() {
//            
//            @Override
//            public void process(String pid, int level) {
//                System.out.println("Processed "+pid);
//            }
//            
//            @Override
//            public boolean breakProcessing(String pid, int level) {
//                // TODO Auto-generated method stub
//                return false;
//            }
//        });
//    }
}
