package cz.incad.kramerius.workers;

import com.sun.jersey.api.client.*;
import cz.incad.kramerius.utils.DNNTBatchUtils;
import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.utils.conf.KConfiguration;
import org.w3c.dom.Document;

import javax.xml.parsers.ParserConfigurationException;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Responsible for setting flag
 */
@Deprecated
public class DNNTWorkerFlag extends DNNTWorker {

    public static Logger LOGGER = Logger.getLogger(DNNTWorkerFlag.class.getName());

    private static final String DNNT_QUERY = "dnnt.solr.query";
    private static final String DNNT_QUERY_UNSET = "dnnt.solr.unsetquery";

    public DNNTWorkerFlag(String parentPid, FedoraAccess fedoraAccess, Client client, boolean flag) {
        super(fedoraAccess, client, parentPid, flag);
        LOGGER.info("Constructing   worker for "+this.parentPid);
    }


    protected Document createSOLRBatchForChildren(List<String> sublist, boolean changedFoxml){
        try {
            return DNNTBatchUtils.createLegacyDNNT(sublist, this.addRemoveFlag);
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    protected Document createSOLRBatchForParents(List<String> sublist, boolean changedFoxmlFlag) {
        return null;
    }

    protected  String solrChildrenQuery(List<String> pidPaths) {
        String pidPathQuery = "pid_path:("+pidPaths.stream().map(it -> "\"" + it + "\"").collect(Collectors.joining(" OR "))+")";
        return this.addRemoveFlag ?
                KConfiguration.getInstance().getConfiguration().getString( DNNT_QUERY,"("+pidPathQuery+" -dnnt:[* TO *]) || ("+pidPathQuery+" +dnnt:false)")  :
                KConfiguration.getInstance().getConfiguration().getString( DNNT_QUERY_UNSET,"("+pidPathQuery+" dnnt:[* TO *]) || ("+pidPathQuery+" +dnnt:true)");
    }

    protected boolean changeFOXMLDown(String pid) {
        return changeDNNTInFOXML(pid);
    }

    @Override
    protected boolean changeFOXMLUp(String pid) {
        return true;
    }
}
