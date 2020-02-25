package cz.incad.kramerius.processes.new_api;

import java.util.List;

public interface ProcessManager {

    /**
     * Returns number of batches that match the filter
     *
     * @param filter
     * @return
     */
    public Integer getBatchesCount(Filter filter);

    /**
     * Returns processes in batches that match the filter
     *
     * @param filter
     * @param offset
     * @param limit
     * @return
     */
    public List<ProcessInBatch> getProcessesInBatches(Filter filter, int offset, int limit);

    /**
     * Returns users, that have ever scheduled any process
     *
     * @return
     */
    public List<ProcessOwner> getProcessesOwners();

    /**
     * Returns batch that is identified by it's first process id
     *
     * @param firstProcessId
     * @return
     */
    public Batch getBatchByFirstProcessId(int firstProcessId);

    /**
     * Deletes batch, i.e. all processes in batch
     *
     * @param batchToken
     * @return number of deleted processes
     */
    public int deleteBatchByBatchToken(String batchToken);

    /**
     * Saves process auth token, so that it can be used to schedule another process
     *
     * @param processId
     * @param processAuthToken
     */
    public void setProcessAuthToken(int processId, String processAuthToken);

    /**
     * @param processAuthToken
     * @return Process data, that will be used by new process (sibling) or null if process was not found by token,
     * either when process has already finished (and token has been dropped) or no such token ever existed
     */
    public ProcessAboutToScheduleSibling getProcessAboutToScheduleSiblingByAuthToken(String processAuthToken);

    public static class ProcessAboutToScheduleSibling {
        private final int id;
        private final String ownerId;
        private final String ownerName;
        private final String batchToken;

        public ProcessAboutToScheduleSibling(int id, String ownerId, String ownerName, String batchToken) {
            this.id = id;
            this.ownerId = ownerId;
            this.ownerName = ownerName;
            this.batchToken = batchToken;
        }

        public int getId() {
            return id;
        }

        public String getOwnerId() {
            return ownerId;
        }

        public String getOwnerName() {
            return ownerName;
        }

        public String getBatchToken() {
            return batchToken;
        }
    }


}

