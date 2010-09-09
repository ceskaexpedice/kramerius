package cz.incad.kramerius;

/**
 * Namespaces in fedora
 * @author pavels
 */
public interface FedoraNamespaces {
    /**
     * RDF namespace
     */
    public static final String RDF_NAMESPACE_URI = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";

    /**
     * Our ontology relationship namespace
     */
    public static final String ONTOLOGY_RELATIONSHIP_NAMESPACE_URI = "http://www.nsdl.org/ontologies/relationships#";

    /**
     * Dublin core namespace
     */
    public static final String DC_NAMESPACE_URI = "http://purl.org/dc/elements/1.1/";

    public static final String FEDORA_MODELS_URI = "info:fedora/fedora-system:def/model#";

    public static final String KRAMERIUS_URI = "http://www.nsdl.org/ontologies/relationships#";

    public static final String BIBILO_MODS_URI = "http://www.loc.gov/mods/v3";

    /**
     * OAI namespace
     */
    public static final String OAI_NAMESPACE_URI = "http://www.openarchives.org/OAI/2.0/";
}
