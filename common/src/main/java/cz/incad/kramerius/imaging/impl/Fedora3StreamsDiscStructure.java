package cz.incad.kramerius.imaging.impl;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;

import cz.incad.kramerius.imaging.DiscStrucutreForStore;
import cz.incad.kramerius.impl.fedora.FedoraDatabaseUtils;

/**
 * Manage same structure for storing objects as fedora 3.<br>  
 * Need connection provider  to fedora 3 database
 * @author pavels
 */
public class Fedora3StreamsDiscStructure implements DiscStrucutreForStore {
    

    private Provider<Connection> feodora3ConProvider;

    
    @Inject
    public Fedora3StreamsDiscStructure(@Named("fedora3")Provider<Connection> feodora3ConProvider) {
        super();
        this.feodora3ConProvider = feodora3ConProvider;
    }

    private static File getUUIDFile(String uuid, List<String> relativeDataStreamPath, File rootDir) throws IOException {
        if ((relativeDataStreamPath != null) && (relativeDataStreamPath.size() > 1)) {
            File curDir = rootDir;
            for (int i = relativeDataStreamPath.size() - 2; i > 0; i--) {
                curDir = new File(curDir, relativeDataStreamPath.get(i));
                if (!curDir.exists()) {
                    if (!curDir.mkdirs()) {
                        throw new IOException("cannot create dir '" + rootDir.getAbsolutePath() + "'");
                    }
                }
            }
            return new File(curDir, uuid);
        } else {
            throw new IOException("uuid has no streamPath in fedora database ");
        }
    }

    public File getUUIDFile(String uuid,  String rootPath) throws IOException {
        try {
                List<String> relativeDataStreamPath = FedoraDatabaseUtils.getRelativeDataStreamPath(uuid, this.feodora3ConProvider);
                File rootDir = new File(rootPath);
                if (!rootDir.exists()) {
                    if (!rootDir.mkdirs()) {
                        throw new IOException("cannot create dir '" + rootPath + "'");
                    }
                }
                return getUUIDFile(uuid, relativeDataStreamPath, rootDir);
        } catch (SQLException e) {
            throw new IOException(e);
        }
    }
}
