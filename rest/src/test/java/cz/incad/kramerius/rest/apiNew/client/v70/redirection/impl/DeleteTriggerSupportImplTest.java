package cz.incad.kramerius.rest.apiNew.client.v70.redirection.impl;

import cz.incad.kramerius.SolrAccess;
import cz.incad.kramerius.rest.apiNew.admin.v70.reharvest.AlreadyRegistedPidsException;
import cz.incad.kramerius.rest.apiNew.admin.v70.reharvest.ReharvestItem;
import cz.incad.kramerius.rest.apiNew.admin.v70.reharvest.ReharvestManager;
import cz.incad.kramerius.rest.apiNew.client.v70.libs.Instances;
import cz.incad.kramerius.rest.apiNew.client.v70.libs.OneInstance;
import cz.incad.kramerius.rest.apiNew.client.v70.redirection.ResourcesUtils;
import cz.incad.kramerius.rest.apiNew.client.v70.redirection.introspect.IntrospectLiveResponse;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.easymock.Capture;
import org.easymock.CaptureType;
import org.easymock.EasyMock;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import static org.easymock.EasyMock.createMockBuilder;

/*
 * Copyright (C) 2025  Inovatika
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
public class DeleteTriggerSupportImplTest  {


    public IntrospectLiveResponse mockLiveResponse(CloseableHttpClient client, Instances instances, String path, String pid) throws IOException {
        JSONObject introspect = ResourcesUtils.introspect(DeleteTriggerSupportImplTest.class, path, pid);
        Assert.assertNotNull(introspect);
        IntrospectLiveResponse.Builder builder = EasyMock.createMockBuilder(IntrospectLiveResponse.Builder.class)
                .addMockedMethod("getIntrospectSolr")
                .createMock();

        EasyMock.expect(builder.getIntrospectSolr()).andReturn(introspect).anyTimes();
        EasyMock.replay(builder);

        IntrospectLiveResponse response = builder.
                withHttpClient(client).
                withInstances(instances).
                withPid(pid).build();
        return response;
    }

    /** Reharvest only the part of the object */
    @Test
    public void testReharvestPAGE_PERIODICAL() throws IOException, ParserConfigurationException, SAXException, AlreadyRegistedPidsException {

        SolrAccess solrAccess = EasyMock.createMock(SolrAccess.class);
        EasyMock.expect(solrAccess.getSolrDataByPid("uuid:03e2e740-865f-11dd-b588-000d606f5dc6")).andReturn(ResourcesUtils.solrAccess(DeleteTriggerSupportImplTest.class,"periodical","uuid:03e2e740-865f-11dd-b588-000d606f5dc6")).anyTimes();
        EasyMock.expect(solrAccess.getSolrDataByPid("uuid:6e4330b0-866c-11dd-a376-000d606f5dc6")).andReturn(ResourcesUtils.solrAccess(DeleteTriggerSupportImplTest.class,"periodical","uuid:6e4330b0-866c-11dd-a376-000d606f5dc6")).anyTimes();

        ReharvestManager manager = EasyMock.createMock(ReharvestManager.class);

        Capture<ReharvestItem> capturedItem = EasyMock.newCapture();
        manager.register(EasyMock.capture(capturedItem));
        EasyMock.expectLastCall();

        EasyMock.expect(manager.getOpenItemByPid("uuid:6e4330b0-866c-11dd-a376-000d606f5dc6")).andReturn(null).anyTimes();

        OneInstance oneInstance = EasyMock.createMock(OneInstance.class);
        EasyMock.expect(oneInstance.getInstanceType()).andReturn(OneInstance.InstanceType.V7).anyTimes();
        EasyMock.expect(oneInstance.getName()).andReturn("test").anyTimes();
        EasyMock.expect(oneInstance.getType()).andReturn(OneInstance.TypeOfChangedStatus.automat).anyTimes();

        Instances instances = EasyMock.createMock(Instances.class);
        EasyMock.expect(instances.allInstances()).andReturn(Arrays.asList(oneInstance));
        EasyMock.expect(instances.enabledInstances()).andReturn(Arrays.asList(oneInstance));

        DeleteTriggerSupportImpl dts = createMockBuilder(DeleteTriggerSupportImpl.class)
                .addMockedMethod("buildResponse")
                .createMock();

        CloseableHttpClient client = EasyMock.createMock(CloseableHttpClient.class);

        IntrospectLiveResponse liveResponse_03e2 = mockLiveResponse(client, instances,"periodical", "uuid:03e2e740-865f-11dd-b588-000d606f5dc6");
        IntrospectLiveResponse liveResponse_6e43 = mockLiveResponse(client, instances,"periodical", "uuid:6e4330b0-866c-11dd-a376-000d606f5dc6");


        EasyMock.expect(dts.buildResponse("uuid:03e2e740-865f-11dd-b588-000d606f5dc6")).andReturn(liveResponse_03e2).anyTimes();
        EasyMock.expect(dts.buildResponse("uuid:6e4330b0-866c-11dd-a376-000d606f5dc6")).andReturn(liveResponse_6e43).anyTimes();

        EasyMock.replay(solrAccess, manager, oneInstance, instances, client, dts);

        dts.setSolrAccess(solrAccess);
        dts.setReharvestManager(manager);
        dts.setInstances(instances);

        dts.executeDeleteTrigger("uuid:03e2e740-865f-11dd-b588-000d606f5dc6");
        ReharvestItem plannedItem = capturedItem.getValue();

        Assert.assertNotNull(plannedItem.getRootPid());
        Assert.assertEquals("uuid:12e13290-8300-11dd-9bc3-000d606f5dc6", plannedItem.getRootPid());

        Assert.assertNotNull(plannedItem.getPid());
        Assert.assertEquals("uuid:6e4330b0-866c-11dd-a376-000d606f5dc6", plannedItem.getPid());

        Assert.assertNotNull(plannedItem.getOwnPidPath());
        Assert.assertEquals("uuid:12e13290-8300-11dd-9bc3-000d606f5dc6/uuid:bf502a60-865f-11dd-95ea-000d606f5dc6/uuid:6e4330b0-866c-11dd-a376-000d606f5dc6", plannedItem.getOwnPidPath());

        Assert.assertEquals(plannedItem.getTypeOfReharvest(), ReharvestItem.TypeOfReharvset.children);
        Assert.assertEquals(plannedItem.getLibraries(), Arrays.asList("kfbz","nkp"));
    }

    /** Reharvest whole root */
    @Test
    public void testReharvestPAGE_MONOGRAPH() throws ParserConfigurationException, IOException, SAXException, AlreadyRegistedPidsException {

        SolrAccess solrAccess = EasyMock.createMock(SolrAccess.class);
        EasyMock.expect(solrAccess.getSolrDataByPid("uuid:200171d8-8d20-11ef-a50e-001b63bd97ba")).andReturn(ResourcesUtils.solrAccess(DeleteTriggerSupportImplTest.class,"monograph","uuid:200171d8-8d20-11ef-a50e-001b63bd97ba")).anyTimes();
        EasyMock.expect(solrAccess.getSolrDataByPid("uuid:f72ece0d-a023-44cb-a903-e69435161721")).andReturn(ResourcesUtils.solrAccess(DeleteTriggerSupportImplTest.class,"monograph","uuid:f72ece0d-a023-44cb-a903-e69435161721")).anyTimes();

        ReharvestManager manager = EasyMock.createMock(ReharvestManager.class);

        Capture<ReharvestItem> capturedItem = EasyMock.newCapture();
        manager.register(EasyMock.capture(capturedItem));
        EasyMock.expectLastCall();

        EasyMock.expect(manager.getOpenItemByPid("uuid:f72ece0d-a023-44cb-a903-e69435161721")).andReturn(null).anyTimes();

        OneInstance oneInstance = EasyMock.createMock(OneInstance.class);
        EasyMock.expect(oneInstance.getInstanceType()).andReturn(OneInstance.InstanceType.V7).anyTimes();
        EasyMock.expect(oneInstance.getName()).andReturn("test").anyTimes();
        EasyMock.expect(oneInstance.getType()).andReturn(OneInstance.TypeOfChangedStatus.automat).anyTimes();

        Instances instances = EasyMock.createMock(Instances.class);
        EasyMock.expect(instances.allInstances()).andReturn(Arrays.asList(oneInstance));
        EasyMock.expect(instances.enabledInstances()).andReturn(Arrays.asList(oneInstance));

        DeleteTriggerSupportImpl dts = createMockBuilder(DeleteTriggerSupportImpl.class)
                .addMockedMethod("buildResponse")
                .createMock();

        CloseableHttpClient client = EasyMock.createMock(CloseableHttpClient.class);


        IntrospectLiveResponse liveResponse_2001 = mockLiveResponse(client, instances,"monograph", "uuid:200171d8-8d20-11ef-a50e-001b63bd97ba");
        IntrospectLiveResponse liveResponse_f72e = mockLiveResponse(client, instances,"monograph", "uuid:f72ece0d-a023-44cb-a903-e69435161721");

        EasyMock.expect(dts.buildResponse("uuid:200171d8-8d20-11ef-a50e-001b63bd97ba")).andReturn(liveResponse_2001).anyTimes();
        EasyMock.expect(dts.buildResponse("uuid:f72ece0d-a023-44cb-a903-e69435161721")).andReturn(liveResponse_f72e).anyTimes();

        EasyMock.replay(solrAccess, manager, oneInstance, instances, client, dts);

        dts.setSolrAccess(solrAccess);
        dts.setReharvestManager(manager);
        dts.setInstances(instances);

        dts.executeDeleteTrigger("uuid:200171d8-8d20-11ef-a50e-001b63bd97ba");
        ReharvestItem plannedItem = capturedItem.getValue();

        Assert.assertNotNull(plannedItem.getRootPid());
        Assert.assertEquals("uuid:f72ece0d-a023-44cb-a903-e69435161721", plannedItem.getRootPid());

        Assert.assertNotNull(plannedItem.getPid());
        Assert.assertEquals("uuid:f72ece0d-a023-44cb-a903-e69435161721", plannedItem.getPid());

        Assert.assertNotNull(plannedItem.getOwnPidPath());
        Assert.assertEquals(plannedItem.getTypeOfReharvest(), ReharvestItem.TypeOfReharvset.root);

        Assert.assertTrue(plannedItem.getLibraries().size() == 1);
    }


    @Test
    public void testReharvestConflict() throws ParserConfigurationException, IOException, SAXException, AlreadyRegistedPidsException {

        SolrAccess solrAccess = EasyMock.createMock(SolrAccess.class);
        EasyMock.expect(solrAccess.getSolrDataByPid("uuid:e4335ed0-69f6-11e3-8387-001018b5eb5c")).andReturn(ResourcesUtils.solrAccess(DeleteTriggerSupportImplTest.class,"conflict","uuid:e4335ed0-69f6-11e3-8387-001018b5eb5c")).times(1);
        EasyMock.expect(solrAccess.getSolrDataByPid("uuid:9056a3d0-fca3-11ee-96e1-005056827e51")).andReturn(ResourcesUtils.solrAccess(DeleteTriggerSupportImplTest.class, "conflict","uuid:9056a3d0-fca3-11ee-96e1-005056827e51")).times(1);
        EasyMock.expect(solrAccess.getSolrDataByPid("uuid:5c1f0af0-31a5-11e3-8d9d-005056827e51")).andReturn(ResourcesUtils.solrAccess(DeleteTriggerSupportImplTest.class, "conflict","uuid:5c1f0af0-31a5-11e3-8d9d-005056827e51")).times(1);

        EasyMock.expect(solrAccess.getSolrDataByPid("uuid:9056a3d0-fca3-11ee-96e1-005056827e51")).andReturn(ResourcesUtils.solrAccess(DeleteTriggerSupportImplTest.class,"conflict","uuid:none")).anyTimes();
        EasyMock.expect(solrAccess.getSolrDataByPid("uuid:5c1f0af0-31a5-11e3-8d9d-005056827e51")).andReturn(ResourcesUtils.solrAccess(DeleteTriggerSupportImplTest.class,"conflict","uuid:none")).anyTimes();

        ReharvestManager manager = EasyMock.createMock(ReharvestManager.class);

        Capture<ReharvestItem> capturedItems = EasyMock.newCapture(CaptureType.ALL);
        manager.register(EasyMock.capture(capturedItems));
        EasyMock.expectLastCall().anyTimes();

        OneInstance oneInstance = EasyMock.createMock(OneInstance.class);
        EasyMock.expect(oneInstance.getInstanceType()).andReturn(OneInstance.InstanceType.V7).anyTimes();
        EasyMock.expect(oneInstance.getName()).andReturn("test").anyTimes();
        EasyMock.expect(oneInstance.getType()).andReturn(OneInstance.TypeOfChangedStatus.automat).anyTimes();

        Instances instances = EasyMock.createMock(Instances.class);
        EasyMock.expect(instances.allInstances()).andReturn(Arrays.asList(oneInstance));
        EasyMock.expect(instances.enabledInstances()).andReturn(Arrays.asList(oneInstance));

        DeleteTriggerSupportImpl dts = createMockBuilder(DeleteTriggerSupportImpl.class)
                .addMockedMethod("buildResponse")
                .createMock();

        CloseableHttpClient client = EasyMock.createMock(CloseableHttpClient.class);


        IntrospectLiveResponse liveResponse_9056 = mockLiveResponse(client, instances,"conflict", "uuid:9056a3d0-fca3-11ee-96e1-005056827e51");
        IntrospectLiveResponse liveResponse_5c1f = mockLiveResponse(client, instances,"conflict", "uuid:5c1f0af0-31a5-11e3-8d9d-005056827e51");

        EasyMock.expect(dts.buildResponse("uuid:9056a3d0-fca3-11ee-96e1-005056827e51")).andReturn(liveResponse_9056).anyTimes();
        EasyMock.expect(dts.buildResponse("uuid:5c1f0af0-31a5-11e3-8d9d-005056827e51")).andReturn(liveResponse_5c1f).anyTimes();


        EasyMock.expect(manager.getOpenItemByPid("uuid:9056a3d0-fca3-11ee-96e1-005056827e51")).andReturn(null).anyTimes();
        EasyMock.expect(manager.getOpenItemByPid("uuid:5c1f0af0-31a5-11e3-8d9d-005056827e51")).andReturn(null).anyTimes();

        EasyMock.replay(solrAccess, manager, oneInstance, instances, client, dts);

        dts.setSolrAccess(solrAccess);
        dts.setReharvestManager(manager);
        dts.setInstances(instances);

        dts.executeConflictTrigger("uuid:e4335ed0-69f6-11e3-8387-001018b5eb5c");
        List<ReharvestItem> values = capturedItems.getValues();
        // delete root
        Assert.assertTrue(values.size() >= 4);
        Assert.assertTrue(values.get(0).getTypeOfReharvest().equals(ReharvestItem.TypeOfReharvset.delete_root));
        Assert.assertTrue(values.get(0).getPid().equals("uuid:9056a3d0-fca3-11ee-96e1-005056827e51"));

        Assert.assertTrue(values.get(1).getTypeOfReharvest().equals(ReharvestItem.TypeOfReharvset.delete_root));
        Assert.assertTrue(values.get(1).getPid().equals("uuid:5c1f0af0-31a5-11e3-8d9d-005056827e51"));

        Assert.assertTrue(values.get(2).getTypeOfReharvest().equals(ReharvestItem.TypeOfReharvset.root));
        Assert.assertTrue(values.get(2).getPid().equals("uuid:9056a3d0-fca3-11ee-96e1-005056827e51"));

        Assert.assertTrue(values.get(3).getTypeOfReharvest().equals(ReharvestItem.TypeOfReharvset.children));
        Assert.assertTrue(values.get(3).getPid().equals("uuid:5c1f0af0-31a5-11e3-8d9d-005056827e51"));

    }
}
