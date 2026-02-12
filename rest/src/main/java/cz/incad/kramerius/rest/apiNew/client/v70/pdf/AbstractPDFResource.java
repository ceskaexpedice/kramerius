package cz.incad.kramerius.rest.apiNew.client.v70.pdf;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Rectangle;
import cz.incad.kramerius.*;
import cz.incad.kramerius.document.DocumentService;
import cz.incad.kramerius.document.model.AbstractPage;
import cz.incad.kramerius.document.model.AkubraDocument;
import cz.incad.kramerius.pdf.FirstPagePDFService;
import cz.incad.kramerius.pdf.GeneratePDFService;
import cz.incad.kramerius.pdf.OutOfRangeException;
import cz.incad.kramerius.pdf.SimplePDFService;
import cz.incad.kramerius.pdf.impl.ConfigurationUtils;
import cz.incad.kramerius.pdf.utils.pdf.FontMap;
import cz.incad.kramerius.rest.api.k5.client.SolrMemoization;
import cz.incad.kramerius.rest.api.k5.client.pdf.PDFResourceBadRequestException;
import cz.incad.kramerius.security.*;
import cz.incad.kramerius.security.SecurityException;
import cz.incad.kramerius.service.TextsService;
import cz.incad.kramerius.statistics.ReportedAction;
import cz.incad.kramerius.statistics.accesslogs.AggregatedAccessLogs;
import cz.incad.kramerius.utils.FedoraUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;
import org.apache.commons.configuration.Configuration;
import org.apache.pdfbox.multipdf.PDFMergerUtility;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Replaces cz.incad.kramerius.rest.api.k5.client.pdf.AbstractPDFResource
 */
public class AbstractPDFResource {
    public enum FirstPageType {
        IMAGES, TEXT;
    }

    public static final Logger LOGGER = Logger.getLogger(AbstractPDFResource.class.getName());

    final boolean PDF_ENDPOINTS_DISABLED = false; //TODO: remove after endpoints are fixed


    @Inject
    @Named("TEXT")
    FirstPagePDFService textFirstPage;

    @Inject
    @Named("IMAGE")
    FirstPagePDFService imageFirstPage;

    @Inject
    SecuredAkubraRepository akubraRepository;

    KConfiguration configuration = KConfiguration.getInstance();

    @Inject
    @Named("new-index")
    SolrAccess solrAccess;

    @Inject
    DocumentService documentService;

    @Inject
    Provider<HttpServletRequest> requestProvider;

//    @Inject
//    JSONDecoratorsAggregate decoratorsAggregate;

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
    RightsResolver rightsResolver;

    @Inject
    Provider<User> userProvider;

    @Inject
    AggregatedAccessLogs statisticsAccessLog;

    public File selection(String[] pids, Rectangle rect, FirstPageType fp) throws DocumentException, IOException, OutOfRangeException {
        FontMap fmap = new FontMap(deprectedService.fontsFolder());

        AkubraDocument rdoc = documentService.buildDocumentFromSelection(pids, new float[]{ rect.getWidth(), rect.getHeight()});
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
                reportAccess(p);
            }

            if (fp == FirstPageType.IMAGES) {
                this.imageFirstPage.selection(rdoc, fpageFos, pids, fmap, null);
            } else {
                this.textFirstPage.selection(rdoc, fpageFos, pids, fmap, null);
            }
            this.simplePdfService.pdf(rdoc, bodyTmpFos, fmap);

            File generatedPDF = File.createTempFile("rendered", "pdf");
            FileOutputStream fos = new FileOutputStream(generatedPDF);

            mergeToOutput(fos, parentFile, firstPageFile);
            return generatedPDF;
        } finally {
            saveDeleteFile(parentFile, firstPageFile);
        }
    }

    public File parent(String pid, int numberOfPags, Rectangle rect, FirstPageType firstPageType) throws DocumentException, IOException, NumberFormatException {
        LOGGER.info("parent(" + pid + ", ...)"); //TODO: remove for production
        FontMap fmap = new FontMap(deprectedService.fontsFolder());
        //Map<String, AbstractObjectPath[]> pathsMap = solrAccess.getModelAndPidPaths(pid);
        //ObjectPidsPath[] paths = (ObjectPidsPath[]) pathsMap.get(ObjectPidsPath.class.getName());

        ObjectPidsPath[] paths = solrAccess.getPidPaths(pid);
        /*System.out.println("paths: " + paths.length);
        for (ObjectPidsPath path1 : paths) {
            System.out.println(path1);
        }*/

        final ObjectPidsPath path = selectOnePath(pid, paths);
        //System.out.println("path: " + path);

        File parentFile = null;
        File firstPageFile = null;
        try {
            int howMany1 = ConfigurationUtils.checkNumber(numberOfPags, KConfiguration.getInstance().getConfiguration());
            AkubraDocument rdoc = this.documentService.buildDocumentAsFlat(path, pid, howMany1, new float[]{rect.getWidth(), rect.getHeight()});
            checkRenderedPDFDoc(rdoc);

            this.mostDesirable.saveAccess(pid, new Date());
            for (AbstractPage p : rdoc.getPages()) {
                reportAccess(p.getUuid());
            }

            parentFile = File.createTempFile("body", "pdf");
            FileOutputStream bodyTmpFos = new FileOutputStream(parentFile);

            firstPageFile = File.createTempFile("head", "pdf");
            FileOutputStream fpageFos = new FileOutputStream(firstPageFile);

            if (firstPageType == FirstPageType.IMAGES) {
                this.imageFirstPage.parent(rdoc, fpageFos, path, fmap, null);
            } else {
                this.textFirstPage.parent(rdoc, fpageFos, path, fmap,null);
            }

            this.simplePdfService.pdf(rdoc, bodyTmpFos, fmap);

            File generatedPDF = File.createTempFile("rendered", "pdf");
            FileOutputStream fos = new FileOutputStream(generatedPDF);

            mergeToOutput(fos, parentFile, firstPageFile);
            return generatedPDF;
        } catch (OutOfRangeException e) {
            throw new PDFResourceBadRequestException(e.getMessage());
        } finally {
            saveDeleteFile(parentFile, firstPageFile);
        }
    }

    private void checkRenderedPDFDoc(AkubraDocument rdoc) throws IOException {
        List<AbstractPage> pages = rdoc.getPages();
        for (AbstractPage apage : pages) {
            if (!this.canBeRenderedAsPDF(apage.getUuid())) {
                throw new SecurityException(new SecurityException.SecurityExceptionInfo(SecuredActions.A_PDF_READ, apage.getUuid()));
            }
        }
    }

    private static void saveDeleteFile(File... files) {
        for (File f : files) {
            if (f != null) {
                f.delete();
            }
        }
    }

    static ObjectPidsPath selectOnePath(String requestedPid, ObjectPidsPath[] paths) {
        ObjectPidsPath path;
        if (paths.length > 0) {
            path = paths[0];
        } else {
            path = new ObjectPidsPath(requestedPid);
        }
        return path;
    }

    static void mergeToOutput(OutputStream fos, File bodyFile, File firstPageFile) throws IOException {
        PDFMergerUtility utility = new PDFMergerUtility();
        utility.addSource(firstPageFile);
        utility.addSource(bodyFile);
        utility.setDestinationStream(fos);
        utility.mergeDocuments();
    }

    private boolean canBeRenderedAsPDF(String pid) throws IOException {
        ObjectPidsPath[] paths = solrAccess.getPidPaths(pid);
        for (ObjectPidsPath pth : paths) {
            if (this.rightsResolver.isActionAllowed(userProvider.get(), SecuredActions.A_PDF_READ.getFormalName(), pid, null, pth.injectRepository()).flag()) {
                return true;
            }
        }
        return false;
    }

    private void reportAccess(String pid) {
        try {
            this.statisticsAccessLog.reportAccess(pid, FedoraUtils.IMG_FULL_STREAM, ReportedAction.PDF.name());
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Can't write statistic records for " + pid, e);
        }
    }

    //replaces ConfigurationUtils.checkNumber(string)
    int extractNumberOfPages(String numberOfPagesStr) throws OutOfRangeException {
        Configuration config = KConfiguration.getInstance().getConfiguration();
        boolean ignoreMaxRange = config.getBoolean("turnOffPdfCheck");
        int maxRange = config.getInt("generatePdfMaxRange");
        if (numberOfPagesStr == null || numberOfPagesStr.trim().isEmpty()) { //numberOfPages not specified
            return ignoreMaxRange ? Integer.MAX_VALUE : maxRange;
        }
        //numberOfPages specified
        int numberOfPages = Integer.valueOf(numberOfPagesStr);
        if (!ignoreMaxRange && numberOfPages > maxRange) {
            throw new OutOfRangeException(String.format("too many pages (requested: %d, max: %d)", numberOfPages, maxRange));
        }
        return numberOfPages;
    }

    FirstPageType extractFirstPageType(String firstPageTypeStr) {
        return firstPageTypeStr == null || firstPageTypeStr.trim().isEmpty()
                ? FirstPageType.TEXT
                : FirstPageType.valueOf(firstPageTypeStr);
    }
}
