package cz.incad.kramerius.workers;

import com.sun.jersey.api.client.*;
import cz.incad.kramerius.FedoraNamespaces;
import cz.incad.kramerius.fedora.om.Repository;
import cz.incad.kramerius.fedora.om.RepositoryException;
import cz.incad.kramerius.solr.SolrFieldsMapping;
import cz.incad.kramerius.utils.DNNTBatchUtils;
import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.utils.conf.KConfiguration;
import cz.incad.kramerius.utils.solr.SolrUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.w3c.dom.Document;

import javax.xml.parsers.ParserConfigurationException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class DNNTLabelWorker extends DNNTWorker {

    public static final String DNNT_LABEL_QUERY = "dnnt.solr.labeled.query";
    public static final String DNNT_LABEL_QUERY_UNSET = "dnnt.solr.labeled.unsetquery";

    public static final String CONTAINS_LICENSES_FOXML = "contains-licenses";
    public static final String LICENSES_FOXML = "licenses";


    private String label;


    public DNNTLabelWorker(String parentPid, FedoraAccess fedoraAccess, Client client, String label, boolean addRemoveFlag) {
        super(fedoraAccess, client, parentPid, addRemoveFlag);
        this.label = label;
    }

    @Override
    protected Document createSOLRBatchForChildren(List<String> sublist, boolean changedFoxml) {
        try {
            return DNNTBatchUtils.createLabelsBatch(sublist, this.label, this.addRemoveFlag);
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected Document createSOLRBatchForParents(List<String> sublist, boolean changedFoxmlFlag) {
        try {
            return DNNTBatchUtils.createContainsLabelsBatch(sublist, this.label, this.addRemoveFlag);
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected String solrChildrenQuery(List<String> pidPaths) {

        // it encoding
        String pidPathQuery =  SolrFieldsMapping.getInstance().getPidPathField()+":("+pidPaths.stream().map(SolrUtils::escapeQuery).map(it-> it+"*").collect(Collectors.joining(" OR "))+")";

        return this.addRemoveFlag ?
                KConfiguration.getInstance().getConfiguration().getString( DNNT_LABEL_QUERY,"("+pidPathQuery+ " -" + SolrFieldsMapping.getInstance().getDnntLabelsField() + ":[* TO *]) || (" +pidPathQuery+ " NOT " + SolrFieldsMapping.getInstance().getDnntLabelsField() + ":\"" +this.label+"\")")  :
                KConfiguration.getInstance().getConfiguration().getString( DNNT_LABEL_QUERY_UNSET,"("+pidPathQuery+ " " + SolrFieldsMapping.getInstance().getDnntLabelsField() + ":[* TO *]) || (" +pidPathQuery+ " " + SolrFieldsMapping.getInstance().getDnntLabelsField() + ":\"" +this.label+"\")");
    }




    List<String> changeFOXML(String pid, String typeOfLiteral, String label) {
        try {
            Repository repo = fedoraAccess.getInternalAPI();
            if (repo.objectExists(pid)) {
                boolean exists = repo.getObject(pid).literalExists(typeOfLiteral, FedoraNamespaces.KRAMERIUS_URI, label);

                if (!exists) {
                    if (addRemoveFlag) repo.getObject(pid).addLiteral(typeOfLiteral, FedoraNamespaces.KRAMERIUS_URI,label);
                } else {
                    repo.getObject(pid).removeLiteral(typeOfLiteral, FedoraNamespaces.KRAMERIUS_URI, label);
                    if (addRemoveFlag) repo.getObject(pid).addLiteral(typeOfLiteral, FedoraNamespaces.KRAMERIUS_URI,label);
                }

                List<Triple<String, String, String>> literals = repo.getObject(pid).getLiterals(FedoraNamespaces.KRAMERIUS_URI);
                return  literals.stream().filter(tr -> {
                    return tr.getLeft().equals(typeOfLiteral);
                }).map(Triple::getRight).collect(Collectors.toList());

            } else {
                LOGGER.warning(String.format("Cannot change label for %s: Pid not found", pid));
                return new ArrayList<>();
            }
        } catch (RepositoryException e) {
            LOGGER.warning(String.format("Cannot change label for %s: Pid not found", pid));
            return new ArrayList<>();
        }

    }

    @Override
    protected boolean changeFOXMLDown(String pid) {
        List<String> labels = changeFOXML(pid, LICENSES_FOXML,  this.label);
        return !labels.isEmpty();
    }

    @Override
    protected boolean changeFOXMLUp(String pid) {
        List<String> labels = changeFOXML(pid, CONTAINS_LICENSES_FOXML, this.label);
        return !labels.isEmpty();
    }
}
