package cz.incad.kramerius.imaging.impl;

import java.io.File;
import java.io.IOException;

import cz.incad.kramerius.imaging.DiscStrucutreForStore;
import cz.incad.kramerius.imaging.paths.Path;
import cz.incad.kramerius.imaging.paths.impl.DirPathImpl;

/**
 * Pure implementation of the interface {@link DiscStrucutreForStore}
 * @author pavels
 */
public class PlainDiscStructure implements DiscStrucutreForStore {

    @Override
    public Path getUUIDFile(String uuid, String rootPath) throws IOException {
        File f = new File(new File(rootPath), uuid);
        DirPathImpl dpath = new DirPathImpl(f, null);
        return dpath;
    }

    
}
