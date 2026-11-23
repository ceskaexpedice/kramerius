package cz.incad.kramerius.rest.apiNew.client.v70.cdk;

import com.google.inject.Inject;
import cz.incad.kramerius.processes.notifications.GenerationNotification;
import cz.incad.kramerius.processes.notifications.GenerationNotificationDispatcher;
import cz.incad.kramerius.rest.apiNew.client.v70.ClientApiResource;
import cz.incad.kramerius.rest.apiNew.client.v70.libs.Instances;
import cz.incad.kramerius.rest.apiNew.client.v70.libs.OneInstance;
import cz.incad.kramerius.rest.apiNew.client.v70.redirection.item.ProxyItemHandler;
import cz.incad.kramerius.rest.apiNew.exceptions.BadRequestException;
import cz.incad.kramerius.rest.apiNew.exceptions.ForbiddenException;
import cz.incad.kramerius.rest.apiNew.exceptions.InternalErrorException;
import cz.incad.kramerius.security.User;
import cz.incad.kramerius.service.impl.MailerImpl;
import cz.incad.kramerius.utils.IPAddressUtils;
import cz.incad.kramerius.utils.StringUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;
import cz.inovatika.dochub.DocumentType;
import cz.inovatika.dochub.UserContentSpace;
import cz.incad.kramerius.utils.pid.LexerException;
import org.apache.commons.io.IOUtils;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.inject.Provider;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Path("/client/v7.0/userrequests")
public class UsersRequestsResource extends ClientApiResource {

    public static final String DELIMITER = ":";
    public static final String X_API_KEY = "X-API-KEY";


    public static Logger LOGGER = Logger.getLogger(UsersRequestsResource.class.getName());

    @javax.inject.Inject
    @javax.inject.Named("forward-client")
    private CloseableHttpClient apacheClient;

    @javax.inject.Inject
    protected Provider<User> userProvider;

    @Inject
    Instances instances;

    @Inject
    UserContentSpace userContentSpace;

    @Inject
    com.google.inject.Provider<HttpServletRequest> requestProvider;

    @POST
    @Path("notifications")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response notifications(@HeaderParam(X_API_KEY) String apiKey, String notificationBody) {
        JSONObject payload = parseNotification(notificationBody);
        String source = payload.optString("source", null);
        if (!isAllowedNotificationSource(source, apiKey)) {
            LOGGER.log(Level.WARNING, String.format(
                    "CDK notification denied: source=%s, xApiKeyPresent=%s",
                    source,
                    apiKey != null
            ));
            throw new ForbiddenException("Access denied: Valid source API key required.");
        }

        String downloadToken = payload.optString("downloadToken", null);
        String downloadCDKToken = payload.optString("downloadCDKToken", null);
        //String downloadUrl = payload.optString("downloadUrl", null);
        String documentType = payload.optString("documentType", null);

        LOGGER.fine("Payload "+payload.toString()+"\n\n");

        String api = KConfiguration.getInstance().getConfiguration().getString("api.client.point");
        String link = String.format("%s/%s/%s/%s", api, "userrequests/userspace", downloadCDKToken != null ? downloadCDKToken : source+":"+downloadToken, documentType);

        GenerationNotificationDispatcher.notify(
                new GenerationNotification.Builder()
                        .pid(payload.optString("pid", null))
                        .user(payload.optString("user", null))
                        .email(payload.optString("email", null))
                        .documentType(documentType)
                        .downloadCDKToken(downloadCDKToken)
                        .filename(payload.optString("filename", null))
                        .downloadToken(downloadToken)
                        .downloadUrl(link)
                        .title(payload.optString("title", payload.optString("filename", null)))
                        .source(source)
                        .build(),
                GenerationNotificationDispatcher.MODE_LOCAL,
                null,
                new GenerationNotificationDispatcher.LocalMailConfiguration(
                        "generate.notification.email.sender",
                        "generate.notification.email.subject",
                        "generate.notification.email.body",
                        null,
                        null,
                        "Soubor pripraven ke stazeni",
                        "Dobry den,\nsoubor $filename$ je pripraven ke stazeni.\nOdkaz ke stazeni: $link$"
                ),
                this::sendEmailNotification
        );

        return Response.ok(new JSONObject().put("status", "accepted").toString()).type(MediaType.APPLICATION_JSON).build();
    }

    @GET
    @Path("requests/{processId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response requests(@PathParam("processId") String processId) {
        String[] parts = processId.split(DELIMITER, 2);
        if (parts.length != 2) throw new NotFoundException("Invalid process token");
        String source = parts[0];
        String processIdWithoutSource = parts[1];
        try {
            ProxyItemHandler redirectHandler = findRedirectHandler(source);
            if (redirectHandler != null) {
                return redirectHandler.requestsStatus(processIdWithoutSource);
            } else {
                return Response.ok().build();
            }
        } catch (WebApplicationException e) {
            throw e;
        } catch (Throwable e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new InternalErrorException(e.getMessage());
        }
    }

    @GET
    @Path("status/{token}")
    @Produces(MediaType.TEXT_HTML + ";charset=utf-8")
    public Response processStatusPage(@PathParam("token") String token) {
        if (!isValidProcessToken(token)) throw new NotFoundException();
        String html = "<!doctype html><html lang=\"cs\"><head><meta charset=\"utf-8\">"
                + "<meta name=\"viewport\" content=\"width=device-width,initial-scale=1\"><title>Stav exportu</title>"
                + "<style>body{font:16px system-ui,sans-serif;max-width:42rem;margin:12vh auto;padding:0 1.25rem;color:#202124}"
                + "h1{font-size:1.6rem}#state{font-size:1.2rem;font-weight:600;display:flex;gap:.6rem;align-items:center}#icon{font-size:1.5rem}.muted{color:#5f6368}</style></head><body>"
                + "<h1>Stav vašeho požadavku</h1><p id=\"state\"><span id=\"icon\" aria-hidden=\"true\">⏳</span><span id=\"label\">Načítám stav…</span></p>"
                + "<p id=\"pid\"></p><p id=\"process-title\" class=\"muted\"></p><p id=\"detail\" class=\"muted\"></p><p id=\"download\"></p>"
                + "<script>(function(){const label=document.getElementById('label'),icon=document.getElementById('icon'),detail=document.getElementById('detail');"
                + "const pid=document.getElementById('pid'),processTitle=document.getElementById('process-title'),download=document.getElementById('download');"
                + "function poll(){fetch(window.location.pathname+'/data',{cache:'no-store',credentials:'omit'})"
                + ".then(r=>{if(!r.ok)throw new Error();return r.json()}).then(d=>{label.textContent=d.label;detail.textContent=d.updated||'';processTitle.textContent=d.processTitle||'';"
                + "icon.textContent=({PLANNED:'🕒',QUEUED:'🕒',PENDING:'🕒',RUNNING:'🔄',STARTED:'🔄',FAILED:'❌',ERROR:'❌',SUCCESS:'✅',SUCCESSED:'✅',SUCCEEDED:'✅',FINISHED:'✅',COMPLETED:'✅',CANCELLED:'⚪',CANCELED:'⚪'})[d.status]||'ℹ️';"
                + "if(d.pid&&d.documentUrl){const a=document.createElement('a');a.href=d.documentUrl;a.textContent=d.pid;a.title='Otevřít původní dílo';pid.replaceChildren(a)}"
                + "if(d.downloadUrl){const a=document.createElement('a');a.href=d.downloadUrl;a.textContent='Stáhnout soubor';download.replaceChildren(a)}"
                + "if(!d.terminal)setTimeout(poll,5000)}).catch(()=>{label.textContent='Stav se nepodařilo načíst. Zkusíme to znovu.';icon.textContent='⚠️';setTimeout(poll,10000)})}poll()})();</script>"
                + "</body></html>";
        return noCache(Response.ok(html, MediaType.TEXT_HTML_TYPE)).build();
    }

    @GET
    @Path("status/{token}/data")
    @Produces(MediaType.APPLICATION_JSON)
    public Response processStatusData(@PathParam("token") String token) {
        if (!isValidProcessToken(token)) throw new NotFoundException();
        Response remote = requests(token);
        if (remote.getStatus() != Response.Status.OK.getStatusCode()) {
            throw new NotFoundException();
        }
        JSONObject process;
        try {
            process = new JSONObject(readResponseEntity(remote));
        } catch (Exception e) {
            throw new InternalErrorException("Unable to read process status");
        }
        String status = process.optString("status", "UNKNOWN").toUpperCase(java.util.Locale.ROOT);
        String label;
        boolean terminal;
        switch (status) {
            case "PLANNED": case "QUEUED": case "PENDING":
                label = "Požadavek čeká ve frontě."; terminal = false; break;
            case "RUNNING": case "STARTED":
                label = "Export právě probíhá."; terminal = false; break;
            case "SUCCESS": case "SUCCESSED": case "SUCCEEDED": case "FINISHED": case "COMPLETED":
                label = "Proces je dokončen, ověřuji dostupnost souboru."; terminal = false; break;
            case "FAILED": case "ERROR":
                label = "Export se nepodařilo dokončit."; terminal = true; break;
            case "CANCELLED": case "CANCELED":
                label = "Požadavek byl zrušen."; terminal = true; break;
            default:
                label = "Stav požadavku: " + status; terminal = false;
        }
        JSONObject result = new JSONObject().put("status", status).put("label", label).put("terminal", terminal);
        result.put("processTitle", process.optString("description", ""));
        if (process.has("finished")) result.put("updated", process.optString("finished"));
        else if (process.has("started")) result.put("updated", process.optString("started"));
        JSONObject payload = process.optJSONObject("payload");
        String documentUrl = buildDocumentUrl(payload != null ? payload.optString("pid", null) : null);
        if (payload != null && payload.has("pid")) result.put("pid", payload.optString("pid"));
        if (documentUrl != null) result.put("documentUrl", documentUrl);
        if (isSuccessfulStatus(status) && payload != null) {
            String source = token.substring(0, token.indexOf(DELIMITER));
            String pid = payload.optString("pid", null);
            String documentType = documentType(process.optString("profileId", ""));
            String user = payload.optString("user", null);
            if (pid != null && user != null && documentType != null) {
                try {
                    String downloadToken = userContentSpace.getToken(pid, user);
                    ProxyItemHandler sourceHandler = findRedirectHandler(source);
                    if (sourceHandler != null) {
                        Response file = sourceHandler.requestsUserSpace(downloadToken, documentType);
                        if (file.getStatus() == Response.Status.OK.getStatusCode()) {
                            String apiBase = KConfiguration.getInstance().getConfiguration().getString("api.client.point");
                            result.put("downloadUrl", apiBase + "/userrequests/userspace/" + source + DELIMITER
                                    + downloadToken + "/" + documentType);
                            result.put("label", "Export byl dokončen, soubor je připraven ke stažení.");
                            result.put("terminal", true);
                        }
                    }
                } catch (Exception e) {
                    LOGGER.log(Level.WARNING, "Unable to check exported file availability for process " + token, e);
                }
            }
        }
        return noCache(Response.ok(result.toString(), MediaType.APPLICATION_JSON_TYPE)).build();
    }

    private static boolean isSuccessfulStatus(String status) {
        return "SUCCESS".equals(status) || "SUCCEEDED".equals(status)
                || "FINISHED".equals(status) || "COMPLETED".equals(status);
    }

    private static String documentType(String profileId) {
        switch (profileId) {
            case "generate_pdf": return "pdf";
            case "special_needs_ebook": return "epub";
            case "special_needs_text": return "text";
            default: return null;
        }
    }

    private static String readResponseEntity(Response response) throws IOException {
        Object entity = response.getEntity();
        if (entity instanceof String) return (String) entity;
        if (entity instanceof byte[]) return new String((byte[]) entity, StandardCharsets.UTF_8);
        if (entity instanceof StreamingOutput) {
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            ((StreamingOutput) entity).write(output);
            return output.toString(StandardCharsets.UTF_8.name());
        }
        throw new IOException("Unsupported forwarded process response entity: "
                + (entity == null ? "null" : entity.getClass().getName()));
    }

    private static boolean isValidProcessToken(String token) {
        return token != null && token.matches("[A-Za-z0-9_-]{1,64}:[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}");
    }

    private static String buildDocumentUrl(String pid) {
        if (pid == null || !pid.matches("[A-Za-z0-9:_-]{1,256}")) return null;
        String apiUrl = KConfiguration.getInstance().getConfiguration().getString("api.client.point");
        if (!StringUtils.isAnyString(apiUrl)) return null;
        String apiSuffix = "/api/client/v7.0";
        if (apiUrl.endsWith(apiSuffix)) apiUrl = apiUrl.substring(0, apiUrl.length() - apiSuffix.length());
        while (apiUrl.endsWith("/")) apiUrl = apiUrl.substring(0, apiUrl.length() - 1);
        return apiUrl + "/uuid/" + pid;
    }

    private static Response.ResponseBuilder noCache(Response.ResponseBuilder response) {
        javax.ws.rs.core.CacheControl cacheControl = new javax.ws.rs.core.CacheControl();
        cacheControl.setNoCache(true);
        cacheControl.setNoStore(true);
        return response.cacheControl(cacheControl).header("X-Content-Type-Options", "nosniff");
    }

    @GET
    @Path("{source}/userspace")
    @Produces(MediaType.APPLICATION_JSON)
    public Response userspace(@PathParam("source") String source) {
        try {
            OneInstance oneInstance = instances.find(source);
            if (oneInstance == null) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
            JSONArray aggregated = new JSONArray();
            ProxyItemHandler redirectHandler = findRedirectHandler(oneInstance);
            if (redirectHandler == null) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
            Response response = redirectHandler.requestsUserSpace();
            String json = (String) response.getEntity();
            JSONArray responseArray = new JSONArray(json);
            for (int i = 0; i < responseArray.length(); i++) {
                JSONObject item = responseArray.getJSONObject(i);
                item.put("source", oneInstance.getName());
                aggregated.put(item);
            }
            return Response.ok(aggregated.toString()).type(MediaType.APPLICATION_JSON).build();
        } catch (WebApplicationException e) {
            throw e;
        } catch (Throwable e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new InternalErrorException(e.getMessage());
        }
    }

//    @GET
//    @Path("userspace}")
//    @Produces(MediaType.APPLICATION_JSON)
//    public Response userspace() {
//        try {
//            List<OneInstance> oneInstances = instances.allInstances();
//            JSONArray aggregated = new JSONArray();
//            for (OneInstance instance : oneInstances) {
//                ProxyItemHandler redirectHandler = findRedirectHandler(instance);
//                if (redirectHandler == null) {
//                    continue;
//                }
//                Response response = redirectHandler.requestsUserSpace();
//                String json = (String) response.getEntity();
//                JSONArray responseArray = new JSONArray(json);
//                for (int i = 0; i < responseArray.length(); i++) {
//                    JSONObject item = responseArray.getJSONObject(i);
//                    item.put("source", instance.getName());
//                    aggregated.put(item);
//                }
//            }
//            return Response.ok(aggregated.toString()).type(MediaType.APPLICATION_JSON).build();
//        } catch (WebApplicationException e) {
//            throw e;
//        } catch (Throwable e) {
//            LOGGER.log(Level.SEVERE, e.getMessage(), e);
//            throw new InternalErrorException(e.getMessage());
//        }
//    }

    @GET
    @Path("userspace")
    @Produces(MediaType.APPLICATION_JSON)
    public Response userspace() {
        JSONArray merged = new JSONArray();
        for (OneInstance instance : this.instances.enabledInstances()) {
            String source = instance.getName();
            if (!instance.hasFullAccess()) {
                continue;
            }
            try {
                JSONArray sourceItems = requestUserspace(source);
                for (int i = 0; i < sourceItems.length(); i++) {
                    JSONObject item = sourceItems.getJSONObject(i);
                    item.put("source", source);
                    if (item.has("token")) {
                        item.put("token", source + DELIMITER + item.getString("token"));
                    }
                    merged.put(item);
                }
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, String.format("Cannot fetch userspace list from source=%s", source), e);
            }
        }
        return Response.ok(merged.toString()).type(MediaType.APPLICATION_JSON).build();
    }

    @GET
    @Path("userspace/{spacetoken}/{docType}")
    public Response userspace(@PathParam("spacetoken") String token, @PathParam("docType") String docTypeStr) {
        String[] parts = token.split(DELIMITER, 2);
        if (parts.length != 2 || !StringUtils.isAnyString(parts[0]) || !StringUtils.isAnyString(parts[1])) {
            throw new BadRequestException("Invalid userspace token: " + token);
        }
        String source = parts[0];
        String tokenWithoutSource = parts[1];
        DocumentType docType = parseDocumentType(docTypeStr);
        try {
            ProxyItemHandler redirectHandler = findRedirectHandler(source);
            if (redirectHandler != null) {
                Response response = redirectHandler.requestsUserSpace(tokenWithoutSource, docTypeStr);
                if (response.getStatus() == Response.Status.OK.getStatusCode()) {
                    return Response.fromResponse(response)
                            .header("Content-Disposition", contentDisposition(tokenWithoutSource, docType))
                            .type(mediaType(docType))
                            .build();
                }
                return response;
            } else {
                return Response.ok().build();
            }
        } catch (WebApplicationException e) {
            throw e;
        } catch (Throwable e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new InternalErrorException(e.getMessage());
        }
    }

    private DocumentType parseDocumentType(String docTypeStr) {
        try {
            return DocumentType.valueOf(docTypeStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid document type: " + docTypeStr);
        }
    }

    private String contentDisposition(String token, DocumentType docType) {
        return "attachment; filename=\"" + switch (docType) {
            case PDF -> "export_" + token + ".pdf";
            case TEXT -> "export_" + token + ".txt";
            case EPUB -> "export_" + token + ".epub";
        } + "\"";
    }

    private String mediaType(DocumentType docType) {
        return switch (docType) {
            case PDF -> "application/pdf";
            case TEXT -> "text/plain";
            case EPUB -> "application/epub+zip";
        };
    }

    private ProxyItemHandler findRedirectHandler(String source) throws LexerException, IOException {
        OneInstance found = instances.find(source);
        if (found != null) {
            return findRedirectHandler(found);
        } else {
            return null;
        }
    }

    private ProxyItemHandler findRedirectHandler(OneInstance oneInstance) {
        String remoteAddress = IPAddressUtils.getRemoteAddress(this.requestProvider.get(), KConfiguration.getInstance().getConfiguration());
        ProxyItemHandler proxyHandler = oneInstance.createProxyItemHandler(this.userProvider.get(), this.apacheClient, null, this.solrAccess, oneInstance.getName(), null, remoteAddress);
        return proxyHandler;
    }

    private JSONArray requestUserspace(String source) throws IOException {
        String baseurl = forwardUrl(source);
        String url = baseurl + (baseurl.endsWith("/") ? "" : "/") + "api/cdk/v7.0/forward/userspace";
        HttpGet get = new HttpGet(url);
        get.setHeader("User-Agent", "CDK/1.0");
        addCdkHeaders(get, source);

        try (CloseableHttpResponse response = apacheClient.execute(get)) {
            int code = response.getCode();
            if (code == Response.Status.OK.getStatusCode()) {
                String json = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
                return new JSONArray(json);
            } else {
                LOGGER.log(Level.WARNING, String.format("Userspace list request failed: source=%s, status=%d, url=%s", source, code, url));
                return new JSONArray();
            }
        }
    }

    private void addCdkHeaders(HttpGet get, String source) {
        User user = this.userProvider.get();
        if (user != null && user.getId() != -1) {
            String header = prepareForwardHeader(source);
            get.setHeader("CDK-TOKEN-PARAMETERS", header);
            get.setHeader("CDK_TOKEN_PARAMETERS", header);
        }
        String apiKey = apiKey(source);
        if (apiKey != null && !apiKey.isEmpty()) {
            get.addHeader("X-API-KEY", apiKey);
        }
    }

    private String prepareForwardHeader(String source) {
        KConfiguration configuration = KConfiguration.getInstance();
        String prefixHeaders = configuration.getConfiguration().getString("cdk.shibboleth.forward.headers");
        boolean shibbolethAttributes = configuration.getConfiguration()
                .getBoolean("cdk.collections.sources." + source + ".shibboleth_attributes", true);

        String header = "";
        User user = this.userProvider.get();
        if (shibbolethAttributes && user != null) {
            header = header + user.getSessionAttributes().keySet().stream()
                    .map(key -> "header_" + key + "=" + user.getSessionAttributes().get(key))
                    .collect(Collectors.joining("|"));
        }

        String remoteAddress = IPAddressUtils.getRemoteAddress(this.requestProvider.get(), configuration.getConfiguration());
        if (remoteAddress != null) {
            header = header + (header.isEmpty() ? "" : "|") + "header_ip_address=" + remoteAddress;
        }
        if (prefixHeaders != null && !prefixHeaders.isEmpty()) {
            header = prefixHeaders + header;
        }
        return header;
    }

    private String forwardUrl(String source) {
        return KConfiguration.getInstance().getConfiguration()
                .getString("cdk.collections.sources." + source + ".forwardurl");
    }

    private String apiKey(String source) {
        return KConfiguration.getInstance().getConfiguration()
                .getString("cdk.collections.sources." + source + ".apikey");
    }

    private JSONObject parseNotification(String notificationBody) {
        if (!StringUtils.isAnyString(notificationBody)) {
            throw new BadRequestException("Notification body is empty");
        }
        try {
            return new JSONObject(notificationBody);
        } catch (Exception e) {
            throw new BadRequestException("Invalid notification JSON");
        }
    }

    private boolean isAllowedNotificationSource(String source, String apiKey) {
        if (!StringUtils.isAnyString(source) || !StringUtils.isAnyString(apiKey)) {
            return false;
        }
        String expectedApiKey = apiKey(source);
        return StringUtils.isAnyString(expectedApiKey) && expectedApiKey.equals(apiKey);
    }

    private void sendEmailNotification(String emailFrom, List<Object> recipients, String subject, String text) throws javax.mail.MessagingException {
        LOGGER.info("\t Sending email notification: from=" + emailFrom + ", recipients=" + recipients + ", subject=" + subject + ", text=" + text);
        Session session = new MailerImpl().getSession(null, null);
        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(emailFrom));
        InternetAddress[] addresses = new InternetAddress[recipients.size()];
        for (int i = 0; i < recipients.size(); i++) {
            addresses[i] = new InternetAddress(String.valueOf(recipients.get(i)));
        }
        message.setRecipients(Message.RecipientType.TO, addresses);
        message.setSubject(subject);
        message.setText(text);
        Transport.send(message);
    }

}
