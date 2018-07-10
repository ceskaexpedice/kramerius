package cz.incad.kramerius.services;

import java.io.*;
import java.net.URLEncoder;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.ws.rs.core.MediaType;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;

import cz.incad.kramerius.service.MigrateSolrIndexException;
import cz.incad.kramerius.utils.IOUtils;
import cz.incad.kramerius.utils.StringUtils;
import cz.incad.kramerius.utils.XMLUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;

public class MigrationUtils {

    private static final String DEST_SOLR_HOST = ".dest.solrHost";
    //private static final String SOLR_MIGRATION_QUERY_KEY = ".migration.solr.query";

    private static final String SOLR_MIGRATION_FIELD_LIST_KEY = ".migration.solr.fieldlist";
    private static final String SOLR_MIGRATION_SORT_FIELD_KEY = ".migration.solr.sort";

    private static final String SOLR_MIGRATION_ROWS_KEY = ".migration.solr.rows";

    private static final String SOLR_MIGRATION_THREAD_KEY = ".migration.threads";
    private static final String SOLR_MIGRATION_BATCHSIZE_KEY = ".migration.solr.batchsize";

    private static final String EXECUTOR_WAIT_TIMEOUT =".migration.executortimeout";

    private static final String SOLR_MIGRATION_BUIDLD_COMPOSITE = ".migration.build.composite";


    public static final String DEFAULT_FIELDLIST = "PID timestamp fedora.model document_type handle status created_date modified_date parent_model " +
            "parent_pid parent_pid parent_title root_model root_pid root_title text_ocr pages_count " +
            "datum_str datum rok datum_begin datum_end datum_page issn mdt ddt dostupnost keywords " +
            "geographic_names collection sec model_path pid_path rels_ext_index level dc.title title_sort " +
            "title_sort dc.creator dc.identifier language dc.description details facet_title browse_title browse_autor img_full_mime viewable " +
            "virtual location range mods.shelfLocator mods.physicalLocation text";

    public static final String DEFAULT_SORT_FIELD = "PID asc";


    public static final int DEFAULT_NUMBER_OF_ROWS = 500;
    public static final int DEFAULT_NUMBER_OF_THREADS = 2;
    public static final int DEFAULT_BATCHSIZE = 10;
    public static final int START = 0;

    public static final Logger LOGGER = Logger.getLogger(MigrationUtils.class.getName());
    public static final String SELECT_ENDPOINT = "select";
    public static final String SEARCH_ENDPOINT = "search";
    public static final int MAXIMUM_BATCH_SIZE = 100;

    private MigrationUtils() {
    }

    public static int counter = 0;

    public static void sendToDest(Client client, Document batchDoc) throws MigrateSolrIndexException {
        try {
            StringWriter writer = new StringWriter();
            String destSolr = confiugredDestinationServer();
            WebResource r = client.resource(destSolr);
            XMLUtils.print(batchDoc, writer);
            ClientResponse resp = r.accept(MediaType.TEXT_XML).type(MediaType.TEXT_XML).entity(writer.toString(), MediaType.TEXT_XML).post(ClientResponse.class);
            if (resp.getStatus() != ClientResponse.Status.OK.getStatusCode()) {
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                InputStream entityInputStream = resp.getEntityInputStream();
                IOUtils.copyStreams(entityInputStream, bos);
            }
        } catch (UniformInterfaceException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new MigrateSolrIndexException(e);
        } catch (ClientHandlerException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new MigrateSolrIndexException(e);
        } catch (TransformerException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new MigrateSolrIndexException(e);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new MigrateSolrIndexException(e);
        }
    }


    public static void commit(Client client, String destServer) throws MigrateSolrIndexException {
        try {
            String destSolr = destServer + "?commit=true";
            WebResource r = client.resource(destSolr);
            Document document = XMLUtils.crateDocument("add");
            StringWriter strWriter = new StringWriter();
            XMLUtils.print(document, strWriter);
            String t = r.accept(MediaType.TEXT_XML).type(MediaType.TEXT_XML).entity(strWriter.toString(), MediaType.TEXT_XML).post(String.class);
        } catch (UniformInterfaceException e) {
            throw new MigrateSolrIndexException(e);
        } catch (ClientHandlerException e) {
            throw new MigrateSolrIndexException(e);
        } catch (ParserConfigurationException e) {
            throw new MigrateSolrIndexException(e);
        } catch (TransformerException e) {
            throw new MigrateSolrIndexException(e);
        }
    }


    /**
     * REturns configured destination server
     *
     * @return
     * @throws MigrateSolrIndexException
     */
    public static int configuredRowsSize() throws MigrateSolrIndexException {
        int rows = KConfiguration.getInstance().getConfiguration().getInt(SOLR_MIGRATION_ROWS_KEY, DEFAULT_NUMBER_OF_ROWS);
        return rows;
    }

    public static int executorWaitTimeout() {
        int timeout = KConfiguration.getInstance().getConfiguration().getInt(EXECUTOR_WAIT_TIMEOUT, 20);
        return  timeout;
    }


    /**
     * REturns configured destination server
     *
     * @return
     * @throws MigrateSolrIndexException
     */
    public static int configuredBatchSize() throws MigrateSolrIndexException {
        int batchSize = KConfiguration.getInstance().getConfiguration().getInt(SOLR_MIGRATION_BATCHSIZE_KEY, DEFAULT_BATCHSIZE);
        if (batchSize > MAXIMUM_BATCH_SIZE) {
            throw new MigrateSolrIndexException("Illegal state. Property  '.migration.solr.batchsize' exceeded the limit (100)");
        }
        return batchSize;
    }


    public static boolean configuredUseCursor() {
        boolean useCursor = KConfiguration.getInstance().getConfiguration().getBoolean("solr.migration.usecursor", false);
        LOGGER.info("Use cursor "+useCursor);
        return useCursor;
    }

    /**
     * REturns configured destination server
     *
     * @return
     * @throws MigrateSolrIndexException
     */
    public static int configuredNumberOfThreads() throws MigrateSolrIndexException {
        return KConfiguration.getInstance().getConfiguration().getInt(SOLR_MIGRATION_THREAD_KEY, DEFAULT_NUMBER_OF_THREADS);
    }


    public static boolean configuredBuildCompositeId() throws MigrateSolrIndexException {
        return KConfiguration.getInstance().getConfiguration().getBoolean(SOLR_MIGRATION_BUIDLD_COMPOSITE, false);
    }


    /**
     * REturns configured destination server
     *
     * @return
     * @throws MigrateSolrIndexException
     */
    public static String confiugredDestinationServer() throws MigrateSolrIndexException {
        String targetDest = KConfiguration.getInstance().getConfiguration().getString(DEST_SOLR_HOST);
        String source = KConfiguration.getInstance().getSolrHost();
        if (targetDest == null || !StringUtils.isAnyString(targetDest)) {
            throw new MigrateSolrIndexException(String.format("missing property %s", DEST_SOLR_HOST));
        }
        if (targetDest.startsWith(source)) {
            throw new MigrateSolrIndexException(String.format("the same index problem %s %s", targetDest, source));
        }
        return targetDest;
    }

    /**
     * Returns source server
     *
     * @return
     * @throws MigrateSolrIndexException
     */
    public static String configuredSourceServer() throws MigrateSolrIndexException {
        String targetDest = KConfiguration.getInstance().getSolrHost();
        return targetDest;
    }





    public static String queryBaseURL() throws MigrateSolrIndexException {
        String solrQuery = KConfiguration.getInstance().getSolrHost();
        solrQuery += (solrQuery.endsWith("/") ? "" : "/") ;
        return solrQuery;
    }

    public static Element fetchDocuments(Client client, String url, List<String> pids)
            throws ParserConfigurationException, SAXException, IOException, MigrateSolrIndexException {
        String reduce = pids.stream().reduce("", (i, v) -> {
            if (!i.equals("")) {
                return i + " OR \"" + v+"\"";
            } else {
                return '"'+v+'"';
            }
        });
        String fieldlist = KConfiguration.getInstance().getConfiguration().getString(SOLR_MIGRATION_FIELD_LIST_KEY, DEFAULT_FIELDLIST);
        String query =  SELECT_ENDPOINT + "?q=PID:(" + URLEncoder.encode(reduce, "UTF-8") + ")&fl=" + URLEncoder.encode(fieldlist, "UTF-8");
        return executeQuery(client, url, query);
    }

    public static Element pidsQueryFilterQuery(Client client, String url, String lastPid)
            throws ParserConfigurationException, SAXException, IOException, MigrateSolrIndexException {
        int rows = MigrationUtils.configuredRowsSize();
        String query = SELECT_ENDPOINT + "?q=*:*" + (lastPid!= null ? String.format("&rows=%d&fq=PID:%s", rows, URLEncoder.encode("[\""+lastPid+"\" TO *]", "UTF-8")) : String.format("&rows=%d", rows))+"&sort=" + URLEncoder.encode(DEFAULT_SORT_FIELD, "UTF-8")+"&fl=PID";
        return executeQuery(client, url, query);
    }

    public static Element pidsCursorQuery(Client client, String url,  String cursor)  throws ParserConfigurationException, SAXException, IOException, MigrateSolrIndexException {
        int rows = MigrationUtils.configuredRowsSize();
        String query = SELECT_ENDPOINT + "?q=*:*" + (cursor!= null ? String.format("&rows=%d&cursorMark=%s", rows, cursor) : String.format("&rows=%d&cursorMark=*", rows))+"&sort=" + URLEncoder.encode(DEFAULT_SORT_FIELD, "UTF-8")+"&fl=PID";
        return executeQuery(client, url, query);
    }


    private static Element executeQuery(Client client, String url, String query) throws ParserConfigurationException, SAXException, IOException {
        LOGGER.info(String.format("[" + Thread.currentThread().getName() + "] processing %s", query));
        WebResource r = client.resource(url+(url.endsWith("/") ? "" : "/")+ query);
        String t = r.accept(MediaType.APPLICATION_XML).get(String.class);
        Document parseDocument = XMLUtils.parseDocument(new StringReader(t));
        return parseDocument.getDocumentElement();
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

    public static List<String> findAllPids(Element elm) {
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

            return elements.stream().map(item -> {
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

        } else return new ArrayList<>();
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


}
