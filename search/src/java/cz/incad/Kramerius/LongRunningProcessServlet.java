package cz.incad.Kramerius;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.inject.Inject;
import com.google.inject.Injector;

import cz.incad.Kramerius.backend.guice.GuiceServlet;
import cz.incad.Kramerius.backend.guice.RequestIPaddressChecker;
import cz.incad.Kramerius.security.KrameriusRoles;
import cz.incad.Kramerius.views.ApplicationURL;
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
import cz.incad.kramerius.security.IPaddressChecker;
import cz.incad.kramerius.security.IsUserInRoleDecision;
import cz.incad.kramerius.utils.conf.KConfiguration;

/**
 * This is support for long running processes
 * @author pavels
 */
public class LongRunningProcessServlet extends GuiceServlet {

	private static final long serialVersionUID = 1L;

	public static final java.util.logging.Logger LOGGER = java.util.logging.Logger
			.getLogger(LongRunningProcessServlet.class.getName());
	
	@Inject
	transient DefinitionManager definitionManager;

	@Inject
	transient LRProcessManager lrProcessManager;

	@Inject
	transient KConfiguration configuration;

	@Inject
	transient ProcessScheduler processScheduler;
	
	@Inject
	transient GCScheduler gcScheduler;
	
	@Inject
	transient IsUserInRoleDecision userInRoleDecision;
	
	@Inject
	transient IPaddressChecker iPaddressChecker;
	
	@Override
	public void init() throws ServletException {
		super.init();
		String appLibPath = getServletContext().getRealPath("WEB-INF/lib");
		
		KConfiguration conf = KConfiguration.getInstance();
		if ((conf.getApplicationURL() == null) || (conf.getApplicationURL().equals(""))) {
			throw new RuntimeException("lr servlet need configuration parameter 'applicationUrl'");
		}
		String lrServlet = conf.getApplicationURL()+'/'+InternalConfiguration.get().getProperties().getProperty("servlets.mapping.lrcontrol");

		this.processScheduler.init(appLibPath, lrServlet);
		this.gcScheduler.init();
	}


	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		String action = req.getParameter("action");
		if (action == null) action = Actions.list.name();
		Actions selectedAction = Actions.valueOf(action);
		selectedAction.doAction(getServletContext(), req, resp, this.definitionManager, this.lrProcessManager, this.userInRoleDecision, this.iPaddressChecker);
	}

	public static LRProcess planNewProcess(HttpServletRequest request, ServletContext context, String def, DefinitionManager definitionManager, String[] params) {
		definitionManager.load();
		LRProcessDefinition definition = definitionManager.getLongRunningProcessDefinition(def);
		if (definition == null) {
			throw new RuntimeException("cannot find process definition '"+def+"'");
		}
		LRProcess newProcess = definition.createNewProcess();
		newProcess.setParameters(Arrays.asList(params));
		newProcess.planMe();
		return newProcess;
	}

	public static LRProcess stopOldProcess(String defaultLibDir, String uuidOfProcess, DefinitionManager defManager, LRProcessManager lrProcessManager) {
		defManager.load();
		lrProcessManager.getLongRunningProcess(uuidOfProcess).stopMe();
		return lrProcessManager.getLongRunningProcess(uuidOfProcess);
	}
	
	
	static enum Actions {
		
		/**
		 * Plan new process
		 */
		start {
			public void doAction(ServletContext context,HttpServletRequest req, HttpServletResponse resp, DefinitionManager defManager, LRProcessManager lrProcessManager, IsUserInRoleDecision userInRoleDecision, IPaddressChecker checker) {
				try {
					String def = req.getParameter("def");
					String out = req.getParameter("out");
					String parametersString = req.getParameter("params");
					String[] params = new String[0];
					if (parametersString !=null) {
						params = parametersString.split(",");
					}
					if (userInRoleDecision.isUserInRole(def) || checker.localHostVisitor()) {
						LRProcess nprocess = planNewProcess(req, context, def, defManager, params);
						if ((out != null) && (out.equals("text"))) {
							resp.getOutputStream().print("["+nprocess.getDefinitionId()+"]"+nprocess.getProcessState().name());
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
							resp.getOutputStream().println(buffer.toString());
						}
					} else {
						resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
					}
				} catch (IOException e) {
					LOGGER.log(Level.SEVERE, e.getMessage(), e);
				}
			}
		},
		
		/**
		 * Stop running process
		 */
		stop {
			@Override
			public void doAction(ServletContext context,HttpServletRequest req,HttpServletResponse resp, DefinitionManager defManager, LRProcessManager lrProcessManager, IsUserInRoleDecision userInRoleDecision, IPaddressChecker checker) {
				if (isInProcessAdminRole(userInRoleDecision) || checker.localHostVisitor()) {
					try {
						String uuid = req.getParameter("uuid");
						String realPath =context.getRealPath("WEB-INF/lib");
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
						resp.getOutputStream().println(buffer.toString());
					} catch (IOException e) {
						LOGGER.log(Level.SEVERE, e.getMessage(), e);
					}
				} else {
					resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
				}
			}

		},

		/**
		 * list all processes
		 */
		list {
			@Override
			public void doAction(ServletContext context,HttpServletRequest req,HttpServletResponse resp, DefinitionManager defManager, LRProcessManager lrProcessManager, IsUserInRoleDecision userInRoleDecision, IPaddressChecker checker) {
				if (isInProcessAdminRole(userInRoleDecision) || checker.localHostVisitor()) {
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
								buffer.append("  ... <a href='"+lrServlet(req)+"?action=stop&uuid="+lrProcess.getUUID()+"'>stop</a>");
							}
							buffer.append("<li>").append("uuid :").append(lrProcess.getUUID());
							buffer.append("<li>").append("name :").append(lrProcess.getProcessName());
							buffer.append("<li>").append("started :"+new Date(lrProcess.getStartTime()));
							buffer.append("<li>").append("processState :").append(lrProcess.getProcessState());
							LRProcessDefinition lrDef = defManager.getLongRunningProcessDefinition(lrProcess.getDefinitionId());
							if (lrDef == null) {
								throw new RuntimeException("cannot find definition '"+lrProcess.getDefinitionId()+"'");
							}
							buffer.append("<li>").append("errOut  :").append(lrDef.getErrStreamFolder()+File.separator+lrProcess.getUUID()+".err");
							buffer.append("<li>").append("standardOut  :").append(lrDef.getStandardStreamFolder()+File.separator+lrProcess.getUUID()+".out");
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
		},

		updatePID {
			@Override
			public void doAction(ServletContext context,HttpServletRequest req,HttpServletResponse resp, DefinitionManager defManager, LRProcessManager lrProcessManager, IsUserInRoleDecision userInRoleDecision, IPaddressChecker checker) {
				String uuid = req.getParameter("uuid");
				String pid = req.getParameter("pid");
				LRProcess longRunningProcess = lrProcessManager.getLongRunningProcess(uuid);
				longRunningProcess.setPid(pid);
				lrProcessManager.updateLongRunningProcessPID(longRunningProcess);
			}
		},
		
		updateStatus {

			@Override
			public void doAction(ServletContext context,HttpServletRequest req,HttpServletResponse resp, DefinitionManager defManager, LRProcessManager processManager, IsUserInRoleDecision userInRoleDecision, IPaddressChecker checker) {
				String uuid = req.getParameter("uuid");
				String state = req.getParameter("state");
				if (state != null) {
					States st = States.valueOf(state);
					LRProcess longRunningProcess = processManager.getLongRunningProcess(uuid);
					longRunningProcess.setProcessState(st);
					processManager.updateLongRunningProcessState(longRunningProcess);
				}
			}
		},

		updateName {

			@Override
			public void doAction(ServletContext context,HttpServletRequest req,
					HttpServletResponse resp, DefinitionManager defManager,
					LRProcessManager processManager, IsUserInRoleDecision userInRoleDecision, IPaddressChecker checker) {
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
				}
			}
		};

		static boolean isInProcessAdminRole(IsUserInRoleDecision userInRoleDecision) {
			return userInRoleDecision.isUserInRole(KrameriusRoles.LRPROCESS_ADMIN.getRoleName());
		}

		abstract void doAction(ServletContext context,  HttpServletRequest req, HttpServletResponse resp, DefinitionManager defManager, LRProcessManager processManager, IsUserInRoleDecision userInRoleDecision, IPaddressChecker iPaddressChecker);
	}
	

	public static String lrServlet(HttpServletRequest request) {
		return ApplicationURL.urlOfPath(request, InternalConfiguration.get().getProperties().getProperty("servlets.mapping.lrcontrol"));
	}
	
}
