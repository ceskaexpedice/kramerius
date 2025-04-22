package cz.incad.kramerius.services.iterators.solr;

import com.sun.jersey.api.client.Client;
import cz.incad.kramerius.services.iterators.ProcessIterationCallback;
import cz.incad.kramerius.services.iterators.ProcessIterationEndCallback;
import cz.incad.kramerius.services.utils.KubernetesSolrUtils;
import cz.incad.kramerius.utils.StringUtils;
import cz.incad.kramerius.utils.XMLUtils;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.stream.Collectors;

import static cz.incad.kramerius.services.utils.KubernetesSolrUtils.*;
import static cz.incad.kramerius.services.iterators.utils.IterationUtils.*;

public class SolrCursorIterator extends AbstractSolrIterator{


    public SolrCursorIterator(String address, String masterQuery, String filterQuery, String endpoint, String id, String sorting,int rows ) {
        super(address, masterQuery, filterQuery, endpoint, id, sorting, rows);
    }

    public SolrCursorIterator(String address, String masterQuery, String filterQuery, String endpoint, String id, String sorting, int rows, String[] fields) {
        super(address, masterQuery, filterQuery, endpoint, id, sorting, rows, fields);
    }

    @NotNull
    private static String pidCursorQuery(String mq, String cursor, int rows, String fq, String endpoint, String identifierField, String sorting, String...flFields)  {
        try {
            String flParam = flFields != null && flFields.length > 0 ? identifierField+","+Arrays.stream(flFields).collect(Collectors.joining(",")) :  identifierField;
            String fullQuery = null;
            if (StringUtils.isAnyString(fq)) {
                fullQuery = "?q="+ mq + (cursor != null ? String.format("&rows=%d&cursorMark=%s", rows, cursor) : String.format("&rows=%d&cursorMark=*", rows))+"&sort=" + URLEncoder.encode(sorting, "UTF-8")+"&fl="+ flParam +"&fq=" + URLEncoder.encode(fq,"UTF-8");
            } else {
                fullQuery = "?q="+ mq + (cursor != null ? String.format("&rows=%d&cursorMark=%s", rows, cursor) : String.format("&rows=%d&cursorMark=*", rows))+"&sort=" + URLEncoder.encode(sorting, "UTF-8")+"&fl="+ flParam;
            }

            String query = endpoint + fullQuery+"&wt=xml";
            return query;
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }


    public static String findCursorMark(Element elm) {
        Element element = XMLUtils.findElement(elm, new XMLUtils.ElementsFilter() {
            @Override
            public boolean acceptElement(Element element) {
                String nodeName = element.getNodeName();
                boolean nextCursorMark = element.hasAttribute("name") && element.getAttribute("name").equals("nextCursorMark");
                return nodeName.equals("str") && nextCursorMark;
            }
        });
        return element != null ? element.getTextContent() : null;
    }

    public static String findQueryCursorMark(Element elm) {
        Element queryParams = XMLUtils.findElement(elm, new XMLUtils.ElementsFilter() {
            @Override
            public boolean acceptElement(Element element) {
                String nodeName = element.getNodeName();
                String paramName = element.getAttribute("name");
                return nodeName.equals("lst") && paramName.equals("params");

            }
        });
        if (queryParams != null) {
            Element element = XMLUtils.findElement(elm, new XMLUtils.ElementsFilter() {
                @Override
                public boolean acceptElement(Element element) {
                    String nodeName = element.getNodeName();
                    boolean nextCursorMark = element.hasAttribute("name") && element.getAttribute("name").equals("cursorMark");
                    return nodeName.equals("str") && nextCursorMark;
                }
            });
            return element != null ? element.getTextContent() : null;
        }
        return null;
    }

    @Override
    public void iterate(Client client, ProcessIterationCallback iterationCallback, ProcessIterationEndCallback endCallback) {
        try {
            String cursorMark = null;
            String queryCursorMark = null;
            do {
                String query = pidCursorQuery(masterQuery, cursorMark, rows, filterQuery, endpoint, id, sorting);
                Element element = executeQueryJersey(client, address, query);

                cursorMark = findCursorMark(element);
                queryCursorMark = findQueryCursorMark(element);
                iterationCallback.call(findAllPids(element, this.address, this.id));
            } while((cursorMark != null && queryCursorMark != null) && !cursorMark.equals(queryCursorMark));
            // callback after iteration
            endCallback.end();
        } catch (ParserConfigurationException  |  IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void iterate(CloseableHttpClient client, ProcessIterationCallback iterationCallback, ProcessIterationEndCallback endCallback) {
        try {
            String cursorMark = null;
            String queryCursorMark = null;
            do {
                String query = pidCursorQuery(masterQuery, cursorMark, rows, filterQuery, endpoint, id, sorting);
                Element element = executeQueryApache(client, address, query);

                cursorMark = findCursorMark(element);
                queryCursorMark = findQueryCursorMark(element);
                iterationCallback.call( findAllPids(element, this.address, this.id));
            } while((cursorMark != null && queryCursorMark != null) && !cursorMark.equals(queryCursorMark));
            // callback after iteration
            endCallback.end();
        } catch (ParserConfigurationException  | IOException e) {
            throw new RuntimeException(e);
        }

    }


}
