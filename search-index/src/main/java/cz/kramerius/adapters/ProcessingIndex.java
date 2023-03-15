package cz.kramerius.adapters;

import cz.incad.kramerius.resourceindex.ResourceIndexException;
import cz.kramerius.shared.Pair;

import java.util.List;
import java.util.Set;

/**
 * Vyrovnává rozdíly mezi současnou, minulou a uvažovanou budoucí verzí cz.incad.kramerius.resourceindex.IResourceIndex.
 * A to tak, že udržuje metody cz.incad.kramerius.resourceindex.IResourceIndex, které byly odebrány a přidává nové metody.
 * Společně s abstraktní implementací implementující dummy metodami vše
 * tak mohou implementace cz.kramerius.adapters.IResourceIndex používat jen vybrané metody z minulosti, přítomnosti a budoucnosti.
 *
 * @see cz.incad.kramerius.resourceindex.IResourceIndex
 * @see cz.kramerius.searchIndex.repositoryAccessImpl.ResourceIndexImplAbstract
 */
public interface ProcessingIndex extends cz.incad.kramerius.resourceindex.IResourceIndex {

    /**
     * @return pids of own children (first) and foster children (second) of an object identified by pid; both lists are sorted in an order from RELS-EXT elements
     */
    public Pair<List<String>, List<String>> getPidsOfChildren(String pid) throws ResourceIndexException;

    /**
     * @return pid of own parent (first) and pids of foster parents of an object identified by pid
     */
    public Pair<String, Set<String>> getPidsOfParents(String pid) throws ResourceIndexException;

    /**
     * @return model of an object identified by pid
     */
    public String getModel(String pid) throws ResourceIndexException;
}
