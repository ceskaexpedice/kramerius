package cz.incad.Kramerius.imaging;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;
import cz.incad.Kramerius.AbstractImageServlet;
import cz.incad.kramerius.ObjectPidsPath;
import cz.incad.kramerius.SolrAccess;
import cz.incad.kramerius.iiif.IIIFUtils;
import cz.incad.kramerius.security.RightsResolver;
import cz.incad.kramerius.security.SecuredActions;
import cz.incad.kramerius.security.User;
import cz.incad.kramerius.statistics.accesslogs.AggregatedAccessLogs;
import cz.incad.kramerius.utils.FedoraUtils;
import cz.incad.kramerius.utils.RESTHelper;
import cz.incad.kramerius.utils.imgs.KrameriusImageSupport;
import org.apache.commons.io.IOUtils;
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


//    @Inject
//    @Named("database")
//    private StatisticsAccessLog databaseAccessLog;
//
//    @Inject
//    @Named("dnnt")
//    StatisticsAccessLog dnntAccessLog;

    @Inject
    AggregatedAccessLogs aggregatedAccessLogs;

    private static final java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(IiifServlet.class.getName());


    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            String requestURL = req.getRequestURL().toString();
            String zoomUrl = DeepZoomServlet.disectZoom(requestURL);
            StringTokenizer tokenizer = new StringTokenizer(zoomUrl, "/");
            String pid = tokenizer.nextToken();

            //unescape PID
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

}
