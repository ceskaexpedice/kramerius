package cz.incad.kramerius;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.antlr.stringtemplate.StringTemplate;
import org.antlr.stringtemplate.language.DefaultTemplateLexer;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.solr.client.solrj.SolrServerException;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import com.google.common.io.Files;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.hazelcast.config.KubernetesConfig;

import cz.incad.kramerius.fedora.RepoModule;
import cz.incad.kramerius.fedora.om.RepositoryException;
import cz.incad.kramerius.impl.SolrAccessImplNewIndex;
import cz.incad.kramerius.resourceindex.ResourceIndexException;
import cz.incad.kramerius.resourceindex.ResourceIndexModule;
import cz.incad.kramerius.rest.apiNew.client.v60.libs.OneInstance;
import cz.incad.kramerius.rest.apiNew.client.v60.libs.properties.DefaultPropertiesInstances;
import cz.incad.kramerius.service.MigrateSolrIndexException;
import cz.incad.kramerius.solr.SolrModule;
import cz.incad.kramerius.statistics.NullStatisticsModule;
import cz.incad.kramerius.utils.StringUtils;
import cz.incad.kramerius.utils.XMLUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;
//import cz.incad.kramerius.utils.solr.SolrUtils;
import cz.kramerius.searchIndex.indexer.SolrConfig;
import cz.kramerius.searchIndex.indexer.SolrIndexAccess;
import cz.incad.kramerius.services.KubernetesProcessImpl;
import cz.incad.kramerius.services.ParallelProcessImpl;
import cz.incad.kramerius.services.utils.ResultsUtils;
import cz.incad.kramerius.services.utils.SolrUtils;


public class ReharvestPids {
    
    public static final Logger LOGGER = Logger.getLogger(ReharvestPids.class.getName());

    
    /**
     * args[0] - action (ADD/REMOVE), from lp.st process/parameters
     * args[1] - authToken
     * args[2] - target (pid:uuid:123, or pidlist:uuid:123;uuid:345;uuid:789, or pidlist_file:/home/kramerius/.kramerius/import-dnnt/grafiky.txt
     * In case of pidlist pids must be separated with ';'. Convenient separator ',' won't work due to way how params are stored in database and transferred to process.
     * <p>
     * args[3] - licence ('dnnt', 'dnnto', 'public_domain', etc.)
     * @throws TransformerException 
     * @throws ParserConfigurationException 
     * @throws MigrateSolrIndexException 
     */
    public static void main(String[] args) throws IOException, SolrServerException, RepositoryException, ResourceIndexException, TransformerException, ParserConfigurationException, MigrateSolrIndexException {
        if (args.length < 2) {
            throw new RuntimeException("Not enough arguments.");
        }
        LOGGER.info("Parameters "+Arrays.asList(args));
        int argsIndex = 0;
//        //params from lp.st
        String authToken = args[argsIndex++];
        String target = args[argsIndex++]; //auth token always second, but still suboptimal solution, best would be if it was outside the scope of this as if ProcessHelper.scheduleProcess() similarly to changing name (ProcessStarter)
        String onlyShowConfiguration = null;
        if (args.length>=3) {
            onlyShowConfiguration =  args[argsIndex++];
        }

        List<String> extractPids = extractPids(target);
        for (String pid : extractPids) { reharvestPID(pid, onlyShowConfiguration); }
    }

    private static void reharvestPID(String pid, String onlyShowConfiguration)
            throws IOException, TransformerException, MigrateSolrIndexException, ParserConfigurationException {
        Injector injector = Guice.createInjector(new SolrModule(), new ResourceIndexModule(), new RepoModule(), new NullStatisticsModule(), new ResourceIndexModule());
        
        SolrAccess searchIndex = injector.getInstance(Key.get(SolrAccessImplNewIndex.class)); //FIXME: hardcoded implementation
        Document document = searchIndex.getSolrDataByPid(pid);
        
        if (document != null) {
            // nalezen v solru 
            int nf = numFound(document);
            if (nf > 0) {
                
                String msg = String.format("Reharvesting pid %s ", pid);
                LOGGER.info(msg);

                String leader = leader(document);
                List<String> collections = collections(document);
                
                ParallelProcessImpl parallelProcess = new ParallelProcessImpl();
                Document deleteByQuery = deleteRootPid(searchIndex, pid);
                if (isOnlyShowConfiguration(onlyShowConfiguration)) {
                    StringWriter writer = new StringWriter();
                    XMLUtils.print(deleteByQuery, writer);
                    LOGGER.info("Delete by query "+writer.toString());
                } else {
                    String s = SolrUtils.sendToDest(getDestinationUpdateUrl(), parallelProcess.getClient(), deleteByQuery);
                }
                
                collections.remove(leader);
                collections.add(0, leader);
                
                reharvestPIDFromGivenCollections(pid, collections, onlyShowConfiguration);
            } else {
                DefaultPropertiesInstances props = new DefaultPropertiesInstances();   
                List<OneInstance> enabledInstances = props.enabledInstances();
                reharvestPIDFromGivenCollections(pid, enabledInstances.stream().map(OneInstance::getName).collect(Collectors.toList()), onlyShowConfiguration);
            }
        }
    }

    
    private static boolean isOnlyShowConfiguration(String onlyShowConfiguration) {
        if (onlyShowConfiguration != null && ("onlyshowconfiguration".equals(onlyShowConfiguration.toLowerCase()) || "true".equals(onlyShowConfiguration))) {
            return true;
        }
        return true;
    }

    private static void reharvestPIDFromGivenCollections(String pid, List<String> collections, String onlyShowConfiguration) {
        List<File> harvestFiles = new ArrayList<>();
        for (String ac : collections) {
            try {
                String api = KConfiguration.getInstance().getConfiguration().getString("cdk.collections.sources." + ac + ".api");
                if (api == null) {
                    LOGGER.warning(String.format("Skipping instance %s", ac));
                    continue;
                }
                
                
                Pair<String,Boolean> iterationUrl = iterationUrl(ac);

                Map<String,String> iteration = new HashMap<>();
                iteration.put("url", iterationUrl.getKey());
                iteration.put("dl", ac);
                iteration.put("fquery", fq(api, pid));
                
                
                if (iterationUrl.getRight()) {
                    String username = KConfiguration.getInstance().getConfiguration().getString("cdk.collections.sources." + ac + ".username");
                    String password = KConfiguration.getInstance().getConfiguration().getString("cdk.collections.sources." + ac + ".pswd");

                    iteration.put("user", username);
                    iteration.put("pass", password);
                }
                
                // destination
                Map<String,String> destination = new HashMap<>();
                destination.put("url",  KConfiguration.getInstance().getSolrSearchHost());
                
                boolean publicEndpoint = KConfiguration.getInstance().getConfiguration().containsKey("cdk.collections.sources." + ac + ".public") ?  KConfiguration.getInstance().getConfiguration().getBoolean("cdk.collections.sources." + ac + ".public") : false;

                String configuration = renderTemplate(publicEndpoint, api, iteration, destination);
                File tmpFile = File.createTempFile(String.format("%s",  ac), "reharvest");
                
                Files.write(configuration.getBytes("UTF-8"), tmpFile);
                harvestFiles.add(tmpFile);
                
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE,e.getMessage(),e);
            }
        }
        
        for (File harvestFile : harvestFiles) {
            try {
                if (isOnlyShowConfiguration(onlyShowConfiguration)) {
                    String config = org.apache.commons.io.IOUtils.toString(new FileInputStream(harvestFile), "UTF-8");
                    LOGGER.info(String.format("Configuration %s" ,config));
                } else {
                    ParallelProcessImpl reharvest = new ParallelProcessImpl();
                    String config = org.apache.commons.io.IOUtils.toString(new FileInputStream(harvestFile), "UTF-8");
                    reharvest.migrate(harvestFile);
                }
                
            } catch (IOException | MigrateSolrIndexException | IllegalAccessException | InstantiationException | ClassNotFoundException | NoSuchMethodException | ParserConfigurationException | SAXException e) {
                LOGGER.log(Level.SEVERE,e.getMessage(),e);
            }
        }
    }

    private static String fq(String api, String pid) {
        switch(api) {
            case "v7": return "root.pid:\""+pid+"\""; 
            case "v5": return "root_pid:\""+pid+"\""; 
        }
        return "root_pid:\""+pid+"\""; 
    }

    private static String renderTemplate(boolean pbl, String api, Map<String,String> iteration, Map<String,String> destination) throws IOException {
        String templatePath = String.format("reharvest_%s.xml", api);
        if (pbl) {
            templatePath = String.format("reharvest_public_%s.xml", api);
        }
        
        InputStream stream = ReharvestPids.class.getResourceAsStream(templatePath);
        
        StringTemplate template = new StringTemplate(
                org.apache.commons.io.IOUtils.toString(stream, "UTF-8"), DefaultTemplateLexer.class);

        template.setAttribute("iteration", iteration);
        template.setAttribute("check", new HashMap<>());
        template.setAttribute("destination", destination);
        template.setAttribute("timestamp", new HashMap<>());

        String configuration = template.toString();
        return configuration;
    }

    public static String getDestinationUpdateUrl() {
        String searchHost = KConfiguration.getInstance().getSolrSearchHost();
        return searchHost +(searchHost.endsWith("/") ? "" : "/")+"update?commit=true";
    }
    
    public static Pair<String, Boolean> iterationUrl(String acronym) {
        String baseurl = KConfiguration.getInstance().getConfiguration().getString("cdk.collections.sources." + acronym + ".baseurl");
        String api = KConfiguration.getInstance().getConfiguration().getString("cdk.collections.sources." + acronym + ".api");
        boolean channelAccess = KConfiguration.getInstance().getConfiguration().containsKey("cdk.collections.sources." + acronym + ".licenses") ?  KConfiguration.getInstance().getConfiguration().getBoolean("cdk.collections.sources." + acronym + ".licenses") : false;
        String channel = KConfiguration.getInstance().getConfiguration().getString("cdk.collections.sources." + acronym + ".forwardurl");
        if (channelAccess) {
            if (StringUtils.isAnyString(api) && api.toLowerCase().equals("v5")) {
                String retval = channel + (channel.endsWith("/") ? "" : "/") + "api/v5.0/cdk/forward/sync/solr";
                return Pair.of(retval, false);
            } else {
                String retval = channel + (channel.endsWith("/") ? "" : "/") + "api/cdk/v7.0/forward/sync/solr";
                return Pair.of(retval, false);
            }
        } else {
            boolean publicEndpoint = KConfiguration.getInstance().getConfiguration().containsKey("cdk.collections.sources." + acronym + ".public") ?  KConfiguration.getInstance().getConfiguration().getBoolean("cdk.collections.sources." + acronym + ".public") : false;
            if (publicEndpoint) {
                String retval = baseurl + (baseurl.endsWith("/") ? "" : "/") + "api/v5.0";
                return Pair.of(retval, false);
            } else {
                String retval = baseurl + (baseurl.endsWith("/") ? "" : "/") + "api/v4.6/cdk/solr";
                return Pair.of(retval, true);
                
            }
        }
    }
    
    
    private static Document deleteRootPid(SolrAccess solrAccess, String pid) throws ParserConfigurationException, DOMException, IOException {
        Document deleteByQuery = XMLUtils.crateDocument("delete");
        Element queryElement = deleteByQuery.createElement("query");
        queryElement.setTextContent(query(solrAccess, pid));
        deleteByQuery.getDocumentElement().appendChild(queryElement);
        return deleteByQuery;
    }

    private static String query(SolrAccess sAccess, String pid) throws IOException {
        String query = String.format("root.pid:\"%s\"",pid);
        if (query.startsWith("root.pid:\"uuid")) {
            int threshold = KConfiguration.getInstance().getConfiguration().getInt("cdk.reharvest.items.threshold",300);

            Document doc = sAccess.requestWithSelectReturningXml("q="+URLEncoder.encode( query, "UTF-8"));
            int numFound = numFound(doc);
            if (numFound > threshold) {
                throw new IllegalStateException("Too many items to reharvest");
            }
            return query;
        } else throw new IllegalStateException("Pid must start with uuid !");
    }

//    private static Document deleteOwnPidPathSubtree(String pidPath) throws ParserConfigurationException {
//        Document deleteByQuery = XMLUtils.crateDocument("delete");
//        Element queryElement = deleteByQuery.createElement("query");
//        queryElement.setTextContent(String.format("own_pid_path:%s/*", pidPath.replace(":", "\\:")));
//        deleteByQuery.getDocumentElement().appendChild(queryElement);
//        return deleteByQuery;
//    }
    
    
    private static List<String> collections(Document document) {
        Element cdkCollections = XMLUtils.findElement(document.getDocumentElement(), new XMLUtils.ElementsFilter() {
            @Override
            public boolean acceptElement(Element element) {
                String name = element.getAttribute("name");
                return name != null && name.equals("cdk.collection");
            }
        });
        return cdkCollections != null ? XMLUtils.getElements(cdkCollections).stream().map(Element::getTextContent).map(String::trim).collect(Collectors.toList()) : new ArrayList<>();
    }

    private static String leader(Document document) {
        Element cdkLeader = XMLUtils.findElement(document.getDocumentElement(), new XMLUtils.ElementsFilter() {
            @Override
            public boolean acceptElement(Element element) {
                String name = element.getAttribute("name");
                return name != null && name.equals("cdk.leader");
            }
        });
        return cdkLeader != null ? cdkLeader.getTextContent().trim() : null;
    }

    public static int numFound(Document document) {
        Element result = XMLUtils.findElement(document.getDocumentElement(), new XMLUtils.ElementsFilter() {
            @Override
            public boolean acceptElement(Element element) {
                String nodeName = element.getNodeName();
                return nodeName.equals("result");
            }
        });
        
        String nf = result.getAttribute("numFound");
        return Integer.parseInt(nf);
    }
    
    
//    private static String ownPidPath(Document document) {
//        Element cdkLeader = XMLUtils.findElement(document.getDocumentElement(), new XMLUtils.ElementsFilter() {
//            @Override
//            public boolean acceptElement(Element element) {
//                String name = element.getAttribute("name");
//                return name != null && name.equals("own_pid_path");
//            }
//        });
//        return cdkLeader != null ? cdkLeader.getTextContent().trim() : null;
//    }

    private static List<String> extractPids(String target) {
        if (target.startsWith("pid:")) {
            String pid = target.substring("pid:".length());
            List<String> result = new ArrayList<>();
            result.add(pid);
            return result;
        } else if (target.startsWith("pidlist:")) {
            List<String> pids = Arrays.stream(target.substring("pidlist:".length()).split(";")).map(String::trim).filter(s -> !s.isEmpty()).collect(Collectors.toList());
            return pids;
        } else if (target.startsWith("pidlist_file:")) {
            String filePath = target.substring("pidlist_file:".length());
            File file = new File(filePath);
            if (file.exists()) {
                try {
                    return IOUtils.readLines(new FileInputStream(file), Charset.forName("UTF-8"));
                } catch (IOException e) {
                    throw new RuntimeException("IOException " + e.getMessage());
                }
            } else {
                throw new RuntimeException("file " + file.getAbsolutePath() + " doesnt exist ");
            }
        } else {
            throw new RuntimeException("invalid target " + target);
        }
    }
}
