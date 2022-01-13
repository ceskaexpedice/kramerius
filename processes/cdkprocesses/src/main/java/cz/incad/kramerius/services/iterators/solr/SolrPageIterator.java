package cz.incad.kramerius.services.iterators.solr;

import com.sun.jersey.api.client.Client;
import cz.incad.kramerius.services.iterators.ProcessIterationCallback;
import cz.incad.kramerius.services.iterators.ProcessIterationEndCallback;
import cz.incad.kramerius.services.utils.SolrUtils;
import cz.incad.kramerius.utils.StringUtils;
import cz.incad.kramerius.utils.XMLUtils;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.net.URLEncoder;

import static cz.incad.kramerius.services.iterators.utils.IterationUtils.pidsToIterationItem;
import static cz.incad.kramerius.services.utils.SolrUtils.*;
import static cz.incad.kramerius.services.iterators.utils.IterationUtils.*;

/**
 *  Iteration by paging (must be combined with filter query)
 */
public class SolrPageIterator extends AbstractSolrIterator{

    public SolrPageIterator(String address, String masterQuery, String filterQuery, String endpoint, String id, String sorting,int rows ) {
        super(address, masterQuery, filterQuery, endpoint, id, sorting, rows);
    }

    public SolrPageIterator(String address, String masterQuery, String filterQuery, String endpoint, String id, String sorting, int rows, String user, String pass) {
        super(address, masterQuery, filterQuery, endpoint, id, sorting, rows, user, pass);
    }

    public static Element paginationQuery(Client client, String url, String mq, String offset, int rows, String filterQuery, String endpoint, String user, String pass) throws IOException, SAXException, ParserConfigurationException {
        String fullQuery = null;
        if (StringUtils.isAnyString(filterQuery)) {
            fullQuery = String.format("?q=%s&start=%s&rows=%d&fq=%s&fl=PID",mq,offset, rows, URLEncoder.encode(filterQuery,"UTF-8"));
        } else {
            fullQuery = String.format("?q=%s&start=%s&rows=%d&fl=PID",mq,offset, rows);
        }
        String query = endpoint+ fullQuery+"&wt=xml";
        return SolrUtils.executeQuery(client, url, query, user, pass);
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
    public void iterate(Client client, ProcessIterationCallback iterationCallback, ProcessIterationEndCallback endCallback) {
        try {
            int offset = 0;
            int numberOfResult = Integer.MAX_VALUE;
            do {
                Element element =  paginationQuery( client, address,masterQuery,  ""+offset, rows, filterQuery, endpoint, this.user, this.pass);
                if (numberOfResult == Integer.MAX_VALUE) {
                    numberOfResult = findNumberOfResults(element);
                }
                iterationCallback.call(pidsToIterationItem(this.address, findAllPids(element)));
                offset += rows;
            }while(offset < numberOfResult);
            // callback after iteration
            endCallback.end();
        } catch (ParserConfigurationException | SAXException | IOException e) {
            throw new RuntimeException(e);
        }

    }
}
