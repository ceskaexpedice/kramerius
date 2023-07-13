package cz.incad.kramerius.rest.api.k5.admin.sync.sdnnt;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.BrokenBarrierException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import com.google.inject.Provider;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;

import cz.incad.kramerius.Constants;
import cz.incad.kramerius.ObjectPidsPath;
import cz.incad.kramerius.processes.DefinitionManager;
import cz.incad.kramerius.processes.LRProcess;
import cz.incad.kramerius.processes.LRProcessDefinition;
import cz.incad.kramerius.processes.LRProcessManager;
import cz.incad.kramerius.processes.annotations.ParameterName;
import cz.incad.kramerius.rest.api.exceptions.ActionNotAllowed;
import cz.incad.kramerius.rest.api.exceptions.GenericApplicationException;
import cz.incad.kramerius.security.IsActionAllowed;
import cz.incad.kramerius.security.SecuredActions;
import cz.incad.kramerius.security.SpecialObjects;
import cz.incad.kramerius.security.User;
import cz.incad.kramerius.security.utils.UserUtils;
import cz.incad.kramerius.service.MigrateSolrIndexException;
import cz.incad.kramerius.utils.IPAddressUtils;
import cz.incad.kramerius.utils.RESTHelper;
import cz.incad.kramerius.utils.XMLUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;
import cz.inovatika.sdnnt.LicenseAPIFetcher;
import cz.inovatika.sdnnt.SyncConfig;

@Path("/v5.0/admin/sdnnt")
public class SDNNTSyncResource {

	public static final SimpleDateFormat FORMATER = new SimpleDateFormat("yyMMddHHmmss");
	
    public static final Logger LOGGER = Logger.getLogger(SDNNTSyncResource.class.getName());
    

    public static enum SyncActionEnum {

        add_dnnto(Arrays.asList("dnnto"), 
        		Arrays.asList(Triple.of("parametrizeddnntlabelset", "dnnto",null))), 

        add_dnntt(Arrays.asList("dnntt"), 
        		Arrays.asList(Triple.of("parametrizeddnntlabelset", "dnntt",null))),
        
        remove_dnnto(Arrays.asList("dnnto"), 
        		Arrays.asList(Triple.of("parametrizeddnntlabelunset", "dnnto",null))),
        
        remove_dnntt(Arrays.asList("dnntt"), 
        		Arrays.asList(Triple.of("parametrizeddnntlabelunset", "dnntt",null))),
        
        change_dnnto_dnntt(Arrays.asList("dnnto", "dnntt"), 
        		Arrays.asList(Triple.of("parametrizeddnntlabelunset", "dnnto","remove_dnnto"), Triple.of("parametrizeddnntlabelset", "dnntt","add_dnnto"))),
        
        change_dnntt_dnnto(
        		Arrays.asList("dnntt", "dnnto"), 
        		Arrays.asList( Triple.of("parametrizeddnntlabelunset", "dnntt","remove_dnntt"), Triple.of("parametrizeddnntlabelset", "dnnto","add_dnntt"))

		);

        private List<String> licenses;
        private List<Triple<String,String, String>> params;

        private SyncActionEnum(List<String> licenses, List<Triple<String,String, String>> params) {
            this.licenses = licenses;
            this.params = params;
        }

        public List<String> getLicenses() {
            return licenses;
        }

        
        public List<Triple<String, String, String>> getProcess() {
			return params;
		}
        
        public File csvFile() {
        	String wdir = Constants.WORKING_DIR;
        	File folder = new File(wdir, "sdnnt_sync");
        	File csvFile = new File(folder, name()+"_csv");
        	return csvFile.exists() ? csvFile : null;
        }
    }

    public static final int DEFAULT_PAGE = 0;
    public static final int DEFAULT_ROWS = 15;

    @Inject
    Provider<HttpServletRequest> requestProvider;
    
    @Inject
    Provider<User> userProvider;

    @Inject
    LRProcessManager lrProcessManager;

    @Inject
    DefinitionManager definitionManager;
    
    @Inject
    IsActionAllowed actionAllowed;


    @GET
    @Path("info")
    @Produces({ MediaType.APPLICATION_JSON + ";charset=utf-8" })
    public Response info() {
    	
    	if (permit(this.userProvider.get())) {

    		JSONObject infoObject = new JSONObject();

    		String kramerius = KConfiguration.getInstance().getConfiguration().getString("sdnnt.check.local.api", KConfiguration.getInstance().getConfiguration().getString("api.point"));
			
    		infoObject.put("kramerius", kramerius);
            infoObject.put("acronym", KConfiguration.getInstance().getConfiguration().getString("sdnnt.check.acronym"));
            infoObject.put("endpoint", KConfiguration.getInstance().getConfiguration().getString("sdnnt.check.endpoint",
                    "https://sdnnt.nkp.cz/sdnnt/api/v1.0/lists/changes"));
            infoObject.put("version",
                    KConfiguration.getInstance().getConfiguration().getString("sdnnt.check.version", "v5"));

            return Response.ok().entity(infoObject.toString(2)).build();
    		
    	} else throw new ActionNotAllowed("action is not allowed");
    	

    }

    @GET
    @Path("sync/timestamp")
    @Produces({ MediaType.APPLICATION_JSON + ";charset=utf-8" })
    public Response lastTimestamp(String dd) {
        try {

        	if (permit(this.userProvider.get())) {

        		String mainQuery = URLEncoder.encode("sync_actions:*", "UTF-8");
                String sort = URLEncoder.encode("fetched asc", "UTF-8");

                String sdnntHost = KConfiguration.getInstance().getConfiguration().getString("solrSdnntHost");
                String url = sdnntHost
                        + String.format("/select?q=%s&wt=json&rows=%d&start=%d&sort=%s", mainQuery, 1, 0, sort);
                InputStream is = RESTHelper.inputStream(url, null, null);

                String res = IOUtils.toString(is, Charset.forName("UTF-8"));
                JSONObject responseJSON = new JSONObject(res);
                JSONObject response = responseJSON.getJSONObject("response");
                JSONArray docs = response.getJSONArray("docs");
                String fetched = null;
                for (int i = 0; i < docs.length(); i++) {
                    JSONObject oneDoc = docs.getJSONObject(i);
                    fetched = oneDoc.getString("fetched");
                }
                if (docs.length() >0) docs.remove(0);

                JSONObject newObject = new JSONObject();
                newObject.put("fetched", fetched);
                docs.put(newObject);

                return Response.ok().entity(response.toString(2)).build();

        	} else throw new ActionNotAllowed("action is not allowed");

        } catch (IOException e) {
            throw new WebApplicationException(e);
        }
    }

    private LRProcess planProcess(File file,  String label,  LRProcessDefinition definition) {
        User user = userProvider.get();
        String loggedUserKey =  (String) requestProvider.get().getSession().getAttribute(UserUtils.LOGGED_USER_KEY_PARAM);

        String batchToken = UUID.randomUUID().toString();

        
    	LRProcess newProcess = definition.createNewProcess(batchToken, batchToken);
        newProcess.setLoggedUserKey(loggedUserKey);
        
        newProcess.setParameters(Arrays.asList(new String[0]));
        newProcess.setUser(user);
        
        Properties props = new Properties();
        props.setProperty("csvfile", file.getAbsolutePath());
        props.setProperty("label", label);
        
        newProcess.planMe(props, IPAddressUtils.getRemoteAddress(this.requestProvider.get(), KConfiguration.getInstance().getConfiguration()));
        lrProcessManager.updateAuthTokenMapping(newProcess, loggedUserKey);
        return newProcess;
    }
    
    @GET
    @Path("sync/batches")
    @Produces({ MediaType.APPLICATION_JSON + ";charset=utf-8" })
    public Response planBatches() {
    	if (permit(this.userProvider.get())) {

    		Client client = Client.create();
            JSONArray response = new JSONArray();
            try {
            	
            	String folderStr = Constants.WORKING_DIR + File.separator + "sdnnt-sync"+File.separator+FORMATER.format(new Date());
            	File folder = new File(folderStr);
            	folder.mkdirs();
            	
            	 
            	SyncActionEnum[] values = SyncActionEnum.values();
            	
            	for (SyncActionEnum action : values) {
            		// folder for actions 
            		File actionFolder = new File(folder, action.name());
            		actionFolder.mkdirs();
            		

                    int batchSize = 5000;
                    // id <-> pid 
                    List<Pair<String, String>> pairs = pidsFromSolr(action);

                    int numberOfBatches = pairs.size() / batchSize;
                    numberOfBatches = numberOfBatches + ((pairs.size() % batchSize) == 0 ? 0 : 1);
                    for (int i = 0; i < numberOfBatches; i++) {
                        int from = i * batchSize;
                        int to = Math.min((i + 1) * batchSize, pairs.size());
                        List<Pair<String, String>> sublist = pairs.subList(from, to);// .stream().map(Pair::getRight).collect(Collectors.toList());
                        
                        
                        User user = this.userProvider.get();
                        // pidy 
                        List<String> pids = sublist.stream().map(p -> {
                            return p.getRight();
                        }).collect(Collectors.toList());


                        List<Triple<String, String, String>> tripple = action.getProcess();
                        for (Triple	<String, String, String> tpl : action.getProcess()) {

                        	String defid = tpl.getLeft();
                        	String license = tpl.getMiddle();
                        	String subfodler = tpl.getRight();
                        	
                        	File f = storePids(pids, actionFolder, subfodler, i);
                        	LRProcess process = planProcess(f, license, this.definitionManager.getLongRunningProcessDefinition(defid));

                        	for (Pair<String,String> pair : sublist) {
                            	try {
    								String sdnntHost = KConfiguration.getInstance().getConfiguration().getString("solrSdnntHost");

    								Document add = XMLUtils.crateDocument("add");
    								Element doc = add.createElement("doc");
    								
    								Element idField = add.createElement("field");
    								idField.setAttribute("name", "id");
    								idField.setTextContent(pair.getLeft());
    								doc.appendChild(idField);
    								
    								
    								Element processUuid = add.createElement("field");
    								processUuid.setAttribute("name", "process_uuid");
    								processUuid.setAttribute("update", "add-distinct");
    								processUuid.setTextContent(process.getUUID().toString());
    								doc.appendChild(processUuid);
    								
    								add.getDocumentElement().appendChild(doc);

    								
    								StringWriter writer = new StringWriter();
    								XMLUtils.print(add, writer);
    								WebResource r = client.resource(sdnntHost+"/update?commitWithin=7000");
    								ClientResponse resp = r.accept(MediaType.TEXT_XML).type(MediaType.TEXT_XML).entity(writer.toString(), MediaType.TEXT_XML).post(ClientResponse.class);
    								if (resp.getStatus() != ClientResponse.Status.OK.getStatusCode()) {
    								    throw new IllegalStateException("Exiting with staus:"+resp.getStatus());
    								}

    								JSONObject retobject = new JSONObject();
    								//retobject.put("processId", batch.processId);
    								retobject.put("processUuid", process.getUUID());
    								retobject.put("sync_actions", action.name());
    								retobject.put("defid", defid);
    								retobject.put("license", license);
    								retobject.put("number_of_objects", sublist.size());
    								retobject.put("batch_number", i);
    								
    								
    								response.put(retobject);
    							} catch (DOMException | UniformInterfaceException | ClientHandlerException | JSONException | ParserConfigurationException | TransformerException e) {
    								LOGGER.log(Level.SEVERE, e.getMessage(),e);
    							}
    						}
    					}
                    }
            	}
                return Response.ok().entity(response.toString(2)).build();
            } catch (JSONException | IOException e) {
                throw new WebApplicationException(e);
            }
    	} else throw new ActionNotAllowed("action is not allowed");
    }

    private File storePids(List<String> pids, File actionFolder, String s, int iteration) throws IOException {
    	File outputFolder = actionFolder;
    	if (s != null) {
        	outputFolder = new File(actionFolder, s);
        	outputFolder.mkdirs();
    	}
    	File csvFile = new File(outputFolder,String.format("%d.csv", iteration));
        OutputStreamWriter outputStreamWriter = new OutputStreamWriter(new FileOutputStream(csvFile), Charset.forName("UTF-8"));
        try (CSVPrinter printer = new CSVPrinter(outputStreamWriter, CSVFormat.DEFAULT.withHeader("pid"))) {
        	pids.forEach(pid -> {
            	try {
					printer.printRecord(pid);
				} catch (IOException e) { LOGGER.log(Level.SEVERE,e.getMessage(),e); }
    		});
        }
        return csvFile;
    }

	public static List<Pair<String, String>> pidsFromSolr(SyncActionEnum action)
            throws UnsupportedEncodingException, IOException {
        List<Pair<String, String>> pids = new ArrayList<>();
        String sdnntHost = KConfiguration.getInstance().getConfiguration().getString("solrSdnntHost");
        String mainQuery = URLEncoder.encode(String.format("sync_actions:%s", action.name()), "UTF-8");
        String cursor = "*";
        String nextCursor = "*";
        do {
            cursor = nextCursor;
            
            String url = sdnntHost + String.format("/select?q=%s&wt=json&sort=%s&cursorMark=%s&fl=%s", mainQuery,
                    URLEncoder.encode("id asc", "UTF-8"), cursor, URLEncoder.encode("pid id", "UTF-8"));
            InputStream is = RESTHelper.inputStream(url, null, null);
            String string = IOUtils.toString(is, Charset.forName("UTF-8"));

            JSONObject result = new JSONObject(string);
            JSONObject responseObject = result.getJSONObject("response");
            JSONArray array = responseObject.getJSONArray("docs");
            for (int i = 0; i < array.length(); i++) {
                JSONObject doc = array.getJSONObject(i);
                String pid = doc.getString("pid");
                String id = doc.getString("id");
                pids.add(Pair.of(id, pid));
            }

            nextCursor = result.getString("nextCursorMark");

        } while (!nextCursor.equals(cursor));
        return pids;
    }

    @GET
    @Path("sync")
    @Produces({ MediaType.APPLICATION_JSON + ";charset=utf-8" })
    public Response sync(@DefaultValue("0") @QueryParam("page") String spage,
            @DefaultValue("15") @QueryParam("rows") String srows) {
    	
    	if (permit(this.userProvider.get())) {
        	
            try {
                
                List<String> pids = new ArrayList<>();
                Map<String, JSONObject> map = new HashMap<>();

                int page = DEFAULT_PAGE;
                int rows = DEFAULT_ROWS;
                try {
                    page = Integer.parseInt(spage);
                } catch (NumberFormatException e) {
                }
                try {
                    rows = Integer.parseInt(srows);
                } catch (NumberFormatException e) {
                }

                String sdnntHost = KConfiguration.getInstance().getConfiguration().getString("solrSdnntHost");

                int start = (page * rows);
                String mainQuery = URLEncoder.encode("type:main AND sync_actions:*", "UTF-8");
                String sort = URLEncoder.encode("sync_sort asc", "UTF-8");

                String url = sdnntHost
                        + String.format("/select?q=%s&wt=json&rows=%d&start=%d&sort=%s", mainQuery, rows, start, sort);
                InputStream is = RESTHelper.inputStream(url, null, null);

                String res = IOUtils.toString(is, Charset.forName("UTF-8"));
                JSONObject responseJSON = new JSONObject(res);
                JSONObject response = responseJSON.getJSONObject("response");
                JSONArray docs = response.getJSONArray("docs");
                for (int i = 0; i < docs.length(); i++) {
                	JSONObject oneDoc = docs.getJSONObject(i);


                    if (!oneDoc.has("real_kram_exists") && oneDoc.has("pid")) {
                        String pid = oneDoc.getString("pid");
                        pids.add(pid);
                        map.put(pid, oneDoc);
                    }

                	if (!pids.isEmpty()) {
                        SyncConfig config = new SyncConfig();
                        LicenseAPIFetcher apiFetcher = LicenseAPIFetcher.Versions.v5.build(config.getBaseUrl(), config.getVersion(), false);
                        Map<String, Map<String, Object>> checked = apiFetcher.check(new HashSet<>(pids));
                        checked.keySet().forEach(pid-> {
                            Map<String, Object> object = checked.get(pid);
                            Object model = object.get(LicenseAPIFetcher.FETCHER_MODEL_KEY);
                            Object date = object.get(LicenseAPIFetcher.FETCHER_DATE_KEY);
                            Object titles = object.get(LicenseAPIFetcher.FETCHER_TITLES_KEY);
                            if (map.containsKey(pid)) {
                                map.get(pid).put("real_kram_exists", true);
                                map.get(pid).put("real_kram_date", date);
                                map.get(pid).put("real_kram_model", model);
                                
                                if (titles != null) {
                                    JSONArray titlesArray = new JSONArray();
                                    ((List)titles).forEach(titlesArray::put);
                                    map.get(pid).put("real_kram_titles_search", titlesArray);
                                }
                            }
                            
                        });
                    }

                	
                	oneDocResult(oneDoc);
                }
                return Response.ok().entity(response.toString(2)).build();
            } catch (IOException e) {
                throw new WebApplicationException(e);
            }

    	} else throw new ActionNotAllowed("action is not allowed");

    }

	private void oneDocResult(JSONObject oneDoc) {
		List<JSONObject> details = new ArrayList<>();
		if (oneDoc.has("process_uuid")) {
			JSONArray processUiids = oneDoc.getJSONArray("process_uuid");
			for (int j = 0; j < processUiids.length(); j++) {
				String uuid = processUiids.getString(j);
				LRProcess longRunningProcess = this.lrProcessManager.getLongRunningProcess(uuid);
				JSONObject procObject = new JSONObject();
				procObject.put("state", longRunningProcess.getProcessState().name());
				procObject.put("finished", longRunningProcess.getFinishedTime());
				procObject.put("uuid", longRunningProcess.getUUID());
				details.add(procObject);
			}
		}
		
		
		if (details.size() > 0) {
			//details.sort(new Comparator<T>)
			details.sort(new Comparator<JSONObject>() {

				@Override
				public int compare(JSONObject o1, JSONObject o2) {
					long o1Finished = o1.optLong("finished");
					long o2Finished = o2.optLong("finished");
					return Long.compare(o2Finished, o1Finished);
				}
				
			});
			JSONObject processUuidsDetail = new JSONObject();
			details.forEach(obj-> {
				String uuid = obj.optString("uuid");
				processUuidsDetail.put(uuid, obj);
			});

			oneDoc.put("process_uuid_details", processUuidsDetail);
		}
		
		oneDoc.remove("_version_");
	}

    @GET
    @Path("sync/granularity/{id}")
    @Produces({ MediaType.APPLICATION_JSON + ";charset=utf-8" })
    public Response syncChildren(@PathParam("id") String id) {
    	if (permit(this.userProvider.get())) {
        	try {
                String sdnntHost = KConfiguration.getInstance().getConfiguration().getString("solrSdnntHost");

                String mainQuery = URLEncoder.encode(String.format("parent_id:\"%s\" AND sync_actions:*", id), "UTF-8");

                String url = sdnntHost + String.format("/select?q=%s&wt=json&rows=4000", mainQuery);
                InputStream is = RESTHelper.inputStream(url, null, null);
                String res = IOUtils.toString(is, Charset.forName("UTF-8"));
                JSONObject responseJSON = new JSONObject(res);
                JSONArray docs = responseJSON.getJSONObject("response").getJSONArray("docs");

                for (int i = 0; i < docs.length(); i++) {
                    JSONObject oneDoc = docs.getJSONObject(i);
                	oneDocResult(oneDoc);
                }

                JSONObject retObject = new JSONObject();
                retObject.put(id, docs);

                return Response.ok().entity(retObject.toString(2)).build();
            } catch (IOException e) {
                throw new WebApplicationException(e);
            }
    	} else throw new ActionNotAllowed("action is not allowed");
    }
    
    boolean permit(User user) {
        if (user != null) {
            boolean admin = this.actionAllowed.isActionAllowed(user,SecuredActions.ADMINISTRATE.getFormalName(), SpecialObjects.REPOSITORY.getPid(), null , ObjectPidsPath.REPOSITORY_PATH).flag();
            boolean dnntadmin = this.actionAllowed.isActionAllowed(user,SecuredActions.DNNT_ADMIN.getFormalName(), SpecialObjects.REPOSITORY.getPid(), null , ObjectPidsPath.REPOSITORY_PATH).flag();
            return admin  || dnntadmin;
        } else
            return false;
    }


    
}
