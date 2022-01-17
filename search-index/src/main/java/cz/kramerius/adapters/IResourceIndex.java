package cz.kramerius.adapters;

import cz.incad.kramerius.resourceindex.ResourceIndexException;
import cz.kramerius.shared.Pair;

import java.util.List;

/**
 * Vyrovnává rozdíly mezi současnou, minulou a uvažovanou budoucí verzí cz.incad.kramerius.resourceindex.IResourceIndex.
 * A to tak, že udržuje metody cz.incad.kramerius.resourceindex.IResourceIndex, které byly odebrány a přidává nové metody.
 * Společně s abstraktní implementací implementující dummy metodami vše
 * tak mohou implementace cz.kramerius.adapters.IResourceIndex používat jen vybrané metody z minulosti, přítomnosti a budoucnosti.
 *
 * @see cz.incad.kramerius.resourceindex.IResourceIndex
 * @see cz.kramerius.searchIndex.repositoryAccessImpl.ResourceIndexImplAbstract
 */
public interface IResourceIndex extends cz.incad.kramerius.resourceindex.IResourceIndex {

    public Pair<List<String>, List<String>> getPidsOfChildren(String pid) throws ResourceIndexException;

    public Pair<String, List<String>> getPidsOfParents(String pid) throws ResourceIndexException;

    public String getModel(String pid) throws ResourceIndexException;
}
