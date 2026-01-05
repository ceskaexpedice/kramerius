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

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import cz.incad.kramerius.utils.StringUtils;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.core5.http.io.entity.EntityUtils;

import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;


public class CDKExistingConflictWorkerItem  implements Conflict {

    public static final Logger LOGGER = Logger.getLogger(CDKExistingConflictWorkerItem.class.getName());

    private final String pid;
    private final List<String> compositeIds;


    public CDKExistingConflictWorkerItem(String pid, List<String> compositeIds) {
        this.pid = pid;
        this.compositeIds = compositeIds;
    }


    public String getPid() {
        return pid;
    }

    public List<String> getCompositeIds() {
        return compositeIds;
    }

    public boolean isConflict() {
        return compositeIds != null && compositeIds.size() > 1;
    }

    @Override
    public String toString() {
        return "CDKExistingConflictWorkerItem{" +
                "compositeIds=" + compositeIds +
                '}';
    }

    @Override
    public void reharvestConflict(CloseableHttpClient client, String reharvestApi) {
        if (StringUtils.isAnyString(reharvestApi)) {
            LinkedHashSet<String> compositeRootPids = new LinkedHashSet<>();

            // Extrakce root PIDů z kompozitních ID (formát root!child)
            if (this.compositeIds != null) {
                this.compositeIds.forEach(c -> {
                    if (c.contains("!")) {
                        String[] splitted = c.split("!");
                        compositeRootPids.add(splitted[0]);
                    }
                });
            }

            if (compositeRootPids.isEmpty()) {
                LOGGER.fine("No composite root PIDs found to resolve.");
                return;
            }

            String pids = String.join(",", compositeRootPids);
            String resolveConflictsUrl = String.format("%s/resolveconflicts/%s", reharvestApi, pids);

            HttpGet request = new HttpGet(resolveConflictsUrl);
            request.setHeader("Accept", "application/json");


            try {
                client.execute(request, response -> {
                    int status = response.getCode();
                    if (status == 200) { // OK
                        LOGGER.log(Level.INFO, String.format("Resolving conflict for %s has been planned", pids));
                    } else {
                        String err = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
                        throw new RuntimeException(String.format("Cannot resolve conflict. HTTP status %d, Error message %s", status, err));
                    }
                    return null;
                });
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "Network error during reharvest conflict resolution for PIDs: " + pids, e);
                throw new RuntimeException("Failed to contact reharvest API", e);
            }
        } else {
            LOGGER.warning("Reharvest API not defined");
        }
    }
}
