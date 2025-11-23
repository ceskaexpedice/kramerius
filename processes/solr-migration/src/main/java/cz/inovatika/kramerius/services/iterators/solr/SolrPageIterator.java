package cz.inovatika.kramerius.services.iterators.solr;

import com.sun.jersey.api.client.Client;
import cz.inovatika.kramerius.services.config.ResponseHandlingConfig;
import cz.inovatika.kramerius.services.iterators.ProcessIterationCallback;
import cz.inovatika.kramerius.services.iterators.ProcessIterationEndCallback;
import cz.incad.kramerius.utils.StringUtils;
import cz.incad.kramerius.utils.XMLUtils;
import cz.inovatika.kramerius.services.iterators.utils.HTTPSolrUtils;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.stream.Collectors;


public class SolrPageIterator extends AbstractSolrIterator {

    public SolrPageIterator(String address, String masterQuery, String filterQuery, String endpoint, String id, String sorting, int rows, String[] fieldList, ResponseHandlingConfig responseHandlingConfig) {
        super(address, masterQuery, filterQuery, endpoint, id, sorting, rows, fieldList, responseHandlingConfig);
    }

    public SolrPageIterator(String address, String masterQuery, String filterQuery, String endpoint, String id, String sorting, int rows, ResponseHandlingConfig responseHandlingConfig) {
        super(address, masterQuery, filterQuery, endpoint, id, sorting, rows, responseHandlingConfig);
    }

    public static Element paginationJersey(Client client, String url, String mq, String offset, int rows, String filterQuery, String endpoint, String identifierField, String sorting, String[] fieldList, ResponseHandlingConfig responseHandlingConfig) throws IOException, SAXException, ParserConfigurationException {
        String query = parinationQuery(mq, offset, rows, filterQuery, endpoint, identifierField, sorting, fieldList);
        return HTTPSolrUtils.executeQueryJersey(client, url, query, responseHandlingConfig);
    }


    public static Element paginationApache(CloseableHttpClient client, String url, String mq, String offset, int rows, String filterQuery, String endpoint, String identifierField, String sorting, String[] fieldList) throws IOException, SAXException, ParserConfigurationException {
        String query = parinationQuery(mq, offset, rows, filterQuery, endpoint, identifierField, sorting,fieldList);
        return HTTPSolrUtils.executeQueryApache(client, url, query);
    }

    private static String parinationQuery(String mq, String offset, int rows, String filterQuery, String endpoint, String identifierField, String sorting, String[] fieldList) throws UnsupportedEncodingException {
        String fieldListParam = fieldList != null && fieldList.length > 0 ?  identifierField+","+ Arrays.stream(fieldList).collect(Collectors.joining(",")) : identifierField;
        String fullQuery = null;
        if (StringUtils.isAnyString(filterQuery)) {
            fullQuery = String.format("?q=%s&start=%s&rows=%d&fq=%s&fl=%s", mq, offset, rows, URLEncoder.encode(filterQuery,"UTF-8"), fieldListParam);
        } else {
            fullQuery = String.format("?q=%s&start=%s&rows=%d&fl=%s", mq, offset, rows, fieldListParam);
        }
        String query = endpoint + fullQuery+"&wt=xml";
        if (StringUtils.isAnyString(sorting)) {
           query = query+"&sort="+URLEncoder.encode(sorting,"UTF-8");
        }
        return query;
    }

    static int findNumberOfResults(Element elm) {
        Element result = XMLUtils.findElement(elm, new XMLUtils.ElementsFilter() {

            @Override
            public boolean acceptElement(Element element) {
                String nodeName = element.getNodeName();
                return nodeName.equals("result");
            }
        });
        //numFound
        String attribute = result.getAttribute("numFound");
        int numfound = Integer.parseInt(attribute);
        return numfound;
    }

    @Override
    public void iterate(CloseableHttpClient client, ProcessIterationCallback iterationCallback, ProcessIterationEndCallback endCallback) {
        try {
            int offset = 0;
            int numberOfResult = Integer.MAX_VALUE;
            do {
                Element element =  paginationApache( client, address,masterQuery,  ""+offset, rows, filterQuery, endpoint, id, this.sorting, this.fieldList);
                if (numberOfResult == Integer.MAX_VALUE) {
                    numberOfResult = findNumberOfResults(element);
                }
                //List<String> allPids = findAllPids(element);
                iterationCallback.call(HTTPSolrUtils.prepareIterationItems(element, this.address, this.id));
                offset += rows;
            }while(offset < numberOfResult);
            // callback after iteration
            endCallback.end();
        } catch (ParserConfigurationException | SAXException | IOException e) {
            throw new RuntimeException(e);
        }

    }



    @Override
    public void iterate(Client client, ProcessIterationCallback iterationCallback, ProcessIterationEndCallback endCallback) {
        try {
            int offset = 0;
            int numberOfResult = Integer.MAX_VALUE;
            do {
                Element element =  paginationJersey( client, address,masterQuery,  ""+offset, rows, filterQuery, endpoint, id, this.sorting, this.fieldList, this.responseHandlingConfig);
                if (numberOfResult == Integer.MAX_VALUE) {
                    numberOfResult = findNumberOfResults(element);
                }
                iterationCallback.call(HTTPSolrUtils.prepareIterationItems(element, this.address, this.id));
                offset += rows;
            }while(offset < numberOfResult);
            // callback after iteration
            endCallback.end();
        } catch (ParserConfigurationException | SAXException | IOException e) {
            throw new RuntimeException(e);
        }
    }
}
