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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
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
import cz.inovatika.sdnnt.SyncConfig;
import cz.inovatika.sdnnt.utils.SDNNTCheckUtils;
//import cz.kramerius.searchIndex.indexer.SolrConfig;

public class SDNNTFetch {
    
    
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
            
            //Map<String,String> pids2idents = new HashMap<>();
            Map<String,List<String>> pids2idents = new HashMap<>();
            
            
            LOGGER.info("Connecting sdnnt list and iterating serials ");
            iterateSDNNTFormat(client, config, config.getSdnntEndpoint(),  "SE", start, pids2idents);
            LOGGER.info("Connecting sdnnt list and iterating books ");
            iterateSDNNTFormat(client, config, config.getSdnntEndpoint(),  "BK", start, pids2idents);

            Map<String, String> idents2pid = new HashMap<>();
            pids2idents.keySet().stream().forEach(pid-> {
                List<String> idents = pids2idents.get(pid);
                idents.forEach(ident-> {  idents2pid.put(ident, pid); });
            });
            
            long stop = System.currentTimeMillis();
            LOGGER.info("SDNNT List fetched; It took " + (stop - start) + " ms");
            
            OffsetDateTime offsetDateTime = OffsetDateTime.ofInstant(new Date(start).toInstant(), ZoneId.systemDefault());
            String format = DateTimeFormatter.ISO_INSTANT.format(offsetDateTime);
            // one minute before process start
            client.deleteByQuery(config.getSyncCollection(), String.format("fetched:[* TO %s-1MINUTE] AND type:(main OR granularity)", format));
            
            if (config.getSyncCollection() != null) client.commit(config.getSyncCollection());

            if (config.getBaseUrl() != null) {
                LicenseAPIFetcher apiFetcher = LicenseAPIFetcher.Versions.valueOf(config.getVersion()).build(config.getBaseUrl(), config.getVersion(), true);

                long kstart = System.currentTimeMillis();
                Map<String, Map<String, Object>> fetchedObject = apiFetcher.check(pids2idents.keySet());
                LOGGER.info("Kramerius documents fetched; It took " + (System.currentTimeMillis() - kstart) + " ms");
                List<String> fetchedPids = new ArrayList<>(fetchedObject.keySet());
                
                int setsize = fetchedObject.size();
                int batchsize = 5000;
                int numberofiteration = setsize / batchsize;

                List<String> allChangedIds = new ArrayList<>(100000);
                Map<String, SolrInputDocument> allChanges = new HashMap<>(100000);

                if (setsize % batchsize != 0) numberofiteration = numberofiteration + 1;
                LOGGER.info(String.format("Updating data;  Number of iteration %d", numberofiteration));
                long ustart = System.currentTimeMillis();
                for (int i = 0; i < numberofiteration; i++) {
                  
                  List<String> batchChangedIds = new ArrayList<>();
                    
                  int from = i*batchsize;
                  int to = Math.min((i+1)*batchsize, setsize);
                  List<String> subList = fetchedPids.subList(from, to);

                  for (int j = 0; j < subList.size(); j++) {
                      
                      String pid = subList.get(j);
                      List<String> idents = pids2idents.get(pid);
                      if (idents != null) {
                          for (String ident : idents) {
                              SolrInputDocument idoc =  null;
                              if (allChanges.containsKey(ident)) {
                                  idoc = allChanges.get(ident.toString());
                              } else {
                                  idoc = new SolrInputDocument();
                                  idoc.setField("id", ident);
                              }
                              
                              
                              List<String> pidLicenses = (List<String>) fetchedObject.get(pid).get(LicenseAPIFetcher.FETCHER_LICENSES_KEY);
                              if (pidLicenses != null && !pidLicenses.isEmpty()) {
                                  for (String lic : pidLicenses) {
                                      atomicAddDistinct(idoc, lic, "real_kram_licenses");
                                  }
                              }
                              
                              // titles
                              List<String> titles = (List<String>) fetchedObject.get(pid).get(LicenseAPIFetcher.FETCHER_TITLES_KEY);
                              if (titles != null && !titles.isEmpty()) {
                                  for (String lic : titles) {
                                      atomicAddDistinct(idoc, lic, "real_kram_titles_search");
                                  }
                              }

                              atomicOneValSet(idoc, true, "real_kram_exists");
                              
                              String date = (String) fetchedObject.get(pid).get(LicenseAPIFetcher.FETCHER_DATE_KEY);
                              if (date != null) {
                                  atomicOneValSet(idoc, date, "real_kram_date");
                              }
                              
                              String model = (String) fetchedObject.get(pid).get(LicenseAPIFetcher.FETCHER_MODEL_KEY);
                              if (model != null) {
                                  atomicOneValSet(idoc, model, "real_kram_model");
                              }

                              if (!allChangedIds.contains(ident.toString())) {
                                  allChangedIds.add(ident);
                                  batchChangedIds.add(ident);
                                  allChanges.put(ident, idoc);
                              }
                          }
                          
                      }
                      
                  }

                  LOGGER.info("Batch number "+i+"; Data updated;  It took " + (System.currentTimeMillis() - ustart) + " ms");
                  LOGGER.info("Batch number "+i+"; Calculating differences");
                  List<String> identifiers = subList.stream().map(pids2idents::get).flatMap(Collection::stream).collect(Collectors.toList());

                  String collection = config.getSyncCollection();
                  SolrDocumentList list = getById(client, identifiers, collection);
                  for (SolrDocument rDoc : list) {
                    Object ident = rDoc.getFieldValue("id");
                    SolrInputDocument in = allChanges.get(ident.toString());

                    Collection<Object> fieldValues = in.getFieldValues("real_kram_licenses");

                    List<String> docLicenses = distinctValues(fieldValues);
                    
                    Collection<Object> syncMasterActions = in.getFieldValues("sync_actions");
                    List<String> masterActions = distinctValues(syncMasterActions);

                    Object type = rDoc.getFieldValue("type");

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
                                        atomicOneValSet(in, SyncActionEnum.change_dnnto_dnntt.getValue(), "sync_sort");
                                        dirty = true;
                                    } else {
                                        atomicAddDistinct(in, SyncActionEnum.add_dnntt.name(), "sync_actions");
                                        atomicOneValSet(in, SyncActionEnum.add_dnntt.getValue(), "sync_sort");
                                        dirty = true;
                                    }
                                }

                                if (license.toString().equals("dnnto") && !docLicenses.contains("dnnto")) {
                                    if (docLicenses.contains("dnntt")) {
                                        atomicAddDistinct(in, SyncActionEnum.change_dnnto_dnntt.name() /*"change_dnnto_dnntt"*/, "sync_actions");
                                        atomicOneValSet(in, SyncActionEnum.change_dnnto_dnntt.getValue() /*"change_dnnto_dnntt"*/, "sync_sort");
                                        dirty = true;
                                    } else {
                                        atomicAddDistinct(in, SyncActionEnum.add_dnnto.name() /*"add_dnnto"*/, "sync_actions");
                                        atomicOneValSet(in, SyncActionEnum.add_dnnto.getValue() /*"add_dnnto"*/, "sync_sort");
                                        dirty = true;
                                    }
                                }
                            }
                        }
                    } else {
                        if (granularityItem || !hasGranularity) {
                            
                            if (docLicenses.contains("dnntt")) {
                                atomicAddDistinct(in, SyncActionEnum.remove_dnntt.name() /*"remove_dnntt"*/, "sync_actions");
                                atomicOneValSet(in, SyncActionEnum.remove_dnntt.getValue() /*"remove_dnntt"*/, "sync_sort");
                                dirty = true;
                            }
                            if (docLicenses.contains("dnnto")) {
                                atomicAddDistinct(in, SyncActionEnum.remove_dnnto.name() /*"remove_dnnto"*/, "sync_actions");
                                if (!docLicenses.contains("dnntt")) {
                                    atomicOneValSet(in, SyncActionEnum.remove_dnnto.getValue() /*"remove_dnnto"*/, "sync_sort");
                                }
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

                                Collection<Object> masterInSyncActions = masterIn.getFieldValues("sync_actions");
                                List<String> actions = distinctValues(masterInSyncActions);

                                if (!actions.contains(SyncActionEnum.partial_change.name())) {
                                    atomicAddDistinct(masterIn, SyncActionEnum.partial_change.name(), "sync_actions");
                                    atomicOneValSet(masterIn,  SyncActionEnum.partial_change.getValue() /* "partial_change"*/, "sync_sort");
                                }
                                //if (fieldValue.con)
                            } else {
                                
                                String masterId = field.toString();
                                String pid = idents2pid.get(masterId);
                                        
                                SolrInputDocument masterIn = new SolrInputDocument();
                                masterIn.setField("id", masterId);
                                atomicAddDistinct(masterIn, SyncActionEnum.partial_change.name(), "sync_actions");
                                atomicOneValSet(masterIn,  SyncActionEnum.partial_change.getValue() /* "partial_change"*/, "sync_sort");

                                if (fetchedObject.keySet().contains(pid)) {
                                    atomicOneValSet(masterIn,  true, "real_kram_exists");
                                    
                                    List<String> pidLicenses = (List<String>) fetchedObject.get(pid).get(LicenseAPIFetcher.FETCHER_LICENSES_KEY);
                                    if (pidLicenses != null && !pidLicenses.isEmpty()) {
                                        for (String lic : pidLicenses) {
                                            atomicAddDistinct(masterIn, lic, "real_kram_licenses");
                                        }
                                    }
                                    
                                    // titles
                                    List<String> titles = (List<String>) fetchedObject.get(pid).get(LicenseAPIFetcher.FETCHER_TITLES_KEY);
                                    if (titles != null && !titles.isEmpty()) {
                                        for (String lic : titles) {
                                            atomicAddDistinct(masterIn, lic, "real_kram_titles_search");
                                        }
                                    }

                                    String date = (String) fetchedObject.get(pid).get(LicenseAPIFetcher.FETCHER_DATE_KEY);
                                    if (date != null) {
                                        atomicOneValSet(masterIn, date, "real_kram_date");
                                    }
                                    
                                    String model = (String) fetchedObject.get(pid).get(LicenseAPIFetcher.FETCHER_MODEL_KEY);
                                    if (model != null) {
                                        atomicOneValSet(masterIn, model, "real_kram_model");
                                    }
                                }

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
                      LOGGER.fine(String.format("Update batch with size %s",  req.getDocuments().size()));
                      try {
                          UpdateResponse response = req.process(client, config.getSyncCollection());
                          LOGGER.fine("qtime:"+response.getQTime());
                          if (config.getSyncCollection() != null) client.commit(config.getSyncCollection());
                      } catch (SolrServerException  | IOException e) {
                          LOGGER.log(Level.SEVERE,e.getMessage());
                      }
                  }
              }
              
            }
    }


    private static List<String> distinctValues(Collection<Object> fieldValues) {
        List<Object> data = new ArrayList<>();
        if (fieldValues != null) {
            fieldValues.stream().forEach(obj-> {
                Map<String,Object> m = (Map<String, Object>) obj;
                //Object val = 
                Iterator<Entry<String, Object>> iterator = m.entrySet().iterator();
                if (iterator.hasNext())  {
                    Object val = iterator.next().getValue();
                    if (val instanceof Collection) {
                        data.addAll( (Collection) val );
                    } else {
                        data.add(val);
                    }
                }
            });
        }
        return data.stream().map(Object::toString).collect(Collectors.toList());
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

    public static void atomicOneValSet(SolrInputDocument idoc, Object fValue, String fName) {
        Object fieldValue = idoc.getFieldValue(fName);
        if (fieldValue == null) {
            Map<String, Object> modifier = new HashMap<>(1);
            modifier.put("set", fValue);
            idoc.addField(fName, modifier);
        }
    }
    
    public static void atomicSet(SolrInputDocument idoc, Object fValue, String fName) {
        if (!addToExistingModifier(idoc, fValue, fName)) {
            Map<String, Object> modifier = new HashMap<>(1);
            modifier.put("set", fValue);
            idoc.addField(fName, modifier);
        }
    }

    public static void atomicAddDistinct(SolrInputDocument idoc, Object fValue, String fName) {
        if (!addToExistingModifier(idoc, fValue, fName)) {
            Map<String, Object> modifier = new HashMap<>(1);
            modifier.put("add-distinct", fValue);
            idoc.addField(fName, modifier);
        }
    }



    private static boolean addToExistingModifier(SolrInputDocument idoc, Object fValue, String fName) {
        Object fieldValue = idoc.getFieldValue(fName);
        if (fieldValue != null) {
            String key = null;
            List<Object> values = new ArrayList<>();
            Map<String, Object> map = (Map<String, Object>) fieldValue;
            Iterator<Entry<String, Object>> iterator = map.entrySet().iterator();
            if (iterator.hasNext())  {
                Entry<String, Object> entry = iterator.next();
                key = entry.getKey();
                Object value = entry.getValue();
                if (value instanceof Collection) {
                    values.addAll((Collection)value);
                    values.add(fValue);
                } else {
                    values.add(value);
                    values.add(fValue);
                }
            }
            
            if (key != null) {
                map.put(key, values);
            }
            return true;
        }
        return false;
        
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
            Map<String,List<String>> pids2oais) throws IOException, InterruptedException, SolrServerException {
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
                
                
                JSONObject mainObject = items.getJSONObject(i);
                String ident = mainObject.getString("catalog_identifier");
                if (!counters.containsKey(ident)) {
                    counters.put(ident, new AtomicInteger(0));
                }
                counters.get(ident).addAndGet(1);
                
                SolrInputDocument doc = new SolrInputDocument();
                doc.setField("id", ident+"_"+counters.get(ident).get());
                
                String catalogIdentifier = mainObject.getString("catalog_identifier");
                
                doc.setField("catalog", mainObject.getString("catalog_identifier"));
                doc.setField("title", mainObject.getString("title"));
                doc.setField("type_of_rec", mainObject.getString("type"));
                doc.setField("state", mainObject.getString("state"));
                doc.setField("state", mainObject.getString("state"));
                doc.setField("fetched", new Date(startProcess));
                // controlfield 008 
                
                JSONObject skc = mainObject.optJSONObject("skc");
                if (skc != null) {
                    String controlField008 = skc.optString("controlfield_008");
                    if (controlField008 != null) {
                        
                        String typeOfDate = controlField008.substring(6, 7);
                        String date1 = controlField008.substring(7, 11);
                        String date2 = controlField008.substring(11, 15);
                        doc.setField("controlField_typeofdate", typeOfDate);
                        doc.setField("controlField_date1", date1);
                        doc.setField("controlField_date2", date2);
                    }
                }
                
                
                if (mainObject.has("pid")) {
                    doc.setField("pid", mainObject.getString("pid"));
                    if (!pids2oais.containsKey(mainObject.getString("pid"))) {
                        pids2oais.put(mainObject.getString("pid"), new ArrayList<>());
                    }

                    pids2oais .get(mainObject.getString("pid")).add(ident+"_"+counters.get(ident).get());
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
                            if (!pids2oais.containsKey(item.getString("pid"))) {
                                pids2oais.put(item.getString("pid"), new ArrayList<>());
                            }
                            //pids2oais.put(item.getString("pid"), ident+"_"+counters.get(ident).get()+"_"+item.getString("pid"));
                            pids2oais.get(item.getString("pid")).add(ident+"_"+counters.get(ident).get()+"_"+item.getString("pid"));
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