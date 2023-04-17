package cz.inovatika.sdnnt;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.request.UpdateRequest;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.SolrInputField;
import org.json.JSONArray;
import org.json.JSONObject;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

import cz.incad.kramerius.utils.conf.KConfiguration;
import cz.inovatika.sdnnt.utils.SDNNTCheckUtils;
import cz.kramerius.searchIndex.indexer.SolrConfig;

public class SDNNTFetch {
    
    //public static final SyncConfig KNAV = new SyncConfig("https://kramerius.lib.cas.cz/search/" , "v5", "knav");
    
    public static enum SyncActionEnum {
        
        add_dnnto(0), 
        add_dnntt(1), 
        remove_dnnto(2), 
        remove_dnntt(3), 
        change_dnnto_dnntt(4), 
        change_dnntt_dnnto(5), 
        partial_change(6);
        
        private int sortValue = 0;

        private SyncActionEnum(int sortValue) {
            this.sortValue = sortValue;
        }
        
        public Integer getValue() {
            return new Integer(this.sortValue);
        }
    }
    
    
    private static final SimpleDateFormat S_DATE_FORMAT = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");

    public static final Logger LOGGER = Logger.getLogger(SDNNTFetch.class.getName());

    public static void main(String[] args) throws IOException, InterruptedException, SolrServerException {
        System.setProperty("solr.cloud.client.stallTime", "119999");

        String sdnntHost  = KConfiguration.getInstance().getConfiguration().getString("solrSdnntHost");
        if (sdnntHost == null) {
            throw new IllegalStateException("Missing configuration key 'solrSdnntHost'");
        }
        
        String[] splitted = sdnntHost.split("/");
        String collection = splitted.length > 0 ? splitted[splitted.length -1] : null;
        if (collection != null) {
            int index = sdnntHost.indexOf(collection);
            if (index > -1) { sdnntHost = sdnntHost.substring(0, index); }
        }
        HttpSolrClient client = new HttpSolrClient.Builder(sdnntHost).build();
        try {
            process(client, new SyncConfig());
        } finally {
            client.close();
        }
    }



    public static void process(HttpSolrClient client, SyncConfig config) throws IOException, InterruptedException, SolrServerException {
            long start = System.currentTimeMillis();
            
            Map<String,String> pids2idents = new HashMap<>();
            
            LOGGER.info("Connecting sdnnt list and iterating serials ");
            iterateSDNNTFormat(client, config, config.getSdnntEndpoint(),  "SE", start, pids2idents);
            LOGGER.info("Connecting sdnnt list and iterating books ");
            iterateSDNNTFormat(client, config, config.getSdnntEndpoint(),  "BK", start, pids2idents);

            long stop = System.currentTimeMillis();
            LOGGER.info("List fetched. It took " + (stop - start) + " ms");
            
            OffsetDateTime offsetDateTime = OffsetDateTime.ofInstant(new Date(start).toInstant(), ZoneId.systemDefault());
            String format = DateTimeFormatter.ISO_INSTANT.format(offsetDateTime);
            client.deleteByQuery(config.getSyncCollection(), String.format("fetched:[* TO %s-1MINUTE] AND type:(main OR granularity)", format));
            
            if (config.getSyncCollection() != null) client.commit(config.getSyncCollection());

            if (config.getBaseUrl() != null) {
                LicenseAPIFetcher apiFetcher = LicenseAPIFetcher.Versions.valueOf(config.getVersion()).build(config.getBaseUrl(), config.getVersion());
                Map<String, List<String>> licenses = apiFetcher.check(pids2idents.keySet());
                List<String> fetchedPids = new ArrayList<>(licenses.keySet());
                
                int setsize = licenses.size();
                int batchsize = 5000;
                int numberofiteration = setsize / batchsize;

                List<String> allChangedIds = new ArrayList<>(100000);
                Map<String, SolrInputDocument> allChanges = new HashMap<>(100000);

                if (setsize % batchsize != 0) numberofiteration = numberofiteration + 1;
                for (int i = 0; i < numberofiteration; i++) {
                  
                  List<String> batchChangedIds = new ArrayList<>();
                    
                  int from = i*batchsize;
                  int to = Math.min((i+1)*batchsize, setsize);
                  List<String> subList = fetchedPids.subList(from, to);

                  for (int j = 0; j < subList.size(); j++) {
                      String pid = subList.get(j);
                      String ident = pids2idents.get(pid);
                      if (ident != null) {
                          SolrInputDocument idoc =  null;
                          if (allChanges.containsKey(ident)) {
                              idoc = allChanges.get(ident.toString());
                          } else {
                              idoc = new SolrInputDocument();
                              idoc.setField("id", ident);
                          }
                          
                          List<String> pidLicenses = licenses.get(pid);
                          if (pidLicenses != null && !pidLicenses.isEmpty()) {
                              
                              //LOGGER.info("Updating document "+ident);
                              for (String lic : pidLicenses) {
                                  atomicAddDistinct(idoc, lic, "real_kram_licenses");
                              }
                          }
                          atomicSet(idoc, true, "real_kram_exists");
                          if (!allChangedIds.contains(ident.toString())) {
                              allChangedIds.add(ident);
                              batchChangedIds.add(ident);
                              allChanges.put(ident, idoc);
                          }
                      }
                  }
                  
                  //int getSize = 100;
                  List<String> pids = subList.stream().map(pids2idents::get).collect(Collectors.toList());
                  String collection = config.getSyncCollection();
                  SolrDocumentList list = getById(client, pids, collection);
                  for (SolrDocument rDoc : list) {
                    Object ident = rDoc.getFieldValue("id");
                    SolrInputDocument in = allChanges.get(ident.toString());

                    Collection<Object> fieldValues = in.getFieldValues("real_kram_licenses");
                    List<String> docLicenses = fieldValues != null ? fieldValues.stream()
                            .map(obj-> {
                                Map<String,String> m = (Map<String, String>) obj;
                                return m.get("add-distinct");
                            }).collect(Collectors.toList()) : new ArrayList<>();

                    
                    Collection<Object> syncMasterActions = in.getFieldValues("sync_actions");
                    List<String> masterActions = syncMasterActions != null ? syncMasterActions.stream()
                            .map(obj-> {
                                Map<String,String> m = (Map<String, String>) obj;
                                return m.get("add-distinct");
                            }).collect(Collectors.toList()) : new ArrayList<>();

                    Object type = rDoc.getFieldValue("type");
                    //Object hasGranularityField = rDoc.getFieldValue("has_granularity");

                    boolean hasGranularity = rDoc.getFieldValue("has_granularity") != null ? (boolean) rDoc.getFieldValue("has_granularity") : false;
                    boolean granularityItem = type != null ? type.toString().equals("granularity") : false;
                    boolean dirty = false;

                    Object rDocState = rDoc.getFieldValue("state");
                    if (rDocState!= null &&  rDocState.toString().equals("A")) {
                        // polozka granularity nebo samostatny titul
                        if (granularityItem || !hasGranularity) {
                            Object license = rDoc.getFieldValue("license");
                            if (license != null) {
                                if (license.toString().equals("dnntt") && !docLicenses.contains("dnntt")) {
                                    if (docLicenses.contains("dnnto")) {
                                        atomicAddDistinct(in, SyncActionEnum.change_dnnto_dnntt.name(), "sync_actions");
                                        atomicSet(in, SyncActionEnum.change_dnnto_dnntt.getValue(), "sync_sort");
                                        dirty = true;
                                    } else {
                                        atomicAddDistinct(in, SyncActionEnum.add_dnntt.name(), "sync_actions");
                                        atomicSet(in, SyncActionEnum.add_dnntt.getValue(), "sync_sort");
                                        dirty = true;
                                    }
                                }

                                if (license.toString().equals("dnnto") && !docLicenses.contains("dnnto")) {
                                    if (docLicenses.contains("dnntt")) {
                                        atomicAddDistinct(in, SyncActionEnum.change_dnnto_dnntt.name() /*"change_dnnto_dnntt"*/, "sync_actions");
                                        atomicSet(in, SyncActionEnum.change_dnnto_dnntt.getValue() /*"change_dnnto_dnntt"*/, "sync_sort");
                                        dirty = true;
                                    } else {
                                        atomicAddDistinct(in, SyncActionEnum.add_dnnto.name() /*"add_dnnto"*/, "sync_actions");
                                        atomicSet(in, SyncActionEnum.add_dnnto.getValue() /*"add_dnnto"*/, "sync_sort");
                                        dirty = true;
                                    }
                                }
                            }
                        }
                    } else {
                        if (granularityItem || !hasGranularity) {
                            if (docLicenses.contains("dnntt")) {
                                atomicAddDistinct(in, SyncActionEnum.remove_dnntt.name() /*"remove_dnntt"*/, "sync_actions");
                                atomicSet(in, SyncActionEnum.remove_dnntt.getValue() /*"remove_dnntt"*/, "sync_sort");
                                dirty = true;
                            }
                            if (docLicenses.contains("dnnto")) {
                                atomicAddDistinct(in, SyncActionEnum.remove_dnnto.name() /*"remove_dnnto"*/, "sync_actions");
                                atomicSet(in, SyncActionEnum.remove_dnnto.getValue() /*"remove_dnnto"*/, "sync_sort");
                                dirty = true;
                            }
                        }
                    }
                    
                    // ja vim ze mam polozku granulairity ... tak menim parenta 
                    if (dirty && granularityItem) {
                        Object field = rDoc.getFieldValue("parent_id"); 
                        if (field!= null) {
                            if (allChangedIds.contains(field.toString())) {
                                SolrInputDocument masterIn = allChanges.get(field.toString());
                                // pozmenime
                                Collection<Object> masterInSyncActions = masterIn.getFieldValues("sync_actions");
                                List<String> actions = masterInSyncActions != null ? masterInSyncActions.stream()
                                        .map(obj-> {
                                            Map<String,String> m = (Map<String, String>) obj;
                                            return m.get("add-distinct");
                                        }).collect(Collectors.toList()) : new ArrayList<>();
                                if (!actions.contains(SyncActionEnum.partial_change.name())) {
                                    atomicAddDistinct(masterIn, SyncActionEnum.partial_change.name(), "sync_actions");
                                    atomicSet(masterIn,  SyncActionEnum.partial_change.getValue() /* "partial_change"*/, "sync_sort");
                                }
                                //if (fieldValue.con)
                            } else {
                                SolrInputDocument masterIn = new SolrInputDocument();
                                masterIn.setField("id", field.toString());
                                atomicAddDistinct(masterIn, SyncActionEnum.partial_change.name(), "sync_actions");
                                atomicSet(masterIn,  SyncActionEnum.partial_change.getValue() /* "partial_change"*/, "sync_sort");
                                allChangedIds.add(field.toString());
                                batchChangedIds.add(field.toString());
                                allChanges.put(field.toString(), masterIn);
                            }
                        }
                    }
                  }
                  
                  
                  if (!allChangedIds.isEmpty()) {
                      UpdateRequest req = new UpdateRequest();
                      batchChangedIds.forEach(ident-> {
                          req.add(allChanges.get(ident));
                      });
                      LOGGER.info(String.format("Update batch with size %s",  req.getDocuments().size()));
                      try {
                          UpdateResponse response = req.process(client, config.getSyncCollection());
                          LOGGER.info("qtime:"+response.getQTime());
                          if (config.getSyncCollection() != null) client.commit(config.getSyncCollection());
                      } catch (SolrServerException  | IOException e) {
                          LOGGER.log(Level.SEVERE,e.getMessage());
                      }
                  }
              }
              
            }
    }



    private static SolrDocumentList getById(HttpSolrClient client, List<String> pids, String collection)
            throws SolrServerException, IOException {
        SolrDocumentList list = new SolrDocumentList();
        int getBatch = 100;
        int numberOfBatch = pids.size() / getBatch;
        numberOfBatch = numberOfBatch + (pids.size() % getBatch == 0 ? 0 : 1);
        for (int i = 0; i < numberOfBatch; i++) {
            int from = i*getBatch;
            int to = Math.min((i+1)*getBatch, pids.size());
            List<String> subPids = pids.subList(from, to);
            list.addAll(client.getById(collection, subPids));
        }
        return list;
    }

    public static void atomicSet(SolrInputDocument idoc, Object fValue, String fName) {
        Map<String, Object> modifier = new HashMap<>(1);
        modifier.put("set", fValue);
        idoc.addField(fName, modifier);
    }

    public static void atomicAddDistinct(SolrInputDocument idoc, Object fValue, String fName) {
        Map<String, Object> modifier = new HashMap<>(1);
        modifier.put("add-distinct", fValue);
        idoc.addField(fName, modifier);
    }


    
    public static File throttle(Client client,  String url) throws IOException, InterruptedException {

        int max_repetion = 3;
        int seconds = 5;

        for (int i = 0; i < max_repetion; i++) {
            WebResource r = client.resource(url);
            ClientResponse response = r.accept(MediaType.APPLICATION_JSON).type(MediaType.APPLICATION_JSON).get(ClientResponse.class);
            int status = response.getStatus();
            if (status == Status.OK.getStatusCode()) {
                String entity = response.getEntity(String.class);
                File tmpFile = File.createTempFile("sdnnt", "resp");
                tmpFile.deleteOnExit();
                IOUtils.write(entity.getBytes(Charset.forName("UTF-8")), new FileOutputStream(tmpFile));
                return tmpFile;
            } else if (status == 409) {
                // wait
                int sleep = KConfiguration.getInstance().getConfiguration().getInt("sdnnt.throttle.wait", 720000);
                LOGGER.info("Server is too busy; waiting for "+(sleep/1000/60)+" min");
                Thread.sleep(sleep);
            }
        }
        throw new IllegalStateException("Maximum number of waiting exceeed");
    }
    
    
    
    private static void iterateSDNNTFormat(
            HttpSolrClient client,
            SyncConfig config, 
            String sdnntChangesEndpoint, 
            String format, 
            long startProcess,
            Map<String,String> pids2oais) throws IOException, InterruptedException, SolrServerException {
        List<SolrInputDocument> docs = new ArrayList<>();
        Map<String, AtomicInteger> counters = new HashMap<>();
        
        int sum = 0;
        String token = "*";
        String prevToken = "";
        Client c = Client.create();
        LOGGER.info(String.format("SDNNT changes endpoint is %s", sdnntChangesEndpoint));
        String sdnntApiEndpoint = sdnntChangesEndpoint + "?format=" + format + "&rows=1000&resumptionToken=%s&digital_library="+config.getAcronym();
        while (token != null && !token.equals(prevToken)) {
            String formatted = String.format(sdnntApiEndpoint, token);
            LOGGER.info("Conctacting sdnnt instance "+format);
            File file = throttle(c, formatted);
            String response = FileUtils.readFileToString(file, Charset.forName("UTF-8"));
            JSONObject resObject = new JSONObject(response);

            prevToken = token;
            token = resObject.optString("resumptiontoken");
            
            JSONArray items = resObject.getJSONArray("items");
            sum = sum+items.length();
            System.out.println("Size :"+items.length() +" and sum:"+(sum));
            for (int i = 0; i < items.length(); i++) {
                
                //List<String> apids = new ArrayList<>();
                
                JSONObject mainObject = items.getJSONObject(i);
                String ident = mainObject.getString("catalog_identifier");
                if (!counters.containsKey(ident)) {
                    counters.put(ident, new AtomicInteger(0));
                }
                counters.get(ident).addAndGet(1);
                
                SolrInputDocument doc = new SolrInputDocument();
                doc.setField("id", ident+"_"+counters.get(ident).get());
                
                doc.setField("catalog", mainObject.getString("catalog_identifier"));
                doc.setField("title", mainObject.getString("title"));
                doc.setField("type_of_rec", mainObject.getString("type"));
                doc.setField("state", mainObject.getString("state"));
                doc.setField("state", mainObject.getString("state"));
                doc.setField("fetched", new Date(startProcess));
                
                
                
                if (mainObject.has("pid")) {
                    doc.setField("pid", mainObject.getString("pid"));
                    pids2oais.put(mainObject.getString("pid"), ident+"_"+counters.get(ident).get());
                }
                
                if (mainObject.has("license")) {
                    doc.setField("license", mainObject.getString("license"));
                }
                doc.setField("type", "main");

                
                if (mainObject.has("granularity")) {
                    
                    JSONArray gr = mainObject.getJSONArray("granularity");
                    doc.setField("has_granularity", new Boolean( gr.length()>0));

                    for (int j = 0; j < gr.length(); j++) {
                        JSONObject item = gr.getJSONObject(j);
                        SolrInputDocument gDod = new SolrInputDocument();
                        gDod.setField("parent_id", ident+"_"+counters.get(ident).get());
                        if (item.has("states")) {
                            Object state = item.get("states");
                            if (state instanceof JSONArray) {
                                JSONArray stateArr = (JSONArray) state;
                                if (stateArr.length() > 0) {
                                    gDod.setField("state", stateArr.getString(0));
                                }
                            } else {
                                gDod.setField("state", state.toString());
                            }
                        }
                        
                        gDod.setField("type", "granularity");
                        
                        if (item.has("pid")) {
                            gDod.setField("pid", item.getString("pid"));
                            pids2oais.put(item.getString("pid"), ident+"_"+counters.get(ident).get()+"_"+item.getString("pid"));
                            gDod.setField("id", ident+"_"+counters.get(ident).get()+"_"+item.getString("pid"));
                        }
                        
                        if (item.has("license")) {
                            gDod.setField("license", item.getString("license"));
                        }
                        gDod.setField("fetched", new Date(startProcess));
                        
                        docs.add(gDod);
                    }
                } else {
                               //"has_granularity"
                    doc.setField("has_granularity", new Boolean(false));
                }
                docs.add(doc);
                
            }
        }
        
        if (docs.size() > 0) {
            int setsize = docs.size();
            int batchsize = 10000;
            int numberofiteration = docs.size() / batchsize;
            if (setsize % batchsize != 0) numberofiteration = numberofiteration + 1;
            for (int i = 0; i < numberofiteration; i++) {
                int from = i*batchsize;
                int to = Math.min((i+1)*batchsize, setsize);
                List<SolrInputDocument> batchDocs = docs.subList(from, to);
                LOGGER.info(String.format("Updating records %d - %d and size %d", from, to,batchDocs.size()));
                
                UpdateRequest req = new UpdateRequest();
                for (SolrInputDocument bDoc : batchDocs) {
                    req.add(bDoc);
                }
                try {
                    UpdateResponse response = req.process(client, config.getSyncCollection());
                    client.commit(config.getSyncCollection());
                    LOGGER.info("qtime:"+response.getQTime());
                } catch (SolrServerException  | IOException e) {
                    LOGGER.log(Level.SEVERE,e.getMessage());
                }
            }
        }
    }

    private static String gItemState(JSONObject gItem) {
        if (gItem.has("states")) {
            Object object = gItem.get("states");
            if (object instanceof JSONArray) {
                JSONArray jsArray = (JSONArray) object;
                return jsArray.getString(0);
            } else {
                return object.toString();
            }
        }
        return null;
    }
}
