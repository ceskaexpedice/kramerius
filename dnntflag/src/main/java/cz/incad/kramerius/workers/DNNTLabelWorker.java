package cz.incad.kramerius.workers;

import com.sun.jersey.api.client.*;
import cz.incad.kramerius.solr.SolrFieldsMapping;
import cz.incad.kramerius.utils.DNNTBatchUtils;
import cz.incad.kramerius.utils.IOUtils;
import cz.incad.kramerius.utils.RESTHelper;
import cz.incad.kramerius.utils.XMLUtils;
import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.ObjectPidsPath;
import cz.incad.kramerius.services.IterationUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Arrays;
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
    protected Document createBatchForParents(List<String> sublist) {
        try {
            return DNNTBatchUtils.createContainsLabelsBatch(sublist, this.label, this.addRemoveFlag);
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        }
    }
    
    
    @Override
	protected boolean checkParentPath(String parentPid, String rootPid, List<String> path)
			throws ParserConfigurationException, SAXException, IOException {
    	String rootFq = "root_pid:"+URLEncoder.encode(rootPid.replaceAll("\\:", "\\\\:"),  "UTF-8"); 
    	String pidFq = "PID:"+URLEncoder.encode(parentPid.replaceAll("\\:", "\\\\:"),  "UTF-8"); 
    	String pathString =  path.stream().collect(Collectors.joining("/"))+"/*";
    	String query = SolrFieldsMapping.getInstance().getPidPathField()+":("+pathString+")";
    	
        String licenseFq = SolrFieldsMapping.getInstance().getDnntLabelsField()+":"+this.label;
    	Element element = IterationUtils.executeQuery(this.client, selectUrl(), "?q="+URLEncoder.encode("NOT ", "UTF-8")+ URLEncoder.encode(query, "UTF-8")+"&fq="+rootFq+"&fq="+URLEncoder.encode("NOT ", "UTF-8")+pidFq+"&fq="+licenseFq+"&fl="+ SolrFieldsMapping.getInstance().getPidPathField()+"&wt=xml");
        Element resultElm = XMLUtils.findElement(element, (e) -> {
            if (e.getNodeName().equals("result")) {
                return true;
            } else return false;
        });
    	if (resultElm != null && resultElm.getAttribute("numFound") !=null) {
    		int numFound = Integer.parseInt(resultElm.getAttribute("numFound"));
    		return numFound > 0 ;
    	} else return false;
	}

    
    @Override
    protected String solrChildrenQuery(List<String> pidPaths) {
        String pidPathQuery = SolrFieldsMapping.getInstance().getPidPathField()+":("+pidPaths.stream().map(it -> "\"" + it + "\"").collect(Collectors.joining(" OR "))+")";
        return this.addRemoveFlag ?
                KConfiguration.getInstance().getConfiguration().getString( DNNT_LABEL_QUERY,"("+pidPathQuery+ " -" + SolrFieldsMapping.getInstance().getDnntLabelsField() + ":[* TO *]) || (" +pidPathQuery+ " NOT " + SolrFieldsMapping.getInstance().getDnntLabelsField() + ":\"" +this.label+"\")")  :
                KConfiguration.getInstance().getConfiguration().getString( DNNT_LABEL_QUERY_UNSET,"("+pidPathQuery+ " " + SolrFieldsMapping.getInstance().getDnntLabelsField() + ":[* TO *]) || (" +pidPathQuery+ " " + SolrFieldsMapping.getInstance().getContainsDnntLabelsField() + ":\"" +this.label+"\")");
    }


    

    @Override
    protected boolean changeParentTree(String pid) {
        List<String> labels = changeDNNTContainsLabelInFOXML(pid, this.label);
        return labels.size() > 0;
	}

	@Override
    protected boolean changeFOXML(String pid) {
        List<String> labels = changeDNNTLabelInFOXML(pid, this.label);
        return changeDNNTInFOXML(pid, labels.size() > 0);
    }

    protected static String selectUrl() {
        String shost = KConfiguration.getInstance().getSolrHost();
        shost = shost  + (shost.endsWith("/") ? ""  : "/") + "select";
        return shost;
    }

}
