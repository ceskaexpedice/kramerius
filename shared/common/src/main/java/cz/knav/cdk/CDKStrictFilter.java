package cz.knav.cdk;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletResponse;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import cz.incad.kramerius.FedoraNamespaceContext;
import cz.incad.kramerius.security.EvaluatingResult;
import cz.incad.kramerius.security.RightCriterium;
import cz.incad.kramerius.security.RightCriteriumContext;
import cz.incad.kramerius.security.RightCriteriumException;
import cz.incad.kramerius.security.RightCriteriumPriorityHint;
import cz.incad.kramerius.security.SecuredActions;
import cz.incad.kramerius.security.impl.criteria.AbstractCriterium;
import cz.incad.kramerius.utils.ApplicationURL;
import cz.incad.kramerius.utils.IOUtils;

public class CDKStrictFilter extends AbstractCriterium implements RightCriterium {

    public static final Logger LOGGER = Logger.getLogger(CDKStrictFilter.class.getName());

    public static String XPATH = 
        "//kramerius:replicatedFrom/text()";

    private XPathFactory xpfactory;

    public CDKStrictFilter() {
        super();
        this.xpfactory = XPathFactory.newInstance();    
    }

    @Override
    public EvaluatingResult evalute() throws RightCriteriumException {
        try {
            RightCriteriumContext ctx = getEvaluateContext();
            String pid = ctx.getRequestedPid();
            String vPid = ctx.getFedoraAccess().findFirstViewablePid(pid);
            Document docRelsExt = ctx.getFedoraAccess().getRelsExt(vPid);
            String testUrl = disectURL(this.xpfactory, docRelsExt);
            if (testUrl != null) {
                int code = connect(testUrl);
                if ((code == HttpServletResponse.SC_OK) || (code == HttpServletResponse.SC_NOT_MODIFIED) || (code == HttpServletResponse.SC_NOT_FOUND)) {
                    return EvaluatingResult.TRUE;
                } else return EvaluatingResult.FALSE;
            } else return EvaluatingResult.FALSE;
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, e.getMessage());
            return EvaluatingResult.FALSE;
        } catch (XPathExpressionException e) {
            LOGGER.log(Level.SEVERE, e.getMessage());
            return EvaluatingResult.FALSE;
        }
    }

    public static int connect(String turl) throws IOException {
        HttpURLConnection connection = null;
        int respCode;
        try {
            URL url = new URL(turl);
            connection = (HttpURLConnection) url.openConnection();
            connection.connect();
            respCode = connection.getResponseCode();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
        return respCode;
    }
    
    public static String disectURL(XPathFactory xpathFactory, Document docRelsExt) throws XPathExpressionException {
        XPath xpath = xpathFactory.newXPath();
        xpath.setNamespaceContext(new FedoraNamespaceContext());
        XPathExpression expr = xpath.compile(XPATH);
        NodeList nlist = (NodeList) expr.evaluate(docRelsExt, XPathConstants.NODESET);
        int length = nlist.getLength();
        if (length > 0) {
            int last = length - 1;
            String n = ((Text)nlist.item(last)).getTextContent();
            //TEXT_OCR becuse of data 
            ///http://localhost:8080/search/img?uuid=uuid:4eac74b0-e92c-11dc-9fa1-000d606f5dc6&stream=TEXT_OCR&action=GETRAW
            String replaced = n.replace("handle/", "img?uuid=")+"&stream=TEXT_OCR&action=GETRAW";
            return replaced;
        } else {
            LOGGER.log(Level.SEVERE,"no replicated sources");
            return null;
        }
    }

    @Override
    public RightCriteriumPriorityHint getPriorityHint() {
        return RightCriteriumPriorityHint.MAX;
    }

    @Override
    public boolean isParamsNecessary() {
        return true;
    }

    @Override
    public SecuredActions[] getApplicableActions() {
        return new SecuredActions[] { SecuredActions.READ };
    }
}
