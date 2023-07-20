package cz.incad.kramerius.rest.apiNew.client.v70.utils;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import cz.incad.kramerius.ObjectPidsPath;
import cz.incad.kramerius.SolrAccess;
import cz.incad.kramerius.fedora.om.RepositoryException;
import cz.incad.kramerius.imaging.ImageStreams;
import cz.incad.kramerius.security.RightsResolver;
import cz.incad.kramerius.security.RightsReturnObject;
import cz.incad.kramerius.security.SecuredActions;
import cz.incad.kramerius.security.impl.criteria.Licenses;
import cz.incad.kramerius.security.impl.criteria.LicensesGEOIPFiltered;
import cz.incad.kramerius.security.impl.criteria.LicensesIPFiltered;
import cz.incad.kramerius.security.impl.criteria.ReadDNNTLabels;
import cz.incad.kramerius.security.impl.criteria.ReadDNNTLabelsIPFiltered;

/**
 * Utility class for disecting <code>providedBy</code> information
 * @author happy
 */
public class ProvidedLicensesUtils {

    public static final String PROVIDED_BY_LICENSES = "providedByLicenses";

    private ProvidedLicensesUtils() {}
    
    /**
     * extract information about licenses provided for current user and current pid;
     */
    //TODO: update javadoc
    public static JSONArray extractLicensesProvidingAccess(RightsResolver rightsResolver, SolrAccess solrAccess, String pid) throws IOException, RepositoryException {
        JSONArray licenseArray = new JSONArray();
        String encoded = URLEncoder.encode("pid:\"" + pid + "\"", "UTF-8");
        JSONObject solrResponseJson = solrAccess.requestWithSelectReturningJson("q=" + encoded + "&fl=pid_paths");
    
        JSONArray docs = solrResponseJson.getJSONObject("response").getJSONArray("docs");
        if (docs.length() > 0) {
            JSONArray pidPaths = docs.getJSONObject(0).getJSONArray("pid_paths");
            List<ObjectPidsPath> pidsPathList = new ArrayList<>();
            for (int i = 0; i < pidPaths.length(); i++) {
                pidsPathList.add(new ObjectPidsPath(pidPaths.getString(i)));
            }
            for (ObjectPidsPath p : pidsPathList) {
                RightsReturnObject actionAllowed = rightsResolver.isActionAllowed(SecuredActions.A_READ.getFormalName(), pid, ImageStreams.IMG_FULL.getStreamName(), p);
                if (actionAllowed.getRight() != null && actionAllowed.getRight().getCriteriumWrapper() != null) {
                    String qName = actionAllowed.getRight().getCriteriumWrapper().getRightCriterium().getQName();
                    if (/*qName.equals(ReadDNNTFlag.class.getName()) ||
                            qName.equals(ReadDNNTFlagIPFiltered.class.getName()) ||*/
                            qName.equals(ReadDNNTLabels.class.getName()) ||
                            qName.equals(ReadDNNTLabelsIPFiltered.class.getName()) ||

                            qName.equals(Licenses.class.getName()) ||
                            qName.equals(LicensesIPFiltered.class.getName()) ||
                            qName.equals(LicensesGEOIPFiltered.class.getName())
                    ) {
                        Map<String, String> evaluateInfoMap = actionAllowed.getEvaluateInfoMap();
                        if (evaluateInfoMap.containsKey(ReadDNNTLabels.PROVIDED_BY_DNNT_LABEL)) {
                            licenseArray.put(evaluateInfoMap.get(ReadDNNTLabels.PROVIDED_BY_DNNT_LABEL));
                        }
                        break;
                    }
                }
            }
        }
        return licenseArray;
    }

}
