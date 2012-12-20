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
/**
 * 
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

/**
 * @author pavels
 *
 */
public class FedoraAccess36ImplTest {

    /** Test getFedoraVersion method - 3.6.x */
    @Test
    public void testGetFedoraAccessVersion36() throws IOException, XPathExpressionException, ParserConfigurationException, SAXException {
        StatisticsAccessLog acLog = EasyMock.createMock(StatisticsAccessLog.class);

        // fedora 3.4
        FedoraAccessImpl fa36 = createMockBuilder(FedoraAccessImpl.class)
        .withConstructor(KConfiguration.getInstance(),acLog)
        .addMockedMethod("getFedoraDescribeStream")
        .createMock();
        
        EasyMock.expect(fa36.getFedoraDescribeStream()).andReturn(DataPrepare.fedoraProfile36());
        replay(fa36,acLog);
        
        assertEquals(fa36.getFedoraVersion(),"3.6.2");
    }

    @Test
    public void testIsStreamAvailable36() throws MalformedURLException, IOException {
        StatisticsAccessLog acLog = EasyMock.createMock(StatisticsAccessLog.class);

        // fedora 3.6
        FedoraAccessImpl fa36 = createMockBuilder(FedoraAccessImpl.class)
        .withConstructor(KConfiguration.getInstance(),acLog)
        .addMockedMethod("getFedoraDescribeStream")
        .addMockedMethod("getFedoraDataStreamsList")
        .createMock();

        EasyMock.expect(fa36.getFedoraDescribeStream()).andReturn(DataPrepare.fedoraProfile36());
        EasyMock.expect(fa36.getFedoraDataStreamsList("uuid:0eaa6730-9068-11dd-97de-000d606f5dc6")).andReturn(DataPrepare.datastreams36());
        EasyMock.expect(fa36.getFedoraDataStreamsList("uuid:0eaa6730-9068-11dd-97de-000d606f5dc6")).andReturn(DataPrepare.datastreams36());
        EasyMock.expect(fa36.getFedoraDataStreamsList("uuid:0eaa6730-9068-11dd-97de-000d606f5dc6")).andReturn(DataPrepare.datastreams36());
        
        replay(fa36,acLog);


        assertEquals(fa36.isStreamAvailable("uuid:0eaa6730-9068-11dd-97de-000d606f5dc6", FedoraUtils.IMG_FULL_STREAM),true);
        assertEquals(fa36.isStreamAvailable("uuid:0eaa6730-9068-11dd-97de-000d606f5dc6", FedoraUtils.RELS_EXT_STREAM),true);
        assertEquals(fa36.isStreamAvailable("uuid:0eaa6730-9068-11dd-97de-000d606f5dc6", FedoraUtils.DC_STREAM),true);
    }

    @Test
    public void testGetMimetypeForStream36() throws IOException {
        StatisticsAccessLog acLog = EasyMock.createMock(StatisticsAccessLog.class);
        // fedora 3.6
        FedoraAccessImpl fa36 = createMockBuilder(FedoraAccessImpl.class)
        .withConstructor(KConfiguration.getInstance(),acLog)
        .addMockedMethod("getFedoraDescribeStream")
        .addMockedMethod("getDsProfileForPIDStream")
        .createMock();

        EasyMock.expect(fa36.getFedoraDescribeStream()).andReturn(DataPrepare.fedoraProfile36());
        EasyMock.expect(fa36.getDsProfileForPIDStream("uuid:0eaa6730-9068-11dd-97de-000d606f5dc6", FedoraUtils.IMG_FULL_STREAM)).andReturn(DataPrepare.dsProfile36());
        
        replay(fa36,acLog);
        
        
        String mimeType36 = fa36.getMimeTypeForStream("uuid:0eaa6730-9068-11dd-97de-000d606f5dc6", FedoraUtils.IMG_FULL_STREAM);
        TestCase.assertTrue(mimeType36.equals("image/jpeg"));
    }

}
