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

import javax.ws.rs.core.MediaType;
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
    public void reharvestConflict(Client client, String reharvestApi) {
        if (StringUtils.isAnyString(reharvestApi)) {
            LinkedHashSet<String> compositeRootPids = new LinkedHashSet<>();

            this.compositeIds.stream().forEach(c-> {
                if (c.contains("!")) {
                    String[] splitted = c.split("!");
                    String croot = splitted[0];
                    compositeRootPids.add(croot);
                }
            });

            String pids = compositeRootPids.stream().collect(Collectors.joining(","));
            String resolveConflicts = String.format("%s/resolveconflicts/%s", reharvestApi,pids);
            WebResource resharvestResource = client.resource(resolveConflicts);
            ClientResponse resolveConflictResp = resharvestResource.accept(MediaType.APPLICATION_JSON)
                    .get(ClientResponse.class);
            if (resolveConflictResp.getStatus() == ClientResponse.Status.OK.getStatusCode()) {
                LOGGER.log(Level.INFO, String.format("Resolving conflict for %s has been planned", pids));
            } else {
                int status = resolveConflictResp.getStatus();
                String err = resolveConflictResp.getEntity(String.class);
                throw new RuntimeException(String.format("Cannot resolve conflict. HTTP status %d, Error message %s", status, err));
            }
        } else {
            LOGGER.warning("Reharvest API not defined");
        }
    }
}
