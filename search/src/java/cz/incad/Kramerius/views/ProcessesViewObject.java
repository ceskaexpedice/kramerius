package cz.incad.Kramerius.views;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;

import antlr.RecognitionException;
import antlr.TokenStreamException;

import cz.incad.Kramerius.processes.ParamsLexer;
import cz.incad.Kramerius.processes.ParamsParser;
import cz.incad.kramerius.processes.DefinitionManager;
import cz.incad.kramerius.processes.LRPRocessFilter;
import cz.incad.kramerius.processes.LRProcess;
import cz.incad.kramerius.processes.LRProcessDefinition;
import cz.incad.kramerius.processes.LRProcessManager;
import cz.incad.kramerius.processes.LRProcessOffset;
import cz.incad.kramerius.processes.LRProcessOrdering;
import cz.incad.kramerius.processes.States;
import cz.incad.kramerius.processes.TypeOfOrdering;
import cz.incad.kramerius.processes.LRPRocessFilter.Tripple;
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

    private LRPRocessFilter filter;

    private String filterParam;

    public ProcessesViewObject(LRProcessManager processManager, DefinitionManager manager, LRProcessOrdering ordering, TypeOfOrdering typeOfOrdering, LRProcessOffset offset, String lrUrl, ResourceBundleService bundleService, Locale locale, String filterParam) throws RecognitionException {
        super();
        this.processManager = processManager;
        this.ordering = ordering;
        this.offset = offset;
        this.typeOfOrdering = typeOfOrdering;
        this.definitionManager = manager;
        this.lrUrl = lrUrl;
        this.bundleService = bundleService;
        this.locale = locale;
        this.filterParam = filterParam;

        this.filter = this.createProcessFilter();
    }

    public LRPRocessFilter getFilter() {
        return filter;
    }
    
    public List<ProcessViewObject> getProcesses() {
        List<LRProcess> lrProcesses = this.processManager.getLongRunningProcessesAsGrouped(this.ordering, this.typeOfOrdering, this.offset, this.filter);
        List<ProcessViewObject> objects = new ArrayList<ProcessViewObject>();
        for (LRProcess lrProcess : lrProcesses) {
            LRProcessDefinition def = this.definitionManager.getLongRunningProcessDefinition(lrProcess.getDefinitionId());
            ProcessViewObject pw = new ProcessViewObject(lrProcess, def, this.ordering, this.offset, this.typeOfOrdering, this.bundleService, this.locale);
            if (lrProcess.isMasterProcess()) {
                List<LRProcess> childSubprecesses = this.processManager.getLongRunningProcessesByToken(lrProcess.getToken());
                for (LRProcess child : childSubprecesses) {
                    if (!child.getUUID().equals(lrProcess.getUUID())) {
                        LRProcessDefinition childDef = this.definitionManager.getLongRunningProcessDefinition(child.getDefinitionId());
                        ProcessViewObject childPW = new ProcessViewObject(child, childDef, this.ordering, this.offset, this.typeOfOrdering, this.bundleService, this.locale);
                        pw.addChildProcess(childPW);
                    }
                }
            }
            objects.add(pw);
        }
        return objects;
    }

    private LRPRocessFilter createProcessFilter() throws RecognitionException {
        if (this.filterParam == null)
            return null;
        try {
            ParamsParser paramsParser = new ParamsParser(new ParamsLexer(new StringReader(this.filterParam)));
            List params = paramsParser.params();
            List<Tripple> tripples = new ArrayList<LRPRocessFilter.Tripple>();
            for (Object object : params) {
                List trippleList = (List) object;
                Tripple tripple = createTripple(trippleList);
                if (tripple.getVal() != null) {
                    tripples.add(tripple);
                }
            }
            LRPRocessFilter filter = LRPRocessFilter.createFilter(tripples);
            // TODO: do it better
            Tripple statusTripple = filter.findTripple("status");
            if (((Integer) statusTripple.getVal()) == -1) {
                filter.removeTripple(statusTripple);
            }

            return filter;
        } catch (TokenStreamException te) {
            te.printStackTrace();
            return null;
        }
    }

    private Tripple createTripple(List trpList) {
        if (trpList.size() == 3) {
            String name = (String) trpList.get(0);
            String op = (String) trpList.get(1);
            String val = (String) trpList.get(2);
            Tripple trp = new Tripple(name, val, op);
            return trp;
        } else
            return null;
    }

    public boolean getHasNext() {
        int count = this.processManager.getNumberOfLongRunningProcesses();
        int oset = Integer.parseInt(this.offset.getOffset());
        int size = Integer.parseInt(this.offset.getSize());
        return (oset + size) < count;
    }

    public int getOffsetValue() {
        return Integer.parseInt(this.offset.getOffset());
    }

    public int getPageSize() {
        return Integer.parseInt(this.offset.getSize());
    }

    public String getOrdering() {
        return this.ordering.toString();
    }

    public String getTypeOfOrdering() {
        return this.typeOfOrdering.getTypeOfOrdering();
    }

    public String getMoreNextAHREF() {
        try {
            String nextString = bundleService.getResourceBundle("labels", locale).getString("administrator.processes.next");
            int count = this.processManager.getNumberOfLongRunningProcesses();
            int offset = Integer.parseInt(this.offset.getOffset());
            int size = Integer.parseInt(this.offset.getSize())*5;
            if ((offset + size) < count) {
                return "<a href=\"javascript:processes.modifyProcessDialogData('" + this.ordering + "','" + this.offset.getNextOffset() + "','" + size + "','" + this.typeOfOrdering.getTypeOfOrdering() + "');\"> " + nextString + " <img  border=\"0\" src=\"img/next_arr.png\"/> </a>";
            } else {
                return "<span>" + nextString + "</span> <img border=\"0\" src=\"img/next_arr.png\" alt=\"next\" />";
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            return "<img border=\"0\" src=\"img/next_arr.png\" alt=\"next\" />";
        }
        
    }
    
    public String getNextAHREF() {
        try {
            String nextString = bundleService.getResourceBundle("labels", locale).getString("administrator.processes.next");
            int count = this.processManager.getNumberOfLongRunningProcesses();
            int offset = Integer.parseInt(this.offset.getOffset());
            int size = Integer.parseInt(this.offset.getSize());
            if ((offset + size) < count) {
                return "<a href=\"javascript:processes.modifyProcessDialogData('" + this.ordering + "','" + this.offset.getNextOffset() + "','" + this.offset.getSize() + "','" + this.typeOfOrdering.getTypeOfOrdering() + "');\"> " + nextString + " <img  border=\"0\" src=\"img/next_arr.png\"/> </a>";
            } else {
                return "<span>" + nextString + "</span> <img border=\"0\" src=\"img/next_arr.png\" alt=\"next\" />";
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
                return "<a href=\"javascript:processes.modifyProcessDialogData('" + this.ordering + "','" + this.offset.getPrevOffset() + "','" + this.offset.getSize() + "','" + this.typeOfOrdering.getTypeOfOrdering() + "');\"> <img border=\"0\" src=\"img/prev_arr.png\"/> " + prevString + " </a>";
            } else {
                return "<img border=\"0\" src=\"img/prev_arr.png\" alt=\"prev\" /> <span>" + prevString + "</span>";
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
            return newOrderingURL(nOrdering, startedString, changeTypeOfOrdering ? switchOrdering() : TypeOfOrdering.ASC);
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
            return newOrderingURL(nOrdering, startedString, changeTypeOfOrdering ? switchOrdering() : TypeOfOrdering.ASC);
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
            return newOrderingURL(nOrdering, pidString, changeTypeOfOrdering ? switchOrdering() : TypeOfOrdering.ASC);
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
            return newOrderingURL(nOrdering, pidString, changeTypeOfOrdering ? switchOrdering() : TypeOfOrdering.ASC);
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
            return newOrderingURL(nOrdering, stateString, changeTypeOfOrdering ? switchOrdering() : TypeOfOrdering.ASC);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            return e.getMessage();
        }
    }

    private String newOrderingURL(LRProcessOrdering nOrdering, String name, TypeOfOrdering ntypeOfOrdering) {
        String href = "<a href=\"javascript:processes.modifyProcessDialogData('" + nOrdering + "','" + this.offset.getOffset() + "','" + this.offset.getSize() + "','" + ntypeOfOrdering.getTypeOfOrdering() + "');\"";
        if (this.ordering.equals(nOrdering)) {
            // href += orderingImg(nOrdering);
            if (typeOfOrdering.equals(TypeOfOrdering.DESC)) {
                href += " class=\"order_down\"";
            } else {
                href += " class=\"order_up\"";
            }

        }
        href += ">" + name + "</a>";
        return href;
    }

    public String getPlannedAfter() {
        if (this.filter != null) {
            List<Tripple> tripples = this.filter.getTripples();
            for (Tripple tripple : tripples) {
                if (tripple.getName().equals("planned") && tripple.getOp().equals(LRPRocessFilter.Op.GT)) {
                    return LRPRocessFilter.getFormattedValue(tripple);
                }
            }
        } 
        return "";
    }

    public String getPlannedBefore() {
        if (this.filter != null) {
            List<Tripple> tripples = this.filter.getTripples();
            for (Tripple tripple : tripples) {
                if (tripple.getName().equals("planned") && tripple.getOp().equals(LRPRocessFilter.Op.LT)) {
                    return LRPRocessFilter.getFormattedValue(tripple);
                }
            }
        } 
        return "";
    }


    public String getStartedAfter() {
        if (this.filter != null) {
            List<Tripple> tripples = this.filter.getTripples();
            for (Tripple tripple : tripples) {
                if (tripple.getName().equals("started") && tripple.getOp().equals(LRPRocessFilter.Op.GT)) {
                    return LRPRocessFilter.getFormattedValue(tripple);
                }
            }
        } 
        return "";
    }

    public String getStartedBefore() {
        if (this.filter != null) {
            List<Tripple> tripples = this.filter.getTripples();
            for (Tripple tripple : tripples) {
                if (tripple.getName().equals("started") && tripple.getOp().equals(LRPRocessFilter.Op.LT)) {
                    return LRPRocessFilter.getFormattedValue(tripple);
                }
            }
        } 
        return "";
    }

    public String getNameLike() {
        if (this.filter != null) {
            List<Tripple> tripples = this.filter.getTripples();
            for (Tripple tripple : tripples) {
                if (tripple.getName().equals("name") && tripple.getOp().equals(LRPRocessFilter.Op.LIKE)) {
                    return LRPRocessFilter.getFormattedValue(tripple);
                }
            }
        }    
        return "";
    }

    public List<ProcessStateWrapper> getStatesForFilter() {
        List<ProcessStateWrapper> wrap = ProcessStateWrapper.wrap(true, States.values());
        if (this.filter != null) {
            Tripple tripple = this.filter.findTripple("status");
            if (tripple != null) {
                Integer intg = (Integer)tripple.getVal();
                if (intg.intValue() >= 0) {
                    for (ProcessStateWrapper wrapper : wrap) {
                        if (wrapper.getVal() == intg.intValue()) {
                            wrapper.setSelected(true);
                        }
                    }
                }
            }
        }
        return wrap;
    }

    private String orderingImg(LRProcessOrdering nOrdering) {
        if (nOrdering.equals(this.ordering)) {
            if (typeOfOrdering.equals(TypeOfOrdering.DESC)) {
                return "<img src=\"img/order_down.png\"></img>";
            } else {
                return "<img src=\"img/order_up.png\"></img>";
            }
        } else
            return "";
    }
}
