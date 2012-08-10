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
package cz.incad.kramerius.rest.api.processes;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.antlr.stringtemplate.StringTemplate;
import org.antlr.stringtemplate.StringTemplateGroup;
import org.antlr.stringtemplate.language.DefaultTemplateLexer;

import antlr.RecognitionException;
import antlr.TokenStreamException;

import com.google.inject.Inject;
import com.google.inject.Provider;

import cz.incad.kramerius.processes.DefinitionManager;
import cz.incad.kramerius.processes.LRPRocessFilter;
import cz.incad.kramerius.processes.LRProcess;
import cz.incad.kramerius.processes.LRProcessDefinition;
import cz.incad.kramerius.processes.LRProcessManager;
import cz.incad.kramerius.processes.LRProcessOffset;
import cz.incad.kramerius.processes.LRProcessOrdering;
import cz.incad.kramerius.processes.TypeOfOrdering;
import cz.incad.kramerius.processes.LRPRocessFilter.Tripple;
import cz.incad.kramerius.security.User;
import cz.incad.kramerius.security.utils.UserUtils;
import cz.incad.kramerius.users.LoggedUsersSingleton;
import cz.incad.kramerius.utils.params.ParamsLexer;
import cz.incad.kramerius.utils.params.ParamsParser;


@Path("/processes")
public class LRResource {

    private static java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(LRResource.class.getName());
    
    private static final String DEFAULT_SIZE = "50";

    
    
    @Inject
    LRProcessManager lrProcessManager;

    @Inject
    DefinitionManager definitionManager;
    
    @Inject
    Provider<HttpServletRequest> requestProvider;

    @Inject
    LoggedUsersSingleton loggedUsersSingleton;
    
    @GET
    @Path("plainStart/{def}")
    public String plainProcessStart(@PathParam("def")String def, @QueryParam("params") String params){
        definitionManager.load();
        LRProcessDefinition definition = definitionManager.getLongRunningProcessDefinition(def);
        if (!definition.isInputTemplateDefined()) {
            String loggedUserKey =  (String) this.requestProvider.get().getSession().getAttribute(UserUtils.LOGGED_USER_KEY_PARAM);

            User user = loggedUsersSingleton.getUser(loggedUserKey);
            
            
            LRProcess newProcess = definition.createNewProcess(null);
            newProcess.setLoggedUserKey(loggedUserKey);
            newProcess.setParameters(Arrays.asList(params));

            newProcess.setUser(user);
            
            newProcess.planMe(new Properties());
            lrProcessManager.updateTokenMapping(newProcess, loggedUserKey);
            
            return lrPRocessToJSONObject(newProcess).toString();
        } else {
            throw new LRResourceCannotStartProcess("not plain process "+def);
        }
    }

    @GET
    @Path("parametrizedStart/{def}")
    public String parametrizedProcessStart(@PathParam("def")String def,  @QueryParam("paramsMapping") String paramsMapping){
        return null;
    }
    
    @GET
    @Path("stop/{uuid}")
    public String processStop(@PathParam("uuid")String uuid){
        return null;
    }

    @GET
    @Path("delete/{uuid}")
    public String deleteProcess(@PathParam("uuid")String uuid){
        return null;
    }

    @GET
    @Path("logs/{uuid}")
    public String processLogs(@PathParam("uuid")String uuid){
        return null;
    }

    @GET
    @Path("desc/{uuid}")
    @Produces(MediaType.APPLICATION_JSON)
    public String getProcessDescription(@PathParam("uuid")String uuid){
        LRProcess lrProcesses = this.lrProcessManager.getLongRunningProcess(uuid);
        return lrPRocessToJSONObject(lrProcesses).toString();
    }    

    
    
    
    @GET
    @Path("list")
    @Produces(MediaType.APPLICATION_JSON)
    public String getProcessDescriptions(@QueryParam("filter")String filter,@QueryParam("ordering")String ordering, @QueryParam("typeofordering") String type,@QueryParam("offset") String of){
        try {
            JSONArray jsonArr = new JSONArray();
            List<LRProcess> lrProcesses = this.lrProcessManager.getLongRunningProcessesAsGrouped(lrProcessOrdering(ordering), typeOfOrdering(type), offset(of), lrPRocessFilter(filter));
            for (LRProcess lrProcess : lrProcesses) {
                JSONObject jsonLrProcess = lrPRocessToJSONObject(lrProcess);
                if (lrProcess.isMasterProcess()) {
                    JSONArray childrenJSONs = new JSONArray();
                    List<LRProcess> childSubprecesses = this.lrProcessManager.getLongRunningProcessesByToken(lrProcess.getToken());
                    for (LRProcess child : childSubprecesses) {
                        if (!child.getUUID().equals(lrProcess.getUUID())) {
                            childrenJSONs.add(lrPRocessToJSONObject(child));
                        }
                    }
                    jsonLrProcess.put("children",childrenJSONs);
                }   
                jsonArr.add(jsonLrProcess);
            }
            return jsonArr.toString();
        } catch (RecognitionException e) {
            LOGGER.log(Level.SEVERE,e.getMessage(),e);
            throw new LRResourceBadFilterException(e.getMessage());
        } catch (TokenStreamException e) {
            LOGGER.log(Level.SEVERE,e.getMessage(),e);
            throw new LRResourceBadFilterException(e.getMessage());
        }
    }

    private LRPRocessFilter lrPRocessFilter(String f) throws RecognitionException, TokenStreamException {
        if (f == null) return null;
        ParamsParser paramsParser = new ParamsParser(new ParamsLexer(new StringReader(f)));
        List params = paramsParser.params();
        List<Tripple> tripples = new ArrayList<LRPRocessFilter.Tripple>();
        for (Object object : params) {
            List trippleList = (List) object;
            Tripple tripple = createTripple(trippleList);
            if (tripple.getVal() != null) {
                tripples.add(tripple);
            }
        }
        LRPRocessFilter filter = LRPRocessFilter.createFilter(tripples);
        return filter;
    }
    
    private Tripple createTripple(List trpList) {
        if (trpList.size() == 3) {
            String name = (String) trpList.get(0);
            // mapping
            String op = (String) trpList.get(1);
            String val = (String) trpList.get(2);
            Tripple trp = new Tripple(name, val, op);
            return trp;
        } else
            return null;
    }

    private LRProcessOffset offset(String of) {
        if (of != null) {
            LRProcessOffset offset = new LRProcessOffset(of, DEFAULT_SIZE);
            return offset;
        } else return null;
    }
    
    private TypeOfOrdering typeOfOrdering(String type) {
        if (type != null) {
            return TypeOfOrdering.valueOf(type);
        } else return null;
    }
    private LRProcessOrdering lrProcessOrdering(String ordering) {
        if (ordering != null) {
            return LRProcessOrdering.valueOf(ordering);
        } else return null;
    }

    private JSONObject lrPRocessToJSONObject(LRProcess lrProcess) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
        
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("uuid", lrProcess.getUUID());
        jsonObject.put("pid", lrProcess.getPid());
        jsonObject.put("def", lrProcess.getDefinitionId());
        jsonObject.put("state", lrProcess.getProcessState().toString());
        jsonObject.put("batchState", lrProcess.getBatchState().toString());
        jsonObject.put("name", lrProcess.getProcessName());
        jsonObject.put("started", format.format(new Date(lrProcess.getStartTime())));
        jsonObject.put("planned", format.format(new Date(lrProcess.getPlannedTime())));
        jsonObject.put("finished", format.format(new Date(lrProcess.getFinishedTime())));
        jsonObject.put("userid", lrProcess.getLoginname());
        jsonObject.put("userFirstname", lrProcess.getFirstname());
        jsonObject.put("userSurname", lrProcess.getSurname());
        
        return jsonObject;
    }

    

    public static void main(String[] args) {
    }

}
