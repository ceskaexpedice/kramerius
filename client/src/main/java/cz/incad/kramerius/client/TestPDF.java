package cz.incad.kramerius.client;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageNode;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.graphics.xobject.PDXObjectImage;

import cz.incad.kramerius.utils.IOUtils;
import cz.incad.kramerius.utils.imgs.ImageMimeType;
import cz.incad.kramerius.utils.imgs.KrameriusImageSupport;

public class TestPDF {

    public static final String PATHNAME = "/home/pavels/tmp/smpdf/SM.pdf";
    public static final String OUTDIR = "/home/pavels/tmp/smpdf/";
    
    public static void main(String[] args) throws IOException {
        //generateImage();

                disectImages();
    }

    private static void disectImages() {
        try {
            File oldFile = new File(PATHNAME);
            if (oldFile.exists()) {
            PDDocument document = PDDocument.load(oldFile);
            PDDocumentCatalog documentCatalog = document.getDocumentCatalog();
            List<PDPage> list = document.getDocumentCatalog().getAllPages();
            if (list.size() > 0) {
                PDPage pdPage = list.get(0);
                PDResources pdResources = pdPage.getResources();
                Map pageImages = pdResources.getImages();
                if (pageImages != null) {
                    int totalImages = 0;
                    Iterator imageIter = pageImages.keySet().iterator();
                    while (imageIter.hasNext()) {
                        String key = (String) imageIter.next();
                        PDXObjectImage pdxObjectImage = (PDXObjectImage) pageImages.get(key);
                        pdxObjectImage.write2file(new File(OUTDIR + "img"+ "_" + totalImages));
                        totalImages++;
                    }
                }
                
            }
        } else {
            System.err.println("File not exists");
        }
         } catch (Exception e) {
        e.printStackTrace();
         }
    }

    private static void generateImage() throws MalformedURLException,
            FileNotFoundException, IOException {
        String pathname = "/home/pavels/tmp/smpdf/SM.pdf";
        File f = new File(pathname);
        URL url = f.toURI().toURL();

        File out = new File("/home/pavels/tmp/smpdf/test.png");
        FileOutputStream fos = new FileOutputStream(out);
        
        BufferedImage img = KrameriusImageSupport.readImage(url, ImageMimeType.PDF, 0);
        ImageIO.write(img, "png", fos);
        
        fos.close();
    }
}
