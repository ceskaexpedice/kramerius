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
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.concurrent.locks.Lock;
import java.util.logging.Level;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import jj2000.j2k.entropy.encoder.ByteOutputBuffer;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.antlr.stringtemplate.StringTemplate;
import org.antlr.stringtemplate.StringTemplateGroup;
import org.antlr.stringtemplate.language.DefaultTemplateLexer;
import org.fedora.api.Condition;

import antlr.RecognitionException;
import antlr.TokenStreamException;
import biz.sourcecode.base64Coder.Base64Coder;

import com.google.inject.Inject;
import com.google.inject.Provider;

import cz.incad.kramerius.processes.BatchStates;
import cz.incad.kramerius.processes.DefinitionManager;
import cz.incad.kramerius.processes.LRPRocessFilter;
import cz.incad.kramerius.processes.LRProcess;
import cz.incad.kramerius.processes.LRProcessDefinition;
import cz.incad.kramerius.processes.LRProcessManager;
import cz.incad.kramerius.processes.LRProcessOffset;
import cz.incad.kramerius.processes.LRProcessOrdering;
import cz.incad.kramerius.processes.States;
import cz.incad.kramerius.processes.TypeOfOrdering;
import cz.incad.kramerius.processes.LRPRocessFilter.Op;
import cz.incad.kramerius.processes.LRPRocessFilter.Tripple;
import cz.incad.kramerius.rest.api.processes.filter.FilterCondition;
import cz.incad.kramerius.rest.api.processes.filter.Operand;
import cz.incad.kramerius.security.IsActionAllowed;
import cz.incad.kramerius.security.SecuredActions;
import cz.incad.kramerius.security.User;
import cz.incad.kramerius.security.utils.UserUtils;
import cz.incad.kramerius.users.LoggedUsersSingleton;
import cz.incad.kramerius.utils.IOUtils;
import cz.incad.kramerius.utils.params.ParamsLexer;
import cz.incad.kramerius.utils.params.ParamsParser;


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
    
    
    @GET
    @Path("plainStart/{def}")
    @Produces(MediaType.APPLICATION_JSON)
    public String plainProcessStart(@PathParam("def")String def, @QueryParam("params") String params){
        if (this.actionAllowed.isActionAllowed(this.userProvider.get(), SecuredActions.MANAGE_LR_PROCESS.getFormalName())) {
            definitionManager.load();
            LRProcessDefinition definition = definitionManager.getLongRunningProcessDefinition(def);
            if (!definition.isInputTemplateDefined()) {
                User user = userProvider.get();
                String loggedUserKey =  (String) this.requestProvider.get().getSession().getAttribute(UserUtils.LOGGED_USER_KEY_PARAM);
                
                HttpServletRequest request = this.requestProvider.get();
                
                LRProcess newProcess = definition.createNewProcess(request.getHeader(AUTH_TOKEN_HEADER_KEY), request.getParameter(TOKEN_ATTRIBUTE_KEY));
                newProcess.setLoggedUserKey(loggedUserKey);
                newProcess.setParameters(Arrays.asList(params));
                newProcess.setUser(user);
                newProcess.planMe(new Properties());
                lrProcessManager.updateAuthTokenMapping(newProcess, loggedUserKey);
                return lrPRocessToJSONObject(newProcess).toString();
            } else {
                throw new LRResourceCannotStartProcess("not plain process "+def);
            }
        } else {
            throw new ActionNotAllowed("not allowed");
        }
    }

    @GET
    @Path("parametrizedStart/{def}")
    @Produces(MediaType.APPLICATION_JSON)
    public String parametrizedProcessStart(@PathParam("def")String def,  @QueryParam("paramsMapping") String paramsMapping){
        if (this.actionAllowed.isActionAllowed(this.userProvider.get(), SecuredActions.MANAGE_LR_PROCESS.getFormalName())) {
            definitionManager.load();

            try {
                LRProcessDefinition definition = definitionManager.getLongRunningProcessDefinition(def);

                User user = userProvider.get();
                String loggedUserKey =  (String) this.requestProvider.get().getSession().getAttribute(UserUtils.LOGGED_USER_KEY_PARAM);
                
                HttpServletRequest request = this.requestProvider.get();
                
                LRProcess newProcess = definition.createNewProcess(request.getHeader(AUTH_TOKEN_HEADER_KEY), request.getParameter(TOKEN_ATTRIBUTE_KEY));
                newProcess.setLoggedUserKey(loggedUserKey);


                ParamsParser paramsParser = new ParamsParser(new ParamsLexer(new StringReader(paramsMapping)));
                List params = paramsParser.params();
                Properties props = new Properties();
                for (Object paramVal : params) {
                    if (paramVal instanceof List) {
                        List alist = (List) paramVal;
                        if (alist.size() == 1 ) {
                            String[] pair = alist.get(0).toString().split("=");
                            props.put(pair[0], pair[1]);
                        }
                    }
                }
                
                newProcess.setParameters(Arrays.asList(new String[0]));
                newProcess.setUser(user);

                newProcess.planMe(props);
                lrProcessManager.updateAuthTokenMapping(newProcess, loggedUserKey);
                return lrPRocessToJSONObject(newProcess).toString();
            } catch (RecognitionException e) {
                throw new LRResourceCannotStartProcess(e.getMessage());
            } catch (TokenStreamException e) {
                throw new LRResourceCannotStartProcess(e.getMessage());
            }
        } else {
            throw new ActionNotAllowed("not allowed");
        }
    }
    
    @GET
    @Path("stop/{uuid}")
    @Produces(MediaType.APPLICATION_JSON)
    public String processStop(@PathParam("uuid")String uuid){
        if (this.actionAllowed.isActionAllowed(this.userProvider.get(), SecuredActions.MANAGE_LR_PROCESS.getFormalName())) {
            this.definitionManager.load();
            LRProcess lrProcess = lrProcessManager.getLongRunningProcess(uuid);
            if (lrProcess == null) throw new LRResourceProcessNotFound(uuid);
            lrProcess.stopMe();
            lrProcessManager.updateLongRunningProcessFinishedDate(lrProcess);
            LRProcess nLrProcess = lrProcessManager.getLongRunningProcess(uuid);
            if (nLrProcess != null) return  lrPRocessToJSONObject(nLrProcess).toString();
            else throw new LRResourceProcessNotFound(uuid);
        } else {
            throw new ActionNotAllowed("not allowed");
        }
    }

    @GET
    @Path("delete/{uuid}")
    @Produces(MediaType.APPLICATION_JSON)
    public String deleteProcess(@PathParam("uuid")String uuid){
        if (this.actionAllowed.isActionAllowed(this.userProvider.get(), SecuredActions.MANAGE_LR_PROCESS.getFormalName())) {
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
                    throw new LRResourceProcessNotFound("process not found "+uuid);
                }
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("deleted", uuid);
                return jsonObject.toString();
            } finally {
                lock.unlock();
            }
        } else {
            throw new ActionNotAllowed("not allowed");
        }
    }

    @GET
    @Path("logs/{uuid}")
    @Produces(MediaType.APPLICATION_JSON)
    public String processLogs(@PathParam("uuid")String uuid){
        if (this.actionAllowed.isActionAllowed(this.userProvider.get(), SecuredActions.MANAGE_LR_PROCESS.getFormalName())) {
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
                    
                    return jsonObj.toString();
                } else  throw new LRResourceProcessNotFound("process not found "+uuid);
    
            } catch (FileNotFoundException e) {
                LOGGER.log(Level.SEVERE,e.getMessage(),e);
                throw new LRResourceCannotReadLogs(e.getMessage());
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE,e.getMessage(),e);
                throw new LRResourceCannotReadLogs(e.getMessage());
            }
        } else {
            throw new ActionNotAllowed("not allowed");
        }
    }

    @GET
    @Path("desc/{uuid}")
    @Produces(MediaType.APPLICATION_JSON)
    public String getProcessDescription(@PathParam("uuid")String uuid){
        if (this.actionAllowed.isActionAllowed(this.userProvider.get(), SecuredActions.MANAGE_LR_PROCESS.getFormalName())) {
            LRProcess lrProcesses = this.lrProcessManager.getLongRunningProcess(uuid);
            return lrPRocessToJSONObject(lrProcesses).toString();
        } else {
            throw new ActionNotAllowed("not allowed");
        }
    }    

    
    
    
    @GET
    @Path("list")
    @Produces(MediaType.APPLICATION_JSON)
    public String getProcessDescriptions(@QueryParam("filter")String filter,@QueryParam("ordering")String ordering, @QueryParam("typeofordering") String type,@QueryParam("offset") String of){
        if (this.actionAllowed.isActionAllowed(this.userProvider.get(), SecuredActions.MANAGE_LR_PROCESS.getFormalName())) {
            try {
                JSONArray jsonArr = new JSONArray();
                List<LRProcess> lrProcesses = this.lrProcessManager.getLongRunningProcessesAsGrouped(lrProcessOrdering(ordering), typeOfOrdering(type), offset(of), lrPRocessFilter(filter));
                for (LRProcess lrProcess : lrProcesses) {
                    JSONObject jsonLrProcess = lrPRocessToJSONObject(lrProcess);
                    if (lrProcess.isMasterProcess()) {
                        JSONArray childrenJSONs = new JSONArray();
                        List<LRProcess> childSubprecesses = this.lrProcessManager.getLongRunningProcessesByGroupToken(lrProcess.getGroupToken());
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
        } else {
            throw new ActionNotAllowed("not allowed");
        }
    }


    
    
    private LRPRocessFilter lrPRocessFilter(String f) throws RecognitionException, TokenStreamException {
        if (f == null) return null;
        ParamsParser paramsParser = new ParamsParser(new ParamsLexer(new StringReader(f)));
        List params = paramsParser.params();
        List<Tripple> tripples = new ArrayList<LRPRocessFilter.Tripple>();
        for (Object object : params) {
            if (object instanceof List) {
                List oneParamCondition = (List) object;
                if (oneParamCondition.size() == 1 ) {
                    Tripple tripple = createTripple(oneParamCondition.get(0).toString());
                    if (tripple == null) return null;
                    else if (tripple.getVal() != null) {
                        tripples.add(tripple);
                    }
                } else {
                    LOGGER.warning("cannot process '"+object+"'");
                }
                
            } else {
                LOGGER.warning("cannot process '"+object+"'");
            }
        }
        LRPRocessFilter filter = LRPRocessFilter.createFilter(tripples);
        return filter;
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
        
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("uuid", lrProcess.getUUID());
        jsonObject.put("pid", lrProcess.getPid());
        jsonObject.put("def", lrProcess.getDefinitionId());
        jsonObject.put("state", lrProcess.getProcessState().toString());
        jsonObject.put("batchState", lrProcess.getBatchState().toString());
        jsonObject.put("name", lrProcess.getProcessName());
        jsonObject.put("started", FORMAT.format(new Date(lrProcess.getStartTime())));
        jsonObject.put("planned", FORMAT.format(new Date(lrProcess.getPlannedTime())));
        jsonObject.put("finished", FORMAT.format(new Date(lrProcess.getFinishedTime())));
        jsonObject.put("userid", lrProcess.getLoginname());
        jsonObject.put("userFirstname", lrProcess.getFirstname());
        jsonObject.put("userSurname", lrProcess.getSurname());
        
        return jsonObject;
    }

    
}
