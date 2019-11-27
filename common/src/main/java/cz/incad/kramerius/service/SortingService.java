package cz.incad.kramerius.service;

import java.util.List;

/**
 * Service for sorting list of FOXML objects based on the contents of their BIBLIO-MODS datastream
 */
public interface SortingService {

    /**
     * Sort the Kramerius relations  in RELS-EXT datastream based on the values in target MODS datastreams
     * @param pid
     * @param startIndexer
     */
    void sortRelations(String pid, boolean startIndexer);

    /**
     * Sort given list of FOXML objects (their PIDs) based on the content of the BIBLIO-MODS datastream
     * @param pids  list of FOXML PIDs to sort
     * @param xpath  XPath expression to extract the data (upon which the objects will be sorted) from BIBLIO-MODS
     * @return sorted list of PIDs
     */
    List<String> sortObjects(List<String> pids, String xpath);
}
