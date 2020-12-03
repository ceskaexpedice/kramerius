package cz.incad.kramerius.repository;

import cz.incad.kramerius.fedora.om.RepositoryException;
import cz.incad.kramerius.repository.utils.NamespaceRemovingVisitor;
import cz.incad.kramerius.utils.java.Pair;
import org.apache.solr.client.solrj.SolrServerException;
import org.dom4j.Document;

import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
    public boolean isOcrTextAvailable(String pid) throws IOException, RepositoryException {
        return repositoryApi.datastreamExists(pid, KnownDatastreams.OCR_TEXT.toString());
    }

    @Override
    public String getOcrText(String pid) throws IOException, RepositoryException {
        return repositoryApi.getLatestVersionOfManagedTextDatastream(pid, KnownDatastreams.OCR_TEXT.toString());
    }

    @Override
    public boolean isOcrAltoAvailable(String pid) throws IOException, RepositoryException {
        return repositoryApi.datastreamExists(pid, KnownDatastreams.OCR_ALTO.toString());
    }

    @Override
    public Document getOcrAlto(String pid, boolean namespaceAware) throws IOException, RepositoryException {
        Document doc = repositoryApi.getLatestVersionOfInlineXmlDatastream(pid, KnownDatastreams.OCR_ALTO.toString());
        if (doc != null && !namespaceAware) {
            doc.accept(new NamespaceRemovingVisitor(true, true));
        }
        return doc;
    }

    @Override
    public boolean isImgFullAvailable(String pid) throws IOException, RepositoryException {
        return repositoryApi.datastreamExists(pid, KnownDatastreams.IMG_FULL.toString());
    }

    @Override
    public String getImgFullMimetype(String pid) throws IOException, RepositoryException {
        return repositoryApi.getDatastreamMimetype(pid, KnownDatastreams.IMG_FULL.toString());
    }

    @Override
    public InputStream getImgFull(String pid) throws IOException, RepositoryException {
        return repositoryApi.getLatestVersionOfDatastream(pid, KnownDatastreams.IMG_FULL.toString());
    }

    @Override
    public boolean isImgThumbAvailable(String pid) throws IOException, RepositoryException {
        return repositoryApi.datastreamExists(pid, KnownDatastreams.IMG_THUMB.toString());
    }

    @Override
    public String getImgThumbMimetype(String pid) throws IOException, RepositoryException {
        return repositoryApi.getDatastreamMimetype(pid, KnownDatastreams.IMG_THUMB.toString());
    }

    @Override
    public InputStream getImgThumb(String pid) throws IOException, RepositoryException {
        return repositoryApi.getLatestVersionOfDatastream(pid, KnownDatastreams.IMG_THUMB.toString());
    }

    @Override
    public boolean isImgPreviewAvailable(String pid) throws IOException, RepositoryException {
        return repositoryApi.datastreamExists(pid, KnownDatastreams.IMG_PREVIEW.toString());
    }

    @Override
    public String getImgPreviewMimetype(String pid) throws IOException, RepositoryException {
        return repositoryApi.getDatastreamMimetype(pid, KnownDatastreams.IMG_PREVIEW.toString());
    }

    @Override
    public InputStream getImgPreview(String pid) throws IOException, RepositoryException {
        return repositoryApi.getLatestVersionOfDatastream(pid, KnownDatastreams.IMG_PREVIEW.toString());
    }

    @Override
    public boolean isAudioMp3Available(String pid) throws IOException, RepositoryException {
        return repositoryApi.datastreamExists(pid, KnownDatastreams.AUDIO_MP3.toString());
    }

    @Override
    public String getAudioMp3Mimetype(String pid) throws IOException, RepositoryException {
        return repositoryApi.getDatastreamMimetype(pid, KnownDatastreams.AUDIO_MP3.toString());
    }

    @Override
    public InputStream getAudioMp3(String pid) throws IOException, RepositoryException {
        return repositoryApi.getLatestVersionOfDatastream(pid, KnownDatastreams.AUDIO_MP3.toString());
    }

    @Override
    public boolean isAudioOggAvailable(String pid) throws IOException, RepositoryException {
        return repositoryApi.datastreamExists(pid, KnownDatastreams.AUDIO_OGG.toString());
    }

    @Override
    public String getAudioOggMimetype(String pid) throws IOException, RepositoryException {
        return repositoryApi.getDatastreamMimetype(pid, KnownDatastreams.AUDIO_OGG.toString());
    }

    @Override
    public InputStream getAudioOgg(String pid) throws IOException, RepositoryException {
        return repositoryApi.getLatestVersionOfDatastream(pid, KnownDatastreams.AUDIO_OGG.toString());
    }

    @Override
    public boolean isAudioWavAvailable(String pid) throws IOException, RepositoryException {
        return repositoryApi.datastreamExists(pid, KnownDatastreams.AUDIO_WAV.toString());
    }

    @Override
    public String getAudioWavMimetype(String pid) throws IOException, RepositoryException {
        return repositoryApi.getDatastreamMimetype(pid, KnownDatastreams.AUDIO_WAV.toString());
    }

    @Override
    public InputStream getAudioWav(String pid) throws IOException, RepositoryException {
        return repositoryApi.getLatestVersionOfDatastream(pid, KnownDatastreams.AUDIO_WAV.toString());
    }

    @Override
    public String getModel(String objectPid) throws RepositoryException, IOException, SolrServerException {
        Map<String, String> description = repositoryApi.getDescription(objectPid);
        String model = description.get("model");
        return model == null ? null : model.substring("model:".length());
    }

    @Override
    public Pair<RepositoryApi.Triplet, List<RepositoryApi.Triplet>> getParents(String objectPid) throws RepositoryException, IOException, SolrServerException {
        List<RepositoryApi.Triplet> pseudoparentTriplets = repositoryApi.getTripletSources(objectPid);
        RepositoryApi.Triplet ownParentTriplet = null;
        List<RepositoryApi.Triplet> fosterParentTriplets = new ArrayList<>();
        for (RepositoryApi.Triplet triplet : pseudoparentTriplets) {
            if (KrameriusRepositoryApi.isOwnRelation(triplet.relation)) {
                if (ownParentTriplet != null) {
                    throw new RepositoryException(String.format("found multiple own parent relations: %s and %s", ownParentTriplet, triplet));
                } else {
                    ownParentTriplet = triplet;
                }
            } else {
                fosterParentTriplets.add(triplet);
            }
        }
        return new Pair(ownParentTriplet, fosterParentTriplets);
    }

    @Override
    public Pair<List<RepositoryApi.Triplet>, List<RepositoryApi.Triplet>> getChildren(String objectPid) throws RepositoryException, IOException, SolrServerException {
        List<RepositoryApi.Triplet> pseudochildrenTriplets = repositoryApi.getTripletTargets(objectPid);
        List<RepositoryApi.Triplet> ownChildrenTriplets = new ArrayList<>();
        List<RepositoryApi.Triplet> fosterChildrenTriplets = new ArrayList<>();
        for (RepositoryApi.Triplet triplet : pseudochildrenTriplets) {
            if (triplet.target.startsWith("uuid:")) { //ignore hasDonator and other indexed relations, that are not binding two objects in repository
                if (KrameriusRepositoryApi.isOwnRelation(triplet.relation)) {
                    ownChildrenTriplets.add(triplet);
                } else {
                    fosterChildrenTriplets.add(triplet);
                }
            }
        }
        return new Pair(ownChildrenTriplets, fosterChildrenTriplets);
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
