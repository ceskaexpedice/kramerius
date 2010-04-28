package cz.incad.Kramerius.views;

import java.text.SimpleDateFormat;
import java.util.Date;

import cz.incad.kramerius.processes.LRProcess;
import cz.incad.kramerius.processes.States;
import cz.incad.kramerius.utils.conf.KConfiguration;

public class ProcessViewObject {

	public static final SimpleDateFormat FORMAT = new SimpleDateFormat("yyyy/dd/MM - hh:mm:ss");
	
	private LRProcess lrProcess;

	public ProcessViewObject(LRProcess lrProcess) {
		super();
		this.lrProcess = lrProcess;
	}

	public String getPid() {
		return lrProcess.getPid();
	}

	public String getProcessName() {
		return lrProcess.getProcessName();
	}

	public String getProcessState() {
		return lrProcess.getProcessState().name();
	}

	public String getStart() {
		return FORMAT.format(new Date(lrProcess.getStart()));
	}
	
	public String getKillURL() {
		return KConfiguration.getKConfiguration().getLRServletURL()+"?action=stop&uuid="+this.lrProcess.getUUID();
	}
}
