package cz.incad.kramerius.service;

import java.util.List;

/**
 * Service for sorting list of FOXML objects based on the contents of their BIBLIO-MODS datastream
 */
public interface SortingService {

    /**
     * Sort the Kramerius relations  in RELS-EXT datastream based on the values in target MODS datastreams
     * @param pid
     */
    void sortRelations(String pid);

    /**
     * Sort given list of FOXML objects (their PIDs) based on the content of the BIBLIO-MODS datastream
     * @param pids  list of FOXML PIDs to sort
     * @param xpath  XPath expression to extract the data (upon which the objects will be sorted) from BIBLIO-MODS
     * @param numeric  when true, the data from xpath will be sorted as numeric (integer) values, otherwise alphabetically
     * @return sorted list of PIDs
     */
    List<String> sortObjects(List<String> pids, String xpath, boolean numeric);
}
