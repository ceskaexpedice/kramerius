package cz.incad.kramerius.imaging;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.sql.Connection;

import com.google.inject.Provider;

import cz.incad.kramerius.imaging.paths.Path;

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
    public Path getUUIDFile(String pid, String rootPath) throws IOException;

}
