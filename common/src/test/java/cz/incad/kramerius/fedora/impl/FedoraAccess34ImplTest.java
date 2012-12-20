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
import java.net.MalformedURLException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import junit.framework.TestCase;

import org.easymock.EasyMock;
import org.junit.Test;
import org.xml.sax.SAXException;

import cz.incad.kramerius.impl.FedoraAccessImpl;
import cz.incad.kramerius.statistics.StatisticsAccessLog;
import cz.incad.kramerius.utils.FedoraUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;

public class FedoraAccess34ImplTest {

    /** Test getFedoraVersion method - fa 3.3 and also 3.4.x */
    @Test
    public void testGetFedoraAccessVersion34() throws IOException, XPathExpressionException, ParserConfigurationException, SAXException {
        StatisticsAccessLog acLog = EasyMock.createMock(StatisticsAccessLog.class);
        // fedora 3.4
        FedoraAccessImpl fa34 = createMockBuilder(FedoraAccessImpl.class)
        .withConstructor(KConfiguration.getInstance(), acLog)
        .addMockedMethod("getFedoraDescribeStream")
        .createMock();
        
        EasyMock.expect(fa34.getFedoraDescribeStream()).andReturn(DataPrepare.fedoraProfile34());
        replay(fa34,acLog);
        
        assertEquals(fa34.getFedoraVersion(),"3.4.2");
    }

    
    @Test
    public void testIsStreamAvailable34() throws MalformedURLException, IOException {
        StatisticsAccessLog acLog = EasyMock.createMock(StatisticsAccessLog.class);

        // fedora 3.4
        FedoraAccessImpl fa34 = createMockBuilder(FedoraAccessImpl.class)
        .withConstructor(KConfiguration.getInstance(),acLog)
        .addMockedMethod("getFedoraDescribeStream")
        .addMockedMethod("getFedoraDataStreamsList")
        .createMock();

        EasyMock.expect(fa34.getFedoraDescribeStream()).andReturn(DataPrepare.fedoraProfile34());
        EasyMock.expect(fa34.getFedoraDataStreamsList("uuid:0eaa6730-9068-11dd-97de-000d606f5dc6")).andReturn(DataPrepare.datastreams34());
        EasyMock.expect(fa34.getFedoraDataStreamsList("uuid:0eaa6730-9068-11dd-97de-000d606f5dc6")).andReturn(DataPrepare.datastreams34());
        EasyMock.expect(fa34.getFedoraDataStreamsList("uuid:0eaa6730-9068-11dd-97de-000d606f5dc6")).andReturn(DataPrepare.datastreams34());
        
        replay(fa34,acLog);


        assertEquals(fa34.isStreamAvailable("uuid:0eaa6730-9068-11dd-97de-000d606f5dc6", FedoraUtils.IMG_FULL_STREAM),false);
        assertEquals(fa34.isStreamAvailable("uuid:0eaa6730-9068-11dd-97de-000d606f5dc6", FedoraUtils.RELS_EXT_STREAM),true);
        assertEquals(fa34.isStreamAvailable("uuid:0eaa6730-9068-11dd-97de-000d606f5dc6", FedoraUtils.DC_STREAM),true);
    }


    @Test
    public void testGetMimeTypeForStream34() throws XPathExpressionException, ParserConfigurationException, SAXException, IOException {
        StatisticsAccessLog acLog = EasyMock.createMock(StatisticsAccessLog.class);
        
        // fedora 3.4
        FedoraAccessImpl fa34 = createMockBuilder(FedoraAccessImpl.class)
        .withConstructor(KConfiguration.getInstance(),acLog)
        .addMockedMethod("getFedoraDescribeStream")
        .addMockedMethod("getDsProfileForPIDStream")
        .createMock();

        EasyMock.expect(fa34.getFedoraDescribeStream()).andReturn(DataPrepare.fedoraProfile34());
        EasyMock.expect(fa34.getDsProfileForPIDStream("uuid:0eaa6730-9068-11dd-97de-000d606f5dc6", FedoraUtils.IMG_FULL_STREAM)).andReturn(DataPrepare.dsProfile34());
        
        replay(fa34,acLog);
        
        
        String mimeType34 = fa34.getMimeTypeForStream("uuid:0eaa6730-9068-11dd-97de-000d606f5dc6", FedoraUtils.IMG_FULL_STREAM);
        TestCase.assertTrue(mimeType34.equals("image/vnd.djvu"));

    }

}
