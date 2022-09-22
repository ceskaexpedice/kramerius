package cz.incad.kramerius.services.iterators.solr;

import com.sun.jersey.api.client.Client;
import cz.incad.kramerius.services.iterators.ProcessIterationCallback;
import cz.incad.kramerius.services.iterators.ProcessIterationEndCallback;
import cz.incad.kramerius.services.iterators.timestamps.TimestampStore;
import cz.incad.kramerius.services.utils.SolrUtils;
import cz.incad.kramerius.utils.StringUtils;
import cz.incad.kramerius.utils.XMLUtils;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.net.URLEncoder;

import static cz.incad.kramerius.services.utils.SolrUtils.*;
import static cz.incad.kramerius.services.iterators.utils.IterationUtils.*;

public class SolrCursorIterator extends AbstractSolrIterator{


    public SolrCursorIterator(TimestampStore tStore,String address, String masterQuery, String filterQuery, String endpoint, String id, String sorting,int rows ) {
        super(tStore,address, masterQuery, filterQuery, endpoint, id, sorting, rows);
    }

    public SolrCursorIterator(TimestampStore tStore,String address, String masterQuery, String filterQuery, String endpoint, String id, String sorting, int rows, String user, String pass) {
        super(tStore,address, masterQuery, filterQuery, endpoint, id, sorting, rows, user, pass);
    }

    static Element pidsCursorQuery(Client client, String url, String mq, String cursor, int rows, String fq, String endpoint, String identifierField, String sorting, String user, String pass)  throws ParserConfigurationException, SAXException, IOException {
        String fullQuery = null;
        if (StringUtils.isAnyString(fq)) {
            fullQuery = "?q="+mq + (cursor!= null ? String.format("&rows=%d&cursorMark=%s", rows, cursor) : String.format("&rows=%d&cursorMark=*", rows))+"&sort=" + URLEncoder.encode(sorting, "UTF-8")+"&fl="+identifierField+"&fq=" + URLEncoder.encode(fq,"UTF-8");
        } else {
            fullQuery = "?q="+mq + (cursor!= null ? String.format("&rows=%d&cursorMark=%s", rows, cursor) : String.format("&rows=%d&cursorMark=*", rows))+"&sort=" + URLEncoder.encode(sorting, "UTF-8")+"&fl="+identifierField;
        }
        String query = endpoint + fullQuery+"&wt=xml";
        
        return SolrUtils.executeQuery(client, url, query, user, pass);
    }


    
    
    static String findCursorMark(Element elm) {
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

    static String findQueryCursorMark(Element elm) {
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
                Element element = pidsCursorQuery(client, address, masterQuery, cursorMark, rows, filterQuery, endpoint, id, sorting, this.user, this.pass);
                cursorMark = findCursorMark(element);
                queryCursorMark = findQueryCursorMark(element);
                iterationCallback.call( pidsToIterationItem(this.address, findAllPids(element)));
            } while((cursorMark != null && queryCursorMark != null) && !cursorMark.equals(queryCursorMark));
            // callback after iteration
            endCallback.end();
        } catch (ParserConfigurationException  | SAXException | IOException e) {
            throw new RuntimeException(e);
        }
    }
}
