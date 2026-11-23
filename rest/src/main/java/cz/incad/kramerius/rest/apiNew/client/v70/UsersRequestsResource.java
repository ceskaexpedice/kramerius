package cz.incad.kramerius.rest.apiNew.client.v70;

import com.google.inject.Inject;
import cz.incad.kramerius.processes.client.ProcessManagerClient;
import cz.incad.kramerius.processes.client.ProcessManagerMapper;
import cz.incad.kramerius.security.User;
import cz.inovatika.dochub.DocumentType;
import cz.inovatika.dochub.UserContentBundle;
import cz.inovatika.dochub.UserContentSpace;
import org.apache.commons.io.IOUtils;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.inject.Provider;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import javax.ws.rs.core.CacheControl;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

@Path("/client/v7.0/userrequests")
public class UsersRequestsResource extends ClientApiResource {

    public static Logger LOGGER = Logger.getLogger(UsersRequestsResource.class.getName());

    @javax.inject.Inject
    @javax.inject.Named("forward-client")
    private CloseableHttpClient apacheClient;

    @Inject
    UserContentSpace userContentSpace;

    @javax.inject.Inject
    protected Provider<User> userProvider;


    @GET
    @Path("requests/{reqid}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response requests(@PathParam("reqid") String reqid) {

        ProcessManagerClient processManagerClient = new ProcessManagerClient(apacheClient);
        JSONObject process = processManagerClient.getProcess(reqid);
        if (process != null) {
            return Response.ok().entity(process.toString()).build();
        } else {
            throw new NotFoundException("there's no process with process_id=" + reqid);
        }
    }

    /**
     * Public bearer-link page for checking a queued process. The process UUID is
     * treated as an unguessable capability; keep the response free of process
     * payloads, owner details and logs.
     */
    @GET
    @Path("status/{token}")
    @Produces(MediaType.TEXT_HTML + ";charset=utf-8")
    public Response processStatusPage(@PathParam("token") String token) {
        if (!isValidProcessToken(token)) {
            throw new NotFoundException();
        }
        String html = "<!doctype html><html lang=\"cs\"><head><meta charset=\"utf-8\">"
                + "<meta name=\"viewport\" content=\"width=device-width,initial-scale=1\"><title>Stav exportu</title>"
                + "<style>body{font:16px system-ui,sans-serif;max-width:42rem;margin:12vh auto;padding:0 1.25rem;color:#202124}"
                + "h1{font-size:1.6rem}#state{font-size:1.2rem;font-weight:600;display:flex;gap:.6rem;align-items:center}#icon{font-size:1.5rem}.muted{color:#5f6368}</style></head><body>"
                + "<h1>Stav vašeho požadavku</h1><p id=\"state\"><span id=\"icon\" aria-hidden=\"true\">⏳</span><span id=\"label\">Načítám stav…</span></p>"
                + "<p id=\"pid\"></p><p id=\"process-title\" class=\"muted\"></p><p id=\"detail\" class=\"muted\"></p><p id=\"download\"></p>"
                + "<script>(function(){const label=document.getElementById('label'),icon=document.getElementById('icon'),detail=document.getElementById('detail');"
                + "const pid=document.getElementById('pid'),processTitle=document.getElementById('process-title');"
                + "const download=document.getElementById('download');const url=window.location.pathname+'/data';"
                + "function poll(){fetch(url,{cache:'no-store',credentials:'omit'}).then(r=>{if(!r.ok)throw new Error();return r.json()})"
                + ".then(d=>{label.textContent=d.label;detail.textContent=d.updated||'';processTitle.textContent=d.processTitle||'';"
                + "icon.textContent=({PLANNED:'🕒',QUEUED:'🕒',PENDING:'🕒',RUNNING:'🔄',STARTED:'🔄',FAILED:'❌',ERROR:'❌',SUCCESS:'✅',SUCCESSED:'✅',SUCCEEDED:'✅',FINISHED:'✅',COMPLETED:'✅',CANCELLED:'⚪',CANCELED:'⚪'})[d.status]||'ℹ️';"
                + "if(d.pid&&d.documentUrl){const a=document.createElement('a');a.href=d.documentUrl;a.textContent=d.pid;a.title='Otevřít původní dílo';pid.replaceChildren(a)}"
                + "if(d.downloadUrl){const a=document.createElement('a');a.href=d.downloadUrl;a.textContent='Stáhnout soubor';download.replaceChildren(a)}"
                + "if(!d.terminal)setTimeout(poll,5000)})"
                + ".catch(()=>{label.textContent='Stav se nepodařilo načíst. Zkusíme to znovu.';icon.textContent='⚠️';setTimeout(poll,10000)})}poll()})();</script>"
                + "</body></html>";
        return noCache(Response.ok(html, MediaType.TEXT_HTML_TYPE)).build();
    }

    /** Small status response used by the HTML page; never returns Process Manager's raw process JSON. */
    @GET
    @Path("status/{token}/data")
    @Produces(MediaType.APPLICATION_JSON)
    public Response processStatusData(@PathParam("token") String token) {
        if (!isValidProcessToken(token)) {
            throw new NotFoundException();
        }
        JSONObject process = new ProcessManagerClient(apacheClient).getProcess(token);
        if (process == null) {
            throw new NotFoundException();
        }
        String status = process.optString(ProcessManagerMapper.PCP_STATUS, "UNKNOWN").toUpperCase(java.util.Locale.ROOT);
        String label;
        boolean terminal;
        switch (status) {
            case "PLANNED": case "QUEUED": case "PENDING":
                label = "Požadavek čeká ve frontě."; terminal = false; break;
            case "RUNNING": case "STARTED":
                label = "Export právě probíhá."; terminal = false; break;
            case "SUCCESS": case "SUCCESSED": case "SUCCEEDED": case "FINISHED": case "COMPLETED":
                label = "Export byl dokončen."; terminal = true; break;
            case "FAILED": case "ERROR":
                label = "Export se nepodařilo dokončit."; terminal = true; break;
            case "CANCELLED": case "CANCELED":
                label = "Požadavek byl zrušen."; terminal = true; break;
            default:
                label = "Stav požadavku: " + status; terminal = false;
        }
        JSONObject result = new JSONObject().put("status", status).put("label", label).put("terminal", terminal);
        result.put("processTitle", process.optString(ProcessManagerMapper.PCP_DESCRIPTION, ""));
        if (process.has("finished")) result.put("updated", process.optString("finished"));
        else if (process.has("started")) result.put("updated", process.optString("started"));
        String pid = process.optJSONObject(ProcessManagerMapper.PCP_PAYLOAD) != null
                ? process.getJSONObject(ProcessManagerMapper.PCP_PAYLOAD).optString("pid", null) : null;
        if (pid != null) result.put("pid", pid);
        String documentUrl = buildDocumentUrl(pid);
        if (documentUrl != null) result.put("documentUrl", documentUrl);
        return noCache(Response.ok(result.toString(), MediaType.APPLICATION_JSON_TYPE)).build();
    }

    private static String buildDocumentUrl(String pid) {
        if (pid == null || !pid.matches("[A-Za-z0-9:_-]{1,256}")) return null;
        String apiUrl = cz.incad.kramerius.utils.conf.KConfiguration.getInstance()
                .getConfiguration().getString("api.client.point");
        if (apiUrl == null || apiUrl.isEmpty()) return null;
        String apiSuffix = "/api/client/v7.0";
        if (apiUrl.endsWith(apiSuffix)) apiUrl = apiUrl.substring(0, apiUrl.length() - apiSuffix.length());
        while (apiUrl.endsWith("/")) apiUrl = apiUrl.substring(0, apiUrl.length() - 1);
        return apiUrl + "/uuid/" + pid;
    }

    private static boolean isValidProcessToken(String token) {
        // Process Platform process IDs are UUIDs. Validate before using the
        // bearer token as a path component or polling target.
        return token != null && token.matches("(?i)[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}");
    }

    private static Response.ResponseBuilder noCache(Response.ResponseBuilder response) {
        CacheControl cacheControl = new CacheControl();
        cacheControl.setNoCache(true);
        cacheControl.setNoStore(true);
        return response.cacheControl(cacheControl).header("X-Content-Type-Options", "nosniff");
    }

    private static StreamingOutput streamingOutput(final InputStream is) {
        return new StreamingOutput() {
            public void write(OutputStream output) throws IOException, WebApplicationException {
                try {
                    IOUtils.copy(is, output);
                } catch (Exception e) {
                    throw new WebApplicationException(e);
                }
            }
        };
    }

    @GET
    @Path("userspace")
    @Produces(MediaType.APPLICATION_JSON)
    public Response userspace() {
        User user = this.userProvider.get();
        try {
            List<UserContentBundle> bundles = this.userContentSpace.listBundles(user.getLoginname());
            JSONArray jsonArray = new JSONArray();
            for (UserContentBundle bundle : bundles) {
                JSONObject item = new JSONObject();
                item.put("token", bundle.getToken());
                item.put("pid", bundle.getPid());
                item.put("type", bundle.getType().name().toLowerCase());
                item.put("created", bundle.getCreated().toString());
                item.put("size", bundle.getSize());
                item.put("available", bundle.isAvailable());
                jsonArray.put(item);
            }
            return Response.ok(jsonArray.toString()).type(MediaType.APPLICATION_JSON).build();
        } catch (Exception e) {
            throw new WebApplicationException(e);
        }
    }

    @GET
    @Path("{source}/userspace")
    @Produces(MediaType.APPLICATION_JSON)
    public Response userspace(@PathParam("source") String source) {
        throw new UnsupportedOperationException("Supported for CDK only");
    }

    @GET
    @Path("userspace/{spacetoken}/{docType}")
    public Response userspace(@PathParam("spacetoken") String token, @PathParam("docType") String docTypeStr) {
        LOGGER.fine("Requesting user space with token: " + token + " and docType: " + docTypeStr);
        DocumentType docType;
        try {
            docType = DocumentType.valueOf(docTypeStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new cz.incad.kramerius.rest.apiNew.exceptions.BadRequestException("Invalid document type: " + docTypeStr);
        }
        User user = this.userProvider.get();

        if (!this.userContentSpace.exists(token, docType)) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new JSONObject().put("error", "User content space for token=" + token + " not found").toString())
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }
        try {
            Optional<InputStream> bundle = this.userContentSpace.getBundle(token, user.getLoginname(), docType);
            if (!bundle.isPresent()) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(new JSONObject().put("error", "Bundle for token=" + token + ", user=" + user.getLoginname() + " and docType=" + docType + " not found").toString())
                        .type(MediaType.APPLICATION_JSON)
                        .build();
            }

            InputStream is = bundle.get();
            StreamingOutput stream = output -> {
                try (is) {
                    is.transferTo(output);
                } catch (Exception e) {
                    throw new WebApplicationException(e);
                }
            };
            return Response.ok(stream)
                    .header("Content-Disposition", "attachment; filename=\"" +
                            switch (docType) {
                                case PDF -> "export_" + token + ".pdf";
                                case TEXT -> "export_" + token + ".txt";
                                case EPUB -> "export_" + token + ".epub";
                            } + "\"")
                    .type(switch (docType) {
                        case PDF -> "application/pdf";
                        case TEXT -> "text/plain";
                        case EPUB -> "application/epub+zip";
                    })
                    .build();
        } catch (ClientErrorException e) {
            throw e;
        } catch (Exception e) {
            throw new WebApplicationException(e);
        }
    }
}
