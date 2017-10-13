package cz.incad.kramerius.fedora.om.impl;

import cz.incad.kramerius.fedora.om.RepositoryException;

/**
 * Created by pstastny on 10/11/2017.
 */
public interface SPARQLBuilderListener {

    public String inform(String path) throws RepositoryException;
}
