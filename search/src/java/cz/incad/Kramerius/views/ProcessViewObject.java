package cz.incad.Kramerius.views;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.logging.Level;

import com.google.inject.Provider;

import cz.incad.kramerius.processes.LRProcess;
import cz.incad.kramerius.processes.LRProcessDefinition;
import cz.incad.kramerius.processes.LRProcessOffset;
import cz.incad.kramerius.processes.LRProcessOrdering;
import cz.incad.kramerius.processes.States;
import cz.incad.kramerius.processes.TypeOfOrdering;
import cz.incad.kramerius.service.ResourceBundleService;
import cz.incad.kramerius.utils.conf.KConfiguration;

public class ProcessViewObject {

	public static final java.util.logging.Logger LOGGER = java.util.logging.Logger
			.getLogger(ProcessViewObject.class.getName());
	
	public static final SimpleDateFormat FORMAT = new SimpleDateFormat("yyyy/dd/MM - hh:mm:ss");
	
	private LRProcess lrProcess;
	private LRProcessDefinition definition;
	private LRProcessOrdering ordering;
	private LRProcessOffset offset;
	private TypeOfOrdering typeOfOrdering;
	private String lrUrl;
	private ResourceBundleService bundleService;
	private Locale locale;
	
	public ProcessViewObject(LRProcess lrProcess, LRProcessDefinition definition, LRProcessOrdering ordering, LRProcessOffset offset, TypeOfOrdering typeOfOrdering, String lrUrl, ResourceBundleService service, Locale locale) {
		super();
		this.lrProcess = lrProcess;
		this.ordering = ordering;
		this.offset = offset;
		this.typeOfOrdering = typeOfOrdering;
		this.definition = definition;
		this.lrUrl = lrUrl;
		this.bundleService = service;
		this.locale = locale;
	}

	public String getPid() {
		return lrProcess.getPid();
	}

	public String getName() {
		if (lrProcess.getProcessName() == null) {
			return lrProcess.getDescription();
		} else {
			return lrProcess.getProcessName();
		}
	}
	
	public String getProcessName() {
		if (this.definition.getProcessOutputURL() != null) {
			return "<a href=\""+this.definition.getProcessOutputURL()+"?uuid="+this.lrProcess.getUUID()+"\" target=\"_blank\">"+getName()+"</a>";
		} else {
			return "<a href=\"dialogs/_processes_logs.jsp?uuid="+this.lrProcess.getUUID()+"\" target=\"_blank\">"+getName()+"</a>";
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
		try {
			if (this.lrProcess.getProcessState().equals(States.RUNNING)) {
				String url = lrUrl+"?action=stop&uuid="+this.lrProcess.getUUID();
				return "<a href=\"javascript:killAndRefresh('"+url+"','"+this.ordering.name()+"',"+this.offset.getOffset()+","+this.offset.getSize()+",'"+this.typeOfOrdering.name()+"');\">"+bundleService.getResourceBundle("labels", locale).getString("administrator.processes.kill.process")+"</a>";
			} else {
				return "";
			}
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
			return "";
		}
	}
}
