package cz.incad.Kramerius.views;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;

import javax.servlet.http.HttpServletRequest;

import com.google.inject.Inject;
import com.google.inject.Provider;

import antlr.RecognitionException;
import antlr.TokenStreamException;
import cz.incad.kramerius.Initializable;
import cz.incad.kramerius.processes.BatchStates;
import cz.incad.kramerius.processes.DefinitionManager;
import cz.incad.kramerius.processes.LRProcess;
import cz.incad.kramerius.processes.LRProcessDefinition;
import cz.incad.kramerius.processes.LRProcessManager;
import cz.incad.kramerius.processes.LRProcessOrdering;
import cz.incad.kramerius.processes.States;
import cz.incad.kramerius.processes.template.OutputTemplateFactory;
import cz.incad.kramerius.security.database.TypeOfOrdering;
import cz.incad.kramerius.service.ResourceBundleService;
import cz.incad.kramerius.utils.database.Offset;
import cz.incad.kramerius.utils.database.SQLFilter;
import cz.incad.kramerius.utils.database.SQLFilter.Tripple;
import cz.incad.kramerius.utils.database.SQLFilter.TypesMapping;
import cz.incad.kramerius.utils.params.ParamsLexer;
import cz.incad.kramerius.utils.params.ParamsParser;

public class ProcessesViewObject implements Initializable {

    private static final int SMALL_SET_OF_DIRECT_PAGES = 25;

    private static final int LARGE_SET_OF_DIRECT_PAGES = 25;

    public static final java.util.logging.Logger LOGGER = java.util.logging.Logger
            .getLogger(ProcessesViewObject.class.getName());

    @Inject
    protected LRProcessManager processManager;

    @Inject
    protected DefinitionManager definitionManager;

    @Inject
    protected ResourceBundleService bundleService;

    @Inject
    protected Provider<Locale> localesProvider;

    @Inject
    protected Provider<HttpServletRequest> requestProvider;

    @Inject
    protected OutputTemplateFactory outputTemplateFactory;

    private LRProcessOrdering ordering;
    // private LRProcessOffset offset;
    private TypeOfOrdering typeOfOrdering;

    private String page;
    private String pageSize;

    private String lrUrl;

    private SQLFilter filter;

    private String filterParam;

    private int numberOfRunningProcesses = -1;

    public ProcessesViewObject() throws RecognitionException {
        super();
    }

    public void init() {
        try {

            String type = this.requestProvider.get().getParameter("type");
            if ((type == null) || (type.trim().equals(""))) {
                type = "DESC";
            }
            this.typeOfOrdering = TypeOfOrdering.valueOf(type);

            String ordering = this.requestProvider.get().getParameter(
                    "ordering");
            if ((ordering == null) || (ordering.trim().equals(""))) {
                ordering = LRProcessOrdering.PLANNED.name();
            }
            this.ordering = LRProcessOrdering.valueOf(ordering);

            this.filterParam = this.requestProvider.get()
                    .getParameter("filter");
            this.filter = this.createProcessFilter();

            String size = this.requestProvider.get().getParameter("size");
            if ((size == null) || (size.trim().equals(""))) {
                size = "5";
            }
            this.pageSize = size;

            String page = this.requestProvider.get().getParameter("page");
            if (page != null) {
                this.page = page;
            } else {
                this.page = "" + getFirstPage();
            }

        } catch (RecognitionException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public SQLFilter getFilter() {
        return filter;
    }

    public List<ProcessViewObject> getProcesses() {
        int pageSize = getDefaultPageSize();
        if (this.isCurrentFirstPage()) {
            pageSize = pageSize + (getNumberOfRunningProcess() % getDefaultPageSize());
        }

        Offset offset = new Offset("" + getOffset(getPage()), "" + pageSize);
        List<LRProcess> lrProcesses = this.processManager
                .getLongRunningProcessesAsGrouped(this.ordering,
                        this.typeOfOrdering, offset, this.filter);
        List<ProcessViewObject> objects = new ArrayList<ProcessViewObject>();
        for (LRProcess lrProcess : lrProcesses) {
            LRProcessDefinition def = this.definitionManager
                    .getLongRunningProcessDefinition(lrProcess
                            .getDefinitionId());
            ProcessViewObject pw = new ProcessViewObject(lrProcess, def,
                    this.ordering, offset, this.typeOfOrdering,
                    this.bundleService, this.localesProvider.get(),
                    this.outputTemplateFactory, this.page);
            if (lrProcess.isMasterProcess()) {
                List<LRProcess> childSubprecesses = this.processManager
                        .getLongRunningProcessesByGroupToken(lrProcess
                                .getGroupToken());
                for (LRProcess child : childSubprecesses) {
                    if (!child.getUUID().equals(lrProcess.getUUID())) {
                        LRProcessDefinition childDef = this.definitionManager
                                .getLongRunningProcessDefinition(child
                                        .getDefinitionId());
                        ProcessViewObject childPW = new ProcessViewObject(
                                child, childDef, this.ordering, offset,
                                this.typeOfOrdering, this.bundleService,
                                this.localesProvider.get(),
                                this.outputTemplateFactory, this.page);
                        pw.addChildProcess(childPW);
                    }
                }
            }
            objects.add(pw);
        }
        return objects;
    }

    private int getOffset(int page) {
        // max page - min offset
        int offsetPage = Math.max(getNumberOfPages() - 1 - page, 0);
        if (offsetPage >= 1) {
            // more then one -> must have bigger offset
            return offsetPage * getDefaultPageSize()
                    + (getNumberOfRunningProcess() % getDefaultPageSize());
        } else {
            return offsetPage * getDefaultPageSize();
        }
    }

    private SQLFilter createProcessFilter() throws RecognitionException {
        if (this.filterParam == null)
            return null;
        try {
            ParamsParser paramsParser = new ParamsParser(new ParamsLexer(
                    new StringReader(this.filterParam)));
            List params = paramsParser.params();
            List<SQLFilter.Tripple> tripples = new ArrayList<SQLFilter.Tripple>();
            for (Object object : params) {
                List trippleList = (List) object;
                Tripple tripple = createTripple(trippleList);
                if (tripple.getVal() != null) {
                    tripples.add(tripple);
                }
            }

            TypesMapping types = new TypesMapping();
            types.map("status", new SQLFilter.IntegerConverter());
            types.map("batch_status", new SQLFilter.IntegerConverter());
            types.map("planned", new SQLFilter.DateConvereter());
            types.map("started", new SQLFilter.DateConvereter());
            types.map("finished", new SQLFilter.DateConvereter());

            SQLFilter filter = SQLFilter.createFilter(types, tripples);
            // TODO: do it better
            if (filter != null) {

                Tripple statusTripple = filter.findTripple("status");
                if (statusTripple != null) {
                    if (((Integer) statusTripple.getVal()) == -1) {
                        filter.removeTripple(statusTripple);
                    }
                }

                Tripple bstatusTripple = filter.findTripple("batch_status");
                if (bstatusTripple != null) {
                    if (((Integer) bstatusTripple.getVal()) == -1) {
                        filter.removeTripple(bstatusTripple);
                    }
                }

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

    public boolean getHasPrevious() {
        int page = getPage();
        int numberOfPages = getNumberOfPages();
        return page < (numberOfPages - 1);
    }

    public boolean getHasNext() {
        return getPage() > 0;
    }

    public int getNumberOfRunningProcess() {
        if (this.numberOfRunningProcesses == -1) {
            this.numberOfRunningProcesses = this.processManager
                    .getNumberOfLongRunningProcesses(this.filter);
        }
        return this.numberOfRunningProcesses;
    }

    public int getDefaultPageSize() {
        return Integer.parseInt(this.pageSize);
    }
    public int getPageSize() {
        return Integer.parseInt(this.pageSize);
    }

    public int getNumberOfPages() {
        return getNumberOfRunningProcess() / getDefaultPageSize();
    }

    public int getPage() {
        return Integer.parseInt(this.page);
    }

    public String getPageLabel() {
        return page;
    }

    public int getPrevPageValue() {
        return (getPage() < getNumberOfPages()) ? getPage() + 1 : 0;
    }

    public int getNextPageValue() {
        return (getPage() > 0) ? getPage() - 1 : getNumberOfPages();
    }

    public int getFirstPage() {
        return Math.max(getNumberOfPages() - 1, 0);
    }

    public int getLastPage() {
        return 0;
    }

    public String getOrdering() {
        return this.ordering.toString();
    }

    public String getTypeOfOrdering() {
        return this.typeOfOrdering.getTypeOfOrdering();
    }

    public boolean isNecessaryDisplayMorePages() {
        return this.getNumberOfPages() > SMALL_SET_OF_DIRECT_PAGES;
    }

    public List<String> getLargeSetOfDirectPates() {
        List<String> hrefs = new ArrayList<String>();
        int pages = getNumberOfPages();
        int page = getPage();
        for (int i = pages - 1; i >= 0; i--) {
            String href = "<a id=\"process_page_"
                    + i
                    + "\" href=\"javascript:_wait();processes.modifyProcessDialogDataByPage('"
                    + this.ordering + "','" + i + "','" + this.pageSize + "','"
                    + this.typeOfOrdering.getTypeOfOrdering() + "');\"> " + i
                    + "</a>";
            hrefs.add(href);
        }
        return hrefs;

    }

    public List<String> getSmallSetOfDirectPages() {
        List<String> hrefs = new ArrayList<String>();
        int pageFrom = Math.min(Math.max(getPage(), SMALL_SET_OF_DIRECT_PAGES),
                getNumberOfPages() - 1);
        int pageTo = Math.max(pageFrom - SMALL_SET_OF_DIRECT_PAGES, 0);
        for (int i = pageFrom; i >= pageTo; i--) {
            String href = "<a href=\"javascript:_wait();processes.modifyProcessDialogDataByPage('"
                    + this.ordering
                    + "','"
                    + i
                    + "','"
                    + this.pageSize
                    + "','"
                    + this.typeOfOrdering.getTypeOfOrdering()
                    + "');\"> "
                    + i
                    + "</a>";
            hrefs.add(href);
        }

        return hrefs;

    }

    public List<String> getDirectPages() {
        List<String> hrefs = new ArrayList<String>();
        int pages = getNumberOfPages();
        for (int i = pages - 1; i >= 0; i--) {
            String href = "<a href=\"javascript:_wait();processes.modifyProcessDialogDataByPage('"
                    + this.ordering
                    + "','"
                    + i
                    + "','"
                    + this.pageSize
                    + "','"
                    + this.typeOfOrdering.getTypeOfOrdering()
                    + "');\"> "
                    + i
                    + "</a>";
            hrefs.add(href);
        }
        return hrefs;
    }

    public String getNextPageAHREF() {
        try {
            String nextString = bundleService.getResourceBundle("labels",
                    this.localesProvider.get()).getString(
                    "administrator.processes.next");
            return "<a href=\"javascript:_wait();processes.modifyProcessDialogDataByPage('"
                    + this.ordering
                    + "','"
                    + getNextPageValue()
                    + "','"
                    + this.pageSize
                    + "','"
                    + this.typeOfOrdering.getTypeOfOrdering()
                    + "');\"> "
                    + nextString
                    + " <img  border=\"0\" src=\"img/next_arr.png\"/> </a>";
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            return "<img border=\"0\" src=\"img/next_arr.png\" alt=\"next\" />";
        }
    }

    public String getPrevPageAHREF() {
        try {
            String prevString = bundleService.getResourceBundle("labels",
                    this.localesProvider.get()).getString(
                    "administrator.processes.prev");
            return "<a href=\"javascript:_wait();processes.modifyProcessDialogDataByPage('"
                    + this.ordering
                    + "','"
                    + this.getPrevPageValue()
                    + "','"
                    + this.pageSize
                    + "','"
                    + this.typeOfOrdering.getTypeOfOrdering()
                    + "');\"> <img border=\"0\" src=\"img/prev_arr.png\"/> "
                    + prevString + " </a>";
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            return "<img border=\"0\" src=\"img/prev_arr.png\" alt=\"prev\" /> ";
        }
    }

    private TypeOfOrdering switchOrdering() {
        return this.typeOfOrdering.equals(TypeOfOrdering.ASC) ? TypeOfOrdering.DESC
                : TypeOfOrdering.ASC;
    }

    public String getOrderingIcon() {
        return this.typeOfOrdering.equals(TypeOfOrdering.ASC) ? "<span class='ui-icon ui-icon-triangle-1-s'></span>"
                : "<span class='ui-icon ui-icon-triangle-1-n'></span>";
    }

    public boolean isNameOrdered() {
        return this.ordering.equals(LRProcessOrdering.NAME);
    }

    public String getNameOrdering() {
        try {
            String nameString = bundleService.getResourceBundle("labels",
                    this.localesProvider.get()).getString(
                    "administrator.processes.name");
            LRProcessOrdering nOrdering = LRProcessOrdering.NAME;
            boolean changeTypeOfOrdering = this.ordering.equals(nOrdering);
            return newOrderingURL(nOrdering, nameString,
                    changeTypeOfOrdering ? switchOrdering()
                            : TypeOfOrdering.ASC);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            return e.getMessage();
        }
    }

    public boolean isStartedDateOrdered() {
        return this.ordering.equals(LRProcessOrdering.STARTED);
    }

    public String getDateOrdering() {
        try {
            String startedString = bundleService.getResourceBundle("labels",
                    this.localesProvider.get()).getString(
                    "administrator.processes.started");
            LRProcessOrdering nOrdering = LRProcessOrdering.STARTED;
            boolean changeTypeOfOrdering = this.ordering.equals(nOrdering);
            return newOrderingURL(nOrdering, startedString,
                    changeTypeOfOrdering ? switchOrdering()
                            : TypeOfOrdering.ASC);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            return e.getMessage();
        }
    }

    public boolean isPlannedDateOrdered() {
        return this.ordering.equals(LRProcessOrdering.PLANNED);
    }

    public String getPlannedDateOrdering() {
        try {
            String startedString = bundleService.getResourceBundle("labels",
                    this.localesProvider.get()).getString(
                    "administrator.processes.planned");
            LRProcessOrdering nOrdering = LRProcessOrdering.PLANNED;
            boolean changeTypeOfOrdering = this.ordering.equals(nOrdering);
            return newOrderingURL(nOrdering, startedString,
                    changeTypeOfOrdering ? switchOrdering()
                            : TypeOfOrdering.ASC);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            return e.getMessage();
        }
    }

    public boolean isFinishedDateOrdered() {
        return this.ordering.equals(LRProcessOrdering.FINISHED);
    }

    public String getFinishedDateOrdering() {
        try {
            String startedString = bundleService.getResourceBundle("labels",
                    this.localesProvider.get()).getString(
                    "administrator.processes.finished");
            LRProcessOrdering nOrdering = LRProcessOrdering.FINISHED;
            boolean changeTypeOfOrdering = this.ordering.equals(nOrdering);
            return newOrderingURL(nOrdering, startedString,
                    changeTypeOfOrdering ? switchOrdering()
                            : TypeOfOrdering.ASC);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            return e.getMessage();
        }
    }

    public boolean isUserOrdered() {
        return this.ordering.equals(LRProcessOrdering.LOGINNAME);
    }

    public String getUserOrdering() {
        try {
            String pidString = bundleService.getResourceBundle("labels",
                    this.localesProvider.get()).getString(
                    "administrator.processes.user");
            LRProcessOrdering nOrdering = LRProcessOrdering.LOGINNAME;
            boolean changeTypeOfOrdering = this.ordering.equals(nOrdering);
            return newOrderingURL(nOrdering, pidString,
                    changeTypeOfOrdering ? switchOrdering()
                            : TypeOfOrdering.ASC);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            return e.getMessage();
        }
    }

    public String getPidOrdering() {
        try {
            String pidString = bundleService.getResourceBundle("labels",
                    this.localesProvider.get()).getString(
                    "administrator.processes.pid");
            LRProcessOrdering nOrdering = LRProcessOrdering.ID;
            boolean changeTypeOfOrdering = this.ordering.equals(nOrdering);
            return newOrderingURL(nOrdering, pidString,
                    changeTypeOfOrdering ? switchOrdering()
                            : TypeOfOrdering.ASC);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            return e.getMessage();
        }
    }

    public boolean isStateOrdered() {
        return this.ordering.equals(LRProcessOrdering.STATE);
    }

    public String getStateOrdering() {
        try {
            String stateString = bundleService.getResourceBundle("labels",
                    this.localesProvider.get()).getString(
                    "administrator.processes.state");
            LRProcessOrdering nOrdering = LRProcessOrdering.STATE;
            boolean changeTypeOfOrdering = this.ordering.equals(nOrdering);
            return newOrderingURL(nOrdering, stateString,
                    changeTypeOfOrdering ? switchOrdering()
                            : TypeOfOrdering.ASC);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            return e.getMessage();
        }
    }

    public boolean isBatchStateOrdered() {
        return this.ordering.equals(LRProcessOrdering.BATCHSTATE);
    }

    public String getBatchStateOrdering() {
        try {
            String stateString = bundleService.getResourceBundle("labels",
                    this.localesProvider.get()).getString(
                    "administrator.processes.batch");
            LRProcessOrdering nOrdering = LRProcessOrdering.BATCHSTATE;
            boolean changeTypeOfOrdering = this.ordering.equals(nOrdering);
            return newOrderingURL(nOrdering, stateString,
                    changeTypeOfOrdering ? switchOrdering()
                            : TypeOfOrdering.ASC);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            return e.getMessage();
        }
    }

    private String newOrderingURL(LRProcessOrdering nOrdering, String name,
            TypeOfOrdering ntypeOfOrdering) {
        String href = "<a href=\"javascript:_wait();processes.modifyProcessDialogDataByPage('"
                + nOrdering
                + "','"
                + this.page
                + "','"
                + this.pageSize
                + "','"
                + ntypeOfOrdering.getTypeOfOrdering() + "');\"";
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
                if (tripple.getName().equals("planned")
                        && tripple.getOp().equals(SQLFilter.Op.GT)) {
                    return this.filter.getFormattedValue(tripple);
                }
            }
        }
        return "";
    }

    public String getPlannedBefore() {
        if (this.filter != null) {
            List<Tripple> tripples = this.filter.getTripples();
            for (Tripple tripple : tripples) {
                if (tripple.getName().equals("planned")
                        && tripple.getOp().equals(SQLFilter.Op.LT)) {

                    return this.filter.getFormattedValue(tripple);
                }
            }
        }
        return "";
    }

    public String getStartedAfter() {
        if (this.filter != null) {
            List<Tripple> tripples = this.filter.getTripples();
            for (Tripple tripple : tripples) {
                if (tripple.getName().equals("started")
                        && tripple.getOp().equals(SQLFilter.Op.GT)) {
                    return this.filter.getFormattedValue(tripple);
                }
            }
        }
        return "";
    }

    public String getStartedBefore() {
        if (this.filter != null) {
            List<Tripple> tripples = this.filter.getTripples();
            for (Tripple tripple : tripples) {
                if (tripple.getName().equals("started")
                        && tripple.getOp().equals(SQLFilter.Op.LT)) {
                    return this.filter.getFormattedValue(tripple);
                }
            }
        }
        return "";
    }

    public String getFinishedAfter() {
        if (this.filter != null) {
            List<Tripple> tripples = this.filter.getTripples();
            for (Tripple tripple : tripples) {
                if (tripple.getName().equals("finished")
                        && tripple.getOp().equals(SQLFilter.Op.GT)) {
                    return this.filter.getFormattedValue(tripple);
                }
            }
        }
        return "";
    }

    public String getFinishedBefore() {
        if (this.filter != null) {
            List<Tripple> tripples = this.filter.getTripples();
            for (Tripple tripple : tripples) {
                if (tripple.getName().equals("finished")
                        && tripple.getOp().equals(SQLFilter.Op.LT)) {
                    return this.filter.getFormattedValue(tripple);
                }
            }
        }
        return "";
    }

    public String getLoginNameLike() {
        if (this.filter != null) {
            List<Tripple> tripples = this.filter.getTripples();
            for (Tripple tripple : tripples) {
                if (tripple.getName().equals("loginname") && tripple.getOp().equals(SQLFilter.Op.LIKE)) {
                    return this.filter.getFormattedValue(tripple);
                }
            }
        }
        return "";
    }

    public String getNameLike() {
        if (this.filter != null) {
            List<Tripple> tripples = this.filter.getTripples();
            for (Tripple tripple : tripples) {
                if (tripple.getName().equals("name")
                        && tripple.getOp().equals(SQLFilter.Op.LIKE)) {
                    return this.filter.getFormattedValue(tripple);
                }
            }
        }
        return "";
    }

    public List<BatchProcessStateWrapper> getBatchStatesForFilter() {
        List<BatchProcessStateWrapper> wrap = BatchProcessStateWrapper.wrap(
                true, BatchStates.values());
        if (this.filter != null) {
            Tripple tripple = this.filter.findTripple("batch_status");
            if (tripple != null) {
                Integer intg = (Integer) tripple.getVal();
                if (intg.intValue() >= 0) {
                    for (BatchProcessStateWrapper wrapper : wrap) {
                        if (wrapper.getVal() == intg.intValue()) {
                            wrapper.setSelected(true);
                        }
                    }
                }
            }
        }
        return wrap;
    }

    public List<ProcessStateWrapper> getStatesForFilter() {
        List<ProcessStateWrapper> wrap = ProcessStateWrapper.wrap(true,
                States.values());
        if (this.filter != null) {
            Tripple tripple = this.filter.findTripple("status");
            if (tripple != null) {
                Integer intg = (Integer) tripple.getVal();
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

    public LRProcessManager getProcessManager() {
        return processManager;
    }

    public void setProcessManager(LRProcessManager processManager) {
        this.processManager = processManager;
    }

    public DefinitionManager getDefinitionManager() {
        return definitionManager;
    }

    public void setDefinitionManager(DefinitionManager definitionManager) {
        this.definitionManager = definitionManager;
    }

    public ResourceBundleService getBundleService() {
        return bundleService;
    }

    public void setBundleService(ResourceBundleService bundleService) {
        this.bundleService = bundleService;
    }

    public Provider<Locale> getLocalesProvider() {
        return localesProvider;
    }

    public void setLocalesProvider(Provider<Locale> localesProvider) {
        this.localesProvider = localesProvider;
    }

    public Provider<HttpServletRequest> getRequestProvider() {
        return requestProvider;
    }

    public void setRequestProvider(Provider<HttpServletRequest> requestProvider) {
        this.requestProvider = requestProvider;
    }

    public boolean isCurrentFirstPage() {
        return getPage() == getNumberOfPages()-1;
    }

    public boolean isCurrentLastPage() {
        return getPage() == 0;
    }

}
