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

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.concurrent.locks.Lock;
import java.util.logging.Level;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriBuilderException;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import biz.sourcecode.base64Coder.Base64Coder;

import com.google.inject.Inject;
import com.google.inject.Provider;

import cz.incad.kramerius.ObjectPidsPath;
import cz.incad.kramerius.processes.BatchStates;
import cz.incad.kramerius.processes.DefinitionManager;
import cz.incad.kramerius.processes.LRPRocessFilter;
import cz.incad.kramerius.processes.LRPRocessFilter.Op;
import cz.incad.kramerius.processes.LRPRocessFilter.Tripple;
import cz.incad.kramerius.processes.LRProcess;
import cz.incad.kramerius.processes.LRProcessDefinition;
import cz.incad.kramerius.processes.LRProcessManager;
import cz.incad.kramerius.processes.LRProcessOffset;
import cz.incad.kramerius.processes.LRProcessOrdering;
import cz.incad.kramerius.processes.States;
import cz.incad.kramerius.processes.TypeOfOrdering;
import cz.incad.kramerius.rest.api.exceptions.ActionNotAllowed;
import cz.incad.kramerius.rest.api.processes.exceptions.CannotReadLogs;
import cz.incad.kramerius.rest.api.processes.exceptions.CannotStartProcess;
import cz.incad.kramerius.rest.api.processes.exceptions.CannotStopProcess;
import cz.incad.kramerius.rest.api.processes.exceptions.LogsNotFound;
import cz.incad.kramerius.rest.api.processes.exceptions.NoDefinitionFound;
import cz.incad.kramerius.rest.api.processes.exceptions.NoProcessFound;
import cz.incad.kramerius.rest.api.processes.filter.FilterCondition;
import cz.incad.kramerius.rest.api.processes.filter.Operand;
import cz.incad.kramerius.rest.api.replication.exceptions.ObjectNotFound;
import cz.incad.kramerius.rest.api.utils.ExceptionJSONObjectUtils;
import cz.incad.kramerius.security.IsActionAllowed;
import cz.incad.kramerius.security.SecuredActions;
import cz.incad.kramerius.security.SpecialObjects;
import cz.incad.kramerius.security.User;
import cz.incad.kramerius.security.utils.UserUtils;
import cz.incad.kramerius.users.LoggedUsersSingleton;
import cz.incad.kramerius.utils.IOUtils;


/**
 * Processes API endpoint
 * @author pavels
 */
@Path("/processes")
public class LRResource {

    
    public static SimpleDateFormat FORMAT = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss:SSS");

    
    private static final String AUTH_TOKEN_HEADER_KEY = "auth-token";
    private static final String TOKEN_ATTRIBUTE_KEY = "token";

    
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
    
    @Inject
    Provider<User> userProvider;
    
    @Inject
    IsActionAllowed actionAllowed;
    
    @Inject
    Application application;
    

    /**
     * Start new process 
     * @param def Process definition
     * @param params Parameters for simple process
     * @param paramsMapping Parameteres mapping for parametrized processes
     * @return
     */
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response start(@QueryParam("def")String def, JSONObject startingOptions) {
        if (startingOptions.containsKey("parameters")) {
            return plainProcessStart(def,startingOptions.getJSONArray("parameters"));
        } else if (startingOptions.containsKey("mapping")) {
            return parametrizedProcessStart(def,startingOptions.getJSONObject("mapping"));
        } else {
            return plainProcessStart(def, new JSONArray());
        }
    }
    
    
    /**
     * Start process without params
     * @param def Process definition
     * @return Started process description
     */
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public Response startWitoutParams(@QueryParam("def")String def) {
        return plainProcessStart(def,new JSONArray());
    }    
    
    Response plainProcessStart(String def, JSONArray array){
        if (this.actionAllowed.isActionAllowed(this.userProvider.get(),SecuredActions.MANAGE_LR_PROCESS.getFormalName(), SpecialObjects.REPOSITORY.getPid(),null,new ObjectPidsPath(SpecialObjects.REPOSITORY.getPid()))) {
            try {
                
                definitionManager.load();
                LRProcessDefinition definition = definitionManager.getLongRunningProcessDefinition(def);
                if (definition != null) {
                    if (!definition.isInputTemplateDefined()) {
                        User user = userProvider.get();
                        String loggedUserKey =  (String) this.requestProvider.get().getSession().getAttribute(UserUtils.LOGGED_USER_KEY_PARAM);
                        
                        HttpServletRequest request = this.requestProvider.get();
                        
                        LRProcess newProcess = definition.createNewProcess(request.getHeader(AUTH_TOKEN_HEADER_KEY), request.getParameter(TOKEN_ATTRIBUTE_KEY));
                        newProcess.setLoggedUserKey(loggedUserKey);

                        List<String> params = new ArrayList<String>();
                        for (Object par : array.toArray()) {
                            params.add(par.toString());
                        }
                        newProcess.setParameters(params);
                        newProcess.setUser(user);
                        newProcess.planMe(new Properties());
                        lrProcessManager.updateAuthTokenMapping(newProcess, loggedUserKey);
                        URI uri = UriBuilder.fromResource(LRResource.class).path("{uuid}").build(newProcess.getUUID());
                        return Response.created(uri).entity(lrPRocessToJSONObject(newProcess).toString()).build();
                    } else {
                        throw new CannotStartProcess("not plain process "+def);
                    }
                } else throw new NoDefinitionFound("cannot find definition '"+def+"'");
            } catch (IllegalArgumentException e) {
                throw new CannotStartProcess(e.getMessage(),e);
            } catch (UriBuilderException e) {
                throw new CannotStartProcess("not plain process "+def);
            } catch (SecurityException e) {
                throw new ActionNotAllowed("action is not allowed");
            }
        } else {
            throw new ActionNotAllowed("action is not allowed");
        }
    }

    Response parametrizedProcessStart(@PathParam("def")String def,  JSONObject mapping){
        if (this.actionAllowed.isActionAllowed(this.userProvider.get(),SecuredActions.MANAGE_LR_PROCESS.getFormalName(), SpecialObjects.REPOSITORY.getPid(),null,new ObjectPidsPath(SpecialObjects.REPOSITORY.getPid()))) {
            definitionManager.load();
            LRProcessDefinition definition = definitionManager.getLongRunningProcessDefinition(def);

            User user = userProvider.get();
            String loggedUserKey =  (String) this.requestProvider.get().getSession().getAttribute(UserUtils.LOGGED_USER_KEY_PARAM);
            
            HttpServletRequest request = this.requestProvider.get();
            
            LRProcess newProcess = definition.createNewProcess(request.getHeader(AUTH_TOKEN_HEADER_KEY), request.getParameter(TOKEN_ATTRIBUTE_KEY));
            newProcess.setLoggedUserKey(loggedUserKey);

            Properties props = new Properties();
            Set keySet = mapping.keySet();
            for (Object key : keySet) {
                props.put(key.toString(),mapping.get(key).toString());
            }
            
            newProcess.setParameters(Arrays.asList(new String[0]));
            newProcess.setUser(user);

            newProcess.planMe(props);
            lrProcessManager.updateAuthTokenMapping(newProcess, loggedUserKey);
            URI uri = UriBuilder.fromResource(LRResource.class).path("{uuid}").build(newProcess.getUUID());
            return Response.created(uri).entity(lrPRocessToJSONObject(newProcess).toString()).build();
        } else {
            throw new ActionNotAllowed("action is not allowed");
        }
    }
    
    /**
     * Stop current running process
     * @param uuid UUID of process
     * @param stop Stop parameter
     * @return
     */
    @PUT
    @Path("{uuid}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response stop(@PathParam("uuid")String uuid, @QueryParam("stop") String stop){
        if (this.actionAllowed.isActionAllowed(this.userProvider.get(),SecuredActions.MANAGE_LR_PROCESS.getFormalName(), SpecialObjects.REPOSITORY.getPid(),null,new ObjectPidsPath(SpecialObjects.REPOSITORY.getPid()))) {
            if (stop != null) {
                this.definitionManager.load();
                LRProcess lrProcess = lrProcessManager.getLongRunningProcess(uuid);
                if (lrProcess == null) throw new NoProcessFound("cannot find process "+uuid);
                if (!States.isFinishState(lrProcess.getProcessState())) {
                    lrProcess.stopMe();
                    lrProcessManager.updateLongRunningProcessFinishedDate(lrProcess);
                    LRProcess nLrProcess = lrProcessManager.getLongRunningProcess(uuid);
                    if (nLrProcess != null) return  Response.ok().entity(lrPRocessToJSONObject(nLrProcess)).build();
                    else throw new NoProcessFound("cannot find process "+uuid);
                } else {
                    throw new CannotStopProcess("cannot stop process "+uuid);
                }
            } else throw new NoProcessFound("cannot find process"+uuid);
        } else {
            throw new ActionNotAllowed("action is not allowed");
        }
    }

    /**
     * Delete process
     * @param uuid Process identification
     * @return Deleted object structure
     */
    @DELETE
    @Path("{uuid}")
    @Produces(MediaType.APPLICATION_JSON)
    public String delete(@PathParam("uuid")String uuid){
        if (this.actionAllowed.isActionAllowed(this.userProvider.get(),SecuredActions.MANAGE_LR_PROCESS.getFormalName(), SpecialObjects.REPOSITORY.getPid(),null,new ObjectPidsPath(SpecialObjects.REPOSITORY.getPid()))) {
            Lock lock = this.lrProcessManager.getSynchronizingLock();
            lock.lock();
            try {
                LRProcess longRunningProcess = this.lrProcessManager.getLongRunningProcess(uuid);
                if (longRunningProcess != null) {
                    if (BatchStates.expect(longRunningProcess.getBatchState(), BatchStates.BATCH_FAILED, BatchStates.BATCH_FINISHED)) {
                        lrProcessManager.deleteBatchLongRunningProcess(longRunningProcess);
                    } else {
                        lrProcessManager.deleteLongRunningProcess(longRunningProcess);
                    }
                } else {
                    throw new NoProcessFound("cannot find process "+uuid);
                }
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("deleted", uuid);
                return jsonObject.toString();
            } finally {
                lock.unlock();
            }
        } else {
            throw new ActionNotAllowed("action is not allowed");
        }
    }

    /**
     * Returns processes logs
     * @param uuid Process identification
     * @return JSON object contains logs encoded in base64
     */
    @GET
    @Path("{uuid}/logs")
    @Produces(MediaType.APPLICATION_JSON)
    public Response logs(@PathParam("uuid")String uuid){
        if (this.actionAllowed.isActionAllowed(this.userProvider.get(),SecuredActions.MANAGE_LR_PROCESS.getFormalName(), SpecialObjects.REPOSITORY.getPid(),null,new ObjectPidsPath(SpecialObjects.REPOSITORY.getPid()))) {
            try {
                JSONObject jsonObj = new JSONObject();
                LRProcess lrProcesses = this.lrProcessManager.getLongRunningProcess(uuid);
                if (lrProcesses != null) {
                    InputStream os = lrProcesses.getErrorProcessOutputStream();
                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    IOUtils.copyStreams(os, bos);
                    jsonObj.put("sout", new String(Base64Coder.encode(bos.toByteArray())));
                    InputStream er = lrProcesses.getErrorProcessOutputStream();
                    ByteArrayOutputStream ber = new ByteArrayOutputStream();
                    IOUtils.copyStreams(er, ber);
                    jsonObj.put("serr", new String(Base64Coder.encode(ber.toByteArray())));
                    return Response.ok().entity(jsonObj).build();
                } else  throw new NoProcessFound("process not found "+uuid);
            } catch (FileNotFoundException e) {
                LOGGER.log(Level.SEVERE,e.getMessage(),e);
                throw new LogsNotFound(e.getMessage(),e);
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE,e.getMessage(),e);
                throw new CannotReadLogs(e.getMessage());
            }
        } else {
            throw new ActionNotAllowed("action is not allowed");
        }
    }


    /**
     * Returns process description
     * @param uuid Process identification
     * @return JSON process description
     */
    @GET
    @Path("{uuid}")
    @Produces({MediaType.APPLICATION_JSON+ ";charset=utf-8"})
    public Response getProcessDescription(@PathParam("uuid")String uuid){
        if (this.actionAllowed.isActionAllowed(this.userProvider.get(),SecuredActions.MANAGE_LR_PROCESS.getFormalName(), SpecialObjects.REPOSITORY.getPid(),null,new ObjectPidsPath(SpecialObjects.REPOSITORY.getPid()))) {
            LRProcess lrProc = this.lrProcessManager.getLongRunningProcess(uuid);
            if (lrProc != null) {
                JSONObject jsonObject = lrPRocessToJSONObject(lrProc);
                if (lrProc.isMasterProcess()) {
                    JSONArray array = new JSONArray();
                    List<LRProcess> childSubprecesses = this.lrProcessManager.getLongRunningProcessesByGroupToken(lrProc.getGroupToken());
                    for (LRProcess child : childSubprecesses) {
                        array.add(lrPRocessToJSONObject(child));
                    }
                    jsonObject.put("children", array);
                }
                return Response.ok().entity(jsonObject).build();
            } else throw new NoProcessFound("cannot find process '"+uuid+"'");
        } else {
            throw new ActionNotAllowed("action is not allowed");
        }
    }
    
    /**
     * Returns filtered processes 
     * @param filterUUID Filter uuid field
     * @param filterPid Filter pid field
     * @param filterDef Filter def field
     * @param filterBatchState Filter batchstate field
     * @param filterName Filter name field
     * @param filterUserId filter userid field
     * @param filterUserFirstname filter userFirstname field
     * @param filterUserSurname filter userSurname field
     * @param of Ofset field
     * @return return filtered json array
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON+ ";charset=utf-8")
    public Response getProcessDescriptions(

            @QueryParam("uuid") String filterUUID,
            @QueryParam("pid")  String filterPid,
            @QueryParam("def")  String filterDef,
            @QueryParam("batchState")  String filterBatchState,
            @QueryParam("name")  String filterName,
            @QueryParam("userid")  String filterUserId,
            @QueryParam("userFirstname")  String filterUserFirstname,
            @QueryParam("userSurname")  String filterUserSurname,

            @QueryParam("offset") String of) {
        if (this.actionAllowed.isActionAllowed(this.userProvider.get(),SecuredActions.MANAGE_LR_PROCESS.getFormalName(), SpecialObjects.REPOSITORY.getPid(),null,new ObjectPidsPath(SpecialObjects.REPOSITORY.getPid()))) {

                Map<String, String> filterMap = new HashMap<String, String>(); {
                    filterMap.put("uuid", filterUUID);
                    filterMap.put("pid", filterPid);
                    filterMap.put("def", filterDef);
                    filterMap.put("batchState", filterBatchState);
                    filterMap.put("name", filterName);
                    filterMap.put("userid", filterUserId);
                    filterMap.put("userFirstname", filterUserFirstname);
                    filterMap.put("userSurname", filterUserSurname);
                };
                LRPRocessFilter filter = lrPRocessFilter(filterMap);
                List<LRProcess> lrProcesses = this.lrProcessManager.getLongRunningProcessesAsGrouped(lrProcessOrdering(null), typeOfOrdering(null), offset(of), filter);
                JSONArray retList = new JSONArray();
                for (LRProcess lrProcess : lrProcesses) {
                    JSONObject ent = lrPRocessToJSONObject(lrProcess);
                    if (lrProcess.isMasterProcess()) {
                        JSONArray array = new JSONArray();
                        List<LRProcess> childSubprecesses = this.lrProcessManager.getLongRunningProcessesByGroupToken(lrProcess.getGroupToken());
                        for (LRProcess child : childSubprecesses) {
                            array.add(lrPRocessToJSONObject(child));
                        }
                        ent.put("children", array);
                    }
                    retList.add(ent);
                }
                return Response.ok().entity(retList).build();
        } else {
            throw new ActionNotAllowed("action is not allowed");
        }
    }

    LRPRocessFilter lrPRocessFilter(Map<String, String>filterMap) {
        List<Tripple> tripples = new ArrayList<LRPRocessFilter.Tripple>();
        for (String key : filterMap.keySet()) {
            String val = filterMap.get(key);
            if (val != null ) {
                Tripple tripple = createTripple(key+"="+val);
                tripples.add(tripple);
            }
        }
        return  LRPRocessFilter.createFilter(tripples);
    }

    private Tripple createTripple(String trpl) {
        StringTokenizer tokenizer = new StringTokenizer(trpl, Op.EQ.getRawString()+Op.GT.getRawString()+Op.LT.getRawString(), true);
        if(tokenizer.hasMoreTokens()) {

            Operand left = Operand.createOperand(tokenizer.nextToken());
            Op op = tokenizer.hasMoreTokens() ? Op.findByString(tokenizer.nextToken()): null;
            Operand right = tokenizer.hasMoreTokens() ? Operand.createOperand(tokenizer.nextToken()) : null;

            if (left != null && op != null && right != null) {
                FilterCondition cond = new FilterCondition();
                cond.setOp(op);
                cond.setLeftOperand(left);
                cond.setRightOperand(right);
                return cond.getFilterValue();
            }
            
        } 
        return null;
    }

    private LRProcessOffset offset(String of) {
        if (of != null) {
            LRProcessOffset offset = new LRProcessOffset(of, DEFAULT_SIZE);
            return offset;
        } else return new LRProcessOffset("0", DEFAULT_SIZE);
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
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("uuid", lrProcess.getUUID());
        jsonObject.put("pid", lrProcess.getPid());
        jsonObject.put("def", lrProcess.getDefinitionId());
        jsonObject.put("state", lrProcess.getProcessState().toString());
        jsonObject.put("batchState", lrProcess.getBatchState().toString());
        jsonObject.put("name", lrProcess.getProcessName());
        if (lrProcess.getStartTime() > 0) {
            jsonObject.put("started", FORMAT.format(new Date(lrProcess.getStartTime())));
        }
        if (lrProcess.getPlannedTime() > 0) {
            jsonObject.put("planned", FORMAT.format(new Date(lrProcess.getPlannedTime())));
        }
        if (lrProcess.getFinishedTime() > 0) {
            jsonObject.put("finished", FORMAT.format(new Date(lrProcess.getFinishedTime())));
        }
        jsonObject.put("userid", lrProcess.getLoginname());
        jsonObject.put("userFirstname", lrProcess.getFirstname());
        jsonObject.put("userSurname", lrProcess.getSurname());
        return jsonObject;
    }
}
