package cz.incad.kramerius.imaging.impl;

import java.io.File;
import java.io.IOException;

import cz.incad.kramerius.imaging.DiscStrucutreForStore;

/**
 * Pure implementation of the interface {@link DiscStrucutreForStore}
 * @author pavels
 */
public class PlainDiscStructure implements DiscStrucutreForStore {

    @Override
    public File getUUIDFile(String uuid, String rootPath) throws IOException {
        return new File(new File(rootPath), uuid);
    }

    
}
