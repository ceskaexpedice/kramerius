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
package cz.incad.kramerius.services.workers.copy.cdk.model;

import cz.incad.kramerius.utils.StringUtils;
import org.apache.hc.client5.http.async.methods.SimpleHttpResponse;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;

import org.apache.hc.core5.http.io.entity.EntityUtils;

import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class CDKNewConflictWorkerItem extends CDKWorkerIndexedItem implements Conflict {

    public static final  Logger LOGGER = Logger.getLogger(CDKNewConflictWorkerItem.class.getName());

    private List<String> rootPids;

    public CDKNewConflictWorkerItem(String idField, List<String> rootPids, Map<String, Object> document) {
        super(idField, document);
        this.rootPids = rootPids;
    }

    public List<String> getRootPids() {
        return rootPids;
    }

    // plan reharvest
    @Override
    public void reharvestConflict(CloseableHttpClient client, String reharvestApi) {

        if (reharvestApi != null && !reharvestApi.isEmpty()) {
            // Spojení PIDů do čárkou odděleného seznamu
            String pids = String.join(",", this.rootPids);

            String resolveConflictsUrl = String.format("%s/resolveconflicts/%s", reharvestApi, pids);
            LOGGER.info(String.format("Calling resolve conflict at: %s", resolveConflictsUrl));

            HttpGet request = new HttpGet(resolveConflictsUrl);
            request.setHeader("Accept", "application/json");


            try {

                client.execute(request, (response) -> {
                    int status = response.getCode();

                    if (status == 200) { // Status.OK
                        LOGGER.log(Level.INFO, String.format("Resolving conflict for %s has been planned", pids));
                    } else {
                        String errorBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
                        throw new RuntimeException(String.format("Cannot resolve conflict. HTTP status %d, Error message %s", status, errorBody));
                    }
                    return null; // ResponseHandler musí něco vrátit nebo null
                });
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "Network error during reharvest conflict resolution", e);
                throw new RuntimeException("Failed to contact reharvest API", e);
            }
        } else {
            LOGGER.warning("Reharvest API not defined");
        }
    }
//        if (StringUtils.isAnyString(reharvestApi)) {
//            String pids = this.rootPids.stream().collect(Collectors.joining(","));
//
//            String resolveConflicts = String.format("%s/resolveconflicts/%s", reharvestApi,pids);
//            WebResource resharvestResource = client.resource(resolveConflicts);
//            ClientResponse resolveConflictResp = resharvestResource.accept(MediaType.APPLICATION_JSON)
//                    .get(ClientResponse.class);
//            if (resolveConflictResp.getStatus() == ClientResponse.Status.OK.getStatusCode()) {
//                LOGGER.log(Level.INFO, String.format("Resolving conflict for %s has been planned", pids));
//            } else {
//                int status = resolveConflictResp.getStatus();
//                String err = resolveConflictResp.getEntity(String.class);
//
//                throw new RuntimeException(String.format("Cannot resolve conflict. HTTP status %d, Error message %s", status, err));
//            }
//        } else {
//            LOGGER.warning("Reharvest API not defined");
//        }
//    }
}
