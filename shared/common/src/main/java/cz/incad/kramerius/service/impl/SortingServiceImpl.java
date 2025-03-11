package cz.incad.kramerius.service.impl;

import javax.annotation.PostConstruct;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.logging.Logger;

import cz.incad.kramerius.processes.starter.ProcessStarter;
import org.ceskaexpedice.akubra.AkubraRepository;
import org.ceskaexpedice.akubra.KnownDatastreams;
import org.ceskaexpedice.akubra.RepositoryNamespaceContext;
import org.w3c.dom.Document;

import com.google.common.collect.Ordering;
import com.google.common.collect.TreeMultimap;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.ibm.icu.text.Collator;

import cz.incad.kramerius.KrameriusModels;
import cz.incad.kramerius.relation.Relation;
import cz.incad.kramerius.relation.RelationModel;
import cz.incad.kramerius.relation.RelationService;
//import cz.incad.kramerius.relation.RelationUtils;
import cz.incad.kramerius.service.SortingService;
import cz.incad.kramerius.utils.NaturalOrderCollator;
import cz.incad.kramerius.utils.conf.KConfiguration;

/**
 * @ author vlahoda
 */
public class SortingServiceImpl implements SortingService {

    public static final Logger LOGGER = Logger.getLogger(SortingServiceImpl.class.getName());
    public static final String CONFIG_KEY = "sort.xpaths";

    AkubraRepository akubraRepository;
    KConfiguration configuration = KConfiguration.getInstance();

    RelationService relationService;
    private XPathFactory xpathFactory = XPathFactory.newInstance();
    private Map<String, String> sortingConfigMap = new HashMap<String, String>();

    @Inject
    public SortingServiceImpl(
            // TODO AK_NEW @Named("rawFedoraAccess") FedoraAccess fedoraAccess,
            AkubraRepository akubraRepository,
            RelationService relationService) {
        this.akubraRepository = akubraRepository;
        initSortingConfigMap();
    }

    public static void main(String[] args) throws IOException {
        LOGGER.info("SortRelations service: " + Arrays.toString(args));
        Injector injector = Guice.createInjector(new SortingModule());
        SortingService inst = injector.getInstance(SortingService.class);
        inst.sortRelations(args[0], true);
        LOGGER.info("SortRelations finished.");
    }

    @Override
    public void sortRelations(String pid, boolean startIndexer) {
        try {
            //TODO: I18n
            if (startIndexer) {
                try {
                    ProcessStarter.updateName("Sort relations (" + pid + ")");
                } catch (Exception ex) {
                }
            }
            Date lastTime = akubraRepository.getProperties(pid).getPropertyLastModified();
            RelationModel model = relationService.load(pid);
            for (KrameriusModels kind : model.getRelationKinds()) {
                if (KrameriusModels.DONATOR.equals(kind))
                    continue;
                List<Relation> relations = model.getRelations(kind);
                List<String> originalPids = new ArrayList<String>(relations.size());
                for (Relation relation : relations) {
                    originalPids.add(relation.getPID());
                }
                String xpath = sortingConfigMap.get(kind.getValue());
                if (xpath == null) {
                    LOGGER.warning("Unsupported relation type for sorting: " + kind.getValue());
                    continue;
                }
                List<String> sortedPids = sortObjects(originalPids, xpath);
                relations.clear();
                for (String sortedPid : sortedPids) {
                    relations.add(new Relation(sortedPid, kind));
                }
            }
            Date currTime = akubraRepository.getProperties(pid).getPropertyLastModified();

            if (currTime.equals(lastTime)) {
                relationService.save(pid, model);
                if (startIndexer) {
                    IndexerProcessStarter.spawnIndexer(true, "Reindexing sorted relations", pid);
                }
            } else {
                LOGGER.warning("Cannot save sorted relations, object " + pid + " was modified.");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<String> sortObjects(List<String> pids, String xpathString) {
        Collator stringCollator = Collator.getInstance(new Locale(configuration.getConfiguration().getString("sort.locale", "cs_CZ")));
        TreeMultimap<String, String> sortedMap = TreeMultimap.create(new NaturalOrderCollator(stringCollator), Ordering.natural());
        List<String> failedList = new ArrayList<String>();
        XPathExpression expr = null;
        try {
            XPath xpath = xpathFactory.newXPath();
            xpath.setNamespaceContext(new RepositoryNamespaceContext());
            expr = xpath.compile(xpathString);
        } catch (XPathExpressionException e) {
            throw new RuntimeException(e);
        }
        for (String pid : pids) {
            String sortingValue = null;
            try {
                Document mods = akubraRepository.getDatastreamContent(pid, KnownDatastreams.BIBLIO_MODS).asDom(true);
                sortingValue = expr.evaluate(mods);
            } catch (Exception e) {
                //ignore, will be logged in next step  (sortingValue test)
            }
            if (sortingValue == null || "".equals(sortingValue)) {
                failedList.add(pid);
                LOGGER.info("Cannot sort relation for invalid value:" + sortingValue + " (" + pid + ")");
            } else {
                try {
                    sortedMap.put(sortingValue, pid);
                } catch (Exception ex) {
                    failedList.add(pid);
                    LOGGER.info("Cannot sort relation for invalid value:" + sortingValue + " (" + pid + ")");
                }
            }
        }
        List<String> result = new ArrayList<String>(pids.size());
        for (String o : sortedMap.values()) {
            result.add(o);
        }
        result.addAll(failedList);
        return result;
    }

    @PostConstruct
    private void initSortingConfigMap() {
        String[] rawConfig = configuration.getConfiguration().getStringArray(CONFIG_KEY);
        for (String modelConfig : rawConfig) {
            String[] configItems = modelConfig.split(";");

            sortingConfigMap.put(configItems[0], configItems[1]);
        }
    }
}

//class SortingModule extends AbstractModule {
//    @Override
//    protected void configure() {
//        bind(FedoraAccess.class).annotatedWith(Names.named("rawFedoraAccess")).to(FedoraAccessImpl.class).in(Scopes.SINGLETON);
//
//        bind(StatisticsAccessLog.class).annotatedWith(Names.named("database")).to(DatabaseStatisticsAccessLogImpl.class).in(Scopes.SINGLETON);
//        bind(StatisticsAccessLog.class).annotatedWith(Names.named("dnnt")).to(DNNTStatisticsAccessLogImpl.class).in(Scopes.SINGLETON);
//
//        bind(AggregatedAccessLogs.class).in(Scopes.SINGLETON);
//        bind(KConfiguration.class).toInstance(KConfiguration.getInstance());
//        bind(RelationService.class).to(RelationServiceImpl.class).in(Scopes.SINGLETON);
//        bind(SortingService.class).to(SortingServiceImpl.class).in(Scopes.SINGLETON);
//    }
//}
//
