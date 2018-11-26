package cz.incad.kramerius.security.impl.criteria.utils;

import cz.incad.kramerius.ObjectPidsPath;
import cz.incad.kramerius.SolrAccess;
import cz.incad.kramerius.security.EvaluatingResultState;
import cz.incad.kramerius.security.RightCriteriumContext;
import cz.incad.kramerius.utils.solr.SolrUtils;
import org.w3c.dom.Document;

import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CriteriaDNNTUtils {

    // DNNT logger
    public static Logger DNNT_LOGGER = Logger.getLogger("dnnt.access");

    public  static Logger LOGGER = Logger.getLogger(CriteriaDNNTUtils.class.getName());


    public static void logDnntAccess(RightCriteriumContext ctx) throws IOException {
        StringBuilder builder = new StringBuilder();
        String pid= ctx.getRequestedPid();
        builder.append(pid).append(',');

        String remoteAddr = ctx.getRemoteAddr();
        builder.append(remoteAddr).append(',');

        String userLoginName = ctx.getUser().getLoginname();
        builder.append(userLoginName).append(',');
        String userEmail = ctx.getUser().getEmail();
        builder.append(userEmail).append(',');

        LocalDateTime date = LocalDateTime.now();
        String timestamp = date.format(DateTimeFormatter.ISO_DATE_TIME);
        builder.append(timestamp).append(',');

        ObjectPidsPath[] paths = ctx.getSolrAccess().getPath(pid);
        for (int i = 0; i < paths.length; i++) {
            if (i > 0 ) builder.append(',');
            builder.append(paths[i].toString());
        }

        DNNT_LOGGER.log(Level.INFO, builder.toString());
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

    public static void main(String[] args) {
        LocalDateTime date = LocalDateTime.now();
        String format = date.format(DateTimeFormatter.ISO_DATE_TIME);
        System.out.println(format);
    }


}
