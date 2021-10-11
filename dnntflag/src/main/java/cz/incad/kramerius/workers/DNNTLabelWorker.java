package cz.incad.kramerius.workers;

import com.sun.jersey.api.client.*;
import cz.incad.kramerius.solr.SolrFieldsMapping;
import cz.incad.kramerius.utils.DNNTBatchUtils;
import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.utils.conf.KConfiguration;
import org.w3c.dom.Document;

import javax.xml.parsers.ParserConfigurationException;
import java.util.List;
import java.util.stream.Collectors;

public class DNNTLabelWorker extends DNNTWorker {

    private static final String DNNT_LABEL_QUERY = "dnnt.solr.labeled.query";
    private static final String DNNT_LABEL_QUERY_UNSET = "dnnt.solr.labeled.unsetquery";

    private String label;


    public DNNTLabelWorker(String parentPid, FedoraAccess fedoraAccess, Client client, String label, boolean addRemoveFlag) {
        super(fedoraAccess, client, parentPid, addRemoveFlag);
        this.label = label;
    }

    @Override
    protected Document createBatchForChildren(List<String> sublist, boolean changedFoxml) {
        try {
            return DNNTBatchUtils.createLabelsBatch(sublist, this.label, this.addRemoveFlag);
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected Document createBatchForParents(List<String> sublist, boolean changedFoxmlFlag) {
        try {
            return DNNTBatchUtils.createContainsLabelsBatch(sublist, this.label, this.addRemoveFlag);
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected String solrChildrenQuery(List<String> pidPaths) {
        String pidPathQuery = SolrFieldsMapping.getInstance().getPidPathField()+":("+pidPaths.stream().map(it -> "\"" + it + "\"").collect(Collectors.joining(" OR "))+")";
        return this.addRemoveFlag ?
                KConfiguration.getInstance().getConfiguration().getString( DNNT_LABEL_QUERY,"("+pidPathQuery+ " -" + SolrFieldsMapping.getInstance().getDnntLabelsField() + ":[* TO *]) || (" +pidPathQuery+ " NOT " + SolrFieldsMapping.getInstance().getDnntLabelsField() + ":\"" +this.label+"\")")  :
                KConfiguration.getInstance().getConfiguration().getString( DNNT_LABEL_QUERY_UNSET,"("+pidPathQuery+ " " + SolrFieldsMapping.getInstance().getDnntLabelsField() + ":[* TO *]) || (" +pidPathQuery+ " " + SolrFieldsMapping.getInstance().getContainsDnntLabelsField() + ":\"" +this.label+"\")");
    }



    @Override
    protected boolean changeFOXML(String pid) {
        List<String> labels = changeDNNTLabelInFOXML(pid, this.label);
        return changeDNNTInFOXML(pid, labels.size() > 0);
    }

}
