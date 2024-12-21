package cz.incad.kramerius.fedora.impl.tmp;

import cz.incad.kramerius.fedora.om.repository.RepositoryException;

import java.io.IOException;

public interface SolrQueryService {

    <T> T query(SolrQueryParameters params, ResultMapper<T> mapper) throws RepositoryException, IOException, SolrServerException;

}
