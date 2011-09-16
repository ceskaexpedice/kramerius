package cz.incad.Kramerius;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.sql.BatchUpdateException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;
import java.util.concurrent.locks.Lock;
import java.util.logging.Level;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import antlr.RecognitionException;
import antlr.TokenStreamException;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.sun.corba.se.impl.activation.ProcessMonitorThread;

import cz.incad.Kramerius.backend.guice.GuiceServlet;
import cz.incad.Kramerius.processes.ParamsLexer;
import cz.incad.Kramerius.processes.ParamsParser;
import cz.incad.Kramerius.security.KrameriusRoles;
import cz.incad.kramerius.ObjectPidsPath;
import cz.incad.kramerius.intconfig.InternalConfiguration;
import cz.incad.kramerius.processes.DefinitionManager;
import cz.incad.kramerius.processes.GCScheduler;
import cz.incad.kramerius.processes.LRProcess;
import cz.incad.kramerius.processes.LRProcessDefinition;
import cz.incad.kramerius.processes.LRProcessManager;
import cz.incad.kramerius.processes.LRProcessOffset;
import cz.incad.kramerius.processes.LRProcessOrdering;
import cz.incad.kramerius.processes.ProcessScheduler;
import cz.incad.kramerius.processes.States;
import cz.incad.kramerius.processes.TypeOfOrdering;
import cz.incad.kramerius.processes.utils.ProcessUtils;
import cz.incad.kramerius.security.IsActionAllowed;
import cz.incad.kramerius.security.Role;
import cz.incad.kramerius.security.SecuredActions;
import cz.incad.kramerius.security.SecurityException;
import cz.incad.kramerius.security.SpecialObjects;
import cz.incad.kramerius.security.User;
import cz.incad.kramerius.security.UserManager;
import cz.incad.kramerius.security.impl.UserImpl;
import cz.incad.kramerius.security.utils.UserUtils;
import cz.incad.kramerius.users.LoggedUsersSingleton;
import cz.incad.kramerius.utils.ApplicationURL;
import cz.incad.kramerius.utils.conf.KConfiguration;
import cz.incad.kramerius.utils.database.JDBCQueryTemplate;

/**
 * This is support for long running processes
 * 
 * @author pavels
 */
public class LongRunningProcessServlet extends GuiceServlet {

    private static final long serialVersionUID = 1L;

    public static final java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(LongRunningProcessServlet.class.getName());

    @Inject
    DefinitionManager definitionManager;

    @Inject
    LRProcessManager lrProcessManager;

    @Inject
    KConfiguration configuration;

    @Inject
    ProcessScheduler processScheduler;

    @Inject
    GCScheduler gcScheduler;


    @Inject
    IsActionAllowed actionAllowed;

    @Inject
    Provider<User> userProvider;
    @Inject
    UserManager usersManager;
    
    @Inject
    LoggedUsersSingleton loggedUsersSingleton;
    
    @Override
    public void init() throws ServletException {
        super.init();
        try {
            // classpath from war
            String appLibPath = getWebAppClasspath();
            // security core from tomcat/lib
            String jarFile = getSecurityCoreJarFile();
            
            KConfiguration conf = KConfiguration.getInstance();
            if ((conf.getApplicationURL() == null) || (conf.getApplicationURL().equals(""))) {
                throw new RuntimeException("lr servlet need configuration parameter 'applicationUrl'");
            }

            this.processScheduler.init(appLibPath, jarFile);
            this.gcScheduler.init();
        } catch (URISyntaxException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(),e);
        }
    }

    public String getSecurityCoreJarFile() throws URISyntaxException {
        URL url= JDBCQueryTemplate.class.getResource(JDBCQueryTemplate.class.getSimpleName()+".class");
        String jarFile = url.getFile();
        if (jarFile.contains("!")) {
            StringTokenizer tokenizer = new StringTokenizer(jarFile,"!");
            if (tokenizer.hasMoreTokens()) {
                String nextToken = tokenizer.nextToken();
                File nfile = new File(new URI(nextToken));
                return nfile.getAbsolutePath();
            } else return null;
        } else return null;
    }

    public String getWebAppClasspath() {
        String appLibPath = getServletContext().getRealPath("WEB-INF/lib");
        return appLibPath;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws  IOException {
        try {
            String action = req.getParameter("action");
            if (action == null)
                action = Actions.start.name();
            Actions selectedAction = Actions.valueOf(action);
            selectedAction.doAction(getServletContext(), req, resp, this.definitionManager, this.lrProcessManager, this.usersManager, this.userProvider, this.actionAllowed, this.loggedUsersSingleton);
        } catch (SecurityException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(),e);
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
        }
    }
    

    public static LRProcess planNewProcess(HttpServletRequest request, ServletContext context, String def, DefinitionManager definitionManager, String[] params, User user, String loggedUserKey) {
        definitionManager.load();
        LRProcessDefinition definition = definitionManager.getLongRunningProcessDefinition(def);
        if (definition == null) {
            throw new RuntimeException("cannot find process definition '" + def + "'");
        }
        String token = request.getParameter("token");
        LRProcess newProcess = definition.createNewProcess(token);
        newProcess.setUser(user);
        newProcess.setLoggedUserKey(loggedUserKey);
        newProcess.setParameters(Arrays.asList(params));
        newProcess.planMe();
        return newProcess;
    }

    public static LRProcess stopOldProcess(String defaultLibDir, String uuidOfProcess, DefinitionManager defManager, LRProcessManager lrProcessManager) {
        defManager.load();
        lrProcessManager.getLongRunningProcess(uuidOfProcess).stopMe();
        return lrProcessManager.getLongRunningProcess(uuidOfProcess);
    }

    public static SecuredActions getAction(String def) {
        SecuredActions[] securedActions = SecuredActions.values();
        for (SecuredActions act : securedActions) {
            if (act.getFormalName().equals(def)) return act;
        }
        return null;
    }

    private static void updateProcessTokenMapping(LRProcess nprocess,String loggedUserKey, LRProcessManager lrProcessManager) {
        lrProcessManager.updateTokenMapping(nprocess, loggedUserKey);
    }
    
    static enum Actions {

        /**
         * Plan new process
         */
        start {
            public void doAction(ServletContext context, HttpServletRequest req, HttpServletResponse resp, DefinitionManager defManager, LRProcessManager lrProcessManager, UserManager userManager, Provider<User> userProvider, IsActionAllowed rightsResolver, LoggedUsersSingleton loggedUserSingleton) {
                try {
                    String def = req.getParameter("def");
                    String out = req.getParameter("out");
                    String[] params = getParams(req);
                    //TODO: Zjisteni predavane autentizace 
                    SecuredActions actionFromDef = SecuredActions.findByFormalName(def);
                    String token = req.getParameter("token");
                    User user = null;
                    String loggedUserKey = null;
                    
                    if (token != null) {
                        
                        List<LRProcess> processes = lrProcessManager.getLongRunningProcessesByToken(token);
                        if (!processes.isEmpty()) {
                            // hledani klice 
                            List<States> childStates = new ArrayList<States>();
                            childStates.add(States.PLANNED);
                            // prvni je master process -> vynechavam
                            for (int i = 1,ll=processes.size(); i < ll; i++) {
                                childStates.add(processes.get(i).getProcessState());
                            }


                            LRProcess process = processes.get(0);
                            process.setProcessState(States.calculateBatchState(childStates));
                            lrProcessManager.updateLongRunningProcessState(process);
                            
                            loggedUserKey = lrProcessManager.getSessionKey(process.getToken());
                            user = loggedUserSingleton.getUser(loggedUserKey);
                        } else {
                            throw new RuntimeException("cannot find process with token '"+token+"'");
                        }
                    } else {
                        user = userProvider.get();
                        loggedUserKey = (String) req.getSession().getAttribute(UserUtils.LOGGED_USER_KEY_PARAM);
                        
                    }
                    boolean permited = user!= null? (rightsResolver.isActionAllowed(user,SecuredActions.MANAGE_LR_PROCESS.getFormalName(), SpecialObjects.REPOSITORY.getPid(), ObjectPidsPath.REPOSITORY_PATH) || 
                                        (actionFromDef != null && rightsResolver.isActionAllowed(user, actionFromDef.getFormalName(), SpecialObjects.REPOSITORY.getPid(), ObjectPidsPath.REPOSITORY_PATH))) : false ;
                    if (permited) {
                        LRProcess nprocess = planNewProcess(req, context, def, defManager, params, user,loggedUserKey);
                        // update process and token mapping
                        updateProcessTokenMapping(nprocess,  loggedUserKey,lrProcessManager);
                        
                        if ((out != null) && (out.equals("text"))) {
                            resp.setContentType("text/plain");
                            resp.getOutputStream().print("[" + nprocess.getDefinitionId() + "]" + nprocess.getProcessState().name());
                        } else {
                            StringBuffer buffer = new StringBuffer();
                            buffer.append("<html><body>");
                            buffer.append("<ul>");
                            buffer.append("<li>").append(nprocess.getDefinitionId());
                            buffer.append("<li>").append(nprocess.getUUID());
                            buffer.append("<li>").append(nprocess.getPid());
                            buffer.append("<li>").append(new Date(nprocess.getStartTime()));
                            buffer.append("<li>").append(nprocess.getProcessState());
                            buffer.append("</ul>");
                            buffer.append("</body></html>");
                            resp.setContentType("text/html");
                            resp.getOutputStream().println(buffer.toString());
                        }
                    } else {
                        throw new SecurityException("access denided");
                    }
                } catch (IOException e) {
                    LOGGER.log(Level.SEVERE, e.getMessage(), e);
                } catch (RecognitionException e) {
                    LOGGER.log(Level.SEVERE, e.getMessage(), e);
                } catch (TokenStreamException e) {
                    LOGGER.log(Level.SEVERE, e.getMessage(), e);
                }
            }

        },

        /**
         * Stop running process
         */
        stop {
            @Override
            public void doAction(ServletContext context, HttpServletRequest req, HttpServletResponse resp, DefinitionManager defManager, LRProcessManager lrProcessManager, UserManager userManager, Provider<User> userProvider, IsActionAllowed actionIsAllowed, LoggedUsersSingleton loggedUserSingleton) {
                if (actionIsAllowed.isActionAllowed(SecuredActions.MANAGE_LR_PROCESS.getFormalName(), SpecialObjects.REPOSITORY.getPid(), ObjectPidsPath.REPOSITORY_PATH)) {
                    try {
                        String uuid = req.getParameter("uuid");
                        String realPath = context.getRealPath("WEB-INF/lib");
                        LRProcess oProcess = stopOldProcess(realPath, uuid, defManager, lrProcessManager);
                        StringBuffer buffer = new StringBuffer();
                        buffer.append("<html><body>");
                        buffer.append("<ul>");
                        buffer.append("<li>").append(oProcess.getDefinitionId());
                        buffer.append("<li>").append(oProcess.getUUID());
                        buffer.append("<li>").append(oProcess.getPid());
                        buffer.append("<li>").append(new Date(oProcess.getStartTime()));
                        buffer.append("<li>").append(oProcess.getProcessState());
                        buffer.append("</ul>");
                        buffer.append("</body></html>");
                        resp.setContentType("text/html");
                        resp.getOutputStream().println(buffer.toString());
                    } catch (IOException e) {
                        LOGGER.log(Level.SEVERE, e.getMessage(), e);
                    }
                } else {
                    resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                }
            }

        },

        /*
        list {
            @Override
            public void doAction(ServletContext context, HttpServletRequest req, HttpServletResponse resp, DefinitionManager defManager, LRProcessManager lrProcessManager, UserManager userManager, Provider<User> userProvider, IsActionAllowed actionAllowed, LoggedUsersSingleton loggedUserSingleton) {
                if (actionAllowed.isActionAllowed(SecuredActions.MANAGE_LR_PROCESS.getFormalName(), SpecialObjects.REPOSITORY.getPid(), ObjectPidsPath.REPOSITORY_PATH)) {
                    try {
                        StringBuffer buffer = new StringBuffer();
                        buffer.append("<html><body>");
                        buffer.append("<h1>Running processes</h1>");
                        buffer.append("<ul>");
                        LRProcessOrdering ordering = LRProcessOrdering.NAME;
                        LRProcessOffset offset = new LRProcessOffset("0", "20");
                        List<LRProcess> longRunningProcesses = lrProcessManager.getLongRunningProcesses(ordering, TypeOfOrdering.ASC, offset);
                        for (LRProcess lrProcess : longRunningProcesses) {
                            buffer.append("<li>").append("PID:").append(lrProcess.getPid());
                            if (lrProcess.canBeStopped()) {
                                buffer.append("  ... <a href='" + lrServlet(req) + "?action=stop&uuid=" + lrProcess.getUUID() + "'>stop</a>");
                            }
                            buffer.append("<li>").append("uuid :").append(lrProcess.getUUID());
                            buffer.append("<li>").append("name :").append(lrProcess.getProcessName());
                            buffer.append("<li>").append("started :" + new Date(lrProcess.getStartTime()));
                            buffer.append("<li>").append("processState :").append(lrProcess.getProcessState());
                            LRProcessDefinition lrDef = defManager.getLongRunningProcessDefinition(lrProcess.getDefinitionId());
                            if (lrDef == null) {
                                throw new RuntimeException("cannot find definition '" + lrProcess.getDefinitionId() + "'");
                            }
                            buffer.append("<li>").append("errOut  :").append(lrDef.getErrStreamFolder() + File.separator + lrProcess.getUUID() + ".err");
                            buffer.append("<li>").append("standardOut  :").append(lrDef.getStandardStreamFolder() + File.separator + lrProcess.getUUID() + ".out");
                            buffer.append("<hr>");
                        }
                        buffer.append("</ul>");
                        buffer.append("</body></html>");

                        resp.setContentType("text/html");
                        resp.setCharacterEncoding("UTF-8");

                        resp.getWriter().println(buffer.toString());
                    } catch (IOException e) {
                        LOGGER.log(Level.SEVERE, e.getMessage(), e);
                    }
                } else {
                    resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                }
            }
        },*/

        updatePID {
            @Override
            public void doAction(ServletContext context, HttpServletRequest req, HttpServletResponse resp, DefinitionManager defManager, LRProcessManager lrProcessManager, UserManager userManager, Provider<User> userProvider, IsActionAllowed actionAllowed, LoggedUsersSingleton loggedUserSingleton) {
                Lock lock = lrProcessManager.getSynchronizingLock();
                lock.lock();
                try {
                    String uuid = req.getParameter("uuid");
                    String pid = req.getParameter("pid");
                    LRProcess longRunningProcess = lrProcessManager.getLongRunningProcess(uuid);
                    longRunningProcess.setPid(pid);
                    lrProcessManager.updateLongRunningProcessPID(longRunningProcess);
                } finally {
                    lock.unlock();
                }
            }
        },

        updateStatus {

            @Override
            public void doAction(ServletContext context, HttpServletRequest req, HttpServletResponse resp, DefinitionManager defManager, LRProcessManager processManager, UserManager userManager, Provider<User> userProvider, IsActionAllowed actionAllowed, LoggedUsersSingleton loggedUserSingleton) {
                String uuid = req.getParameter("uuid");
                String state = req.getParameter("state");
                Lock lock = processManager.getSynchronizingLock();
                lock.lock();
                try  {
                    LRProcess longRunningProcess = processManager.getLongRunningProcess(uuid);
                    List<LRProcess> processes = processManager.getLongRunningProcessesByToken(longRunningProcess.getToken());
                    if (processes.size() > 1) {
                        // zmena master processu
                        if (longRunningProcess.getUUID().equals(processes.get(0).getUUID())) {
                            boolean hasBatchState = States.expect(longRunningProcess.getProcessState(), States.BATCH_FAILED, States.BATCH_FINISHED, States.BATCH_STARTED);
                            boolean newStateIsBatch = States.expect(States.valueOf(state), States.BATCH_FAILED, States.BATCH_FINISHED, States.BATCH_STARTED);
                            if (hasBatchState && newStateIsBatch) {
                                longRunningProcess.setProcessState(States.valueOf(state));
                                processManager.updateLongRunningProcessState(longRunningProcess);
                            } else {
                                LOGGER.fine("calculating new master state");
                                List<States> childStates = new ArrayList<States>();
                                // prvni je master process -> vynechavam
                                for (int i = 1,ll=processes.size(); i < ll; i++) {
                                    childStates.add(processes.get(i).getProcessState());
                                }
                                longRunningProcess.setProcessState(States.calculateBatchState(childStates));
                                processManager.updateLongRunningProcessState(longRunningProcess);
                            }
                        // zmena child procesu    
                        } else {
                            
                            List<States> childStates = new ArrayList<States>();
                            childStates.add(States.valueOf(state));
                            // prvni je master process -> vynechavam
                            for (int i = 1,ll=processes.size(); i < ll; i++) {
                                childStates.add(processes.get(i).getProcessState());
                            }
                            processes.get(0).setProcessState(States.calculateBatchState(childStates));
                            processManager.updateLongRunningProcessState(processes.get(0));
                            
                            changeChildProcessState(processManager, state, longRunningProcess);
                        }
                    } else {
                        // uprava stavu pro jeden samotinky proces
                        changeChildProcessState(processManager, state, longRunningProcess);
                    }
                    
//                    if (!processes.isEmpty()) {

                } finally {
                    lock.unlock();
                }
            }

            public void changeChildProcessState(LRProcessManager processManager, String state, LRProcess longRunningProcess) {
                if (state != null) {
                    States st = States.valueOf(state);
                    if (st.equals(States.KILLED) && longRunningProcess.getProcessState().equals(States.RUNNING)) {
                        longRunningProcess.setProcessState(st);
                        processManager.updateLongRunningProcessState(longRunningProcess);
                    } else if (!st.equals(States.KILLED)) {
                        longRunningProcess.setProcessState(st);
                        processManager.updateLongRunningProcessState(longRunningProcess);
                    }
                }
            }
        },

        updateName {

            @Override
            public void doAction(ServletContext context, HttpServletRequest req, HttpServletResponse resp, DefinitionManager defManager, LRProcessManager processManager, UserManager userManager, Provider<User> userProvider, IsActionAllowed actionAllowed, LoggedUsersSingleton loggedUserSingleton) {
                Lock lock = processManager.getSynchronizingLock();
                lock.lock();
                try {
                    String uuid = req.getParameter("uuid");
                    String name = req.getParameter("name");
                    if (name != null) {
                        name = URLDecoder.decode(name, "UTF-8");
                        LRProcess longRunningProcess = processManager.getLongRunningProcess(uuid);
                        longRunningProcess.setProcessName(name);
                        processManager.updateLongRunningProcessName(longRunningProcess);
                    }
                } catch (UnsupportedEncodingException e) {
                    LOGGER.log(Level.SEVERE, e.getMessage(), e);
                } finally {
                    lock.unlock();
                }
            }
        },

        delete {
            @Override
            public void doAction(ServletContext context, HttpServletRequest req, HttpServletResponse resp, DefinitionManager defManager, LRProcessManager processManager, UserManager userManager, Provider<User> userProvider, IsActionAllowed actionAllowed, LoggedUsersSingleton loggedUserSingleton) {
                Lock lock = processManager.getSynchronizingLock();
                lock.lock();
                try {
                    String uuid = req.getParameter("uuid");
                    LRProcess longRunningProcess = processManager.getLongRunningProcess(uuid);
                    
                    if (longRunningProcess != null) {
                        if (longRunningProcess.getProcessState().equals(States.BATCH_FAILED) || 
                                longRunningProcess.getProcessState().equals(States.BATCH_FINISHED)) {
                            processManager.deleteBatchLongRunningProcess(longRunningProcess);
                        } else {
                            processManager.deleteLongRunningProcess(longRunningProcess);
                        }
                    }
                    
                } finally {
                    lock.unlock();
                }
            }
        };

//        static boolean isInProcessAdminRole(IsUserInRoleDecision userInRoleDecision) {
//            return userInRoleDecision.isUserInRole(KrameriusRoles.LRPROCESS_ADMIN.getRoleName());
//        }

        abstract void doAction(ServletContext context, HttpServletRequest req, HttpServletResponse resp, DefinitionManager defManager, LRProcessManager processManager, UserManager userManager, Provider<User> userProvider, IsActionAllowed actionAllowed, LoggedUsersSingleton loggedUserSingleton);
    }

    
    
    public static String lrServlet(HttpServletRequest request) {
        return ApplicationURL.urlOfPath(request, InternalConfiguration.get().getProperties().getProperty("servlets.mapping.lrcontrol"));
    }

    public static String[] getParams(HttpServletRequest req) throws RecognitionException, TokenStreamException {
        String parametersString = req.getParameter("params");
        if ((parametersString !=null) && (!parametersString.trim().equals("")))  {
            return parametersString.split(",");
        } else {
            parametersString = req.getParameter("nparams");
            if ((parametersString !=null) && (!parametersString.trim().equals("")))  {
                ParamsParser parser = new ParamsParser(new ParamsLexer(new StringReader(parametersString)));
                List paramsList = parser.params();
                String[] revals = new String[paramsList.size()];
                for (int i = 0,ll=paramsList.size(); i < ll; i++) {
                    Object prm = paramsList.get(i);
                    if (prm instanceof String) {
                        String sprm = (String) prm;
                        revals[i]= sprm;
                    } else {
                        List lprm = (List) prm;
                        revals[i] = ProcessUtils.nparams((String[]) lprm.toArray(new String[lprm.size()]));
                    }
                }
                
                return revals;
            }
            return new String[0];
        }
    }
}
