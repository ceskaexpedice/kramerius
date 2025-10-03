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
package cz.incad.kramerius.rest.apiNew.admin.v70.processes.client;

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
import org.mockito.MockedStatic;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;

import static cz.incad.kramerius.rest.apiNew.admin.v70.processes.client.ProcessManagerProcessEndpoint.OUT_LOG_PART;
import static org.mockito.Mockito.*;

/**
 * TestProcessManagerClient
 *
 * @author ppodsednik
 */
public class TestProcessManagerClient {
    public static final String SCHEDULED_PROCESS_ID = "ed25ce29-2149-439d-85c4";
    public static final String PROFILE_ID = "testPlugin1-big";
    public static final String PLUGIN_ID = "testPlugin1";

    private static final String MANAGER_BASE_URL = "http://localhost:9998/process-manager/api/";

    private HttpServer server;
    private ProcessManagerClient processManagerClient;
    private MockedStatic<KConfiguration> kConfigStatic;

    @Before
    public void setUp() throws Exception {
        final ResourceConfig rc = new ResourceConfig(ProcessManagerProcessEndpoint.class,
                ProcessManagerProfileEndpoint.class, ProcessManagerPluginEndpoint.class);
        server = GrizzlyHttpServerFactory.createHttpServer(URI.create(MANAGER_BASE_URL), rc);
        server.start();

        // Mock static KConfiguration
        MockedStatic<KConfiguration> mockedStatic = mockStatic(KConfiguration.class);
        KConfiguration mockConfig = mock(KConfiguration.class);
        mockedStatic.when(KConfiguration::getInstance).thenReturn(mockConfig);
        when(mockConfig.getProcessManagerURL()).thenReturn(MANAGER_BASE_URL);
        processManagerClient = new ProcessManagerClient(getClient());
        this.kConfigStatic = mockedStatic;

        processManagerClient = new ProcessManagerClient(getClient());
    }

    @After
    public void tearDown() {
        if (kConfigStatic != null) {
            kConfigStatic.close(); // release static mock
        }
        server.shutdownNow();
    }

    @Test
    public void testScheduleProcess() {
        String scheduleMainProcess = "            {" +
                "              \"profileId\" : \"testPlugin1-big\"," +
                "              \"payload\" : {" +
                "                \"surname\" : \"Po\"," +
                "                \"name\" : \"Pe\"" +
                "              }," +
                "              \"ownerId\" : \"PePo\"" +
                "            }";
        String processId = processManagerClient.scheduleProcess(new JSONObject(scheduleMainProcess));
        Assertions.assertEquals(SCHEDULED_PROCESS_ID, processId);
    }

    @Test
    public void testGetOwners() {
        JSONObject owners = processManagerClient.getOwners();
        Assertions.assertEquals(2, owners.getJSONArray("owners").length());
    }

    @Test
    public void testGetProcess() {
        JSONObject process = processManagerClient.getProcess(SCHEDULED_PROCESS_ID);
        Assertions.assertEquals(SCHEDULED_PROCESS_ID, process.getString("processId"));
    }

    @Test
    public void testGetProfile() {
        JSONObject profile = processManagerClient.getProfile(PROFILE_ID);
        Assertions.assertEquals(PROFILE_ID, profile.getString("profileId"));
    }

    @Test
    public void testGetPlugin() {
        JSONObject profile = processManagerClient.getPlugin(PLUGIN_ID);
        Assertions.assertEquals(PLUGIN_ID, profile.getString("pluginId"));
    }

    @Test
    public void testGetBatches() {
        String DATE_STRING = "2025-09-07T14:30:00";
        JSONObject pcpBatches = processManagerClient.getBatches("0", "50", "PePo", DATE_STRING, DATE_STRING, "PLANNED");
        JSONArray jsonArray = pcpBatches.getJSONArray("batches");
        Assertions.assertEquals(2, jsonArray.length());
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject pcpBatch = jsonArray.getJSONObject(i);
        }
    }

    @Test
    public void testGetProcessLog() throws IOException {
        InputStream processLog = processManagerClient.getProcessLog(SCHEDULED_PROCESS_ID, false);
        String outLog = new String(processLog.readAllBytes(), StandardCharsets.UTF_8);
        Assertions.assertTrue(outLog.contains(OUT_LOG_PART));
    }

    @Test
    public void testGetProcessLogLines() {
        JSONObject logLines = processManagerClient.getProcessLogLines(SCHEDULED_PROCESS_ID, "0", "50", false);
        Assertions.assertEquals(2, logLines.getJSONArray("lines").length());
    }

    @Test
    public void testDeleteBatch() {
        int deleted = processManagerClient.deleteBatch(SCHEDULED_PROCESS_ID);
        Assertions.assertEquals(2, deleted);
    }

    @Test
    public void testKillBatch() {
        int killed = processManagerClient.killBatch(SCHEDULED_PROCESS_ID);
        Assertions.assertEquals(2, killed);
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
