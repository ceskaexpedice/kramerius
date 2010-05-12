package cz.incad.kramerius.service;

public interface ExportService {
    
    /**
     * Export the tree of Kramerius objects including the root with given PID, in FOXML1.1 format, archive context
     * @param pid PID of the tree root object
     */
    public void exportTree (String pid);

}
