package cz.incad.Kramerius.views.localprint;

import java.awt.Dimension;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.xml.xpath.XPathExpressionException;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;

import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.Initializable;
import cz.incad.kramerius.ObjectPidsPath;
import cz.incad.kramerius.SolrAccess;
import cz.incad.kramerius.rest.api.k5.client.SolrMemoization;
import cz.incad.kramerius.rest.api.k5.client.item.utils.ItemResourceUtils;
import cz.incad.kramerius.security.IsActionAllowed;
import cz.incad.kramerius.security.SecuredActions;
import cz.incad.kramerius.security.User;
import cz.incad.kramerius.utils.ApplicationURL;
import cz.incad.kramerius.utils.StringUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;
import cz.incad.kramerius.utils.imgs.KrameriusImageSupport;

public class PicturePrepareViewObject extends AbstractPrepareViewObject  implements Initializable{

    public static final Logger LOGGER = Logger.getLogger(PicturePrepareViewObject.class.getName());
    
    @Inject
    Provider<HttpServletRequest> servletRequestProvider;
    
    @Inject
    @Named("securedFedoraAccess")
    FedoraAccess fedoraAccess;

    @Inject
    Provider<User> userProvider;

    @Inject
    IsActionAllowed actionAllowed;
    
    @Inject
    SolrAccess solrAccess;
    
    @Inject
    SolrMemoization solrMemoization;
    
    private List<String> pids = new ArrayList<String>();
    
    
    @Override
    public void init() {
        try {
            double ratio = KConfiguration.getInstance().getConfiguration().getDouble("search.print.pageratio",1.414);
            HttpServletRequest request = this.servletRequestProvider.get();
            
            String pidsString = request.getParameter("pids");
            String startPid = request.getParameter("startPid");
            String stopPid = request.getParameter("stopPid");
            String parentPid = request.getParameter("parentPid");

            String[] pids = new String[0];
            if (StringUtils.isAnyString(pidsString)) {
                pids = pidsAsList(pidsString);
            } else if (StringUtils.isAnyString(startPid)){
                pids = pidsAsSiblings(startPid, stopPid);
            } else if (StringUtils.isAnyString(parentPid)){
                pids =  pidsAsChildren(parentPid);
            }

            String transcode = request.getParameter("transcode");
            int bits = numberOfBits(pids.length);
            for (int i = 0; i < pids.length; i++) {
                String p = pids[i];
                boolean canBeRead = canBeRead(p);
                if (canBeRead) {
                    p = this.fedoraAccess.findFirstViewablePid(p);
                    String ident = createIdent(i,bits); 
                    this.pids.add(URLDecoder.decode(p, "UTF-8"));

                    String url ="../img?pid="+URLEncoder.encode(p,"UTF-8")+"&stream=IMG_FULL&action="+(Boolean.parseBoolean(transcode) ? "TRANSCODE":"GETRAW");
                    String imageElement = "<img src='"+url+"' id='"+ident+"'></img>";
                    this.imgelements.add(imageElement);
                    
                    Dimension readDim = KrameriusImageSupport.readDimension(p, "IMG_FULL", fedoraAccess, 0);
                    LOGGER.fine("Dimension is :"+readDim);
                    createStyle(ratio, ident, readDim);
                }
            }
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        } catch (XPathExpressionException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    private String[] pidsAsChildren(String parentPid)
            throws IOException {
        String[] pids = new String[0];
        List<String> solrChildrenPids = ItemResourceUtils.solrChildrenPids(parentPid, new ArrayList<String>(), this.solrAccess, this.solrMemoization);
        return solrChildrenPids.toArray(new String[solrChildrenPids.size()]);
    }

    
    private String[] pidsAsSiblings( String startPid, String stopPid)
            throws IOException {
        String[] pids = new String[0];
        ObjectPidsPath[] paths = this.solrAccess.getPath(startPid);
        ObjectPidsPath pths = selectOnePath(startPid, paths);
        String[] pidsPths = pths.getPathFromRootToLeaf();
        if (pidsPths.length > 1) {
            String parent = pidsPths[pidsPths.length -2];
            List<String> solrChildrenPids = ItemResourceUtils.solrChildrenPids(parent, new ArrayList<String>(), this.solrAccess, this.solrMemoization);
            List<String> rest = new ArrayList<String>();
            boolean append = false;
            for (String pid : solrChildrenPids) {
                if ((!append) && pid.equals(startPid)) {
                    append = true;
                }
                if (append) {
                    rest.add(pid);
                }
                if ((append) && pid.equals(stopPid)) {
                    append = false;
                    break;
                }
            }
            pids = rest.toArray(new String[rest.size()]);
        }
        return pids;
    }


    private String[] pidsAsList(String pidsString) {
        String[] pids;
        pids = pidsString.split(",");
        return pids;
    }


    private boolean canBeRead(String pid) throws IOException {
        ObjectPidsPath[] paths = solrAccess.getPath(pid);
        for (ObjectPidsPath pth : paths) {
            if (this.actionAllowed.isActionAllowed(userProvider.get(), SecuredActions.READ.getFormalName(), pid, null, pth)) {
                return true;
            }
        }
        return false;
    }

    public static ObjectPidsPath selectOnePath(String requestedPid,
            ObjectPidsPath[] paths) {
        ObjectPidsPath path;
        if (paths.length > 0) {
            path = paths[0];
        } else {
            path = new ObjectPidsPath(requestedPid);
        }
        return path;
    }

}

