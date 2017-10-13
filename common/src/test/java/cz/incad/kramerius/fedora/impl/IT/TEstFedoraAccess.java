package cz.incad.kramerius.fedora.impl.IT;

import cz.incad.kramerius.fedora.impl.Fedora4AccessImpl;
import cz.incad.kramerius.utils.conf.KConfiguration;
import junit.framework.TestCase;
import org.w3c.dom.Document;

import java.io.IOException;

/**
 * Created by pstastny on 10/13/2017.
 */
public class TEstFedoraAccess extends TestCase {

    public void testObjectPid() throws IOException {
        Fedora4AccessImpl fa = new Fedora4AccessImpl(KConfiguration.getInstance(), null);

        Document biblioMods = fa.getBiblioMods("uuid:69d79410-490a-11de-9d6a-000d606f5dc6");
        System.out.println(biblioMods);
    }
}
