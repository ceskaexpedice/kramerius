package cz.incad.kramerius.rest.apiNew.admin.v70.reharvest;

import java.util.List;

public class AlreadyRegistedPidsException extends Exception {
    
    private List<String> pids;
    
    public AlreadyRegistedPidsException(List<String> pids) {
        super();
        this.pids = pids;
    }

    public List<String> getPids() {
        return pids;
    }
}
