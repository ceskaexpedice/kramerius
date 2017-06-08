package cz.incad.kramerius.client;

import static cz.incad.kramerius.client.utils.ApiCallsHelp.*;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.configuration.ConfigurationException;
import org.json.JSONException;
import org.json.JSONObject;

import cz.incad.kramerius.client.utils.RedirectHelp;
import cz.incad.kramerius.utils.StringUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;

public class PrintFunctionalityServlet extends HttpServlet {

    public static Logger LOGGER = Logger
            .getLogger(PrintFunctionalityServlet.class.getName());

    private static boolean encodedTypes(String mtype) {
        return mtype.contains("pdf") || mtype.contains("djvu")
                || mtype.contains("jp2");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        try {
            String params = req.getParameter("pids");
            String startPid = req.getParameter("startPid");
            String parentPid = req.getParameter("parentPid");
            String stopPid = req.getParameter("stopPid");

            if (StringUtils.isAnyString(params)) {
                String[] splitted = params.split(",");
                if (splitted.length > 0) {
                    boolean transcode = false;
                    for (String pid : splitted) {
                        try {
                            String api = KConfiguration.getInstance()
                                    .getConfiguration().getString("api.point");
                            if (!api.endsWith("/")) {
                                api += "/";
                            }

                            String jsoned = getJSON(api + "item/" + pid
                                    + "/streams");
                            JSONObject jObject = new JSONObject(jsoned);
                            JSONObject imgFull = jObject
                                    .getJSONObject("IMG_FULL");
                            String mType = imgFull.getString("mimeType");
                            if (encodedTypes(mType)) {
                                transcode = true;
                                break;
                            }
                        } catch (JSONException e) {
                            LOGGER.log(Level.SEVERE, e.getMessage(), e);
                        }
                    }

                    String redirecthost = RedirectHelp.redirectApplication(req);
                    String str = redirecthost + "search/inc/_iprint.jsp?pids="
                            + params + "&transcode=" + transcode
                            + "&page=A4&layout=portrait";
                    resp.sendRedirect(str);
                }
            } else if (StringUtils.isAnyString(startPid)) {
                String redirecthost = RedirectHelp.redirectApplication(req);
                String str = redirecthost + "search/inc/_iprint.jsp?startPid="
                        + startPid;
                if  (StringUtils.isAnyString(stopPid)) {
                	str = str + "&stopPid="+stopPid;
                }
                resp.sendRedirect(str);

            } else if (StringUtils.isAnyString(parentPid)) {
                String redirecthost = RedirectHelp.redirectApplication(req);
                String str = redirecthost + "search/inc/_iprint.jsp?parentPid="
                        + parentPid;
                resp.sendRedirect(str);
            }
        } catch (ConfigurationException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        }
    }
}
