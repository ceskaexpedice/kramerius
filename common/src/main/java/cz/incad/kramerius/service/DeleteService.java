package cz.incad.kramerius.service;

import java.io.IOException;

public interface DeleteService {
    
    /**
     * Delete the tree of Kramerius objects including the root with given PID 
     * @param pid PID of the tree root object
     * @throws IOException 
     */
    public void deleteTree (String pid, String message) throws IOException;

}
