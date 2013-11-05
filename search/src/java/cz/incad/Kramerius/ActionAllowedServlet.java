package cz.incad.Kramerius;

import static cz.incad.utils.IKeys.UUID_PARAMETER;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.antlr.stringtemplate.StringTemplate;
import org.antlr.stringtemplate.StringTemplateGroup;
import org.antlr.stringtemplate.language.DefaultTemplateLexer;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;

import cz.incad.Kramerius.backend.guice.GuiceServlet;
import cz.incad.kramerius.AbstractObjectPath;
import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.ObjectPidsPath;
import cz.incad.kramerius.SolrAccess;
import cz.incad.kramerius.imaging.DeepZoomCacheService;
import cz.incad.kramerius.imaging.ImageStreams;
import cz.incad.kramerius.security.IsActionAllowed;
import cz.incad.kramerius.security.RightCriteriumContextFactory;
import cz.incad.kramerius.security.RightsManager;
import cz.incad.kramerius.security.SecuredActions;
import cz.incad.kramerius.security.SecurityException;
import cz.incad.kramerius.security.SpecialObjects;
import cz.incad.kramerius.security.User;
import cz.incad.kramerius.security.impl.http.AbstractLoggedUserProvider;
import cz.incad.kramerius.utils.IOUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;
import cz.incad.kramerius.utils.imgs.ImageMimeType;
import cz.incad.kramerius.utils.solr.SolrUtils;

public class ActionAllowedServlet extends GuiceServlet {

    public static final java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(MimeTypeServlet.class.getName());
    @Inject
    @Named("rawFedoraAccess")
    FedoraAccess fedoraAccess;
    @Inject
    SolrAccess solrAccess;
    @Inject
    IsActionAllowed actionAllowed;
    @Inject
    RightCriteriumContextFactory ctxFactory;
    @Inject
    Provider<User> currentLoggedUserProvider;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            StringBuilder out = new StringBuilder();
            out.append("{");
            String action = req.getParameter("action");
            String[] pids = req.getParameterValues("pid");

            User user = currentLoggedUserProvider.get();
            HashMap map = new HashMap();
            int i = 0;
            for (String pid : pids) {
                i++;
                boolean b = isActionAllowed(user, action, pid);
                out.append("\"");
                out.append(pid);
                out.append("\":");
                out.append(b);
                if (i < pids.length) {
                    out.append(",");
                }
            }
            out.append("}");
            resp.setContentType("application/json");
            resp.getWriter().println(out.toString());
        } catch (SecurityException e) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
        }
    }

    private boolean isActionAllowed(User user, String action, String pid) throws IOException {
        ObjectPidsPath[] paths = this.solrAccess.getPath(pid);
        for (ObjectPidsPath p : paths) {
            boolean b = actionAllowed.isActionAllowed(user, action, pid, null, p);
            if (b) {
                return true;
            }
        }
        return false;
    }

    private StringTemplateGroup stGroup() throws IOException {
        InputStream stream = ActionAllowedServlet.class.getResourceAsStream("viewinfo.stg");
        String string = IOUtils.readAsString(stream, Charset.forName("UTF-8"), true);
        StringTemplateGroup group = new StringTemplateGroup(new StringReader(string), DefaultTemplateLexer.class);
        return group;
    }

    public static void main(String[] args) {
        StringTemplate template = new StringTemplate(
                "$data.keys:{action| $data.(action).keys:{ key| $key$ :  $data.(action).(key)$ };separator=\",\"$ }$");

        HashMap map = new HashMap();

        HashMap<String, String> data = new HashMap<String, String>();
        {
            data.put("drobnustky", "true");
            data.put("stranka", "true");
            data.put("repository", "true");
        };
        map.put("edit", data);

        template.setAttribute("data", map);
        System.out.println(template.toString());

    }
}
