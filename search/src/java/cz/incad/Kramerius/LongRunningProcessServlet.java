package cz.incad.Kramerius;

import antlr.RecognitionException;
import antlr.TokenStreamException;
import com.google.inject.Inject;
import com.google.inject.Provider;
import cz.incad.Kramerius.backend.guice.GuiceServlet;
import cz.incad.kramerius.ObjectPidsPath;
import cz.incad.kramerius.intconfig.InternalConfiguration;
import cz.incad.kramerius.processes.*;
import cz.incad.kramerius.processes.template.InputTemplateFactory;
import cz.incad.kramerius.processes.template.OutputTemplateFactory;
import cz.incad.kramerius.processes.template.ProcessInputTemplate;
import cz.incad.kramerius.processes.template.ProcessOutputTemplate;
import cz.incad.kramerius.processes.utils.ProcessUtils;
import cz.incad.kramerius.security.*;
import cz.incad.kramerius.security.SecurityException;
import cz.incad.kramerius.security.utils.UserUtils;
import cz.incad.kramerius.users.LoggedUsersSingleton;
import cz.incad.kramerius.utils.ApplicationURL;
import cz.incad.kramerius.utils.IPAddressUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;
import cz.incad.kramerius.utils.database.JDBCQueryTemplate;
import cz.incad.kramerius.utils.params.ParamsLexer;
import cz.incad.kramerius.utils.params.ParamsParser;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.logging.Level;

/**
 * This is support for long running processes
 * 
 * @author pavels
 */
public class LongRunningProcessServlet extends GuiceServlet {

    private static final String AUTH_TOKEN_HEADER_KEY = "auth-token";
    private static final String TOKEN_ATTRIBUTE_KEY = "token";


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
    
    @Inject
    InputTemplateFactory iTemplateFactory;


    @Inject
    OutputTemplateFactory outputTemplateFactory;
    
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
        String appLibPath = getServletContext().getRealPath("/WEB-INF/lib");
        return appLibPath;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws  IOException {
        try {
            String action = req.getParameter("action");
            if (action == null)
                action = Actions.start.name();
            Actions selectedAction = Actions.valueOf(action);
            selectedAction.doAction(getServletContext(), req, resp, this.definitionManager, this.lrProcessManager, this.usersManager, this.userProvider, this.actionAllowed, this.loggedUsersSingleton, this.iTemplateFactory, this.outputTemplateFactory);
        } catch (SecurityException e) {
            LOGGER.log(Level.INFO, e.getMessage());
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
        }
    }
    

    public static LRProcess planNewProcess(HttpServletRequest request, ServletContext context, LRProcessDefinition definition,  String[] params, User user, String loggedUserKey, Properties paramsMapping) {
        String token = request.getParameter(TOKEN_ATTRIBUTE_KEY);
        //String authToken = request.getHeader(AUTH_TOKEN_HEADER_KEY);
        LRProcess newProcess = definition.createNewProcess(null, token);
        newProcess.setUser(user);
        newProcess.setLoggedUserKey(loggedUserKey);
        newProcess.setParameters(Arrays.asList(params));
        newProcess.planMe(paramsMapping, IPAddressUtils.getRemoteAddress(request, KConfiguration.getInstance().getConfiguration()));
        return newProcess;
    }

    public static LRProcess stopOldProcess(String defaultLibDir, String uuidOfProcess, DefinitionManager defManager, LRProcessManager lrProcessManager) {
        defManager.load();
        LRProcess lrProcess = lrProcessManager.getLongRunningProcess(uuidOfProcess);
        lrProcess.stopMe();
        lrProcessManager.updateLongRunningProcessFinishedDate(lrProcess);
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
        lrProcessManager.updateAuthTokenMapping(nprocess, loggedUserKey);
    }
    
    static enum Actions {

        /**
         * Plan new process
         */
        start {
            public void doAction(ServletContext context, HttpServletRequest req, HttpServletResponse resp, DefinitionManager defManager, LRProcessManager lrProcessManager, UserManager userManager, Provider<User> userProvider, IsActionAllowed rightsResolver, LoggedUsersSingleton loggedUserSingleton, InputTemplateFactory iTemplateFactory, OutputTemplateFactory oTemplateFactory) {
                try {
                    String def = req.getParameter("def");
                    //String def, DefinitionManager definitionManager,
                    defManager.load();
             
                    LRProcessDefinition definition = defManager.getLongRunningProcessDefinition(def);
             
                    String out = req.getParameter("out");
                    String[] params = getParams(req);
                    //TODO: Zjisteni predavane autentizace 
                    SecuredActions actionFromDef = securedAction(def, definition);

                    String grpToken = req.getParameter(TOKEN_ATTRIBUTE_KEY);
                    String authToken = req.getHeader(AUTH_TOKEN_HEADER_KEY);

                    
                    String loggedUserKey = findLoggedUserKey(req, lrProcessManager, grpToken, authToken,  userProvider);
                    User user = loggedUserSingleton.getUser(loggedUserKey);
                    if (user == null) {
                        // no user
                        throw new SecurityException(new SecurityException.SecurityExceptionInfo(SecuredActions.MANAGE_LR_PROCESS));
                    }
                    
                    boolean permited = permitStart(rightsResolver, actionFromDef, user);
                    if (permited) {
                        

                        if (definition == null) {
                            throw new RuntimeException("cannot find process definition '" + def + "'");
                        }
                        
                        if (definition.isInputTemplateDefined()) {
                            if ((out != null) && (out.equals("text"))) {
                                resp.setContentType("text/plain");
                                resp.getOutputStream().print("[" + def + "]" + States.NOT_RUNNING.name());
                            } else {
                                StringBuffer buffer = new StringBuffer();
                                buffer.append("<html><body>");
                                buffer.append("<ul>");
                                buffer.append("<li>").append(def);
                                buffer.append("<li>").append(States.NOT_RUNNING.name());
                                buffer.append("</ul>");
                                buffer.append("</body></html>");
                                resp.setContentType("text/html");
                                resp.getOutputStream().println(buffer.toString());
                            }
                        } else {
                            // plan process
                            LRProcess nprocess = planNewProcess(req, context, definition, params, user,loggedUserKey, /* no mapping */ new Properties() );
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
                        }
                        
                    } else {
                        throw new SecurityException(new SecurityException.SecurityExceptionInfo(SecuredActions.MANAGE_LR_PROCESS));
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
            public void doAction(ServletContext context, HttpServletRequest req, HttpServletResponse resp, DefinitionManager defManager, LRProcessManager lrProcessManager, UserManager userManager, Provider<User> userProvider, IsActionAllowed actionIsAllowed, LoggedUsersSingleton loggedUserSingleton, InputTemplateFactory iTemplateFactory, OutputTemplateFactory oTemplateFactory) {
                if (actionIsAllowed.isActionAllowed(SecuredActions.MANAGE_LR_PROCESS.getFormalName(), SpecialObjects.REPOSITORY.getPid(),null, ObjectPidsPath.REPOSITORY_PATH)) {
                    try {
                        String uuid = req.getParameter("uuid");
                        String realPath = context.getRealPath("WEB-INF/lib");
                        LRProcess oProcess = stopOldProcess(realPath, uuid, defManager, lrProcessManager);
                        
                        // update parent process
                        List<LRProcess> processes = lrProcessManager.getLongRunningProcessesByGroupToken(oProcess.getGroupToken());
                        if (processes.size() > 1) {
                            LOGGER.fine("calculating new master state");
                            List<States> childStates = new ArrayList<States>();
                            for (int i = 0, ll = processes.size(); i < ll; i++) {
                                childStates.add(processes.get(i).getProcessState());
                            }
                            processes.get(0).setBatchState(BatchStates.calculateBatchState(childStates));
                            LOGGER.fine("calculated state '"+processes.get(0)+"'");
                            lrProcessManager.updateLongRunninngProcessBatchState(processes.get(0));
                        }


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

        form_get {

            @Override
            void doAction(ServletContext context, HttpServletRequest req, HttpServletResponse resp, DefinitionManager defManager, LRProcessManager processManager, UserManager userManager, Provider<User> userProvider, IsActionAllowed actionAllowed, LoggedUsersSingleton loggedUserSingleton, InputTemplateFactory iTemplateFactory, OutputTemplateFactory oTemplateFactory) {
                String def = req.getParameter("def");
                LRProcessDefinition definition = defManager.getLongRunningProcessDefinition(def);
                
                SecuredActions actionFromDef = securedAction(def, definition);
                String grpToken = req.getParameter(TOKEN_ATTRIBUTE_KEY);
                String authToken = req.getHeader(AUTH_TOKEN_HEADER_KEY);
                
                String loggedUserKey = findLoggedUserKey(req, processManager, grpToken, authToken, userProvider);
                User user = loggedUserSingleton.getUser(loggedUserKey);
                if (user == null) {
                    // no user
                    throw new SecurityException(new SecurityException.SecurityExceptionInfo(SecuredActions.MANAGE_LR_PROCESS));
                }

                boolean permitted = permitStart(actionAllowed, actionFromDef, user);
                if (permitted) {
                    try {
                        
                        if (definition.isInputTemplateDefined()) {
                            resp.setContentType("text/html;charset=UTF-8");
                            String inputTemplateClz = definition.getInputTemplateClass();
                            ProcessInputTemplate template = iTemplateFactory.create(inputTemplateClz);
                            template.renderInput(definition,  resp.getWriter(), getParamsMapping(req));
                        }
                    } catch (IOException e) {
                        LOGGER.log(Level.SEVERE,e.getMessage());
                        resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    } catch (ClassNotFoundException e) {
                        LOGGER.log(Level.SEVERE,e.getMessage(),e);
                        resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    } catch (InstantiationException e) {
                        LOGGER.log(Level.SEVERE,e.getMessage(),e);
                        resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    } catch (IllegalAccessException e) {
                        LOGGER.log(Level.SEVERE,e.getMessage(),e);
                        resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    } catch (RecognitionException e) {
                        LOGGER.log(Level.SEVERE,e.getMessage(),e);
                        resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    } catch (TokenStreamException e) {
                        LOGGER.log(Level.SEVERE,e.getMessage(),e);
                        resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    }
                } else {
                    resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                }
            }

        },
        
        form_post {

            @Override
            void doAction(ServletContext context, HttpServletRequest req, HttpServletResponse resp, DefinitionManager defManager, LRProcessManager processManager, UserManager userManager, Provider<User> userProvider, IsActionAllowed actionAllowed, LoggedUsersSingleton loggedUserSingleton, InputTemplateFactory iTemplateFactory, OutputTemplateFactory oTemplateFactory) {

                String def = req.getParameter("def");
                //String def, DefinitionManager definitionManager,
                defManager.load();
                LRProcessDefinition definition = defManager.getLongRunningProcessDefinition(def);
                
                SecuredActions actionFromDef = securedAction(def, definition);
                String grpToken = req.getParameter(TOKEN_ATTRIBUTE_KEY);
                String authToken = req.getHeader(AUTH_TOKEN_HEADER_KEY);

                
                String loggedUserKey = findLoggedUserKey(req, processManager, grpToken, authToken,  userProvider);
                User user = loggedUserSingleton.getUser(loggedUserKey);
                if (user == null) {
                    // no user
                    throw new SecurityException(new SecurityException.SecurityExceptionInfo(SecuredActions.MANAGE_LR_PROCESS));
                }

                boolean permitted = permitStart(actionAllowed, actionFromDef, user);
                if (permitted) {

                    try {
                        String out = req.getParameter("out");
                        String[] params = getParams(req);
                        //TODO: Zjisteni predavane autentizace 


                        LRProcess nprocess = planNewProcess(req, context, definition, params, user,loggedUserKey, getParamsMapping(req));

                        // update process and token mapping
                        updateProcessTokenMapping(nprocess,  loggedUserKey,processManager);
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
                    
                        
                    } catch (RecognitionException e) {
                        LOGGER.log(Level.SEVERE,e.getMessage());
                        resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    } catch (TokenStreamException e) {
                        LOGGER.log(Level.SEVERE,e.getMessage());
                        resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    } catch (IOException e) {
                        LOGGER.log(Level.SEVERE,e.getMessage());
                        resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    }
                } else {
                    resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                }
            }

        },        
        
        outputTemplate {

            @Override
            void doAction(ServletContext context, HttpServletRequest req, HttpServletResponse resp, DefinitionManager defManager, LRProcessManager processManager, UserManager userManager, Provider<User> userProvider, IsActionAllowed actionAllowed, LoggedUsersSingleton loggedUserSingleton,
                    InputTemplateFactory iTemplateFactory, OutputTemplateFactory oTemplateFactory) {
                if (actionAllowed.isActionAllowed(SecuredActions.MANAGE_LR_PROCESS.getFormalName(), SpecialObjects.REPOSITORY.getPid(),null, ObjectPidsPath.REPOSITORY_PATH)) {
                    try {
                        String uuid = req.getParameter("uuid");
                        String templateId = req.getParameter("templateId");
                        
                        LRProcess longRunningProcess = processManager.getLongRunningProcess(uuid);
                        String definitionId = longRunningProcess.getDefinitionId();
                        LRProcessDefinition definition = defManager.getLongRunningProcessDefinition(definitionId);
                        ProcessOutputTemplate oTemplate = template(oTemplateFactory, templateId, definition);
                        
                        resp.setContentType("text/html;charset=UTF-8");
                        oTemplate.renderOutput(longRunningProcess, definition, resp.getWriter());
                        
                    } catch (ClassNotFoundException e) {
                        LOGGER.log(Level.SEVERE,e.getMessage(),e);
                    } catch (InstantiationException e) {
                        LOGGER.log(Level.SEVERE,e.getMessage(),e);
                    } catch (IllegalAccessException e) {
                        LOGGER.log(Level.SEVERE,e.getMessage(),e);
                    } catch (IOException e) {
                        LOGGER.log(Level.SEVERE,e.getMessage(),e);
                    }
                } else {
                    resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                }
            }

            public ProcessOutputTemplate template(OutputTemplateFactory oTemplateFactory, String templateId, LRProcessDefinition definition) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
                List<String> outputTemplateClasses = definition.getOutputTemplateClasses();
                for (String clz : outputTemplateClasses) {
                    ProcessOutputTemplate oT = oTemplateFactory.create(clz);
                    if (oT.getOutputTemplateId().equals(templateId)) {
                        return oT;
                    }
                }
                return null;
            }
            
        },
        
        updatePID {
            @Override
            public void doAction(ServletContext context, HttpServletRequest req, HttpServletResponse resp, DefinitionManager defManager, LRProcessManager lrProcessManager, UserManager userManager, Provider<User> userProvider, IsActionAllowed actionAllowed, LoggedUsersSingleton loggedUserSingleton, InputTemplateFactory iTemplateFactory, OutputTemplateFactory oTemplateFactory) {
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
            public void doAction(ServletContext context, HttpServletRequest req, HttpServletResponse resp, DefinitionManager defManager, LRProcessManager processManager, UserManager userManager, Provider<User> userProvider, IsActionAllowed actionAllowed, LoggedUsersSingleton loggedUserSingleton, InputTemplateFactory iTemplateFactory, OutputTemplateFactory oTemplateFactory) {
                String uuid = req.getParameter("uuid");
                String state = req.getParameter("state");
                Lock lock = processManager.getSynchronizingLock();
                lock.lock();
                try  {
                    LRProcess longRunningProcess = processManager.getLongRunningProcess(uuid);
                    
                    // zmena procesu
                    changeProcessState(processManager, state, longRunningProcess);

                    if (States.notRunningState(States.valueOf(state))) {
                        longRunningProcess.setFinishedTime(System.currentTimeMillis());
                        processManager.updateLongRunningProcessFinishedDate(longRunningProcess);
                    }
                    
                    
                    // nacteni z db ? 
                    List<LRProcess> processes = processManager.getLongRunningProcessesByGroupToken(longRunningProcess.getGroupToken());
                    if (processes.size() > 1) {
                        LOGGER.fine("calculating new master state");
                        List<States> childStates = new ArrayList<States>();
                        for (int i = 0, ll = processes.size(); i < ll; i++) {
                            childStates.add(processes.get(i).getProcessState());
                        }
                        processes.get(0).setBatchState(BatchStates.calculateBatchState(childStates));
                        LOGGER.fine("calculated state '"+processes.get(0)+"'");
                        processManager.updateLongRunninngProcessBatchState(processes.get(0));

                    }

                } finally {
                    lock.unlock();
                }
            }

            public void changeProcessState(LRProcessManager processManager, String state, LRProcess longRunningProcess) {
                if (state != null) {
                    States st = States.valueOf(state);
                    longRunningProcess.setProcessState(st);
                    processManager.updateLongRunningProcessState(longRunningProcess);
                }
            }
        },

        updateName {

            @Override
            public void doAction(ServletContext context, HttpServletRequest req, HttpServletResponse resp, DefinitionManager defManager, LRProcessManager processManager, UserManager userManager, Provider<User> userProvider, IsActionAllowed actionAllowed, LoggedUsersSingleton loggedUserSingleton, InputTemplateFactory iTemplateFactory, OutputTemplateFactory oTemplateFactory) {
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

        closeToken {

            @Override
            void doAction(ServletContext context, HttpServletRequest req, HttpServletResponse resp, DefinitionManager defManager, LRProcessManager processManager, UserManager userManager, Provider<User> userProvider, IsActionAllowed actionAllowed, LoggedUsersSingleton loggedUserSingleton,
                    InputTemplateFactory iTemplateFactory, OutputTemplateFactory oTemplateFactory) {
                String uuid = req.getParameter("uuid");
                if (uuid != null) {
                    LRProcess longRunningProcess = processManager.getLongRunningProcess(uuid);
                    processManager.closeAuthToken(longRunningProcess.getAuthToken());
                }
            }
        },
        
        delete {
            @Override
            public void doAction(ServletContext context, HttpServletRequest req, HttpServletResponse resp, DefinitionManager defManager, LRProcessManager processManager, UserManager userManager, Provider<User> userProvider, IsActionAllowed actionAllowed, LoggedUsersSingleton loggedUserSingleton, InputTemplateFactory iTemplateFactory, OutputTemplateFactory oTemplateFactory) {
                if (actionAllowed.isActionAllowed(SecuredActions.MANAGE_LR_PROCESS.getFormalName(), SpecialObjects.REPOSITORY.getPid(),null, ObjectPidsPath.REPOSITORY_PATH)) {
                    Lock lock = processManager.getSynchronizingLock();
                    lock.lock();
                    try {
                        String uuid = req.getParameter("uuid");
                        LRProcess longRunningProcess = processManager.getLongRunningProcess(uuid);
                        
                        if (longRunningProcess != null) {
                            if (BatchStates.expect(longRunningProcess.getBatchState(), BatchStates.BATCH_FAILED, BatchStates.BATCH_FINISHED)) {
                                processManager.deleteBatchLongRunningProcess(longRunningProcess);
                            } else {
                                processManager.deleteLongRunningProcess(longRunningProcess);
                                
                                // update state when delete process
                                List<LRProcess> processes = processManager.getLongRunningProcessesByGroupToken(longRunningProcess.getGroupToken());
                                if (!processes.isEmpty()) {
                                    List<States> sts = new ArrayList<States>();
                                    for (LRProcess lrProcess : processes) { sts.add(lrProcess.getProcessState()); }
                                    processes.get(0).setBatchState(BatchStates.calculateBatchState(sts));
                                    LOGGER.fine("calculated state '"+processes.get(0)+"'");
                                    processManager.updateLongRunninngProcessBatchState(processes.get(0));
                                }
                            }
                        }
                        
                    } finally {
                        lock.unlock();
                    }
                } else {
                    resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                }
            }
        };


        abstract void doAction(ServletContext context, HttpServletRequest req, HttpServletResponse resp, DefinitionManager defManager, LRProcessManager processManager, UserManager userManager, Provider<User> userProvider, IsActionAllowed actionAllowed, LoggedUsersSingleton loggedUserSingleton, InputTemplateFactory iTemplateFactory, OutputTemplateFactory oTemplateFactory);

        public String findLoggedUserKey(HttpServletRequest req, LRProcessManager lrProcessManager, String grpToken, String authToken,Provider<User> userProvider) {
            if (grpToken != null) {
                if (lrProcessManager.isAuthTokenClosed(authToken)) {
                    throw new SecurityException(new SecurityException.SecurityExceptionInfo(SecuredActions.MANAGE_LR_PROCESS));
                }
                List<LRProcess> processes = lrProcessManager.getLongRunningProcessesByGroupToken(grpToken);
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
                    throw new RuntimeException("cannot find process with token '"+grpToken+"'");
                }
            } else {
                userProvider.get();
                return (String) req.getSession().getAttribute(UserUtils.LOGGED_USER_KEY_PARAM);
            }
        }
        
        public boolean permitStart(IsActionAllowed rightsResolver, SecuredActions actionFromDef, User user) {
            //TODO: where are actions ?
            boolean permited = user!= null? (rightsResolver.isActionAllowed(user,SecuredActions.MANAGE_LR_PROCESS.getFormalName(), SpecialObjects.REPOSITORY.getPid(), null , ObjectPidsPath.REPOSITORY_PATH) || 
                                (actionFromDef != null && rightsResolver.isActionAllowed(user, actionFromDef.getFormalName(), SpecialObjects.REPOSITORY.getPid(),null, ObjectPidsPath.REPOSITORY_PATH))) : false ;
            return permited;
        }

        public SecuredActions securedAction(String def, LRProcessDefinition definition) {
            return definition.getSecuredAction() != null ? SecuredActions.findByFormalName(definition.getSecuredAction()) : SecuredActions.findByFormalName(def);
        }
    }

    
    
    public static String lrServlet(HttpServletRequest request) {
        return ApplicationURL.urlOfPath(request, InternalConfiguration.get().getProperties().getProperty("servlets.mapping.lrcontrol"));
    }


    public static Properties getParamsMapping(HttpServletRequest req) throws RecognitionException, TokenStreamException {
        Properties props = new Properties();
        String paramsMapping = req.getParameter("paramsMapping");
        if ((paramsMapping !=null) && (!paramsMapping.trim().equals("")))  {
            ParamsParser parser = new ParamsParser(new ParamsLexer(new StringReader(paramsMapping)));
            List paramsList = parser.params();
            for (Object paramPair : paramsList) {
                String[] splitted = paramPair.toString().split("=");
                if (splitted.length == 2) {
                    props.setProperty(splitted[0], splitted[1]);
                } else {
                    LOGGER.warning("skipping param mapping pair '"+paramPair+"'");
                }
            }
        }        
        return props;
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
