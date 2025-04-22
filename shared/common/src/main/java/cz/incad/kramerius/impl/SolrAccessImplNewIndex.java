/*
 * Copyright (C) 2010 Pavel Stastny
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
package cz.incad.kramerius.impl;

import cz.incad.kramerius.AbstractObjectPath;
import cz.incad.kramerius.ObjectModelsPath;
import cz.incad.kramerius.ObjectPidsPath;
import cz.incad.kramerius.SolrAccess;
import cz.incad.kramerius.security.SpecialObjects;
import cz.incad.kramerius.utils.XMLUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;

import cz.inovatika.monitoring.ApiCallEvent;
import org.apache.hc.client5.http.HttpResponseException;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.core5.http.HttpEntity;
import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import java.io.*;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import java.util.Objects;
import org.apache.commons.io.IOUtils;




public class SolrAccessImplNewIndex implements SolrAccess {

    public static final Logger LOGGER = Logger.getLogger(SolrAccessImplNewIndex.class.getName());
    
    private SolrUtils utils = new SolrUtils( KConfiguration.getInstance().getSolrSearchHost(), SolrAccessImplNewIndex.this);

    @Inject
    @Named("solr-client")
    Provider<CloseableHttpClient> provider;

    @Override
	public List<String> getExistingPids(List<String> pids) throws IOException {
    	List<String> pp = pids.stream().map(p->{
			String rp = "pid:\"" + p+"\"";
			return rp;
    	}).filter(Objects::nonNull).collect(Collectors.toList());
    	if (!pp.isEmpty()) {
    		String collect = pp.stream().collect(Collectors.joining(" OR "));
    		String query = "q=(" + URLEncoder.encode(collect,"UTF-8")+")&fl=pid";
        	Document document = utils.requestWithSelectReturningXml(query, null);
        	List<Element> docs = XMLUtils.getElementsRecursive(document.getDocumentElement(), new XMLUtils.ElementsFilter() {
				@Override
				public boolean acceptElement(Element element) {
					if (element.getNodeName().equals("doc")) {
            			return true;
            		}
					return false;
				}
			});

            
            List<String> existingPids = docs.stream().map(e-> {
            	Element strElm = XMLUtils.findElement(e, "str");
            	return strElm != null ? strElm.getTextContent() : null;
            }).filter(Objects::nonNull).collect(Collectors.toList());
            
            return existingPids;
    	}
    	return null;
	}


	@Override
	public boolean documentExist(String pid) throws IOException {
    	String query = "q=" + URLEncoder.encode("pid:" + pid.replace(":", "\\:"), "UTF-8")+"&fl=pid";
        Document document = utils.requestWithSelectReturningXml(query, null);
        Element foundElement = XMLUtils.findElement(document.getDocumentElement(), "result");
        if (foundElement != null) {
        	String numFound = foundElement.getAttribute("numFound");
        	int parsed = Integer.parseInt(numFound);
        	return parsed > 0;
        }
		return false;
	}



	@Override
    public Document getSolrDataByPid(String pid) throws IOException {
        //TODO: allow special object pids?
        //TODO: allow datastreams pids?
        String query = "q=" + URLEncoder.encode("pid:" + pid.replace(":", "\\:"), "UTF-8");
        return utils.requestWithSelectReturningXml(query, null);
    }

	

    @Override
	public Document getSolrDataByPid(String pid, String fl) throws IOException {
        String query = "q=" + URLEncoder.encode("pid:" + pid.replace(":", "\\:"), "UTF-8")+"&fl="+fl;
        return utils.requestWithSelectReturningXml(query,null);
	}


	public SolrUtils getUtils() {
        return utils;
    }

    public void setUtils(SolrUtils solrUtils) {
        this.utils = solrUtils;
    }

    @Override
    public JSONObject getJSONSolrDataByPid(String pid) throws IOException {
        JSONObject solrData = utils.getSolrDataJson(pid);
        return solrData;
    }

    @Override
    public ObjectPidsPath[] getPidPaths(Document solrDataDoc) throws IOException {
        try {
            List<String> disected = cz.incad.kramerius.utils.solr.SolrUtils.disectPidPaths(solrDataDoc);
            List<ObjectPidsPath> collected = disected.stream().map(p-> {
                return new ObjectPidsPath(p.split("/"));
            }).collect(Collectors.toList());
            
            return collected.toArray(new ObjectPidsPath[collected.size()]);
        } catch (XPathExpressionException e) {
            throw new IOException(e);
        }
    }
    
    
    @Override
    public ObjectPidsPath[] getOwnPidPaths(Document solrDataDoc) throws IOException {
        try {
            List<String> disected = cz.incad.kramerius.utils.solr.SolrUtils.disectOwnPidPaths(solrDataDoc);
            List<ObjectPidsPath> collected = disected.stream().map(p-> {
                return new ObjectPidsPath(p.split("/"));
            }).collect(Collectors.toList());
            
            return collected.toArray(new ObjectPidsPath[collected.size()]);
        } catch (XPathExpressionException e) {
            throw new IOException(e);
        }
    }

    @Override
    public ObjectPidsPath[] getPidPaths(String pid) throws IOException {
        //TODO: allow special object pids?
        //TODO: allow datastream pids
        if (pid.equals(SpecialObjects.REPOSITORY.getPid())) {
            return new ObjectPidsPath[] {ObjectPidsPath.REPOSITORY_PATH};
        }
        try {
            List<ObjectPidsPath> paths = new ArrayList<>();
            JSONObject solrData = utils.getSolrDataJson(pid);
            if (solrData != null) {
                if (solrData.has("pid_paths")) {
                    JSONArray pidPathsJsonArray = solrData.getJSONArray("pid_paths");
                    for (int i = 0; i < pidPathsJsonArray.length(); i++) {
                        paths.add(toObjectPidPath(pidPathsJsonArray.getString(i)));
                    }
                }
            }
            return paths.toArray(new ObjectPidsPath[0]);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE,e.getMessage(),e);
            throw new IOException(e);
        }
    }

    private ObjectPidsPath toObjectPidPath(String pidPath) {
        String[] pids = pidPath.split("/");
        return new ObjectPidsPath(pids);
    }

    private ObjectModelsPath toModelPidPath(String modelPath) {
        String[] pids = modelPath.split("/");
        return new ObjectModelsPath(pids);

    }


    @Override
    public ObjectPidsPath[] getPidPaths(String datastreamName, Document solrData) throws IOException {
        try {
            List<String> disected = cz.incad.kramerius.utils.solr.SolrUtils.disectPidPaths(solrData);
            return pathsInternal(datastreamName, disected);
        } catch (XPathExpressionException e) {
            throw new IOException(e);
        }
    }

    @Override
    public ObjectPidsPath[] getPidPaths(String datastreamName, Element solrDocParentElement) throws IOException {
        try {
            List<String> disected = cz.incad.kramerius.utils.solr.SolrUtils.disectPidPaths(solrDocParentElement);
            return pathsInternal(datastreamName, disected);
        } catch (XPathExpressionException e) {
            throw new IOException(e);
        }
    }

    private ObjectPidsPath[] pathsInternal(String datastreamName, List<String> disected) {
        ObjectPidsPath[] paths = new ObjectPidsPath[disected.size()];
        for (int i = 0; i < paths.length; i++) {
            String[] splitted = disected.get(i).split("/");
            if (datastreamName != null) {
                String[] splittedWithStreams = new String[splitted.length];
                for (int j = 0; j < splittedWithStreams.length; j++) {
                    splittedWithStreams[j] = splitted[j] + "/" + datastreamName;
                }
                splitted = splittedWithStreams;
            }

            ObjectPidsPath path = new ObjectPidsPath(splitted);
            // pdf in solr has special
            if (path.getLeaf().startsWith("@")) {
                String pageParent = path.cutTail(0).getLeaf();
                // path = path.injectObjectBetween(pageParent, new
                // AbstractObjectPath.Between(pageParent, path.getLeaf()));
                path = path.replace(path.getLeaf(), pageParent + "/" + path.getLeaf());
            }
            paths[i] = path;
        }

        return paths;
    }


    @Override
    public Document getSolrDataByHandle(String handle) throws IOException {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public ObjectModelsPath[] getModelPaths(String pid) throws IOException {

        //TODO: allow special object pids?
        //TODO: allow datastream pids?
        try {
            List<ObjectModelsPath> paths = new ArrayList<>();
            JSONObject solrData = utils.getSolrDataJson(pid);
            if (solrData != null) {
                String modelPath = solrData.getString("own_model_path");
                paths.add(toModelPidPath(modelPath));
            }
            return paths.toArray(new ObjectModelsPath[0]);
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    @Override
    public ObjectModelsPath[] getModelPaths(Document solrData) throws IOException {
        try {
            synchronized (solrData) {
                List<String> disected = cz.incad.kramerius.utils.solr.SolrUtils.disectModelPaths(solrData);
                ObjectModelsPath[] paths = new ObjectModelsPath[disected.size()];
                for (int i = 0; i < paths.length; i++) {
                    String[] models = disected.get(i).split("/");
                    paths[i] = new ObjectModelsPath(models);
                }
                return paths;
            }
        } catch (XPathExpressionException e) {
            throw new IOException(e);
        }
    }

    @Override
    public Map<String, AbstractObjectPath[]> getModelAndPidPaths(String pid) throws IOException {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public Document requestWithSelectReturningXml(String query, ApiCallEvent event) throws IOException {
        return utils.requestWithSelectReturningXml(query,event);
    }

    @Override
    public JSONObject requestWithSelectReturningJson(String query,ApiCallEvent event) throws IOException {
        return utils.requestWithSelectReturningJson(query, event);
    }


    @Override
    public InputStream requestWithSelectReturningInputStream(String query, String type, ApiCallEvent event) throws IOException {
        return cz.incad.kramerius.utils.solr.SolrUtils.requestWithSelectReturningStream(this.provider.get(), KConfiguration.getInstance().getSolrSearchHost(), query, type, event);
    }

    @Override
    public String requestWithSelectReturningString(String query, String type, ApiCallEvent event) throws IOException {
        return cz.incad.kramerius.utils.solr.SolrUtils.requestWithSelectReturningString(this.provider.get(),KConfiguration.getInstance().getSolrSearchHost(), query, type, event);
    }

    @Override
    public InputStream requestWithTerms(String query, String type) throws IOException {
        return cz.incad.kramerius.utils.solr.SolrUtils.requestWithTermsReturningStream(this.provider.get(),KConfiguration.getInstance().getSolrSearchHost(),query, type);
    }

    @Override
    public Document getSolrDataByParentPid(String parentPid, String offset) throws IOException {
        throw new UnsupportedOperationException("not implemented");
    }

    /**
     * @see cz.incad.kramerius.utils.solr.SolrUtils
     */
    private static class SolrUtils {

        private final String solrHost;
        //private final CloseableHttpClient client;
        private SolrAccessImplNewIndex solrAccessImplNewIndex;

        public SolrUtils(String solrHost, SolrAccessImplNewIndex index ) {
            this.solrAccessImplNewIndex = index;
            this.solrHost = solrHost;
        }

        JSONObject getSolrDataJson(String pid) throws IOException {
            String query = "q=" + URLEncoder.encode("pid:" + pid.replace(":", "\\:"), "UTF-8");
            JSONObject json = requestWithSelectReturningJson(query, null);
            return getFirstResponseDoc(json);
        }

        private JSONObject getFirstResponseDoc(JSONObject json) {
            JSONObject response = json.getJSONObject("response");
            if (response.getInt("numFound") > 0) {
                return response.getJSONArray("docs").getJSONObject(0);
            } else {
                return null;
            }
        }

        /**
         * @param query for example: q=model%3Amonograph&fl=pid%2Ctitle.search&start=0&sort=created+desc&fq=model%3Aperiodical+OR+model%3Amonograph&rows=24&hl.fragsize=20
         *              i.e. url encoded and without query param wt
         */
        JSONObject requestWithSelectReturningJson(String query, ApiCallEvent event) throws IOException {
            String jsonStr = cz.incad.kramerius.utils.solr.SolrUtils.requestWithSelectReturningString(this.solrAccessImplNewIndex.provider.get(),this.solrHost,  query, "json", event);
            JSONObject jsonObject = new JSONObject(jsonStr);
            return jsonObject;
        }

        Document requestWithSelectReturningXml(String query, ApiCallEvent event) throws IOException {
            try {
                InputStream in = cz.incad.kramerius.utils.solr.SolrUtils.requestWithSelectReturningStream(this.solrAccessImplNewIndex.provider.get(),this.solrHost,query, "xml", event);
                return XMLUtils.parseDocument(in);
            } catch (ParserConfigurationException e) {
                throw new IOException(e);
            } catch (SAXException e) {
                throw new IOException(e);
            }
        }

        String requestWithSelectReturningString(String query, String type) throws IOException {
            InputStream in = requestWithSelectReturningStream(query, type);
            BufferedReader streamReader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
            StringBuilder responseStrBuilder = new StringBuilder();
            String inputStr;
            while ((inputStr = streamReader.readLine()) != null) {
                responseStrBuilder.append(inputStr);
            }
            return responseStrBuilder.toString();
        }

        /**
         * @param query for example: q=model%3Amonograph&fl=pid%2Ctitle.search&start=0&sort=created+desc&fq=model%3Aperiodical+OR+model%3Amonograph&rows=24&hl.fragsize=20
         *              i.e. url encoded and without query param wt
         */
        InputStream requestWithSelectReturningStream(String query, String type) throws IOException {
            String url = String.format("%s/select?%s&wt=%s", solrHost, query, type);
            HttpGet httpGet = new HttpGet(url);

            try (CloseableHttpResponse response = solrAccessImplNewIndex.provider.get().execute(httpGet)) {
                if (response.getCode() == 200) {
                    return readContentAndProvideThroughBufferedStream(response.getEntity());
                } else {
                    HttpEntity entity = response.getEntity();
                    if (entity != null) {
                        InputStream stream = readContentAndProvideThroughBufferedStream(entity);
                        String strEntity = IOUtils.toString(stream, "UTF-8");
                        LOGGER.log(Level.SEVERE,  String.format("Error entity %s", strEntity));
                    }
                    throw new HttpResponseException(response.getCode(), response.getReasonPhrase());
                }
            }
        }

        /**
         * @param query for example: q=model%3Amonograph&fl=pid%2Ctitle.search&start=0&sort=created+desc&fq=model%3Aperiodical+OR+model%3Amonograph&rows=24&hl.fragsize=20
         *              i.e. url encoded and without query param wt
         */
        InputStream requestWithTermsReturningStream(String query, String type) throws IOException {
            String url = String.format("%s/terms?%s&wt=%s", solrHost, query, type);
            HttpGet httpGet = new HttpGet(url);
            try (CloseableHttpResponse response = solrAccessImplNewIndex.provider.get().execute(httpGet)) {
                if (response.getCode() == 200) {
                    return readContentAndProvideThroughBufferedStream(response.getEntity());
                } else {
                    throw new HttpResponseException(response.getCode(), response.getReasonPhrase());
                }
            }
        }

        //reads and closes entity's content stream
        private InputStream readContentAndProvideThroughBufferedStream(org.apache.hc.core5.http.HttpEntity entity) throws IOException {
            try (InputStream src = entity.getContent()) {
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                IOUtils.copy(src, bos);
                return new ByteArrayInputStream(bos.toByteArray());
            }
        }
    }
}
