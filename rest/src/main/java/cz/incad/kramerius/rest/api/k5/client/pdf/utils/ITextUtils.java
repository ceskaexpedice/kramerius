package cz.incad.kramerius.rest.api.k5.client.pdf.utils;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.xpath.XPathExpressionException;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.google.inject.Provider;
import com.lowagie.text.BadElementException;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.PdfWriter;

import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.ObjectPidsPath;
import cz.incad.kramerius.SolrAccess;
import cz.incad.kramerius.document.model.RenderedDocument;
import cz.incad.kramerius.imaging.ImageStreams;
import cz.incad.kramerius.pdf.FirstPagePDFService;
import cz.incad.kramerius.pdf.utils.pdf.DocumentUtils;
import cz.incad.kramerius.pdf.utils.pdf.FontMap;
import cz.incad.kramerius.rest.api.k5.client.JSONDecoratorsAggregate;
import cz.incad.kramerius.rest.api.k5.client.SolrMemoization;
import cz.incad.kramerius.rest.api.k5.client.item.utils.ItemResourceUtils;
import cz.incad.kramerius.security.SecurityException;
import cz.incad.kramerius.service.TextsService;
import cz.incad.kramerius.utils.imgs.KrameriusImageSupport;

public class ITextUtils {

    public static final Logger LOGGER = Logger.getLogger(ITextUtils.class.getName());
    
    public static File bodyPDF(String pid, int maxnumber,SolrAccess solrAccess, SolrMemoization solrMemoization, JSONDecoratorsAggregate decoratorsAggregate, FedoraAccess fedoraAccess,
            FontMap fmap,  RenderedDocument rdoc, TextsService textService, Provider<Locale> localesProvider)
            throws IOException, BadElementException, MalformedURLException,
            DocumentException {

        File tmpFile = File.createTempFile("body", "pdf");
        Document doc = null;
        FileOutputStream bodyTmpFos = null;
        try {
            
            bodyTmpFos = new FileOutputStream(tmpFile);
            doc = DocumentUtils.createDocument(rdoc);

            PdfWriter writer = PdfWriter.getInstance(doc, bodyTmpFos);
            doc.open();

            doc.newPage();

            JSONArray jsonArray = ItemResourceUtils.decoratedJSONChildren(pid, solrAccess, solrMemoization, decoratorsAggregate);
            for (int i = 0; i < jsonArray.size(); i++) {
                doc.newPage();
                if (maxnumber >= -1 && i >= maxnumber) break;
                JSONObject jsonObj = jsonArray.getJSONObject(i);
                if (jsonObj.getString("model").equals("page")) {
                    String pagePid = jsonObj.getString("pid");
                    try {
                        BufferedImage javaImg = KrameriusImageSupport.readImage(pagePid,ImageStreams.IMG_FULL.getStreamName(), fedoraAccess,0);
                        Images.insertJavaImage(doc, 1.0f, javaImg);
                    } catch (XPathExpressionException e) {
                        LOGGER.log(Level.SEVERE,e.getMessage(),e);
                    } catch (SecurityException e) {
                        LOGGER.log(Level.SEVERE,e.getMessage(),e);
                        Font regFont = fmap.getRegistredFont("normal");
                        Images.imageNotAvailable(doc, textService, localesProvider, regFont);
                    }
                }
                
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE,e.getMessage(),e);
        } finally {
            if (doc != null) doc.close();
            org.apache.commons.io.IOUtils.closeQuietly(bodyTmpFos);
        }
        
        return tmpFile;
    }

    public static File firstPagePDF(FirstPagePDFService firstPagePDFService,
            String imgServletUrl, String i18nUrl, FontMap fmap,
            final ObjectPidsPath path, RenderedDocument rdoc)
            throws IOException, FileNotFoundException {
        File fpage = File.createTempFile("head", "pdf");
        FileOutputStream fpageFos = new FileOutputStream(fpage);
        firstPagePDFService.generateFirstPageForParent(rdoc, fpageFos, path, i18nUrl, fmap);
        fpageFos.close();
        return fpage;
    }

}
