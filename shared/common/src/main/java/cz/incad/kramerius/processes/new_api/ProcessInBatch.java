package cz.incad.kramerius.processes.new_api;

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

}


