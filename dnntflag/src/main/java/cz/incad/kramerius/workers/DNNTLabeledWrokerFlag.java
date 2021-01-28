package cz.incad.kramerius.workers;

import com.sun.jersey.api.client.*;
import cz.incad.kramerius.utils.DNNTBatchUtils;
import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.utils.conf.KConfiguration;
import org.w3c.dom.Document;

import javax.xml.parsers.ParserConfigurationException;
import java.util.List;
import java.util.stream.Collectors;

public class DNNTLabeledWrokerFlag extends DNNTWorker {

    private static final String DNNT_LABEL_QUERY = "dnnt.solr.labeled.query";
    private static final String DNNT_LABEL_QUERY_UNSET = "dnnt.solr.labeled.unsetquery";

    private String label;


    public DNNTLabeledWrokerFlag(String parentPid, FedoraAccess fedoraAccess, Client client, String label, boolean flag) {
        super(fedoraAccess, client, parentPid, flag);
        this.label = label;
    }

    @Override
    protected Document createBatch(List<String> sublist) {
        try {
            return DNNTBatchUtils.createLabeledDNNT(sublist, this.label, this.flag);
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    protected String solrChildrenQuery(List<String> pidPaths) {
        String pidPathQuery = "pid_path:("+pidPaths.stream().map(it -> "\"" + it + "\"").collect(Collectors.joining(" OR "))+")";
        return this.flag ?
                KConfiguration.getInstance().getConfiguration().getString( DNNT_LABEL_QUERY,"("+pidPathQuery+" -dnnt-labels:[* TO *]) || ("+pidPathQuery+" NOT dnnt-labels:"+this.label+")")  :
                KConfiguration.getInstance().getConfiguration().getString( DNNT_LABEL_QUERY,"("+pidPathQuery+" dnnt-labels:[* TO *]) || ("+pidPathQuery+" dnnt-labels:"+this.label+")");
    }



    @Override
    protected void changeFOXML(String pid) {
        changeDNNTInFOXML(pid);
        changeDNNTLabelInFOXML(pid, this.label);
    }

}
