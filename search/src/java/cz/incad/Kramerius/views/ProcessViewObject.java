package cz.incad.Kramerius.views;

import java.text.SimpleDateFormat;
import java.util.Date;

import cz.incad.kramerius.processes.LRProcess;
import cz.incad.kramerius.processes.LRProcessDefinition;
import cz.incad.kramerius.processes.LRProcessOffset;
import cz.incad.kramerius.processes.LRProcessOrdering;
import cz.incad.kramerius.processes.States;
import cz.incad.kramerius.processes.TypeOfOrdering;
import cz.incad.kramerius.utils.conf.KConfiguration;

public class ProcessViewObject {

	public static final SimpleDateFormat FORMAT = new SimpleDateFormat("yyyy/dd/MM - hh:mm:ss");
	
	private LRProcess lrProcess;
	private LRProcessDefinition definition;
	private LRProcessOrdering ordering;
	private LRProcessOffset offset;
	private TypeOfOrdering typeOfOrdering;

	
	public ProcessViewObject(LRProcess lrProcess, LRProcessDefinition definition, LRProcessOrdering ordering, LRProcessOffset offset, TypeOfOrdering typeOfOrdering) {
		super();
		this.lrProcess = lrProcess;
		this.ordering = ordering;
		this.offset = offset;
		this.typeOfOrdering = typeOfOrdering;
		this.definition = definition;
	}

	public String getPid() {
		return lrProcess.getPid();
	}

	public String getProcessName() {
		if (this.definition.getProcessOutputURL() != null) {
			return "<a href=\""+this.definition.getProcessOutputURL()+"&uuid="+this.lrProcess.getUUID()+"\">"+this.lrProcess.getProcessName()+"</a>";
		} else {
			return lrProcess.getProcessName();
		}
	}

	public String getProcessState() {
		return lrProcess.getProcessState().name();
	}

	public String getStart() {
		return FORMAT.format(new Date(lrProcess.getStart()));
	}
	
	//function killAndRefresh(url,ordering, offset, size, type) {

	public String getKillURL() {
		if (this.lrProcess.getProcessState().equals(States.RUNNING)) {
			String url = KConfiguration.getKConfiguration().getLRServletURL()+"?action=stop&uuid="+this.lrProcess.getUUID();
			return "<a href=\"javascript:killAndRefresh('"+url+"','"+this.ordering.name()+"',"+this.offset.getOffset()+","+this.offset.getSize()+",'"+this.typeOfOrdering.name()+"');\">Zastavit</a>";
		} else {
			return "";
		}
	}
}
