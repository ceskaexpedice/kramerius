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
package cz.incad.kramerius.workmode;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import cz.incad.kramerius.utils.conf.KConfiguration;
import org.json.JSONObject;

import javax.ws.rs.core.MediaType;

/**
 * ApiWorkModeServiceImpl
 * @author ppodsednik
 */
public class ApiWorkModeServiceImpl implements WorkModeService {
    private final String workModeUrl;
    private String authToken;

    public ApiWorkModeServiceImpl(String authToken) {
        this.authToken = authToken;
        String adminPoint = KConfiguration.getInstance().getConfiguration().getString("api.admin.v7.point");
        if (!adminPoint.endsWith("/")) adminPoint = adminPoint + "/";
        workModeUrl = adminPoint + "workmode";
    }

    @Override
    public void setWorkMode(WorkMode workMode) {
        JSONObject requestJson = new JSONObject();
        requestJson.put("readOnly", workMode.isReadOnly());
        requestJson.put("reason", workMode.getReason().name());

        Client client = Client.create();
        WebResource resource = client.resource(workModeUrl);
        ClientResponse response = resource
                .header("parent-process-auth-token", authToken)
                .type(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .put(ClientResponse.class, requestJson.toString());

        if (response.getStatus() != 200) {
            throw new RuntimeException("Failed to update readOnly mode: HTTP error code " + response.getStatus());
        }
    }

    @Override
    public WorkMode getWorkMode() {
        Client c = Client.create();
        WebResource resource = c.resource(workModeUrl);

        String collectionJSON = resource
                .header("parent-process-auth-token", authToken)
                .accept(MediaType.APPLICATION_JSON)
                .get(String.class);

        JSONObject collectionObject = new JSONObject(collectionJSON);
        boolean readOnly = collectionObject.optBoolean("readOnly", false);
        String reason = collectionObject.optString("reason");
        return new WorkMode(readOnly, WorkModeReason.valueOf(reason));
    }

}
