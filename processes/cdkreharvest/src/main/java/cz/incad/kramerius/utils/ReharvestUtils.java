package cz.incad.kramerius.utils;

import static cz.incad.kramerius.services.iterators.solr.SolrCursorIterator.findCursorMark;
import static cz.incad.kramerius.services.iterators.solr.SolrCursorIterator.findQueryCursorMark;
import static cz.incad.kramerius.services.iterators.solr.SolrCursorIterator.pidsCursorQuery;
import static cz.incad.kramerius.services.utils.SolrUtils.findAllPids;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.antlr.stringtemplate.StringTemplate;
import org.antlr.stringtemplate.language.DefaultTemplateLexer;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import com.google.common.io.Files;
import com.sun.jersey.api.client.Client;

import cz.incad.kramerius.KubernetesReharvestProcess;
import cz.incad.kramerius.service.MigrateSolrIndexException;
import cz.incad.kramerius.services.ParallelProcessImpl;
import cz.incad.kramerius.services.utils.SolrUtils;

public class ReharvestUtils {
    
    public static final Logger LOGGER = Logger.getLogger(ReharvestUtils.class.getName());
    
    private ReharvestUtils() {}

    /** Delete all given pids */
    public static void deleteAllGivenPids(Client client,Map<String, String> destinationMap, List<Pair<String,String>> allPidsList, boolean onlyShowConfig)
            throws ParserConfigurationException {
        int batchSize = 1000;
        int batches = allPidsList.size() / batchSize;
        if (allPidsList.size() % batchSize > 0) {
            batches = batches + 1;
        }
        for (int i = 0; i < batches; i++) {
            int min = i * batchSize;
            int max = Math.min((i + 1) * batchSize, allPidsList.size());
            Document deleteBatch = XMLUtils.crateDocument("delete");
            List<Pair<String,String>> batchPids = allPidsList.subList(min, max);
    
            batchPids.forEach(pid -> {
                Element idElm = deleteBatch.createElement("id");
                idElm.setTextContent(pid.getRight().trim());
                deleteBatch.getDocumentElement().appendChild(idElm);
            });
            try {
                if (!onlyShowConfig) {
                    LOGGER.info("Deleting identifiers:"+batchPids);
                    String destinationUrl = destinationMap.get("url")+"/update?commit=true";
                    String s = SolrUtils.sendToDest(destinationUrl, client, deleteBatch);
                } else {
                    StringWriter writer = new StringWriter();
                    XMLUtils.print(deleteBatch, writer);
                    LOGGER.info(writer.toString());
                }
    
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, e.getMessage(), e);
            }
        }
    }
    
    /** Find all pids by given root.pid */
    public static List<Pair<String,String>> findAllPidsByGivenRootPid(Map<String, String> iterationMap, Client client, JSONArray pids) {
        LinkedHashSet<Pair<String,String>> allPids = new LinkedHashSet<>();
        for (int i = 0; i < pids.length(); i++) {
            String pid = pids.getString(i);
            //allPids.add(pid);
            String iterationUrl = iterationMap.get("url");
            
            String masterQuery = "*:*";
            String filterQuery = "root.pid:\"" + pid + "\"";
            try {
                String cursorMark = null;
                String queryCursorMark = null;
                do {
                    
                    Element element = pidsCursorQuery(client, iterationUrl, masterQuery, cursorMark, 3000,
                            filterQuery, "select", "compositeId+pid", "compositeId asc", "", "");
                    cursorMark = findCursorMark(element);
                    queryCursorMark = findQueryCursorMark(element);
                    
                    List<Element> docs = XMLUtils.getElementsRecursive(element, new XMLUtils.ElementsFilter() {
                        
                        @Override
                        public boolean acceptElement(Element element) {
                            return element.getNodeName().equals("doc");
                        }
                    });
                    
                    List<Pair<String,String>> pairs = docs.stream().map(doc-> {
                        
                        Element pidElm = XMLUtils.findElement(doc, new XMLUtils.ElementsFilter() {
                            
                            @Override
                            public boolean acceptElement(Element element) {
                                String name = element.getAttribute("name");
                                return (name != null && name.equals("pid"));
                            }
                        });

                        Element compositeIdElm = XMLUtils.findElement(doc, new XMLUtils.ElementsFilter() {
                            @Override
                            public boolean acceptElement(Element element) {
                                String name = element.getAttribute("name");
                                return (name != null && name.equals("compositeId"));
                            }
                        });
                        
                        
                        if (pidElm != null && compositeIdElm != null ) {
                            return Pair.of(pidElm.getTextContent(), compositeIdElm.getTextContent());
                        } else return null;
                    }).filter(x -> x != null).collect(Collectors.toList());
                    
                    allPids.addAll(pairs);
                    LOGGER.info(String.format( "Collected pids to delete %d", allPids.size()));
                    
                } while ((cursorMark != null && queryCursorMark != null)
                        && !cursorMark.equals(queryCursorMark));
            } catch (ParserConfigurationException | SAXException | IOException e) {
                throw new RuntimeException(e);
            }
        }
        return  new ArrayList<Pair<String,String>>(allPids);
    }

    /** Render template */
    public static String renderTemplate(String api, Map<String,String> iteration, Map<String,String> destination) throws IOException {
        String templatePath = String.format("reharvest_%s.xml", api);
        
        InputStream stream = KubernetesReharvestProcess.class.getResourceAsStream(templatePath);
        
        StringTemplate template = new StringTemplate(
                org.apache.commons.io.IOUtils.toString(stream, "UTF-8"), DefaultTemplateLexer.class);
    
        template.setAttribute("iteration", iteration);
        template.setAttribute("check", new HashMap<>());
        template.setAttribute("destination", destination);
        template.setAttribute("timestamp", new HashMap<>());
    
        String configuration = template.toString();
        return configuration;
    }

    public static String fq(String api, String pid) {
        switch(api) {
            case "v7": return "root.pid:\""+pid+"\""; 
            case "v5": return "root_pid:\""+pid+"\""; 
        }
        return "root_pid:\""+pid+"\""; 
    }

    public static void reharvestPIDFromGivenCollections(String pid, Map<String,JSONObject> collectionConfigurations, String onlyShowConfiguration, Map<String, String> destinationMap, Map<String,String> iterationMap) throws IllegalAccessException, InstantiationException, ClassNotFoundException, NoSuchMethodException, MigrateSolrIndexException, IOException, ParserConfigurationException, SAXException {
        List<File> harvestFiles = new ArrayList<>();
        for (String ac : collectionConfigurations.keySet()) {
            try {
                JSONObject colObject = collectionConfigurations.get(ac);
                String apiVersion = colObject.optString("api","v5");
                if (!colObject.has("forwardurl")) {
                    LOGGER.severe(String.format("Skipping %s", ac));
                    continue;
                }
                String channel = colObject.optString("forwardurl");

                
                Map<String,String> iteration = new HashMap<>(iterationMap);
                //http://mzk-tunnel.cdk-proxy.svc.cluster.local/search"
                //http://knav-tunnel.cdk-proxy.svc.cluster.local/search/api/v5.0/cdk/forward/sync/solr
                //v7.0
                //search/api/cdk/v7.0/forward/sync/solr/
                if (apiVersion.toLowerCase().equals("v5")) {
                    //channel = 
                    iteration.put("url", channel+(channel.endsWith("/") ? "" : "/")+"api/v5.0/cdk/forward/sync/solr");
                } else {
                    iteration.put("url", channel+(channel.endsWith("/") ? "" : "/")+"api/cdk/v7.0/forward/sync/solr");
                }
                
                
                iteration.put("dl", ac);
                iteration.put("fquery", fq(apiVersion, pid));
    
                String configuration = renderTemplate( apiVersion, iteration, destinationMap);
                File tmpFile = File.createTempFile(String.format("%s",  ac), "reharvest");
                
                Files.write(configuration.getBytes("UTF-8"), tmpFile);
                harvestFiles.add(tmpFile);
                
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE,e.getMessage(),e);
            }
        }
        
        for (File harvestFile : harvestFiles) {
            if (ReharvestUtils.isOnlyShowConfiguration(onlyShowConfiguration)) {
                String config = org.apache.commons.io.IOUtils.toString(new FileInputStream(harvestFile), "UTF-8");
                LOGGER.info(String.format("Configuration %s" ,config));
            } else {
                // safra ?? 
                try {
                    ParallelProcessImpl reharvest = new ParallelProcessImpl();
                    String config = org.apache.commons.io.IOUtils.toString(new FileInputStream(harvestFile), "UTF-8");
                    LOGGER.info(String.format("Configuration %s" ,config));
                    reharvest.migrate(harvestFile);
                } catch (IllegalAccessException | InstantiationException | ClassNotFoundException
                        | NoSuchMethodException | MigrateSolrIndexException | IOException | ParserConfigurationException
                        | SAXException e) {
                    LOGGER.log(Level.SEVERE,e.getMessage(),e);
                }
            }
        }
    }

    public static boolean isOnlyShowConfiguration(String onlyShowConfiguration) {
        if (onlyShowConfiguration != null && ("onlyshowconfiguration".equals(onlyShowConfiguration.toLowerCase()) || "true".equals(onlyShowConfiguration))) {
            return true;
        }
        return false;
    }
    
    
}
