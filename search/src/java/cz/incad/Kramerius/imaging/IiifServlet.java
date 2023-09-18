package cz.incad.Kramerius.imaging;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;
import com.sun.jersey.api.client.Client;

import cz.incad.Kramerius.AbstractImageServlet;
import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.ObjectPidsPath;
import cz.incad.kramerius.SolrAccess;
import cz.incad.kramerius.audio.AudioStreamForwardingHelper;
import cz.incad.kramerius.fedora.utils.CDKUtils;
import cz.incad.kramerius.rest.api.k5.client.item.utils.IIIFUtils;
import cz.incad.kramerius.rest.apiNew.client.v60.ZoomifyHelper;
import cz.incad.kramerius.rest.apiNew.client.v60.libs.Instances;
import cz.incad.kramerius.rest.apiNew.client.v60.libs.OneInstance;
import cz.incad.kramerius.rest.apiNew.client.v60.redirection.item.ProxyItemHandler;
import cz.incad.kramerius.security.RightsResolver;
import cz.incad.kramerius.security.SecuredActions;
import cz.incad.kramerius.security.User;
import cz.incad.kramerius.statistics.StatisticsAccessLog;
import cz.incad.kramerius.statistics.accesslogs.AggregatedAccessLogs;
import cz.incad.kramerius.utils.FedoraUtils;
import cz.incad.kramerius.utils.IPAddressUtils;
import cz.incad.kramerius.utils.RESTHelper;
import cz.incad.kramerius.utils.conf.KConfiguration;
import cz.incad.kramerius.utils.imgs.KrameriusImageSupport;
import cz.incad.kramerius.utils.pid.LexerException;

import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.json.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Response;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.StringTokenizer;
import java.util.logging.Level;

/**
 * Created by rumanekm on 5.8.15.
 */
public class IiifServlet extends AbstractImageServlet {

    @Inject
    Provider<HttpServletRequest> requestProvider;

    /**
     * Because of rights and licenses
     */
    @Inject
    @Named("new-index")
    private SolrAccess solrAccess;


    @Inject
    private Provider<User> userProvider;

    @Inject
    @Named("cachedFedoraAccess")
    private transient FedoraAccess fedoraAccess;


    @Inject
    ZoomifyHelper zoomifyHelper;

    @Inject
    AudioStreamForwardingHelper audioHelper;

    @Inject
    @Named("forward-client")
    Provider<Client> clientProvider;

    

    @Inject
    RightsResolver rightsResolver;

    @Inject
    Instances instances;
    

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
            String redirectUrl = "/search/api/v7.0/client/items/iiif"+(!pathInfo.endsWith("/") ? "/":"") +pathInfo;
            resp.sendRedirect(redirectUrl);
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
    


}
