package cz.incad.Kramerius.imaging;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;
import cz.incad.Kramerius.AbstractImageServlet;
import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.ObjectPidsPath;
import cz.incad.kramerius.SolrAccess;
import cz.incad.kramerius.rest.api.k5.client.item.utils.IIIFUtils;
import cz.incad.kramerius.security.IsActionAllowed;
import cz.incad.kramerius.security.SecuredActions;
import cz.incad.kramerius.security.User;
import cz.incad.kramerius.utils.RESTHelper;
import cz.incad.kramerius.utils.imgs.KrameriusImageSupport;
import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.json.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.xpath.XPathExpressionException;
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
    private IsActionAllowed actionAllowed;

    @Inject
    private Provider<User> userProvider;

    @Inject
    @Named("cachedFedoraAccess")
    private transient FedoraAccess fedoraAccess;

    private static final java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(IiifServlet.class.getName());


    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException {
        try {
            String requestURL = req.getRequestURL().toString();
            String zoomUrl = DeepZoomServlet.disectZoom(requestURL);
            StringTokenizer tokenizer = new StringTokenizer(zoomUrl, "/");
            String pid = tokenizer.nextToken();

            //unescape PID
            pid = URLDecoder.decode(pid, "UTF-8");

            if (!pid.matches("uuid:[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}")) {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }

            ObjectPidsPath[] paths = solrAccess.getPath(pid);
            boolean permited = false;
            for (ObjectPidsPath pth : paths) {
                permited = this.actionAllowed.isActionAllowed(userProvider.get(), SecuredActions.READ.getFormalName(), pid, null, pth);
                if (permited) break;
            }

            if (permited) {
                try {
                    String u = IIIFUtils.iiifImageEndpoint(pid, this.fedoraAccess);
                    StringBuilder url = new StringBuilder(u);
                    while (tokenizer.hasMoreTokens()) {
                        String nextToken = tokenizer.nextToken();
                        url.append("/").append(nextToken);
                        if ("info.json".equals(nextToken)) {
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
                } catch (JSONException e) {
                    LOGGER.log(Level.SEVERE, e.getMessage());
                }
            } else {
                resp.sendError(HttpServletResponse.SC_FORBIDDEN);
            }
        } catch (IOException e) {
            LOGGER.severe(e.getMessage());
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
}
