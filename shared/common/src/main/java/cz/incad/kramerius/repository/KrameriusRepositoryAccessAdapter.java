package cz.incad.kramerius.repository;

import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.resourceindex.IResourceIndex;
import cz.incad.kramerius.resourceindex.ResourceIndexException;
import org.dom4j.Document;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * This class is an adapter for Kramerius-specific operations over general repository (formally Fedora, newly Akubra) and Resource Index.
 * deprecated, use KrameriusRepositoryApi instead
 */
@Deprecated
public class KrameriusRepositoryAccessAdapter {

    private final FedoraAccess repository;
    private final IResourceIndex resourceIndex;

    public KrameriusRepositoryAccessAdapter(FedoraAccess repository, IResourceIndex resourceIndex) {
        this.repository = repository;
        this.resourceIndex = resourceIndex;
    }

    //OBJECT

    /*
     This is here only since we're implementing getObjectFoxml(pid):Document, that is missing in FedoraAccess.
     So client classes will probably use only this and not FedoraAccess directly
     */
    public boolean isObjectAvailable(String pid) throws IOException {
        return repository.isObjectAvailable(pid);
    }

    //TODO: mozna metody pro ziskani konkretnich properties rovnou tady, pak uz by snad nemelo byt potreba getObjectFoxml

    public Document getObjectFoxml(String pid, boolean nsAware) throws IOException {
        //TODO: vyjasnit parametr archive od FedoraAccess.getFoxml(pid, archive) - je to implementacni detail, jehoz vyznam je nejasny a zneprehlenduje API (FedoraAccess)
        InputStream is = repository.getFoxml(pid, false);
        return Utils.inputstreamToDocument(is, nsAware);
    }

    //structure

    /**
     * @param pid
     * @return pids of all parents, both own-parent and foster-parents in one list
     * @throws ResourceIndexException
     */
    public List<String> getPidsOfParents(String pid) throws ResourceIndexException {
        return resourceIndex.getParentsPids(pid);
    }

    //RELS-EXT

    public boolean isRelsExtAvailable(String pid) throws IOException {
        return repository.isStreamAvailable(pid, KnownDatastreams.RELS_EXT_STREAM);
    }

    public Document getRelsExt(String pid, boolean nsAware) throws IOException {
        InputStream is = repository.getDataStream(pid, KnownDatastreams.RELS_EXT_STREAM);
        return Utils.inputstreamToDocument(is, nsAware);
    }

    //DC

    public boolean isDublinCoreAvailable(String pid) throws IOException {
        return repository.isStreamAvailable(pid, KnownDatastreams.DC_STREAM);
    }

    public Document getDublinCore(String pid, boolean nsAware) throws IOException {
        InputStream is = repository.getDataStream(pid, KnownDatastreams.DC_STREAM);
        return Utils.inputstreamToDocument(is, nsAware);
    }

    //MODS

    public boolean isModsAvailable(String pid) throws IOException {
        return repository.isStreamAvailable(pid, KnownDatastreams.BIBLIO_MODS_STREAM);
    }

    public Document getMods(String pid, boolean nsAware) throws IOException {
        InputStream is = repository.getDataStream(pid, KnownDatastreams.BIBLIO_MODS_STREAM);
        return Utils.inputstreamToDocument(is, nsAware);
    }

    /*public void setMods(String pid, Document modsDoc) throws IOException {
        //TODO: implement, ale ne tady
    }*/

    public boolean isOcrTextAvailable(String pid) throws IOException {
        return repository.isStreamAvailable(pid, KnownDatastreams.TEXT_OCR_STREAM);
    }

    public String getOcrText(String pid) throws IOException {
        InputStream is = repository.getDataStream(pid, KnownDatastreams.TEXT_OCR_STREAM);
        String result = Utils.inputstreamToString(is);
        return result == null ? null : result.trim();
    }

    public static class KnownDatastreams {
        public static final String RELS_EXT_STREAM = "RELS-EXT";
        public static final String IMG_THUMB_STREAM = "IMG_THUMB";
        public static final String IMG_FULL_STREAM = "IMG_FULL";
        public static final String IMG_PREVIEW_STREAM = "IMG_PREVIEW";
        public static final String ALTO_STREAM = "ALTO";
        public static final String DC_STREAM = "DC";
        public static final String BIBLIO_MODS_STREAM = "BIBLIO_MODS";
        public static final String TEXT_OCR_STREAM = "TEXT_OCR";
        public static final String MP3_STREAM = "MP3";
        public static final String OGG_STREAM = "OGG";
        public static final String WAV_STREAM = "WAV";
        public static final String MIGRATION_STREAM = "MIGRATION";
        public static final String POLICY_STREAM = "POLICY";
    }

}
