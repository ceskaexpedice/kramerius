package cz.inovatika.kramerius.fedora.impl.tmp;

import cz.incad.kramerius.fedora.impl.tmp.ProcessingIndexQueryParameters;
import cz.incad.kramerius.fedora.impl.tmp.ResultMapper;
import cz.incad.kramerius.fedora.om.repository.RepositoryException;

import java.io.IOException;

public interface SolrQueryService {

    <T> T query(ProcessingIndexQueryParameters params, ResultMapper<T> mapper) throws RepositoryException, IOException, SolrServerException;

}
