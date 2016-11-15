package cz.incad.kramerius.solr;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import cz.incad.kramerius.utils.imgs.ImageMimeType;
import cz.incad.kramerius.utils.imgs.KrameriusImageSupport;

public class TestImageIO {
    
    public static void main(String[] args) throws MalformedURLException, IOException, InterruptedException {
        URL f = new URL("file:///C:/Users/pstastny.SEARCH/Documents/image_1.png");
        BufferedImage img = KrameriusImageSupport.readImage(f, ImageMimeType.PNG, -1);
        
        System.out.println(img);
        Thread.sleep(300000);
    }
}
