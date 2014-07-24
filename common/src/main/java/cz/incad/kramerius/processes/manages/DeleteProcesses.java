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
/**
 * 
 */
package cz.incad.kramerius.processes.manages;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.ws.rs.core.MediaType;

import net.sf.json.JSON;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import cz.incad.kramerius.processes.States;
import cz.incad.kramerius.processes.annotations.ParameterName;
import cz.incad.kramerius.processes.annotations.Process;
import cz.incad.kramerius.processes.impl.ProcessStarter;
import cz.incad.kramerius.utils.StringUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientRequest;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.filter.ClientFilter;
import com.sun.jersey.core.util.Base64;


/**
 * @author pavels
 *
 */
public class DeleteProcesses {

    static java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(DeleteProcesses.class.getName());
    
    static final SimpleDateFormat JSON_RESULT_FORMAT = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss:SSS");
    static final SimpleDateFormat INPUT_DIALOG_FORMAT = new SimpleDateFormat("dd/MM/yyyy");
    

    public static List<JSONObject> list(String baseURL , Client client, BasicAuthenticationFilter authFilter, Date from, Date to, String state, String batchState) throws ParseException {
        List<JSONObject> l = new ArrayList<JSONObject>();
        
        //String baseURL = getHostURL()+"?offset=";

        boolean process = true;
        int offset = 0;
        while(process) {
            String url = baseURL + offset;
            LOGGER.info(" URL :"+url);
            WebResource r = client.resource(url);
            if (authFilter!=null) r.addFilter(authFilter);
            String t = r.header("auth-token", authToken()).header("token", groupToken()).accept(MediaType.APPLICATION_JSON).get(String.class);

            JSONArray jsonArray = JSONArray.fromObject(t);
            process = jsonArray.size() > 0;
            for (Object object : jsonArray) {
                JSONObject jsonObj = (JSONObject) object;
                boolean add = filterState(state, jsonObj);
                if (add) add = filterBatchState(batchState, jsonObj);
                if (add) add = filterDateFrom(from, jsonObj);
                if (add) add = filterDateTo(to, jsonObj);
                if (add) {
                  l.add(jsonObj);
                }
            }
            offset = offset + jsonArray.size();
        }
        return l;
    }



    /**
     * @return
     */
    private static String getHostURL() {
        String appURL = KConfiguration.getInstance().getApplicationURL();
        return appURL + (appURL.endsWith("/") ? "" : "/")+"api/v4.6/processes";
    }



    /**
     * @param from
     * @param jsonObj
     * @return
     * @throws ParseException 
     */
    private static boolean filterDateFrom(Date from, JSONObject jsonObj) throws ParseException {
        if (from != null) {
            //10/25/2012 15:46:38:565
            if (jsonObj.containsKey("started")) {
                String dateFrom = jsonObj.getString("started");
                Date parsed = JSON_RESULT_FORMAT.parse(dateFrom);
                return parsed.after(from);
            } else {
                return true;
            }
        } else {
            return true;
        }
    }

    /**
     * @param from
     * @param jsonObj
     * @return
     * @throws ParseException 
     */
    private static boolean filterDateTo(Date to, JSONObject jsonObj) throws ParseException {
        if (to != null) {
            //10/25/2012 15:46:38:565
            if (jsonObj.containsKey("started")) {
                String dateFrom = jsonObj.getString("started");
                Date parsed = JSON_RESULT_FORMAT.parse(dateFrom);
                return parsed.before(to);
            } else {
                return true;
            }
        } else {
            return true;
        }
    }

    public static boolean filterState(String state, JSONObject jsonObj) {
        if (state != null) {
            String jsonState =  jsonObj.getString("state");
            if (state.equals(jsonState)) {
                return true;
            } else return false;
        } else {
            return true;
        }
    }
    
    public static boolean filterBatchState(String batchState, JSONObject jsonObj) {
        if (batchState != null) {
            String jsonState =  jsonObj.getString("batchState");
            if (batchState.equals(jsonState)) {
                return true;
            } else return false;
        } else {
            return true;
        }
    }

    @Process
    public static void  deleteProcesses(@ParameterName("from")String from, @ParameterName("to")String to, @ParameterName("state")String state, @ParameterName("batchState")String batchState) throws ParseException {
        LOGGER.info("from :"+from);
        LOGGER.info("to :"+to);
        LOGGER.info("state :"+state);
        LOGGER.info("batchState :"+batchState);
        Client c = Client.create();
        String baseURL = getHostURL()+"?offset=";
        List<JSONObject> processesToDelete = list(baseURL, c,null, from != null ? INPUT_DIALOG_FORMAT.parse(from) : null, to != null ? INPUT_DIALOG_FORMAT.parse(to) : null, state, batchState);
        LOGGER.info("found process  :"+processesToDelete.size());
        for (JSONObject jsonObject : processesToDelete) {
            if (jsonObject.containsKey("uuid")) {
                String jsonState = jsonObject.getString("state");
                if (!States.valueOf(jsonState).equals(States.RUNNING) && (!States.valueOf(jsonState).equals(States.PLANNED))) {
                    WebResource r = c.resource(getHostURL()+"/" + jsonObject.getString("uuid"));
                    LOGGER.info("deleting process '"+jsonObject.getString("uuid")+"'");
                    String deleted = r.header("auth-token", authToken()).header("token", groupToken()).delete(String.class);
                    LOGGER.info("\t deleted "+deleted);
                } else {
                    LOGGER.info("\t skipped process  '"+jsonObject.getString("uuid")+"'");
                }
            }
        }
    }

    public static void main(String[] args) throws ParseException {
        //09/07/2014
        BasicAuthenticationFilter bf = new BasicAuthenticationFilter("krameriusAdmin", "krameriusAdmin");
        String from = null;
        String to = null;
        String state = "FINISHED";
        String batchState = null;
        Client c = Client.create();
        List<JSONObject> processesToDelete = list("http://vmkramerius:8080/search/api/v4.6/processes?offset=",c,bf, from != null ? INPUT_DIALOG_FORMAT.parse(from) : null, to != null ? INPUT_DIALOG_FORMAT.parse(to) : null, state, batchState);
        int ll = processesToDelete.size();
        for (JSONObject jsonObject : processesToDelete) {
            String uuid = jsonObject.getString("uuid");
            System.out.println(uuid);
        }
    }
    
    static String authToken() {
        return System.getProperty(ProcessStarter.AUTH_TOKEN_KEY);
    }
    
    static String groupToken() {
        String grpToken = System.getProperty(ProcessStarter.TOKEN_KEY);
        return grpToken;
    }
}


class BasicAuthenticationFilter extends ClientFilter {
    
    public BasicAuthenticationFilter(final String username, final String password) {
        this.username = username;
        this.password = password;
    }

    public ClientResponse handle(ClientRequest clientRequest) throws ClientHandlerException {

        // encode the password
        byte[] encoded = Base64.encode((username + ":" + password).getBytes());

        // add the header
        List<Object> headerValue = new ArrayList<Object>();
        headerValue.add("Basic " + new String(encoded));
        clientRequest.getMetadata().put("Authorization", headerValue);

        return getNext().handle(clientRequest);
    }

    private String username;
    private String password;

}
