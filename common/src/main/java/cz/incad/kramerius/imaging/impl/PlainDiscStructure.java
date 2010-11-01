package cz.incad.kramerius.imaging.impl;

import java.io.File;
import java.io.IOException;

import cz.incad.kramerius.imaging.DiscStrucutreForStore;

public class PlainDiscStructure implements DiscStrucutreForStore {

    @Override
    public File getUUIDFile(String uuid, String rootPath) throws IOException {
        return new File(new File(rootPath), uuid);
    }

    
}
