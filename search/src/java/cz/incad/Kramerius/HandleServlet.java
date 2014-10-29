package cz.incad.Kramerius;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.google.inject.Inject;

import cz.incad.Kramerius.backend.guice.GuiceServlet;
import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.ObjectModelsPath;
import cz.incad.kramerius.ObjectPidsPath;
import cz.incad.kramerius.SolrAccess;
import cz.incad.kramerius.intconfig.InternalConfiguration;
import cz.incad.kramerius.utils.ApplicationURL;
import cz.incad.kramerius.utils.RESTHelper;
import cz.incad.kramerius.utils.XMLUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;
import cz.incad.kramerius.utils.handle.DisectHandle;
import cz.incad.kramerius.utils.pid.LexerException;
import cz.incad.kramerius.utils.pid.PIDParser;
import cz.incad.kramerius.utils.solr.SolrUtils;

/**
 * This is support for persistent URL
 * @author pavels
 */
public class HandleServlet extends GuiceServlet {

    private static final long serialVersionUID = 1L;
    public static final java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(HandleServlet.class.getName());
    @Inject
    transient KConfiguration kConfiguration;
    @Inject
    SolrAccess solrAccess;

    @Override
    public void init() throws ServletException {
        super.init();
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {

            String requestURL = req.getRequestURL().toString();
            String handle = DisectHandle.disectHandle(requestURL);
            handle = URLDecoder.decode(handle,"UTF-8");
            
            HandleType handleType = HandleType.createType(handle);

            handleType.redirect(handle, solrAccess, req, resp);
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            resp.sendError(500);
        }
    }

    enum HandleType {

        UUID {

            @Override
            Document dataFromSolr(String pid, SolrAccess solrAccess) throws IOException {
                try {
                    PIDParser parser = new PIDParser(pid);
                    parser.objectPid();
                    return solrAccess.getSolrDataDocument(parser.getObjectId());
                } catch (LexerException e) {
                    LOGGER.log(Level.SEVERE, e.getMessage(), e);
                    return solrAccess.getSolrDataDocument(pid);
                }
            }

            @Override
            void redirect(String handle, SolrAccess solrAccess, HttpServletRequest request, HttpServletResponse response) throws IOException {
                Map parameterMap = request.getParameterMap();
                String applicationCotext = ApplicationURL.applicationContextPath(request);
                String redirectUrl = "/" + applicationCotext + "/i.jsp?pid=" + handle;
                response.sendRedirect(redirectUrl);
            }
        },
        KRAMERIUS3 {

            @Override
            Document dataFromSolr(String handle, SolrAccess solrAccess) throws IOException {
                return solrAccess.getSolrDataDocumentByHandle(handle);
            }

            @Override
            void redirect(String handle, SolrAccess solrAccess, HttpServletRequest request, HttpServletResponse response) throws IOException {
                try {
                    Document parsedDocument = dataFromSolr(handle, solrAccess);
                    String pid = SolrUtils.disectPid(parsedDocument);
                    String applicationCotext = ApplicationURL.applicationContextPath(request);
                    String redirectUrl = "/" + applicationCotext + "/i.jsp?pid=" + pid;
                    response.sendRedirect(redirectUrl);
                } catch (XPathExpressionException e) {
                    throw new IOException(e);
                }
            }
            
        };

        //abstract String construct(String handle);
        abstract Document dataFromSolr(String handle, SolrAccess solrAccess) throws IOException;
        
        abstract void redirect(String handle, SolrAccess solrAccess, HttpServletRequest request, HttpServletResponse response) throws IOException;
        
        public static HandleType createType(String handle) {
            if (handle.toLowerCase().startsWith("uuid:")) {
                return UUID;
            } else {
                return KRAMERIUS3;
            }
        }
    }
}
