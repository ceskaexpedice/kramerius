package cz.incad.kramerius.indexer;

import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.impl.FedoraAccessImpl;
import cz.incad.kramerius.resourceindex.IResourceIndex;
import cz.incad.kramerius.resourceindex.ResourceIndexService;
import cz.incad.kramerius.utils.conf.KConfiguration;
import dk.defxws.fedoragsearch.server.*;

import java.util.ArrayList;
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

    private static String getBaseURL(String fedoraSoap)
            throws Exception {
        final String end = "/services";
        String baseURL = fedoraSoap;
        if (fedoraSoap.endsWith(end)) {
            return fedoraSoap.substring(0, fedoraSoap.length() - end.length());
        } else {
            throw new Exception("Unable to determine baseURL from fedoraSoap" + " value (expected it to end with '" + end + "'): " + fedoraSoap);
        }
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

    public String getParentsOld(String pid) {
        try {
            String query = "$object * <info:fedora/" + pid + ">";
            String urlStr = KConfiguration.getInstance().getConfiguration().getString("FedoraResourceIndex") + "?type=triples&flush=true&lang=spo&format=N-Triples&limit=&distinct=off&stream=off"
                    + "&query=" + java.net.URLEncoder.encode(query, "UTF-8");
            StringBuilder sb = new StringBuilder();

            java.net.URL url = new java.net.URL(urlStr);

            java.io.BufferedReader in = new java.io.BufferedReader(new java.io.InputStreamReader(url.openStream()));
            String inputLine;
            int end;
            while ((inputLine = in.readLine()) != null) {
//<info:fedora/uuid:5fe0b160-62d5-11dd-bdc7-000d606f5dc6> <http://www.nsdl.org/ontologies/relationships#hasPage> <info:fedora/uuid:75fca1f0-64b2-11dd-9fd4-000d606f5dc6> .
//<info:fedora/uuid:f0da6570-8f3b-11dd-b796-000d606f5dc6> <http://www.nsdl.org/ontologies/relationships#isOnPage> <info:fedora/uuid:75fca1f0-64b2-11dd-9fd4-000d606f5dc6> .
                end = inputLine.indexOf(">");
//18 je velikost   <info:fedora/uuid:
                inputLine = inputLine.substring(18, end);
                sb.append(inputLine);
                sb.append(";");
            }
            in.close();
            sb.delete(sb.length() - 1, sb.length());
            return sb.toString();

        } catch (Exception ex) {
            ex.printStackTrace();
            logger.severe(ex.toString());
            return "";
        }
    }

    public String getParents(String pid) {
        try {
            StringBuilder sb = new StringBuilder();
            //logger.info("getParents: " + pid);
            if (rindex == null) {
                rindex = ResourceIndexService.getResourceIndexImpl();
            }
            ArrayList<String> l = rindex.getParentsPids(pid);
            for (int i = 0; i < l.size(); i++) {
                sb.append(l.get(i));
                if (i < l.size() - 1) {
                    sb.append(";");
                }
            }

            return sb.toString();

        } catch (Exception ex) {
            ex.printStackTrace();
            logger.severe(ex.toString());
            return "";
        }
    }

    public String getDatastreamText(String pid, String dsId, String pageNum) throws Exception {
        //logger.fine("getDatastreamText" + " pid=" + pid + " repositoryName=" + repositoryName + " dsId=" + dsId + " fedoraSoap=" + fedoraSoap + " fedoraUser=" + fedoraUser + " fedoraPass=" + fedoraPass + " trustStorePath=" + trustStorePath + " trustStorePass=" + trustStorePass);

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
