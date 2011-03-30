package cz.incad.kramerius;

import java.util.Arrays;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Map;

import static cz.incad.kramerius.FedoraNamespaces.*;

import javax.xml.namespace.NamespaceContext;

public class FedoraNamespaceContext implements NamespaceContext {

    private static final Map<String, String> MAP_PREFIX2URI = new IdentityHashMap<String, String>();
    private static final Map<String, String> MAP_URI2PREFIX = new IdentityHashMap<String, String>();

    static {
        MAP_PREFIX2URI.put("mods", BIBILO_MODS_URI);
        MAP_PREFIX2URI.put("dc", DC_NAMESPACE_URI);
        MAP_PREFIX2URI.put("fedora-models", FEDORA_MODELS_URI);
        MAP_PREFIX2URI.put("kramerius", KRAMERIUS_URI);
        MAP_PREFIX2URI.put("rdf", RDF_NAMESPACE_URI);
        MAP_PREFIX2URI.put("oai", OAI_NAMESPACE_URI);
        MAP_PREFIX2URI.put("sparql", SPARQL_NAMESPACE_URI);
        MAP_PREFIX2URI.put("apia", FEDORA_ACCESS_NAMESPACE_URI);
        MAP_PREFIX2URI.put("apim", FEDORA_MANAGEMENT_NAMESPACE_URI);

        for (Map.Entry<String, String> entry : MAP_PREFIX2URI.entrySet()) {
            MAP_URI2PREFIX.put(entry.getValue(), entry.getKey());
        }
    }

    @Override
    public String getNamespaceURI(String arg0) {
        return MAP_PREFIX2URI.get(arg0.intern());
    }

    @Override
    public String getPrefix(String arg0) {
        return MAP_URI2PREFIX.get(arg0.intern());
    }

    @Override
    public Iterator getPrefixes(String arg0) {
        String prefixInternal = MAP_URI2PREFIX.get(arg0.intern());
        if (prefixInternal != null) {
            return Arrays.asList(prefixInternal).iterator();
        } else {
            return Collections.emptyList().iterator();
        }
    }
}
