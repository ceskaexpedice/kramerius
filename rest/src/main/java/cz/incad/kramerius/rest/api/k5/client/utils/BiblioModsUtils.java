package cz.incad.kramerius.rest.api.k5.client.utils;

import cz.incad.kramerius.FedoraAccess;
import org.w3c.dom.Document;

import java.io.IOException;
import java.util.Map;

/**
 * Resposible for getting BIBLIO_MODS, storing it in the context and returning it
 */
public class BiblioModsUtils {

    public static final String BIBLIOMODS_PID_DOCUMENT_KEY = "bibliomods_pid_document";


    public static Document getBiblioModsDocument(String pid,
                                                 Map<String, Object> context, FedoraAccess fedoraAccess)
            throws IOException {
        String key = BIBLIOMODS_PID_DOCUMENT_KEY + "_" + pid;
        if (!context.containsKey(key)) {
            context.put(key, fedoraAccess.getBiblioMods(pid));
        }
        return (Document) context.get(key);
    }

}
