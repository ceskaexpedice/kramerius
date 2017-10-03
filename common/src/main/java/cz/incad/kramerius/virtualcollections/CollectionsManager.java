package cz.incad.kramerius.virtualcollections;

import java.util.List;
import java.util.Locale;

/**
 * Collection manager
 */
public interface CollectionsManager {
    
    /**
     * Sorting type enum
     */
    public static enum SortOrder  {
            ASC,
            DESC;
    }
    
    public static enum SortType {
    	ALPHABET,
    	NUMBER_OF_DCOUMENTS
    }
    
    /**
     * Return one collection
     * @param pid Collection's pid
     * @return
     * @throws CollectionException
     */
    public Collection getCollection(String pid) throws CollectionException;
    
    /**
     * Returns list of collections
     * @return
     * @throws CollectionException
     */
    public List<Collection> getCollections() throws CollectionException;

    /**
     * REturns sorted list of collection
     * @param locale
     * @param type TODO
     * @param type
     * @return
     * @throws CollectionException
     */
    public List<Collection> getSortedCollections(Locale locale, SortOrder order, SortType type) throws CollectionException;
    
        
    /**
     * Returns true if created collection contains datastream
     * @param pid PID of collection 
     * @param streamName Stream name
     * @return
     * @throws CollectionException
     */
    public boolean containsDataStream(String pid, String streamName) throws CollectionException;
    
}
