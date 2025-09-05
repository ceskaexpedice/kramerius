package cz.incad.kramerius.rest.apiNew.admin.v70.processes.mapper;

import java.time.LocalDateTime;

public class ProcessInBatch {

    public String batchToken;
    public String batchId;
    public Integer batchStateCode;
    public LocalDateTime batchPlanned;
    public LocalDateTime batchStarted;
    public LocalDateTime batchFinished;
    public String batchOwnerId;
    public String batchOwnerName;
    public int batchSize;

    public String processId;
    public String processUuid;
    public String processDefid;
    public String processName;
    public Integer processStateCode;
    public LocalDateTime processPlanned;
    public LocalDateTime processStarted;
    public LocalDateTime processFinished;

    @Override
    public String toString() {
        return "ProcessInBatch{" +
                "batchToken='" + batchToken + '\'' +
                ", batchId='" + batchId + '\'' +
                ", batchStateCode=" + batchStateCode +
                ", batchPlanned=" + batchPlanned +
                ", batchStarted=" + batchStarted +
                ", batchFinished=" + batchFinished +
                ", batchOwnerId='" + batchOwnerId + '\'' +
                ", batchOwnerName='" + batchOwnerName + '\'' +
                ", batchSize=" + batchSize +
                ", processId='" + processId + '\'' +
                ", processUuid='" + processUuid + '\'' +
                ", processDefid='" + processDefid + '\'' +
                ", processName='" + processName + '\'' +
                ", processStateCode=" + processStateCode +
                ", processPlanned=" + processPlanned +
                ", processStarted=" + processStarted +
                ", processFinished=" + processFinished +
                '}';
    }
}


