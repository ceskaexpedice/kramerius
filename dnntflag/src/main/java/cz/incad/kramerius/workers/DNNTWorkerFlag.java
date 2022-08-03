package cz.incad.kramerius.workers;

import com.sun.jersey.api.client.*;
import cz.incad.kramerius.utils.DNNTBatchUtils;
import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.utils.conf.KConfiguration;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Responsible for setting flag
 */
public class DNNTWorkerFlag extends DNNTWorker {

    public static Logger LOGGER = Logger.getLogger(DNNTWorkerFlag.class.getName());

    private static final String DNNT_QUERY = "dnnt.solr.query";
    private static final String DNNT_QUERY_UNSET = "dnnt.solr.unsetquery";

    public DNNTWorkerFlag(String parentPid, FedoraAccess fedoraAccess, Client client, boolean flag) {
        super(fedoraAccess, client, parentPid, flag);
        LOGGER.info("Constructing   worker for "+this.parentPid);
    }


    protected Document createBatchForChildren(List<String> sublist, boolean changedFoxml){
        try {
            return DNNTBatchUtils.createLegacyDNNT(sublist, this.addRemoveFlag);
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    protected Document createBatchForParents(List<String> sublist) {
        return null;
    }

	@Override
	protected boolean checkParentPath(String parentPid, String rootPid, List<String> path)
			throws ParserConfigurationException, SAXException, IOException {
		// TODO Auto-generated method stub
		return false;
	}


	protected  String solrChildrenQuery(List<String> pidPaths) {
        String pidPathQuery = "pid_path:("+pidPaths.stream().map(it -> "\"" + it + "\"").collect(Collectors.joining(" OR "))+")";
        return this.addRemoveFlag ?
                KConfiguration.getInstance().getConfiguration().getString( DNNT_QUERY,"("+pidPathQuery+" -dnnt:[* TO *]) || ("+pidPathQuery+" +dnnt:false)")  :
                KConfiguration.getInstance().getConfiguration().getString( DNNT_QUERY_UNSET,"("+pidPathQuery+" dnnt:[* TO *]) || ("+pidPathQuery+" +dnnt:true)");
    }

    protected boolean changeFOXML(String pid) {
        return changeDNNTInFOXML(pid);
    }
}
