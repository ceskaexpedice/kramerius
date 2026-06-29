package cz.inovatika.kramerius.services.iterators.solr;

import cz.inovatika.kramerius.services.config.ResponseHandlingConfig;
import cz.inovatika.kramerius.services.iterators.ApacheHTTPRequestEnricher;
import cz.inovatika.kramerius.services.iterators.MigrationIterator;
import cz.inovatika.kramerius.services.iterators.utils.HTTPSolrUtils;
import cz.incad.kramerius.utils.StringUtils;
import cz.incad.kramerius.utils.XMLUtils;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.w3c.dom.Element;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.logging.Logger;

public abstract class AbstractSolrIterator implements MigrationIterator {

    private static final Logger LOGGER = Logger.getLogger(AbstractSolrIterator.class.getName());

    protected String address;
    protected String masterQuery;
    protected int rows;
    protected String filterQuery;
    protected String endpoint;
    protected String id;
    protected String sorting;
    protected String[] fieldList;

    protected ResponseHandlingConfig responseHandlingConfig;
    protected ApacheHTTPRequestEnricher enricher;


    public AbstractSolrIterator(String address, String masterQuery, String filterQuery, String endpoint, String id, String sorting, int rows, String[] fieldList, ResponseHandlingConfig responseHandlingConfig, ApacheHTTPRequestEnricher enricher) {
        this.id = id;
        this.rows = rows;
        this.filterQuery = filterQuery;
        this.sorting = sorting;
        this.endpoint = endpoint;
        this.address = address;
        this.masterQuery = masterQuery;
        this.fieldList = fieldList;
        this.responseHandlingConfig = responseHandlingConfig;
        this.enricher = enricher;
    }

    public AbstractSolrIterator(String address, String masterQuery, String filterQuery, String endpoint, String id, String sorting, int rows, ResponseHandlingConfig responseHandlingConfig, ApacheHTTPRequestEnricher enricher) {
        this( address, masterQuery, filterQuery, endpoint, id, sorting, rows, new String[]{}, responseHandlingConfig,enricher);
    }


    public String getAddress() {
        return address;
    }

    public int getRows() {
        return rows;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public String getId() {
        return id;
    }

    public String getMasterQuery() {
        return masterQuery;
    }

    public String getFilterQuery() {
        return filterQuery;
    }

    public String getSorting() {
        return sorting;
    }

    public ApacheHTTPRequestEnricher getEnricher() {
        return enricher;
    }

    public String[] getFieldList() {
        return fieldList;
    }

    @Override
    public long estimateTotalDocuments(CloseableHttpClient client) {
        try {
            String query = totalQuery();
            Element response = HTTPSolrUtils.executeQueryApache(client, this.enricher, this.address, query);
            Element result = XMLUtils.findElement(response, element -> "result".equals(element.getNodeName()));
            if (result == null) {
                return -1L;
            }
            String numFound = result.getAttribute("numFound");
            if (numFound == null || numFound.trim().isEmpty()) {
                return -1L;
            }
            return Long.parseLong(numFound);
        } catch (Exception e) {
            LOGGER.fine(String.format("Unable to estimate total documents for migration progress: %s", e.getMessage()));
            return -1L;
        }
    }

    private String totalQuery() throws UnsupportedEncodingException {
        StringBuilder query = new StringBuilder(endpoint)
                .append("?q=").append(masterQuery)
                .append("&rows=0");
        if (StringUtils.isAnyString(filterQuery)) {
            query.append("&fq=").append(URLEncoder.encode(filterQuery, "UTF-8"));
        }
        query.append("&wt=xml");
        return query.toString();
    }
}
