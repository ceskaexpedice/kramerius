package cz.incad.kramerius.rest.apiNew.client.v70.redirection.item;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import cz.incad.kramerius.rest.apiNew.client.v70.redirection.DeleteTriggerSupport;
import cz.inovatika.cdk.cache.CDKRequestCacheSupport;
import cz.inovatika.cdk.cache.CDKRequestItem;
import cz.inovatika.cdk.cache.impl.CDKRequestItemFactory;
import cz.inovatika.monitoring.ApiCallEvent;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.StreamingOutput;
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

    public static final Logger LOGGER = Logger.getLogger(V7ForwardHandler.class.getName());

    public V7ForwardHandler(CDKRequestCacheSupport cacheSupport,
                            ReharvestManager reharvestManager,
                            Instances instances,
                            User user,
                            CloseableHttpClient closeableHttpClient,
                            DeleteTriggerSupport triggerSupport,
                            ExecutorService executorService,
                            SolrAccess solrAccess,
                            String source,
                            String pid,
                            String remoteAddr) {
        super(cacheSupport, reharvestManager,instances, user,  closeableHttpClient, triggerSupport, executorService, solrAccess, source, pid, remoteAddr);
	}

    protected String forwardUrl() {
        String baseurl = KConfiguration.getInstance().getConfiguration()
                .getString("cdk.collections.sources." + this.source + ".forwardurl");
        return baseurl;
    }

    @Override
    public Response info(ApiCallEvent event) throws ProxyHandlerException {
        long startTime = System.currentTimeMillis();
        String baseurl = this.forwardUrl();
        String info = baseurl + (baseurl.endsWith("/") ? "" : "/") + "api/cdk/v7.0/forward/info/" + this.pid;
        try {
            HttpGet get = apacheGet(info, apiKey(), true);
            try (CloseableHttpResponse response = apacheClient.execute(get)) {
                String result = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
                return Response.status(response.getCode()).entity(result.toString()).build();
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "ProvidedBy fetch failed", e);
        }
        return super.info(event);
    }


    @Override
    public Response imageThumb(RequestMethodName method, ApiCallEvent event) throws ProxyHandlerException {
        String baseurl = baseUrl();
        String url = baseurl + (baseurl.endsWith("/") ? "" : "/") + "api/client/v7.0/items/" + this.pid + "/image/thumb";

        if (method == RequestMethodName.head) {
            return buildForwardApacheResponseHEAD(url, apiKey(), null, this.pid, true, true);
        }

        CDKRequestItem cdkRequestItem = cacheItemHit_PID_USER(url, pid, false, "thumb", event);
        if (cdkRequestItem != null) {
            ByteBuffer buffer = (ByteBuffer) cdkRequestItem.getData();
            final int contentLength = buffer.remaining();
            StreamingOutput stream = output -> {
                if (buffer.hasArray()) {
                    output.write(buffer.array(), buffer.arrayOffset() + buffer.position(), contentLength);
                } else {
                    byte[] bytes = new byte[contentLength];
                    buffer.get(bytes);
                    output.write(bytes);
                }
                output.flush();
            };

            Response.ResponseBuilder respEntity = Response.ok(stream);
            if (cdkRequestItem.getMimeType() != null) {
                respEntity.type(cdkRequestItem.getMimeType());
            }
            respEntity.header("Content-Length", contentLength);
            return respEntity.build();
        } else {
            return buildForwardApacheResponseGET(url, apiKey(), null, this.pid, true, true, event, (data, mimetype) -> {
                CompletableFuture.runAsync(() -> {
                    try {
                        CDKRequestItem<ByteBuffer> cacheItem = (CDKRequestItem<ByteBuffer>) CDKRequestItemFactory.createCacheItem(
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
                        LOGGER.log(Level.SEVERE, "Async thumb cache save failed: " + e.getMessage(), e);
                    }
                }, this.executor);
            });
        }
    }

    @Override
    public Response image(RequestMethodName method,ApiCallEvent event) throws ProxyHandlerException {
        String baseurl = this.forwardUrl();
        String url = baseurl + (baseurl.endsWith("/") ? "" : "/") + "api/cdk/v7.0/forward/item/" + this.pid
                + "/streams/IMG_FULL";
        if (method == RequestMethodName.head) {
            return buildForwardApacheResponseHEAD(url, apiKey(), null, this.pid, false, true);
        } else {
            return buildForwardApacheResponseGET(url, apiKey(), null, this.pid, false, true, event, null);
        }
    }

    @Override
    public Response imagePreview(RequestMethodName method, ApiCallEvent event) throws ProxyHandlerException {
        String baseurl = this.forwardUrl();
        String url = baseurl + (baseurl.endsWith("/") ? "" : "/") + "api/cdk/v7.0/forward/item/" + this.pid
                + "/streams/IMG_PREVIEW";
        if (method == RequestMethodName.head) {
            return buildForwardApacheResponseHEAD(url, apiKey(), null, this.pid, false, true);
        } else {
            return buildForwardApacheResponseGET(url, apiKey(), null, this.pid, false, true, event, null);
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
                return buildForwardApacheResponseGET(url, apiKey(), null, this.pid, true, false, event, (data, mimeType)-> {
                    CompletableFuture.runAsync(() -> {
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
            return buildForwardApacheResponseGET(url, apiKey(), null, this.pid, true, true, event, null);
       }
    }
    
    @Override
    public Response zoomifyTile(String tileGroupStr, String tileStr, ApiCallEvent event) throws ProxyHandlerException {
        String baseurl = forwardUrl();
        String formatted = String.format("api/cdk/v7.0/forward/zoomify/%s/%s/%s", this.pid, tileGroupStr, tileStr);
        String url = baseurl + (baseurl.endsWith("/") ? "" : "/") + formatted;
        return buildForwardApacheResponseGET(url, apiKey(), null, this.pid, true, true, event, null);
    }


    @Override
    public Response textOCR(RequestMethodName method, ApiCallEvent event) throws ProxyHandlerException {
        String baseurl = this.forwardUrl();
        String url = baseurl + (baseurl.endsWith("/") ? "" : "/") + "api/cdk/v7.0/forward/item/" + this.pid
                + "/streams/TEXT_OCR";

        if (method == RequestMethodName.head) {
            return buildForwardApacheResponseHEAD(url, apiKey(), null, this.pid, false, true);
        } else {
            return buildForwardApacheResponseGET(url, apiKey(), null, this.pid, false, true, event, null);
        }
    }

    @Override
    public Response altoOCR(RequestMethodName method, ApiCallEvent event) throws ProxyHandlerException {
        String baseurl = this.forwardUrl();
        String url = baseurl + (baseurl.endsWith("/") ? "" : "/") + "api/cdk/v7.0/forward/item/" + this.pid
                + "/streams/ALTO";
        if (method == RequestMethodName.head) {
            return buildForwardApacheResponseHEAD(url, apiKey(), "application/xml;charset=utf-8", this.pid, false, true);
        } else {
            return buildForwardApacheResponseGET(url, apiKey(), "application/xml;charset=utf-8", this.pid, false, true, event, null);
        }
    }


    @Override
    public boolean isStreamDCAvaiable(ApiCallEvent event) throws ProxyHandlerException {
        String baseurl = this.forwardUrl();
        String url = baseurl + (baseurl.endsWith("/") ? "" : "/") + "api/cdk/v7.0/forward/item/" + this.pid
                + "/streams/DC";
        return exists(url, apiKey());
    }

    @Override
    public boolean isStreamBiblioModsAvaiable(ApiCallEvent event) throws ProxyHandlerException {
        String baseurl = this.forwardUrl();
        String url = baseurl + (baseurl.endsWith("/") ? "" : "/") + "api/cdk/v7.0/forward/item/" + this.pid
                + "/streams/DC";
        return exists(url, apiKey());
    }

    @Override
    public InputStream directStreamDC(ApiCallEvent event) throws ProxyHandlerException {
        String baseurl = this.forwardUrl();
        String url = baseurl + (baseurl.endsWith("/") ? "" : "/") + "api/cdk/v7.0/forward/item/" + this.pid
                + "/streams/DC";
        return inputStream(url, apiKey());
    }

    @Override
    public InputStream directStreamBiblioMods(ApiCallEvent event) throws ProxyHandlerException {
        String baseurl = this.forwardUrl();
        String url = baseurl + (baseurl.endsWith("/") ? "" : "/") + "api/cdk/v7.0/forward/item/" + this.pid
                + "/streams/BIBLIO_MODS";
        return inputStream(url,apiKey());
    }




//    @Override
//    public void iiifTileAsync(String pid, String iiifPath, HttpServletResponse resp, ApiCallEvent event) throws ProxyHandlerException {
//        String baseurl = this.forwardUrl();
//        String url = baseurl + (baseurl.endsWith("/") ? "" : "/") + "api/cdk/v7.0/forward/item/" + this.pid
//                + "/streams/BIBLIO_MODS";
//
//        // 1. Sestavíme URL na zdrojový Kramerius
//        OneInstance remote = instances.instance(this.source);
//        String remoteUrl = remote.getBaseUrl() + "/search/iiif/" + pid + "/" + iiifPath;
//
//        // 2. Použijeme tvou asynchronní metodu (kterou vložíš do ProxyHandlerSupport)
//        try {
//            this.copyFromImageServer(remoteUrl, resp);
//        } catch (IOException e) {
//            throw new ProxyHandlerException("Failed to proxy IIIF tile for " + pid, e);
//        }
//    }
}
