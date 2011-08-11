package cz.incad.Kramerius.views;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;

import cz.incad.kramerius.processes.DefinitionManager;
import cz.incad.kramerius.processes.LRProcess;
import cz.incad.kramerius.processes.LRProcessDefinition;
import cz.incad.kramerius.processes.LRProcessManager;
import cz.incad.kramerius.processes.LRProcessOffset;
import cz.incad.kramerius.processes.LRProcessOrdering;
import cz.incad.kramerius.processes.TypeOfOrdering;
import cz.incad.kramerius.service.ResourceBundleService;

public class ProcessesViewObject {

	public static final java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(ProcessesViewObject.class.getName());
	
	private LRProcessManager processManager;
	private DefinitionManager definitionManager;
	private LRProcessOrdering ordering;
	private LRProcessOffset offset;
	private TypeOfOrdering typeOfOrdering;

	private String lrUrl;
	private ResourceBundleService bundleService;
	private Locale locale;
	
	public ProcessesViewObject(LRProcessManager processManager, DefinitionManager manager, LRProcessOrdering ordering, TypeOfOrdering typeOfOrdering, LRProcessOffset offset, String lrUrl, ResourceBundleService bundleService, Locale locale) {
		super();
		this.processManager = processManager;
		this.ordering = ordering;
		this.offset = offset;
		this.typeOfOrdering = typeOfOrdering;
		this.definitionManager = manager;
		this.lrUrl = lrUrl;
		this.bundleService = bundleService;
		this.locale = locale;
	}

	public List<ProcessViewObject> getProcesses() {
		List<LRProcess> lrProcesses = this.processManager.getLongRunningProcesses(this.ordering, this.typeOfOrdering, this.offset);
		List<ProcessViewObject> objects = new ArrayList<ProcessViewObject>();
		for (LRProcess lrProcess : lrProcesses) {
			LRProcessDefinition def = this.definitionManager.getLongRunningProcessDefinition(lrProcess.getDefinitionId());
			objects.add(new ProcessViewObject(lrProcess, def, this.ordering, this.offset, this.typeOfOrdering, this.bundleService, this.locale));
		}
		return objects;
	}
        
        public boolean getHasNext(){
            int count = this.processManager.getNumberOfLongRunningProcesses();
            int oset = Integer.parseInt(this.offset.getOffset());
            int size = Integer.parseInt(this.offset.getSize());
            return (oset+size) < count ;
        }
        
        public int getOffsetValue(){
            return Integer.parseInt(this.offset.getOffset());
        }
        
        public int getPageSize(){
            return Integer.parseInt(this.offset.getSize());
        }
        
        public String getOrdering(){
            return this.ordering.toString();
        }
        
        public String getTypeOfOrdering(){
            return this.typeOfOrdering.getTypeOfOrdering();
        }
	
	public String getNextAHREF() {
		try {
			String nextString = bundleService.getResourceBundle("labels", locale).getString("administrator.processes.next");
			int count = this.processManager.getNumberOfLongRunningProcesses();
			int offset = Integer.parseInt(this.offset.getOffset());
			int size = Integer.parseInt(this.offset.getSize());
			if ((offset+size) < count ) {
				return "<a href=\"javascript:modifyProcessDialogData('"+this.ordering+"','"+this.offset.getNextOffset()+"','"+this.offset.getSize()+"','"+this.typeOfOrdering.getTypeOfOrdering()+"');\"> "+nextString+" <img  border=\"0\" src=\"img/next_arr.png\"/> </a>";
			} else {
				return "<span>" + nextString+"</span> <img border=\"0\" src=\"img/next_arr.png\" alt=\"next\" />";
			}
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
			return "<img border=\"0\" src=\"img/next_arr.png\" alt=\"next\" />";
		}
	}
	
	public String getPrevAHREF() {
			try {
				String prevString = bundleService.getResourceBundle("labels", locale).getString("administrator.processes.prev");
				int offset = Integer.parseInt(this.offset.getOffset());
				if (offset > 0) {
					return "<a href=\"javascript:modifyProcessDialogData('"+this.ordering+"','"+this.offset.getPrevOffset()+"','"+this.offset.getSize()+"','"+this.typeOfOrdering.getTypeOfOrdering()+"');\"> <img border=\"0\" src=\"img/prev_arr.png\"/> "+prevString+" </a>";
				} else {
					return "<img border=\"0\" src=\"img/prev_arr.png\" alt=\"prev\" /> <span>"+prevString+"</span>";
				}
			} catch (IOException e) {
				LOGGER.log(Level.SEVERE, e.getMessage(), e);
				return "<img border=\"0\" src=\"img/prev_arr.png\" alt=\"prev\" /> ";
			}
	}

	private TypeOfOrdering switchOrdering() {
		return this.typeOfOrdering.equals(TypeOfOrdering.ASC) ? TypeOfOrdering.DESC : TypeOfOrdering.ASC;
	}

	public String getNameOrdering() {
		try {
			String nameString = bundleService.getResourceBundle("labels", locale).getString("administrator.processes.name");
			LRProcessOrdering nOrdering = LRProcessOrdering.NAME;
			boolean changeTypeOfOrdering = this.ordering.equals(nOrdering);
			return newOrderingURL(nOrdering, nameString, changeTypeOfOrdering ? switchOrdering() : TypeOfOrdering.ASC);
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
			return e.getMessage();
		}
	}


	public String getDateOrdering() {
		try {
			String startedString = bundleService.getResourceBundle("labels", locale).getString("administrator.processes.started");
			LRProcessOrdering nOrdering = LRProcessOrdering.STARTED;
			boolean changeTypeOfOrdering = this.ordering.equals(nOrdering);
			return newOrderingURL(nOrdering,startedString,changeTypeOfOrdering ? switchOrdering() : TypeOfOrdering.ASC);
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
			return e.getMessage();
		}
	}

	   public String getPlannedDateOrdering() {
	        try {
	            String startedString = bundleService.getResourceBundle("labels", locale).getString("administrator.processes.planned");
	            LRProcessOrdering nOrdering = LRProcessOrdering.PLANNED;
	            boolean changeTypeOfOrdering = this.ordering.equals(nOrdering);
	            return newOrderingURL(nOrdering,startedString,changeTypeOfOrdering ? switchOrdering() : TypeOfOrdering.ASC);
	        } catch (IOException e) {
	            LOGGER.log(Level.SEVERE, e.getMessage(), e);
	            return e.getMessage();
	        }
	    }

	public String getUserOrdering() {
        try {
            String pidString = bundleService.getResourceBundle("labels", locale).getString("administrator.processes.user");
            LRProcessOrdering nOrdering = LRProcessOrdering.LOGINNAME;
            boolean changeTypeOfOrdering = this.ordering.equals(nOrdering);
            return newOrderingURL(nOrdering,pidString,changeTypeOfOrdering ? switchOrdering() : TypeOfOrdering.ASC);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            return e.getMessage();
        }
	}
	   
	public String getPidOrdering() {
		try {
			String pidString = bundleService.getResourceBundle("labels", locale).getString("administrator.processes.pid");
			LRProcessOrdering nOrdering = LRProcessOrdering.ID;
			boolean changeTypeOfOrdering = this.ordering.equals(nOrdering);
			return newOrderingURL(nOrdering,pidString,changeTypeOfOrdering ? switchOrdering() : TypeOfOrdering.ASC);
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
			return e.getMessage();
		}
	}

	public String getStateOrdering() {
		try {
			String stateString = bundleService.getResourceBundle("labels", locale).getString("administrator.processes.state");
			LRProcessOrdering nOrdering = LRProcessOrdering.STATE;
			boolean changeTypeOfOrdering = this.ordering.equals(nOrdering);
			return newOrderingURL(nOrdering,stateString,changeTypeOfOrdering ? switchOrdering() : TypeOfOrdering.ASC);
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
			return e.getMessage();
		}
	}
	
	private String newOrderingURL(LRProcessOrdering nOrdering, String name, TypeOfOrdering ntypeOfOrdering) {
		String href = "<a href=\"javascript:modifyProcessDialogData('"+
                        nOrdering+"','"+
                        this.offset.getOffset()+"','"+
                        this.offset.getSize()+"','"+
                        ntypeOfOrdering.getTypeOfOrdering()+"');\"";
		if (this.ordering.equals(nOrdering)) {
			//href += orderingImg(nOrdering);
                    if (typeOfOrdering.equals(TypeOfOrdering.DESC)) {
			href += " class=\"order_down\"";
                    } else {
                        href += " class=\"order_up\"";
                    }
                    
		}
                href += ">"+ name+"</a>";
		return href;
	}
	
	private String orderingImg(LRProcessOrdering nOrdering) {
		if (nOrdering.equals(this.ordering)) {
			if (typeOfOrdering.equals(TypeOfOrdering.DESC)) {
				return "<img src=\"img/order_down.png\"></img>";
			} else {
				return "<img src=\"img/order_up.png\"></img>";
			}
		} else return "";
	}
}
