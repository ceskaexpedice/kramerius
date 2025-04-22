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
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.ws.rs.core.MediaType;
import javax.xml.parsers.ParserConfigurationException;

import org.antlr.stringtemplate.StringTemplate;
import org.antlr.stringtemplate.language.DefaultTemplateLexer;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.core5.http.HttpEntity;
import org.json.JSONObject;
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
import cz.incad.kramerius.services.ParallelProcessImpl;
import cz.incad.kramerius.services.iterators.IterationItem;
import cz.incad.kramerius.services.iterators.ProcessIterator;
import cz.incad.kramerius.services.iterators.solr.SolrCursorIterator;
import cz.incad.kramerius.services.iterators.solr.SolrFilterQueryIterator;
import cz.incad.kramerius.services.iterators.solr.SolrPageIterator;
import cz.incad.kramerius.services.iterators.solr.SolrIteratorFactory.TypeOfIteration;
import cz.incad.kramerius.services.utils.KubernetesSolrUtils;

public class ReharvestUtils {
    
    public static final String ITERATION_ROWS_STRING_VALUE = "300";
    public static final Logger LOGGER = Logger.getLogger(ReharvestUtils.class.getName());
    
    private ReharvestUtils() {}

    /** Delete all given pids */
    public static String deleteAllGivenPids(CloseableHttpClient closeableHttpClient,Map<String, String> destinationMap, List<Pair<String,String>> allPidsList, boolean onlyDisplayCommand)
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
                    LOGGER.info(String.format("Deleting identifiers (%d):%s", batchPids.size(), batchPids.toString()));
                    String destinationUrl = destinationMap.get("url")+"/update?commit=true";
                    String s = KubernetesSolrUtils.sendToDest(destinationUrl, closeableHttpClient, deleteBatch);
                    retBuilder.append(s).append("\n");
                } else {
                    StringWriter writer = new StringWriter();
                    XMLUtils.print(deleteBatch, writer);
                    LOGGER.info(writer.toString());
                    retBuilder.append(writer.toString()).append("\n");
                }
    
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, e.getMessage(), e);
            }
        }
        return retBuilder.toString();
    }


    
    /** Find all pids by given root.pid */
    public static List<Pair<String,String>> findPidByType(Map<String, String> iterationMap, CloseableHttpClient client, ReharvestItem item, int maxItems) {
        try {
            String iterationUrl = iterationMap.get("url");
            String masterQuery = "*:*";
            String filterQuery = fq("v7", item, false);

            String sRows = iterationMap.containsKey("rows")   ? iterationMap.get("rows")  : ITERATION_ROWS_STRING_VALUE  ;
            TypeOfIteration typeOfIteration = iterationMap.containsKey("type")  ? TypeOfIteration.valueOf(iterationMap.get("type")) : TypeOfIteration.CURSOR;
            ProcessIterator processIterator = null;
            switch (typeOfIteration) {
                case CURSOR: {
                    processIterator =  new SolrCursorIterator(iterationUrl, masterQuery, filterQuery, "select", "compositeId", "compositeId asc",Integer.parseInt(sRows));
                    break;
                }
                case FILTER: {
                    processIterator  = new SolrFilterQueryIterator( iterationUrl, masterQuery, filterQuery, "select", "compositeId", "compositeId asc",Integer.parseInt(sRows));
                    break;
                }
                case PAGINATION: {
                    processIterator = new SolrPageIterator( iterationUrl, masterQuery, filterQuery, "select", "compositeId", "compositeId asc",Integer.parseInt(sRows));
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
                    throw new TooBigException("too_big",retvals.size());
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
                        final String finalPidPath = rootPid;


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
                                return fieldName.equals(finalRootPid);
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
                                return fieldName.equals(finalPidPath);
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
                        final String finalPidPath = rootPid;
                        
                        
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
                                return fieldName.equals(finalRootPid);
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
                                return fieldName.equals(finalPidPath);
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
        List<File> harvestFiles = new ArrayList<>();
        for (String ac : collectionConfigurations.keySet()) {
            try {
                JSONObject colObject = collectionConfigurations.get(ac);

                String apiVersion = colObject.optString("api","v5");
                if (!colObject.has("forwardurl")) {
                    LOGGER.severe(String.format("Skipping %s", ac));
                    continue;
                }
                Map<String,String> iteration = new HashMap<>(iterationMap);
                String channel = colObject.optString("forwardurl");

                String fullChannelUrl = ChannelUtils.solrChannelUrl(apiVersion, channel);
                
                iteration.put("url",  fullChannelUrl); 
                boolean cloud = colObject.optBoolean("solrcloud", false);
                if (cloud) {
                    iteration.put("id", "compositeId");
                } else {
                    if (apiVersion.toLowerCase().equals("v5")) {
                        iteration.put("id", "PID");
                    } else {
                        iteration.put("id", "pid");
                    }
                }

                iteration.put("dl", ac);
                iteration.put("fquery", fq(apiVersion, item, false));
    
                String configuration = renderTemplate( apiVersion, iteration, destinationMap);
                File tmpFile = File.createTempFile(String.format("%s",  ac), "reharvest");
                
                Files.write(configuration.getBytes("UTF-8"), tmpFile);
                harvestFiles.add(tmpFile);
                
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE,e.getMessage(),e);
                throw new RuntimeException(e);
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
    
    
    public static void main(String[] args) {
        //uuid:440337bd-5625-11e1-9505-005056a60003/uuid:014d9bc6-f65b-4aca-9d69-daf842e20f0f/uuid:1a5a3fb5-e86b-43c1-b22c-91f31b4c9637
        Client client = Client.create();
        
    }
}
