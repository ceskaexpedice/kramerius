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
package cz.incad.kramerius.utils;

import cz.incad.kramerius.rest.apiNew.admin.v70.reharvest.ReharvestItem;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.message.BasicClassicHttpResponse;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.junit.Assert;
import org.junit.Test;
import org.json.JSONObject;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReharvestUtilsTest {

    @Test
    public void testFindPidByTypeUsesCursorIteratorAndReturnsUniqueCompositeIds() throws Exception {
        ReharvestItem item = new ReharvestItem("id", "reharvest", "open",
                "uuid:root", "uuid:root/uuid:child");
        item.setTypeOfReharvest(ReharvestItem.TypeOfReharvset.delete_tree);

        Map<String, String> iteration = new HashMap<>();
        iteration.put("url", "http://solr.example/solr/search");
        iteration.put("type", "CURSOR");
        iteration.put("rows", "2");

        CapturingSolrClient client = new CapturingSolrClient(
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                        + "<response>"
                        + "<lst name=\"responseHeader\"><lst name=\"params\"><str name=\"cursorMark\">*</str></lst></lst>"
                        + "<str name=\"nextCursorMark\">*</str>"
                        + "<result name=\"response\" numFound=\"3\" start=\"0\">"
                        + "<doc><str name=\"compositeId\">lib1!uuid:root</str></doc>"
                        + "<doc><str name=\"compositeId\">lib1!uuid:child</str></doc>"
                        + "<doc><str name=\"compositeId\">lib1!uuid:child</str></doc>"
                        + "</result>"
                        + "</response>");

        List<Pair<String, String>> pairs = ReharvestUtils.findPidByType(iteration, client, item, 10);

        Assert.assertEquals(2, pairs.size());
        Assert.assertEquals("uuid:root", pairs.get(0).getLeft());
        Assert.assertEquals("lib1!uuid:root", pairs.get(0).getRight());
        Assert.assertEquals("uuid:child", pairs.get(1).getLeft());
        Assert.assertEquals("lib1!uuid:child", pairs.get(1).getRight());

        URI requestedUri = client.getRequestedUris().get(0);
        Map<String, String> query = splitQuery(requestedUri);
        Assert.assertEquals("http://solr.example/solr/search/select", requestedUri.getScheme() + "://"
                + requestedUri.getAuthority() + requestedUri.getPath());
        Assert.assertEquals("*:*", query.get("q"));
        Assert.assertEquals("2", query.get("rows"));
        Assert.assertEquals("*", query.get("cursorMark"));
        Assert.assertEquals("compositeId asc", query.get("sort"));
        Assert.assertEquals("compositeId", query.get("fl"));
        Assert.assertEquals("own_pid_path:uuid\\:root/uuid\\:child*", query.get("fq"));
        Assert.assertEquals("xml", query.get("wt"));
    }

    @Test
    public void testReharestUtilsFQV5() throws UnsupportedEncodingException {

        ReharvestItem itemRootPid1 = new ReharvestItem("id","reharvest","open","uuid:xxxx","uuid:xxxx");
        itemRootPid1.setRootPid("uuid:delete");
        itemRootPid1.setTypeOfReharvest(ReharvestItem.TypeOfReharvset.root);
        String fqRP1 = ReharvestUtils.fq("v5", itemRootPid1, true);
        Assert.assertEquals(URLEncoder.encode("root_pid:\"uuid:delete\"","UTF-8"), fqRP1);

        HttpGet testGet = new HttpGet(String.format("http://test_tunel/solr?q=%s",fqRP1));
        Assert.assertNotNull(testGet);

        ReharvestItem itemRootPid2 = new ReharvestItem("id","reharvest","open",
                "uuid:xxxx",
                "uuid:xxxx");
        itemRootPid2.setTypeOfReharvest(ReharvestItem.TypeOfReharvset.delete_root);
        itemRootPid2.setRootPid("uuid:delete");
        String fqRP2 = ReharvestUtils.fq("v5", itemRootPid2, true);
        Assert.assertEquals(URLEncoder.encode("root_pid:\"uuid:delete\"","UTF-8") , fqRP2);

        testGet = new HttpGet(String.format("http://test_tunel/solr?q=%s",fqRP2));
        Assert.assertNotNull(testGet);


        ReharvestItem onlyPid = new ReharvestItem("id","reharvest","open",
                "uuid:xxxx",
                "uuid:xxxx/uuid:yyyy");
        onlyPid.setTypeOfReharvest(ReharvestItem.TypeOfReharvset.only_pid);
        String fqOnlyPid = ReharvestUtils.fq("v5", onlyPid, true);
        Assert.assertEquals(URLEncoder.encode("PID:\"uuid:xxxx\"","UTF-8"), fqOnlyPid);

        testGet = new HttpGet(String.format("http://test_tunel/solr?q=%s",fqOnlyPid));
        Assert.assertNotNull(testGet);


        ReharvestItem itemPid = new ReharvestItem("id","reharvest","open",
                "uuid:xxxx",
                "uuid:xxxx");
        itemPid.setTypeOfReharvest(ReharvestItem.TypeOfReharvset.delete_pid);
        String fqP = ReharvestUtils.fq("v5", itemPid, true);
        Assert.assertEquals(URLEncoder.encode("PID:\"uuid:xxxx\"","UTF-8"), fqP);

        testGet = new HttpGet(String.format("http://test_tunel/solr?q=%s",fqP));
        Assert.assertNotNull(testGet);


        ReharvestItem deleteTree = new ReharvestItem("id","reharvest","open",
                "uuid:xxxx",
                "uuid:xxxx/uuid:yyyy");
        deleteTree.setTypeOfReharvest(ReharvestItem.TypeOfReharvset.delete_tree);
        String fqTree = ReharvestUtils.fq("v5", deleteTree, true);
        Assert.assertEquals(URLEncoder.encode("pid_path:uuid\\:xxxx/uuid\\:yyyy*", "UTF-8") , fqTree);
        testGet = new HttpGet(String.format("http://test_tunel/solr?q=%s",fqTree));
        Assert.assertNotNull(testGet);


        ReharvestItem childrenTree = new ReharvestItem("id","reharvest","open",
                "uuid:xxxx",
                "uuid:xxxx/uuid:yyyy");
        childrenTree.setTypeOfReharvest(ReharvestItem.TypeOfReharvset.children);
        String fqChildren = ReharvestUtils.fq("v5", childrenTree, true);
        Assert.assertEquals(URLEncoder.encode("pid_path:uuid\\:xxxx/uuid\\:yyyy*", "UTF-8"), fqChildren);

        testGet = new HttpGet(String.format("http://test_tunel/solr?q=%s",fqChildren));
        Assert.assertNotNull(testGet);

    }


    @Test
    public void testReharestUtilsFQV7() throws UnsupportedEncodingException {

        ReharvestItem itemRootPid1 = new ReharvestItem("id","reharvest","open",
                "uuid:xxxx",
                "uuid:xxxx");
        itemRootPid1.setRootPid("uuid:delete");
        itemRootPid1.setTypeOfReharvest(ReharvestItem.TypeOfReharvset.root);
        String fqRP1 = ReharvestUtils.fq("v7", itemRootPid1, true);
        Assert.assertEquals(URLEncoder.encode("root.pid:\"uuid:delete\"","UTF-8"),fqRP1);

        HttpGet testGet = new HttpGet(String.format("http://test_tunel/solr?q=%s",fqRP1));
        Assert.assertNotNull(testGet);


        ReharvestItem itemRootPid2 = new ReharvestItem("id","reharvest","open","uuid:xxxx","uuid:xxxx");
        itemRootPid2.setTypeOfReharvest(ReharvestItem.TypeOfReharvset.delete_root);
        itemRootPid2.setRootPid("uuid:delete");
        String fqRP2 = ReharvestUtils.fq("v7", itemRootPid2, true);
        Assert.assertEquals(URLEncoder.encode("root.pid:\"uuid:delete\"","UTF-8"),fqRP2);

        testGet = new HttpGet(String.format("http://test_tunel/solr?q=%s",fqRP2));
        Assert.assertNotNull(testGet);

        ReharvestItem onlyPid = new ReharvestItem("id","reharvest","open",
                "uuid:xxxx",
                "uuid:xxxx/uuid:yyyy");
        onlyPid.setTypeOfReharvest(ReharvestItem.TypeOfReharvset.only_pid);
        String fqOnlyPid = ReharvestUtils.fq("v7", onlyPid, true);
        Assert.assertEquals(URLEncoder.encode("pid:\"uuid:xxxx\"","UTF-8"), fqOnlyPid);

        testGet = new HttpGet(String.format("http://test_tunel/solr?q=%s",fqOnlyPid));
        Assert.assertNotNull(testGet);

        ReharvestItem itemPid = new ReharvestItem("id","reharvest","open",
                "uuid:xxxx",
                "uuid:xxxx");
        itemPid.setTypeOfReharvest(ReharvestItem.TypeOfReharvset.delete_pid);
        String fqP = ReharvestUtils.fq("v7", itemPid, true);
        Assert.assertEquals(URLEncoder.encode("pid:\"uuid:xxxx\"","UTF-8"), fqP);

        testGet = new HttpGet(String.format("http://test_tunel/solr?q=%s",fqP));
        Assert.assertNotNull(testGet);


        ReharvestItem deleteTree = new ReharvestItem("id","reharvest","open",
                "uuid:xxxx",
                "uuid:xxxx/uuid:yyyy");
        deleteTree.setTypeOfReharvest(ReharvestItem.TypeOfReharvset.delete_tree);
        String fqTree = ReharvestUtils.fq("v7", deleteTree, true);

        testGet = new HttpGet(String.format("http://test_tunel/solr?q=%s",fqTree));
        Assert.assertNotNull(testGet);


        Assert.assertEquals(URLEncoder.encode("own_pid_path:uuid\\:xxxx/uuid\\:yyyy*","UTF-8"), fqTree);

        ReharvestItem childrenTree = new ReharvestItem("id","reharvest","open","uuid:xxxx","uuid:xxxx/uuid:yyyy");
        childrenTree.setTypeOfReharvest(ReharvestItem.TypeOfReharvset.children);
        String fqChildren = ReharvestUtils.fq("v7", childrenTree, true);

        Assert.assertEquals(URLEncoder.encode("own_pid_path:uuid\\:xxxx/uuid\\:yyyy*","UTF-8"), fqChildren);

        testGet = new HttpGet(String.format("http://test_tunel/solr?q=%s",fqChildren));
        Assert.assertNotNull(testGet);

    }


    @Test
    public void testReharvestDelete() throws ParserConfigurationException, IOException, SAXException, TransformerException {

        Map<String,String> destMap = new HashMap<>();
        destMap.put("url","http://localhost:8983/search");

        List<Pair<String,String>> allPidList = new ArrayList<>();
        allPidList.add(Pair.of("uuid:1","uuid:r1!uuid:1"));
        allPidList.add(Pair.of("uuid:2","uuid:r2!uuid:2"));
        allPidList.add(Pair.of("uuid:3","uuid:r3!uuid:3"));
        allPidList.add(Pair.of("uuid:4","uuid:r4!uuid:4"));
        allPidList.add(Pair.of("uuid:5","uuid:r5!uuid:5"));
        allPidList.add(Pair.of("uuid:6","uuid:r6!uuid:6"));

        String content = ReharvestUtils.deleteAllGivenPids(null, destMap, allPidList, true);
        Assert.assertEquals(content, "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><delete><id>uuid:r1!uuid:1</id><id>uuid:r2!uuid:2</id><id>uuid:r3!uuid:3</id><id>uuid:r4!uuid:4</id><id>uuid:r5!uuid:5</id><id>uuid:r6!uuid:6</id></delete>\n");

    }

    @Test
    public void testReharvestV7ConfigurationShowsSolrAddFields() throws IOException {
        Map<String, String> iteration = new HashMap<>();
        iteration.put("dl", "vkol");
        iteration.put("url", "http://source.example/solr/search");
        iteration.put("id", "compositeId");
        iteration.put("batch", "45");
        iteration.put("fquery", "cdk.collection:vkol");

        Map<String, String> destination = new HashMap<>();
        destination.put("url", "http://destination.example/solr/search");

        String configuration = compact(ReharvestUtils.renderTemplate("v7", iteration, destination));

        Assert.assertTrue(configuration.contains(
                "<onindex>"
                        + "<remove.dest.field><field name=\"collection\"></field></remove.dest.field>"
                        + "<update.dest.field>"
                        + "<field name=\"cdk.collection\">vkol</field>"
                        + "<field name=\"cdk.leader\">vkol</field>"
                        + "</update.dest.field>"
                        + "</onindex>"));

        Assert.assertTrue(configuration.contains(
                "<onupdate>"
                        + "<fieldlist>pid root.pid licenses contains_licenses licenses_of_ancestors titles.* collection.* in_collections in_collections.* title.* titles.* text_ocr</fieldlist>"
                        + "<update.dest.field>"
                        + "<field name=\"cdk.collection\" update=\"add-distinct\">vkol</field>"
                        + "</update.dest.field>"
                        + "</onupdate>"));
    }

    @Test
    public void findRootsAndPidPathsFromLibsUsesOwnPidPathAndRootPidSeparately() throws Exception {
        Map<String, JSONObject> configurations = new HashMap<>();
        JSONObject config = new JSONObject();
        config.put("api", "v7");
        config.put("forwardurl", "http://solr.example/solr/search");
        configurations.put("lib1", config);

        CapturingSolrClient client = new CapturingSolrClient(
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                        + "<response>"
                        + "<result name=\"response\" numFound=\"1\" start=\"0\">"
                        + "<doc>"
                        + "<str name=\"pid\">uuid:child</str>"
                        + "<str name=\"root.pid\">uuid:root</str>"
                        + "<str name=\"own_pid_path\">uuid:root/uuid:child</str>"
                        + "</doc>"
                        + "</result>"
                        + "</response>");

        Map<String, Map<String, String>> result = ReharvestUtils.findRootsAndPidPathsFromLibs(
                client,
                "uuid:child",
                configurations);

        Assert.assertEquals("uuid:root/uuid:child", result.get("lib1").get("own_pid_path"));
        Assert.assertEquals("uuid:root", result.get("lib1").get("root.pid"));
    }

    private static String compact(String xml) {
        return xml.replaceAll("(?s)<!--.*?-->", "")
                .replaceAll(">\\s+<", "><")
                .trim();
    }

    private static Map<String, String> splitQuery(URI uri) {
        Map<String, String> params = new HashMap<>();
        for (String pair : uri.getRawQuery().split("&")) {
            int idx = pair.indexOf('=');
            String key = idx >= 0 ? pair.substring(0, idx) : pair;
            String value = idx >= 0 ? pair.substring(idx + 1) : "";
            params.put(
                    decode(key),
                    decode(value));
        }
        return params;
    }

    private static String decode(String value) {
        try {
            return URLDecoder.decode(value, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    private static class CapturingSolrClient extends CloseableHttpClient {

        private final String body;
        private final List<URI> requestedUris = new ArrayList<>();

        private CapturingSolrClient(String body) {
            this.body = body;
        }

        private List<URI> getRequestedUris() {
            return requestedUris;
        }

        @Override
        protected CloseableHttpResponse doExecute(HttpHost target, ClassicHttpRequest request, HttpContext context) throws IOException {
            try {
                requestedUris.add(request.getUri());
            } catch (URISyntaxException e) {
                throw new IOException(e);
            }
            BasicClassicHttpResponse response = new BasicClassicHttpResponse(200);
            response.setEntity(new StringEntity(body, StandardCharsets.UTF_8));
            return closeableResponse(response);
        }

        @Override
        public void close() {
        }

        @Override
        public void close(org.apache.hc.core5.io.CloseMode closeMode) {
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

}
