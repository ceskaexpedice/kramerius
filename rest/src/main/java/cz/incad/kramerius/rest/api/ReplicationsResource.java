/*
 * Copyright (C) 2012 Pavel Stastny
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
package cz.incad.kramerius.rest.api;

import java.io.UnsupportedEncodingException;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import biz.sourcecode.base64Coder.Base64Coder;

import com.google.inject.Inject;

import cz.incad.kramerius.service.ReplicateException;
import cz.incad.kramerius.service.ReplicationService;

/**
 * API endpoint for replications
 * @author pavels
 */
@Path("/replication/{pid}")
public class ReplicationsResource {

    @Inject
    ReplicationService replicationService;

    @GET
    @Path("prepare")
    @Produces(MediaType.APPLICATION_JSON)
    public String prepareExport(@PathParam("pid") String pid) throws ReplicateException {
        List<String> pidList = replicationService.prepareExport(pid);
        JSONArray jsonArray = new JSONArray();
        for (String npid : pidList) {
            jsonArray.add(npid);
        }
        JSONObject jsonObj = new JSONObject();
        jsonObj.put("pids", jsonArray);
        return jsonObj.toString();
    }

    @GET
    @Path("exportedFOXML")
    @Produces(MediaType.APPLICATION_JSON)
    public String getExportedFOXML(@PathParam("pid")String pid) throws ReplicateException, UnsupportedEncodingException {
        JSONObject jsonObj = new JSONObject();
        char[] encoded = Base64Coder.encode(replicationService.getExportedFOXML(pid));
        jsonObj.put("raw", new String(encoded));
        return jsonObj.toString();
    }
    
}
