package cz.incad.Kramerius.views;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;

import com.google.inject.Provider;

import cz.incad.kramerius.processes.LRDefinitionAction;
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
	
	public static final SimpleDateFormat FORMAT = new SimpleDateFormat("yyyy/dd/MM - HH:mm:ss");
	
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
		try {
			String unnamed = bundleService.getResourceBundle("labels", locale).getString("administrator.processes.unnamedprocess");
			if (lrProcess.getProcessName() == null) {
				return unnamed+" <br> <span style='font-size:80%; font-style:italic'>"+lrProcess.getDescription()+"</span>";
			} else {
			    return lrProcess.getProcessName()+" <br> <span style='font-size:80%; font-style:italic'>"+lrProcess.getDescription()+"</span>";
			}
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
			return "";
		}
	}
	
	public String getProcessName() {
		return getName();
//		if (this.definition.getProcessOutputURL() != null) {
//			return "<a href=\""+this.definition.getProcessOutputURL()+"?uuid="+this.lrProcess.getUUID()+"\" target=\"_blank\">"+getName()+"</a>";
//		} else {
//			return "<a href=\"dialogs/_processes_logs.jsp?uuid="+this.lrProcess.getUUID()+"\" target=\"_blank\">"+getName()+"</a>";
//		}
	}

	public String getProcessState() {
		return lrProcess.getProcessState().getVal()+" "+lrProcess.getProcessState().name();
	}

	public String getStart() {
		Date date = new Date(lrProcess.getStartTime());
        return FORMAT.format(date);
	}

	public String getPlanned() {
        Date date = new Date(lrProcess.getPlannedTime());
        return FORMAT.format(date);
	}
	
	//function killAndRefresh(url,ordering, offset, size, type) {

	public String getKillURL() {
		try {
			if ((this.lrProcess.getProcessState().equals(States.RUNNING)) || 
			(this.lrProcess.getProcessState().equals(States.PLANNED)))  {
				String url = lrUrl+"?action=stop&uuid="+this.lrProcess.getUUID();
				String renderedAHREF = "<a href=\"javascript:killAndRefresh('"+url+"','"+this.ordering.name()+"',"+this.offset.getOffset()+","+this.offset.getSize()+",'"+this.typeOfOrdering.name()+"');\">"+bundleService.getResourceBundle("labels", locale).getString("administrator.processes.kill.process")+"</a>";
				if (!this.definition.getActions().isEmpty()) {
					renderedAHREF += " || ";
				}
				return renderedAHREF;
			} else {
				return "";
			}
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
			return "";
		}
	}

	public String getActionsURLs() {
		StringBuilder builder =  new StringBuilder();
		List<LRDefinitionAction> actions = this.definition.getActions();
		for (int i = 0,ll=actions.size(); i < ll; i++) {
			LRDefinitionAction action = actions.get(i);
			builder.append(getActionAHREF(action));
			if (i != ll-1) {
				builder.append(" || ");
			}
		}
		return builder.toString();
	}
	
	private String getActionAHREF(LRDefinitionAction act) {
		try {
			String bundleKey = act.getResourceBundleKey();
			if (bundleKey != null) {
				return "<a href=\""+act.getActionURL()+"?uuid="+this.lrProcess.getUUID()+"\" target=\"_blank\">"+ bundleService.getResourceBundle("labels", locale).getString(bundleKey) +"</a>";
			} else {
				LOGGER.info(" action '"+act.getName()+"' has no bundle key");
				return "<a href=\""+act.getActionURL()+"?uuid="+this.lrProcess.getUUID()+"\" target=\"_blank\">"+ act.getName() +"</a>";
			}
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
			return "";
		}
	}
}
