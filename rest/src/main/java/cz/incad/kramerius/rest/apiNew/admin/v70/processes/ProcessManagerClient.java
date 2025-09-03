/*
 * Copyright (C) 2025 Inovatika
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
package cz.incad.kramerius.rest.apiNew.admin.v70.processes;

import com.fasterxml.jackson.databind.ObjectMapper;
import cz.incad.kramerius.rest.apiNew.exceptions.InternalErrorException;
import cz.incad.kramerius.utils.conf.KConfiguration;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.net.URIBuilder;
import org.json.JSONObject;

import javax.inject.Inject;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Logger;

/**
 * ProcessManagerClient
 * @author ppodsednik
 */
// TODO pepo
public class ProcessManagerClient {

    private static final Logger LOGGER = Logger.getLogger(ProcessManagerClient.class.getName());

    private final CloseableHttpClient closeableHttpClient;
    private final ObjectMapper mapper = new ObjectMapper();
    private final String baseUrl;

    @Inject
    ProcessManagerClient(CloseableHttpClient closeableHttpClient) {
        this.closeableHttpClient = closeableHttpClient;
        baseUrl = KConfiguration.getInstance().getProcessManagerURL();
    }

    public JSONObject getOwners() {
        URIBuilder uriBuilder;
        HttpGet get;
        try {
            //String baseUrl = "http://localhost:8088/process-manager/api/";
            uriBuilder = new URIBuilder(baseUrl + "process/owner");
//            uriBuilder = new URIBuilder(workerConfiguration.getManagerBaseUrl() + "process/owner");
            URI uri = uriBuilder.build();
            get = new HttpGet(uri);
        } catch (URISyntaxException e) {
            throw new InternalErrorException(e.toString(), e);
        }
        int statusCode = -1;
        try (CloseableHttpResponse response = closeableHttpClient.execute(get)) {
            int code = response.getCode();
            if (code == 200) {
                HttpEntity entity = response.getEntity();
                String json = EntityUtils.toString(entity);

                JSONObject jsonObject = new JSONObject(json);


                //ScheduledProcess process = mapper.readValue(json, ScheduledProcess.class);
                return jsonObject;
            } else if (code == 404) {
                return null;
            } else {
                throw new InternalErrorException("Failed to get next scheduled process");
            }
        } catch (IOException | ParseException e) {
            throw new InternalErrorException(e.getMessage(), e);
        }
    }

}
