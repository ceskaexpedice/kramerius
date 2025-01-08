package cz.inovatika.kramerius.fedora;

import org.apache.solr.common.SolrDocument;

import java.util.List;

@FunctionalInterface
public interface ProcessingIndexResultMapper<T> {

    T map(List<SolrDocument> documents, long totalRecords);

}