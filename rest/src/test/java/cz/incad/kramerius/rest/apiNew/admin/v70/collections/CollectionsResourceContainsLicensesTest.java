package cz.incad.kramerius.rest.apiNew.admin.v70.collections;

import cz.incad.kramerius.processes.client.ProcessManagerMapper;
import cz.incad.kramerius.SolrAccess;
import cz.kramerius.searchIndex.indexer.nodes.RepositoryNode;
import org.apache.commons.io.IOUtils;
import org.ceskaexpedice.akubra.AkubraRepository;
import org.ceskaexpedice.akubra.DatastreamContentWrapper;
import org.ceskaexpedice.akubra.LockOperation;
import org.ceskaexpedice.akubra.relsext.RelsExtHelper;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.io.InputStream;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CollectionsResourceContainsLicensesTest {

    @Test
    public void shouldReplaceContainsLicensesInRelsExt() throws Exception {
        Document afterAdd = collectionRelsExt("uuid:item-a", "uuid:item-b");
        CollectionsResource.replaceContainsLicenses(afterAdd, new HashSet<>(Arrays.asList("dnnto", "onsite", "public")));

        Assert.assertEquals(
                new HashSet<>(Arrays.asList("dnnto", "onsite", "public")),
                containsLicenses(afterAdd)
        );
        Assert.assertEquals(3, relationCount(afterAdd, "containsLicense"));
        Assert.assertEquals(0, relationCount(afterAdd, "containsLicenses"));
        Assert.assertEquals(0, relationCount(afterAdd, "contains-license"));

        Document afterRemove = collectionRelsExt("uuid:item-a");
        CollectionsResource.replaceContainsLicenses(afterRemove, new HashSet<>(Arrays.asList("stale")));
        CollectionsResource.replaceContainsLicenses(afterRemove, new HashSet<>(Arrays.asList("dnnto", "onsite")));

        Assert.assertEquals(
                new HashSet<>(Arrays.asList("dnnto", "onsite")),
                containsLicenses(afterRemove)
        );
        Assert.assertEquals(2, relationCount(afterRemove, "containsLicense"));
        Assert.assertEquals(0, relationCount(afterRemove, "containsLicenses"));
        Assert.assertEquals(0, relationCount(afterRemove, "contains-license"));
    }

    @Test
    public void shouldPrepareTreeAndFosterTreesReindexationForCollectionItem() {
        JSONObject schedule = new TestableCollectionsResource().scheduleForCollectionItem("uuid:item-a", "admin");
        JSONObject payload = schedule.getJSONObject(ProcessManagerMapper.PCP_PAYLOAD);

        Assert.assertEquals("new_indexer_index_object", schedule.getString(ProcessManagerMapper.PCP_PROFILE_ID));
        Assert.assertEquals("admin", schedule.getString(ProcessManagerMapper.PCP_OWNER_ID_SCH));
        Assert.assertEquals("uuid:item-a", payload.getString("pid"));
        Assert.assertEquals("TREE_AND_FOSTER_TREES", payload.getString("type"));
        Assert.assertEquals("true", payload.getString("ignoreInconsistentObjects"));
    }

    @Test
    public void shouldUpdateContainsLicensesInRepositoryAndSearchIndex() throws Exception {
        String collectionPid = "uuid:collection";
        Document[] collectionRelsExt = new Document[] { collectionRelsExt("uuid:item-a", "uuid:item-b") };

        RelsExtHelper relsExtHelper = mock(RelsExtHelper.class);
        DatastreamContentWrapper collectionWrapper = datastreamContentWrapper(collectionRelsExt);
        when(relsExtHelper.get(collectionPid)).thenReturn(collectionWrapper);
        org.mockito.Mockito.doAnswer(invocation -> {
            collectionRelsExt[0] = DocumentHelper.parseText(IOUtils.toString((InputStream) invocation.getArgument(1), "UTF-8"));
            return null;
        }).when(relsExtHelper).update(eq(collectionPid), any());

        AkubraRepository repository = mock(AkubraRepository.class);
        when(repository.re()).thenReturn(relsExtHelper);
        when(repository.doWithLock(eq(collectionPid), any())).thenAnswer(invocation -> {
            LockOperation<?> operation = invocation.getArgument(1);
            return operation.execute();
        });

        TestableCollectionsResource resource = new TestableCollectionsResource();
        resource.akubraRepository = repository;
        resource.solrAccess = solrAccessReturningLicenses("dnnto", "onsite", "public");
        RepositoryNode collectionNode = new RepositoryNode(
                collectionPid, "collection", null,
                collectionPid, "collection", Arrays.asList(collectionPid),
                collectionPid, "collection", null,
                null, null, null, null,
                null, null, null,
                null, null,
                null, null, null, null,
                null, null, null, null
        );
        CapturingSearchIndexFieldWriter searchIndexWriter = new CapturingSearchIndexFieldWriter();

        resource.updateCollectionContainsLicensesInRepositoryAndSearchIndex(
                collectionPid, true, Arrays.asList("uuid:item-a", "uuid:item-b"), java.util.Collections.emptyList(), collectionNode, searchIndexWriter);

        Assert.assertEquals(
                new HashSet<>(Arrays.asList("dnnto", "onsite", "public")),
                containsLicenses(collectionRelsExt[0])
        );
        Assert.assertEquals(collectionPid, searchIndexWriter.pid);
        Assert.assertSame(collectionNode, searchIndexWriter.repositoryNode);
        Assert.assertEquals("contains_licenses", searchIndexWriter.fieldName);
        Assert.assertEquals(new HashSet<>(Arrays.asList("dnnto", "onsite", "public")), new HashSet<>((List<String>) searchIndexWriter.value));
        Assert.assertTrue(searchIndexWriter.indexTime);
        Assert.assertTrue(searchIndexWriter.explicitCommit);
        verify(resource.solrAccess).requestWithSelectReturningJson(
                org.mockito.ArgumentMatchers.contains("pid_paths.children"),
                any()
        );
    }

    private SolrAccess solrAccessReturningLicenses(String... licenses) throws Exception {
        SolrAccess solrAccess = mock(SolrAccess.class);
        when(solrAccess.requestWithSelectReturningJson(any(String.class), any())).thenReturn(facetResponse(licenses));
        when(solrAccess.getSolrDataByPid(eq("uuid:item-a"), eq("pid_paths"))).thenReturn(solrResponseWithPidPaths("uuid:item-a"));
        when(solrAccess.getSolrDataByPid(eq("uuid:item-b"), eq("pid_paths"))).thenReturn(solrResponseWithPidPaths("uuid:item-b"));
        return solrAccess;
    }

    private org.w3c.dom.Document solrResponseWithPidPaths(String... pidPaths) throws Exception {
        javax.xml.parsers.DocumentBuilderFactory factory = javax.xml.parsers.DocumentBuilderFactory.newInstance();
        javax.xml.parsers.DocumentBuilder builder = factory.newDocumentBuilder();
        org.w3c.dom.Document doc = builder.newDocument();
        org.w3c.dom.Element response = doc.createElement("response");
        org.w3c.dom.Element result = doc.createElement("result");
        org.w3c.dom.Element solrDoc = doc.createElement("doc");
        org.w3c.dom.Element arr = doc.createElement("arr");
        arr.setAttribute("name", "pid_paths");
        for (String pidPath : pidPaths) {
            org.w3c.dom.Element str = doc.createElement("str");
            str.setTextContent(pidPath);
            arr.appendChild(str);
        }
        solrDoc.appendChild(arr);
        result.appendChild(solrDoc);
        response.appendChild(result);
        doc.appendChild(response);
        return doc;
    }

    private JSONObject facetResponse(String... licenses) {
        org.json.JSONArray facet = new org.json.JSONArray();
        for (String license : licenses) {
            facet.put(license);
            facet.put(1);
        }
        return new JSONObject()
                .put("facet_counts", new JSONObject()
                        .put("facet_fields", new JSONObject()
                                .put("licenses", facet)));
    }

    private Document collectionRelsExt(String... itemPids) throws DocumentException {
        String contains = Arrays.stream(itemPids)
                .map(pid -> "<rel:contains rdf:resource=\"info:fedora/" + pid + "\"/>")
                .collect(Collectors.joining());
        return relsExt(
                contains +
                "<rel:containsLicenses>legacy</rel:containsLicenses>" +
                "<rel:contains-license>legacy-hyphen</rel:contains-license>"
        );
    }

    private Document relsExt(String descriptionContent) throws DocumentException {
        return DocumentHelper.parseText(
                "<rdf:RDF xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\" " +
                "xmlns:rel=\"http://www.nsdl.org/ontologies/relationships#\">" +
                "<rdf:Description rdf:about=\"info:fedora/uuid:collection\">" +
                descriptionContent +
                "</rdf:Description>" +
                "</rdf:RDF>"
        );
    }

    private DatastreamContentWrapper datastreamContentWrapper(Document[] document) {
        DatastreamContentWrapper wrapper = mock(DatastreamContentWrapper.class);
        when(wrapper.asDom4j(any(Boolean.class))).thenAnswer(invocation -> document[0]);
        return wrapper;
    }

    private Set<String> containsLicenses(Document relsExt) {
        Set<String> result = new HashSet<>();
        for (Element element : elementsByName(relsExt.getRootElement(), "containsLicense")) {
            result.add(element.getText());
        }
        return result;
    }

    private int relationCount(Document relsExt, String localName) {
        return elementsByName(relsExt.getRootElement(), localName).size();
    }

    private List<Element> elementsByName(Element root, String localName) {
        List<Element> result = new java.util.ArrayList<>();
        collectElementsByName(root, localName, result);
        return result;
    }

    private void collectElementsByName(Element element, String localName, List<Element> result) {
        if (localName.equals(element.getName())) {
            result.add(element);
        }
        for (Object child : element.elements()) {
            collectElementsByName((Element) child, localName, result);
        }
    }

    private static class TestableCollectionsResource extends CollectionsResource {
        JSONObject scheduleForCollectionItem(String pid, String userId) {
            return getScheduleReindexationPar(pid, userId, "TREE_AND_FOSTER_TREES", true, pid);
        }
    }

    private static class CapturingSearchIndexFieldWriter implements CollectionsResource.SearchIndexFieldWriter {
        private String pid;
        private RepositoryNode repositoryNode;
        private String fieldName;
        private Object value;
        private boolean indexTime;
        private boolean explicitCommit;

        @Override
        public void setSingleFieldValue(String pid, RepositoryNode repositoryNode, String fieldName, Object value, boolean indexTime, boolean explicitCommit) {
            this.pid = pid;
            this.repositoryNode = repositoryNode;
            this.fieldName = fieldName;
            this.value = value;
            this.indexTime = indexTime;
            this.explicitCommit = explicitCommit;
        }
    }
}
