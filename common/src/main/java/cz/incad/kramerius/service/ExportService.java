package cz.incad.kramerius.service;

import java.io.IOException;

public interface ExportService {
    
    /**
     * Export the tree of Kramerius objects including the root with given PID, in FOXML1.1 format, archive context
     * @param pid PID of the tree root object
     * @throws IOException 
     */
    public void exportTree (String pid) throws IOException;

}
