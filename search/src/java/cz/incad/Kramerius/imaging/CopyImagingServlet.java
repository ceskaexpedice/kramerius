package cz.incad.Kramerius.imaging;

import static cz.incad.kramerius.utils.IOUtils.copyStreams;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URLConnection;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import cz.incad.Kramerius.AbstractImageServlet;
import cz.incad.kramerius.utils.RESTHelper;

public class CopyImagingServlet extends AbstractImageServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        copyFromImageServer(req, resp);
    }

    private void copyFromImageServer(HttpServletRequest req,
            HttpServletResponse resp) throws MalformedURLException, IOException {
//        String urlString = "http://192.168.1.5/fcgi-bin/iipsrv.fcgi";
      
        //http://192.168.1.5/fcgi-bin/iipsrv.fcgi?DeepZoom=/var/www/test.jp2.dzi
        String urlString = "http://192.168.1.5/fcgi-bin/iipsrv.fcgi";
        urlString += "?"+req.getQueryString();
        System.out.println(urlString);
        URLConnection con = RESTHelper.openConnection(urlString, "", "");
        String contentType = con.getContentType();
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        copyStreams(con.getInputStream(), bos);
        //System.out.println(new String(bos.toByteArray()));
        copyStreams(new ByteArrayInputStream(bos.toByteArray()), resp.getOutputStream());
        resp.setContentType(contentType);
    }

    
}
