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

//	protected boolean checkParentPAth(String rootPid, List<String> path, String label) throws ParserConfigurationException, SAXException, IOException {
//    	String rootFq = "root_pid:"+URLEncoder.encode(rootPid.replaceAll(":", "\\:"),  "UTF-8"); 
//    	String pathString =  path.stream().collect(Collectors.joining("/"))+"/*";
//        String pidPathQuery = SolrFieldsMapping.getInstance().getPidPathField()+":("+pathString+")";
//        String licenseFq = SolrFieldsMapping.getInstance().getDnntLabelsField()+":"+label;
//    	
//    	Element element = IterationUtils.executeQuery(this.client, selectUrl(), "?q= NOT "+ URLEncoder.encode(pidPathQuery, "UTF-8")+"&fq="+rootFq+"&fq="+licenseFq+"&fl="+ SolrFieldsMapping.getInstance().getPidPathField()+"&wt=xml");
//        Element resultElm = XMLUtils.findElement(element, (e) -> {
//            if (e.getNodeName().equals("result")) {
//                return true;
//            } else return false;
//        });
//    	if (resultElm != null && resultElm.getAttribute("numFound") !=null) {
//    		int numFound = Integer.parseInt(resultElm.getAttribute("numFound"));
//    		return numFound > 0 ;
//    	} else return false;
//    }

    

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

    protected static String selectUrl() {
        String shost = KConfiguration.getInstance().getSolrHost();
        shost = shost  + (shost.endsWith("/") ? ""  : "/") + "select";
        return shost;
    }


    static void testURL(String rootPid,List<String> path, String label) throws IOException {
    	//String rootFq = "root_pid:"+URLEncoder.encode(rootPid.replaceAll(":", "\\\\:"),  "UTF-8"); 
    	String pathString =  path.stream().collect(Collectors.joining("/"))+"/*";
        String pidPathQuery = SolrFieldsMapping.getInstance().getPidPathField()+":("+pathString+")";
        String licenseFq = SolrFieldsMapping.getInstance().getDnntLabelsField()+":"+label;
    	
    	String url = selectUrl()+ "?q="+URLEncoder.encode("NOT ", "UTF-8")+ URLEncoder.encode(pidPathQuery, "UTF-8")+"&fq="+licenseFq+"&fl="+ SolrFieldsMapping.getInstance().getPidPathField()+"&wt=xml";
        InputStream inputStream = RESTHelper.inputStream(url, pidPathQuery, licenseFq);
        String string = org.apache.commons.io.IOUtils.toString(inputStream);
        System.out.println(string);
    }
    

    
    public static void main(String[] args) throws IOException {
    	String[] splitted = "uuid:045b1250-7e47-11e0-add1-000d606f5dc6/uuid:f7e50720-80b6-11e0-9ec7-000d606f5dc6/uuid:91214030-80bb-11e0-b482-000d606f5dc6/uuid:ab7e5a1a-bddb-11e0-bff9-0016e6840575".split("/");
    	
    	
    	ObjectPidsPath objPidPath = new ObjectPidsPath(Arrays.asList(splitted));
		ObjectPidsPath cutTail = objPidPath.cutTail(0);
		while(cutTail.getLength() > 0) {
			System.out.println(cutTail);
			cutTail = cutTail.cutTail(0);
		}    
    	
//		List<String> collectedPids = Arrays.stream( path.split("/")).collect(Collectors.toList());
//		ObjectPidsPath objPidPath = new ObjectPidsPath(collectedPids);
//		ObjectPidsPath cutTail = objPidPath.cutTail(0);
//
//		while(cutTail.getLength() > 0) {
//
//			List<String> changed = Arrays.stream(cutTail.getPathFromRootToLeaf()).map(str -> {
//    			return str.replaceAll("\\:", "\\\\:");
//    		}).collect(Collectors.toList());
//			
//			System.out.println(changed);
//			
//			testURL("uuid:045b1250-7e47-11e0-add1-000d606f5dc6", changed, "dnnto");
//		}
    }
    
}
