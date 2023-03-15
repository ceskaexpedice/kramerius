package cz.kramerius.searchIndex.repositoryAccessImpl;

import cz.kramerius.adapters.RepositoryAccess;
import cz.incad.kramerius.ProcessSubtreeException;
import cz.incad.kramerius.StreamHeadersObserver;
import cz.incad.kramerius.TreeNodeProcessor;
import cz.incad.kramerius.fedora.om.Repository;
import cz.incad.kramerius.fedora.om.RepositoryException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.List;
import java.util.Map;

public abstract class RepositoryAccessImplAbstract implements RepositoryAccess {

    @Override
    public Document getRelsExt(String pid) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getKrameriusModelName(Document relsExt) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getKrameriusModelName(String pid) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<String> getModelsOfRel(Document relsExt) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<String> getModelsOfRel(String pid) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getDonator(Document relsExt) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getDonator(String pid) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Document getBiblioMods(String pid) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Document getDC(String pid) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public String findFirstViewablePid(String pid) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean getFirstViewablePath(List<String> pids, List<String> models) throws IOException {
        return false;
    }

    @Override
    public List<Element> getPages(String pid, boolean deep) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Element> getPages(String pid, Element rootElementOfRelsExt) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public InputStream getSmallThumbnail(String pid) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Document getSmallThumbnailProfile(String pid) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getSmallThumbnailMimeType(String pid) throws IOException, XPathExpressionException {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isFullthumbnailAvailable(String pid) throws IOException {
        return false;
    }

    @Override
    public InputStream getFullThumbnail(String pid) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getFullThumbnailMimeType(String pid) throws IOException, XPathExpressionException {
        throw new UnsupportedOperationException();
    }

    @Override
    public InputStream getImageFULL(String pid) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Document getImageFULLProfile(String pid) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getImageFULLMimeType(String pid) throws IOException, XPathExpressionException {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isImageFULLAvailable(String pid) throws IOException {
        return false;
    }

    @Override
    public boolean isStreamAvailable(String pid, String streamName) throws IOException {
        return false;
    }

    @Override
    public boolean isObjectAvailable(String pid) throws IOException {
        return false;
    }

    @Override
    public boolean isContentAccessible(String pid) throws IOException {
        return false;
    }

    @Override
    public Repository getInternalAPI() throws RepositoryException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Repository getTransactionAwareInternalAPI() throws RepositoryException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void processSubtree(String pid, TreeNodeProcessor processor) throws ProcessSubtreeException, IOException {

    }

    @Override
    public List<String> getPids(String pid) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public InputStream getDataStream(String pid, String datastreamName) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void observeStreamHeaders(String pid, String datastreamName, StreamHeadersObserver streamObserver) throws IOException {

    }

    @Override
    public InputStream getDataStreamXml(String pid, String datastreamName) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Document getDataStreamXmlAsDocument(String pid, String datastreamName) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getMimeTypeForStream(String pid, String datastreamName) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getFedoraVersion() throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Document getStreamProfile(String pid, String stream) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Document getObjectProfile(String pid) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public InputStream getFedoraDataStreamsList(String pid) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Document getFedoraDataStreamsListAsDocument(String pid) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Date getStreamLastmodifiedFlag(String pid, String stream) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Date getObjectLastmodifiedFlag(String pid) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Map<String, String>> getStreamsOfObject(String pid) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public InputStream getFoxml(String pid) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getFirstItemPid(String pid) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getFirstVolumePid(Document relsExt) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getFirstVolumePid(String pid) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getExternalStreamURL(String pid, String datastreamName) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public InputStream getFoxml(String pid, boolean archive) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getFirstItemPid(Document relsExt) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getDatastreamMimeType(String pid, String datastreamName) throws IOException {
        throw new UnsupportedOperationException();
    }
}
