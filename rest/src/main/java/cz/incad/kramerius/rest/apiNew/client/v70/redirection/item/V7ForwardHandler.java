package cz.incad.kramerius.rest.apiNew.client.v70.redirection.item;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.StreamingOutput;

import cz.incad.kramerius.processes.client.ProcessManagerMapper;
import cz.incad.kramerius.rest.apiNew.client.v70.cdk.UsersRequestsResource;
import cz.incad.kramerius.rest.apiNew.client.v70.redirection.DeleteTriggerSupport;
import cz.incad.kramerius.utils.StringUtils;
import cz.inovatika.cdk.cache.CDKRequestCacheSupport;
import cz.inovatika.cdk.cache.CDKRequestItem;
import cz.inovatika.cdk.cache.impl.CDKRequestItemFactory;
import cz.inovatika.monitoring.ApiCallEvent;
import org.apache.commons.io.IOUtils;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;

import cz.incad.kramerius.SolrAccess;
import cz.incad.kramerius.rest.apiNew.admin.v70.reharvest.ReharvestManager;
import cz.incad.kramerius.rest.apiNew.client.v70.libs.Instances;
import cz.incad.kramerius.rest.apiNew.client.v70.redirection.ProxyHandlerException;
import cz.incad.kramerius.security.User;
import cz.incad.kramerius.utils.conf.KConfiguration;
import org.apache.hc.core5.net.URIBuilder;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

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
        super(cacheSupport, reharvestManager, instances, user, closeableHttpClient, triggerSupport, executorService, solrAccess, source, pid, remoteAddr);
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
        return Response.status(500).build();
        //return super.info(event);
    }


    @Override
    public Response imageThumb(RequestMethodName method, ApiCallEvent event) throws ProxyHandlerException {
        String baseurl = baseUrl();
        String url = baseurl + (baseurl.endsWith("/") ? "" : "/") + "api/client/v7.0/items/" + this.pid + "/image/thumb";

        if (method == RequestMethodName.head) {
            return buildForwardApacheResponseHEAD(apiKey(), url, null, this.pid, true, true);
        }

        CDKRequestItem cdkRequestItem = cacheItemHit_PID_USER(url, pid, false, "thumb", event);
        if (cdkRequestItem != null) {
            final ByteBuffer buffer = ((ByteBuffer) cdkRequestItem.getData()).duplicate();
            final int contentLength = buffer.remaining();

            String cacheMimeType = cdkRequestItem.getMimeType();
            LOGGER.log(Level.FINE, String.format("[THUMB-CACHE-HIT] PID: %s | Mime-Type z DB: %s | Vypocitana delka (contentLength): %d",
                    this.pid, (cacheMimeType != null ? cacheMimeType : "NULL!"), contentLength));

            final byte[] safeBytes = new byte[contentLength];
            buffer.get(safeBytes);
            LOGGER.log(Level.FINE, String.format("[THUMB-CACHE-HIT] PID: %s, Skutecne vycteno do safeBytes: %d", this.pid, safeBytes.length));


            ResponseBuilder respEntity = Response.ok(safeBytes);
            if (cdkRequestItem.getMimeType() != null && !cdkRequestItem.getMimeType().isEmpty()) {
                respEntity.type(cdkRequestItem.getMimeType());
            } else {
                respEntity.type("image/jpeg");
            }

            if (cacheMimeType != null && !cacheMimeType.isEmpty()) {
                respEntity.type(cacheMimeType);
            } else {
                LOGGER.log(Level.WARNING, String.format("[THUMB-WARN] PID: %s nema v cache Mime-Type! Nastavuji image/jpeg jako fallback.", this.pid));
                respEntity.type("image/jpeg");
            }

            if (cdkRequestItem.getMimeType() != null) {
                respEntity.type(cdkRequestItem.getMimeType());
            }

            respEntity.header("Content-Length", String.valueOf(contentLength));

            Response responseToReturn = respEntity.build();
            Object finalContentType = responseToReturn.getMetadata().getFirst("Content-Type");
            Object finalContentLength = responseToReturn.getMetadata().getFirst("Content-Length");

            LOGGER.log(Level.FINE, String.format("[THUMB-RESPONSE-OUT] PID: %s | Odesilam do JAX-RS -> Content-Type: %s | Content-Length: %s",
                    this.pid, finalContentType, finalContentLength));

            return responseToReturn;

        } else {
            LOGGER.log(Level.FINE, String.format("[THUMB-CACHE-MISS] Data nejsou v cache, jdu na Apache pro PID: %s", this.pid));
            return buildForwardApacheResponseGET(url, apiKey(), null, this.pid, false, true, event, (data, mimetype) -> {
                CompletableFuture.runAsync(() -> {
                    try {
                        LOGGER.log(Level.FINE, String.format("[THUMB-ASYNC-SAVE] Ukladam do cache pro PID: %s, velikost dat: %d", this.pid, data.length));

                        CDKRequestItem<ByteBuffer> cacheItem = (CDKRequestItem<ByteBuffer>) CDKRequestItemFactory.createCacheItem(
                                ByteBuffer.wrap(Arrays.copyOf(data, data.length)),
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
    public Response image(RequestMethodName method, ApiCallEvent event) throws ProxyHandlerException {
        String baseurl = this.forwardUrl();
        String url = baseurl + (baseurl.endsWith("/") ? "" : "/") + "api/cdk/v7.0/forward/item/" + this.pid
                + "/streams/IMG_FULL";
        if (method == RequestMethodName.head) {
            return buildForwardApacheResponseHEAD(apiKey(), url, null, this.pid, false, true);
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
            return buildForwardApacheResponseHEAD(apiKey(), url, null, this.pid, false, true);
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
            String modsString = super.cacheStringHit_PID_USER(url, this.pid, false, "mods", event);
            if (modsString == null) {
                return buildForwardApacheResponseGET(url, apiKey(), null, this.pid, true, false, event, (data, mimeType) -> {
                    CompletableFuture.runAsync(() -> {
                        try {
                            CDKRequestItem<String> cacheItem = (CDKRequestItem<String>) CDKRequestItemFactory.createCacheItem(
                                    new String(data, Charset.forName("UTF-8")),
                                    "application/xml",
                                    url,
                                    this.pid,
                                    source,
                                    LocalDateTime.now(),
                                    null
                            );
                            LOGGER.fine(String.format("Storing cache item %s", cacheItem.toString()));
                            this.cacheSupport.save(cacheItem);
                        } catch (SQLException e) {
                            LOGGER.log(Level.SEVERE, e.getMessage(), e);
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
            return super.zoomifyImageProperties(method, event);
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
    public Response requestsStatus(String processId) throws ProxyHandlerException {
        String baseurl = this.forwardUrl();
        String url = baseurl + (baseurl.endsWith("/") ? "" : "/") + "api/cdk/v7.0/forward/requests/" + processId;
        return buildForwardApacheResponseGET(url, apiKey(), null, this.pid, true, true, null, null);
    }

    @Override
    public Response requestsUserSpace(String token, String docType) throws ProxyHandlerException {
        String baseurl = this.forwardUrl();
        String url = baseurl + (baseurl.endsWith("/") ? "" : "/") + "api/cdk/v7.0/forward/userspace/" + token + "/" + docType;
        return buildForwardApacheResponseGET(url, apiKey(), null, this.pid, true, true, null, null);
    }

    @Override
    public Response requestsUserSpace() throws ProxyHandlerException {
        String baseurl = this.forwardUrl();
        String url = baseurl + (baseurl.endsWith("/") ? "" : "/") + "api/cdk/v7.0/forward/userspace";
        return buildForwardApacheResponseGET(url, apiKey(), null, this.pid, true, true, null, null);
    }

    @Override
    public Response pdfSelection(String pidsParam, String firstPageType, String format, String language) throws ProxyHandlerException {

        try {
            String baseurl = this.forwardUrl();
            String url = baseurl + (baseurl.endsWith("/") ? "" : "/") + "api/cdk/v7.0/forward/pdf/selection";
            URIBuilder builder = new URIBuilder(url);
            builder.setParameter("pids", pidsParam);
            if (StringUtils.isAnyString(firstPageType)) {
                builder.setParameter("firstPageType", firstPageType);
            }
            if (StringUtils.isAnyString(format)) {
                builder.setParameter("format", format);
            }
            if (StringUtils.isAnyString(language)) {
                builder.setParameter("language", language);

            }
            return buildForwardApacheResponseGET(builder.toString(), apiKey(), null, this.pid, true, true, null, null);
        } catch (URISyntaxException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Override
    public Response  collectionClips(ApiCallEvent event) {
        String baseurl = this.forwardUrl();
        String url = baseurl + (baseurl.endsWith("/") ? "" : "/") + "api/cdk/v7.0/forward/" + pid + "collection/cuttings";
        return buildForwardApacheResponseGET(url, apiKey(), null, this.pid, true, true, event, null);
    }

    @Override
    public Response  collectionThumb(ApiCallEvent event, String thumbId) {
        String baseurl = this.forwardUrl();
        String url = baseurl + (baseurl.endsWith("/") ? "" : "/") + "api/cdk/v7.0/forward/" + pid + "collection/cuttings/image/" + thumbId;
        return buildForwardApacheResponseGET(url, apiKey(), null, this.pid, true, true, event, null);
    }

    @Override
    public Response requests(String reqType, String lang, JSONObject reqDefinition) throws ProxyHandlerException {
        try {
            String baseurl = this.forwardUrl();
            String url = baseurl + (baseurl.endsWith("/") ? "" : "/") + "api/cdk/v7.0/forward/" + this.pid
                    + "/requests/" + reqType;
            JSONObject forwardedReqDefinition = reqDefinition != null ? new JSONObject(reqDefinition.toString()) : new JSONObject();
            forwardedReqDefinition.put("notificationMode", "cdk");
            forwardedReqDefinition.put("notificationCallbackUrl", notificationCallbackUrl());
            forwardedReqDefinition.put("notificationSource", this.source);
            URIBuilder builder = new URIBuilder(url);
            if (lang != null) {
                builder.setParameter("lang", lang);
            }
            AtomicReference<byte[]> responseBytes = new AtomicReference<>();
            AtomicReference<String> responseMimeType = new AtomicReference<>();
            Response response = buildForwardApacheResponsePOST(builder.toString(), forwardedReqDefinition, apiKey(), this.pid, true, (data, mimeType) -> {
                responseBytes.set(data);
                responseMimeType.set(mimeType);
            });
            int status = response.getStatus();
            LOGGER.log(Level.FINE, "requests response status: " + status);
            if (status != Response.Status.OK.getStatusCode()) {
                LOGGER.log(Level.WARNING, String.format(
                        "Forwarded item request failed: source=%s, pid=%s, reqType=%s, status=%d, url=%s",
                        source, this.pid, reqType, status, builder.toString()
                ));
                return response;
            }

            byte[] bytes = responseBytes.get();
            String entity = bytes != null ? new String(bytes, StandardCharsets.UTF_8) : "";
            JSONObject jsonObject = new JSONObject(entity);
            String prefix = source + UsersRequestsResource.DELIMITER;
            if (jsonObject.has(ProcessManagerMapper.PCP_PROCESS_ID)) {
                String originalPid = jsonObject.getString(ProcessManagerMapper.PCP_PROCESS_ID);
                jsonObject.put(ProcessManagerMapper.PCP_PROCESS_ID, prefix + originalPid);
            }
            if (jsonObject.has("token")) {
                String originalToken = jsonObject.getString("token");
                jsonObject.put("token", prefix + originalToken);
            }
            String mimeType = responseMimeType.get();
            return Response.status(status)
                    .entity(jsonObject.toString())
                    .type(mimeType != null ? mimeType : MediaType.APPLICATION_JSON)
                    .build();
        } catch (URISyntaxException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    private String notificationCallbackUrl() {
        String configured = KConfiguration.getInstance().getConfiguration()
                .getString("generate.notification.cdk.callback_url", null);
        if (StringUtils.isAnyString(configured)) {
            return configured;
        }
        String clientPoint = KConfiguration.getInstance().getConfiguration()
                .getString("api.client.point");
        return clientPoint + (clientPoint.endsWith("/") ? "" : "/") + "userrequests/notifications";
    }

    @Override
    public Response  collectionClips(ApiCallEvent event) {
        String baseurl = this.forwardUrl();
        String url = baseurl + (baseurl.endsWith("/") ? "" : "/") + "api/cdk/v7.0/forward/" + pid + "collection/cuttings";
        return buildForwardApacheResponseGET(url, apiKey(), null, this.pid, true, true, event, null);
    }

    @Override
    public Response  collectionThumb(ApiCallEvent event, String thumbId) {
        String baseurl = this.forwardUrl();
        String url = baseurl + (baseurl.endsWith("/") ? "" : "/") + "api/cdk/v7.0/forward/" + pid + "collection/cuttings/image/" + thumbId;
        return buildForwardApacheResponseGET(url, apiKey(), null, this.pid, true, true, event, null);
    }

    @Override
    public Response textOCR(RequestMethodName method, ApiCallEvent event) throws ProxyHandlerException {
        String baseurl = this.forwardUrl();
        String url = baseurl + (baseurl.endsWith("/") ? "" : "/") + "api/cdk/v7.0/forward/item/" + this.pid
                + "/streams/TEXT_OCR";

        if (method == RequestMethodName.head) {
            return buildForwardApacheResponseHEAD(apiKey(), url, null, this.pid, false, true);
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
            return buildForwardApacheResponseHEAD(apiKey(), url, "application/xml;charset=utf-8", this.pid, false, true);
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
        return inputStream(url, apiKey());
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
