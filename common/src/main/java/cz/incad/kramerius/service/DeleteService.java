package cz.incad.kramerius.service;

public interface DeleteService {
    
    /**
     * Delete the tree of Kramerius objects including the root with given PID 
     * @param pid PID of the tree root object
     */
    public void deleteTree (String pid, String message);

}
