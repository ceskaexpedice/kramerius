package cz.incad.kramerius.rest.apiNew.client.v70.redirection.item;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.StreamingOutput;

import cz.incad.kramerius.rest.apiNew.client.v70.redirection.DeleteTriggerSupport;
import cz.inovatika.cdk.cache.CDKRequestCacheSupport;
import cz.inovatika.cdk.cache.CDKRequestItem;
import cz.inovatika.cdk.cache.impl.CDKRequestItemFactory;
import cz.inovatika.monitoring.ApiCallEvent;
import org.apache.commons.io.IOUtils;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpHead;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.core5.http.HttpEntity;
import org.json.JSONArray;
import org.json.JSONObject;

import cz.incad.kramerius.SolrAccess;
import cz.incad.kramerius.rest.apiNew.admin.v70.reharvest.ReharvestManager;
import cz.incad.kramerius.rest.apiNew.client.v70.libs.Instances;
import cz.incad.kramerius.rest.apiNew.client.v70.redirection.ProxyHandlerException;
import cz.incad.kramerius.security.User;
import cz.incad.kramerius.utils.conf.KConfiguration;

public class V7ForwardHandler extends V7RedirectHandler {

    public static final Logger LOGGER = Logger.getLogger(V5ForwardHandler.class.getName());

    public V7ForwardHandler(CDKRequestCacheSupport cacheSupport, ReharvestManager reharvestManager, Instances instances, User user, CloseableHttpClient closeableHttpClient, DeleteTriggerSupport triggerSupport, SolrAccess solrAccess, String source, String pid, String remoteAddr) {
        super(cacheSupport, reharvestManager,instances, user,  closeableHttpClient, triggerSupport, solrAccess, source, pid, remoteAddr);
	}

    protected String forwardUrl() {
        String baseurl = KConfiguration.getInstance().getConfiguration()
                .getString("cdk.collections.sources." + this.source + ".forwardurl");
        return baseurl;
    }

    
    @Override
    public Response info(ApiCallEvent event) throws ProxyHandlerException {

        JSONArray licenses = null;
        String baseurl = this.forwardUrl();
        String providedByUrl = baseurl + (baseurl.endsWith("/") ? "" : "/") + "api/cdk/v7.0/forward/providedBy/" + this.pid;
        LOGGER.fine("Provided by url "+providedByUrl +  " = "+ this.user.toString());

        String providedByLicenses = super.cacheStringHit_PID_USER(providedByUrl, this.pid, true,"info", event);
        if (providedByLicenses == null) {


            HttpGet providedByHttpGet =  apacheGet(providedByUrl, true);
            try (CloseableHttpResponse response = apacheClient.execute(providedByHttpGet)) {
                int code = response.getCode();
                if (code == 200) {
                    HttpEntity entity = response.getEntity();
                    InputStream is = entity.getContent();
                    String providedByString = IOUtils.toString(is, Charset.forName("UTF-8"));
                    try {
                        CDKRequestItem<String> cacheItem = (CDKRequestItem<String>)  CDKRequestItemFactory.createCacheItem(
                                providedByString,
                                "application/json",
                                providedByUrl,
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

                    JSONObject providedByJSON = new JSONObject(providedByString);
                    licenses = providedByJSON.optJSONArray("licenses");
                    if (licenses != null)  providedByJSON.put("providedByLicenses", licenses);
                }
            } catch (IOException ex) {
                LOGGER.log(Level.SEVERE,ex.getMessage(),ex);
            }
        } else {
            JSONObject providedByJSON = new JSONObject(providedByLicenses);
            licenses = providedByJSON.optJSONArray("licenses");
        }
        LOGGER.fine(String.format("Resolved licenses %s, finding info",licenses));


        String url = baseurl + (baseurl.endsWith("/") ? "" : "/") + "api/client/v7.0/items/" + this.pid + "/info";
        // Delete trigger
        HttpHead infoHead =  apacheHead(url, true);
        try (CloseableHttpResponse response = apacheClient.execute(infoHead)) {
            int code = response.getCode();
            if (code == 404 && this.deleteTriggerSupport != null) {
                this.deleteTriggerSupport.executeDeleteTrigger(this.pid);
            }
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE,ex.getMessage(),ex);
        }
        String infoContentString = super.cacheStringHit_PID_USER(url, this.pid, false,"info", event);
        JSONObject infoContentJSON = null;
        if (infoContentString != null) {
            // found in cache
            infoContentJSON = new JSONObject(infoContentString);
            if (licenses != null) {
                CACHE_LOGGER.log(Level.FINE, "<<< CACHE HIT !!! /info");
                infoContentJSON.put("providedByLicenses", licenses);
            }
        } else {
            HttpGet infoGet =  apacheGet(url, true);
            try (CloseableHttpResponse response = apacheClient.execute(infoGet)) {
                int code = response.getCode();
                if (code == 200) {
                    HttpEntity entity = response.getEntity();
                    InputStream is = entity.getContent();
                    infoContentString = IOUtils.toString(is, Charset.forName("UTF-8"));
                    infoContentJSON = new JSONObject(infoContentString);
                    try {
                        CDKRequestItem<String> cacheItem = (CDKRequestItem<String>)  CDKRequestItemFactory.createCacheItem(
                                infoContentString,
                                "application/json",
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

                    if (licenses != null) {
                        infoContentJSON.put("providedByLicenses", licenses);
                    }

                } else {
                    return Response.status(code).build();
                }
            } catch (IOException ex) {
                LOGGER.log(Level.SEVERE,ex.getMessage(),ex);
            }
        }

        if (infoContentJSON != null) {
            LOGGER.fine(String.format("Returning from json %s", infoContentJSON.toString()));
            ResponseBuilder respEntity = Response.status(200).entity(infoContentJSON.toString());
            return respEntity.build();
        } else {
            return super.info(event);
        }
    }


    @Override
    public Response imageThumb(RequestMethodName method, ApiCallEvent event) throws ProxyHandlerException {
        String baseurl = baseUrl();
        String url = baseurl + (baseurl.endsWith("/") ? "" : "/") + "api/client/v7.0/items/" + this.pid + "/image/thumb";
        if (method == RequestMethodName.head) {
            return buildForwardApacheResponseHEAD(url, null, this.pid, true, true);
        } else {
            CDKRequestItem cdkRequestItem = cacheItemHit_PID_USER(url, pid, false, "thumb", event);
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
                ResponseBuilder respEntity = null;
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
                return buildForwardApacheResponseGET(url, null, this.pid, true, true, event, (data, mimetype)-> {

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
                        this.cacheSupport.save(cacheItem);
                    } catch (SQLException e) {
                        LOGGER.log(Level.SEVERE,e.getMessage(),e);
                    }
                });
            }
        }
    }

    @Override
    public Response image(RequestMethodName method,ApiCallEvent event) throws ProxyHandlerException {
        String baseurl = this.forwardUrl();
        String url = baseurl + (baseurl.endsWith("/") ? "" : "/") + "api/cdk/v7.0/forward/item/" + this.pid
                + "/streams/IMG_FULL";
        if (method == RequestMethodName.head) {
            return buildForwardApacheResponseHEAD(url, null, this.pid, true, true);
        } else {
            return buildForwardApacheResponseGET(url, null, this.pid, true, true, event, null);
        }
    }

    @Override
    public Response imagePreview(RequestMethodName method, ApiCallEvent event) throws ProxyHandlerException {
        String baseurl = this.forwardUrl();
        String url = baseurl + (baseurl.endsWith("/") ? "" : "/") + "api/cdk/v7.0/forward/item/" + this.pid
                + "/streams/IMG_PREVIEW";
        if (method == RequestMethodName.head) {
            return buildForwardApacheResponseHEAD(url, null, this.pid, true, true);
        } else {
            return buildForwardApacheResponseGET(url, null, this.pid, true, true, event, null);
        }
    }

    @Override
    public Response mods(RequestMethodName method, ApiCallEvent event) throws ProxyHandlerException {
        if (method == RequestMethodName.head) {
            return super.mods(method, event);
        } else {
            String baseurl = this.forwardUrl();
            String url = baseurl + (baseurl.endsWith("/") ? "" : "/") + "api/cdk/v7.0/forward/item/" + this.pid
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
                //CACHE_LOGGER.log(Level.FINE, "<<< CACHE HIT !!! /metadata/mods");
                return Response.ok(modsString).type("application/xml").build();
            }
        }
    }

    @Override
    public Response zoomifyImageProperties(RequestMethodName method, ApiCallEvent event) throws ProxyHandlerException {
        if (method == RequestMethodName.head) {
            return super.zoomifyImageProperties(method,event);
        } else {
            
            String baseurl = forwardUrl();
            String url = baseurl + (baseurl.endsWith("/") ? "" : "/") + "api/cdk/v7.0/forward/zoomify/" + this.pid
                    + "/ImageProperties.xml";

            return buildForwardApacheResponseGET(url, null, this.pid, true, true, event, null);
       }
    }
    
    @Override
    public Response zoomifyTile(String tileGroupStr, String tileStr, ApiCallEvent event) throws ProxyHandlerException {
        String baseurl = forwardUrl();
        String formatted = String.format("api/cdk/v7.0/forward/zoomify/%s/%s/%s", this.pid, tileGroupStr, tileStr);
        String url = baseurl + (baseurl.endsWith("/") ? "" : "/") + formatted;
        return buildForwardApacheResponseGET(url, null, this.pid, true, true, event, null);
    }

    
    @Override
    public Response textOCR(RequestMethodName method, ApiCallEvent event) throws ProxyHandlerException {
        String baseurl = this.forwardUrl();
        String url = baseurl + (baseurl.endsWith("/") ? "" : "/") + "api/cdk/v7.0/forward/item/" + this.pid
                + "/streams/TEXT_OCR";

        if (method == RequestMethodName.head) {
            return buildForwardApacheResponseHEAD(url, null, this.pid, true, true);
        } else {
            return buildForwardApacheResponseGET(url, null, this.pid, true, true, event, null);
        }
    }

    @Override
    public Response altoOCR(RequestMethodName method, ApiCallEvent event) throws ProxyHandlerException {
        String baseurl = this.forwardUrl();
        String url = baseurl + (baseurl.endsWith("/") ? "" : "/") + "api/cdk/v7.0/forward/item/" + this.pid
                + "/streams/ALTO";
        if (method == RequestMethodName.head) {
            return buildForwardApacheResponseHEAD(url, "application/xml;charset=utf-8", this.pid, true, true);
        } else {
            return buildForwardApacheResponseGET(url, "application/xml;charset=utf-8", this.pid, true, true, event, null);
        }
    }


}
