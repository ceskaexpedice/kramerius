package cz.incad.kramerius.rest.api.k5.client.pdf;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.pdfbox.exceptions.COSVisitorException;
import org.apache.pdfbox.util.PDFMergerUtility;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;
import com.lowagie.text.DocumentException;

import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.ObjectPidsPath;
import cz.incad.kramerius.ProcessSubtreeException;
import cz.incad.kramerius.SolrAccess;
import cz.incad.kramerius.document.DocumentService;
import cz.incad.kramerius.document.model.AbstractRenderedDocument;
import cz.incad.kramerius.document.model.RenderedDocument;
import cz.incad.kramerius.pdf.FirstPagePDFService;
import cz.incad.kramerius.pdf.GeneratePDFService;
import cz.incad.kramerius.pdf.impl.ImageFetcher;
import cz.incad.kramerius.pdf.utils.pdf.FontMap;
import cz.incad.kramerius.rest.api.k5.client.JSONDecoratorsAggregate;
import cz.incad.kramerius.rest.api.k5.client.SolrMemoization;
import cz.incad.kramerius.rest.api.k5.client.pdf.utils.ITextUtils;
import cz.incad.kramerius.service.TextsService;
import cz.incad.kramerius.utils.conf.KConfiguration;

public class AbstractPDFResource {

    public enum FirstPage {
        IMAGES, TEXT;
    }

    public static final Logger LOGGER = Logger.getLogger(AbstractPDFResource.class.getName());
    
    @Inject
    @Named("TEXT")
    FirstPagePDFService textFirstPage;

    @Inject
    @Named("IMAGE")
    FirstPagePDFService imageFirstPage;

    @Inject
    @Named("securedFedoraAccess")
    FedoraAccess fedoraAccess;

    @Inject
    KConfiguration configuration;

    @Inject
    SolrAccess solrAccess;

    @Inject
    DocumentService documentService;

    @Inject
    Provider<HttpServletRequest> requestProvider;

    @Inject
    JSONDecoratorsAggregate decoratorsAggregate;

    
    @Inject
    SolrMemoization solrMemoization;

    @Inject
    TextsService textService;
    
    @Inject
    Provider<Locale> localesProvider;

    @Inject
    GeneratePDFService service;


    public static void checkNumber(String number) {
        String maxPage = KConfiguration.getInstance().getProperty(
                "generatePdfMaxRange");
        
        boolean turnOff = KConfiguration.getInstance().getConfiguration().getBoolean("turnOffPdfCheck");
        if (turnOff) return;
    
        if (Integer.parseInt(number) >= Integer.parseInt(maxPage)) {
            throw new PDFResourceBadRequestException("too much pages");
        }
    }


    public static void checkNumber(String[] pids) {
        String maxPage = KConfiguration.getInstance().getProperty(
                "generatePdfMaxRange");
        boolean turnOff = KConfiguration.getInstance().getConfiguration().getBoolean("turnOffPdfCheck");
        if (turnOff) return;
        if (pids.length >= Integer.parseInt(maxPage)) {
            throw new PDFResourceBadRequestException("too much pages");
        }
    }

    @GET
    @Path("conf")
    @Produces({ MediaType.APPLICATION_JSON + ";charset=utf-8" })
    public Response info() {
        JSONObject jsonObject = new JSONObject();
        try {
            String maxPage = KConfiguration.getInstance().getProperty(
                    "generatePdfMaxRange");
            jsonObject.put("maxpage", maxPage);
            
            boolean turnOff = KConfiguration.getInstance().getConfiguration().getBoolean("turnOffPdfCheck");
            jsonObject.put("turnOffPdfCheck", turnOff);
        } catch (JSONException e) {
            LOGGER.log(Level.SEVERE,e.getMessage(),e);
        }
        return Response.ok().entity(jsonObject.toString()).build();
    }

    public File parent(String pid, String number, FirstPagePDFService firstPagePDFService, GeneratePDFService pdfService,
            SolrAccess solrAccess, DocumentService documentService, String imgServletUrl, String i18nUrl, String srect)
            throws DocumentException, IOException, COSVisitorException,
            NumberFormatException, ProcessSubtreeException {
            
                FontMap fmap = new FontMap(pdfService.fontsFolder());
            
                AbstractPDFResource.checkNumber(number);
            
                ObjectPidsPath[] paths = solrAccess.getPath(pid);
                final ObjectPidsPath path = AbstractPDFResource.selectOnePath(pid, paths);
            
                RenderedDocument rdoc = new RenderedDocument(this.fedoraAccess.getKrameriusModelName(pid), pid);
                File parentFile = ITextUtils.bodyPDF(pid, Integer.parseInt(number), solrAccess, solrMemoization, this.decoratorsAggregate, fedoraAccess, fmap, rdoc,textService, localesProvider);
                File firstPageFile = ITextUtils.firstPagePDF(firstPagePDFService, imgServletUrl, i18nUrl,
                        fmap, path, rdoc);
                File generatedPDF = File.createTempFile("rendered", "pdf");
                FileOutputStream fos = new FileOutputStream(generatedPDF);
            
                AbstractPDFResource.mergeToOutput(fos, parentFile, firstPageFile);
            
                return generatedPDF;
            }

    static File selection(FirstPagePDFService firstPagePDFService,
            GeneratePDFService pdfService, DocumentService documentService,
            String imgServletUrl, String i18nUrl, String[] pids, String srect)
            throws IOException, FileNotFoundException, DocumentException,
            ProcessSubtreeException, COSVisitorException {
    
        List<File> filesToDelete = new ArrayList<File>();
        FileOutputStream generatedPDFFos = null;
    
        File tmpFile = File.createTempFile("body", "pdf");
        filesToDelete.add(tmpFile);
        FileOutputStream bodyTmpFos = new FileOutputStream(tmpFile);
        File fpage = File.createTempFile("head", "pdf");
        filesToDelete.add(fpage);
        FileOutputStream fpageFos = new FileOutputStream(fpage);
    
        int[] irects = srect(srect);
    
        FontMap fMap = new FontMap(pdfService.fontsFolder());
    
        AbstractRenderedDocument rdoc = documentService
                .buildDocumentFromSelection(pids, irects);
    
        firstPagePDFService.generateFirstPageForSelection(rdoc, fpageFos, pids,
                 i18nUrl, fMap);
    
        pdfService.generateCustomPDF(rdoc, bodyTmpFos, fMap, imgServletUrl,
                i18nUrl, ImageFetcher.WEB);
    
        bodyTmpFos.close();
        fpageFos.close();
    
        File generatedPDF = File.createTempFile("rendered", "pdf");
        generatedPDFFos = new FileOutputStream(generatedPDF);
    
        mergeToOutput(generatedPDFFos, tmpFile, fpage);
        return generatedPDF;
    }

    static ObjectPidsPath selectOnePath(String requestedPid,
            ObjectPidsPath[] paths) {
        ObjectPidsPath path;
        if (paths.length > 0) {
            path = paths[0];
        } else {
            path = new ObjectPidsPath(requestedPid);
        }
        return path;
    }

    static int[] srect(String srect) {
        int[] rect = null;
        if (srect != null) {
            String[] arr = srect.split(",");
            if (arr.length == 2) {
                rect = new int[2];
                rect[0] = Integer.parseInt(arr[0]);
                rect[1] = Integer.parseInt(arr[1]);
            }
        }
        return rect;
    }

    static void mergeToOutput(OutputStream fos, File bodyFile,
            File firstPageFile) throws IOException, COSVisitorException {
        PDFMergerUtility utility = new PDFMergerUtility();
        utility.addSource(firstPageFile);
        utility.addSource(bodyFile);
        utility.setDestinationStream(fos);
        utility.mergeDocuments();
    }

}
