package cz.kramerius.adapters.impl.dummy;

import cz.kramerius.adapters.impl.RepositoryAccessImplAbstract;
import org.w3c.dom.Document;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class RepositoryAccessImplDummy extends RepositoryAccessImplAbstract {

    @Override
    public InputStream getFoxml(String pid) throws IOException {
        return new FileInputStream(new File("src/main/resources/repository_access_dummy/foxml.xml"));
    }

    @Override
    public String getFirstItemPid(Document relsExt) throws IOException {
        return null;
    }
}
