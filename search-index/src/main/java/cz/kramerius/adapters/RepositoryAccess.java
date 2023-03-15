package cz.kramerius.adapters;

import cz.kramerius.adapters.impl.RepositoryAccessImplAbstract;

import java.io.IOException;
import java.io.InputStream;

/**
 * Vyrovnává rozdíly mezi současnou, minulou a uvažovanou budoucí verzí cz.incad.kramerius.FedoraAccess.
 * A to tak, že udržuje metody cz.incad.kramerius.FedoraAccess, které byly odebrány a přidává nové metody.
 * Společně s abstraktní implementací implementující dummy metodami vše
 * tak mohou implementace cz.kramerius.adapters.FedoraAccess používat jen vybrané metody z minulosti, přítomnosti a budoucnosti.
 *
 * @see cz.incad.kramerius.FedoraAccess
 * @see RepositoryAccessImplAbstract
 */
public interface RepositoryAccess extends cz.incad.kramerius.FedoraAccess {

    public InputStream getFoxml(String pid) throws IOException;

    public String getDatastreamMimeType(String pid, String datastreamName) throws IOException;
}
