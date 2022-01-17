package cz.incad.kramerius.processes.new_api;

import java.time.LocalDateTime;

public class Filter {
    public String owner;
    public LocalDateTime from;
    public LocalDateTime until;
    public Integer stateCode;

    public boolean isEmpty() {
        return owner == null &&
                from == null &&
                until == null &&
                stateCode == null
                ;
    }

}
