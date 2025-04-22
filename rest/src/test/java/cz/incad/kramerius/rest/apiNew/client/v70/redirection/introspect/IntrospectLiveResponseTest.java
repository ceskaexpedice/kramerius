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
package cz.incad.kramerius.rest.apiNew.client.v70.redirection.introspect;

import cz.incad.kramerius.rest.apiNew.client.v70.libs.Instances;
import cz.incad.kramerius.rest.apiNew.client.v70.redirection.ResourcesUtils;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.easymock.EasyMock;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class IntrospectLiveResponseTest {

    @Test
    public void testIntrospectLiveResults() throws IOException {
        JSONObject introspect = ResourcesUtils.introspect(IntrospectLiveResponse.class, "", "uuid:5c1f0af0-31a5-11e3-8d9d-005056827e51");
        Assert.assertNotNull(introspect);
        IntrospectLiveResponse.Builder builder = EasyMock.createMockBuilder(IntrospectLiveResponse.Builder.class)
                .addMockedMethod("getIntrospectSolr")
                .createMock();

        CloseableHttpClient client = EasyMock.createMock(CloseableHttpClient.class);
        Instances instances = EasyMock.createMock(Instances.class);

        EasyMock.expect(builder.getIntrospectSolr()).andReturn(introspect).anyTimes();
        EasyMock.replay(builder, client, instances);

        IntrospectLiveResponse response = builder.withHttpClient(client).withInstances(instances).withPid("uuid:5c1f0af0-31a5-11e3-8d9d-005056827e51").build();
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getPid(), "uuid:5c1f0af0-31a5-11e3-8d9d-005056827e51");
        Assert.assertEquals(response.getKeys(), Arrays.asList("nkp","mzk").stream().collect(Collectors.toSet()));
        List<IntrospectLiveResponse.IntrospectLiveResponseItem> introspectItems = response.getIntrospectItems();

        Assert.assertTrue(introspectItems.size() == 2);

        Assert.assertTrue(introspectItems.get(0).acronym().equals("nkp"));
        Assert.assertNotNull(introspectItems.get(0).response());

        Assert.assertTrue(introspectItems.get(1).acronym().equals("mzk"));
        Assert.assertNotNull(introspectItems.get(1).response());

        Assert.assertFalse(response.isConflictedResult());
        Assert.assertEquals(response.getUniqueModels(), Arrays.asList("monographunit"));
        Assert.assertEquals(response.getUniqueRootPids(), Arrays.asList("uuid:9056a3d0-fca3-11ee-96e1-005056827e51"));

        Assert.assertNotNull(response.getPivotItem());
        Assert.assertEquals(response.getPivotItem().pid(), "uuid:5c1f0af0-31a5-11e3-8d9d-005056827e51");
        Assert.assertEquals(response.getPivotItem().rootPid(), "uuid:9056a3d0-fca3-11ee-96e1-005056827e51");
        Assert.assertEquals(response.getPivotItem().model(), "monographunit");
        Assert.assertEquals(response.getPivotItem().getFirtsPidPath(), "uuid:9056a3d0-fca3-11ee-96e1-005056827e51/uuid:5c1f0af0-31a5-11e3-8d9d-005056827e51");
        Assert.assertEquals(response.getPivotItem().getOwnParent(), "uuid:9056a3d0-fca3-11ee-96e1-005056827e51");

        Assert.assertFalse(response.getPivotItem().isTopLevelModel());
    }
}
