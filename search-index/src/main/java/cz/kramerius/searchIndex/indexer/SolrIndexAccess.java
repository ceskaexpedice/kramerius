package cz.kramerius.searchIndex.indexer;

import cz.incad.kramerius.utils.IterationUtils;
import cz.kramerius.searchIndex.indexer.nodes.RepositoryNode;
import cz.kramerius.shared.Dom4jUtils;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpClientUtil;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.params.MapSolrParams;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;

import java.io.*;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static cz.kramerius.searchIndex.indexer.execution.Indexer.*;

public class SolrIndexAccess {

    public static final int MAX_TIME_WITHOUT_COMMIT_MS = 15000; //15 seconds
    public static final int CONNECTION_TIMEOUT = 10000;
    public static final int SOCKET_TIMEOUT = 60000;

    private static HttpSolrClient solrClient;



    private final String collection; //because solrClient is buggy and still requires explicit collection-name as a parameter for some operations even though it gets collection-name in the constructor

    public SolrIndexAccess(SolrConfig config) {
        System.setProperty("solr.cloud.client.stallTime", "119999");

        if (solrClient == null) {
                solrClient = buildHttpSolrClientWithoutAuth(config.baseUrl, config.collection, config.useHttps);
        }

        this.collection = config.collection;
    }


    private HttpSolrClient buildHttpSolrClientWithoutAuth(String baseUrl, String collection, boolean useHttps) {
        return new HttpSolrClient.Builder(buildUrl(baseUrl, collection, useHttps))
                .withConnectionTimeout(CONNECTION_TIMEOUT)
                .withSocketTimeout(SOCKET_TIMEOUT)
                .build();
    }

    private String buildUrl(String baseUrl, String collection, boolean useHttps) {
        StringBuilder builder = new StringBuilder();
        if (useHttps) {
            builder.append("https://");
        } else {
            builder.append("http://");
        }
        builder.append(baseUrl);
        if (!baseUrl.endsWith("/")) {
            builder.append('/');
        }
        //builder.append(collection).append('/');
        return builder.toString();
    }

    public UpdateResponse indexFromXmlFile(File xmlFile, boolean explicitCommit) throws IOException, SolrServerException, DocumentException {
        return indexFromXmlInputStream(new FileInputStream(xmlFile), explicitCommit);
    }

    public UpdateResponse indexFromXmlString(String xmlString, boolean explicitCommit) throws SolrServerException, IOException, DocumentException {
        return indexFromXmlInputStream(new ByteArrayInputStream(xmlString.getBytes()), explicitCommit);
    }

    private UpdateResponse indexFromXmlInputStream(InputStream in, boolean explicitCommit) throws IOException, SolrServerException, DocumentException {
        UpdateResponse addResponse = null;
        for (SolrInputDocument doc : extractSolrInputDocuments(in)) {
            //there will always only be on ADD anyway
            addResponse = solrClient.add(collection, doc, MAX_TIME_WITHOUT_COMMIT_MS);
        }
        if (explicitCommit) {
            commit();
        }
        return addResponse;
    }

    private List<SolrInputDocument> extractSolrInputDocuments(InputStream in) throws DocumentException {
        ArrayList<SolrInputDocument> solrDocList = new ArrayList<>();
        List<Node> docEls = Dom4jUtils.buildXpath("add/doc").selectNodes(new SAXReader().read(in));
        for (Node docEl : docEls) {
            SolrInputDocument solrInputDoc = new SolrInputDocument();
            for (Node field : docEl.selectNodes("field")) {
                String name = ((Element) field).attribute("name").getValue();
                String value = field.getStringValue().trim();
                solrInputDoc.addField(name, value);
            }
            solrDocList.add(solrInputDoc);
        }
        return solrDocList;
    }

    public UpdateResponse deleteById(String id) throws IOException, SolrServerException {
        //System.out.println("deleting " + id);
        UpdateResponse deleteResponse = IterationUtils.useCompositeId()
                ? solrClient.deleteByQuery(collection, "pid:" + id.replace(":", "\\:"))
                : solrClient.deleteById(collection, id);//        UpdateResponse deleteResponse = solrClient.deleteByQuery(collection, "root.pid:\""+rootPid+"\"");
        //System.out.println("delete response: " + deleteResponse); UpdateResponse does not have a ToString anyway
        return commit();
    }

    public UpdateResponse deleteByParentRootPid(String rootpid) throws IOException, SolrServerException {
        //UpdateResponse deleteResponse = 
        solrClient.deleteByQuery(collection, "own_parent.pid:\"" + rootpid + "\"");
        return commit();
    }

    public UpdateResponse deleteByIds(List<String> ids) throws IOException, SolrServerException {
        //UpdateResponse updateResponse;
        for (String id : ids) {
            //updateResponse = 
            deleteById(id);
        }
        //UpdateResponse commitResponse = commit();
        //System.out.println("commit response: " + commitResponse);
        return commit();
    }

    public UpdateResponse deleteAll() throws IOException, SolrServerException {
        //System.out.println("deleting all");
        //UpdateResponse deleteResponse = 
        solrClient.deleteByQuery(collection, "*");
        //System.out.println("delete response: " + deleteResponse);
        //UpdateResponse commitResponse = commit();
        //System.out.println("commit response: " + commitResponse);
        return commit();
    }

    public SolrDocumentList searchInAllFields(String query) throws IOException, SolrServerException {
        return solrClient.query(collection, new MapSolrParams(Collections.singletonMap("q", query))).getResults();
    }

    public SolrDocument getObjectByPid(String pid) throws SolrServerException, IOException {
        SolrDocumentList result = searchInAllFields("pid:" + pid.replace(":", "\\:"));
        return !result.isEmpty() ? result.get(0) : null;
    }

    public String getCompositeIdByPid(String pid) throws SolrServerException, IOException {
        SolrDocumentList result = searchInAllFields("pid:" + pid.replace(":", "\\:"), "compositeId");
        return !result.isEmpty() ? (String) result.get(0).getFieldValue("compositeId") : "";
    }

    public SolrDocumentList searchInAllFields(String query, String outputFieldList) throws IOException, SolrServerException {
        Map<String, String> queryParamMap = new HashMap<>();
        queryParamMap.put("q", query);
        if (outputFieldList != null) {
            queryParamMap.put("fl", outputFieldList);
        }
        MapSolrParams queryParams = new MapSolrParams(queryParamMap);
        return solrClient.query(collection, queryParams).getResults();
    }

    public UpdateResponse commit() throws IOException, SolrServerException {
        return solrClient.commit(collection);
    }

    public void setSingleFieldValue(String pid, RepositoryNode repositoryNode, String fieldName, Object value, boolean indexTime, boolean explicitCommit) {
        try {
            SolrInputDocument updateDoc = new SolrInputDocument();
            ensureCompositeId(updateDoc, repositoryNode, pid);
            updateDoc.addField("pid", pid);

            Map<String, Object> updateData = new HashMap<>();
            updateData.put("set", value == null ? null : value.toString());
            updateDoc.addField(fieldName, updateData);

            if (indexTime) {
                Map<String, Object> timestamp = new HashMap<>();
                timestamp.put("set", ZonedDateTime.now().format(DateTimeFormatter.ISO_INSTANT));
                updateDoc.addField("indexed", timestamp);
            }

            solrClient.add(collection, updateDoc, MAX_TIME_WITHOUT_COMMIT_MS);
            if (explicitCommit) {
                commit();
            }
        } catch (IOException | SolrServerException e) {
            //e.printStackTrace();
            throw new RuntimeException((e));
        }
    }

    public void addSingleFieldValueForMultipleObjects(List<String> pids, String fieldName, Object value, boolean indexTime, boolean explicitCommit) {
        if (pids.isEmpty()) {
            return;
        }
        try {
            List<SolrInputDocument> inputDocs = new ArrayList<>();
            for (String pid : pids) {
                SolrInputDocument inputDoc = new SolrInputDocument();
                if (IterationUtils.useCompositeId()) {
                    inputDoc.addField("compositeId", getCompositeIdByPid(pid));
                }
                inputDoc.addField("pid", pid);

                Map<String, Object> updateData = new HashMap<>();
                updateData.put("add-distinct", value == null ? null : value.toString());
                inputDoc.addField(fieldName, updateData);

                if (indexTime) {
                    Map<String, Object> timestamp = new HashMap<>();
                    timestamp.put("set", ZonedDateTime.now().format(DateTimeFormatter.ISO_INSTANT));
                    inputDoc.addField("indexed", timestamp);
                }
                inputDocs.add(inputDoc);

            }
            solrClient.add(collection, inputDocs, MAX_TIME_WITHOUT_COMMIT_MS);
            if (explicitCommit) {
                commit();
            }
        } catch (IOException | SolrServerException e) {
            //e.printStackTrace();
            throw new RuntimeException((e));
        }

    }

    public void removeSingleFieldValueFromMultipleObjects(List<String> pids, String fieldName, Object value, boolean indexTime, boolean explicitCommit) {
        if (pids.isEmpty()) {
            return;
        }
        try {
            List<SolrInputDocument> inputDocs = new ArrayList<>();
            for (String pid : pids) {
                SolrInputDocument inputDoc = new SolrInputDocument();
                if (IterationUtils.useCompositeId()) {
                    inputDoc.addField("compositeId", getCompositeIdByPid(pid));
                }
                inputDoc.addField("pid", pid);

                Map<String, Object> updateData = new HashMap<>();
                updateData.put("removeregex", value == null ? null : value.toString()); //'remove' neodstranuje vsechny kopie stejne hodnoty (ac to tvrdi dokumentace)
                inputDoc.addField(fieldName, updateData);

                if (indexTime) {
                    Map<String, Object> timestamp = new HashMap<>();
                    timestamp.put("set", ZonedDateTime.now().format(DateTimeFormatter.ISO_INSTANT));
                    inputDoc.addField("indexed", timestamp);
                }

                inputDocs.add(inputDoc);
            }
            solrClient.add(collection, inputDocs, MAX_TIME_WITHOUT_COMMIT_MS);
            if (explicitCommit) {
                commit();
            }
        } catch (IOException | SolrServerException e) {
            //e.printStackTrace();
            throw new RuntimeException((e));
        }

    }
}
