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
import cz.incad.kramerius.utils.StringUtils;

import javax.ws.rs.core.MediaType;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Represents a newly detected conflict that arises during the indexing process.
 * <p>
 * A new conflict typically occurs when a PID (primary identifier) being indexed
 * is already used by a different model in the target system — i.e., the PID is already known,
 * but the related root object(s) (e.g., intellectual entities) differ.
 * </p>
 * This class extends {@link ReplicateRecord}, carrying the conflicting PID,
 * and adds information about the root PIDs of the models involved in the conflict.
 * These root PIDs help identify which models (e.g., monographs, periodicals) are in conflict.
 */
public class NewConflictRecord extends ReplicateRecord implements Conflict {

    public static final  Logger LOGGER = Logger.getLogger(NewConflictRecord.class.getName());

    /** List of root PIDs (e.g., top-level objects) related to the PID in conflict. */
    private List<String> rootPids;

    /**
     * Constructs a new conflict record
     *
     * @param pid The PID that is in conflict.
     * @param rootPids The list of root PIDs (top-level models) associated with this conflict.
     */
    public NewConflictRecord(String pid, List<String> rootPids) {
        super(pid);
        this.rootPids = rootPids;
    }

    /**
     * Returns the list of root PIDs involved in this conflict.
     * These typically identify the conflicting models sharing the same PID.
     *
     * @return List of root PIDs.
     */
    public List<String> getRootPids() {
        return rootPids;
    }

    // plan reharvest
    @Override
    public void reharvestConflict(Client client, String reharvestApi) {
        if (StringUtils.isAnyString(reharvestApi)) {
            String pids = this.rootPids.stream().collect(Collectors.joining(","));

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
