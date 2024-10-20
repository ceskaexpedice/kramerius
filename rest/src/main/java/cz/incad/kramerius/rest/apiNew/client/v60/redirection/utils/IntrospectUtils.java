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
}
