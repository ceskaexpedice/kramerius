package cz.incad.kramerius.services.workers.copy.cdk.model;

import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.io.HttpClientResponseHandler;
import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class CDKConflictFeederItemTest {

    @Test
    public void existingConflictDetectsOnlyMultipleCompositeIds() {
        Assert.assertFalse(new CDKExistingConflictFeederItem("uuid:1", null).isConflict());
        Assert.assertFalse(new CDKExistingConflictFeederItem("uuid:1", Collections.singletonList("root!uuid:1")).isConflict());
        Assert.assertTrue(new CDKExistingConflictFeederItem("uuid:1", Arrays.asList("root1!uuid:1", "root2!uuid:1")).isConflict());
    }

    @Test
    public void existingConflictReharvestsDistinctCompositeRootPids() throws Exception {
        CloseableHttpClient client = EasyMock.createMock(CloseableHttpClient.class);
        expectSuccessfulRequest(client, "https://reharvest.example/resolveconflicts/root1,root2");
        EasyMock.replay(client);

        CDKExistingConflictFeederItem item = new CDKExistingConflictFeederItem(
                "uuid:child",
                Arrays.asList("root1!uuid:child", "root2!uuid:child", "root1!uuid:child"));

        item.reharvestConflict(client, "https://reharvest.example");

        EasyMock.verify(client);
    }

    @Test
    public void newConflictReharvestsRootPidsInGivenOrder() throws Exception {
        CloseableHttpClient client = EasyMock.createMock(CloseableHttpClient.class);
        expectSuccessfulRequest(client, "https://reharvest.example/resolveconflicts/root1,root2");
        EasyMock.replay(client);

        CDKNewConflictFeederItem item = new CDKNewConflictFeederItem(
                "root1!uuid:child",
                Arrays.asList("root1", "root2"),
                document("uuid:child", "root1!uuid:child"));

        item.reharvestConflict(client, "https://reharvest.example");

        EasyMock.verify(client);
    }

    private void expectSuccessfulRequest(CloseableHttpClient client, String expectedUri) throws Exception {
        EasyMock.expect(client.execute(
                EasyMock.isA(ClassicHttpRequest.class),
                EasyMock.isA(HttpClientResponseHandler.class))).andAnswer(() -> {
            Object[] arguments = EasyMock.getCurrentArguments();
            ClassicHttpRequest request = (ClassicHttpRequest) arguments[0];
            HttpClientResponseHandler<?> handler = (HttpClientResponseHandler<?>) arguments[1];

            Assert.assertEquals(expectedUri, request.getUri().toString());
            Assert.assertEquals("application/json", request.getHeader("Accept").getValue());

            ClassicHttpResponse response = EasyMock.createMock(ClassicHttpResponse.class);
            EasyMock.expect(response.getCode()).andReturn(200).anyTimes();
            EasyMock.replay(response);
            Object result = handler.handleResponse(response);
            EasyMock.verify(response);
            return result;
        });
    }

    private Map<String, Object> document(String pid, String compositeId) {
        Map<String, Object> document = new HashMap<>();
        document.put("pid", pid);
        document.put("compositeId", compositeId);
        return document;
    }
}
