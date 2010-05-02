package cz.incad.Kramerius.views;

import java.util.ArrayList;
import java.util.List;

import cz.incad.kramerius.processes.DefinitionManager;
import cz.incad.kramerius.processes.LRProcess;
import cz.incad.kramerius.processes.LRProcessDefinition;
import cz.incad.kramerius.processes.LRProcessManager;
import cz.incad.kramerius.processes.LRProcessOffset;
import cz.incad.kramerius.processes.LRProcessOrdering;
import cz.incad.kramerius.processes.TypeOfOrdering;

public class ProcessesViewObject {

	public static final java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(ProcessesViewObject.class.getName());
	
	private LRProcessManager processManager;
	private DefinitionManager definitionManager;
	private LRProcessOrdering ordering;
	private LRProcessOffset offset;
	private TypeOfOrdering typeOfOrdering;
	
	public ProcessesViewObject(LRProcessManager processManager, DefinitionManager manager, LRProcessOrdering ordering, TypeOfOrdering typeOfOrdering, LRProcessOffset offset) {
		super();
		this.processManager = processManager;
		this.ordering = ordering;
		this.offset = offset;
		this.typeOfOrdering = typeOfOrdering;
		this.definitionManager = manager;
	}

	public List<ProcessViewObject> getProcesses() {
		List<LRProcess> lrProcesses = this.processManager.getLongRunningProcesses(this.ordering, this.typeOfOrdering, this.offset);
		List<ProcessViewObject> objects = new ArrayList<ProcessViewObject>();
		for (LRProcess lrProcess : lrProcesses) {
			LRProcessDefinition def = this.definitionManager.getLongRunningProcessDefinition(lrProcess.getDefinitionId());
			objects.add(new ProcessViewObject(lrProcess, def, this.ordering, this.offset, this.typeOfOrdering));
		}
		return objects;
	}
	
	public String getNextAHREF() {
		int count = this.processManager.getNumberOfLongRunningProcesses();
		int offset = Integer.parseInt(this.offset.getOffset());
		int size = Integer.parseInt(this.offset.getSize());
		if ((offset+size) < count ) {
			return "<a href=\"javascript:modifyProcessDialogData('"+this.ordering+"','"+this.offset.getNextOffset()+"','"+this.offset.getSize()+"','"+this.typeOfOrdering.getTypeOfOrdering()+"');\"><img src=\"img/process_right.png\"/></a>";
		} else {
			return "<img src=\"img/process_right_disabled.png\"/>";
		}
	}
	
	public String getPrevAHREF() {
		int offset = Integer.parseInt(this.offset.getOffset());
		if (offset > 0) {
			return "<a href=\"javascript:modifyProcessDialogData('"+this.ordering+"','"+this.offset.getPrevOffset()+"','"+this.offset.getSize()+"','"+this.typeOfOrdering.getTypeOfOrdering()+"');\"><img src=\"img/process_left.png\"/></a>";
		} else {
			return "<img src=\"img/process_left_disabled.png\"/>";
		}
	}

	private TypeOfOrdering switchOrdering() {
		return this.typeOfOrdering.equals(TypeOfOrdering.ASC) ? TypeOfOrdering.DESC : TypeOfOrdering.ASC;
	}

	public String getNameOrdering() {
		LRProcessOrdering nOrdering = LRProcessOrdering.NAME;
		boolean changeTypeOfOrdering = this.ordering.equals(nOrdering);
		return newOrderingURL(nOrdering, "Název procesu", changeTypeOfOrdering ? switchOrdering() : TypeOfOrdering.ASC);
	}


	public String getDateOrdering() {
		LRProcessOrdering nOrdering = LRProcessOrdering.STARTED;
		boolean changeTypeOfOrdering = this.ordering.equals(nOrdering);
		return newOrderingURL(nOrdering,"Spusteno",changeTypeOfOrdering ? switchOrdering() : TypeOfOrdering.ASC);
	}

	public String getPidOrdering() {
		LRProcessOrdering nOrdering = LRProcessOrdering.ID;
		boolean changeTypeOfOrdering = this.ordering.equals(nOrdering);
		return newOrderingURL(nOrdering,"PID",changeTypeOfOrdering ? switchOrdering() : TypeOfOrdering.ASC);
	}

	public String getStateOrdering() {
		LRProcessOrdering nOrdering = LRProcessOrdering.STATE;
		boolean changeTypeOfOrdering = this.ordering.equals(nOrdering);
		return newOrderingURL(nOrdering,"Stav",changeTypeOfOrdering ? switchOrdering() : TypeOfOrdering.ASC);
	}
	
	private String newOrderingURL(LRProcessOrdering nOrdering, String name, TypeOfOrdering ntypeOfOrdering) {
		String href = "<a href=\"javascript:modifyProcessDialogData('"+nOrdering+"','"+this.offset.getOffset()+"','"+this.offset.getSize()+"','"+ntypeOfOrdering.getTypeOfOrdering()+"');\">"+name+"</a>";
		if (this.ordering.equals(nOrdering)) {
			href += orderingImg(nOrdering);
		}
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
