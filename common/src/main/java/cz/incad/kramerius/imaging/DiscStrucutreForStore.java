package cz.incad.kramerius.imaging;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;

import com.google.inject.Provider;

/**
 * Implementation of this interface can organize folders for storing and loading
 * uuid objects in disc
 * 
 * @author pavels
 */
public interface DiscStrucutreForStore {

    /**
     * Returns file for given uuid
     * 
     * @param uuid
     *            UUID of the object
     * @param rootPath
     *            Root path to where object shoul be stored
     * @return
     * @throws IOException
     */
    public File getUUIDFile(String uuid, String rootPath) throws IOException;

}
