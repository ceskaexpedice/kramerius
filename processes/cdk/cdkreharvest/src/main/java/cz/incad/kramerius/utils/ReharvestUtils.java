package cz.incad.kramerius.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.ws.rs.core.MediaType;
import javax.xml.parsers.ParserConfigurationException;

import cz.inovatika.kramerius.services.iterators.config.TypeOfIteration;
import org.antlr.stringtemplate.StringTemplate;
import org.antlr.stringtemplate.language.DefaultTemplateLexer;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.core5.http.HttpEntity;
import org.json.JSONObject;
import org.json.JSONArray;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import com.google.common.io.Files;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

import cz.incad.kramerius.KubernetesReharvestProcess;
import cz.incad.kramerius.TooBigException;
import cz.incad.kramerius.cdk.ChannelUtils;
import cz.incad.kramerius.rest.apiNew.admin.v70.reharvest.ReharvestItem;
import cz.incad.kramerius.service.MigrateSolrIndexException;
import cz.incad.kramerius.utils.conf.KConfiguration;
import cz.inovatika.kramerius.services.Migration;
import cz.inovatika.kramerius.services.iterators.IterationItem;
import cz.inovatika.kramerius.services.iterators.MigrationIterator;
import cz.inovatika.kramerius.services.iterators.solr.SolrCursorIterator;
import cz.inovatika.kramerius.services.iterators.solr.SolrFilterQueryIterator;
import cz.inovatika.kramerius.services.iterators.solr.SolrPageIterator;
import cz.inovatika.kramerius.services.iterators.utils.HTTPSolrUtils;

public class ReharvestUtils {
    
    public static final String ITERATION_ROWS_STRING_VALUE = "300";
    public static final long DEFAULT_COMMIT_WAIT_MILLIS = 1000L;
    public static final Logger LOGGER = Logger.getLogger(ReharvestUtils.class.getName());
    
    private ReharvestUtils() {}

    public static long parseCommitWaitMillis(String value) {
        if (org.apache.commons.lang3.StringUtils.isBlank(value)) {
            return DEFAULT_COMMIT_WAIT_MILLIS;
        }
        long parsed = Long.parseLong(value.trim());
        if (parsed < 0) {
            throw new IllegalArgumentException("commitWaitMillis must be zero or greater");
        }
        return parsed;
    }

    /** Delete all given pids */
    public static String deleteAllGivenPids(CloseableHttpClient closeableHttpClient,Map<String, String> destinationMap, List<Pair<String,String>> allPidsList, boolean onlyDisplayCommand)
            throws ParserConfigurationException {
        return deleteAllGivenPids(closeableHttpClient, destinationMap, allPidsList, onlyDisplayCommand,
                DEFAULT_COMMIT_WAIT_MILLIS);
    }

    public static String deleteAllGivenPids(CloseableHttpClient closeableHttpClient, Map<String, String> destinationMap,
            List<Pair<String, String>> allPidsList, boolean onlyDisplayCommand, long commitWaitMillis)
            throws ParserConfigurationException {
        StringBuilder retBuilder = new StringBuilder();
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
                if (!onlyDisplayCommand) {
                    StringBuilder identifiersLog = new StringBuilder(String.format(
                            "Deleting identifiers: batch %d/%d (%d PIDs)", i + 1, batches, batchPids.size()));
                    for (Pair<String, String> pid : batchPids) {
                        identifiersLog.append("\n  PID: ").append(pid.getLeft())
                                .append(" | compositeId: ").append(pid.getRight());
                    }
                    LOGGER.info(identifiersLog.toString());
                    String destinationUrl = destinationMap.get("url")+"/update";
                    String s = HTTPSolrUtils.sendToDest(destinationUrl, closeableHttpClient, deleteBatch);
                    retBuilder.append(s).append("\n");
                } else {
                    StringWriter writer = new StringWriter();
                    XMLUtils.print(deleteBatch, writer);
                    //LOGGER.info(writer.toString());
                    retBuilder.append(writer.toString()).append("\n");
                }
    
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, e.getMessage(), e);
                throw new IllegalStateException(String.format("Failed deleting reharvest batch %d/%d", i + 1, batches), e);
            }
        }
        if (!onlyDisplayCommand) {
            String destinationUrl = destinationMap.get("url").replaceAll("/+$", "") + "/update";
            commitAndWait(closeableHttpClient, destinationUrl, "reharvest deletion", commitWaitMillis);
        }
        return retBuilder.toString();
    }

    /** Mark a queue item closed when its Process Platform execution finishes. */
    public static void closeQueueItem(String itemId) {
        updateQueueItemState(itemId, "closed");
    }

    public static void updateQueueItemState(String itemId, String state) {
        if (itemId == null || itemId.trim().isEmpty()) {
            return;
        }
        String targetState = state == null || state.trim().isEmpty() ? "closed" : state.trim();
        try {
            String reharvest = KConfiguration.getInstance().getSolrReharvestHost();
            JSONObject update = new JSONObject();
            update.put("id", itemId);
            update.put("state", new JSONObject().put("set", targetState));
            WebResource resource = Client.create().resource(reharvest + "/update/json/docs?commit=true");
            String response = resource.accept(MediaType.APPLICATION_JSON)
                    .type(MediaType.APPLICATION_JSON)
                    .entity(update.toString(), MediaType.APPLICATION_JSON)
                    .post(String.class);
            LOGGER.info(String.format("Updated reharvest queue item %s to state %s: %s", itemId, targetState, response));
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Unable to update reharvest queue item " + itemId + " to state " + targetState, e);
        }
    }

    /** Log and validate the association between a process payload and its queue item. */
    public static void logQueueItemAssociation(String itemId, ReharvestItem processItem) {
        if (itemId == null || itemId.trim().isEmpty()) {
            LOGGER.info("CDK reharvest process is not linked to a queue item (no reharvestItemId supplied)");
            return;
        }

        LOGGER.info(String.format("CDK reharvest process for PID %s is linked to queue item %s",
                processItem.getPid(), itemId));
        try {
            String reharvest = KConfiguration.getInstance().getSolrReharvestHost();
            String query = URLEncoder.encode("id:" + itemId, "UTF-8");
            String response = Client.create().resource(reharvest + "/select?q=" + query + "&rows=1&wt=json")
                    .accept(MediaType.APPLICATION_JSON).get(String.class);
            JSONArray docs = new JSONObject(response).getJSONObject("response").getJSONArray("docs");
            if (docs.length() == 0) {
                LOGGER.warning("Linked reharvest queue item was not found: " + itemId);
                return;
            }

            ReharvestItem queueItem = ReharvestItem.fromJSON(docs.getJSONObject(0));
            boolean pidMatches = Objects.equals(processItem.getPid(), queueItem.getPid());
            boolean rootPidMatches = Objects.equals(processItem.getRootPid(), queueItem.getRootPid());
            boolean pathMatches = Objects.equals(processItem.getOwnPidPath(), queueItem.getOwnPidPath());
            boolean typeMatches = processItem.getTypeOfReharvest() == queueItem.getTypeOfReharvest();
            boolean librariesMatch = sortedLibraries(processItem.getLibraries()).equals(sortedLibraries(queueItem.getLibraries()));

            LOGGER.info(String.format(
                    "Queue item %s loaded: pid=%s, root.pid=%s, own_pid_path=%s, type=%s, libraries=%s; " +
                            "matches process payload: pid=%s, root.pid=%s, own_pid_path=%s, type=%s, libraries=%s",
                    itemId, queueItem.getPid(), queueItem.getRootPid(), queueItem.getOwnPidPath(),
                    queueItem.getTypeOfReharvest(), queueItem.getLibraries(),
                    pidMatches, rootPidMatches, pathMatches, typeMatches, librariesMatch));
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Unable to load or compare linked reharvest queue item " + itemId, e);
        }
    }

    private static List<String> sortedLibraries(List<String> libraries) {
        List<String> sorted = libraries == null ? new ArrayList<>() : new ArrayList<>(libraries);
        Collections.sort(sorted);
        return sorted;
    }


    
    /** Find all pids by given root.pid */
    public static List<Pair<String,String>> findPidByType(Map<String, String> iterationMap, CloseableHttpClient client, ReharvestItem item, int maxItems) {
        try {
            String iterationUrl = iterationMap.get("url");
            String masterQuery = "*:*";
            String filterQuery = fq("v7", item, false);

            String sRows = iterationMap.containsKey("rows")   ? iterationMap.get("rows")  : ITERATION_ROWS_STRING_VALUE  ;
            TypeOfIteration typeOfIteration = iterationMap.containsKey("type")  ? TypeOfIteration.valueOf(iterationMap.get("type")) : TypeOfIteration.CURSOR;
            MigrationIterator processIterator = null;
            switch (typeOfIteration) {
                case CURSOR: {
                    processIterator =  new SolrCursorIterator(iterationUrl, masterQuery, filterQuery, "select", "compositeId", "compositeId asc",Integer.parseInt(sRows), null, null);
                    break;
                }
                case FILTER: {
                    processIterator  = new SolrFilterQueryIterator( iterationUrl, masterQuery, filterQuery, "select", "compositeId", "compositeId asc",Integer.parseInt(sRows), null, null);
                    break;
                }
                case PAGINATION: {
                    processIterator = new SolrPageIterator( iterationUrl, masterQuery, filterQuery, "select", "compositeId", "compositeId asc",Integer.parseInt(sRows), null, null);
                    break;
                }
            }
            LOGGER.info(String.format("Solr iterator %s", processIterator.getClass().getName()));
            final LinkedHashSet<Pair<String,String>> retvals  = new LinkedHashSet<>();
            processIterator.iterate(client, (list)-> {
                for (IterationItem it : list) {
                    String compositeId = it.getId();
                    String[] arr = compositeId.split("!");
                    if (arr.length == 2) { 
                        Pair<String,String> pair = Pair.of(arr[1], compositeId);
                        retvals.add(pair); 
                    }
                }
                if (retvals.size()>maxItems) {
                    throw new TooBigException("Reharvest subtree exceeds the configured safety limit",
                            retvals.size(), maxItems);
                }
                
            }, ()->{});
        
            return  new ArrayList<Pair<String,String>>(retvals);
            
        } catch (TooBigException e) {
            throw e; // Vyhození výjimky dál
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    /** Render template */
    public static String renderTemplate(String api, Map<String,String> iteration, Map<String,String> destination) throws IOException {
        String templatePath = "/cz/incad/kramerius/services/workers/replicate/configurations/default_k7_v2.xml";
        InputStream stream = KubernetesReharvestProcess.class.getResourceAsStream(templatePath);
        if (stream == null) {
            throw new IOException("Cannot find configuration template " + templatePath);
        }

        try (InputStream templateStream = stream) {
            StringTemplate template = new StringTemplate(
                    IOUtils.toString(templateStream, "UTF-8"), DefaultTemplateLexer.class);

            template.setAttribute("iteration", iteration);
            template.setAttribute("check", new HashMap<>());
            template.setAttribute("destination", destination);
            template.setAttribute("timestamp", new HashMap<>());
            template.setAttribute("feeder", Map.of("batchsize", iteration.getOrDefault("batch", "45")));
            Map<String, String> comparing = new HashMap<>();
            comparing.put("identifier", iteration.getOrDefault("comparingIdentifier", "pid"));
            template.setAttribute("comparing", comparing);
            template.setAttribute("proxy", new HashMap<>());
            template.setAttribute("reharvest", new HashMap<>());

            return template.toString();
        }
    }

    public static String encode(String val, boolean enc) {
        try {
            if (enc) return URLEncoder.encode(val, "UTF-8");
            return val;
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    public static String fq(String api, ReharvestItem item, boolean encode) throws UnsupportedEncodingException {
        switch(item.getTypeOfReharvest()) {

            case only_pid:
            case delete_pid:
                if (!StringUtils.isAnyString(item.getPid())) {
                    throw  new RuntimeException("expecting  pid ");
                }
                switch(api) {
                    case "v7": return encode("pid:\""+item.getPid()+"\"", encode);
                    case "v5": return encode("PID:\""+item.getPid()+"\"", encode);
                }
                return encode("PID:\""+item.getPid()+"\"",encode);

            case new_root:
            case delete_root:
            case root:
                if (!StringUtils.isAnyString(item.getRootPid())) {
                    throw  new RuntimeException("expecting root pid ");
                }
                switch(api) {
                    case "v7": return encode("root.pid:\""+item.getRootPid()+"\"", encode);
                    case "v5": return encode("root_pid:\""+item.getRootPid()+"\"", encode);
                }
                return encode("root_pid:\""+item.getRootPid()+"\"",encode);

            case new_children:
            case delete_tree:
            case children:
                if (!StringUtils.isAnyString(item.getOwnPidPath())) {
                    throw  new RuntimeException("expecting own pid path ");
                }

                switch(api) {
                    case "v7": {
                        String ownPidPath =  item.getOwnPidPath().replaceAll(":", "\\\\:")+"*";
                        String fq  = encode(String.format("own_pid_path:%s", ownPidPath),encode);;
                        return fq;
                    }
                    case "v5": {
                        String pidPath =  item.getOwnPidPath().replaceAll(":", "\\\\:")+"*";
                        String fq  = encode(String.format("pid_path:%s", pidPath),encode);
                        return fq;
                    }
                }
                return encode("root_pid:\""+item.getPid()+"\"", encode);

        }
        return encode("root_pid:\""+ item.getPid()+"\"",encode);
    }
    public static Map<String, Map<String,String>> findRootsAndPidPathsFromLibs(CloseableHttpClient client, String pid, Map<String,JSONObject> collectionConfigurations) throws UnsupportedEncodingException {
        Map<String,Map<String,String>> retval = new HashMap<>();
        for (String ac : collectionConfigurations.keySet()) {
            JSONObject colObject = collectionConfigurations.get(ac);

            String apiVersion = colObject.optString("api","v5");
            if (!colObject.has("forwardurl")) {
                LOGGER.severe(String.format("Skipping %s", ac));
                continue;
            }
            String channel = colObject.optString("forwardurl");
            String fullChannelUrl = ChannelUtils.solrChannelUrl(apiVersion, channel);
            String pidIdentifier = "pid";
            String rootPid ="root.pid";
            String pidPath = "own_pid_path";
            switch (apiVersion) {
                case "v5":
                    pidIdentifier="PID";
                    rootPid="root_pid";
                    pidPath = "pid_path";
                    break;
                case "v7":
                    pidIdentifier="pid";
                    rootPid="root.pid";
                    pidPath = "own_pid_path";
                    break;
                default:
                    pidIdentifier = "pid";
                    rootPid="root.pid";
                    pidPath = "pid_path";
                    break;
            }

            String query = URLEncoder.encode(String.format("%s:\"%s\"", pidIdentifier,pid), "UTF-8");
            HttpGet get = new HttpGet(fullChannelUrl+ String.format("/select?q=%s&rows=1&wt=xml",query));
            try(CloseableHttpResponse response = client.execute(get)) {
                int code = response.getCode();
                if (code == 200) {
                    HttpEntity entity = response.getEntity();
                    InputStream is = entity.getContent();
                    String str = IOUtils.toString(is, "UTF-8");
                    try {
                        final String finalPidIdentifier = pidIdentifier;
                        final String finalRootPid = rootPid;
                        final String finalPidPath = pidPath;


                        Document parsed = XMLUtils.parseDocument(new StringReader(str));
                        Map<String, String> map = new HashMap<>();

                        Element pidElement = XMLUtils.findElement(parsed.getDocumentElement(), new XMLUtils.ElementsFilter() {
                            @Override
                            public boolean acceptElement(Element element) {
                                String fieldName = element.getAttribute("name");
                                return fieldName.equals(finalPidIdentifier);
                            }
                        });
                        if (pidElement != null) {
                            map.put("pid", pidElement.getTextContent());
                        }

                        Element pidPathElement = XMLUtils.findElement(parsed.getDocumentElement(), new XMLUtils.ElementsFilter() {
                            @Override
                            public boolean acceptElement(Element element) {
                                String fieldName = element.getAttribute("name");
                                return fieldName.equals(finalPidPath);
                            }
                        });
                        if (pidPathElement != null) {
                            List<Element> elms = XMLUtils.getElements(pidPathElement);
                            if (!elms.isEmpty()) {
                                List<String> pids = elms.stream().map(Element::getTextContent).collect(Collectors.toList());
                                if (pids.size() >0 ) {
                                    map.put("own_pid_path", pids.get(0));
                                }
                            } else {
                                map.put("own_pid_path", pidPathElement.getTextContent());
                            }
                        }


                        Element rootPidElm = XMLUtils.findElement(parsed.getDocumentElement(), new XMLUtils.ElementsFilter() {
                            @Override
                            public boolean acceptElement(Element element) {
                                String fieldName = element.getAttribute("name");
                                return fieldName.equals(finalRootPid);
                            }
                        });
                        if (rootPidElm != null) {
                            map.put("root.pid", rootPidElm.getTextContent());
                        }

                        retval.put(ac, map);

                    } catch (DOMException | ParserConfigurationException | SAXException | IOException e) {
                        LOGGER.log(Level.SEVERE,e.getMessage(),e);
                    }


                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return retval;
    }

    public static Map<String, Map<String,String>> findRootsAndPidPathsFromLibs(Client client, String pid, Map<String,JSONObject> collectionConfigurations){
        Map<String,Map<String,String>> retval = new HashMap<>();
        for (String ac : collectionConfigurations.keySet()) {
                JSONObject colObject = collectionConfigurations.get(ac);

                String apiVersion = colObject.optString("api","v5");
                if (!colObject.has("forwardurl")) {
                    LOGGER.severe(String.format("Skipping %s", ac));
                    continue;
                }
                //Map<String,String> iteration = new HashMap<>(iterationMap);
                String channel = colObject.optString("forwardurl");

                String fullChannelUrl = ChannelUtils.solrChannelUrl(apiVersion, channel);
                String pidIdentifier = "pid";
                String rootPid ="root.pid";
                String pidPath = "own_pid_path";
                switch (apiVersion) {
                    case "v5":
                        pidIdentifier="PID";
                        rootPid="root_pid";
                        pidPath = "pid_path";
                        break;
                    case "v7":
                        pidIdentifier="pid";
                        rootPid="root.pid";
                        pidPath = "own_pid_path";
                        break;
                    default:
                        pidIdentifier = "pid";
                        rootPid="root.pid";
                        pidPath = "pid_path";
                        break;
                }    
                
                String query = String.format("%s:\"%s\"", pidIdentifier,pid);
                WebResource solrResource = client.resource(fullChannelUrl+ String.format("/select?q=%s&rows=1&wt=xml",query));
                ClientResponse solrREsp = solrResource.accept(MediaType.APPLICATION_JSON)
                        .get(ClientResponse.class);
                if (solrREsp.getStatus() == ClientResponse.Status.OK.getStatusCode()) {
                    String str = solrREsp.getEntity(String.class);
                    try {
                        
                        final String finalPidIdentifier = pidIdentifier;
                        final String finalRootPid = rootPid;
                        final String finalPidPath = pidPath;
                        
                        
                        Document parsed = XMLUtils.parseDocument(new StringReader(str));
                        Map<String, String> map = new HashMap<>();
                        
                        Element pidElement = XMLUtils.findElement(parsed.getDocumentElement(), new XMLUtils.ElementsFilter() {
                            @Override
                            public boolean acceptElement(Element element) {
                                String fieldName = element.getAttribute("name");
                                return fieldName.equals(finalPidIdentifier);
                            }
                        });
                        if (pidElement != null) {
                            map.put("pid", pidElement.getTextContent());
                        }
                        
                        Element pidPathElement = XMLUtils.findElement(parsed.getDocumentElement(), new XMLUtils.ElementsFilter() {
                            @Override
                            public boolean acceptElement(Element element) {
                                String fieldName = element.getAttribute("name");
                                return fieldName.equals(finalPidPath);
                            }
                        });
                        if (pidPathElement != null) {
                            List<Element> elms = XMLUtils.getElements(pidPathElement);
                            if (!elms.isEmpty()) {
                                List<String> pids = elms.stream().map(Element::getTextContent).collect(Collectors.toList());
                                if (pids.size() >0 ) {
                                    map.put("own_pid_path", pids.get(0));
                                }
                            } else {
                                map.put("own_pid_path", pidPathElement.getTextContent());
                            }
                        }


                        Element rootPidElm = XMLUtils.findElement(parsed.getDocumentElement(), new XMLUtils.ElementsFilter() {
                            @Override
                            public boolean acceptElement(Element element) {
                                String fieldName = element.getAttribute("name");
                                return fieldName.equals(finalRootPid);
                            }
                        });
                        if (rootPidElm != null) {
                            map.put("root.pid", rootPidElm.getTextContent());
                        }
                        
                        retval.put(ac, map);
                        
                    } catch (DOMException | ParserConfigurationException | SAXException | IOException e) {
                        LOGGER.log(Level.SEVERE,e.getMessage(),e);
                    }
                } 
        }
        return retval;
    }
    
    public static void reharvestPIDFromGivenCollections(String pid, Map<String,JSONObject> collectionConfigurations, String onlyShowConfiguration, Map<String, String> destinationMap, Map<String,String> iterationMap, ReharvestItem item) throws IllegalAccessException, InstantiationException, ClassNotFoundException, NoSuchMethodException, MigrateSolrIndexException, IOException, ParserConfigurationException, SAXException {
        reharvestPIDFromGivenCollections(pid, collectionConfigurations, onlyShowConfiguration, destinationMap,
                iterationMap, item, DEFAULT_COMMIT_WAIT_MILLIS);
    }

    public static void reharvestPIDFromGivenCollections(String pid, Map<String,JSONObject> collectionConfigurations,
            String onlyShowConfiguration, Map<String, String> destinationMap, Map<String,String> iterationMap,
            ReharvestItem item, long commitWaitMillis) throws IllegalAccessException, InstantiationException,
            ClassNotFoundException, NoSuchMethodException, MigrateSolrIndexException, IOException,
            ParserConfigurationException, SAXException {
        List<Pair<String, File>> harvestFiles = new ArrayList<>();
        for (String ac : collectionConfigurations.keySet()) {
            try {
                JSONObject colObject = collectionConfigurations.get(ac);

                if (!colObject.has("forwardurl")) {
                    LOGGER.severe(String.format("Skipping %s", ac));
                    continue;
                }
                Map<String,String> iteration = new HashMap<>(iterationMap);
                String channel = colObject.optString("forwardurl");

                String apiKey = KConfiguration.getInstance().getConfiguration()
                        .getString("cdk.collections.sources." + ac + ".apikey");
                if (StringUtils.isAnyString(apiKey)) {
                    iteration.put("apikey", apiKey);
                }

                String fullChannelUrl = ChannelUtils.solrChannelUrl("v7", channel);
                
                iteration.put("url",  fullChannelUrl); 
                String sourcePrefix = "cdk.collections.sources." + ac + ".";
                String iterationId = KConfiguration.getInstance().getConfiguration()
                        .getString(sourcePrefix + "iterationId", "pid");
                iteration.put("id", iterationId);

                iteration.put("dl", ac);
                iteration.put("fquery", fq("v7", item, false));
    
                String configuration = renderTemplate("v7", iteration, destinationMap);
                File tmpFile = File.createTempFile(String.format("%s",  ac), "reharvest");
                
                Files.write(configuration.getBytes("UTF-8"), tmpFile);
                harvestFiles.add(Pair.of(ac, tmpFile));
                
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE,e.getMessage(),e);
                throw new RuntimeException(e);
            }
        }
        
        for (Pair<String, File> harvestEntry : harvestFiles) {
            String library = harvestEntry.getLeft();
            File harvestFile = harvestEntry.getRight();
            if (ReharvestUtils.isOnlyShowConfiguration(onlyShowConfiguration)) {
                String config = org.apache.commons.io.IOUtils.toString(new FileInputStream(harvestFile), "UTF-8");
                LOGGER.info(String.format("Configuration %s" ,config));
            } else {
                // safra ?? 
                try {
                    Migration reharvest = new Migration();
                    String config = org.apache.commons.io.IOUtils.toString(new FileInputStream(harvestFile), "UTF-8");
                    LOGGER.info(String.format("Configuration %s" ,config));
                    reharvest.migrate(harvestFile);
                    LOGGER.info("Library harvest and its Solr commit completed for " + library);
                    waitAfterCommit("library harvest " + library, commitWaitMillis);
                } catch (IllegalAccessException | InstantiationException | ClassNotFoundException
                        | NoSuchMethodException | MigrateSolrIndexException | IOException | ParserConfigurationException
                        | SAXException e) {
                    LOGGER.log(Level.SEVERE,e.getMessage(),e);
                    throw new RuntimeException(e);
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

    private static void commitAndWait(CloseableHttpClient client, String destinationUrl, String stage,
            long commitWaitMillis) {
        HTTPSolrUtils.commitApache(client, destinationUrl);
        LOGGER.info("Explicit Solr commit completed after " + stage);
        waitAfterCommit(stage, commitWaitMillis);
    }

    private static void waitAfterCommit(String stage, long commitWaitMillis) {
        if (commitWaitMillis < 0) {
            throw new IllegalArgumentException("commitWaitMillis must be zero or greater");
        }
        if (commitWaitMillis == 0) {
            LOGGER.info("No wait after commit for " + stage);
            return;
        }
        LOGGER.info(String.format("Waiting %d ms after commit for %s", commitWaitMillis, stage));
        try {
            Thread.sleep(commitWaitMillis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while waiting after Solr commit for " + stage, e);
        }
    }
    
    
    public static void main(String[] args) {
        //uuid:440337bd-5625-11e1-9505-005056a60003/uuid:014d9bc6-f65b-4aca-9d69-daf842e20f0f/uuid:1a5a3fb5-e86b-43c1-b22c-91f31b4c9637
        Client client = Client.create();
        
    }
}
