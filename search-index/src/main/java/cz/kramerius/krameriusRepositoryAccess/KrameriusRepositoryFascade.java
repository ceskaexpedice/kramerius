package cz.kramerius.krameriusRepositoryAccess;

import cz.incad.kramerius.resourceindex.ResourceIndexException;
import cz.kramerius.adapters.RepositoryAccess;
import cz.kramerius.adapters.ProcessingIndex;
import cz.kramerius.shared.IoUtils;
import cz.kramerius.shared.Pair;
import org.dom4j.Document;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Set;

/**
 * This class is an adapter for Kramerius-specific operations over general repository (formally Fedora, newly Akubra) and Resource Index.
 */
public class KrameriusRepositoryFascade {

    private final RepositoryAccess repository;
    private final ProcessingIndex processingIndex;

    public KrameriusRepositoryFascade(RepositoryAccess repository, ProcessingIndex processingIndex) {
        this.repository = repository;
        this.processingIndex = processingIndex;
    }

    //OBJECT

    /*
     This is here only since we're implementing getObjectFoxml(pid):Document, that is missing in FedoraAccess.
     So client classes will probably use only this and not FedoraAccess directly
     */
    public boolean isObjectAvailable(String pid) throws IOException {
        return repository.isObjectAvailable(pid);
    }

    public Document getObjectFoxml(String pid, boolean nsAware) throws IOException {
        InputStream is = repository.getFoxml(pid);
        return IoUtils.inputstreamToDocument(is, nsAware);
    }

    //structure

    public String getModel(String pid) throws ResourceIndexException {
        return processingIndex.getModel(pid);
    }

    /**
     * @param pid
     * @return pids of own parent (first) and foster parents (second)
     * @throws ResourceIndexException
     */
    public Pair<String, Set<String>> getPidsOfParents(String pid) throws ResourceIndexException {
        return processingIndex.getPidsOfParents(pid);
    }

    /**
     * @param pid
     * @return pids of own children (first) and foster children (second) in an order matching RELS-EXT elements
     * @throws ResourceIndexException
     */
    public Pair<List<String>, List<String>> getPidsOfChildren(String pid) throws ResourceIndexException {
        return processingIndex.getPidsOfChildren(pid);
    }

    //RELS-EXT

    public boolean isRelsExtAvailable(String pid) throws IOException {
        return repository.isStreamAvailable(pid, KnownDatastreams.RELS_EXT);
    }

    public Document getRelsExt(String pid, boolean nsAware) throws IOException {
        InputStream is = repository.getDataStream(pid, KnownDatastreams.RELS_EXT);
        return IoUtils.inputstreamToDocument(is, nsAware);
    }

    //DC

    public boolean isDublinCoreAvailable(String pid) throws IOException {
        return repository.isStreamAvailable(pid, KnownDatastreams.METADATA_DC);
    }

    public Document getDublinCore(String pid, boolean nsAware) throws IOException {
        InputStream is = repository.getDataStream(pid, KnownDatastreams.METADATA_DC);
        return IoUtils.inputstreamToDocument(is, nsAware);
    }

    //MODS

    public boolean isModsAvailable(String pid) throws IOException {
        return repository.isStreamAvailable(pid, KnownDatastreams.METADATA_MODS);
    }

    public Document getMods(String pid, boolean nsAware) throws IOException {
        InputStream is = repository.getDataStream(pid, KnownDatastreams.METADATA_MODS);
        return IoUtils.inputstreamToDocument(is, nsAware);
    }

    //OCR

    public boolean isOcrTextAvailable(String pid) throws IOException {
        return repository.isStreamAvailable(pid, KnownDatastreams.OCR_TEXT);
    }

    public String getOcrText(String pid) throws IOException {
        InputStream is = repository.getDataStream(pid, KnownDatastreams.OCR_TEXT);
        String result = IoUtils.inputstreamToString(is);
        return result == null ? null : result.trim();
    }

    //IMAGE
    public String getImgFullMimetype(String pid) throws IOException {
        return repository.getDatastreamMimeType(pid, KnownDatastreams.IMG_FULL);
    }

    public InputStream getImgFull(String pid) throws IOException {
        InputStream is = repository.getDataStream(pid, KnownDatastreams.IMG_FULL);
        return is;
    }

    //AUDIO
    public boolean isAudioWavAvailable(String pid) throws IOException {
        return repository.isStreamAvailable(pid, KnownDatastreams.AUDIO_WAV);
    }

    public InputStream getAudioWav(String pid) throws IOException {
        InputStream is = repository.getDataStream(pid, KnownDatastreams.AUDIO_WAV);
        return is;
    }

    public boolean isAudioMp3Available(String pid) throws IOException {
        return repository.isStreamAvailable(pid, KnownDatastreams.AUDIO_MP3);
    }

    public InputStream getAudioMp3(String pid) throws IOException {
        InputStream is = repository.getDataStream(pid, KnownDatastreams.AUDIO_MP3);
        return is;
    }

    public boolean isAudioOggAvailable(String pid) throws IOException {
        return repository.isStreamAvailable(pid, KnownDatastreams.AUDIO_OGG);
    }

    public InputStream getAudioOgg(String pid) throws IOException {
        InputStream is = repository.getDataStream(pid, KnownDatastreams.AUDIO_OGG);
        return is;
    }

    public static class KnownDatastreams {
        public static final String RELS_EXT = "RELS-EXT";
        public static final String IMG_THUMB = "IMG_THUMB";
        public static final String IMG_FULL = "IMG_FULL";
        public static final String IMG_PREVIEW = "IMG_PREVIEW";
        public static final String OCR_ALTO = "ALTO";
        public static final String OCR_TEXT = "TEXT_OCR";
        public static final String METADATA_DC = "DC";
        public static final String METADATA_MODS = "BIBLIO_MODS";
        public static final String AUDIO_WAV = "WAV";
        public static final String AUDIO_MP3 = "MP3";
        public static final String AUDIO_OGG = "OGG";
        public static final String POLICY = "POLICY";
        public static final String MIGRATION = "MIGRATION";
    }


}
