package cz.inovatika.kramerius.fedora.impl;

import cz.incad.kramerius.fedora.ProcessingIndexAccess;
import cz.incad.kramerius.fedora.RepositoryAccess;
import cz.incad.kramerius.fedora.impl.tmp.ProcessingIndexQueryParameters;
import cz.incad.kramerius.fedora.impl.tmp.ResultMapper;
import cz.incad.kramerius.fedora.om.repository.RepositoryException;
import cz.incad.kramerius.utils.StringUtils;
import cz.incad.kramerius.utils.java.Pair;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.SolrDocument;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class ProcessingIndexAccessImpl implements ProcessingIndexAccess {

    // 1 ------------- description + model
    @Override
    public Pair<Long, List<String>> getPidsOfObjectsByModel(String model, int rows, int pageIndex) throws RepositoryException, IOException, SolrServerException {
        String query = String.format("type:description AND model:%s", "model\\:" + model); //prvni "model:" je filtr na solr pole, druhy "model:" je hodnota pole, coze  uprime zbytecne
        ProcessingIndexQueryParameters params = new ProcessingIndexQueryParameters.Builder()
                .queryString(query)
                .sortField("title")
                .ascending(true)
                .rows(rows)
                .pageIndex(pageIndex)
                .fieldsToFetch(List.of("source"))
                .build();
        org.apache.commons.lang3.tuple.Pair<Long, List<SolrDocument>> cp =
                akubraRepositoryImpl.getProcessingIndexFeeder().getPageSortedByTitle(
                        params.getQueryString(),
                        params.getRows(),
                        params.getPageIndex(),
                        params.getFieldsToFetch()
                );
        return new ResultMapper<Pair<Long, List<String>>>() {
            @Override
            public Pair<Long, List<String>> map(List<SolrDocument> documents, long totalRecords) {
                List<String> pids = documents.stream().map(sd -> {
                    Object fieldValue = sd.getFieldValue("source");
                    return fieldValue.toString();
                }).collect(Collectors.toList());
                return new Pair<>(totalRecords, pids);
            }
        }.map(cp.getRight(), cp.getLeft());

        // ---------- original---------------------------
        String query = String.format("type:description AND model:%s", "model\\:" + model); //prvni "model:" je filtr na solr pole, druhy "model:" je hodnota pole, coze  uprime zbytecne
        org.apache.commons.lang3.tuple.Pair<Long, List<SolrDocument>> cp = akubraRepositoryImpl.getProcessingIndexFeeder().getPageSortedByTitle(query, rows, pageIndex, Arrays.asList("source"));
        Long numberOfRecords = cp.getLeft();
        List<String> pids = cp.getRight().stream().map(sd -> {
            Object fieldValue = sd.getFieldValue("source");
            return fieldValue.toString();
        }).collect(Collectors.toList());
        return new Pair<>(numberOfRecords, pids);
    }
    // 1 ---------------------------
    @Override
    public Pair<Long, List<String>> getPidsOfObjectsByModel(String model, String titlePrefix, int rows, int pageIndex) throws RepositoryException, IOException, SolrServerException {
        String query = String.format("type:description AND model:%s", "model\\:" + model);
        if (StringUtils.isAnyString(titlePrefix)) {
            query = String.format("type:description AND model:%s AND title_edge:%s", "model\\:" + model, titlePrefix); //prvni "model:" je filtr na solr pole, druhy "model:" je hodnota pole, coze  uprime zbytecne
        }
        ProcessingIndexQueryParameters params = new ProcessingIndexQueryParameters.Builder()
                .queryString(query)
                .sortField("title")
                .ascending(true)
                .rows(rows)
                .pageIndex(pageIndex)
                .fieldsToFetch(List.of("source"))
                .build();
        org.apache.commons.lang3.tuple.Pair<Long, List<SolrDocument>> cp =
                akubraRepositoryImpl.getProcessingIndexFeeder().getPageSortedByTitle(
                        params.getQueryString(),
                        params.getRows(),
                        params.getPageIndex(),
                        params.getFieldsToFetch()
                );
        return new ResultMapper<Pair<Long, List<String>>>() {
            @Override
            public Pair<Long, List<String>> map(List<SolrDocument> documents, long totalRecords) {
                List<String> pids = documents.stream().map(sd -> {
                    Object fieldValue = sd.getFieldValue("source");
                    return fieldValue.toString();
                }).collect(Collectors.toList());
                return new Pair<>(totalRecords, pids);
            }
        }.map(cp.getRight(), cp.getLeft());

        // ---------- original---------------------------
        String query = String.format("type:description AND model:%s", "model\\:" + model);
        if (StringUtils.isAnyString(titlePrefix)) {
            query = String.format("type:description AND model:%s AND title_edge:%s", "model\\:" + model, titlePrefix); //prvni "model:" je filtr na solr pole, druhy "model:" je hodnota pole, coze  uprime zbytecne
        }
        org.apache.commons.lang3.tuple.Pair<Long, List<SolrDocument>> cp = akubraRepositoryImpl.getProcessingIndexFeeder().getPageSortedByTitle(query, rows, pageIndex, Arrays.asList("source"));
        Long numberOfRecords = cp.getLeft();
        List<String> pids = cp.getRight().stream().map(sd -> {
            Object fieldValue = sd.getFieldValue("source");
            return fieldValue.toString();
        }).collect(Collectors.toList());
        return new Pair<>(numberOfRecords, pids);
    }
    // 2 ----------------------------------
    @Override
    public RepositoryAccess.TitlePidPairs getPidsOfObjectsWithTitlesByModel(String model, boolean ascendingOrder, int offset, int limit) throws RepositoryException, IOException, SolrServerException {
        List<Pair<String, String>> titlePidPairs = new ArrayList<>();
        String query = String.format("type:description AND model:%s", "model\\:" + model); //prvni "model:" je filtr na solr pole, druhy "model:" je hodnota pole, coze je mozna zbytecne (ten prefix)
        akubraRepositoryImpl.getProcessingIndexFeeder().iterateSectionOfProcessingSortedByTitle(query, ascendingOrder, offset, limit, (doc) -> {
            Object fieldPid = doc.getFieldValue("source");
            Object fieldTitle = doc.getFieldValue("dc.title");
            String pid = null;
            String title = null;
            if (fieldPid != null) {
                pid = fieldPid.toString();
            }
            if (fieldTitle != null) {
                title = fieldTitle.toString().trim();
            }
            titlePidPairs.add(new Pair(title, pid));
        });
        RepositoryAccess.TitlePidPairs result = new RepositoryAccess.TitlePidPairs();
        result.titlePidPairs = titlePidPairs;
        return result;
    }
    // 3 ------------------------------------
    @Override
    public RepositoryAccess.TitlePidPairs getPidsOfObjectsWithTitlesByModelWithCursor(String model, boolean ascendingOrder, String cursor, int limit) throws RepositoryException, IOException, SolrServerException {
        List<Pair<String, String>> titlePidPairs = new ArrayList<>();
        String query = String.format("type:description AND model:%s", "model\\:" + model); //prvni "model:" je filtr na solr pole, druhy "model:" je hodnota pole, coze je mozna zbytecne (ten prefix)
        String nextCursorMark = akubraRepositoryImpl.getProcessingIndexFeeder().iterateSectionOfProcessingSortedByTitleWithCursor(query, ascendingOrder, cursor, limit, (doc) -> {
            Object fieldPid = doc.getFieldValue("source");
            Object fieldTitle = doc.getFieldValue("dc.title");
            String pid = null;
            String title = null;
            if (fieldPid != null) {
                pid = fieldPid.toString();
            }
            if (fieldTitle != null) {
                title = fieldTitle.toString().trim();
            }
            titlePidPairs.add(new Pair(title, pid));
        });
        RepositoryAccess.TitlePidPairs result = new RepositoryAccess.TitlePidPairs();
        result.titlePidPairs = titlePidPairs;
        result.nextCursorMark = nextCursorMark;
        return result;
    }
    // 4 -----------------------------------
    @Override
    public Map<String, String> getDescription(String objectPid) throws RepositoryException, IOException, SolrServerException {
        Map<String, String> description = new HashMap<>();
        String query = String.format("type:description AND source:%s", objectPid.replace(":", "\\:"));
        akubraRepositoryImpl.getProcessingIndexFeeder().iterateProcessingSortedByPid(query, (doc) -> { //iterating, but there should only be one hit
            for (String name : doc.getFieldNames()) {
                description.put(name, doc.getFieldValue(name).toString());
            }
        });
        return description;
    }
    // 4 ----------------------------------
    @Override
    public List<String> getPidsOfAllObjects() throws RepositoryException, IOException, SolrServerException {
        List<String> pids = new ArrayList<>();
        //TODO: offset, limit
        String query = "type:description";
        akubraRepositoryImpl.getProcessingIndexFeeder().iterateProcessingSortedByPid(query, (doc) -> {
            Object fieldValue = doc.getFieldValue("source");
            if (fieldValue != null) {
                String valueStr = fieldValue.toString();
                pids.add(valueStr);
            }
        });
        return pids;
    }
    // 5 ---------------------------------------------------------------
    @Override
    public List<String> getTripletTargets(String sourcePid, String relation) throws RepositoryException, IOException, SolrServerException {
        List<String> pids = new ArrayList<>();
        String query = String.format("source:%s AND relation:%s", sourcePid.replace(":", "\\:"), relation);
        akubraRepositoryImpl.getProcessingIndexFeeder().iterateProcessingSortedByIndexationDate(query, true, (doc) -> {
            Object fieldValue = doc.getFieldValue("targetPid");
            if (fieldValue != null) {
                String valueStr = fieldValue.toString();
                pids.add(valueStr);
            }
        });
        return pids;
    }
    // 5 ----------------------------------------------
    @Override
    public List<RepositoryAccess.Triplet> getTripletTargets(String sourcePid) throws RepositoryException, IOException, SolrServerException {
        List<RepositoryAccess.Triplet> triplets = new ArrayList<>();
        String query = String.format("source:%s", sourcePid.replace(":", "\\:"));
        akubraRepositoryImpl.getProcessingIndexFeeder().iterateProcessingSortedByIndexationDate(query, true, (doc) -> {
            Object targetPid = doc.getFieldValue("targetPid");
            Object relation = doc.getFieldValue("relation");
            if (targetPid != null && relation != null) {
                triplets.add(new RepositoryAccess.Triplet(sourcePid, relation.toString(), targetPid.toString()));
            }
        });
        return triplets;
    }
    // 5 ------------------------------------------------
    @Override
    public Pair<List<RepositoryApi.Triplet>, List<RepositoryApi.Triplet>> getChildren(String objectPid) throws RepositoryException, IOException, SolrServerException {
        List<RepositoryApi.Triplet> pseudochildrenTriplets = getTripletTargets(objectPid);
        List<RepositoryApi.Triplet> ownChildrenTriplets = new ArrayList<>();
        List<RepositoryApi.Triplet> fosterChildrenTriplets = new ArrayList<>();
        for (RepositoryApi.Triplet triplet : pseudochildrenTriplets) {
            if (triplet.target.startsWith("uuid:")) { //ignore hasDonator and other indexed relations, that are not binding two objects in repository
                if (KrameriusRepositoryApi.isOwnRelation(triplet.relation)) {
                    ownChildrenTriplets.add(triplet);
                } else {
                    fosterChildrenTriplets.add(triplet);
                }
            }
        }
        return new Pair(ownChildrenTriplets, fosterChildrenTriplets);
    }
    // 5 -----------------------------------------------
    @Override
    public List<String> getPidsOfItemsInCollection(String collectionPid) throws RepositoryException, IOException, SolrServerException {
        return getTripletTargets(collectionPid, RepositoryAccess.KnownRelations.CONTAINS.toString());
    }
    // 6 --------------------------------------------------
    @Override
    public List<String> getTripletSources(String relation, String targetPid) throws RepositoryException, IOException, SolrServerException {
        List<String> pids = new ArrayList<>();
        String query = String.format("relation:%s AND targetPid:%s", relation, targetPid.replace(":", "\\:"));
        akubraRepositoryImpl.getProcessingIndexFeeder().iterateProcessingSortedByIndexationDate(query, true, (doc) -> {
            Object fieldValue = doc.getFieldValue("source");
            if (fieldValue != null) {
                String valueStr = fieldValue.toString();
                pids.add(valueStr);
            }
        });
        return pids;
    }
    // 6 -----------------------------------------------
    @Override
    public List<RepositoryAccess.Triplet> getTripletSources(String targetPid) throws RepositoryException, IOException, SolrServerException {
        List<RepositoryAccess.Triplet> triplets = new ArrayList<>();
        String query = String.format("targetPid:%s", targetPid.replace(":", "\\:"));
        akubraRepositoryImpl.getProcessingIndexFeeder().iterateProcessingSortedByIndexationDate(query, true, (doc) -> {
            Object sourcePid = doc.getFieldValue("source");
            Object relation = doc.getFieldValue("relation");
            if (sourcePid != null && relation != null) {
                triplets.add(new RepositoryAccess.Triplet(sourcePid.toString(), relation.toString(), targetPid));
            }
        });
        return triplets;
    }
    // 6 ----------------------------------------------
    @Override
    public List<String> getPidsOfCollectionsContainingItem(String itemPid) throws RepositoryException, IOException, SolrServerException {
        return getTripletSources(RepositoryAccess.KnownRelations.CONTAINS.toString(), itemPid);
    }
    // 6 -------------------------------------
    @Override
    public Pair<RepositoryApi.Triplet, List<RepositoryApi.Triplet>> getParents(String objectPid) throws RepositoryException, IOException, SolrServerException {
        List<RepositoryApi.Triplet> pseudoparentTriplets = getTripletSources(objectPid);
        RepositoryApi.Triplet ownParentTriplet = null;
        List<RepositoryApi.Triplet> fosterParentTriplets = new ArrayList<>();
        for (RepositoryApi.Triplet triplet : pseudoparentTriplets) {
            if (KrameriusRepositoryApi.isOwnRelation(triplet.relation)) {
                if (ownParentTriplet != null) {
                    throw new RepositoryException(String.format("found multiple own parent relations: %s and %s", ownParentTriplet, triplet));
                } else {
                    ownParentTriplet = triplet;
                }
            } else {
                fosterParentTriplets.add(triplet);
            }
        }
        return new Pair(ownParentTriplet, fosterParentTriplets);
    }
    // 7 ------------------------------------------
    @Override
    public List<String> getPidsOfObjectsByModel(String model) throws RepositoryException, IOException, SolrServerException {
        List<String> pids = new ArrayList<>();
        //TODO: offset, limit
        String query = String.format("type:description AND model:%s", "model\\:" + model); //prvni "model:" je filtr na solr pole, druhy "model:" je hodnota pole, coze  uprime zbytecne
        akubraRepositoryImpl.getProcessingIndexFeeder().iterateProcessingSortedByTitle(query, (doc) -> {
            Object fieldValue = doc.getFieldValue("source");
            if (fieldValue != null) {
                String valueStr = fieldValue.toString();
                pids.add(valueStr);
            }
        });
        return pids;
    }
    // 7 ---------------------------------------------
    public List<String> getPidsByCriteria(Map<String, String> filters, String sortField, boolean ascending)
            throws RepositoryException, IOException, SolrServerException {
        List<String> pids = new ArrayList<>();
        StringBuilder queryBuilder = new StringBuilder();

        // Build the query from filters
        filters.forEach((field, value) -> queryBuilder.append(field).append(":").append(value).append(" AND "));
        if (queryBuilder.length() > 0) {
            queryBuilder.setLength(queryBuilder.length() - 5); // Remove trailing " AND "
        }

        // Sort query string
        String sortClause = sortField != null ? String.format("sort:%s %s", sortField, ascending ? "ASC" : "DESC") : "";

        akubraRepositoryImpl.getProcessingIndexFeeder().iterateProcessingSortedByTitle(
                queryBuilder.toString(),
                (doc) -> {
                    Object fieldValue = doc.getFieldValue("source");
                    if (fieldValue != null) {
                        String valueStr = fieldValue.toString();
                        pids.add(valueStr);
                    }
                }
        );

        return pids;
    }

}
