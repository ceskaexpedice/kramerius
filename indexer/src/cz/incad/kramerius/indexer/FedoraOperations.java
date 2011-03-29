package cz.incad.kramerius.indexer;

import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.impl.FedoraAccessImpl;
import cz.incad.kramerius.resourceindex.IResourceIndex;
import cz.incad.kramerius.resourceindex.ResourceIndexService;
import cz.incad.kramerius.utils.conf.KConfiguration;
import dk.defxws.fedoragsearch.server.*;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import org.fedora.api.FedoraAPIA;
import org.fedora.api.FedoraAPIM;
import org.fedora.api.MIMETypedStream;

public class FedoraOperations {

    private static final Logger logger =
            Logger.getLogger(FedoraOperations.class.getName());
    //private static final Map fedoraClients = new HashMap();
    protected String fgsUserName;
    protected String indexName_;
    //public Properties config;
    public byte[] foxmlRecord;
    protected String dsID;
    protected byte[] ds;
    protected String dsText;
    protected String[] params = null;
    String foxmlFormat;
    FedoraAccess fa;
    IResourceIndex rindex;

    public FedoraOperations() throws Exception {
        fa = new FedoraAccessImpl(KConfiguration.getInstance());
    }

    public void init(String indexName/*, Properties currentConfig*/) {
        init(null, indexName/*, currentConfig*/);
    }

    public void init(String fgsUserName, String indexName/*, Properties currentConfig*/) {
//        config = currentConfig;
        foxmlFormat = KConfiguration.getInstance().getConfiguration().getString("FOXMLFormat");
        this.fgsUserName = KConfiguration.getInstance().getConfiguration().getString("fgsUserName");
        if (null == this.fgsUserName || this.fgsUserName.length() == 0) {
            try {
                this.fgsUserName = KConfiguration.getInstance().getConfiguration().getString("fedoragsearch.testUserName");
            } catch (Exception e) {
                this.fgsUserName = "fedoragsearch.testUserName";
            }
        }
    }

    public void updateIndex(String action, String value, ArrayList<String> requestParams) throws java.rmi.RemoteException, Exception {
        logger.info("updateIndex"
                + " action=" + action
                + " value=" + value);

        SolrOperations ops = new SolrOperations(this);
        ops.updateIndex(action, value, requestParams);
    }

    public byte[] getAndReturnFoxmlFromPid(String pid) throws java.rmi.RemoteException, Exception {
        logger.fine("getAndReturnFoxmlFromPid pid=" + pid);

        try {
            return fa.getAPIM().export(pid, foxmlFormat, "public");
        } catch (Exception e) {
            throw new Exception("Fedora Object " + pid + " not found. ", e);
        }
    }

    public void getFoxmlFromPid(String pid) throws java.rmi.RemoteException, Exception {

        logger.info("getFoxmlFromPid pid=" + pid);

        String format = "info:fedora/fedora-system:FOXML-1.1";
        try {
            foxmlRecord = fa.getAPIM().export(pid, format, "public");
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception("Fedora Object " + pid + " not found. ", e);
        }
    }

    public int getPdfPagesCount(String pid, String dsId) throws Exception {
        ds = null;
        if (dsId != null) {
            try {
                FedoraAPIA apia = fa.getAPIA();
                MIMETypedStream mts = apia.getDatastreamDissemination(pid,
                        dsId, null);
                if (mts == null) {
                    return 1;
                }
                ds = mts.getStream();
                return (new TransformerToText().getPdfPagesCount(ds) + 1);

            } catch (Exception e) {
                throw new Exception(e.getClass().getName() + ": " + e.toString());
            }
        }
        return 1;
    }

    public void getPidPaths(String pid) {
        logger.info("getPidPaths");
        ArrayList<String> pid_paths = new ArrayList<String>();
        pid_paths.add(pid);
        getPidPaths(pid_paths);
        for(String s: pid_paths){
            logger.info(s);
        }
    }

    private void getPidPaths(ArrayList<String> pid_paths) {
        String first;
        boolean changed = false;
        ArrayList<String> old = new ArrayList<String>(pid_paths);
        pid_paths.clear();
        for (int i = 0; i < old.size(); i++) {
            first = old.get(i).split("/")[0];
            ArrayList<String> p = getParentsArray(first);
            if(!p.isEmpty()){
                changed = true;
                for(String s: p){
                    pid_paths.add(s + "/" + old.get(i));
                }
            }else{
                pid_paths.add(old.get(i));
            }
        }
        if(changed) getPidPaths(pid_paths);
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

            //logger.info("getParents: " + pid);
            if (rindex == null) {
                rindex = ResourceIndexService.getResourceIndexImpl();
            }
            return rindex.getParentsPids(pid);

        } catch (Exception ex) {
            ex.printStackTrace();
            logger.severe(ex.toString());
            return null;
        }
    }

    public String getDatastreamText(String pid, String dsId, String pageNum) throws Exception {

        StringBuffer dsBuffer = new StringBuffer();
        String mimetype = "";
        ds = null;
        if (dsId != null) {
            try {
                FedoraAPIA apia = fa.getAPIA();
                MIMETypedStream mts = apia.getDatastreamDissemination(pid,
                        dsId, null);
                if (mts == null) {
                    return "";
                }
                ds = mts.getStream();
                mimetype = mts.getMIMEType();
            } catch (Exception e) {
                throw new Exception(e.getClass().getName() + ": " + e.toString());
            }
        }
        if (ds != null) {
            dsBuffer = (new TransformerToText().getText(ds, mimetype, pageNum));
        } else {
            logger.fine("ds is null");
        }
        logger.fine("getDatastreamText"
                + " pid=" + pid
                + " dsId=" + dsId
                + " mimetype=" + mimetype
                + " dsBuffer=" + dsBuffer.toString());
        return dsBuffer.toString();
    }
}
