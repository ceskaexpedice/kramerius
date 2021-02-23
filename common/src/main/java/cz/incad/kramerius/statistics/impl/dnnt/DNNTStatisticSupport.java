package cz.incad.kramerius.statistics.impl.dnnt;

import com.google.inject.Provider;
import cz.incad.kramerius.ObjectModelsPath;
import cz.incad.kramerius.ObjectPidsPath;
import cz.incad.kramerius.security.RightsReturnObject;
import cz.incad.kramerius.security.User;
import cz.incad.kramerius.security.impl.criteria.utils.CriteriaDNNTUtils;
import cz.incad.kramerius.statistics.impl.dnnt.format.DNNTStatisticsDateFormat;
import cz.incad.kramerius.utils.IPAddressUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class DNNTStatisticSupport {

    // access logger for kibana processing
    public static Logger KRAMERIUS_LOGGER_FOR_KIBANA = Logger.getLogger("kramerius.access");


    private Provider<HttpServletRequest> requestProvider;
    private Provider<User> userProvider;
    private DNNTStatisticsDateFormat dateFormat;

    public DNNTStatisticSupport(Provider<HttpServletRequest> requestProvider, Provider<User> userProvider, DNNTStatisticsDateFormat dateFormat) {
        this.requestProvider = requestProvider;
        this.userProvider = userProvider;
        this.dateFormat = dateFormat;
    }

    public void log(String pid, String rootTitle, String dcTitle, String solrDate, String modsDate, String dnntFlag, String policy, List<String> dcPublishers, List<String> dcAuthors, ObjectPidsPath[] paths, ObjectModelsPath[] mpaths) throws IOException {
        User user = this.userProvider.get();
        RightsReturnObject rightsReturnObject = CriteriaDNNTUtils.currentThreadReturnObject.get();
        boolean providedByDnnt =  rightsReturnObject != null ? CriteriaDNNTUtils.allowedByReadDNNTFlagRight(rightsReturnObject) : false;

        // store json object
        JSONObject jObject = toJSON(pid, rootTitle, dcTitle,
                IPAddressUtils.getRemoteAddress(requestProvider.get(), KConfiguration.getInstance().getConfiguration()),
                user != null ? user.getLoginname() : null,
                user != null ? user.getEmail() : null,
                solrDate,
                modsDate,
                dnntFlag,
                providedByDnnt,
                policy,
                rightsReturnObject.getEvaluateInfoMap(),
                user.getSessionAttributes(),
                dcAuthors,
                dcPublishers,
                paths,
                mpaths
        );

        DNNTStatisticSupport.KRAMERIUS_LOGGER_FOR_KIBANA.log(Level.INFO, jObject.toString());
    }

    public JSONObject toJSON(String pid,  String rootTitle, String dcTitle, String remoteAddr, String username, String email, String publishedDate, String modsDate, String dnntFlag, boolean providedByDnnt, String policy, Map<String,String> rightEvaluationAttribute, Map<String,String> sessionAttributes, List<String> dcAuthors, List<String> dcPublishers, ObjectPidsPath[] paths, ObjectModelsPath[] mpaths) throws IOException {

        LocalDateTime date = LocalDateTime.now();
        String timestamp = date.format(DateTimeFormatter.ISO_DATE_TIME);

        JSONObject jObject = new JSONObject();

        jObject.put("pid",pid);
        jObject.put("remoteAddr",remoteAddr);
        jObject.put("username",username);
        jObject.put("email",email);

        jObject.put("rootTitle",rootTitle);
        jObject.put("dcTitle",dcTitle);

        if (dnntFlag != null )  jObject.put("dnnt", dnntFlag.trim().toLowerCase().equals("true"));

        // info from criteriums
        rightEvaluationAttribute.keySet().stream().forEach(key->{ jObject.put(key, rightEvaluationAttribute.get(key)); });

        jObject.put("providedByDnnt", providedByDnnt);
        jObject.put("policy", policy);

        if (getDate(publishedDate) != null)  jObject.put("solrDate", getDate(publishedDate));
        if (getDate(modsDate) != null) jObject.put("publishedDate", getDate(modsDate));

        jObject.put("date",timestamp);

        sessionAttributes.keySet().stream().forEach(key->{ jObject.put(key, sessionAttributes.get(key)); });


        if (!dcAuthors.isEmpty()) {
            JSONArray authorsArray = new JSONArray();
            for (int i=0,ll=dcAuthors.size();i<ll;i++) {
                authorsArray.put(dcAuthors.get(i));
            }
            jObject.put("authors",authorsArray);
        }

        if (!dcPublishers.isEmpty()) {
            JSONArray publishersArray = new JSONArray();
            for (int i=0,ll=dcPublishers.size();i<ll;i++) {
                publishersArray.put(dcPublishers.get(i));
            }
            jObject.put("publishers",publishersArray);
        }

        JSONArray pidsArray = new JSONArray();
        for (int i = 0; i < paths.length; i++) {
            pidsArray.put(Arrays.stream(paths[i].getPathFromRootToLeaf()).collect(Collectors.joining("/")));
        }
        jObject.put("pids_path",pidsArray);

        JSONArray modelsArray = new JSONArray();
        for (int i = 0; i < mpaths.length; i++) {
            modelsArray.put(Arrays.stream(mpaths[i].getPathFromRootToLeaf()).collect(Collectors.joining("/")));
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
        return jObject;
    }

    private  String getDate(String publishedDate)  {
        if (this.dateFormat != null) {
            return dateFormat.format(publishedDate);
        } else return null;
    }
}
