package cz.incad.kramerius;

import static cz.incad.kramerius.services.iterators.utils.IterationUtils.pidsToIterationItem;
import static cz.incad.kramerius.services.utils.SolrUtils.findAllPids;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.core.MediaType;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.antlr.stringtemplate.StringTemplate;
import org.antlr.stringtemplate.language.DefaultTemplateLexer;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.json.JSONConfiguration;

import cz.incad.kramerius.services.KubernetesProcessImpl;
import cz.incad.kramerius.services.ParallelProcessImpl;
import cz.incad.kramerius.services.utils.kubernetes.KubernetesEnvSupport;
import cz.incad.kramerius.utils.XMLUtils;

import static cz.incad.kramerius.services.iterators.solr.SolrCursorIterator.*;


public class KubernetesReharvestProcess {

    public static final Logger LOGGER = Logger.getLogger(KubernetesReharvestProcess.class.getName());
    
    
    protected static Client buildClient() {
        //Client client = Client.create();
        ClientConfig cc = new DefaultClientConfig();
        cc.getProperties().put(
                ClientConfig.PROPERTY_FOLLOW_REDIRECTS, true);
        cc.getFeatures().put(JSONConfiguration.FEATURE_POJO_MAPPING, Boolean.TRUE);
        return Client.create(cc);
    }   

    
    public static void main(String[] args) throws UnsupportedEncodingException, ParserConfigurationException {
            
        TimeZone.setDefault(TimeZone.getTimeZone("Europe/Prague"));

        Map<String, String> env = System.getenv();
        Map<String, String> iterationMap = KubernetesEnvSupport.iterationMap(env);
        Map<String, String> proxyMap = KubernetesEnvSupport.reharvestMap(env);
        if (proxyMap.containsKey("url")) {
            Client client= buildClient();
            String wurl = proxyMap.get("url");
            if (!wurl.endsWith("/")) {
                wurl = wurl +"/";
            }
            WebResource r = client.resource(wurl + "top");
            
            ClientResponse clientResponse = r.accept(MediaType.APPLICATION_JSON).get(ClientResponse.class);
            if (clientResponse.getStatus() == ClientResponse.Status.OK.getStatusCode()) {

                String t = clientResponse.getEntity(String.class);

                JSONObject itemObject = new JSONObject(t);
                String id = itemObject.getString("id");

                LinkedHashSet<String> allPids = new LinkedHashSet<>();
                JSONArray pids = itemObject.getJSONArray("pids");
                for (int i = 0; i < pids.length(); i++) {
                    String pid = pids.getString(i);
                    allPids.add(pid);
                    String iterationUrl = iterationMap.get("url");
                    String masterQuery = "*:*";
                    String filterQuery = "root.pid:\""+pid+"\"";
                    try {
                        String cursorMark = null;
                        String queryCursorMark = null;
                        do {
                            Element element =  pidsCursorQuery(client, iterationUrl, masterQuery, cursorMark, 100, filterQuery, "select", "compositeId+pid", "compositeId asc", "", "");
                            cursorMark = findCursorMark(element);
                            queryCursorMark = findQueryCursorMark(element);
                            allPids.addAll(findAllPids(element));
                        } while((cursorMark != null && queryCursorMark != null) && !cursorMark.equals(queryCursorMark));
                    } catch (ParserConfigurationException  | SAXException | IOException e) {
                        throw new RuntimeException(e);
                    }
                }
                
                //System.out.println("Number of pids "+allPids.size());
                List<String> allPidsList  = new ArrayList<>();
                allPidsList.addAll(allPids);
                allPidsList.sort(String::compareTo);
                
                int batchSize = 42;
                int batches = allPidsList.size() / batchSize;
                if (allPidsList.size() % batchSize > 0) {
                    batches = batches + 1;
                }
                
                for (int i = 0; i < batches; i++) {
                    int min = i* batchSize;
                    int max = Math.min((i+1)*batchSize, allPidsList.size());
                    
                    Document deleteBatch = XMLUtils.crateDocument("delete");
                    List<String> batchPids = allPidsList.subList(min, max);
                    
                    batchPids.forEach(pid-> {
                        Element idElm = deleteBatch.createElement("id");
                        idElm.setTextContent(pid.trim());
                        deleteBatch.getDocumentElement().appendChild(idElm);
                    }) ;                   
                    
                    try {
                        StringWriter writer = new StringWriter();
                        XMLUtils.print(deleteBatch, writer);
                        System.out.println(writer.toString());
                    } catch (TransformerException e) {
                        e.printStackTrace();
                    }
                }
                
            } else if (clientResponse.getStatus() == ClientResponse.Status.NOT_FOUND.getStatusCode()) {
                LOGGER.log(Level.SEVERE,"No item for reharvest");
            } else {
                throw new UniformInterfaceException(clientResponse);
            }

            
//          Map<String, String> iteration = KubernetesEnvSupport.iterationMap(env);
//          Map<String, String> check = KubernetesEnvSupport.checkMap(env);
//          Map<String, String> destination = KubernetesEnvSupport.destinationMap(env);
//          Map<String, String> timestamps = KubernetesEnvSupport.timestampMap(env, destination);
//
//          StringTemplate template = new StringTemplate(
//                  IOUtils.toString(stream, "UTF-8"), DefaultTemplateLexer.class);

        }
        
        
        //        
//        if (env.containsKey(KubernetesEnvSupport.CONFIG_SOURCE) || args.length > 0) {
//            String configSource = env.containsKey(KubernetesEnvSupport.CONFIG_SOURCE) ?  env.get(KubernetesEnvSupport.CONFIG_SOURCE) : args[0];
//            InputStream stream = KubernetesProcessImpl.class.getResourceAsStream(configSource);
//            if (configSource.trim().startsWith("file:///")) {
//                URL fileUrl = new URL(configSource);
//                stream = fileUrl.openStream();
//            } 
//            if (stream != null) {
//                
//                Map<String, String> iteration = KubernetesEnvSupport.iterationMap(env);
//                Map<String, String> check = KubernetesEnvSupport.checkMap(env);
//                Map<String, String> destination = KubernetesEnvSupport.destinationMap(env);
//                Map<String, String> timestamps = KubernetesEnvSupport.timestampMap(env, destination);
//
//                StringTemplate template = new StringTemplate(
//                        IOUtils.toString(stream, "UTF-8"), DefaultTemplateLexer.class);
//
//                template.setAttribute("iteration", iteration);
//                template.setAttribute("check", check);
//                template.setAttribute("destination", destination);
//                template.setAttribute("timestamp", timestamps);
//
//                String configuration = template.toString();
//                LOGGER.info("Loading configuration "+configuration);
//
//                File tmpFile  = File.createTempFile("temp", "file");
//                FileUtils.write(tmpFile, configuration, "UTF-8");
//
//
//                if (!env.containsKey(ONLY_SHOW_CONFIGURATION)) {
//                    ParallelProcessImpl migr = new ParallelProcessImpl();
//                    migr.migrate(tmpFile);
//                }
//
//            } else {
//                LOGGER.severe(String.format("Cannot find resource %s", configSource));
//            }
//        }
    }
    
}
