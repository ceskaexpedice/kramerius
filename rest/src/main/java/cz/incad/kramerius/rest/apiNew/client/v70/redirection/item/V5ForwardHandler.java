package cz.incad.kramerius.rest.apiNew.client.v70.redirection.item;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

import cz.incad.kramerius.rest.apiNew.client.v70.redirection.DeleteTriggerSupport;
import cz.incad.kramerius.utils.pid.LexerException;
import cz.inovatika.cdk.cache.CDKRequestCacheSupport;
import cz.inovatika.cdk.cache.CDKRequestItem;
import cz.inovatika.cdk.cache.impl.CDKRequestItemFactory;
import cz.inovatika.monitoring.ApiCallEvent;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpHead;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.core5.http.HttpEntity;
import org.ceskaexpedice.akubra.RepositoryException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cz.incad.kramerius.SolrAccess;
import cz.incad.kramerius.rest.apiNew.admin.v70.reharvest.ReharvestManager;
import cz.incad.kramerius.rest.apiNew.client.v70.libs.Instances;
import cz.incad.kramerius.rest.apiNew.client.v70.redirection.ProxyHandlerException;
import cz.incad.kramerius.security.User;
import cz.incad.kramerius.utils.conf.KConfiguration;

public class V5ForwardHandler extends V5RedirectHandler {

    public static final Logger LOGGER = Logger.getLogger(V5ForwardHandler.class.getName());

    
    
    protected String forwardUrl() {
        String baseurl = KConfiguration.getInstance().getConfiguration()
                .getString("cdk.collections.sources." + this.source + ".forwardurl");
        return baseurl;
    }

    
    public V5ForwardHandler(CDKRequestCacheSupport cacheSupport, ReharvestManager reharvestManager, Instances instances, User user, CloseableHttpClient closeableHttpClient, DeleteTriggerSupport triggerSupport, SolrAccess solrAccess, String source,
                            String pid, String remoteAddr) {
        super(cacheSupport, reharvestManager, instances, user,  closeableHttpClient, triggerSupport, solrAccess, source, pid, remoteAddr);
    }

    @Override
    public Response image(RequestMethodName method, ApiCallEvent event) throws ProxyHandlerException {
        String baseurl = this.forwardUrl();
        String url = baseurl + (baseurl.endsWith("/") ? "" : "/") + "api/v5.0/cdk/forward/item/" + this.pid
                + "/streams/IMG_FULL";
        if (method == RequestMethodName.head) {
            return buildForwardApacheResponseHEAD(url, null, this.pid, false, true);
        } else {
            return buildForwardApacheResponseGET(url, null, this.pid, false, true, event, null);
        }
    }


    @Override
    public Response imagePreview(RequestMethodName method, ApiCallEvent event) throws ProxyHandlerException {
        String baseurl = this.forwardUrl();
        String url = baseurl + (baseurl.endsWith("/") ? "" : "/") + "api/v5.0/cdk/forward/item/" + this.pid
                + "/streams/IMG_PREVIEW";

        if (method == RequestMethodName.head) {
            return buildForwardApacheResponseHEAD(url, null, this.pid, false, true);
        } else {
            return buildForwardApacheResponseGET(url, null, this.pid, false, true, event, null);
        }
    }

    @Override
    public Response imageThumb(RequestMethodName method, ApiCallEvent callEvent) throws ProxyHandlerException {
        String baseurl = baseUrl();
        String url = baseurl + (baseurl.endsWith("/") ? "" : "/") + "api/v5.0/item/" + this.pid + "/thumb";
        if (method == RequestMethodName.head) {
            return buildForwardApacheResponseHEAD(url, null, this.pid, true, true);
        } else {
            CDKRequestItem cdkRequestItem = cacheItemHit_PID_USER(url, pid, false,"thumb", callEvent);
            if (cdkRequestItem != null) {
                byte[] data = ((ByteBuffer)cdkRequestItem.getData()).array();
                StreamingOutput stream = new StreamingOutput() {
                    public void write(OutputStream output) throws IOException, WebApplicationException {
                        try {
                            IOUtils.copy(new ByteArrayInputStream(data), output);
                        } catch (Exception e) {
                            throw new WebApplicationException(e);
                        }
                    }
                };
                Response.ResponseBuilder respEntity = null;
                if (cdkRequestItem.getMimeType() != null) {
                    respEntity = Response.status(200).entity(stream).type(cdkRequestItem.getMimeType());
                } else {
                    respEntity = Response.status(200).entity(stream);
                }
                long contentLength = data.length;
                if (contentLength >= 0) {
                    respEntity.header("Content-Length", String.valueOf(contentLength));
                }
                return respEntity.build();
            } else {


                return buildForwardApacheResponseGET(url, null, this.pid, true, true, callEvent, (data, mimetype)-> {

                    try {
                        CDKRequestItem<ByteBuffer> cacheItem = (CDKRequestItem<ByteBuffer>)  CDKRequestItemFactory.createCacheItem(
                                ByteBuffer.wrap(data),
                                mimetype,
                                url,
                                this.pid,
                                source,
                                LocalDateTime.now(),
                                null
                        );
                        LOGGER.fine( String.format("Storing cache item %s", cacheItem.toString()));
                        this.cacheSupport.save(cacheItem);

                    } catch (SQLException e) {
                        LOGGER.log(Level.SEVERE,e.getMessage(),e);
                    }


                });
            }
        }
    }


    @Override
    public Response mods(RequestMethodName method, ApiCallEvent event) throws ProxyHandlerException {

        if (method == RequestMethodName.head) {
            String baseurl = this.baseUrl();
            String url = baseurl + (baseurl.endsWith("/") ? "" : "/") + "api/v5.0/item/" + this.pid
                    + "/streams/BIBLIO_MODS";
            return buildRedirectResponse(url);
        } else {
            String baseurl = this.forwardUrl();
            String url = baseurl + (baseurl.endsWith("/") ? "" : "/") + "api/v5.0/cdk/forward/item/" + this.pid
                    + "/streams/BIBLIO_MODS";

            String modsString = super.cacheStringHit_PID_USER(url, this.pid, false,"mods", event);
            if (modsString == null) {
                return buildForwardApacheResponseGET(url, null, this.pid, true, false, event, (data, mimeType)-> {
                    try {
                        CDKRequestItem<String> cacheItem = (CDKRequestItem<String>)  CDKRequestItemFactory.createCacheItem(
                                new String(data, Charset.forName("UTF-8")),
                                "application/xml",
                                url,
                                this.pid,
                                source,
                                LocalDateTime.now(),
                                null
                        );
                        LOGGER.fine( String.format("Storing cache item %s", cacheItem.toString()));
                        this.cacheSupport.save(cacheItem);
                    } catch (SQLException e) {
                        LOGGER.log(Level.SEVERE,e.getMessage(),e);
                    }
                });
            } else {
                return Response.ok(modsString).type("application/xml").build();
            }
        }
    }

    @Override
    public Response info(ApiCallEvent event) throws ProxyHandlerException {
        try {

            List<Triple<String, Long, Long>> granularTimeSnapshots = event != null ?  event.getGranularTimeSnapshots() : null;


            // Fiktivni url
            String baseurl = super.baseUrl();
            String url = baseurl + (baseurl.endsWith("/") ? "" : "/") + "api/v5.0/item/" + this.pid+"/full";
            JSONObject json = new JSONObject();

            // --- First head request --
            String headerUrl = baseurl + (baseurl.endsWith("/") ? "" : "/") + "api/v5.0/item/" + this.pid;
            long startfHead = System.currentTimeMillis();
            HttpHead httpHead = apacheHead(headerUrl, false);
            httpHead.setHeader("Accept", "application/json");
            try (CloseableHttpResponse response = this.apacheClient.execute(httpHead)) {
                long stopfHead = System.currentTimeMillis();
                int code = response.getCode();
                if (granularTimeSnapshots != null) {
                    granularTimeSnapshots.add(Triple.of("http/v5/exists", startfHead, stopfHead));
                }
                if (code == 404 && this.deleteTriggerSupport != null) {
                    this.deleteTriggerSupport.executeDeleteTrigger(pid);
                }
            } catch(IOException ex) {
                LOGGER.log(Level.SEVERE,ex.getMessage(),ex);
            }

            String content = this.cacheStringHit_PID_USER(url, this.pid, false, "full", event);
            if (content == null) {
                //TODO: Cache

                JSONObject basicDoc = retrieveBasicDoc(this.pid);
                if (basicDoc != null) {
                    JSONObject streams = new JSONObject(retrieveStreams(event));
                    JSONObject info = new JSONObject(retrieveInfo(event));
                    JSONObject data = extractAvailableDataInfo(streams);
                    JSONObject image = extractImageSourceInfo(info, streams, basicDoc);
                    JSONObject struct = extractStructureInfo(this.source, info, basicDoc);

                    //JSONObject json = new JSONObject();
                    json.put("data", data);
                    json.put("structure", struct);
                    json.put("image", image);

                    try {
                        CDKRequestItem<String> cacheItem = (CDKRequestItem<String>)  CDKRequestItemFactory.createCacheItem(
                                json.toString(),
                                "application/json",
                                url,
                                this.pid,
                                source,
                                LocalDateTime.now(),
                                null
                        );

                        LOGGER.fine( String.format("Storing cache item is %s", cacheItem.toString()));
                        this.cacheSupport.save(cacheItem);
                    } catch (SQLException e) {
                        LOGGER.log(Level.SEVERE,e.getMessage(),e);
                    }
                } else {
                    return Response.status(Response.Status.NOT_FOUND).build();
                }
            } else {
                json =  new JSONObject(content);
                CACHE_LOGGER.log(Level.FINE, "<<< CACHE HIT !!! /info");
            }
            json.put("providedByLicenses", providedByLicense(event));
            return Response.ok(json).build();
        } catch (JSONException | LexerException | IOException | RepositoryException e) {
            throw new ProxyHandlerException(e);
        }
    }

    @Override
    public Response zoomifyImageProperties(RequestMethodName method, ApiCallEvent event) throws ProxyHandlerException {
        String baseurl = this.forwardUrl();
        String url = baseurl + (baseurl.endsWith("/") ? "" : "/") + "api/v5.0/cdk/forward/zoomify/" + this.pid
                + "/ImageProperties.xml";

        if (method == RequestMethodName.head) {
            return buildForwardApacheResponseHEAD(url, null, this.pid, true, true);
        } else {
            return buildForwardApacheResponseGET(url, null, this.pid, true, true,event, null);
        }
    }



    @Override
    public Response zoomifyTile(String tileGroupStr, String tileStr, ApiCallEvent callEvent) throws ProxyHandlerException {
        String baseurl = this.forwardUrl();
        String formatted = String.format("api/v5.0/cdk/forward/zoomify/%s/%s/%s", this.pid, tileGroupStr, tileStr);
        String url = baseurl + (baseurl.endsWith("/") ? "" : "/") + formatted;
        return buildForwardApacheResponseGET(url, null, this.pid, false, true, callEvent, null);
    }

    @Override
    public Response textOCR(RequestMethodName method, ApiCallEvent callEvent) throws ProxyHandlerException {
        String baseurl = this.forwardUrl();
        String url = baseurl + (baseurl.endsWith("/") ? "" : "/") + "api/v5.0/cdk/forward/item/" + this.pid
                + "/streams/TEXT_OCR";

        if (method == RequestMethodName.head) {
            return buildForwardApacheResponseHEAD(url, null, this.pid, false, true);
        } else {
            return buildForwardApacheResponseGET(url, null, this.pid, false, true, callEvent, null);
        }
    }

    @Override
    public Response altoOCR(RequestMethodName method, ApiCallEvent callEvent) throws ProxyHandlerException {

        String baseurl = this.forwardUrl();
        String url = baseurl + (baseurl.endsWith("/") ? "" : "/") + "api/v5.0/cdk/forward/item/" + this.pid
                + "/streams/ALTO";

        if (method == RequestMethodName.head) {
            return buildForwardApacheResponseHEAD(url, null, this.pid, false, true);
        } else {
            return buildForwardApacheResponseGET(url, null, this.pid, false, true, callEvent, null);
        }
    }

    protected JSONArray providedByLicense(ApiCallEvent event) {
        List<Triple<String, Long, Long>> granularTimeSnapshots = event != null ?  event.getGranularTimeSnapshots() : null;
        long start = System.currentTimeMillis();

        JSONArray retVal = new JSONArray();
        String baseurl = this.forwardUrl();
        String url = baseurl + (baseurl.endsWith("/") ? "" : "/") + "api/v5.0/cdk/forward/providedBy/" + this.pid;

        String providedByLicenses = super.cacheStringHit_PID_USER(url, this.pid, true,"providedBy", event);
        if (providedByLicenses == null) {
            HttpGet httpGet = apacheGet(url, true);

            String headers = "("+ Arrays.stream(httpGet.getHeaders()).map(h-> {
                return String.format("%s = %s", h.getName(), h.getValue());
            }).collect(Collectors.joining(", "))+")";
            LOGGER.log(Level.FINE,  String.format("GET %s %s", httpGet.toString(), headers));

            try (CloseableHttpResponse response = apacheClient.execute(httpGet)) {
                int code = response.getCode();
                if (code == 200) {
                    long stop = System.currentTimeMillis();

                    if (granularTimeSnapshots != null) {
                        granularTimeSnapshots.add(Triple.of("http/v5/providedBy", start, stop));
                    }

                    HttpEntity entity = response.getEntity();
                    InputStream is = entity.getContent();
                    String jsonString = IOUtils.toString(is, "UTF-8");

                    try {
                        CDKRequestItem<String> cacheItem = (CDKRequestItem<String>)  CDKRequestItemFactory.createCacheItem(
                                jsonString,
                                "application/json",
                                url,
                                this.pid,
                                source,
                                LocalDateTime.now(),
                                userCacheIdentification()
                        );
                        LOGGER.fine( String.format("Storing cache item is %s", cacheItem.toString()));
                        this.cacheSupport.save(cacheItem);
                    } catch (SQLException e) {
                        LOGGER.log(Level.SEVERE,e.getMessage(),e);
                    }

                    JSONObject jsonObject = new JSONObject(jsonString);
                    LOGGER.log(Level.FINE, String.format( "Provided by label  %s", jsonObject.toString()));
                    if (jsonObject.has("providedByLabel")) {
                        retVal.put(jsonObject.getString("providedByLabel"));
                    }
                }
            } catch (IOException ex) {
                LOGGER.log(Level.SEVERE,ex.getMessage(),ex);
            }
            return retVal;
        } else {
            JSONObject jsonObject = new JSONObject(providedByLicenses);
            if (jsonObject.has("providedByLabel")) {
                retVal.put(jsonObject.getString("providedByLabel"));
            }
            return retVal;
        }

    }


    @Override
    public Response iiifInfo(RequestMethodName method, String pid, ApiCallEvent callEvent ) throws ProxyHandlerException {
        String baseurl = this.forwardUrl();
        String url = baseurl + (baseurl.endsWith("/") ? "" : "/") + "api/v5.0/cdk/forward/iiif/" + this.pid
                + "/info.json";

        if (method == RequestMethodName.head) {
            return buildForwardApacheResponseHEAD(url, null, this.pid, true, true);
        } else {
            return buildForwardApacheResponseGET(url, null, this.pid, true, true, callEvent, null);
        }
    }

    

    @Override
    public Response iiifTile(RequestMethodName method, String pid, String region, String size, String rotation, String qf, ApiCallEvent callEvent)
            throws ProxyHandlerException {

        String defaultMime = IIIF_SUPPORTED_MIMETYPES.get("jpg");

        String baseurl = this.forwardUrl();
        String  postfix =  String.format("/%s/%s/%s/%s", region,size, rotation,qf);
        String url = baseurl + (baseurl.endsWith("/") ? "" : "/") + "api/v5.0/cdk/forward/iiif/" + this.pid+postfix;
        if (method == RequestMethodName.head) {
            return buildForwardApacheResponseHEAD(url, null, this.pid, true, true);
        } else {
            String mime = defaultMime;
            String[] splited = qf.split("\\.");
            if (splited.length > 1) {
                mime =  IIIF_SUPPORTED_MIMETYPES.containsKey(splited[1]) ? IIIF_SUPPORTED_MIMETYPES.get(splited[1]) :  defaultMime;
            }
            return buildForwardApacheResponseGET(url, mime, this.pid, false, true, callEvent, null);
        }
    }

    
    static Map<String, String> IIIF_SUPPORTED_MIMETYPES = new HashMap<>();
    static  {
        IIIF_SUPPORTED_MIMETYPES.put("jpg", "image/jpeg");
        IIIF_SUPPORTED_MIMETYPES.put("tif", "image/tiff");
        IIIF_SUPPORTED_MIMETYPES.put("png", "image/png");
        IIIF_SUPPORTED_MIMETYPES.put("jp2", "image/jp2");
        IIIF_SUPPORTED_MIMETYPES.put("pdf", "application/pdf");
        IIIF_SUPPORTED_MIMETYPES.put("webp", "image/webp");
    }
    

}
