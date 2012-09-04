package cz.incad.kramerius.imaging;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;

import com.google.inject.Provider;

/**
 * Implementation of this interface can organize folders for storing and loading
 * objects on disc
 * 
 * @author pavels
 */
public interface DiscStrucutreForStore {

    /**
     * Returns file for given pid
     * 
     * @param pid object's PID
     * @param rootPath Root path to where the object should be stored
     * @return file for given pid
     * @throws IOException IO error has been occurred
     */
    public File getUUIDFile(String pid, String rootPath) throws IOException;

}
