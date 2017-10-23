package cz.incad.kramerius.fedora.om.impl;

import cz.incad.kramerius.fedora.om.RepositoryException;

/**
 * Created by pstastny on 10/11/2017.
 */
public interface RELSEXTSPARQLBuilderListener {

    public String inform(String path, String localName) throws RepositoryException;
}
