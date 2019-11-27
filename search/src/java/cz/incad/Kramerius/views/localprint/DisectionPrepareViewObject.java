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

import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.Initializable;
import cz.incad.kramerius.utils.conf.KConfiguration;
import cz.incad.kramerius.utils.imgs.KrameriusImageSupport;

public class DisectionPrepareViewObject extends AbstractPrepareViewObject implements Initializable{

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
            
            String pidsString = request.getParameter("pid");
            pidsString = this.fedoraAccess.findFirstViewablePid(pidsString);
            String transcode = request.getParameter("transcode");
            String width = request.getParameter("width");
            String height = request.getParameter("height");
            String xpos = request.getParameter("xpos");
            String ypos = request.getParameter("ypos");

            int bits = numberOfBits(1);


            String ident = createIdent(0,bits); 
            this.pids.add(URLDecoder.decode(pidsString, "UTF-8"));

            String url ="../imgcut?pid="+URLEncoder.encode(pidsString,"UTF-8")+"&xpos="+xpos+"&ypos="+ypos+"&width="+width+"&height="+height;
            String imageElement = "<img src='"+url+"' id='"+ident+"'></img>";
            this.imgelements.add(imageElement);

            double dwidth = Double.parseDouble(width);
            double dheight = Double.parseDouble(height);
            int iwidth = (int)(dwidth * 1000);
            int iheight = (int)(dheight *1000);
            
            Dimension readDim =  new Dimension(iwidth, iheight);
            createStyle(ratio, ident, readDim);

        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
