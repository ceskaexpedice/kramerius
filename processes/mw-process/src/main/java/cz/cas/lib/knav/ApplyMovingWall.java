package cz.cas.lib.knav;

import java.io.IOException;
import java.util.logging.Logger;

import javax.xml.xpath.XPathExpressionException;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.name.Names;
import cz.cas.lib.knav.indexer.CollectPidForIndexing;
import cz.incad.kramerius.SolrAccess;
import cz.incad.kramerius.fedora.RepoModule;
import cz.incad.kramerius.fedora.om.RepositoryException;
import cz.incad.kramerius.impl.SolrAccessImplNewIndex;
import cz.incad.kramerius.resourceindex.ResourceIndexModule;
import cz.incad.kramerius.security.RightCriteriumException;
import cz.incad.kramerius.solr.SolrModule;
import cz.incad.kramerius.statistics.NullStatisticsModule;

/**
 * Process sets flag public | private according to configuration
 * 
 * @author pavels
 */
public class ApplyMovingWall {

    public static final Logger LOGGER = Logger.getLogger(ApplyMovingWall.class
            .getName());

    public static void main(String[] args) throws IOException,
            RightCriteriumException, XPathExpressionException, RepositoryException {
        Injector injector = Guice.createInjector(new SolrModule(), new ResourceIndexModule(), new RepoModule(), new NullStatisticsModule());
        FedoraAccess fa = injector.getInstance(Key.get(FedoraAccess.class, Names.named("rawFedoraAccess")));

        SolrAccess sa = new SolrAccessImplNewIndex();
        CollectPidForIndexing coll = new CollectPidForIndexing();

        String userValue = userValue(args);
        if (userValue != null) {
            // first argument is user defined value 
            args = restArgs(args,1);
        }

        String mode = mode(args);
        if (mode != null) {
            // second argument is mode
            args = restArgs(args,1);
        }
        else {
            mode = "year";
        }

        String[] pids = args;
        ApplyMWUtils.applyMWOverPidsArray(fa, sa, coll, userValue, mode, pids);
    }

    static String[] restArgs(String[] args, int i) {
        String[] nargs = new String[args.length - i];
        System.arraycopy(args, i, nargs, 0, args.length-i);
        return nargs;
    }

    static String userValue(String[] args) {
        if (args.length > 0) {
            String first = args[0];
            if (!first.startsWith("uuid:")) {
                return first;
            }
        }
        return null;
    }

    static String mode(String[] args) {
        if (args.length > 0) {
            String first = args[0];
            if (!first.startsWith("uuid:")) {
                return first;
            }
        }
        return null;
    }
}
