package cz.incad.kramerius.security.impl.criteria.utils;

import cz.incad.kramerius.ObjectModelsPath;
import cz.incad.kramerius.ObjectPidsPath;
import cz.incad.kramerius.SolrAccess;
import cz.incad.kramerius.security.*;
import cz.incad.kramerius.security.impl.criteria.CriteriaPrecoditionException;
import cz.incad.kramerius.security.impl.criteria.PDFDNNTFlag;
import cz.incad.kramerius.security.impl.criteria.ReadDNNTFlag;
import cz.incad.kramerius.security.impl.criteria.ReadDNNTFlagIPFiltered;
import cz.incad.kramerius.statistics.impl.DatabaseStatisticsAccessLogImpl;
import cz.incad.kramerius.utils.solr.SolrUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Document;

import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class CriteriaDNNTUtils {


    public static ThreadLocal<RightsReturnObject> currentThreadReturnObject = new ThreadLocal<>();


    public  static Logger LOGGER = Logger.getLogger(CriteriaDNNTUtils.class.getName());


    //    public static void toJSON(RightCriteriumContext ctx) throws IOException {
//        toJSON(ctx.getRequestedPid(),
//                ctx.getRemoteAddr(),
//                ctx.getRequestedStream(),
//                ctx.getUser().getLoginname(),
//                ctx.getUser().getEmail(),
//                ctx.getSolrAccess().getPath(ctx.getRequestedPid()),
//                ctx.getSolrAccess().getPathOfModels(ctx.getRequestedPid())
//                );
//    }

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

    public static boolean checkContainsCriteriumReadDNNT(RightsReturnObject obj) {
        if (obj.getRight() != null && obj.getRight().getCriteriumWrapper() != null && obj.getRight().getCriteriumWrapper() != null) {
            if (obj.getRight().getCriteriumWrapper().getRightCriterium().getQName().equals(ReadDNNTFlag.class.getName()) ||
                    obj.getRight().getCriteriumWrapper().getRightCriterium().getQName().equals(ReadDNNTFlagIPFiltered.class.getName())) {
                return true;
            }
        }
        return false;
    }


    public static void checkContainsCriteriumPDFDNNT(RightCriteriumContext ctx, RightsManager manager) throws CriteriaPrecoditionException {
        //PDFDNNTFlag.class.getName()
        checkContainsCriterium(ctx, manager, PDFDNNTFlag.class);
    }

    public static void checkContainsCriterium(RightCriteriumContext ctx, RightsManager manager, Class ... clzs) throws CriteriaPrecoditionException {
        String[] pids = new String[] {SpecialObjects.REPOSITORY.getPid()};
        Right[] rights = manager.findRights(pids, SecuredActions.PDF_RESOURCE.getFormalName(), ctx.getUser());
        for (Right r : rights) {
            if (r == null) continue;
            if (r.getCriteriumWrapper() == null) continue;
            RightCriterium rightCriterium = r.getCriteriumWrapper().getRightCriterium();
            String qName = rightCriterium.getQName();
            for (Class clz:clzs) {
                if (qName.equals(clz.getName())) {
                    return;
                }
            }
        }
        List<String> collections = Arrays.stream(clzs).map(s -> s.getName()).collect(Collectors.toList());
        throw new CriteriaPrecoditionException("These flags are not set : "+collections);
    }

    public static void checkContainsCriteriumReadDNNT(RightCriteriumContext ctx, RightsManager manager) throws CriteriaPrecoditionException {
        checkContainsCriterium(ctx, manager, PDFDNNTFlag.class);
    }

}
