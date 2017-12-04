package cz.incad.kramerius;

import java.util.Date;
import java.util.List;

/**
 * Most desirable objects in application
 * 
 * @author pavels
 *
 */
public interface MostDesirable {

    /**
     * Save access to some object
     * 
     * @param uuid
     *            UUID of object
     * @param date
     *            access date
     */
    public void saveAccess(String uuid, Date date);

    /**
     * Return most desirable objects
     * 
     * @param count
     *            How many objects do you want ?
     * @param offset
     *            Offset
     * @param model
     *            Filtered model
     * @return
     */
    public List<String> getMostDesirable(int count, int offset, String model);

}
