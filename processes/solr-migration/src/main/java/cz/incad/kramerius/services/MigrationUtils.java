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
    private static final String SOLR_MIGRATION_FQUERY_KEY = ".migration.solr.fqquery";

    private static final String SOLR_MIGRATION_FIELD_LIST_KEY = ".migration.solr.fieldlist";
    private static final String SOLR_MIGRATION_SORT_FIELD_KEY = ".migration.solr.sort";

    private static final String SOLR_MIGRATION_ROWS_KEY = ".migration.solr.rows";

    private static final String SOLR_MIGRATION_THREAD_KEY = ".migration.threads";
    private static final String SOLR_MIGRATION_BATCHSIZE_KEY = ".migration.solr.batchsize";

    private static final String EXECUTOR_WAIT_TIMEOUT =".migration.executortimeout";

    private static final String SOLR_MIGRATION_BUIDLD_COMPOSITE = ".migration.build.composite";


    public static final String DEFAULT_FIELDLIST = "*";


    public static final int DEFAULT_NUMBER_OF_ROWS = 100;
    public static final int DEFAULT_NUMBER_OF_THREADS = 2;
    public static final int DEFAULT_BATCHSIZE = 10;
    public static final int START = 0;

    public static final Logger LOGGER = Logger.getLogger(MigrationUtils.class.getName());
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
                LOGGER.log(Level.SEVERE, "status "+resp.getStatus() +" error message "+new String(bos.toByteArray(), "UTF-8"));
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

    public static boolean configuredPagination() {
        boolean useCursor = KConfiguration.getInstance().getConfiguration().getBoolean("solr.migration.pagination", false);
        LOGGER.info("Use pagination "+useCursor);
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




    public static String filterQuery() {
        String fq = KConfiguration.getInstance().getConfiguration().getString(SOLR_MIGRATION_FQUERY_KEY, "");
        return  fq;
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
        String query =  IterationUtils.SELECT_ENDPOINT + "?q=pid:(" + URLEncoder.encode(reduce, "UTF-8") + ")&fl=" + URLEncoder.encode(fieldlist, "UTF-8");
        return IterationUtils.executeQuery(client, url, query);
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


}
