package cz.incad.kramerius.rest.apiNew.client.v70.utils;

import cz.incad.kramerius.ObjectPidsPath;
import cz.incad.kramerius.SolrAccess;
import cz.incad.kramerius.imaging.ImageStreams;
import cz.incad.kramerius.security.Right;
import cz.incad.kramerius.security.RightsResolver;
import cz.incad.kramerius.security.RightsReturnObject;
import cz.incad.kramerius.security.SecuredActions;
import cz.incad.kramerius.security.impl.criteria.Licenses;
import cz.incad.kramerius.security.licenses.lock.ExclusiveLockMap;
import cz.incad.kramerius.security.utils.LicensesCriteriaList;
import org.apache.commons.lang3.tuple.Triple;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Utility class for disecting <code>providedBy</code> information
 * @author happy
 */
public class RightRuntimeInformations {

    public static final String PROVIDED_BY_LICENSES = "providedByLicenses";
    public static final String ACCESSIBLE_LOCSK = "accessibleLocks";

    private RightRuntimeInformations() {}
    

    /**
     * extract information about licenses provided for current user and current pid;
     */
    //TODO: update javadoc
    public static RuntimeInformation extractInformations(RightsResolver rightsResolver, SolrAccess solrAccess, String pid) throws IOException {
        
        List<String> licenseList = new ArrayList<>();
        List<Triple<String, String, Right>> locks = new ArrayList<>();
        
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
                            
                            LicensesCriteria(qName)
                    ) {
                        Map<String, String> evaluateInfoMap = actionAllowed.getEvaluateInfoMap();
                        if (evaluateInfoMap.containsKey(Licenses.PROVIDED_BY_LABEL)) {
                            licenseList.add(evaluateInfoMap.get(Licenses.PROVIDED_BY_LABEL));
                        }
                        
                        if (evaluateInfoMap.containsKey(ExclusiveLockMap.LOCK_HASH)) {
                            String hash = evaluateInfoMap.get(ExclusiveLockMap.LOCK_HASH);
                            String type = evaluateInfoMap.get(ExclusiveLockMap.LOCK_TYPE);
                            locks.add(Triple.of(hash, type, actionAllowed.getRight()));
                            
                        }
                        break;
                    }
                }
            }
        }
        
        return new RuntimeInformation(licenseList, locks);
        
    }
    
    public static boolean LicensesCriteria(String qName) {
        return LicensesCriteriaList.NAMES.contains(qName);
    }

    public static class RuntimeInformation {
        
        private List<String> providingLicenses;
        // hash, type, right
        private List<Triple<String, String, Right>> locks;
 
        public RuntimeInformation(List<String> providingLicenses, List<Triple<String, String, Right>> locks) {
            super();
            this.providingLicenses = providingLicenses;
            this.locks = locks;
        }
        
        // getProvidedLicenses
        public List<String> getProvidingLicenses() {
            return providingLicenses;
        }
        
        public JSONArray getProvidingLicensesAsJSONArray() {
          JSONArray jsonArray = new JSONArray();
          this.providingLicenses.forEach(jsonArray::put);
          return jsonArray;
        }

        // getLocks
        public List<Triple<String, String, Right>> getLocks() {
            return locks;
        }

        public JSONArray getLockAsJSONArray() {
            JSONArray jsonArray = new JSONArray();
            this.locks.forEach(t-> {
                JSONObject tripleObject = new JSONObject();
                
                String hash = t.getLeft();
                tripleObject.put("hash", hash);
                String type = t.getMiddle();
                tripleObject.put("type", type);

                Right r = t.getRight();
                
                JSONObject rightObj = new JSONObject();
                rightObj.put("id", r.getId());
                rightObj.put("license", r.getCriteriumWrapper().getLicense().getName());
                rightObj.put("params", r.getCriteriumWrapper().getCriteriumParams().getShortDescription());
                
                tripleObject.put("right", rightObj);
                jsonArray.put(tripleObject);
            });
            return jsonArray;
        }
        
    }
    
}
