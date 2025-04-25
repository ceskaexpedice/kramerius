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
package cz.incad.kramerius.services.workers.replicate.records;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import cz.incad.kramerius.rest.apiNew.admin.v70.reharvest.ReharvestItem;
import cz.incad.kramerius.utils.StringUtils;

import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;


/**
 * Represents an existing indexing conflict within a Solr composite structure.
 * <p>
 * A conflict occurs when the same PID is associated with more than one {@code compositeId},
 * which may indicate inconsistent or erroneous data in the index.
 * </p>
 *
 * <p>
 * This class is intended to encapsulate and describe such conflicts discovered
 * during the replication process. It can be used for reporting, validation,
 * or conflict resolution logic in later stages.
 * </p>
 */
public class ExistingConflictRecord  extends ReplicateRecord implements Conflict {

    public static final Logger LOGGER = Logger.getLogger(ExistingConflictRecord.class.getName());

    /**
     * A list of composite IDs that are associated with the given PID.
     * More than one entry in this list indicates a conflict.
     */
    private final List<String> compositeIds;

    /**
     * Constructs a new {@code ExistingConflictRecord}.
     *
     * @param pid          The PID (child element in composite structure).
     * @param compositeIds The list of composite IDs associated with this PID.
     */
    public ExistingConflictRecord(String pid, List<String> compositeIds) {
        super(pid);
        this.compositeIds = compositeIds;
    }


    /**
     * Returns the list of composite IDs linked to this PID.
     *
     * @return List of composite IDs.
     */
    public List<String> getCompositeIds() {
        return compositeIds;
    }

    /**
     * Determines whether the current record represents a true conflict,
     * i.e., if it has more than one unique composite ID.
     *
     * @return {@code true} if conflict exists, otherwise {@code false}.
     */
    public boolean isConflict() {
        return compositeIds != null && compositeIds.size() > 1;
    }

    @Override
    public String toString() {
        return "ExistingConflict{" +
                "pid='" + getPid() + '\'' +
                ", compositeIds=" + compositeIds +
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
