package cz.incad.kramerius.service;

import java.io.OutputStream;

public interface METSService {
    
    /**
     * Export the Kramerius objects  with given PID, in METS format
     * @param pid PID of the tree root object
     */
    public void exportMETS (String pid, OutputStream os);

}
