package cz.incad.kramerius.fedora.om.impl;

import cz.incad.kramerius.fedora.om.RepositoryException;

/**
 * Listener is able to receive information about processing RELS-EXT
 * @see RELSEXTSPARQLBuilder
 */
public interface RELSEXTSPARQLBuilderListener {

    /**
     * Returns changed path
     * @param path Path parsed from RELS-EXT
     * @param localName Local name
     * @return
     * @throws RepositoryException
     */
    public String inform(String path, String localName) throws RepositoryException;

}
