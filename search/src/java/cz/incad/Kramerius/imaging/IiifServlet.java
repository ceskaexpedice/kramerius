package cz.incad.Kramerius.imaging;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;
import cz.incad.Kramerius.AbstractImageServlet;
import cz.incad.Kramerius.imaging.utils.CDKIIIFServletUtils;
import cz.incad.kramerius.ObjectPidsPath;
import cz.incad.kramerius.SolrAccess;
import cz.incad.kramerius.cdk.CDKUtils;
import cz.incad.kramerius.iiif.IIIFUtils;
import cz.incad.kramerius.rest.apiNew.client.v70.libs.Instances;
import cz.incad.kramerius.rest.apiNew.client.v70.libs.OneInstance;
import cz.incad.kramerius.rest.apiNew.client.v70.redirection.DeleteTriggerSupport;
import cz.incad.kramerius.rest.apiNew.client.v70.redirection.item.ProxyItemHandler;
import cz.incad.kramerius.security.RightsResolver;
import cz.incad.kramerius.security.SecuredActions;
import cz.incad.kramerius.security.User;
import cz.incad.kramerius.statistics.accesslogs.AggregatedAccessLogs;
import cz.incad.kramerius.utils.FedoraUtils;
import cz.incad.kramerius.utils.IPAddressUtils;
import cz.incad.kramerius.utils.RESTHelper;
import cz.incad.kramerius.utils.conf.KConfiguration;
import cz.incad.kramerius.utils.imgs.KrameriusImageSupport;
import cz.incad.kramerius.utils.pid.LexerException;
import org.apache.commons.io.IOUtils;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.ceskaexpedice.akubra.AkubraRepository;
import org.json.JSONException;
import org.json.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Level;

/**
 * Created by rumanekm on 5.8.15.
 */
public class IiifServlet extends AbstractImageServlet {

    @Inject
    @Named("cachedSolrAccess")
    private SolrAccess solrAccess;

    @Inject
    private RightsResolver rightsResolver;

    @Inject
    private Provider<User> userProvider;

    @Inject
    private AkubraRepository akubraRepository;

    @Inject
    DeleteTriggerSupport deleteTriggerSupport;


    @Inject
    AggregatedAccessLogs aggregatedAccessLogs;

    // CDK injection
    @Inject
    Instances instances;

    @Inject
    @javax.inject.Named("forward-client")
    Provider<CloseableHttpClient> apacheClient;


    private static final java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(IiifServlet.class.getName());


    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            boolean cdkServerMode = KConfiguration.getInstance().getConfiguration().getBoolean("cdk.server.mode");
            if (cdkServerMode) {
                String requestURL = req.getRequestURL().toString();
                String zoomUrl = DeepZoomServlet.disectZoom(requestURL);
                StringTokenizer tokenizer = new StringTokenizer(zoomUrl, "/");

                if (!tokenizer.hasMoreTokens()) {
                    resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
                    return;
                }

                String firstToken = URLDecoder.decode(tokenizer.nextToken(), "UTF-8");
                String pid;
                String acronym = null;

                if (!firstToken.contains(":")) {
                    acronym = firstToken;
                    if (tokenizer.hasMoreTokens()) {
                        pid = URLDecoder.decode(tokenizer.nextToken(), "UTF-8");
                    } else {
                        resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing PID after acronym: " + acronym);
                        return;
                    }
                } else {
                    pid = firstToken;
                }

                if (acronym == null) {
                    acronym = defaultDocumentSource(pid);
                }

                String forwardUrl = forwardUrl(acronym);
                if (forwardUrl == null) {
                    resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Source not found for PID: " + pid + (acronym != null ? " (acronym: " + acronym + ")" : ""));
                    return;
                }


                StringBuilder iiifUrl = new StringBuilder(forwardUrl + (forwardUrl.endsWith("/") ? "" : "/") + "api/cdk/v7.0/forward/iiif/" + pid);
                boolean isInfoRequest = false;
                while (tokenizer.hasMoreTokens()) {
                    String token = tokenizer.nextToken();
                    iiifUrl.append("/").append(token);
                    if ("info.json".equals(token)) isInfoRequest = true;
                }

                if (isInfoRequest) {
                    proxyInfoJson(acronym, iiifUrl.toString(), req, resp);
                } else {
                    proxyTile(acronym, iiifUrl.toString(), resp);
                }
            } else {
                String requestURL = req.getRequestURL().toString();
                String zoomUrl = DeepZoomServlet.disectZoom(requestURL);
                StringTokenizer tokenizer = new StringTokenizer(zoomUrl, "/");
                String pid = tokenizer.nextToken();

                pid = URLDecoder.decode(pid, "UTF-8");

                ObjectPidsPath[] paths = solrAccess.getPidPaths(pid);
                boolean permited = false;
                for (ObjectPidsPath pth : paths) {
                    permited = this.rightsResolver.isActionAllowed(userProvider.get(), SecuredActions.A_READ.getFormalName(), pid, null, pth.injectRepository()).flag();
                    if (permited) break;
                }
                if (permited) {
                    try {
                        String u = IIIFUtils.iiifImageEndpoint(pid, akubraRepository);
                        if (u != null) {
                            StringBuilder url = new StringBuilder(u);
                            while (tokenizer.hasMoreTokens()) {
                                String nextToken = tokenizer.nextToken();
                                url.append("/").append(nextToken);
                                if ("info.json".equals(nextToken)) {
                                    reportAccess(pid);
                                    resp.setContentType("application/ld+json");
                                    resp.setCharacterEncoding("UTF-8");
                                    HttpURLConnection con = (HttpURLConnection) RESTHelper.openConnection(url.toString(), "", "");
                                    InputStream inputStream = con.getInputStream();
                                    String json = IOUtils.toString(inputStream, Charset.defaultCharset());
                                    JSONObject object = new JSONObject(json);
                                    String urlRequest = req.getRequestURL().toString();
                                    object.put("@id", urlRequest.substring(0, urlRequest.lastIndexOf('/')));
                                    PrintWriter out = resp.getWriter();
                                    out.print(object.toString());
                                    out.flush();
                                    return;
                                }
                            }
                            copyFromImageServer(url.toString(),resp);
                        } else {
                            LOGGER.severe(String.format("No iip url for %s", pid));
                            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                        }
                    } catch (JSONException e) {
                        LOGGER.log(Level.SEVERE, e.getMessage(),e);
                        resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    }
                } else {
                    resp.sendError(HttpServletResponse.SC_FORBIDDEN);
                }
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(),e);
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, e.getMessage(),e);
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public KrameriusImageSupport.ScalingMethod getScalingMethod() {
        return null;
    }

    @Override
    public boolean turnOnIterateScaling() {
        return false;
    }

    private void reportAccess(String pid) {
        try {
            this.aggregatedAccessLogs.reportAccess(pid, FedoraUtils.IMG_FULL_STREAM);
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Can't write statistic records for " + pid, e);
        }
    }

    private static String afterServlet(String pathInfo) {
        String afterServlet = pathInfo.substring(pathInfo.indexOf("search/iiif/") + "search/iiif/".length());
        return afterServlet;
    }


    protected String forwardUrl(String source) {
        String baseurl = KConfiguration.getInstance().getConfiguration()
                .getString("cdk.collections.sources." + source + ".forwardurl");
        return baseurl;
    }


    private void proxyInfoJson(String source, String url, HttpServletRequest req, HttpServletResponse resp) throws IOException {
        org.apache.hc.client5.http.classic.methods.HttpGet get = new org.apache.hc.client5.http.classic.methods.HttpGet(url);

        CDKIIIFServletUtils.httpRequestAPIKey(source, get);

        try (org.apache.hc.client5.http.impl.classic.CloseableHttpResponse response = (org.apache.hc.client5.http.impl.classic.CloseableHttpResponse) apacheClient.get().execute(get)) {
            int code = response.getCode();
            if (code == 200) {
                String json = org.apache.commons.io.IOUtils.toString(response.getEntity().getContent(), java.nio.charset.StandardCharsets.UTF_8);
                JSONObject object = new JSONObject(json);

                String urlRequest = req.getRequestURL().toString();
                if (urlRequest.endsWith("/info.json")) {
                    object.put("@id", urlRequest.substring(0, urlRequest.lastIndexOf("/info.json")));
                } else {
                    object.put("@id", urlRequest);
                }

                resp.setContentType("application/ld+json");
                resp.setCharacterEncoding("UTF-8");
                PrintWriter out = resp.getWriter();
                out.print(object.toString());
                out.flush();
            } else {
                LOGGER.log(Level.WARNING, "Remote info.json failed with code: {0} for URL: {1}", new Object[]{code, url});
                resp.sendError(code);
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error during proxying info.json from: " + url, e);
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }


    private void proxyTile(String source, String url, HttpServletResponse resp) throws IOException {
        org.apache.hc.client5.http.classic.methods.HttpGet get = new org.apache.hc.client5.http.classic.methods.HttpGet(url);

        CDKIIIFServletUtils.httpRequestAPIKey(source, get);

        try (org.apache.hc.client5.http.impl.classic.CloseableHttpResponse response = (org.apache.hc.client5.http.impl.classic.CloseableHttpResponse) apacheClient.get().execute(get)) {
            int code = response.getCode();
            resp.setStatus(code);
            if (code == 200) {
                org.apache.hc.core5.http.HttpEntity entity = response.getEntity();

                if (entity.getContentType() != null) {
                    resp.setContentType(entity.getContentType());
                }

                if (entity.getContentLength() >= 0) {
                    resp.setHeader("Content-Length", String.valueOf(entity.getContentLength()));
                }
                resp.setHeader("Access-Control-Allow-Origin", "*");
                CDKIIIFServletUtils.copyCacheHeaders(response, resp);
                try (InputStream is = entity.getContent()) {
                    org.apache.commons.io.IOUtils.copy(is, resp.getOutputStream());
                }
                resp.getOutputStream().flush();
            } else {
                LOGGER.log(Level.WARNING, "Remote tile failed with code: {0} for URL: {1}", new Object[]{code, url});
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error during proxying tile from: " + url, e);
            if (!resp.isCommitted()) {
                resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
        }
    }

    private String defaultDocumentSource(String pid) throws IOException {
        org.w3c.dom.Document solrDataByPid = solrAccess.getSolrDataByPid(pid);
        String leader = CDKUtils.findCDKLeader(solrDataByPid.getDocumentElement());
        List<String> sources = CDKUtils.findSources(solrDataByPid.getDocumentElement());
        return leader != null ? leader : (!sources.isEmpty() ? sources.get(0) : null);
    }


}
