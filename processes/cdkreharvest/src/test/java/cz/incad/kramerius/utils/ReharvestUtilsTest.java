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

import com.sun.jersey.api.client.Client;
import cz.incad.kramerius.rest.apiNew.admin.v70.reharvest.ReharvestItem;
import cz.incad.kramerius.services.ParallelProcessImpl;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.core5.http.HttpHeaders;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.ws.rs.core.MediaType;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.easymock.EasyMock.createMockBuilder;

public class ReharvestUtilsTest {

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

}
