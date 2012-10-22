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
package cz.incad.kramerius.rest.api.client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.ws.rs.core.MediaType;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientRequest;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.filter.ClientFilter;
import com.sun.jersey.core.util.Base64;

/**
 * Simple testing utility
 * @author pavels
 */
public class ProcessesClient {

    private static final String DEFAULT_NAME = "krameriusAdmin";
    private static final String DEFAULT_PSWD = "kram";

    static java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(ProcessesClient.class.getName());

    /**
     * Returns logs
     * 
     * @param uuid Process indentifier
     * @return Logs
     */
    public static String logs(String uuid) {
        try {
            Client c = Client.create();
            WebResource r = c.resource("http://localhost:8080/search/api/v4.6/processes/" + uuid + "/logs");
            r.addFilter(new BasicAuthenticationFilter(DEFAULT_NAME, DEFAULT_PSWD));
            String t = r.accept(MediaType.APPLICATION_JSON).get(String.class);
            return t;
        } catch (UniformInterfaceException e) {
            int status = e.getResponse().getStatus();
            if (status == 404) {
                LOGGER.severe("Process not found ");
            }
            throw new IllegalStateException(e);
        }
    }

    /**
     * Plan new process, wait 20 sec and kill it
     * 
     * @throws InterruptedException
     */
    public static void planAndStop() throws InterruptedException {
        String planned = ProcessesClient.planWithoutParams();
        JSONObject obj = JSONObject.fromObject(planned);
        System.out.println(obj);
        Thread.sleep(20000);
        String stopped = ProcessesClient.stop(obj.getString("uuid"));
        System.out.println(stopped);
    }

    /**
     * Plan new process, wait 20 sec, kill it and delete it
     * @throws InterruptedException
     */
    public static void planStopAndDelete() throws InterruptedException {
        String planned = ProcessesClient.planWithoutParams();
        JSONObject obj = JSONObject.fromObject(planned);
        System.out.println(obj);
        Thread.sleep(20000);
        String stopped = ProcessesClient.stop(obj.getString("uuid"));
        System.out.println(stopped);
        String deleted = delete(obj.getString("uuid"));
        System.out.println(deleted);
    }
    
    /**
     * Stop the process
     * 
     * @param uuid Process indentification
     */
    public static String stop(String uuid) {
        Client c = Client.create();
        WebResource r = c.resource("http://localhost:8080/search/api/v4.6/processes/" + uuid + "?stop=true");
        r.addFilter(new BasicAuthenticationFilter(DEFAULT_NAME, DEFAULT_PSWD));
        String t = r.accept(MediaType.APPLICATION_JSON).put(String.class);
        return t;
    }

    /**
     * Plan new process without parameters
     * @return
     */
    public static String planWithoutParams() {
        Client c = Client.create();
        WebResource r = c.resource("http://localhost:8080/search/api/v4.6/processes?def=mock");
        r.addFilter(new BasicAuthenticationFilter(DEFAULT_NAME, DEFAULT_PSWD));
        String t = r.accept(MediaType.APPLICATION_JSON).post(String.class);
        return t;
    }

    /**
     * Plan proc with params
     * @return
     */
    public static String planProcWithParams() {
        Client c = Client.create();
        WebResource r = c.resource("http://localhost:8080/search/api/v4.6/processes?def=mock");
        r.addFilter(new BasicAuthenticationFilter(DEFAULT_NAME, DEFAULT_PSWD));
        JSONObject object = new JSONObject();
        object.put("parameters", JSONArray.fromObject(Arrays.asList("first", "second", "third")));
        String t = r.accept(MediaType.APPLICATION_JSON).type(MediaType.APPLICATION_JSON).entity(object.toString(), MediaType.APPLICATION_JSON).post(String.class);
        return t;
    }

    /**
     * Plan new proc with named params
     * @return
     */
    public static String planProcWithNamedParams() {
        Client c = Client.create();
        WebResource r = c.resource("http://localhost:8080/search/api/v4.6/processes?def=wmock");
        r.addFilter(new BasicAuthenticationFilter(DEFAULT_NAME, DEFAULT_PSWD));
        JSONObject object = new JSONObject();
        JSONObject mapping = new JSONObject();
        mapping.put("inputFolder", JSONArray.fromObject(new String[] { "/home/pavels/]" }));
        mapping.put("processName", JSONArray.fromObject(new String[] { "Muj nazev procesu" }));
        object.put("mapping", mapping);
        String t = r.accept(MediaType.APPLICATION_JSON).type(MediaType.APPLICATION_JSON).entity(object.toString(), MediaType.APPLICATION_JSON).post(String.class);
        return t;
    }

    /**
     * Process description
     * 
     * @param uuid
     * @return
     */
    public static String desc(String uuid) {
        Client c = Client.create();
        WebResource r = c.resource("http://localhost:8080/search/api/v4.6/processes/" + uuid);
        r.addFilter(new BasicAuthenticationFilter(DEFAULT_NAME, DEFAULT_PSWD));
        String t = r.accept(MediaType.APPLICATION_JSON).get(String.class);
        return t;
    }

    /**
     * Delete process
     * 
     * @param uuid
     */
    public static String delete(String uuid) {
        Client c = Client.create();
        WebResource r = c.resource("http://localhost:8080/search/api/v4.6/processes/" + uuid);
        r.addFilter(new BasicAuthenticationFilter(DEFAULT_NAME, DEFAULT_PSWD));
        String t = r.accept(MediaType.APPLICATION_JSON).delete(String.class);
        return t;
    }

}
