package cz.incad.kramerius.services.workers.replicate;


import cz.inovatika.kramerius.services.iterators.IterationItem;
import cz.incad.kramerius.services.workers.replicate.records.ExistingConflictRecord;
import cz.incad.kramerius.services.workers.replicate.records.IndexedRecord;
import cz.incad.kramerius.services.workers.replicate.records.NewConflictRecord;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Represents the context of a replication operation.
 * <p>
 * This class stores the state during the replication process, distinguishing between:
 * <ul>
 *     <li>Documents that are already indexed (available in the target index as {@link IndexedRecord}).</li>
 *     <li>Documents that are not yet indexed (found in the source library but missing in the index).</li>
 *     <li>Existing conflicts, where a single PID is already indexed with multiple {@code compositeId} values.</li>
 *     <li>Newly detected conflicts during indexing — when two different models share the same PID.</li>
 * </ul>
 */
public class CDKReplicateContext {

    /** Documents that are already indexed (i.e., present both in source and in local index). */
    private List<IndexedRecord> alreadyIndexed;

    /** Documents found in the source library but not yet indexed locally. */
    private List<IterationItem> notIndexed= new ArrayList<>();

    /**
     * Existing indexing conflicts discovered during the initial iteration.
     * These are documents where a single PID is associated with multiple {@code compositeId}s in the existing index.
     */
    private List<ExistingConflictRecord> existingConflictRecords  = new ArrayList<>();


    /**
     * New conflicts identified dynamically during the indexing process.
     * These arise when a PID already exists in the index and a new model tries to use the same PID
     * with a different {@code compositeId}, indicating a future conflict.
     */
    private List<NewConflictRecord> newConflictRecords = new ArrayList<>();

    /**
     * Constructs a replication context with indexed and not-indexed document data.
     *
     * @param alreadyIndexed List of already indexed documents.
     * @param notIndexed List of PIDs of documents that have not been indexed yet.
     */
    public CDKReplicateContext(List<IndexedRecord> alreadyIndexed, List<ExistingConflictRecord> conflictRecords, List<IterationItem> notIndexed) {
        this.alreadyIndexed = alreadyIndexed;
        this.notIndexed = notIndexed;
        this.existingConflictRecords = conflictRecords;
    }

    /**
     * Returns the list of already indexed documents.
     *
     * @return List of IndexedRecord objects.
     */
    public List<IndexedRecord> getAlreadyIndexed() {
        return alreadyIndexed;
    }

    /**
     * Returns the list of already indexed documents as a map, keyed by PID.
     *
     * @return Map of PID to IndexedRecord.
     */
    public Map<String, IndexedRecord> getAlreadyIndexedAsMap() {
        return alreadyIndexed.stream()
                .collect(Collectors.toMap(IndexedRecord::getPid, r -> r));
    }

    /**
     * Returns the list of PIDs of documents that have not yet been indexed.
     *
     * @return List of not indexed PIDs.
     */
    public List<IterationItem> getNotIndexed() {
        return notIndexed;
    }


    /**
     * Returns the list of existing conflicts detected during replication.
     * Each conflict indicates a PID with multiple distinct {@code compositeId} values.
     *
     * @return List of {@link ExistingConflictRecord} instances, or {@code null} if not initialized.
     */
    public List<ExistingConflictRecord> getExistingConflictRecords() {
        return existingConflictRecords;
    }

    /**
     * @return List of new conflicts detected during the current indexing process.
     */
    public List<NewConflictRecord> getNewConflictRecords() {
        return newConflictRecords;
    }

    /**
     * Sets the list of new conflicts detected during the current indexing process.
     *
     * @param newConflictRecords The new conflict records to set.
     */
    public void setNewConflictRecords(List<NewConflictRecord> newConflictRecords) {
        this.newConflictRecords = newConflictRecords;
    }

    public void addConflictRecord(NewConflictRecord newConflictRecord) {
        this.newConflictRecords.add(newConflictRecord);
    }

    public void removeConflictRecord(NewConflictRecord newConflictRecord) {
        this.newConflictRecords.remove(newConflictRecord);
    }
}