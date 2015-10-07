package cz.incad.kramerius.rest.api.k5.client.pdf;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.util.Locale;
import java.util.Map;
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
import com.lowagie.text.PageSize;
import com.lowagie.text.Rectangle;

import cz.incad.kramerius.AbstractObjectPath;
import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.ObjectModelsPath;
import cz.incad.kramerius.ObjectPidsPath;
import cz.incad.kramerius.ProcessSubtreeException;
import cz.incad.kramerius.SolrAccess;
import cz.incad.kramerius.document.DocumentService;
import cz.incad.kramerius.document.model.PreparedDocument;
import cz.incad.kramerius.pdf.FirstPagePDFService;
import cz.incad.kramerius.pdf.GeneratePDFService;
import cz.incad.kramerius.pdf.OutOfRangeException;
import cz.incad.kramerius.pdf.SimplePDFService;
import cz.incad.kramerius.pdf.utils.pdf.FontMap;
import cz.incad.kramerius.rest.api.k5.client.JSONDecoratorsAggregate;
import cz.incad.kramerius.rest.api.k5.client.SolrMemoization;
import cz.incad.kramerius.service.TextsService;
import cz.incad.kramerius.utils.conf.KConfiguration;

public class AbstractPDFResource {

    public enum FirstPage {
        IMAGES, TEXT;
    }

    public static final Logger LOGGER = Logger
            .getLogger(AbstractPDFResource.class.getName());

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
    GeneratePDFService deprectedService;

    @Inject
    SimplePDFService simplePdfService;

    
    
@GET
    @Path("conf")
    @Produces({ MediaType.APPLICATION_JSON + ";charset=utf-8" })
    public Response info() {
        JSONObject jsonObject = new JSONObject();
        try {
            String maxPage = KConfiguration.getInstance().getProperty(
                    "generatePdfMaxRange");
            jsonObject.put("maxpage", maxPage);

            boolean turnOff = KConfiguration.getInstance().getConfiguration()
                    .getBoolean("turnOffPdfCheck");
            jsonObject.put("turnOffPdfCheck", turnOff);
        } catch (JSONException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        }
        return Response.ok().entity(jsonObject.toString()).build();
    }

    
    public File selection(String[] pids, Rectangle rect,FirstPage fp) throws DocumentException, IOException, ProcessSubtreeException, OutOfRangeException, COSVisitorException {
        FontMap fmap = new FontMap(deprectedService.fontsFolder());

        //int[] irects = srect(srect);
        PreparedDocument rdoc = documentService.buildDocumentFromSelection(pids, new int[] {(int)rect.getWidth(), (int)rect.getHeight()});

        File parentFile = null;
        File firstPageFile = null;
        
        try {
            // abstract document
            parentFile = File.createTempFile("body", "pdf");
            FileOutputStream bodyTmpFos = new FileOutputStream(parentFile);

            firstPageFile = File.createTempFile("head", "pdf");
            FileOutputStream fpageFos = new FileOutputStream(firstPageFile);

            if (fp == FirstPage.IMAGES) {
                this.imageFirstPage.selection(rdoc, fpageFos, pids, fmap);
            } else {
                this.textFirstPage.selection(rdoc, fpageFos, pids, fmap);
            }
            this.simplePdfService.pdf(rdoc, bodyTmpFos,  fmap);

            File generatedPDF = File.createTempFile("rendered", "pdf");
            FileOutputStream fos = new FileOutputStream(generatedPDF);

            AbstractPDFResource.mergeToOutput(fos, parentFile, firstPageFile);
            return generatedPDF;
        } finally {
            saveDeleteFile(parentFile, firstPageFile);
        }
        
    }

    public File parent(String pid, int n, Rectangle rect, FirstPage fp) throws DocumentException,
            IOException, COSVisitorException, NumberFormatException,
            ProcessSubtreeException {

        FontMap fmap = new FontMap(deprectedService.fontsFolder());


        Map<String, AbstractObjectPath[]> pathsMap = solrAccess.getPaths(pid);

        ObjectPidsPath[] paths = (ObjectPidsPath[]) pathsMap.get(ObjectPidsPath.class.getName());
        ObjectModelsPath[] models = (ObjectModelsPath[]) pathsMap.get(ObjectModelsPath.class.getName());;

        final ObjectPidsPath path = AbstractPDFResource.selectOnePath(pid, paths);
        
        File parentFile = null;
        File firstPageFile = null;
        try {
            
            PreparedDocument rdoc = this.documentService.buildDocumentAsFlat(path, pid, n, new int[] {(int)rect.getWidth(), (int)rect.getHeight()});
            
            parentFile = File.createTempFile("body", "pdf");
            FileOutputStream bodyTmpFos = new FileOutputStream(parentFile);

            firstPageFile = File.createTempFile("head", "pdf");
            FileOutputStream fpageFos = new FileOutputStream(firstPageFile);

            if (fp == FirstPage.IMAGES) {
                this.imageFirstPage.parent(rdoc, fpageFos, path, fmap);
            } else {
                this.textFirstPage.parent(rdoc, fpageFos, path, fmap);
            }

            this.simplePdfService.pdf(rdoc, bodyTmpFos, fmap);

            File generatedPDF = File.createTempFile("rendered", "pdf");
            FileOutputStream fos = new FileOutputStream(generatedPDF);

            AbstractPDFResource.mergeToOutput(fos, parentFile, firstPageFile);
            return generatedPDF;
        } catch (OutOfRangeException e) {
            throw new PDFResourceBadRequestException(e.getMessage());
        } finally {
            saveDeleteFile(parentFile, firstPageFile);
        }
    }

//    static File selection(FirstPagePDFService firstPagePDFService,
//            GeneratePDFService pdfService, DocumentService documentService,
//            String imgServletUrl, String i18nUrl, String[] pids, String srect)
//            throws IOException, FileNotFoundException, DocumentException,
//            ProcessSubtreeException, COSVisitorException {
//
//        
//        File tmpFile = null;
//        File fpage = null;
//
//        try {
//            List<File> filesToDelete = new ArrayList<File>();
//            FileOutputStream generatedPDFFos = null;
//
//            tmpFile = File.createTempFile("body", "pdf");
//            filesToDelete.add(tmpFile);
//            FileOutputStream bodyTmpFos = new FileOutputStream(tmpFile);
//            fpage = File.createTempFile("head", "pdf");
//            filesToDelete.add(fpage);
//            FileOutputStream fpageFos = new FileOutputStream(fpage);
//
//            int[] irects = srect(srect);
//
//            FontMap fMap = new FontMap(pdfService.fontsFolder());
//
//            PreparedDocument rdoc = documentService
//                    .buildDocumentFromSelection(pids, irects);
//
//            firstPagePDFService.selection(rdoc, fpageFos, pids,
//                    i18nUrl, fMap);
//
//            pdfService.generateCustomPDF(rdoc, bodyTmpFos, fMap, imgServletUrl,
//                    i18nUrl, ImageFetcher.WEB);
//
//            bodyTmpFos.close();
//            fpageFos.close();
//
//            File generatedPDF = File.createTempFile("rendered", "pdf");
//            generatedPDFFos = new FileOutputStream(generatedPDF);
//
//            mergeToOutput(generatedPDFFos, tmpFile, fpage);
//            return generatedPDF;
//        } finally {
//            saveDeleteFile(tmpFile, fpage);
//        }
//    }

    private static void saveDeleteFile(File ... files) {
        for (File f : files) {
            if (f != null) {
                f.delete();
            }
        }
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

    /*
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
    }*/

    static void mergeToOutput(OutputStream fos, File bodyFile,
            File firstPageFile) throws IOException, COSVisitorException {
        PDFMergerUtility utility = new PDFMergerUtility();
        utility.addSource(firstPageFile);
        utility.addSource(bodyFile);
        utility.setDestinationStream(fos);
        utility.mergeDocuments();
    }

    public static void main(String[] args) throws IllegalArgumentException, IllegalAccessException {
        Field[] fields = PageSize.class.getFields();
        for (Field field : fields) {
            if (java.lang.reflect.Modifier.isStatic(field.getModifiers())) {
                String name = field.getName();
                Object value = field.get(null);
                System.out.println(""+name+" : "+value);
            }
        }
    }
    
    
}
