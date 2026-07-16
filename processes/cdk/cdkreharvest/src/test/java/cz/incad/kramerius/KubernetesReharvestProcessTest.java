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
package cz.incad.kramerius;

import cz.incad.kramerius.services.utils.kubernetes.KubernetesEnvSupport;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.message.BasicClassicHttpResponse;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

@Ignore
public class KubernetesReharvestProcessTest {

    @Test
    public void testRunWithKubernetesReharvestConfigChangesStatePodAndDeletesFoundCompositeId() throws Exception {
        Map<String, String> env = new HashMap<>();
        env.put("ITERATION_BATCH", "30");
        env.put("ITERATION_ROWS", "4000");
        env.put("ITERATION_TYPE", "PAGINATION");
        env.put(KubernetesEnvSupport.ITERATION_URL, "http://cloud-solr.solr:8983/solr/search_v2");
        env.put(KubernetesEnvSupport.DESTINATION_URL, "http://cloud-solr.solr:8983/solr/search_v2");
        env.put(KubernetesEnvSupport.PROXY_API_URL, "https://api.ceskadigitalniknihovna.cz/search/api/admin/v7.0/connected");
        env.put(KubernetesEnvSupport.REHARVEST_URL, "https://api.ceskadigitalniknihovna.cz/search/api/admin/v7.0/reharvest");
        env.put(KubernetesReharvestProcess.MAX_ITEMS_TO_DELETE, "300000");
        env.put("HOSTNAME", "reharvest-pod-1");

        RecordingHttpClient client = new RecordingHttpClient();
        client.respond("GET", "https://api.ceskadigitalniknihovna.cz/search/api/admin/v7.0/reharvest/top?state=open",
                "{"
                        + "\"id\":\"reharvest-1\","
                        + "\"name\":\"test\","
                        + "\"state\":\"open\","
                        + "\"pid\":\"uuid:pid-1\","
                        + "\"root.pid\":\"uuid:root-1\","
                        + "\"own_pid_path\":\"uuid:root-1/uuid:pid-1\","
                        + "\"type\":\"delete_pid\","
                        + "\"libraries\":[\"lib1\"]"
                        + "}");
        client.respond("GET", "https://api.ceskadigitalniknihovna.cz/search/api/admin/v7.0/connected", "{\"lib1\":{\"status\":false}}");
        client.respond("POST", "http://cloud-solr.solr:8983/solr/search_v2/update?commit=true", "<response/>");
        client.respondByPath("GET", "http://cloud-solr.solr:8983/solr/search_v2/select", solrResponse());

        AtomicReference<String> idReference = new AtomicReference<>();

        KubernetesReharvestProcess.run(env, client, idReference);

        Assert.assertEquals("reharvest-1", idReference.get());
        Assert.assertTrue(client.requested("PUT", "https://api.ceskadigitalniknihovna.cz/search/api/admin/v7.0/reharvest/reharvest-1/state?state=running"));
        Assert.assertTrue(client.requested("PUT", "https://api.ceskadigitalniknihovna.cz/search/api/admin/v7.0/reharvest/reharvest-1/pod?pod=reharvest-pod-1"));
        Assert.assertTrue(client.requested("PUT", "https://api.ceskadigitalniknihovna.cz/search/api/admin/v7.0/reharvest/reharvest-1/state?state=closed"));
        Assert.assertFalse(client.requested("PUT", "https://api.ceskadigitalniknihovna.cz/search/api/admin/v7.0/reharvest/reharvest-1/state?state=too_big"));
        Assert.assertFalse(client.requested("PUT", "https://api.ceskadigitalniknihovna.cz/search/api/admin/v7.0/reharvest/reharvest-1/state?state=failed"));

        RecordedRequest solrRequest = client.findByPath("GET", "http://cloud-solr.solr:8983/solr/search_v2/select");
        Assert.assertNotNull(solrRequest);
        Assert.assertTrue(solrRequest.uri.getRawQuery().contains("start=0"));
        Assert.assertTrue(solrRequest.uri.getRawQuery().contains("rows=4000"));
        Assert.assertTrue(solrRequest.uri.getRawQuery().contains("fq=pid%3A%22uuid%3Apid-1%22"));
        Assert.assertTrue(solrRequest.uri.getRawQuery().contains("fl=compositeId"));

        RecordedRequest deleteRequest = client.find("POST", "http://cloud-solr.solr:8983/solr/search_v2/update?commit=true");
        Assert.assertNotNull(deleteRequest);
        Assert.assertTrue(deleteRequest.body.contains("<id>lib1!uuid:pid-1</id>"));
    }

    @Test
    public void testRunWithNewChildrenUsesOwnPidPathAndOnlyShowsConfiguration() throws Exception {
        Map<String, String> env = new HashMap<>();
        env.put("ITERATION_BATCH", "30");
        env.put("ITERATION_ROWS", "4000");
        env.put("ITERATION_TYPE", "CURSOR");
        env.put(KubernetesEnvSupport.ITERATION_URL, "http://cloud-solr.solr:8983/solr/search_v2");
        env.put(KubernetesEnvSupport.DESTINATION_URL, "http://cloud-solr.solr:8983/solr/search_v2");
        env.put(KubernetesEnvSupport.PROXY_API_URL, "https://api.ceskadigitalniknihovna.cz/search/api/admin/v7.0/connected");
        env.put(KubernetesEnvSupport.REHARVEST_URL, "https://api.ceskadigitalniknihovna.cz/search/api/admin/v7.0/reharvest");
        env.put(KubernetesReharvestProcess.ONLY_SHOW_CONFIGURATION, "true");
        env.put(KubernetesReharvestProcess.MAX_ITEMS_TO_DELETE, "300000");

        RecordingHttpClient client = new RecordingHttpClient();
        client.respond("GET", "https://api.ceskadigitalniknihovna.cz/search/api/admin/v7.0/reharvest/top?state=open",
                "{"
                        + "\"id\":\"reharvest-2\","
                        + "\"name\":\"test\","
                        + "\"state\":\"open\","
                        + "\"pid\":\"uuid:pid-2\","
                        + "\"root.pid\":\"uuid:root-2\","
                        + "\"own_pid_path\":\"uuid:root-2/uuid:pid-2\","
                        + "\"type\":\"new_children\","
                        + "\"libraries\":[\"lib1\"]"
                        + "}");
        client.respond("GET", "https://api.ceskadigitalniknihovna.cz/search/api/admin/v7.0/connected",
                "{\"lib1\":{\"status\":true}}");
        client.respond("GET", "https://api.ceskadigitalniknihovna.cz/search/api/admin/v7.0/connected/lib1/config",
                "{\"api\":\"v7\",\"forwardurl\":\"http://lib1.example/solr/search\"}");
        client.respond("GET", "http://lib1.example/solr/search/select?q=*:*&rows=0&wt=json", "{}");
        client.respondByPath("GET", "http://lib1.example/solr/search/select", lib1SolrResponse());
        client.respondByPath("GET", "http://cloud-solr.solr:8983/solr/search_v2/select", rootSolrResponse());

        AtomicReference<String> idReference = new AtomicReference<>();

        KubernetesReharvestProcess.run(env, client, idReference);

        Assert.assertEquals("reharvest-2", idReference.get());
        RecordedRequest iterationRequest = client.findByPath("GET", "http://cloud-solr.solr:8983/solr/search_v2/select");
        Assert.assertNotNull(iterationRequest);
        Assert.assertTrue(iterationRequest.uri.getRawQuery().contains("fq=own_pid_path%3Auuid%5C%3Aroot-2%2Fuuid%5C%3Apid-2*"));
        Assert.assertFalse(client.requested("PUT", "https://api.ceskadigitalniknihovna.cz/search/api/admin/v7.0/reharvest/reharvest-2/state?state=closed"));
    }

    private static String solrResponse() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + "<response>"
                + "<lst name=\"responseHeader\"><lst name=\"params\"><str name=\"cursorMark\">*</str></lst></lst>"
                + "<str name=\"nextCursorMark\">*</str>"
                + "<result name=\"response\" numFound=\"1\" start=\"0\">"
                + "<doc><str name=\"compositeId\">lib1!uuid:pid-1</str></doc>"
                + "</result>"
                + "</response>";
    }

    private static String rootSolrResponse() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + "<response>"
                + "<lst name=\"responseHeader\"><lst name=\"params\"><str name=\"cursorMark\">*</str></lst></lst>"
                + "<str name=\"nextCursorMark\">*</str>"
                + "<result name=\"response\" numFound=\"1\" start=\"0\">"
                + "<doc>"
                + "<str name=\"pid\">uuid:pid-2</str>"
                + "<str name=\"root.pid\">uuid:root-2</str>"
                + "<str name=\"own_pid_path\">uuid:root-2/uuid:pid-2</str>"
                + "</doc>"
                + "</result>"
                + "</response>";
    }

    private static String lib1SolrResponse() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + "<response>"
                + "<result name=\"response\" numFound=\"1\" start=\"0\">"
                + "<doc>"
                + "<str name=\"pid\">uuid:pid-2</str>"
                + "<str name=\"root.pid\">uuid:root-2</str>"
                + "<str name=\"own_pid_path\">uuid:root-2/uuid:pid-2</str>"
                + "</doc>"
                + "</result>"
                + "</response>";
    }

    private static class RecordingHttpClient extends CloseableHttpClient {

        private final Map<String, String> responses = new HashMap<>();
        private final Map<String, String> pathResponses = new HashMap<>();
        private final List<RecordedRequest> requests = new ArrayList<>();

        private void respond(String method, String uri, String body) {
            responses.put(method + " " + uri, body);
        }

        private void respondByPath(String method, String uriWithoutQuery, String body) {
            pathResponses.put(method + " " + uriWithoutQuery, body);
        }

        private boolean requested(String method, String uri) {
            return find(method, uri) != null;
        }

        private RecordedRequest find(String method, String uri) {
            for (RecordedRequest request : requests) {
                if (request.method.equals(method) && request.uri.toString().equals(uri)) {
                    return request;
                }
            }
            return null;
        }

        private RecordedRequest findByPath(String method, String uriWithoutQuery) {
            for (RecordedRequest request : requests) {
                String actual = request.uri.getScheme() + "://" + request.uri.getAuthority() + request.uri.getPath();
                if (request.method.equals(method) && actual.equals(uriWithoutQuery)) {
                    return request;
                }
            }
            return null;
        }

        @Override
        protected CloseableHttpResponse doExecute(HttpHost target, ClassicHttpRequest request, HttpContext context) throws IOException {
            URI uri = requestUri(request);
            String body = requestBody(request);
            requests.add(new RecordedRequest(request.getMethod(), uri, body));

            String responseBody = responses.get(request.getMethod() + " " + uri.toString());
            if (responseBody == null) {
                String uriWithoutQuery = uri.getScheme() + "://" + uri.getAuthority() + uri.getPath();
                responseBody = pathResponses.get(request.getMethod() + " " + uriWithoutQuery);
            }
            if (responseBody == null && "PUT".equals(request.getMethod())) {
                responseBody = "{}";
            }
            if (responseBody == null) {
                throw new IOException("Unexpected request " + request.getMethod() + " " + uri);
            }

            BasicClassicHttpResponse response = new BasicClassicHttpResponse(200);
            response.setEntity(new StringEntity(responseBody, StandardCharsets.UTF_8));
            return closeableResponse(response);
        }

        @Override
        public void close() {
        }

        @Override
        public void close(org.apache.hc.core5.io.CloseMode closeMode) {
        }

        private static URI requestUri(ClassicHttpRequest request) throws IOException {
            try {
                return request.getUri();
            } catch (URISyntaxException e) {
                throw new IOException(e);
            }
        }

        private static String requestBody(ClassicHttpRequest request) throws IOException {
            HttpEntity entity = request.getEntity();
            if (entity == null) {
                return "";
            }
            try (InputStream inputStream = entity.getContent()) {
                return org.apache.commons.io.IOUtils.toString(inputStream, "UTF-8");
            }
        }

        private static CloseableHttpResponse closeableResponse(BasicClassicHttpResponse response) throws IOException {
            try {
                Method adapt = CloseableHttpResponse.class.getDeclaredMethod(
                        "adapt", org.apache.hc.core5.http.ClassicHttpResponse.class);
                adapt.setAccessible(true);
                return (CloseableHttpResponse) adapt.invoke(null, response);
            } catch (NoSuchMethodException | IllegalAccessException e) {
                throw new IOException(e);
            } catch (InvocationTargetException e) {
                Throwable cause = e.getCause();
                if (cause instanceof IOException) {
                    throw (IOException) cause;
                }
                throw new IOException(cause);
            }
        }
    }

    private static class RecordedRequest {

        private final String method;
        private final URI uri;
        private final String body;

        private RecordedRequest(String method, URI uri, String body) {
            this.method = method;
            this.uri = uri;
            this.body = body;
        }
    }
}
