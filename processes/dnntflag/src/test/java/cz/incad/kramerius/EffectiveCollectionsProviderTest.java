package cz.incad.kramerius;

import org.ceskaexpedice.akubra.AkubraRepository;
import org.ceskaexpedice.akubra.processingindex.OwnedAndFosteredChildren;
import org.ceskaexpedice.akubra.processingindex.OwnedAndFosteredParents;
import org.ceskaexpedice.akubra.processingindex.ProcessingIndex;
import org.ceskaexpedice.akubra.processingindex.ProcessingIndexItem;
import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.util.Arrays;
import java.util.Collections;

public class EffectiveCollectionsProviderTest {
    @Test
    public void processingIndexProviderReturnsEmptyForMissingTargetPid() {
        AkubraRepository repository = EasyMock.createNiceMock(AkubraRepository.class);
        EasyMock.replay(repository);
        java.util.Set<String> result = new ProcessingIndexEffectiveCollectionsProvider(repository)
                .getAffectedCollections(null);
        Assert.assertTrue(result.isEmpty());
        EasyMock.verify(repository);
    }

    @Test
    public void processingIndexProviderMapsOwnedDescendantsToFosterCollectionsAndAncestors() {
        String periodical = "uuid:periodical";
        String issue = "uuid:issue";
        String page = "uuid:page";
        String collection = "uuid:collection";
        String parentCollection = "uuid:parent-collection";

        ProcessingIndex processingIndex = EasyMock.createNiceMock(ProcessingIndex.class);
        AkubraRepository repository = EasyMock.createNiceMock(AkubraRepository.class);
        EasyMock.expect(repository.pi()).andReturn(processingIndex).anyTimes();

        EasyMock.expect(processingIndex.getOwnedAndFosteredChildren(periodical))
                .andReturn(children(issue));
        EasyMock.expect(processingIndex.getOwnedAndFosteredChildren(issue))
                .andReturn(children(page));
        EasyMock.expect(processingIndex.getOwnedAndFosteredChildren(page))
                .andReturn(children());

        EasyMock.expect(processingIndex.getOwnedAndFosteredParents(periodical))
                .andReturn(parents(null));
        EasyMock.expect(processingIndex.getOwnedAndFosteredParents(issue))
                .andReturn(parents(periodical));
        EasyMock.expect(processingIndex.getOwnedAndFosteredParents(page))
                .andReturn(new OwnedAndFosteredParents(parent(issue),
                        Collections.singletonList(parent(collection))));
        EasyMock.expect(processingIndex.getOwnedAndFosteredParents(collection))
                .andReturn(parents(parentCollection));
        EasyMock.expect(processingIndex.getOwnedAndFosteredParents(parentCollection))
                .andReturn(parents(null));
        EasyMock.expect(processingIndex.getModel(collection)).andReturn("collection");
        EasyMock.expect(processingIndex.getModel(parentCollection)).andReturn("collection");

        EasyMock.replay(repository, processingIndex);

        java.util.Set<String> result = new ProcessingIndexEffectiveCollectionsProvider(repository)
                .getAffectedCollections(periodical);

        Assert.assertEquals(new java.util.HashSet<>(Arrays.asList(collection, parentCollection)), result);
        EasyMock.verify(repository, processingIndex);
    }

    @Test
    public void searchIndexProviderReturnsTargetMemberships() throws Exception {
        String periodical = "uuid:periodical";

        SolrAccess searchIndex = EasyMock.createMock(SolrAccess.class);
        EasyMock.expect(searchIndex.getSolrDataByPid(periodical,
                        "model,in_collections,in_collections.direct,own_pid_path"))
                .andReturn(solrXml("<arr name=\"in_collections\"><str>uuid:old-collection</str></arr>"));
        EasyMock.replay(searchIndex);

        java.util.Set<String> result = new SearchIndexEffectiveCollectionsProvider(searchIndex)
                .getAffectedCollections(periodical);

        Assert.assertEquals(Collections.singleton("uuid:old-collection"), result);
        EasyMock.verify(searchIndex);
    }

    @Test
    public void searchIndexProviderIncludesTargetWhenItIsACollection() throws Exception {
        String collection = "uuid:collection";
        SolrAccess searchIndex = EasyMock.createMock(SolrAccess.class);
        EasyMock.expect(searchIndex.getSolrDataByPid(collection,
                        "model,in_collections,in_collections.direct,own_pid_path"))
                .andReturn(solrXml("<str name=\"model\">collection</str>"
                        + "<arr name=\"in_collections\"><str>uuid:parent-collection</str></arr>"));
        EasyMock.replay(searchIndex);

        java.util.Set<String> result = new SearchIndexEffectiveCollectionsProvider(searchIndex)
                .getAffectedCollections(collection);

        Assert.assertEquals(new java.util.HashSet<>(Arrays.asList(collection, "uuid:parent-collection")), result);
        EasyMock.verify(searchIndex);
    }

    @Test
    public void searchIndexProviderReturnsEmptyWhenTargetIsNotIndexed() throws Exception {
        SolrAccess searchIndex = EasyMock.createMock(SolrAccess.class);
        EasyMock.expect(searchIndex.getSolrDataByPid("uuid:missing",
                        "model,in_collections,in_collections.direct,own_pid_path"))
                .andReturn(null);
        EasyMock.replay(searchIndex);

        java.util.Set<String> result = new SearchIndexEffectiveCollectionsProvider(searchIndex)
                .getAffectedCollections("uuid:missing");

        Assert.assertTrue(result.isEmpty());
        EasyMock.verify(searchIndex);
    }

    @Test
    @Ignore
    public void searchIndexProviderDoesNotRequirePidPaths() throws Exception {
        SolrAccess searchIndex = EasyMock.createMock(SolrAccess.class);
        EasyMock.expect(searchIndex.getSolrDataByPid("uuid:target",
                        "model,in_collections,in_collections.direct,own_pid_path"))
                .andReturn(solrXml("<str name=\"pid\">uuid:target</str>"));
        EasyMock.replay(searchIndex);

        java.util.Set<String> result = new SearchIndexEffectiveCollectionsProvider(searchIndex)
                .getAffectedCollections("uuid:target");
        Assert.assertTrue(result.isEmpty());
        EasyMock.verify(searchIndex);
    }

    private OwnedAndFosteredChildren children(String... childPids) {
        return new OwnedAndFosteredChildren(
                Arrays.stream(childPids).map(this::child).collect(java.util.stream.Collectors.toList()),
                Collections.emptyList());
    }

    private OwnedAndFosteredParents parents(String ownParentPid) {
        return new OwnedAndFosteredParents(ownParentPid == null ? null : parent(ownParentPid),
                Collections.emptyList());
    }

    private ProcessingIndexItem parent(String pid) {
        return item(pid, null);
    }

    private ProcessingIndexItem child(String pid) {
        return item(null, pid);
    }

    private ProcessingIndexItem item(String sourcePid, String targetPid) {
        return new ProcessingIndexItem(sourcePid, null, null, null, null, null, null,
                null, null, targetPid, Collections.emptyList());
    }

    private Document solrXml(String fields) throws Exception {
        String xml = "<response><result name=\"response\"><doc>" + fields + "</doc></result></response>";
        return DocumentBuilderFactory.newInstance().newDocumentBuilder()
                .parse(new InputSource(new StringReader(xml)));
    }
}
