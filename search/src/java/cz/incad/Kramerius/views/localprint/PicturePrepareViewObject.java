package cz.incad.Kramerius.views.localprint;

import java.awt.Dimension;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.xml.xpath.XPathExpressionException;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;

import cz.incad.Kramerius.Initializable;
import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.utils.ApplicationURL;
import cz.incad.kramerius.utils.StringUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;
import cz.incad.kramerius.utils.imgs.KrameriusImageSupport;

public class PicturePrepareViewObject extends AbstractPrepareViewObject  implements Initializable{

    @Inject
    Provider<HttpServletRequest> servletRequestProvider;
    
    @Inject
    @Named("securedFedoraAccess")
    FedoraAccess fedoraAccess;

    private List<String> pids = new ArrayList<String>();
    
    
    @Override
    public void init() {
        try {
            double ratio = KConfiguration.getInstance().getConfiguration().getDouble("search.print.pageratio",1.414);
            HttpServletRequest request = this.servletRequestProvider.get();
            String pidsString = request.getParameter("pids");
            String[] pids = pidsString.split(",");
            String transcode = request.getParameter("transcode");
            
            int bits = numberOfBits(pids.length);

            for (int i = 0; i < pids.length; i++) {
                String p = pids[i];
                p = this.fedoraAccess.findFirstViewablePid(p);
                String ident = createIdent(i,bits); 
                this.pids.add(URLDecoder.decode(p, "UTF-8"));

                String url ="../img?pid="+URLEncoder.encode(p,"UTF-8")+"&stream=IMG_FULL&action="+(Boolean.parseBoolean(transcode) ? "TRANSCODE":"GETRAW");
                String imageElement = "<img src='"+url+"' id='"+ident+"'></img>";
                this.imgelements.add(imageElement);
                
                Dimension readDim = KrameriusImageSupport.readDimension(p, "IMG_FULL", fedoraAccess, 0);
                createStyle(ratio, ident, readDim);
            }
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        } catch (XPathExpressionException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

