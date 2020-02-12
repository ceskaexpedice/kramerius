package cz.incad.kramerius.processes.new_api;

import java.util.List;

public interface ProcessManager {

    /**
     * Returns number of batches that match the filter
     * @param filter
     * @return
     */
    public Integer getBatchesCount(Filter filter);

    /**
     * Returns processes in batches that match the filter
     * @param filter
     * @param offset
     * @param limit
     * @return
     */
    public List<ProcessInBatch> getProcessesInBatches(Filter filter, int offset, int limit);

    /**
     * Returns users, that have ever scheduled any process
     * @return
     */
    public List<ProcessOwner> getProcessesOwners();

    /**
     * Returns batch that is identified by it's first process id
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

}
