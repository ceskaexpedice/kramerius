package cz.incad.kramerius.repository;

import cz.incad.kramerius.fedora.om.RepositoryException;
import cz.incad.kramerius.repository.utils.NamespaceRemovingVisitor;
import org.apache.solr.client.solrj.SolrServerException;
import org.dom4j.Document;

import javax.inject.Inject;
import java.io.IOException;
import java.util.List;

public class KrameriusRepositoryApiImpl implements KrameriusRepositoryApi {

    @Inject
    private RepositoryApiImpl repositoryApi;

    @Override
    public RepositoryApi getLowLevelApi() {
        return repositoryApi;
    }

    @Override
    public boolean isRelsExtAvailable(String pid) throws IOException, RepositoryException {
        return repositoryApi.datastreamExists(pid, KnownDatastreams.RELS_EXT.toString());
    }

    @Override
    public Document getRelsExt(String pid, boolean namespaceAware) throws IOException, RepositoryException {
        Document doc = repositoryApi.getLatestVersionOfInlineXmlDatastream(pid, KnownDatastreams.RELS_EXT.toString());
        if (doc != null && !namespaceAware) {
            doc.accept(new NamespaceRemovingVisitor(true, true));
        }
        return doc;
    }

    @Override
    public boolean isModsAvailable(String pid) throws IOException, RepositoryException {
        return repositoryApi.datastreamExists(pid, KnownDatastreams.BIBLIO_MODS.toString());
    }

    @Override
    public Document getMods(String pid, boolean namespaceAware) throws IOException, RepositoryException {
        Document doc = repositoryApi.getLatestVersionOfInlineXmlDatastream(pid, KnownDatastreams.BIBLIO_MODS.toString());
        if (doc != null && !namespaceAware) {
            doc.accept(new NamespaceRemovingVisitor(true, true));
        }
        return doc;
    }

    @Override
    public boolean isDublinCoreAvailable(String pid) throws IOException, RepositoryException {
        return repositoryApi.datastreamExists(pid, KnownDatastreams.BIBLIO_DC.toString());
    }

    @Override
    public Document getDublinCore(String pid, boolean namespaceAware) throws IOException, RepositoryException {
        Document doc = repositoryApi.getLatestVersionOfInlineXmlDatastream(pid, KnownDatastreams.BIBLIO_DC.toString());
        if (doc != null && !namespaceAware) {
            doc.accept(new NamespaceRemovingVisitor(true, true));
        }
        return doc;
    }

    @Override
    public List<String> getPidsOfItemsInCollection(String collectionPid) throws RepositoryException, IOException, SolrServerException {
        return repositoryApi.getTripletTargets(collectionPid, KnownRelations.CONTAINS.toString());
    }

    @Override
    public List<String> getPidsOfCollectionsContainingItem(String itemPid) throws RepositoryException, IOException, SolrServerException {
        return repositoryApi.getTripletSources(KnownRelations.CONTAINS.toString(), itemPid);
    }

    @Override
    public void updateRelsExt(String pid, Document relsExtDoc) throws IOException, RepositoryException {
        repositoryApi.updateInlineXmlDatastream(pid, KnownDatastreams.RELS_EXT.toString(), relsExtDoc, KnownXmlFormatUris.RELS_EXT);
    }

    @Override
    public void updateMods(String pid, Document modsDoc) throws IOException, RepositoryException {
        repositoryApi.updateInlineXmlDatastream(pid, KnownDatastreams.BIBLIO_MODS.toString(), modsDoc, KnownXmlFormatUris.BIBLIO_MODS);
    }

    @Override
    public void updateDublinCore(String pid, Document dcDoc) throws IOException, RepositoryException {
        repositoryApi.updateInlineXmlDatastream(pid, KnownDatastreams.BIBLIO_DC.toString(), dcDoc, KnownXmlFormatUris.BIBLIO_DC);
    }

}
