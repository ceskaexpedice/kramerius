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

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import cz.incad.kramerius.utils.conf.KConfiguration;
import org.json.JSONObject;

/**
 * ApiWorkModeServiceImpl for Jersey 3 / Jakarta
 */
public class ApiWorkModeServiceImpl implements WorkModeService {

    private final String workModeUrl;
    private final String authToken;

    public ApiWorkModeServiceImpl(String authToken) {
        this.authToken = authToken;

        String adminPoint = KConfiguration.getInstance()
                .getConfiguration()
                .getString("api.admin.v7.point");

        if (!adminPoint.endsWith("/")) {
            adminPoint = adminPoint + "/";
        }

        workModeUrl = adminPoint + "workmode";
    }

    @Override
    public void setWorkMode(WorkMode workMode) {
        JSONObject requestJson = new JSONObject();
        requestJson.put("readOnly", workMode.isReadOnly());
        requestJson.put("reason", workMode.getReason().name());

        Client client = ClientBuilder.newClient();
        WebTarget target = client.target(workModeUrl);

        try (Response response = target
                .request(MediaType.APPLICATION_JSON)
                .header("parent-process-auth-token", authToken)
                .put(Entity.json(requestJson.toString()))) {

            if (response.getStatus() != 200) {
                throw new RuntimeException(
                        "Failed to update readOnly mode: HTTP error code " + response.getStatus()
                );
            }
        } finally {
            client.close();
        }
    }

    @Override
    public WorkMode getWorkMode() {
        Client client = ClientBuilder.newClient();
        WebTarget target = client.target(workModeUrl);

        String collectionJSON;
        try (Response response = target
                .request(MediaType.APPLICATION_JSON)
                .header("parent-process-auth-token", authToken)
                .get()) {

            collectionJSON = response.readEntity(String.class);
        } finally {
            client.close();
        }

        JSONObject collectionObject = new JSONObject(collectionJSON);
        boolean readOnly = collectionObject.optBoolean("readOnly", false);
        String reason = collectionObject.optString("reason");

        return new WorkMode(readOnly, WorkModeReason.valueOf(reason));
    }
}