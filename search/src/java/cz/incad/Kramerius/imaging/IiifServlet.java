package cz.incad.Kramerius.imaging;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;
import cz.incad.Kramerius.AbstractImageServlet;
import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.ObjectPidsPath;
import cz.incad.kramerius.SolrAccess;
import cz.incad.kramerius.rest.utils.IIIFUtils;
import cz.incad.kramerius.security.RightsResolver;
import cz.incad.kramerius.security.SecuredActions;
import cz.incad.kramerius.security.User;
import cz.incad.kramerius.statistics.StatisticsAccessLog;
import cz.incad.kramerius.statistics.accesslogs.AggregatedAccessLogs;
import cz.incad.kramerius.utils.FedoraUtils;
import cz.incad.kramerius.utils.RESTHelper;
import cz.incad.kramerius.utils.imgs.KrameriusImageSupport;
import org.apache.commons.io.IOUtils;
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
    @Named("cachedFedoraAccess")
    private transient FedoraAccess fedoraAccess;


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
            String pathInfo = req.getPathInfo();
            if (pathInfo.indexOf("uuid:")>0) {
                String startOfPid  = pathInfo.substring(pathInfo.indexOf("uuid:"));
                String pid = startOfPid.substring(0, startOfPid.indexOf("/"));
                String end = pathInfo.substring(pathInfo.indexOf(pid)+pid.length()+1);
                String redirectUrl = String.format("/search/api/client/v7.0/items/%s/image/iiif/%s", pid, end);
                resp.sendRedirect(redirectUrl);
            } else {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
            }
        } catch (IOException e) {
            LOGGER.severe(e.getMessage());
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
