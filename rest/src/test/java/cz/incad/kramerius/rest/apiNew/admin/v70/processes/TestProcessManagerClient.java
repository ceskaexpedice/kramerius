/*
 * Copyright © 2021 Accenture and/or its affiliates. All Rights Reserved.
 * Permission to any use, copy, modify, and distribute this software and
 * its documentation for any purpose is subject to a licensing agreement
 * duly entered into with the copyright owner or its affiliate.
 * All information contained herein is, and remains the property of Accenture
 * and/or its affiliates and its suppliers, if any.  The intellectual and
 * technical concepts contained herein are proprietary to Accenture and/or
 * its affiliates and its suppliers and may be covered by one or more patents
 * or pending patent applications in one or more jurisdictions worldwide,
 * and are protected by trade secret or copyright law. Dissemination of this
 * information or reproduction of this material is strictly forbidden unless
 * prior written permission is obtained from Accenture and/or its affiliates.
 */
package cz.incad.kramerius.rest.apiNew.admin.v70.processes;

import cz.incad.kramerius.utils.conf.KConfiguration;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.*;
import org.junit.jupiter.api.Assertions;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;

import static cz.incad.kramerius.rest.apiNew.admin.v70.processes.ProcessManagerProcessEndpoint.OUT_LOG_PART;
import static org.mockito.ArgumentMatchers.eq;

// TODO pepo

/**
 * TestWorkerClient
 *
 * @author ppodsednik
 */
public class TestProcessManagerClient {
    public static final String MANAGER_BASE_URL = "http://localhost:9998/process-manager/api/";
    /*
    @Mock
    private ProcessService processServiceMock;
    @Mock
    private NodeService nodeServiceMock;

     */

    private HttpServer server;
    private ProcessManagerClient processManagerClient;

    @BeforeClass
    public static void beforeAll() throws URISyntaxException {
/*
        URL resource = TestProcessManagerClient.class.getResource("configuration.properties");
        Path filePath = Paths.get(resource.toURI());
        KConfiguration.setWorkingDir(filePath.getParent().toString());
*/

    }

    @Before
    public void setUp() throws Exception {
        /*
        MockitoAnnotations.openMocks(this);
        ProcessInfo processInfo = new ProcessInfo();
        processInfo.setProcessId(ManagerTestsUtils.PROCESS1_ID);
        processInfo.setWorkerId(ManagerTestsUtils.NODE_WORKER1_ID);
        when(processServiceMock.getProcess(eq(ManagerTestsUtils.PROCESS1_ID))).thenReturn(processInfo);
        Node node = new Node();
        node.setNodeId(ManagerTestsUtils.NODE_WORKER1_ID);
        node.setUrl(ManagerTestsUtils.WORKER_BASE_URI);
        when(nodeServiceMock.getNode(eq(ManagerTestsUtils.NODE_WORKER1_ID))).thenReturn(node);

         */

        final ResourceConfig rc = new ResourceConfig(ProcessManagerProcessEndpoint.class);
        server = GrizzlyHttpServerFactory.createHttpServer(URI.create(MANAGER_BASE_URL), rc);
        server.start();
        processManagerClient = new ProcessManagerClient(getClient());
    }

    @After
    public void tearDown() {
        server.shutdownNow();
    }

    @Ignore
    @Test
    public void testGetOwners() {
        JSONObject owners = processManagerClient.getOwners();
        JSONObject jsonObject = ProcessManagerMapper.mapOwners(owners);
        System.out.println(jsonObject);
        //Assertions.assertTrue(outLog.contains(OUT_LOG_PART));
    }

    @Ignore
    @Test
    public void testGetProcess() {
        JSONObject process = processManagerClient.getProcess("ed25ce29-2149-439d-85c4");
       // ProcessInBatch processInBatch = ProcessManagerMapper.mapProcess(process);
        JSONObject processInBatch = ProcessManagerMapper.mapProcess(process);
        System.out.println(processInBatch);
        //Assertions.assertTrue(outLog.contains(OUT_LOG_PART));
    }

    @Ignore
    @Test
    public void testGetBatches() {
        String DATE_STRING = "2025-09-07T14:30:00";
        JSONObject batches = processManagerClient.getBatches("0", "50", "PePo", DATE_STRING, DATE_STRING, "PLANNED");
        JSONArray jsonArray = batches.getJSONArray("batches");
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonObjectBatch = jsonArray.getJSONObject(i);

            JSONObject batchToJson = ProcessManagerMapper.mapBatchWithProcesses(jsonObjectBatch);

            System.out.println(jsonObjectBatch);
        }
        //ProcessInBatch processInBatch = ProcessManagerMapper.mapProcess(process);
        System.out.println(batches);
        //Assertions.assertTrue(outLog.contains(OUT_LOG_PART));
    }

    @Ignore
    @Test
    public void testGetProcessLog() throws IOException {
        InputStream processLog = processManagerClient.getProcessLog("ed25ce29-2149-439d-85c4", false);
        String outLog = new String(processLog.readAllBytes(), StandardCharsets.UTF_8);
        Assertions.assertTrue(outLog.contains(OUT_LOG_PART));
    }

    @Ignore
    @Test
    public void testDeleteBatch() throws IOException {
        processManagerClient.deleteBatch("ed25ce29-2149-439d-85c4");
    }

    private CloseableHttpClient getClient() {
        PoolingHttpClientConnectionManager poolConnectionManager = new PoolingHttpClientConnectionManager();
        RequestConfig requestConfig = RequestConfig.custom().build();
        CloseableHttpClient closeableHttpClient = HttpClients.custom()
                .setConnectionManager(poolConnectionManager)
                .disableAuthCaching()
                .disableCookieManagement()
                .setDefaultRequestConfig(requestConfig)
                .build();
        return closeableHttpClient;
    }
}
