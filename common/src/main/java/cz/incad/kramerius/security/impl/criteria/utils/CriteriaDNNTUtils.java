package cz.incad.kramerius.security.impl.criteria.utils;

import cz.incad.kramerius.ObjectPidsPath;
import cz.incad.kramerius.SolrAccess;
import cz.incad.kramerius.security.EvaluatingResultState;
import cz.incad.kramerius.security.RightCriteriumContext;
import cz.incad.kramerius.security.RightsReturnObject;
import cz.incad.kramerius.security.impl.criteria.ReadDNNTFlag;
import cz.incad.kramerius.security.impl.criteria.ReadDNNTFlagIPFiltered;
import cz.incad.kramerius.utils.solr.SolrUtils;
import org.w3c.dom.Document;

import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CriteriaDNNTUtils {


    public static ThreadLocal<RightsReturnObject> currentThreadReturnObject = new ThreadLocal<>();


    // DNNT logger
    public static Logger DNNT_LOGGER = Logger.getLogger("dnnt.access");

    public  static Logger LOGGER = Logger.getLogger(CriteriaDNNTUtils.class.getName());


    public static void logDnntAccess(String pid,
                                     String stream,
                                     String remoteAddr,
                                     String username,
                                     String email,
                                     ObjectPidsPath[] paths) throws IOException {

        StringBuilder builder = new StringBuilder();
        builder.append(pid).append(',');
        builder.append(remoteAddr).append(',');
        builder.append(username).append(',');
        builder.append(email).append(',');

        LocalDateTime date = LocalDateTime.now();
        String timestamp = date.format(DateTimeFormatter.ISO_DATE_TIME);
        builder.append(timestamp).append(',');

        for (int i = 0; i < paths.length; i++) {
            if (i > 0 ) builder.append(',');
            builder.append(paths[i].toString());
        }
        DNNT_LOGGER.log(Level.INFO, builder.toString());
    }

    public static void logDnntAccess(RightCriteriumContext ctx) throws IOException {
        logDnntAccess(ctx.getRequestedPid(),
                ctx.getRemoteAddr(),
                ctx.getRequestedStream(),
                ctx.getUser().getLoginname(),
                ctx.getUser().getEmail(),
                ctx.getSolrAccess().getPath(ctx.getRequestedPid()));
    }

    public static EvaluatingResultState checkDnnt(RightCriteriumContext ctx) {
        try {
            SolrAccess solrAccess = ctx.getSolrAccess();
            String pid = ctx.getRequestedPid();
            String xpath = "//bool[@name='dnnt']/text()";
            Document doc = solrAccess.getSolrDataDocument(pid);
            String val = SolrUtils.strValue(doc, xpath);
            return (val !=  null && val.equals("true")) ? EvaluatingResultState.TRUE : EvaluatingResultState.NOT_APPLICABLE;
        } catch (XPathExpressionException e) {
            LOGGER.log(Level.SEVERE,e.getMessage(),e);
            return EvaluatingResultState.NOT_APPLICABLE;
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE,e.getMessage(),e);
            return EvaluatingResultState.NOT_APPLICABLE;
        }
    }

    public static boolean checkContainsCriterium(RightsReturnObject obj) {
        if (obj.getRight().getCriteriumWrapper() != null) {
            if (obj.getRight().getCriteriumWrapper().getRightCriterium().getQName().equals(ReadDNNTFlag.class.getName()) ||
                    obj.getRight().getCriteriumWrapper().getRightCriterium().getQName().equals(ReadDNNTFlagIPFiltered.class.getName())) {
                return true;
            }
        }
        return false;
    }



}
