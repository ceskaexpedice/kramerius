package cz.incad.kramerius.service.impl;

import com.google.inject.*;
import com.google.inject.name.Named;
import com.google.inject.name.Names;
import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.FedoraNamespaceContext;
import cz.incad.kramerius.KrameriusModels;
import cz.incad.kramerius.imaging.lp.guice.GenerateDeepZoomCacheModule;
import cz.incad.kramerius.impl.FedoraAccessImpl;
import cz.incad.kramerius.processes.impl.ProcessStarter;
import cz.incad.kramerius.relation.Relation;
import cz.incad.kramerius.relation.RelationModel;
import cz.incad.kramerius.relation.RelationService;
import cz.incad.kramerius.relation.RelationUtils;
import cz.incad.kramerius.relation.impl.RelationServiceImpl;
import cz.incad.kramerius.service.SortingService;
import cz.incad.kramerius.statistics.StatisticsAccessLog;
import cz.incad.kramerius.utils.conf.KConfiguration;
import org.w3c.dom.Document;

import javax.annotation.PostConstruct;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;

/**
 * @ author vlahoda
 */
public class SortingServiceImpl implements SortingService {

    public static final Logger LOGGER = Logger.getLogger(SortingServiceImpl.class.getName());

    public static final String CONFIG_KEY = "sort.xpaths";


    FedoraAccess fedoraAccess;

    KConfiguration configuration;


    RelationService relationService;

    @Inject
    public SortingServiceImpl(@Named("rawFedoraAccess") FedoraAccess fedoraAccess, KConfiguration configuration, RelationService relationService){
        this.fedoraAccess = fedoraAccess;
        this.configuration = configuration;
        this.relationService = relationService;
        initSortingConfigMap();
    }


    @Override
    public void sortRelations(String pid, boolean startIndexer) {
        try {
            //TODO: I18n
            if (startIndexer){
                try{
                    ProcessStarter.updateName("Sort relations (" + pid + ")");
                }catch(Exception ex){}
            }
            RelationModel model = relationService.load(pid);
            for (KrameriusModels kind : model.getRelationKinds()) {
                if (KrameriusModels.DONATOR.equals(kind)) continue;
                List<Relation> relations = model.getRelations(kind);
                List<String> originalPids = new ArrayList<String>(relations.size());
                for (Relation relation : relations) {
                    originalPids.add(relation.getPID());
                }
                SortingConfig sortingConfig = sortingConfigMap.get(kind.getValue());
                if (sortingConfig == null){
                    LOGGER.warning("Unsupported relation type for sorting: "+kind.getValue());
                    continue;
                }
                List<String> sortedPids = sortObjects(originalPids, sortingConfig.xpath, sortingConfig.numeric);
                relations.clear();
                for (String sortedPid : sortedPids) {
                    relations.add(new Relation(sortedPid, kind));
                }
            }
            relationService.save(pid, model);
            if (startIndexer){
                IndexerProcessStarter.spawnIndexer(true, "Reindexing sorted relations", pid);
            }
            


        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<String> sortObjects(List<String> pids, String xpathString, boolean numeric) {
        TreeMap<Object, String> sortedMap = new TreeMap<Object, String>();
        List<String> failedList = new ArrayList<String>();
        XPathExpression expr = null;
        try {
            XPath xpath = xpathFactory.newXPath();
            xpath.setNamespaceContext(new FedoraNamespaceContext());
            expr = xpath.compile(xpathString);
        } catch (XPathExpressionException e) {
            throw new RuntimeException(e);
        }
        for (String pid : pids) {
            String sortingValue = null;
            try{
                Document mods = RelationUtils.getMods(pid, fedoraAccess);
                sortingValue = expr.evaluate(mods);
            } catch (Exception e) {
                //ignore, will be logged in next step  (sortingValue test)
            }
            if (sortingValue == null || "".equals(sortingValue)){
                failedList.add(pid);
                LOGGER.info("Cannot sort relation for invalid value:"+sortingValue + " ("+pid+")");
            }else{
                if (numeric){
                    try{
                        Integer ordinal = Integer.parseInt(sortingValue);
                        String existing = sortedMap.put(ordinal,pid);
                        if (existing != null){
                            failedList.add(existing);
                        }
                    }catch (Exception ex){
                        failedList.add(pid);
                        LOGGER.info("Cannot sort relation for invalid numeric value:"+sortingValue + " ("+pid+")");
                    }
                }else{
                    String existing = sortedMap.put(sortingValue,pid);
                    if (existing != null){
                        failedList.add(existing);
                    }
                }
            }
        }
        List<String> result = new ArrayList<String>(pids.size());
        for (Map.Entry<Object,String> entry:sortedMap.entrySet()){
            result.add(entry.getValue());
        }
        result.addAll(failedList);
        return result;
    }

    private XPathFactory xpathFactory = XPathFactory.newInstance();
    private Map<String, SortingConfig> sortingConfigMap = new HashMap<String,SortingConfig>();

    @PostConstruct
    private void initSortingConfigMap(){
        String[] rawConfig = configuration.getConfiguration().getStringArray(CONFIG_KEY);
        for (String modelConfig:rawConfig){
            String[] configItems = modelConfig.split(";");
            SortingConfig sortingConfig = new SortingConfig();
            sortingConfig.xpath = configItems[1];
            sortingConfig.numeric = Boolean.parseBoolean(configItems[2]);
            sortingConfigMap.put(configItems[0], sortingConfig);
        }
    }



    public static void main(String[] args) throws IOException {
        LOGGER.info("SortRelations service: " + Arrays.toString(args));
        Injector injector = Guice.createInjector(new SortingModule());
        SortingService inst = injector.getInstance(SortingService.class);
        inst.sortRelations(args[0], true);
        LOGGER.info("SortRelations finished.");
    }
}

class SortingModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(FedoraAccess.class).annotatedWith(Names.named("rawFedoraAccess")).to(FedoraAccessImpl.class).in(Scopes.SINGLETON);
        bind(StatisticsAccessLog.class).to(GenerateDeepZoomCacheModule.NoStatistics.class).in(Scopes.SINGLETON);
        bind(KConfiguration.class).toInstance(KConfiguration.getInstance());
        bind(RelationService.class).to(RelationServiceImpl.class).in(Scopes.SINGLETON);
        bind(SortingService.class).to(SortingServiceImpl.class).in(Scopes.SINGLETON);
    }
}

class SortingConfig  {
    String xpath;
    boolean numeric;
}
