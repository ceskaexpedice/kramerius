package cz.incad.kramerius.pdf.impl;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.xpath.XPathExpressionException;

import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.imaging.ImageStreams;
import cz.incad.kramerius.utils.imgs.ImageMimeType;
import cz.incad.kramerius.utils.imgs.KrameriusImageSupport;
import org.ceskaexpedice.akubra.AkubraRepository;

public enum ImageFetcher {

    PROCESS {
        @Override
        public BufferedImage fetch(String pid, String imgServlet, ImageMimeType imageMimeType, AkubraRepository akubraRepository) throws IOException{
            try {
                BufferedImage javaImg = KrameriusImageSupport.readImage(new URL(createIMGFULL(pid, imgServlet)), imageMimeType, 0);
                return javaImg;
            } catch (MalformedURLException e) {
                throw new IOException(e);
            }
        }
    }, 
    WEB {
        @Override
        public BufferedImage fetch(String pid, String imgServlet, ImageMimeType imageMimeType, AkubraRepository akubraRepository) throws IOException {
            try {
                BufferedImage img = KrameriusImageSupport.readImage(pid, ImageStreams.IMG_FULL.name(), akubraRepository, 0);
                return img;
            } catch (XPathExpressionException e) {
                throw new IOException(e);
            }
        }
    };
    
    private static String createIMGFULL(String objectId, String imgServletUrl) {
        String imgUrl = imgServletUrl + "?uuid=" + objectId + "&action=GETRAW&stream=" + ImageStreams.IMG_FULL.getStreamName();
        return imgUrl;
    }

    public abstract BufferedImage fetch(String pid, String imgServlet, ImageMimeType mimeType, AkubraRepository akubraRepository) throws IOException;
}
