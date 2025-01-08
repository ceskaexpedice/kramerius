package cz.inovatika.kramerius.fedora.impl.tmp;

import cz.incad.kramerius.fedora.om.repository.RepositoryException;
import cz.incad.kramerius.utils.java.Pair;
import cz.inovatika.kramerius.fedora.ProcessingIndexQueryParameters;
import cz.inovatika.kramerius.fedora.ProcessingIndexResultMapper;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.SolrDocument;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class SolrQueryServiceImpl implements SolrQueryService {

    @Override
    public <T> T query(ProcessingIndexQueryParameters params, ProcessingIndexResultMapper<T> mapper)
            throws RepositoryException, IOException, SolrServerException {

        // Build query string from parameters
        StringBuilder queryBuilder = new StringBuilder();
        params.getFilters().forEach((field, value) ->
                queryBuilder.append(field).append(":").append(value).append(" AND ")
        );
        if (queryBuilder.length() > 0) {
            queryBuilder.setLength(queryBuilder.length() - 5); // Remove trailing " AND "
        }

        // Fetch results from SOLR
        org.apache.commons.lang3.tuple.Pair<Long, List<SolrDocument>> cp =
                akubraRepositoryImpl.getProcessingIndexFeeder().getPageSortedByTitle(
                        queryBuilder.toString(),
                        params.getRows(),
                        params.getPageIndex(),
                        params.getFieldsToFetch()
                );

        // Use the provided mapper to convert results
        return mapper.map(cp.getRight(), cp.getLeft());
    }

    public static void main(String[] args) {
        // Fetching a List of Strings:
        ProcessingIndexQueryParameters params = new ProcessingIndexQueryParameters.Builder()
                .queryString("type:description AND model:model:page")
                .sortField("title")
                .ascending(true)
                .rows(10)
                .pageIndex(0)
                .fieldsToFetch(List.of("source"))
                .build();

        List<String> pids = solrQueryService.query(params, (docs, total) ->
                docs.stream()
                        .map(doc -> doc.getFieldValue("source").toString())
                        .collect(Collectors.toList())
        );

        // Fetching a Pair (Total Records, List of Strings):
        Pair<Long, List<String>> result = solrQueryService.query(params, (docs, total) ->
                new Pair<>(total, docs.stream()
                        .map(doc -> doc.getFieldValue("source").toString())
                        .collect(Collectors.toList()))
        );

        // Fetching Custom Triplets:
        List<Triplet<String, String, String>> triplets = solrQueryService.query(params, (docs, total) ->
                docs.stream()
                        .map(doc -> new Triplet<>(
                                doc.getFieldValue("source").toString(),
                                doc.getFieldValue("title").toString(),
                                doc.getFieldValue("model").toString()))
                        .collect(Collectors.toList())
        );

    }
}