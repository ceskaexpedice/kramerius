package cz.incad.kramerius.fedora.impl.tmp;

import org.apache.solr.common.SolrDocument;

import java.util.List;

@FunctionalInterface
public interface ResultMapper<T> {

    T map(List<SolrDocument> documents, long totalRecords);

}