package cz.cas.lib.knav;

import java.io.IOException;
import java.util.Arrays;
import java.util.logging.Logger;

import javax.xml.xpath.XPathExpressionException;

import cz.cas.lib.knav.indexer.CollectPidForIndexing;
import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.SolrAccess;
import cz.incad.kramerius.impl.FedoraAccessImpl;
import cz.incad.kramerius.impl.SolrAccessImpl;
import cz.incad.kramerius.security.RightCriteriumException;
import cz.incad.kramerius.utils.conf.KConfiguration;
import cz.incad.kramerius.utils.pid.LexerException;
import cz.incad.kramerius.utils.pid.PIDParser;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;

/**
 * Process sets flag public | private according to configuration
 * 
 * @author pavels
 */
public class ApplyMovingWall {

    public static final Logger LOGGER = Logger.getLogger(ApplyMovingWall.class
            .getName());

    public static void main(String[] args) throws IOException,
            RightCriteriumException, XPathExpressionException {

        FedoraAccess fa = new FedoraAccessImpl(KConfiguration.getInstance(),
                null);
        SolrAccess sa = new SolrAccessImpl();
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
