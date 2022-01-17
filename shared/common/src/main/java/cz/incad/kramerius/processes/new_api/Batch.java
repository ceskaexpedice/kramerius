package cz.incad.kramerius.processes.new_api;

import java.time.LocalDateTime;

public class Batch {
    public String token;
    public String firstProcessId;
    public Integer stateCode;
    public LocalDateTime planned;
    public LocalDateTime started;
    public LocalDateTime finished;
    public String ownerId;
    public String ownerName;
    public int size;
}
