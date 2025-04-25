/*
 * Copyright (C) 2025  Inovatika
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package cz.incad.kramerius.rest.apiNew.client.v70.redirection.introspect;

import com.google.common.base.Functions;
import com.google.common.collect.Lists;
import cz.incad.kramerius.rest.apiNew.client.v70.libs.Instances;
import cz.incad.kramerius.rest.apiNew.client.v70.redirection.utils.IntrospectUtils;
import cz.incad.kramerius.utils.StringUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/** Reprezentuje jeden dotaz na chranene kanaly ale pouze ty, ktere maji dany dokument*/
public class IntrospectLiveResponse {

    public static final Logger LOGGER = Logger.getLogger(IntrospectLiveResponse.class.getName());

    /** Polozky ze vsech 'zivych' kanalu */
    private List<IntrospectLiveResponseItem> introspectItems = new ArrayList<>();

    private String pid;

    public IntrospectLiveResponse(String pid) {
        this.pid = pid;
    }

    public String getPid() {
        return pid;
    }

    public List<IntrospectLiveResponseItem> getIntrospectItems() {
        return introspectItems;
    }



    public List<String> getUniqueRootPids() {
        return getUniqueValues("root.pid");
    }

    public List<String> getUniqueModels() {
        return getUniqueValues("model");
    }

    public boolean isConflictedResult() {
        return this.getUniqueModels().size() > 1 || this.getUniqueRootPids().size() > 1;
    }

    @NotNull
    private List<String> getUniqueValues(String key) {
        List<String> vals = new ArrayList<>();
        this.introspectItems.stream().forEach(item-> {
            JSONObject obj = item.response();
            if (obj.has(key)) {
                String rootPid = obj.getString(key);
                if (!vals.contains(rootPid)) vals.add(rootPid);
            }
        });
        return vals;
    }


    public Set<String> getKeys(String... filteredKeys) {
        Set<String> keys= this.introspectItems.stream().map(IntrospectLiveResponseItem::acronym).collect(Collectors.toSet());
        Arrays.stream(filteredKeys).forEach(fk-> {
            keys.remove(fk);
        });
        return keys;
    }



    public IntrospectLiveResponseItem getPivotItem() {
        if (!isConflictedResult() && this.introspectItems.size() > 0) {
            return this.introspectItems.get(0);
        } else return null;
    }


    public static class Builder {
        private CloseableHttpClient apacheClient;
        private Instances instances;
        private String pid;

        public Builder withHttpClient(CloseableHttpClient client) {
            this.apacheClient = client;
            return this;
        }

        public Builder withInstances(Instances instances) {
            this.instances = instances;
            return this;
        }

        public Builder withPid(String pid) {
            this.pid = pid;
            return this;
        }

        public IntrospectLiveResponse build() {
            if (!StringUtils.isAnyString(this.pid))  {
                throw new IllegalStateException("Pid must be provided!");
            }
            if (this.instances == null) {
                throw new IllegalStateException("Instances must be set!");
            }
            if (this.apacheClient == null) {
                throw new IllegalStateException("Apache HTTP client must be set!");
            }
            IntrospectLiveResponse response = new IntrospectLiveResponse(pid);
            try {
                JSONObject jsonResult = getIntrospectSolr();
                Set<?> keys = jsonResult.keySet();
                for (Object keyObj : keys) {
                    String key = keyObj.toString();
                    JSONObject solrResult = jsonResult.getJSONObject(key);
                    JSONObject solrResponse = solrResult.getJSONObject("response");
                    int numFound = solrResponse.optInt("numFound");
                    if (numFound > 0) {
                        JSONObject doc = solrResponse.getJSONArray("docs").getJSONObject(0);
                        String pid = doc.optString("pid");
                        String rootPid = doc.optString("root.pid");
                        String model = doc.optString("model");
                        JSONArray optPidPaths = doc.optJSONArray("pid_paths");
                        List<String> pidPaths = new ArrayList<>();
                        if (optPidPaths != null) {
                            for (int i = 0; i < optPidPaths.length(); i++) { pidPaths.add(optPidPaths.getString(i));}
                        }
                        response.introspectItems.add(new IntrospectLiveResponseItem(key,pid,rootPid, model,pidPaths, doc));
                    }
                }
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }
            return response;
        }

        @NotNull
        public JSONObject getIntrospectSolr() throws UnsupportedEncodingException {
            return IntrospectUtils.introspectSolr(apacheClient, instances, pid, false);
        }
    }

    @Override
    public String toString() {
        return "IntrospectLiveResponse{" +
                "introspectItems=" + introspectItems +
                ", pid='" + pid + '\'' +
                '}';
    }

    /** Introspect record */
    public record IntrospectLiveResponseItem(String acronym, String pid, String rootPid, String model, List<String> pidPaths, JSONObject response){

        public String getFirtsPidPath() {
            return (this.pidPaths != null && !this.pidPaths.isEmpty()) ?   pidPaths.get(0) : null;
        }

        public String getOwnParent() {
            String firtsPidPath = getFirtsPidPath();
            List<String> listPath = Arrays.stream(firtsPidPath.split("/")).collect(Collectors.toList());
            int lastIndex = listPath.size()-2;
            return lastIndex >=0 ? listPath.get(lastIndex) : null;
        }

        public boolean isTopLevelModel() {
            List<String> topLevelModels = Lists.transform(KConfiguration.getInstance().getConfiguration().getList("fedora.topLevelModels"), Functions.toStringFunction());
            return topLevelModels.contains(model);
        }
    }

}
