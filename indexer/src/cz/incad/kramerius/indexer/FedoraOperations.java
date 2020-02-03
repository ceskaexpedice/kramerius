package cz.incad.kramerius.indexer;

import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.FedoraNamespaces;
import cz.incad.kramerius.impl.FedoraAccessImpl;
import cz.incad.kramerius.resourceindex.IResourceIndex;
import cz.incad.kramerius.resourceindex.ResourceIndexService;
import cz.incad.kramerius.utils.UTFSort;
import cz.incad.kramerius.utils.XMLUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;
import dk.defxws.fedoragsearch.server.TransformerToText;
import org.fedora.api.FedoraAPIA;
import org.fedora.api.MIMETypedStream;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

//TODO: rewrite it 
public class FedoraOperations {

    private static final Logger logger =
            Logger.getLogger(FedoraOperations.class.getName());
    //protected String fgsUserName;
    protected String indexName_;
    public byte[] foxmlRecord;
    protected String dsID;
    protected byte[] ds;
    protected String dsText;
    protected String[] params = null;
    String foxmlFormat;
    FedoraAccess fa;
    IResourceIndex rindex;
    UTFSort utf_sort;

    public FedoraOperations() throws Exception {
        fa = new FedoraAccessImpl(KConfiguration.getInstance(),null);
        foxmlFormat = KConfiguration.getInstance().getConfiguration().getString("FOXMLFormat");
        utf_sort = new UTFSort();
        utf_sort.init();
    }

    public void updateIndex(String action, String value, ArrayList<String> requestParams) throws java.rmi.RemoteException, Exception {
        logger.log(Level.INFO, "updateIndex action={0} value={1}", new Object[]{action, value});

        SolrOperations ops = new SolrOperations(this);
        ops.updateIndex(action, value);
    }

    public byte[] getAndReturnFoxmlFromPid(String pid) throws java.rmi.RemoteException, Exception {
        logger.log(Level.FINE, "getAndReturnFoxmlFromPid pid={0}", pid);

        try {
            return fa.getAPIM().export(pid, foxmlFormat, "public");
        } catch (Exception e) {
            throw new Exception("Fedora Object " + pid + " not found. ", e);
        }
    }

    public void getFoxmlFromPid(String pid) throws java.rmi.RemoteException, Exception {

        logger.log(Level.INFO, "getFoxmlFromPid pid={0}", pid);

        try {
            foxmlRecord = fa.getAPIM().export(pid, foxmlFormat, "public");

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error getting object", e);
            throw new Exception("Fedora Object " + pid + " not found. ", e);
        }
    }

    public int getPdfPagesCount_(String pid, String dsId) throws Exception {
        ds = null;
        if (dsId != null) {
            FedoraAPIA apia = fa.getAPIA();
            MIMETypedStream mts = apia.getDatastreamDissemination(pid,
                    dsId, null);
            if (mts == null) {
                return 1;
            }
            ds = mts.getStream();
//            getPDFDocument(pid);
//            int ret = (pdDoc.getNumberOfPages() + 1);
//            closePDFDocument();
//            return ret;
            return (new TransformerToText().getPdfPagesCount_(ds) + 1);
        }
        return 1;
    }

    private List<String> getTreePredicates() {
        return Arrays.asList(KConfiguration.getInstance().getPropertyList("fedora.treePredicates"));
    }

    public int getRelsIndex(String pid) throws Exception {
        ArrayList<String> p = getParentsArray(pid);
        String uuid;
        int relsindex = 0;
        if (!p.isEmpty()) {
            String fedoraPid = "info:fedora/" + pid;
            for (String s : p) {
                Document relsExt = fa.getRelsExt(s);
                Element descEl = XMLUtils.findElement(relsExt.getDocumentElement(), "Description", FedoraNamespaces.RDF_NAMESPACE_URI);
                List<Element> els = XMLUtils.getElements(descEl);
                int i = 0;
                for (Element el : els) {
                    if (getTreePredicates().contains(el.getLocalName()) && !el.getLocalName().contains("isOnPage")) {
                        if (el.hasAttribute("rdf:resource")) {
                            uuid = el.getAttributes().getNamedItem("rdf:resource").getNodeValue();
                            if (uuid.equals(fedoraPid)) {
                                relsindex = Math.max(relsindex, i);
                            }
                            i++;
                        }
                    }
                }
            }
        } else {
            //pid_paths.add(old.get(i));
        }
        return relsindex;
    }

    public ArrayList<Integer> getRelsIndexByPath(ArrayList<String> pid_paths) throws Exception {

        ArrayList<Integer> idxs = new ArrayList<Integer>();
        String parent;
        String fedoraPid;
        String uuid;
        if (!pid_paths.isEmpty()) {
            for (String s : pid_paths) {
                String[] pids = s.split("/");
                if (pids.length == 1) {
                    idxs.add(0);
                } else {
                    fedoraPid = "info:fedora/" + pids[pids.length - 1];
                    parent = pids[pids.length - 2];
                    Document relsExt = fa.getRelsExt(parent);
                    Element descEl = XMLUtils.findElement(relsExt.getDocumentElement(), "Description", FedoraNamespaces.RDF_NAMESPACE_URI);
                    List<Element> els = XMLUtils.getElements(descEl);
                    int i = 0;
                    for (Element el : els) {
                        if (getTreePredicates().contains(el.getLocalName())) {
                            if (el.hasAttribute("rdf:resource")) {
                                uuid = el.getAttributes().getNamedItem("rdf:resource").getNodeValue();
                                if (uuid.equals(fedoraPid)) {
                                    idxs.add(i);
                                    break;
                                }
                                i++;
                            }
                        }
                    }
                }
            }
        } else {
            idxs.add(0);
        }
        return idxs;
    }

    public ArrayList<String> getPidPaths(String pid) {
        ArrayList<String> pid_paths = new ArrayList<String>();
        pid_paths.add(pid);
        getPidPaths(pid_paths);
        return pid_paths;
    }

    private void getPidPaths(ArrayList<String> pid_paths) {
        String first;
        boolean changed = false;
        ArrayList<String> old = new ArrayList<String>(pid_paths);
        pid_paths.clear();
        for (int i = 0; i < old.size(); i++) {
            first = old.get(i).split("/")[0];
            ArrayList<String> p = getParentsArray(first);
            if (!p.isEmpty()) {
                for (String s : p) {
                    if(!old.get(i).contains(s)){
                        changed = true;
                        pid_paths.add(s + "/" + old.get(i));
                    }else{
                        logger.log(Level.WARNING, "Cyclic reference on "+s);
                        pid_paths.add(old.get(i));
                    }
                }
            } else {
                pid_paths.add(old.get(i));
            }
        }
        if (changed) {
            getPidPaths(pid_paths);
        }
    }

    public String getParents(String pid) {
        ArrayList<String> l = getParentsArray(pid);
        StringBuilder sb = new StringBuilder();
        for (String s : l) {
            sb.append(s).append(";");
        }
        sb.delete(sb.length() - 1, sb.length());
        return sb.toString();
    }

    public ArrayList<String> getParentsArray(String pid) {
        try {
            if (rindex == null) {
                rindex = ResourceIndexService.getResourceIndexImpl();
            }
            ArrayList<String> ret =  rindex.getParentsPids(pid);
            
            if(ret.contains(pid)){
                logger.log(Level.WARNING, "Cyclic reference on {0}", pid);
                ret.remove(pid);
            }
            return ret;

        } catch (Exception ex) {
            logger.log(Level.WARNING, ex.toString());
            return null;
        }
    }

    public String prepareCzech(String s) throws Exception {
        //return removeDiacritic(s).toLowerCase().replace("ch", "hz");
        return utf_sort.translate(s);
    }

    public String getDatastreamText(String pid, String dsId, String pageNum) throws Exception {
        if (dsId == null) {
            return "";
        }
        StringBuffer dsBuffer = new StringBuffer();
        ds = null;

        try {
            FedoraAPIA apia = fa.getAPIA();
            MIMETypedStream mts = apia.getDatastreamDissemination(pid,
                    dsId, null);
            if (mts == null) {
                return "";
            }
            ds = mts.getStream();
            String mimetype = mts.getMIMEType();

            if (ds != null) {
                if (mimetype.equals("application/pdf")) {
                    //getPDFDocument(pid);
                    //dsBuffer = TransformerToText.getTextFromPDF(pdDoc, pageNum);
                } else {
                    dsBuffer = (new TransformerToText().getText(ds, mimetype, pageNum));
                }
            } else {
                logger.fine("ds is null");
            }
            logger.log(Level.FINE,
                    "getDatastreamText  pid={0} dsId={1} mimetype={2} dsBuffer={3}",
                    new Object[]{pid, dsId, mimetype, dsBuffer.toString()});
            String text = dsBuffer.toString();
            return SolrOperations.removeTroublesomeCharacters(text);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Cant get datastream " + dsId);
            throw new Exception(e.getClass().getName() + ": " + e.toString());
        }
    }
}
