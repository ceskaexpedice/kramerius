package cz.incad.kramerius.rest.api.k5.client.pdf;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.List;
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

import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Rectangle;

import cz.incad.kramerius.AbstractObjectPath;
import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.MostDesirable;
import cz.incad.kramerius.ObjectModelsPath;
import cz.incad.kramerius.ObjectPidsPath;
import cz.incad.kramerius.ProcessSubtreeException;
import cz.incad.kramerius.SolrAccess;
import cz.incad.kramerius.document.DocumentService;
import cz.incad.kramerius.document.model.AbstractPage;
import cz.incad.kramerius.document.model.PreparedDocument;
import cz.incad.kramerius.pdf.FirstPagePDFService;
import cz.incad.kramerius.pdf.GeneratePDFService;
import cz.incad.kramerius.pdf.OutOfRangeException;
import cz.incad.kramerius.pdf.SimplePDFService;
import cz.incad.kramerius.pdf.utils.pdf.FontMap;
import cz.incad.kramerius.rest.api.k5.client.JSONDecoratorsAggregate;
import cz.incad.kramerius.rest.api.k5.client.SolrMemoization;
import cz.incad.kramerius.security.IsActionAllowed;
import cz.incad.kramerius.security.SecuredActions;
import cz.incad.kramerius.security.SecurityException;
import cz.incad.kramerius.security.User;
import cz.incad.kramerius.service.TextsService;
import cz.incad.kramerius.statistics.ReportedAction;
import cz.incad.kramerius.statistics.StatisticsAccessLog;
import cz.incad.kramerius.utils.FedoraUtils;
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
    FirstPagePDFService imageFirstPage  ;

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

    
    @Inject
    MostDesirable mostDesirable;
    
    
    @Inject
    IsActionAllowed actionAllowed;
    
    @Inject
    Provider<User> userProvider;
    
    @Inject
    StatisticsAccessLog statisticsAccessLog;
    
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

    
    public File selection(String[] pids, Rectangle rect,FirstPage fp) throws DocumentException, IOException, ProcessSubtreeException, OutOfRangeException {
        FontMap fmap = new FontMap(deprectedService.fontsFolder());

        PreparedDocument rdoc = documentService.buildDocumentFromSelection(pids, new int[] {(int)rect.getWidth(), (int)rect.getHeight()});
        checkRenderedPDFDoc(rdoc);

        File parentFile = null;
        File firstPageFile = null;
        
        try {
            // abstract document
            parentFile = File.createTempFile("body", "pdf");
            FileOutputStream bodyTmpFos = new FileOutputStream(parentFile);

            firstPageFile = File.createTempFile("head", "pdf");
            FileOutputStream fpageFos = new FileOutputStream(firstPageFile);
            
            // most desirable
            for (String p : pids) {
                this.mostDesirable.saveAccess(p, new Date());
                try {
                    this.statisticsAccessLog.reportAccess(p, FedoraUtils.IMG_FULL_STREAM, ReportedAction.PDF.name());
                } catch (Exception e) {
                    LOGGER.severe("cannot write statistic records");
                    LOGGER.log(Level.SEVERE, e.getMessage(),e);
                }
            }

            
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
            IOException, NumberFormatException,
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
            checkRenderedPDFDoc(rdoc);
            
            
            this.mostDesirable.saveAccess(pid, new Date());
            for (AbstractPage p : rdoc.getPages()) {
                try {
                    this.statisticsAccessLog.reportAccess(p.getUuid(), FedoraUtils.IMG_FULL_STREAM, ReportedAction.PDF.name());
                } catch (Exception e) {
                    LOGGER.severe("cannot write statistic records");
                    LOGGER.log(Level.SEVERE, e.getMessage(),e);
                }
            }

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


    private void checkRenderedPDFDoc(PreparedDocument rdoc) throws IOException {
        List<AbstractPage> pages = rdoc.getPages();
        for (AbstractPage apage : pages) {
            if (!this.canBeRenderedAsPDF(apage.getUuid())) {
                throw new SecurityException(new SecurityException.SecurityExceptionInfo(SecuredActions.PDF_RESOURCE, apage.getUuid()));
            }
        }
    }


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

    static void mergeToOutput(OutputStream fos, File bodyFile,
            File firstPageFile) throws IOException {
        PDFMergerUtility utility = new PDFMergerUtility();
        utility.addSource(firstPageFile);
        utility.addSource(bodyFile);
        utility.setDestinationStream(fos);
        utility.mergeDocuments();
    }

    

    private boolean canBeRenderedAsPDF(String pid) throws IOException {
        ObjectPidsPath[] paths = solrAccess.getPath(pid);
        for (ObjectPidsPath pth : paths) {
            if (this.actionAllowed.isActionAllowed(userProvider.get(), SecuredActions.PDF_RESOURCE.getFormalName(), pid, null, pth)) {
                return true;
            }
        }
        return false;
    }


 
    
    
}
