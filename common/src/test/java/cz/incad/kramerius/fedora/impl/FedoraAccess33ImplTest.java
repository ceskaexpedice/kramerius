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

import junit.framework.TestCase;

import org.easymock.EasyMock;
import org.junit.Test;

import cz.incad.kramerius.impl.FedoraAccessImpl;
import cz.incad.kramerius.statistics.StatisticsAccessLog;
import cz.incad.kramerius.utils.FedoraUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;

public class FedoraAccess33ImplTest {

    @Test
    public void testGetFedoraAccessVersion33() throws IOException {
        StatisticsAccessLog acLog = EasyMock.createMock(StatisticsAccessLog.class);
        
        // fedora 3.3
        FedoraAccessImpl fa33 = createMockBuilder(FedoraAccessImpl.class)
        .withConstructor(KConfiguration.getInstance(), acLog)
        .addMockedMethod("getFedoraDescribeStream")
        .createMock();
        
        EasyMock.expect(fa33.getFedoraDescribeStream()).andReturn(DataPrepare.fedoraProfile33());
        replay(fa33, acLog);
        
        assertEquals(fa33.getFedoraVersion(),"3.3");
    }


    @Test
    public void testIsStreamAvailable33() throws IOException, MalformedURLException {
        StatisticsAccessLog acLog = EasyMock.createMock(StatisticsAccessLog.class);
        // fedora 3.3
        FedoraAccessImpl fa33 = createMockBuilder(FedoraAccessImpl.class)
        .withConstructor(KConfiguration.getInstance(),acLog)
        .addMockedMethod("getFedoraDescribeStream")
        .addMockedMethod("getFedoraDataStreamsList")
        .createMock();

        EasyMock.expect(fa33.getFedoraDescribeStream()).andReturn(DataPrepare.fedoraProfile33());
        EasyMock.expect(fa33.getFedoraDataStreamsList("uuid:0eaa6730-9068-11dd-97de-000d606f5dc6")).andReturn(DataPrepare.datastreams33());
        EasyMock.expect(fa33.getFedoraDataStreamsList("uuid:0eaa6730-9068-11dd-97de-000d606f5dc6")).andReturn(DataPrepare.datastreams33());
        EasyMock.expect(fa33.getFedoraDataStreamsList("uuid:0eaa6730-9068-11dd-97de-000d606f5dc6")).andReturn(DataPrepare.datastreams33());
        
        replay(fa33, acLog);
        
        
        assertEquals(fa33.isStreamAvailable("uuid:0eaa6730-9068-11dd-97de-000d606f5dc6", FedoraUtils.IMG_FULL_STREAM),false);
        assertEquals(fa33.isStreamAvailable("uuid:0eaa6730-9068-11dd-97de-000d606f5dc6", FedoraUtils.RELS_EXT_STREAM),true);
        assertEquals(fa33.isStreamAvailable("uuid:0eaa6730-9068-11dd-97de-000d606f5dc6", FedoraUtils.DC_STREAM),true);
    }

    @Test
    public void testGetMimetypeForStream33() throws IOException {
        StatisticsAccessLog acLog = EasyMock.createMock(StatisticsAccessLog.class);
        // fedora 3.3
        FedoraAccessImpl fa33 = createMockBuilder(FedoraAccessImpl.class)
        .withConstructor(KConfiguration.getInstance(),acLog)
        .addMockedMethod("getFedoraDescribeStream")
        .addMockedMethod("getDsProfileForPIDStream")
        .createMock();

        EasyMock.expect(fa33.getFedoraDescribeStream()).andReturn(DataPrepare.fedoraProfile33());
        EasyMock.expect(fa33.getDsProfileForPIDStream("uuid:0eaa6730-9068-11dd-97de-000d606f5dc6", FedoraUtils.IMG_FULL_STREAM)).andReturn(DataPrepare.dsProfile33());
        
        replay(fa33,acLog);
        
        
        String mimeType33 = fa33.getMimeTypeForStream("uuid:0eaa6730-9068-11dd-97de-000d606f5dc6", FedoraUtils.IMG_FULL_STREAM);
        TestCase.assertTrue(mimeType33.equals("image/vnd.djvu"));
    }

}
