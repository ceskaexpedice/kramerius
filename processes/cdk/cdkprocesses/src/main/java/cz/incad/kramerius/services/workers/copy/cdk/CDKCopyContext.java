package cz.incad.kramerius.services.workers.copy.cdk;


import cz.incad.kramerius.services.workers.copy.cdk.model.CDKWorkerIndexedItem;
import cz.inovatika.kramerius.services.iterators.IterationItem;
import cz.incad.kramerius.services.workers.copy.cdk.model.CDKExistingConflictFeederItem;
import cz.incad.kramerius.services.workers.copy.cdk.model.CDKNewConflictFeederItem;
import cz.inovatika.kramerius.services.workers.copy.CopyMigrationIndexFeederContext;

import java.util.ArrayList;
import java.util.List;

public class CDKCopyContext extends CopyMigrationIndexFeederContext<CDKWorkerIndexedItem> {

    /** Documents that are already indexed (i.e., present both in source and in local index). */
    //private List<CDKWorkerIndexedItem> alreadyIndexed;

    /**
     * Existing indexing conflicts discovered during the initial iteration.
     * These are documents where a single PID is associated with multiple {@code compositeId}s in the existing index.
     */
    private List<CDKExistingConflictFeederItem> existingConflictRecords  = new ArrayList<>();


    /**
     * New conflicts identified dynamically during the indexing process.
     * These arise when a PID already exists in the index and a new model tries to use the same PID
     * with a different {@code compositeId}, indicating a future conflict.
     */
    private List<CDKNewConflictFeederItem> newConflictRecords = new ArrayList<>();

    /**
     * Constructs a replication context with indexed and not-indexed document data.
     *
     * @param alreadyIndexed List of already indexed documents.
     * @param notIndexed List of PIDs of documents that have not been indexed yet.
     */
    public CDKCopyContext(List<IterationItem> allBachItems, List<CDKWorkerIndexedItem> alreadyIndexed, List<CDKExistingConflictFeederItem> conflictRecords, List<IterationItem> notIndexed) {
        super(allBachItems, alreadyIndexed, notIndexed);
        this.workerIndexedItems = alreadyIndexed;
        this.notIndexed = notIndexed;
        this.existingConflictRecords = conflictRecords;
    }


    public CDKWorkerIndexedItem getAlreadyIndexedAsItem(String p) {
        CDKWorkerIndexedItem indexedItem = this.workerIndexedItems.stream().filter((i) -> {
            String iPid =  i.getPid();
            return iPid.equals(p);
        }).findFirst().orElse(null);
        return indexedItem;
    }

    /**
     * Returns the list of existing conflicts detected during replication.
     * Each conflict indicates a PID with multiple distinct {@code compositeId} values.
     *
     * @return List of {@link CDKExistingConflictFeederItem} instances, or {@code null} if not initialized.
     */
    public List<CDKExistingConflictFeederItem> getExistingConflictRecords() {
        return existingConflictRecords;
    }

    /**
     * @return List of new conflicts detected during the current indexing process.
     */
    public List<CDKNewConflictFeederItem> getNewConflictRecords() {
        return newConflictRecords;
    }

    /**
     * Sets the list of new conflicts detected during the current indexing process.
     *
     * @param newConflictRecords The new conflict records to set.
     */
    public void setNewConflictRecords(List<CDKNewConflictFeederItem> newConflictRecords) {
        this.newConflictRecords = newConflictRecords;
    }

    public void addConflictRecord(CDKNewConflictFeederItem newConflictRecord) {
        this.newConflictRecords.add(newConflictRecord);
    }

    public void removeConflictRecord(CDKNewConflictFeederItem newConflictRecord) {
        this.newConflictRecords.remove(newConflictRecord);
    }
}