package cz.incad.kramerius.security.impl.criteria.utils;

import cz.incad.kramerius.SolrAccess;
import cz.incad.kramerius.security.*;
import cz.incad.kramerius.security.impl.criteria.*;
import cz.incad.kramerius.security.licenses.License;
import cz.incad.kramerius.security.licenses.impl.lock.ExclusiveLockMapItemImpl;
import cz.incad.kramerius.security.licenses.lock.ExclusiveLock;
import cz.incad.kramerius.security.licenses.lock.ExclusiveLockMap;
import cz.incad.kramerius.security.licenses.lock.ExclusiveLockMapItem;
import cz.incad.kramerius.security.utils.LicensesCriteriaList;
import cz.incad.kramerius.utils.conf.KConfiguration;
import cz.incad.kramerius.utils.solr.SolrUtils;

import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Document;

import com.maxmind.geoip2.DatabaseReader;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class CriteriaLicenseUtils {

    public static ThreadLocal<RightsReturnObject> currentThreadReturnObject = new ThreadLocal<>();

    public static Logger LOGGER = Logger.getLogger(CriteriaLicenseUtils.class.getName());

    // check dnnt flag from solr
    public static EvaluatingResultState checkDnnt(RightCriteriumContext ctx) {
        try {
            SolrAccess solrAccess = ctx.getSolrAccessNewIndex();
            String pid = ctx.getRequestedPid();
            Document doc = solrAccess.getSolrDataByPid(pid);
            String val = SolrUtils.disectDNNTFlag(doc.getDocumentElement());
            return (val != null && val.equals("true")) ? EvaluatingResultState.TRUE
                    : EvaluatingResultState.NOT_APPLICABLE;
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            return EvaluatingResultState.NOT_APPLICABLE;
        }
    }

    // allowed by license
    public static boolean allowedByReadLicenseRight(RightsReturnObject obj, License license) {
        if (obj.getRight() != null && obj.getRight().getCriteriumWrapper() != null) {
            if (LicensesCriteriaList.NAMES
                    .contains(obj.getRight().getCriteriumWrapper().getRightCriterium().getQName())) {

                String providedByLicense = obj.getEvaluateInfoMap().get(ReadDNNTLabels.PROVIDED_BY_LICENSE);
                if (providedByLicense == null) {
                    providedByLicense = obj.getEvaluateInfoMap().get(ReadDNNTLabels.PROVIDED_BY_LABEL);
                }

                return license != null && license.getName() != null && providedByLicense != null
                        && providedByLicense.equals(license.getName());
            }
        }
        return false;
    }

    public static boolean allowedByReadDNNTFlagRight(RightsReturnObject obj) {
        List<String> dnntLicenses = Arrays.asList("dnnto", "dnntt");
        if (obj.getRight() != null && obj.getRight().getCriteriumWrapper() != null) {
            if (LicensesCriteriaList.NAMES
                    .contains(obj.getRight().getCriteriumWrapper().getRightCriterium().getQName())) {
                License license = obj.getRight().getCriteriumWrapper().getLicense();
                return dnntLicenses.contains(license.getName());
            }
        }
        return false;
    }

    public static void checkContainsCriterium(RightCriteriumContext ctx, RightsManager manager, Class... clzs)
            throws CriteriaPrecoditionException {
        String[] pids = new String[] { SpecialObjects.REPOSITORY.getPid() };
        Right[] rights = manager.findRights(pids, SecuredActions.A_PDF_READ.getFormalName(), ctx.getUser());
        for (Right r : rights) {
            if (r == null)
                continue;
            if (r.getCriteriumWrapper() == null)
                continue;
            RightCriterium rightCriterium = r.getCriteriumWrapper().getRightCriterium();
            String qName = rightCriterium.getQName();
            for (Class clz : clzs) {
                if (qName.equals(clz.getName())) {
                    return;
                }
            }
        }
        List<String> collections = Arrays.stream(clzs).map(s -> s.getName()).collect(Collectors.toList());
        throw new CriteriaPrecoditionException("These flags are not set : " + collections);
    }

    public static boolean matchLicense(Document solrDoc, License license) {
        List<String> indexedLabels = SolrUtils.disectLicenses(solrDoc.getDocumentElement());
        if (indexedLabels != null && license != null) {
            String labelName = license.getName();
            if (indexedLabels.contains(labelName))
                return true;
        }
        return false;
    }

    public static Object INTERNAL_SYNC_LOCK = new Object();

    public static EvaluatingResultState licenseLock(Right right, RightCriteriumContext ctx, String pid, License lic) throws IOException {
        User user = ctx.getUser();
        if (user.getSessionAttributes().containsKey("token_id")) {
            String licensesPid = pid;
            String tokenId = user.getSessionAttributes().get("token_id");
            ExclusiveLock exclusiveLock = lic.getExclusiveLock();

            String q = "pid:\""+pid+"\"";
            String query = "fl=pid+root.pid+licenses+own_pid_path&rows=1&q=" + URLEncoder.encode(q, "UTF-8");
            JSONObject jsonObject = ctx.getSolrAccessNewIndex().requestWithSelectReturningJson(query);
            JSONObject response = jsonObject.getJSONObject("response");
            JSONArray docs = response.getJSONArray("docs");
            if (docs.length() > 0 ) {
                JSONObject doc = docs.getJSONObject(0);
                if (doc.has("licenses")) {
                    licensesPid = doc.getString("pid");
                } else {
                    String ownPidPath = doc.getString("own_pid_path");
                    String rootPid = doc.getString("root.pid");
                    String rootPidQ = "root.pid:\""+rootPid +"\" AND licenses:"+lic.getName();
                    String rootPidEncodedQ = "fl=pid+root.pid+licenses+own_pid_path&rows=1000&q=" + URLEncoder.encode(rootPidQ, "UTF-8");
                    // tituly, ktere maji prirazenou licenci a ktere maji stejny root.pid
                    JSONObject rootPidObjects = ctx.getSolrAccessNewIndex().requestWithSelectReturningJson(rootPidEncodedQ);
                    JSONObject rootPidResponse = rootPidObjects.getJSONObject("response");
                    JSONArray rootPidDocs = rootPidResponse.getJSONArray("docs");
                    if (rootPidDocs.length() == 1) {
                        licensesPid =  rootPidDocs.getJSONObject(0).getString("pid");
                    } else if (rootPidDocs.length() > 1) {
                        // vybrat nejlepsi prefix; nyni pouze prvni
                        licensesPid =  rootPidDocs.getJSONObject(0).getString("pid");
                    }
                }
            }
            
            String lockHash = exclusiveLock.createLockHash(lic, right, licensesPid);
            ExclusiveLockMap lockMap = ctx.getExclusiveLockMaps().findOrCreateByHash(lockHash, lic, licensesPid);
            synchronized (INTERNAL_SYNC_LOCK) {
                ExclusiveLockMapItem item = lockMap.findByTokenId(tokenId);
                if (item != null) {
                    boolean validitem = item.isValid(Instant.now());
                    if (validitem) {
                        ctx.getEvaluateInfoMap().put(ReadDNNTLabels.PROVIDED_BY_LABEL, lic.getName());
                        ctx.getEvaluateInfoMap().put(ReadDNNTLabels.PROVIDED_BY_LICENSE, lic.getName());
                        return EvaluatingResultState.TRUE;
                    } else {
                        // no valid
                        lockMap.deregisterItem(item);
                        ctx.getEvaluateInfoMap().put(ExclusiveLockMap.LOCK_HASH, lockHash);
                        ctx.getEvaluateInfoMap().put(ExclusiveLockMap.LOCK_TYPE,
                                lic.getExclusiveLock().getType().name());
                        return EvaluatingResultState.NEED_LOCK;
                    }
                } else {
                    if (lockMap.checkAvailabitlity()) {
                        ctx.getEvaluateInfoMap().put(ExclusiveLockMap.LOCK_HASH, lockHash);
                        ctx.getEvaluateInfoMap().put(ExclusiveLockMap.LOCK_TYPE,
                                lic.getExclusiveLock().getType().name());
                        return EvaluatingResultState.NEED_LOCK;
                    } else {
                        return EvaluatingResultState.NOT_APPLICABLE;
                    }
                }
            }
            // }
        } else {
            return EvaluatingResultState.NOT_APPLICABLE;
        }
    }
}