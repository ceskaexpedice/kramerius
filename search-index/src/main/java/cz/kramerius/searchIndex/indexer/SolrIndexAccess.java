package cz.kramerius.searchIndex.indexer;

import cz.kramerius.searchIndex.repositoryAccess.nodes.RepositoryNode;
import cz.kramerius.searchIndex.repositoryAccess.nodes.RepositoryNodeManager;
import cz.kramerius.shared.Dom4jUtils;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpClientUtil;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.params.MapSolrParams;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static cz.kramerius.searchIndex.indexerProcess.Indexer.*;

public class SolrIndexAccess {

    private static final int MAX_TIME_WITHOUT_COMMIT_MS = 15000; //15 seconds
    private static final int CONNECTION_TIMEOUT = 10000;
    private static final int SOCKET_TIMEOUT = 60000;

    private final HttpSolrClient solrClient;
    private final String collection; //because solrClient is buggy and still requires explicit collection-name as a parameter for some operations even though it gets collection-name in the constructor

    public SolrIndexAccess(SolrConfig config) {
        System.setProperty("solr.cloud.client.stallTime", "119999");
        this.solrClient = config.login == null
                ? buildHttpSolrClientWithoutAuth(config.baseUrl, config.collection, config.useHttps)
                : buildHttpSolrClientWithAuth(config.baseUrl, config.collection, config.useHttps, config.login, config.password);
        this.collection = config.collection;
    }

    private HttpSolrClient buildHttpSolrClientWithAuth(String baseUrl, String collection, boolean useHttps, String login, String password) {
        CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(login, password);
        credentialsProvider.setCredentials(AuthScope.ANY, credentials);

        ModifiableSolrParams params = new ModifiableSolrParams();
        params.set(HttpClientUtil.PROP_BASIC_AUTH_USER, login);
        params.set(HttpClientUtil.PROP_BASIC_AUTH_PASS, password);
        params.set(HttpClientUtil.PROP_CONNECTION_TIMEOUT, CONNECTION_TIMEOUT);
        params.set(HttpClientUtil.PROP_SO_TIMEOUT, SOCKET_TIMEOUT);
        //params.set(HttpClientUtil.PROP_ALLOW_COMPRESSION, true);
        //params.set(HttpClientUtil.PROP_FOLLOW_REDIRECTS, true);
        //params.set(HttpClientUtil.PROP_MAX_CONNECTIONS, 22345);
        //params.set(HttpClientUtil.PROP_MAX_CONNECTIONS_PER_HOST, 32345);
        //params.set(HttpClientUtil.PROP_USE_RETRY, false);

        //@see https://stackoverflow.com/questions/36822522/solr-nonrepeatablerequestexception-in-save-action
        HttpClientUtil.addRequestInterceptor(new PreemptiveAuthInterceptor());
        HttpClient httpClient = HttpClientUtil.createClient(params);

        return new HttpSolrClient.Builder(buildUrl(baseUrl, collection, useHttps))
                .withHttpClient(httpClient)
                /*.withConnectionTimeout(CONNECTION_TIMEOUT)
                .withSocketTimeout(SOCKET_TIMEOUT)*/
                .build();
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
        List<SolrInputDocument> solrDoc = extractSolrInputDocuments(in);
        UpdateResponse addResponse = null;
        for (SolrInputDocument doc : solrDoc) {
            //there will always only be on ADD anyway
            addResponse = solrClient.add(collection, doc, MAX_TIME_WITHOUT_COMMIT_MS);
        }
        if (explicitCommit) {
            solrClient.commit(collection);
        }
        return addResponse;
    }

    private List<SolrInputDocument> extractSolrInputDocuments(InputStream in) throws DocumentException {
        ArrayList<SolrInputDocument> solrDocList = new ArrayList<>();
        SAXReader reader = new SAXReader();
        Document doc = reader.read(in);
        List<Node> docEls = Dom4jUtils.buildXpath("add/doc").selectNodes(doc);
        for (Node docEl : docEls) {
            SolrInputDocument solrInputDoc = new SolrInputDocument();
            List<Node> fieldEls = docEl.selectNodes("field");
            for (Node field : fieldEls) {
                Element fieldEl = (Element) field;
                String name = fieldEl.attribute("name").getValue();
                String value = field.getStringValue().trim();
                solrInputDoc.addField(name, value);
            }
            solrDocList.add(solrInputDoc);
        }
        return solrDocList;
    }

    public UpdateResponse deleteById(String id) throws IOException, SolrServerException {
        //System.out.println("deleting " + id);
        if (useCompositeId()){
            UpdateResponse deleteResponse = solrClient.deleteByQuery(collection, "pid:"+id.replace(":", "\\:"));
        }else {
            UpdateResponse deleteResponse = solrClient.deleteById(collection, id);
        }
        //System.out.println("delete response: " + deleteResponse);
        UpdateResponse commitResponse = solrClient.commit(collection);
        //System.out.println("commit response: " + commitResponse);
        return null;
    }

    public UpdateResponse deleteByIds(List<String> ids) throws IOException, SolrServerException {
        //System.out.println("deleting " + id);
        for (String id : ids) {
            if (useCompositeId()){
                UpdateResponse deleteResponse = solrClient.deleteByQuery(collection, "pid:"+id.replace(":", "\\:"));
            }else {
                UpdateResponse deleteResponse = solrClient.deleteById(collection, id);
            }
            //System.out.println("delete response: " + deleteResponse);
        }
        UpdateResponse commitResponse = solrClient.commit(collection);
        //System.out.println("commit response: " + commitResponse);
        return null;
    }

    public UpdateResponse deleteAll() throws IOException, SolrServerException {
        //System.out.println("deleting all");
        UpdateResponse deleteResponse = solrClient.deleteByQuery(collection, "*");
        //System.out.println("delete response: " + deleteResponse);
        UpdateResponse commitResponse = solrClient.commit(collection);
        //System.out.println("commit response: " + commitResponse);
        return null;
    }

    public SolrDocumentList searchInAllFields(String query) throws IOException, SolrServerException {
        Map<String, String> queryParamMap = new HashMap<>();
        //queryParamMap.put("q", "title:*");
        //queryParamMap.put("q", "*");
        queryParamMap.put("q", query);
        //queryParamMap.put("fl", "id, title");
        MapSolrParams queryParams = new MapSolrParams(queryParamMap);

        QueryResponse response = solrClient.query(collection, queryParams);
        return response.getResults();
    }

    public SolrDocumentList searchInAllFields(String query, Long start, Integer rows, String outputFieldList) throws IOException, SolrServerException {
        Map<String, String> queryParamMap = new HashMap<>();
        queryParamMap.put("q", query);
        queryParamMap.put("start", Long.toString(start));
        if (rows != null) {
            queryParamMap.put("rows", Integer.toString(rows));
        }
        if (outputFieldList != null) {
            queryParamMap.put("fl", outputFieldList);
        }
        MapSolrParams queryParams = new MapSolrParams(queryParamMap);
        QueryResponse response = solrClient.query(collection, queryParams);
        return response.getResults();
    }

    public void commit() throws IOException, SolrServerException {
        solrClient.commit(collection);
    }

    public void setSingleFieldValue(String pid, RepositoryNode repositoryNode, String fieldName, Object value, boolean explicitCommit) {
        try {
            SolrInputDocument updateDoc = new SolrInputDocument();
            ensureCompositeId(updateDoc,repositoryNode, pid);
            updateDoc.addField("pid", pid);

            Map<String, Object> updateData = new HashMap<>();
            updateData.put("set", value == null ? null : value.toString());
            updateDoc.addField(fieldName, updateData);
            solrClient.add(collection, updateDoc, MAX_TIME_WITHOUT_COMMIT_MS);
            if (explicitCommit) {
                solrClient.commit(collection);
            }
        } catch (IOException | SolrServerException e) {
            e.printStackTrace();
            throw new RuntimeException((e));
        }
    }

    public void addSingleFieldValueForMultipleObjects(List<String> pids, String fieldName, Object value, boolean explicitCommit) {
        if (!pids.isEmpty()) {
            try {
                List<SolrInputDocument> inputDocs = new ArrayList<>();
                for (String pid : pids) {
                    SolrInputDocument inputDoc = new SolrInputDocument();
                    inputDoc.addField("pid", pid);
                    Map<String, Object> updateData = new HashMap<>();
                    updateData.put("add-distinct", value == null ? null : value.toString());
                    inputDoc.addField(fieldName, updateData);
                    inputDocs.add(inputDoc);
                }
                solrClient.add(collection, inputDocs, MAX_TIME_WITHOUT_COMMIT_MS);
                if (explicitCommit) {
                    solrClient.commit(collection);
                }
            } catch (IOException | SolrServerException e) {
                e.printStackTrace();
                throw new RuntimeException((e));
            }
        }
    }

    public void removeSingleFieldValueFromMultipleObjects(List<String> pids, String fieldName, Object value, boolean explicitCommit) {
        if (!pids.isEmpty()) {
            try {
                List<SolrInputDocument> inputDocs = new ArrayList<>();
                for (String pid : pids) {
                    SolrInputDocument inputDoc = new SolrInputDocument();
                    inputDoc.addField("pid", pid);
                    Map<String, Object> updateData = new HashMap<>();
                    updateData.put("removeregex", value == null ? null : value.toString()); //'remove' neodstranuje vsechny kopie stejne hodnoty (ac to tvrdi dokumentace)
                    inputDoc.addField(fieldName, updateData);
                    inputDocs.add(inputDoc);
                }
                solrClient.add(collection, inputDocs, MAX_TIME_WITHOUT_COMMIT_MS);
                if (explicitCommit) {
                    solrClient.commit(collection);
                }
            } catch (IOException | SolrServerException e) {
                e.printStackTrace();
                throw new RuntimeException((e));
            }
        }
    }
}
