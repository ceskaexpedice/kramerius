package cz.inovatika.kramerius.fedora.om.processingindex;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import cz.incad.kramerius.utils.XMLUtils;
import cz.inovatika.kramerius.fedora.FedoraNamespaces;
import cz.inovatika.kramerius.fedora.RepositoryException;
import cz.inovatika.kramerius.fedora.om.repository.RepositoryObject;
import cz.inovatika.kramerius.fedora.utils.FedoraUtils;
import cz.inovatika.kramerius.fedora.om.repository.impl.RepositoryUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.params.CursorMarkParams;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * This is the helper. It is dedicated for creating supporting index which should replace
 * resource index in the future.
 *
 * @author pstastny
 */
public class ProcessingIndexFeeder {
    
    public static enum TitleType {
        dc,mods;
    }

    public static final String DEFAULT_ITERATE_QUERY = "*:*";

    private static final String TYPE_RELATION = "relation";
    private static final String TYPE_DESC = "description";

    public static final Logger LOGGER = Logger.getLogger(ProcessingIndexFeeder.class.getName());

    private SolrClient solrClient;

    @Inject
    public ProcessingIndexFeeder(@Named("processingUpdate") SolrClient solrClient) {
        super();
        this.solrClient = solrClient;

    }

    // 1 ---------------------------------
    public Pair<Long, List<SolrDocument>> getPageSortedByTitle(String query, int rows, int pageIndex, List<String> fieldList) throws IOException, SolrServerException {
        SolrQuery solrQuery = new SolrQuery(query);
        int offset = pageIndex*rows;
        solrQuery.setStart(offset).setRows(rows);
        solrQuery.setSort("title", SolrQuery.ORDER.asc);
        if (fieldList != null &&  !fieldList.isEmpty()) {
            String[] fl = fieldList.toArray(new String[fieldList.size()]);
            solrQuery.setFields(fl);
        }
        QueryResponse response = this.solrClient.query(solrQuery);
        long numFound = response.getResults().getNumFound();
        List<SolrDocument> docs = new ArrayList<>();

        response.getResults().forEach((doc) -> {
            docs.add(doc);
        });
        return Pair.of(numFound, docs);
    }
    // 1 -------------------------------
    public Pair<Long, List<SolrDocument>> getPageSortedByTitle(String query, int rows, int offset) throws IOException, SolrServerException {
        return getPageSortedByTitle(query, rows, offset, new ArrayList<>());
    }
    // 2 -----------------------------------
    public void iterateSectionOfProcessingSortedByTitle(String query, boolean ascending, int offset, int limit, Consumer<SolrDocument> action) throws IOException, SolrServerException {
        iterateSectionOfProcessing(query, "dc.title", ascending ? SolrQuery.ORDER.asc : SolrQuery.ORDER.desc, offset, limit, action);
    }
    // 3 -----------------------------------------------
    public String iterateSectionOfProcessingSortedByTitleWithCursor(String query, boolean ascending, String cursor, int limit, Consumer<SolrDocument> action) throws IOException, SolrServerException {
        SolrQuery solrQuery = new SolrQuery(query);
        solrQuery.setParam("cursorMark", cursor).setRows(limit);
        List<SolrQuery.SortClause> sortClauses = new ArrayList<>();
        sortClauses.add(SolrQuery.SortClause.create("dc.title", ascending ? SolrQuery.ORDER.asc : SolrQuery.ORDER.desc));
        sortClauses.add(SolrQuery.SortClause.asc("pid")); //cursor query requires unique sorting field
        solrQuery.setSorts(sortClauses);
        QueryResponse response = this.solrClient.query(solrQuery);
        response.getResults().forEach((doc) -> {
            action.accept(doc);
        });
        return response.getNextCursorMark();
    }
    // 4 -------------------------------
    /**
     * This iteration guarantees, that order of "description" records is always the same after rebuilding Processing index.
     * Also order of "relation" records from same RELS-EXT is the same, but it does NOT match order of elements in RELS-EXT.
     * Order of "relation" records from different RELS-EXTs is undefined and may change between rebuilding of Processing index.
     */
    public void iterateProcessingSortedByPid(String query, Consumer<SolrDocument> action) throws IOException, SolrServerException {
        iterateProcessingWithSort(query, "pid", SolrQuery.ORDER.asc, action);
    }
    // 4 -------------------------------------------
    public List<Pair<String, String>> findByTargetPid(String pid) throws IOException, SolrServerException {
        final List<Pair<String, String>> retvals = new ArrayList<>();
        iterateProcessingSortedByPid("targetPid:\"" + pid + "\"", (doc) -> {
            Pair<String, String> pair = new ImmutablePair<>(doc.getFieldValue("source").toString(), doc.getFieldValue("relation").toString());
            retvals.add(pair);
        });
        return retvals;
    }
    // 5 ----------------------------------------
    public void iterateSectionOfProcessingSortedByIndexationDate(String query, int offset, int limit, Consumer<SolrDocument> action) throws IOException, SolrServerException {
        //řazení podle date zaručí jen jednoznačné řazení, což by ale zvládlo (a lépe) i řazení podle pid
        //date obsahuje timestamp vytvoření záznamu v processing indexu, ten proces ale probíhá paraleleně, takže tohle pořadí se po rebuildu processing indexu změní
        //takže "date" nijak nesouvisí s přidáním do repozitáře, nebo snad publikací
        iterateSectionOfProcessing(query, "date", SolrQuery.ORDER.desc, offset, limit, action);
    }
    // 6 ---------------------------------------------
    /**
     * This iteration guarantees, that order of "relation" records matches order of elements in any single RELS-EXT.
     * Order of "description" record is undefined as is order of "relation" records from different RELS-EXTs.
     * Field "date" depends on process, that rebuilds Processing index. This process is parallelized and results may differ between different runs.
     */
    public void iterateProcessingSortedByIndexationDate(String query, boolean ascending, Consumer<SolrDocument> action) throws IOException, SolrServerException {
        iterateProcessingWithSort(query, "date", ascending ? SolrQuery.ORDER.asc : SolrQuery.ORDER.desc, action);
    }
    // 7 ------------------------------------
    /**
     * This iteration is convenient, if you want to show data at least somehow sorted
     */
    public void iterateProcessingSortedByTitle(String query, Consumer<SolrDocument> action) throws IOException, SolrServerException {
        // TODO: Change sorting
        iterateProcessingWithSort(query, "dc.title", SolrQuery.ORDER.asc, action);
    }

    private void iterateSectionOfProcessing(String query, String sortField, SolrQuery.ORDER order, int offset, int limit, Consumer<SolrDocument> action) throws IOException, SolrServerException {
        SolrQuery solrQuery = new SolrQuery(query);
        solrQuery.setStart(offset).setRows(limit);
        solrQuery.setSort(sortField, order);
        QueryResponse response = this.solrClient.query(solrQuery);
        response.getResults().forEach((doc) -> {
            action.accept(doc);
        });
    }
    private void iterateProcessingWithSort(String query, String sortField, SolrQuery.ORDER order, Consumer<SolrDocument> action) throws IOException, SolrServerException {
        SolrQuery solrQuery = new SolrQuery(query);
        int rows = 1000;
        solrQuery.setRows(rows);
        solrQuery.addSort("pid", SolrQuery.ORDER.asc);
        solrQuery.addSort(sortField, order);
        String cursorMark = CursorMarkParams.CURSOR_MARK_START;
        boolean done = false;
        while (!done) {
            solrQuery.set(CursorMarkParams.CURSOR_MARK_PARAM, cursorMark);
            QueryResponse response = this.solrClient.query(solrQuery);
            String nextCursorMark = response.getNextCursorMark();
            response.getResults().forEach((doc) -> {
                action.accept(doc);
            });
            if (cursorMark.equals(nextCursorMark)) {
                done = true;
            }
            cursorMark = nextCursorMark;
        }
    }

    public UpdateResponse feedDescriptionDocument(String sourcePid, String model, String title, String ref, Date date, TitleType ttype) throws IOException, SolrServerException {

        //String processingSolrHost = KConfiguration.getInstance().getSolrProcessingHost();

        SolrInputDocument sdoc = new SolrInputDocument();
        sdoc.addField("source", sourcePid);
        sdoc.addField("type", TYPE_DESC);
        sdoc.addField("model", model);
        if (ttype.equals(TitleType.mods)) {
            sdoc.addField("mods.title", title);
        } else {
            sdoc.addField("dc.title", title);
        }
        sdoc.addField("ref", ref);
        sdoc.addField("date", date);
        sdoc.addField("pid", TYPE_DESC + "|" + sourcePid);
        return feedDescriptionDocument(sdoc);
    }
    public UpdateResponse feedDescriptionDocument(String sourcePid, String model, String title, String ref, Date date) throws IOException, SolrServerException {
        SolrInputDocument sdoc = new SolrInputDocument();
        sdoc.addField("source", sourcePid);
        sdoc.addField("type", TYPE_DESC);
        sdoc.addField("model", model);
        sdoc.addField("dc.title", title);
        sdoc.addField("ref", ref);
        sdoc.addField("date", date);
        sdoc.addField("pid", TYPE_DESC + "|" + sourcePid);
        return feedDescriptionDocument(sdoc);
    }
    public UpdateResponse feedDescriptionDocument(SolrInputDocument doc) throws IOException, SolrServerException {
        UpdateResponse response = this.solrClient.add(doc);
        return response;
    }

    public UpdateResponse feedRelationDocument(String sourcePid, String relation, String targetPid) throws IOException, SolrServerException {
        SolrInputDocument sdoc = new SolrInputDocument();
        sdoc.addField("source", sourcePid);
        sdoc.addField("type", TYPE_RELATION);
        sdoc.addField("relation", relation);
        sdoc.addField("targetPid", targetPid);
        sdoc.addField("pid", TYPE_RELATION + "|" + sourcePid + "|" + relation + "|" + targetPid);
        return feedRelationDocument(sdoc);
    }
    public UpdateResponse feedRelationDocument(SolrInputDocument sdoc) throws IOException, SolrServerException {
        UpdateResponse resp = this.solrClient.add(sdoc);
        return resp;
    }

    public UpdateResponse deleteProcessingIndex() throws IOException, SolrServerException {
        UpdateResponse response = this.solrClient.deleteByQuery("*:*");
        return response;
    }
    public UpdateResponse deleteByPid(String pid) throws IOException, SolrServerException {
        UpdateResponse response = this.solrClient.deleteByQuery("source:\"" + pid + "\"");
        return response;
    }
    public UpdateResponse deleteByTargetPid(String pid) throws IOException, SolrServerException {
        UpdateResponse response = this.solrClient.deleteByQuery("targetPid:\"" + pid + "\"");
        return response;
    }
    public UpdateResponse deleteDescriptionByPid(String pid) throws IOException, SolrServerException {
        UpdateResponse response = this.solrClient.deleteByQuery("source:\"" + pid + "\" AND type:\"description\"");
        return response;
    }
    public UpdateResponse deleteByRelationsForPid(String pid) throws IOException, SolrServerException {
        String query = "source:\"" + pid + "\" AND type:\"relation\"";
        UpdateResponse response = this.solrClient.deleteByQuery(query);
        return response;
    }

    public void rebuildProcessingIndex(RepositoryObject repositoryObject, InputStream content) throws RepositoryException {
        try {
            String s = IOUtils.toString(content, "UTF-8");
            RELSEXTSPARQLBuilder sparqlBuilder = new RELSEXTSPARQLBuilderImpl();
            sparqlBuilder.sparqlProps(s.trim(), (object, localName) -> {
                processRELSEXTRelationAndFeedProcessingIndex(repositoryObject, object, localName);
                return object;
            });
        } catch (IOException e) {
            throw new RepositoryException(e);
        } catch (SAXException e) {
            throw new RepositoryException(e);
        } catch (ParserConfigurationException e) {
            throw new RepositoryException(e);
        } finally {
            try {
                this.commit();
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (SolrServerException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Process one relation and feed processing index
     */
    private void processRELSEXTRelationAndFeedProcessingIndex(RepositoryObject repositoryObject, String object, String localName) {
        if (localName.equals("hasModel")) {
            try {
                boolean dcStreamExists = repositoryObject.streamExists(FedoraUtils.DC_STREAM);
                // TODO: Biblio mods ukladat jinam ??
                boolean modsStreamExists = repositoryObject.streamExists(FedoraUtils.BIBLIO_MODS_STREAM);
                if (dcStreamExists || modsStreamExists) {
                    try {
                        //LOGGER.info("DC or BIBLIOMODS exists");
                        if (dcStreamExists) {
                            List<String> dcTList = dcTitle(repositoryObject);
                            if (dcTList != null && !dcTList.isEmpty()) {
                                this.indexDescription(repositoryObject.getPath(), object, dcTList.stream().collect(Collectors.joining(" ")));
                            } else {
                                this.indexDescription(repositoryObject.getPath(), object, "");
                            }
                        } else if (modsStreamExists) {
                            // czech title or default
                            List<String> modsTList = modsTitle(repositoryObject, "cze");
                            if (modsTList != null && !modsTList.isEmpty()) {
                                this.indexDescription(repositoryObject.getPath(), object, modsTList.stream().collect(Collectors.joining(" ")), ProcessingIndexFeeder.TitleType.mods);
                            } else {
                                this.indexDescription(repositoryObject.getPath(), object, "");
                            }
                        }
                    } catch (ParserConfigurationException e) {
                        LOGGER.log(Level.SEVERE, e.getMessage(), e);
                        this.indexDescription(repositoryObject.getPath(), object, "");
                    } catch (SAXException e) {
                        LOGGER.log(Level.SEVERE, e.getMessage(), e);
                        this.indexDescription(repositoryObject.getPath(), object, "");
                    }
                } else {
                    LOGGER.info("Index description without dc or mods");
                    this.indexDescription(repositoryObject.getPath(), object, "");
                }
            } catch (Throwable th) {
                LOGGER.log(Level.SEVERE, "Cannot update processing index for "+ repositoryObject.getPath() + " - reindex manually.", th);
            }
        } else {
            try {
                this.feedRelationDocument(repositoryObject.getPath(), localName, object);
            } catch (Throwable th) {
                LOGGER.log(Level.SEVERE, "Cannot update processing index for "+ repositoryObject.getPath() + " - reindex manually.", th);
            }
        }
    }

    private void indexDescription(String pid, String model, String title, ProcessingIndexFeeder.TitleType ttype) throws IOException, SolrServerException {
        this.feedDescriptionDocument(pid, model, title.trim(), RepositoryUtils.getAkubraInternalId(pid), new Date(), ttype);
    }

    private void indexDescription(String pid, String model, String title) throws IOException, SolrServerException {
        this.feedDescriptionDocument(pid, model, title.trim(), RepositoryUtils.getAkubraInternalId(pid), new Date());
    }

    private List<String> dcTitle(RepositoryObject repositoryObject) throws RepositoryException, ParserConfigurationException, SAXException, IOException {
        InputStream stream = repositoryObject.getStream(FedoraUtils.DC_STREAM).getContent();
        Element title = XMLUtils.findElement(XMLUtils.parseDocument(stream, true).getDocumentElement(), "title", FedoraNamespaces.DC_NAMESPACE_URI);
        return title != null ? Arrays.asList(title.getTextContent()) : new ArrayList<>();
    }

    private List<String> modsTitle(RepositoryObject repositoryObject, String lang) throws RepositoryException, ParserConfigurationException, SAXException, IOException {
        InputStream stream = repositoryObject.getStream(FedoraUtils.BIBLIO_MODS_STREAM).getContent();
        Element docElement = XMLUtils.parseDocument(stream, true).getDocumentElement();

        List<Element> elements = XMLUtils.getElementsRecursive(docElement, new XMLUtils.ElementsFilter() {
            @Override
            public boolean acceptElement(Element element) {
                if (element.getNamespaceURI().equals(FedoraNamespaces.BIBILO_MODS_URI)) {
                    if (element.getLocalName().equals("title") && element.hasAttribute("lang") && element.getAttribute("lang").equals("cze")) {
                        return true;
                    }
                }
                return false;
            }
        });


        if (elements.isEmpty()) {
            elements = XMLUtils.getElementsRecursive(docElement, new XMLUtils.ElementsFilter() {
                @Override
                public boolean acceptElement(Element element) {
                    if (element.getNamespaceURI().equals(FedoraNamespaces.BIBILO_MODS_URI)) {
                        // TODO: Change it
                        if (element.getLocalName().equals("title")) {
                            return true;
                        }
                    }
                    return false;
                }
            });

        }

        return elements.stream().map(Element::getTextContent).collect(Collectors.toList());

    }
    // commit to solr
    public void commit() throws IOException, SolrServerException {
        this.solrClient.commit();
        LOGGER.info("Processing index commit ");
    }



}
