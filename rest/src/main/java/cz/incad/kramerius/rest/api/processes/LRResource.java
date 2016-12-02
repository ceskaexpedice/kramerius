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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.locks.Lock;
import java.util.logging.Level;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import biz.sourcecode.base64Coder.Base64Coder;

import com.google.inject.Inject;
import com.google.inject.Provider;

import cz.incad.kramerius.ObjectPidsPath;
import cz.incad.kramerius.processes.BatchStates;
import cz.incad.kramerius.processes.DefinitionManager;
import cz.incad.kramerius.processes.LRProcess;
import cz.incad.kramerius.processes.LRProcessDefinition;
import cz.incad.kramerius.processes.LRProcessManager;
import cz.incad.kramerius.processes.LRProcessOrdering;
import cz.incad.kramerius.processes.States;
import cz.incad.kramerius.rest.api.exceptions.ActionNotAllowed;
import cz.incad.kramerius.rest.api.exceptions.GenericApplicationException;
import cz.incad.kramerius.rest.api.processes.exceptions.CannotReadLogs;
import cz.incad.kramerius.rest.api.processes.exceptions.CannotStartProcess;
import cz.incad.kramerius.rest.api.processes.exceptions.CannotStopProcess;
import cz.incad.kramerius.rest.api.processes.exceptions.LogsNotFound;
import cz.incad.kramerius.rest.api.processes.exceptions.NoDefinitionFound;
import cz.incad.kramerius.rest.api.processes.exceptions.NoProcessFound;
import cz.incad.kramerius.rest.api.utils.dbfilter.DbFilterUtils;
import cz.incad.kramerius.security.IsActionAllowed;
import cz.incad.kramerius.security.SecuredActions;
import cz.incad.kramerius.security.SecurityException;
import cz.incad.kramerius.security.SpecialObjects;
import cz.incad.kramerius.security.User;
import cz.incad.kramerius.security.database.TypeOfOrdering;
import cz.incad.kramerius.security.utils.UserUtils;
import cz.incad.kramerius.users.LoggedUsersSingleton;
import cz.incad.kramerius.utils.IOUtils;
import cz.incad.kramerius.utils.IPAddressUtils;
import cz.incad.kramerius.utils.StringUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;
import cz.incad.kramerius.utils.database.Offset;
import cz.incad.kramerius.utils.database.SQLFilter;
import cz.incad.kramerius.utils.database.SQLFilter.TypesMapping;


/**
 * Processes API endpoint
 * @author pavels
 */
@Path("/v4.6/processes")
public class LRResource {
	
    
	public static TypesMapping TYPES = new TypesMapping(); static {
		TYPES.map("status", new SQLFilter.IntegerConverter());
                TYPES.map("pid", new SQLFilter.IntegerConverter());
		TYPES.map("batch_status", new SQLFilter.IntegerConverter());
		TYPES.map("planned", new SQLFilter.DateConvereter());
		TYPES.map("started", new SQLFilter.DateConvereter());
		TYPES.map("finished", new SQLFilter.DateConvereter());
	}
	
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
        try {
            if (startingOptions.has("parameters")) {
                Object parameters = startingOptions.get("parameters");
                if (parameters instanceof JSONArray) {
                    return plainProcessStart(def,(JSONArray) parameters);
                } else {
                    throw new CannotStartProcess("invalid parameters key");
                }
            } else if (startingOptions.has("mapping")) {
                Object mapping = startingOptions.get("mapping");
                if (mapping instanceof JSONObject) {
                    return parametrizedProcessStart(def,startingOptions.getJSONObject("mapping"));
                } else {
                    throw new CannotStartProcess("invalid mapping key");
                }
            } else {
                return plainProcessStart(def, new JSONArray());
            }
        } catch (JSONException e) {
            throw new GenericApplicationException(e.getMessage());
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

    
    Response plainProcessStart(String def, JSONArray array) {
        LRProcessDefinition definition = processDefinition(def);
        if (definition == null) throw new NoProcessFound("definition not found");

        
        String loggedUserKey = findLoggedUserKey();
        User user = this.loggedUsersSingleton.getUser(loggedUserKey);
        if (user == null) {
            //throw new SecurityException("access denided");
            throw new ActionNotAllowed("action is not allowed");
        }

        SecuredActions actionFromDef = securedAction(def, definition);
        boolean permitted = permit(actionAllowed, actionFromDef, user);
        if (permitted) {
            try {
                
                if (definition != null) {
                    if (!definition.isInputTemplateDefined()) {
                        
                        LRProcess newProcess = definition.createNewProcess(authToken(), groupToken());
                        newProcess.setLoggedUserKey(loggedUserKey);

                        
                        List<String> params = new ArrayList<String>();
                        for (int i = 0,ll=array.length(); i < ll; i++) {
                            params.add(array.getString(i));
                        }
                        newProcess.setParameters(params);
                        newProcess.setUser(user);
                        newProcess.planMe(new Properties(),IPAddressUtils.getRemoteAddress(this.requestProvider.get(), KConfiguration.getInstance().getConfiguration()));
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
            } catch (JSONException e) {
                throw new CannotStartProcess(e.getMessage(),e);
            }
        } else {
            throw new ActionNotAllowed("action is not allowed");
        }
    }


    public String authToken() {
        return requestProvider.get().getHeader(AUTH_TOKEN_HEADER_KEY);
    }

    public String groupToken() {
        HttpServletRequest request = requestProvider.get();
        String gtoken = request.getHeader(TOKEN_ATTRIBUTE_KEY);
        return gtoken;
    }

    Response parametrizedProcessStart(@PathParam("def")String def,  JSONObject mapping){
        LRProcessDefinition definition = processDefinition(def);
        if (definition == null) throw new NoProcessFound("definition not found");
        
        String loggedUserKey = findLoggedUserKey();
        User user = this.loggedUsersSingleton.getUser(loggedUserKey);
        if (user == null) {
            // no user
            //throw new SecurityException("access denided");
            throw new ActionNotAllowed("action is not allowed");
        }

        SecuredActions actionFromDef = securedAction(def, definition);
        boolean permitted = permit(actionAllowed, actionFromDef, user);

        if (permitted) {
            try {
                LRProcess newProcess = definition.createNewProcess(authToken(), groupToken());
                newProcess.setLoggedUserKey(loggedUserKey);

                Properties props = new Properties();
                for (Iterator iterator = mapping.keys(); iterator.hasNext();) {
                    String key = (String) iterator.next();
                    try {
                        props.put(key.toString(),mapping.get(key).toString());
                    } catch (JSONException e) {
                        throw new GenericApplicationException(e.getMessage());
                    }
                    
                }
                
                newProcess.setParameters(Arrays.asList(new String[0]));
                newProcess.setUser(user);

                newProcess.planMe(props, IPAddressUtils.getRemoteAddress(this.requestProvider.get(), KConfiguration.getInstance().getConfiguration()));
                lrProcessManager.updateAuthTokenMapping(newProcess, loggedUserKey);
                URI uri = UriBuilder.fromResource(LRResource.class).path("{uuid}").build(newProcess.getUUID());
                return Response.created(uri).entity(lrPRocessToJSONObject(newProcess).toString()).build();
            } catch (JSONException e) {
                throw new GenericApplicationException(e.getMessage());
            }
        } else {
            throw new ActionNotAllowed("action is not allowed");
        }
    }


    public LRProcessDefinition processDefinition(String def) {
        definitionManager.load();
        LRProcessDefinition definition = definitionManager.getLongRunningProcessDefinition(def);
        return definition;
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

        LRProcess lrPRocess = lrProcessManager.getLongRunningProcess(uuid);
        String loggedUserKey = findLoggedUserKey();
        User user = this.loggedUsersSingleton.getUser(loggedUserKey);
        if (user == null) {
            // no user
            //throw new SecurityException("access denided");
            throw new ActionNotAllowed("action is not allowed");
        }

        SecuredActions actionFromDef = securedAction(lrPRocess.getDefinitionId(), processDefinition(lrPRocess.getDefinitionId()));
        boolean permitted = permit(actionAllowed, actionFromDef, user);
        if (permitted) {
            if (stop != null) {
                this.definitionManager.load();
                LRProcess lrProcess = lrProcessManager.getLongRunningProcess(uuid);
                if (lrProcess == null) throw new NoProcessFound("cannot find process "+uuid);
                if (!States.notRunningState(lrProcess.getProcessState())) {
                    try {
                        lrProcess.stopMe();
                        lrProcessManager.updateLongRunningProcessFinishedDate(lrProcess);
                        LRProcess nLrProcess = lrProcessManager.getLongRunningProcess(uuid);
                        if (nLrProcess != null) {
                            String jsonRepre = lrPRocessToJSONObject(nLrProcess).toString();
                            
                            Response build = Response.ok().entity(jsonRepre).build();
                            return  build;
                        } else throw new NoProcessFound("cannot find process "+uuid);
                    } catch (JSONException e) {
                        throw new GenericApplicationException(e.getMessage());
                    }
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

        LRProcess lrPRocess = lrProcessManager.getLongRunningProcess(uuid);
        if (lrPRocess == null) {
            throw new NoProcessFound("cannot find process"+uuid);
        }
        String loggedUserKey = findLoggedUserKey();
        User user = this.loggedUsersSingleton.getUser(loggedUserKey);
        if (user == null) {
            // no user
            //throw new SecurityException("access denided");
            throw new ActionNotAllowed("action is not allowed");
        }

        SecuredActions actionFromDef = securedAction(lrPRocess.getDefinitionId(), processDefinition(lrPRocess.getDefinitionId()));
        boolean permitted = permit(actionAllowed, actionFromDef, user);

        
        //TODO: security
        if (permitted) {
            Lock lock = this.lrProcessManager.getSynchronizingLock();
            lock.lock();
            try {
                LRProcess longRunningProcess = this.lrProcessManager.getLongRunningProcess(uuid);
                if (longRunningProcess != null) {
                    if (BatchStates.expect(longRunningProcess.getBatchState(), BatchStates.BATCH_FAILED, BatchStates.BATCH_FINISHED, BatchStates.BATCH_WARNING)) {
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
            } catch (JSONException e) {
                throw new GenericApplicationException(e.getMessage());
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
        LRProcess lrPRocess = lrProcessManager.getLongRunningProcess(uuid);
        String loggedUserKey = findLoggedUserKey();
        User user = this.loggedUsersSingleton.getUser(loggedUserKey);
        if (user == null) {
            // no user
            //throw new SecurityException("access denided");
            throw new ActionNotAllowed("action is not allowed");
        }

        SecuredActions actionFromDef = securedAction(lrPRocess.getDefinitionId(), processDefinition(lrPRocess.getDefinitionId()));
        boolean permitted = permit(actionAllowed, actionFromDef, user);
        if (permitted) {
            try {
                JSONObject jsonObj = new JSONObject();
                LRProcess lrProcesses = this.lrProcessManager.getLongRunningProcess(uuid);
                if (lrProcesses != null) {
                    InputStream os = lrProcesses.getStandardProcessOutputStream();
                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    IOUtils.copyStreams(os, bos);
                    jsonObj.put("sout", new String(Base64Coder.encode(bos.toByteArray())));
                    InputStream er = lrProcesses.getErrorProcessOutputStream();
                    ByteArrayOutputStream ber = new ByteArrayOutputStream();
                    IOUtils.copyStreams(er, ber);
                    jsonObj.put("serr", new String(Base64Coder.encode(ber.toByteArray())));
                    return Response.ok().entity(jsonObj.toString()).build();
                } else  throw new NoProcessFound("process not found "+uuid);
            } catch (FileNotFoundException e) {
                LOGGER.log(Level.SEVERE,e.getMessage(),e);
                throw new LogsNotFound(e.getMessage(),e);
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE,e.getMessage(),e);
                throw new CannotReadLogs(e.getMessage());
            } catch (JSONException e) {
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
        LRProcess lrPRocess = lrProcessManager.getLongRunningProcess(uuid);
        String loggedUserKey = findLoggedUserKey();
        User user = this.loggedUsersSingleton.getUser(loggedUserKey);
        if (user == null) {
            // no user
            //throw new SecurityException("access denided");
            throw new ActionNotAllowed("action is not allowed");
        }

        SecuredActions actionFromDef = securedAction(lrPRocess.getDefinitionId(), processDefinition(lrPRocess.getDefinitionId()));
        boolean permitted = permit(actionAllowed, actionFromDef, user);
        if (permitted) {
            LRProcess lrProc = this.lrProcessManager.getLongRunningProcess(uuid);
            if (lrProc != null) {
                try {
                    JSONObject jsonObject = lrPRocessToJSONObject(lrProc);
                    if (lrProc.isMasterProcess()) {
                        JSONArray array = new JSONArray();
                        List<LRProcess> childSubprecesses = this.lrProcessManager.getLongRunningProcessesByGroupToken(lrProc.getGroupToken());
                        for (LRProcess child : childSubprecesses) {
                            array.put(lrPRocessToJSONObject(child));
                        }
                        jsonObject.put("children", array);
                    }
                    return Response.ok().entity(jsonObject.toString()).build();
                } catch (JSONException e) {
                    throw new GenericApplicationException(e.getMessage());
                }
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
            @QueryParam("state")  String filterState,
            @QueryParam("batchState")  String filterBatchState,
            @QueryParam("name")  String filterName,
            @QueryParam("userid")  String filterUserId,
            @QueryParam("userFirstname")  String filterUserFirstname,
            @QueryParam("userSurname")  String filterUserSurname,
            @QueryParam("offset") String of,
            @QueryParam("resultSize") String resultSize,
            @QueryParam("ordering") @DefaultValue("ASC")String ordering) {
    	
    	
    	if (ordering != null) {
        	ordering = ordering.toUpperCase();
        	if (!ordering.equals("ASC") && !ordering.equals("DESC")) {
        		ordering = "ASC";
        	}
    	}

        //LRProcess lrPRocess = lrProcessManager.getLongRunningProcess(uuid);
        String loggedUserKey = findLoggedUserKey();
        User user = this.loggedUsersSingleton.getUser(loggedUserKey);
        if (user == null) {
            // no user
            //throw new SecurityException("access denided");
            throw new ActionNotAllowed("action is not allowed");
        }

        boolean permitted = permit(actionAllowed,  user);

        if (permitted) {

                try {
                    Map<String, String> filterMap = new HashMap<String, String>(); {
                        if (StringUtils.isAnyString(filterUUID)) filterMap.put("uuid", filterUUID);
                        if (StringUtils.isAnyString(filterPid)) filterMap.put("pid", filterPid);
                        if (StringUtils.isAnyString(filterDef)) filterMap.put("def", filterDef);
                        if (StringUtils.isAnyString(filterState)) filterMap.put("state", filterState);
                        if (StringUtils.isAnyString(filterBatchState)) filterMap.put("batchState", filterBatchState);
                        if (StringUtils.isAnyString(filterName)) filterMap.put("name", filterName);
                        if (StringUtils.isAnyString(filterUserId)) filterMap.put("userid", filterUserId);
                        if (StringUtils.isAnyString(filterUserFirstname)) filterMap.put("userFirstname", filterUserFirstname);
                        if (StringUtils.isAnyString(filterUserSurname)) filterMap.put("userSurname", filterUserSurname);
                    };
                    SQLFilter filter = DbFilterUtils.simpleFilter(filterMap, TYPES);
                    List<LRProcess> lrProcesses = this.lrProcessManager.getLongRunningProcessesAsGrouped(lrProcessOrdering(LRProcessOrdering.PLANNED.name()), typeOfOrdering(ordering), offset(of, resultSize), filter);
                    JSONArray retList = new JSONArray();

                    for (LRProcess lrProcess : lrProcesses) {
                        JSONObject ent = lrPRocessToJSONObject(lrProcess);
                        if (lrProcess.isMasterProcess()) {
                            JSONArray array = new JSONArray();
                            List<LRProcess> childSubprecesses = this.lrProcessManager.getLongRunningProcessesByGroupToken(lrProcess.getGroupToken());
                            for (LRProcess child : childSubprecesses) {
                                array.put(lrPRocessToJSONObject(child));
                            }
                            ent.put("children", array);
                        }
                        retList.put(ent);
                    }
                    return Response.ok().entity(retList.toString()).build();
                } catch (JSONException e) {
                    throw new GenericApplicationException(e.getMessage());
                }
        } else {
            throw new ActionNotAllowed("action is not allowed");
        }
    }

    /*	
    SQLFilter lrPRocessFilter(Map<String, String>filterMap) {
        List<Tripple> tripples = new ArrayList<SQLFilter.Tripple>();
        for (String key : filterMap.keySet()) {
            String val = filterMap.get(key);
            if (val != null ) {
                Tripple tripple = createTripple(key+"="+val);
                tripples.add(tripple);
            }
        }
        return  SQLFilter.createFilter(tripples);
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
    }*/

    private Offset offset(String of, String resultSize) {
        String sof = of != null ? of : "0";
        String sres = resultSize != null ? resultSize : DEFAULT_SIZE;
        return new Offset(sof, sres);
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


    private JSONObject lrPRocessToJSONObject(LRProcess lrProcess) throws JSONException {
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
    
    
    public String findLoggedUserKey() {
        
        if (groupToken() != null) {
            if (lrProcessManager.isAuthTokenClosed(authToken())) {
                //throw new SecurityException("access denided");
                throw new ActionNotAllowed("action is not allowed");
            }
            List<LRProcess> processes = lrProcessManager.getLongRunningProcessesByGroupToken(groupToken());
            if (!processes.isEmpty()) {
                // hledani klice 
                List<States> childStates = new ArrayList<States>();
                childStates.add(States.PLANNED);
                // prvni je master process -> vynechavam
                for (int i = 1,ll=processes.size(); i < ll; i++) {
                    childStates.add(processes.get(i).getProcessState());
                }

                LRProcess process = processes.get(0);
                //process.setProcessState(States.calculateBatchState(childStates));
                process.setBatchState(BatchStates.calculateBatchState(childStates));
                
                lrProcessManager.updateLongRunningProcessState(process);
                
                return lrProcessManager.getSessionKey(process.getAuthToken());
            } else {
                throw new RuntimeException("cannot find process with token '"+groupToken()+"'");
            }
        } else {
            userProvider.get();
            return (String) requestProvider.get().getSession().getAttribute(UserUtils.LOGGED_USER_KEY_PARAM);
        }
    }


    boolean permit(IsActionAllowed rightsResolver, User user) {
        boolean permited = user != null ? rightsResolver.isActionAllowed(user,SecuredActions.MANAGE_LR_PROCESS.getFormalName(), SpecialObjects.REPOSITORY.getPid(), null , ObjectPidsPath.REPOSITORY_PATH) : false;
        return permited;
    }

    boolean permit(IsActionAllowed rightsResolver, SecuredActions action, User user) {
        boolean permited = user!= null? (rightsResolver.isActionAllowed(user,SecuredActions.MANAGE_LR_PROCESS.getFormalName(), SpecialObjects.REPOSITORY.getPid(), null , ObjectPidsPath.REPOSITORY_PATH) || 
                            (action != null && rightsResolver.isActionAllowed(user, action.getFormalName(), SpecialObjects.REPOSITORY.getPid(),null, ObjectPidsPath.REPOSITORY_PATH))) : false ;
        return permited;
    }

    public SecuredActions securedAction(String def, LRProcessDefinition definition) {
        return definition.getSecuredAction() != null ? SecuredActions.findByFormalName(definition.getSecuredAction()) : SecuredActions.findByFormalName(def);
    }
    
}
