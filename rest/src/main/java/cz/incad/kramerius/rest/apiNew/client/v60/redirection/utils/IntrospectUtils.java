package cz.incad.kramerius.rest.apiNew.client.v60.redirection.utils;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.json.JSONArray;
import org.json.JSONObject;

import com.sun.jersey.api.client.Client;

import cz.incad.kramerius.cdk.ChannelUtils;
import cz.incad.kramerius.rest.apiNew.client.v60.libs.Instances;
import cz.incad.kramerius.rest.apiNew.client.v60.libs.OneInstance;
import cz.incad.kramerius.rest.apiNew.client.v60.libs.OneInstance.InstanceType;
import cz.incad.kramerius.utils.conf.KConfiguration;

public class IntrospectUtils {
    
    private IntrospectUtils() {}

    public static Pair<List<String>, List<String>> introspectPid(Client client, Instances instancesObject,  String pid) throws UnsupportedEncodingException {
        List<String> models = new ArrayList<>();
        List<String> liveInstances = new ArrayList<>();
        List<OneInstance> instances = instancesObject.enabledInstances();
        for(OneInstance inst:instances) {
            String library = inst.getName();
            boolean channelAccess = KConfiguration.getInstance().getConfiguration().containsKey("cdk.collections.sources." + library + ".licenses") ?  KConfiguration.getInstance().getConfiguration().getBoolean("cdk.collections.sources." + library + ".licenses") : false;
            if(channelAccess) {
                String channel = KConfiguration.getInstance().getConfiguration().getString("cdk.collections.sources." + library + ".forwardurl");
                String solrChannelUrl = ChannelUtils.solrChannelUrl(inst.getInstanceType().name(), channel);
                InstanceType instType = inst.getInstanceType();
                String solrPid = ChannelUtils.solrChannelPidExistence(client, channel, solrChannelUrl, instType.name(), pid);
                
                JSONObject obj = new JSONObject(solrPid);
                JSONObject responseObject = obj.getJSONObject("response");
                JSONArray docs = responseObject.getJSONArray("docs");
                if (docs.length() > 0 ) {
                    JSONObject doc = docs.getJSONObject(0);
                    switch(inst.getInstanceType()) {
                        case V5:
                            models.add(doc.optString("fedora.model"));
                            liveInstances.add(inst.getName());
                        break;
                        case V7:
                            models.add(doc.optString("model"));
                            liveInstances.add(inst.getName());
                        break;
                    }
                }
            }
        }
        return Pair.of(models, liveInstances);
    }

    /** introspect utils */
    public static JSONObject introspectSolr(Client client, Instances libraries, String pid) throws UnsupportedEncodingException {
            JSONObject obj = new JSONObject();
            List<OneInstance> instances = libraries.enabledInstances();
            for(OneInstance inst:instances) {
                String library = inst.getName();
                boolean channelAccess = KConfiguration.getInstance().getConfiguration().containsKey("cdk.collections.sources." + library + ".licenses") ?  KConfiguration.getInstance().getConfiguration().getBoolean("cdk.collections.sources." + library + ".licenses") : false;
                if(channelAccess) {
                    String channel = KConfiguration.getInstance().getConfiguration().getString("cdk.collections.sources." + library + ".forwardurl");
                    String solrChannelUrl = ChannelUtils.solrChannelUrl(inst.getInstanceType().name(), channel);
                    InstanceType instType = inst.getInstanceType();
                    String solrPid = ChannelUtils.solrChannelPidExistence(client, channel, solrChannelUrl, instType.name(), pid);
                    if (solrPid != null) {
                        JSONObject responseObj = null; 
                        switch(instType) {
                            case V5:
                                // make solr fields accessible
                                // PID copy to pid
                                // fedora.model copy to model
                                // root_pid copy to root_pid
                                // pid_path copy to pid_paths
                                JSONObject k5resp = new JSONObject(solrPid);
                                JSONObject optJSONObject = k5resp.optJSONObject("response");
                                if (optJSONObject != null) {
                                    JSONArray docs = optJSONObject.getJSONArray("docs");
                                    for (int i = 0; i < docs.length(); i++) {
                                        JSONObject doc = docs.getJSONObject(i);
                                        if (doc.has("PID")) {
                                            doc.put("pid", doc.getString("PID"));
                                        }
                                        if (doc.has("fedora.model")) {
                                            doc.put("model", doc.getString("fedora.model"));
                                        }
    
                                        if (doc.has("pid_path")) {
                                            doc.put("pid_paths", doc.getJSONArray("pid_path"));
                                        }
    
                                        if (doc.has("root_pid")) {
                                            doc.put("root.pid", doc.getString("root_pid"));
                                        }
    }
                                }
                                responseObj = k5resp;
                                break;
                                
                            default:
                                responseObj = new JSONObject(solrPid); 
                                break;
                        }
                        obj.put(library, responseObj);
                    }
                }
            }
            return obj;
        }
}
