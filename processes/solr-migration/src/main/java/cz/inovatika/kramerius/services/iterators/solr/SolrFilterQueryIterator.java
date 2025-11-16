package cz.inovatika.kramerius.services.iterators.solr;

import com.sun.jersey.api.client.Client;
import cz.inovatika.kramerius.services.iterators.ProcessIterationCallback;
import cz.inovatika.kramerius.services.iterators.ProcessIterationEndCallback;
import cz.incad.kramerius.utils.StringUtils;
import cz.incad.kramerius.utils.XMLUtils;
import cz.inovatika.kramerius.services.iterators.utils.KubernetesSolrUtils;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.stream.Collectors;



/**
 * Solr filter iterator; sorts by PID and used last pid on the page
 * NOT USED
 */
public class SolrFilterQueryIterator extends AbstractSolrIterator {

    public static final String DEFAULT_SORT_FIELD = "PID asc";


    public SolrFilterQueryIterator(  String address, String masterQuery, String filterQuery, String endpoint, String id, String sorting,int rows ) {
        super( address, masterQuery, filterQuery, endpoint, id, sorting, rows);
    }


    public SolrFilterQueryIterator(String address, String masterQuery, String filterQuery, String endpoint, String id, String sorting, int rows, String[] fieldsParam) {
        super(address, masterQuery, filterQuery, endpoint, id, sorting, rows, fieldsParam);
    }

    public static Element pidsFilterApache(CloseableHttpClient client, String url, String mq, String lastPid, int rows, String fq, String endpoint,String[] fieldsParam)
            throws ParserConfigurationException, SAXException, IOException {
        String query = pidsFilterQuery(mq, lastPid, rows, fq, endpoint, fieldsParam);
        return KubernetesSolrUtils.executeQueryApache(client, url, query);
    }

    public static Element pidsFilterJersey(Client client, String url, String mq, String lastPid, int rows, String fq, String endpoint,String[] fieldsParam)
            throws ParserConfigurationException, SAXException, IOException {
        String query = pidsFilterQuery(mq, lastPid, rows, fq, endpoint, fieldsParam);
        return KubernetesSolrUtils.executeQueryJersey(client, url, query);
    }

    private static String pidsFilterQuery(String mq, String lastPid, int rows, String fq, String endpoint, String[] fieldsList) throws UnsupportedEncodingException {
        String fullQuery = null;
        if (StringUtils.isAnyString(fq)) {
            fullQuery = (lastPid != null ? String.format("&rows=%d&fq=PID:%s", rows, URLEncoder.encode("[\""+ lastPid +"\" TO *] AND "+ fq, "UTF-8")) : String.format("&rows=%d&fq=%s", rows, URLEncoder.encode(fq,"UTF-8")));
        } else {
            fullQuery = (lastPid != null ? String.format("&rows=%d&fq=PID:%s", rows, URLEncoder.encode("[\""+ lastPid +"\" TO *]", "UTF-8")) : String.format("&rows=%d", rows));
        }
        String query = endpoint + "?q="+ mq + fullQuery +"&sort=" + URLEncoder.encode(DEFAULT_SORT_FIELD, "UTF-8")+"&fl=PID&wt=xml";
        return query;
    }

    public static String findLastPid(Element elm) {
        Element result = XMLUtils.findElement(elm, new XMLUtils.ElementsFilter() {
            @Override
            public boolean acceptElement(Element element) {
                String nodeName = element.getNodeName();
                return nodeName.equals("result");
            }
        });
        if (result != null) {

            List<Element> elements = XMLUtils.getElements(result, new XMLUtils.ElementsFilter() {
                @Override
                public boolean acceptElement(Element element) {
                    String nodeName = element.getNodeName();
                    return nodeName.equals("doc");
                }
            });

            List<String> pids = elements.stream().map(item->{
                        Element str = XMLUtils.findElement(item, new XMLUtils.ElementsFilter() {
                                    @Override
                                    public boolean acceptElement(Element element) {
                                        return element.getNodeName().equals("str");
                                    }
                                }
                        );
                        return str.getTextContent();
                    }
            ).collect(Collectors.toList());

            return !pids.isEmpty() ? pids.get(pids.size() -1) : null;
        }

        return null;
    }

    @Override
    public void iterate(CloseableHttpClient client, ProcessIterationCallback iterationCallback, ProcessIterationEndCallback endCallback) {
        try {
            String lastPid = null;
            String previousPid = null;
            do {
                Element element = pidsFilterApache( client, address,masterQuery,  lastPid, rows, filterQuery, endpoint, this.fieldList);
                previousPid = lastPid;
                lastPid = findLastPid(element);
                iterationCallback.call(KubernetesSolrUtils.prepareIterationItems(element, this.address, this.id));
            }while(lastPid != null  && !lastPid.equals(previousPid));
            // callback after iteration
            endCallback.end();
        } catch (ParserConfigurationException | SAXException | IOException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public void iterate(Client client, ProcessIterationCallback iterationCallback, ProcessIterationEndCallback endCallback) {
        try {
            String lastPid = null;
            String previousPid = null;
            do {
                //    private static Element pidsFilterQuery(ConfigurationBase configuration, Client client, String url, String mq, String lastPid, int rows, String fq)
                Element element = pidsFilterJersey( client, address,masterQuery,  lastPid, rows, filterQuery, endpoint, fieldList);
                previousPid = lastPid;
                lastPid = findLastPid(element);
                iterationCallback.call(KubernetesSolrUtils.prepareIterationItems(element, this.address, this.id));
            }while(lastPid != null  && !lastPid.equals(previousPid));
            // callback after iteration
            endCallback.end();
        } catch (ParserConfigurationException | SAXException | IOException e) {
            throw new RuntimeException(e);
        }
    }
}
