package cz.incad.kramerius.security.impl.criteria.utils;

import cz.incad.kramerius.ObjectModelsPath;
import cz.incad.kramerius.ObjectPidsPath;
import cz.incad.kramerius.SolrAccess;
import cz.incad.kramerius.security.*;
import cz.incad.kramerius.security.impl.criteria.CriteriaPrecoditionException;
import cz.incad.kramerius.security.impl.criteria.PDFDNNTFlag;
import cz.incad.kramerius.security.impl.criteria.ReadDNNTFlag;
import cz.incad.kramerius.security.impl.criteria.ReadDNNTFlagIPFiltered;
import cz.incad.kramerius.utils.solr.SolrUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Document;

import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CriteriaDNNTUtils {


    public static ThreadLocal<RightsReturnObject> currentThreadReturnObject = new ThreadLocal<>();


    // DNNT logger
    public static Logger DNNT_LOGGER = Logger.getLogger("dnnt.access");

    public  static Logger LOGGER = Logger.getLogger(CriteriaDNNTUtils.class.getName());


    public static void logDnntAccess(String pid,
                                     String stream,
                                     String rootTitle,
                                     String dcTitle,
                                     String remoteAddr,
                                     String username,
                                     String email,
                                     ObjectPidsPath[] paths,
                                     ObjectModelsPath[] mpaths) throws IOException {

        LocalDateTime date = LocalDateTime.now();
        String timestamp = date.format(DateTimeFormatter.ISO_DATE_TIME);

        JSONObject jObject = new JSONObject();

        jObject.put("pid",pid);
        jObject.put("remoteAddr",remoteAddr);
        jObject.put("username",username);
        jObject.put("email",email);

        jObject.put("rootTitle",rootTitle);
        jObject.put("dcTitle",dcTitle);

        jObject.put("date",timestamp);


        JSONArray pidsArray = new JSONArray();
        for (int i = 0; i < paths.length; i++) {
            pidsArray.put(pathToString(paths[i].getPathFromRootToLeaf()));
        }
        jObject.put("pids_path",pidsArray);

        JSONArray modelsArray = new JSONArray();
        for (int i = 0; i < mpaths.length; i++) {
            modelsArray.put(pathToString(mpaths[i].getPathFromRootToLeaf()));
        }
        jObject.put("models_path",modelsArray);

        if (paths.length > 0) {
            String[] pathFromRootToLeaf = paths[0].getPathFromRootToLeaf();
            if (pathFromRootToLeaf.length > 0) {
                jObject.put("rootPid",pathFromRootToLeaf[0]);
            }
        }

        if (mpaths.length > 0) {
            String[] mpathFromRootToLeaf = mpaths[0].getPathFromRootToLeaf();
            if (mpathFromRootToLeaf.length > 0) {
                jObject.put("rootModel",mpathFromRootToLeaf[0]);
            }
        }

        DNNT_LOGGER.log(Level.INFO, jObject.toString());
    }

    private static String pathToString(String[] pArray) {
        return Arrays.stream(pArray).reduce("/", (identity, v) -> {
                    if (!identity.equals("/")) {
                        return identity + "/" + v;
                    } else {
                        return identity + v;
                    }
                });
    }


//    public static void logDnntAccess(RightCriteriumContext ctx) throws IOException {
//        logDnntAccess(ctx.getRequestedPid(),
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
        String[] pids = new String[] {SpecialObjects.REPOSITORY.getPid()};
        Right[] rights = manager.findRights(pids, SecuredActions.PDF_RESOURCE.getFormalName(), ctx.getUser());
        for (Right r : rights) {
            if (r == null) continue;
            if (r.getCriteriumWrapper() == null) continue;
            RightCriterium rightCriterium = r.getCriteriumWrapper().getRightCriterium();
            String qName = rightCriterium.getQName();
            if (qName.equals(PDFDNNTFlag.class.getName())) {
                return;
            }
        }
        throw new CriteriaPrecoditionException("The PDF resource must be secured by "+PDFDNNTFlag.class.getName());
    }
}
