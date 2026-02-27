package cz.incad.kramerius.rest.apiNew.monitoring.impl;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.sun.jersey.api.client.Client;
import cz.incad.kramerius.rest.api.exceptions.BadRequestException;
import cz.incad.kramerius.rest.apiNew.exceptions.InternalErrorException;
import cz.inovatika.monitoring.APICallMonitor;
import cz.inovatika.monitoring.ApiCallEvent;
import cz.incad.kramerius.utils.IPAddressUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;
import cz.incad.kramerius.utils.solr.SolrUpdateUtils;
import org.apache.hc.client5.http.HttpResponseException;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.json.JSONException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.inject.Named;
import jakarta.servlet.http.HttpServletRequest;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static org.apache.http.HttpStatus.SC_BAD_REQUEST;

public class SolrAPICallMonitor implements APICallMonitor  {

    public static final Logger LOGGER = Logger.getLogger(SolrAPICallMonitor.class.getName());

    private static final String SOLR_POINT = "api.monitor.point";

    private Client client;
    private DocumentBuilderFactory documentBuilderFactory;

    @Inject
    Provider<HttpServletRequest> requestProvider;

    @javax.inject.Inject
    @Named("solr-client")
    javax.inject.Provider<CloseableHttpClient> provider;

    @javax.inject.Inject
    @Named("forward-client")
    javax.inject.Provider<PoolingHttpClientConnectionManager> apachePoolManager;


    public SolrAPICallMonitor() {
        this.client = Client.create();
        this.documentBuilderFactory = DocumentBuilderFactory.newInstance();

        client.setReadTimeout(Integer.parseInt(KConfiguration.getInstance().getProperty("http.timeout", "10000")));
        client.setConnectTimeout(Integer.parseInt(KConfiguration.getInstance().getProperty("http.timeout", "10000")));
    }



    @Override
    public ApiCallEvent start( String resource, String endpoint, String queryString, String httpMethod) {
        List<Object> labels = KConfiguration.getInstance().getConfiguration().getList("labels");
        ApiCallEvent event = new ApiCallEvent(resource, endpoint, queryString, httpMethod);
        event.setLabels(labels.stream().map(Object::toString).collect(Collectors.toList()));
        if (this.requestProvider != null) {
            HttpServletRequest req = this.requestProvider.get();
            event.setIpAddress(IPAddressUtils.getRemoteAddress(req));
        }
        return event;
    }

    @Override
    public ApiCallEvent start(String resource,String endpoint, String queryString, String httpMethod, String pid) {
        List<Object> labels = KConfiguration.getInstance().getConfiguration().getList("labels");
        ApiCallEvent event = new ApiCallEvent(resource, endpoint, queryString, httpMethod, pid);
        event.setLabels(labels.stream().map(Object::toString).collect(Collectors.toList()));
        if (this.requestProvider != null) {
            HttpServletRequest req = this.requestProvider.get();
            event.setIpAddress(IPAddressUtils.getRemoteAddress(req));
        }
        return event;
    }

    @Override
    public void commit() {
        try {
            String apiMonitor = KConfiguration.getInstance().getProperty(SOLR_POINT,"http://localhost:8983/solr/monitor");
            String updateUrl = apiMonitor+(apiMonitor.endsWith("/") ?  "" : "/")+"update";
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder document = factory.newDocumentBuilder();
            Document commitDoc = document.newDocument();
            Element add = commitDoc.createElement("commit");
            commitDoc.appendChild(add);
            SolrUpdateUtils.sendToDest(this.client, commitDoc, updateUrl);
        } catch (ParserConfigurationException e) {
            LOGGER.log(Level.SEVERE,e.getMessage(),e);
        }
    }

    @Override
    public void stop(ApiCallEvent event, String userId) {
        // threshold
        event.setEndTime(System.currentTimeMillis());
        event.setUserId(userId);

        int threshold = KConfiguration.getInstance().getConfiguration().getInt("api.monitor.threshold", 1000);
        if (event.getDuration() > threshold) {


            String apiMonitor = KConfiguration.getInstance().getProperty(SOLR_POINT,"http://localhost:8983/solr/monitor");
            String updateUrl = apiMonitor+(apiMonitor.endsWith("/") ?  "" : "/")+"update";
            try {
                Document apiEvent = event.toSolrDocument(this.documentBuilderFactory);
                SolrUpdateUtils.sendToDest(this.client, apiEvent, updateUrl);
            } catch (ParserConfigurationException e) {
                LOGGER.log(Level.SEVERE,e.getMessage(),e);
            } catch (TransformerException e) {
                LOGGER.log(Level.SEVERE,e.getMessage(),e);
            }
        }
    }


    @Override
    public String apiMonitorRequestJson(String solrQuery) {
        try {
            String apiMonitor = KConfiguration.getInstance().getProperty(SOLR_POINT,"http://localhost:8983/solr/monitor");
            LOGGER.info(String.format("Endpoint, querystring = %s, %s", apiMonitor,solrQuery));
            return cz.incad.kramerius.utils.solr.SolrUtils.requestWithSelectReturningString(this.provider.get(), apiMonitor, solrQuery, "json", null);
        } catch (HttpResponseException e) {
            if (e.getStatusCode() == SC_BAD_REQUEST) {
                LOGGER.log(Level.INFO, "SOLR Bad Request: " + solrQuery);
                throw new BadRequestException(e.getMessage());
            } else {
                LOGGER.log(Level.INFO, "SOLR Exception: " + solrQuery);
                LOGGER.log(Level.INFO, e.getMessage(), e);
                throw new InternalErrorException(e.getMessage());
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new InternalErrorException(e.getMessage());
        } catch (JSONException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new InternalErrorException(e.getMessage());
        }
    }

    @Override
    public String apiMonitorRequestXML(String solrQuery) {
        try {
            String apiMonitor = KConfiguration.getInstance().getProperty(SOLR_POINT,"http://localhost:8983/solr/monitor");
            //String selectEndpoint = apiMonitor+(apiMonitor.endsWith("/") ?  "" : "/")+"select";
            LOGGER.info(String.format("Endpoint, querystring = %s, %s", apiMonitor,solrQuery));
            return cz.incad.kramerius.utils.solr.SolrUtils.requestWithSelectReturningString(this.provider.get(), apiMonitor, solrQuery, "xml", null);
        } catch (HttpResponseException e) {
            if (e.getStatusCode() == SC_BAD_REQUEST) {
                LOGGER.log(Level.INFO, "SOLR Bad Request: " + solrQuery);
                throw new BadRequestException(e.getMessage());
            } else {
                LOGGER.log(Level.INFO, e.getMessage(), e);
                throw new InternalErrorException(e.getMessage());
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new InternalErrorException(e.getMessage());
        }
    }


}
