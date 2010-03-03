package cz.incad.Kramerius;

import java.io.IOException;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.inject.Inject;

import cz.incad.Kramerius.backend.guice.GuiceServlet;
import cz.incad.kramerius.processes.DefinitionManager;
import cz.incad.kramerius.processes.LRProcess;
import cz.incad.kramerius.processes.LRProcessDefinition;
import cz.incad.kramerius.processes.LRProcessManager;

public class LongRunningProcessServlet extends GuiceServlet {

	@Inject
	DefinitionManager definitionManager;

	@Inject
	LRProcessManager lrProcessManager;
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		
		String action = req.getParameter("action");
		if ("start".equals(action)) {
			String def = req.getParameter("def");
			LRProcess nprocess = startNewProcess(def);
			StringBuffer buffer = new StringBuffer();
			buffer.append("<html><body>");
			buffer.append("<ul>");
			buffer.append("<li>").append(nprocess.getDefinitionId());
			buffer.append("<li>").append(nprocess.getUUID());
			buffer.append("<li>").append(nprocess.getPid());
			buffer.append("<li>").append(new Date(nprocess.getStart()));
			buffer.append("<li>").append(nprocess.getProcessState());
			buffer.append("</ul>");
			buffer.append("</body></html>");
			resp.getOutputStream().println(buffer.toString());
		} else if ("stop".equals(action)) {
			String uuid = req.getParameter("uuid");
			LRProcess oProcess = stopOldProcess(uuid);
			StringBuffer buffer = new StringBuffer();
			buffer.append("<html><body>");
			buffer.append("<ul>");
			buffer.append("<li>").append(oProcess.getDefinitionId());
			buffer.append("<li>").append(oProcess.getUUID());
			buffer.append("<li>").append(oProcess.getPid());
			buffer.append("<li>").append(new Date(oProcess.getStart()));
			buffer.append("<li>").append(oProcess.getProcessState());
			buffer.append("</ul>");
			buffer.append("</body></html>");
			resp.getOutputStream().println(buffer.toString());
		}
	}
	
	public LRProcess startNewProcess(String def) {
		this.definitionManager.load();
		LRProcessDefinition definition = this.definitionManager.getLongRunningProcessDefinition(def);
		LRProcess newProcess = definition.createNewProcess();
		newProcess.startMe(false);
		return newProcess;
	}

	public LRProcess stopOldProcess(String uuidOfProcess) {
		this.definitionManager.load();
		this.lrProcessManager.getLongRunningProcess(uuidOfProcess).stopMe();
		return this.lrProcessManager.getLongRunningProcess(uuidOfProcess);
	}
}
