package cz.incad.Kramerius;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.pdfbox.multipdf.PDFMergerUtility;

import antlr.RecognitionException;
import antlr.TokenStreamException;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;
import com.lowagie.text.DocumentException;

import cz.incad.Kramerius.backend.guice.GuiceServlet;
import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.ObjectPidsPath;
import cz.incad.kramerius.ProcessSubtreeException;
import cz.incad.kramerius.SolrAccess;
import cz.incad.kramerius.document.DocumentService;
import cz.incad.kramerius.document.model.PreparedDocument;
import cz.incad.kramerius.pdf.FirstPagePDFService;
import cz.incad.kramerius.pdf.GeneratePDFService;
import cz.incad.kramerius.pdf.OutOfRangeException;
import cz.incad.kramerius.pdf.SimplePDFService;
import cz.incad.kramerius.pdf.impl.ImageFetcher;
import cz.incad.kramerius.pdf.utils.PDFExlusiveGenerateSupport;
import cz.incad.kramerius.pdf.utils.pdf.FontMap;
import cz.incad.kramerius.utils.ApplicationURL;
import cz.incad.kramerius.utils.conf.KConfiguration;
import cz.incad.kramerius.utils.params.ParamsLexer;
import cz.incad.kramerius.utils.params.ParamsParser;

@Deprecated
public class GeneratePDFServlet extends GuiceServlet {
    
    //TODO: Delete it; it is not necessary; all requests are routing through Remote API
    // stores handle for pdf
    private static HashMap<String, File> PREPARED_FILES = new HashMap<String, File>();

    private static final long serialVersionUID = 1L;

    public static final java.util.logging.Logger LOGGER = java.util.logging.Logger
            .getLogger(GeneratePDFServlet.class.getName());

    public static final String UUID_FROM = "uuidFrom";
    public static final String PID_FROM = "pidFrom";
    public static final String UUID_TO = "uuidTo";
    public static final String HOW_MANY = "howMany";
    public static final String PATH = "path";

    @Inject 
    SimplePDFService simplePDFService;
    
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

    public static synchronized void pushFile(String uuid, File renderedPDF) {
        PREPARED_FILES.put(uuid, renderedPDF);
    }

    public static synchronized File popFile(String uuid) {
        return PREPARED_FILES.remove(uuid);
    }

    @Override
    public void init() throws ServletException {
        super.init();
        LOGGER.fine("initializing servlet ...");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException {
        boolean acquired = false;
    }

    static void renderGenericError(HttpServletRequest req,
            HttpServletResponse resp) throws IOException, ServletException {
        LOGGER.info("error while generating pdf document");
        resp.setContentType("text/plain");
        resp.getWriter().println(
                "{" + "errorType:'generateerror',\n"
                        + "redirect:'generatepdferror.jsp',\n" + "returnUrl:'"
                        + req.getParameter("redirectURL") + "'" + "}");

    }

    static void renderErrorServerBusy(HttpServletRequest req,
            HttpServletResponse resp) throws IOException, ServletException {
        LOGGER.info("server busy forward");
        resp.setContentType("text/plain");
        resp.getWriter().println(
                "{" + "errorType:'serverbusy',\n"
                        + "redirect:'serverbusy.jsp',\n" + "returnUrl:'"
                        + req.getParameter("redirectURL") + "'" + "}");

    }

    static void renderErrorTooMuchPages(HttpServletRequest req,
            HttpServletResponse resp) throws IOException, ServletException {
        LOGGER.info("server busy forward");

        resp.setContentType("text/plain");
        resp.getWriter().println(
                "{" + "errorType:'maxpage',\n"
                        + "redirect:'pdfmaxpageserror.jsp',\n" + "returnUrl:'"
                        + req.getParameter("redirectURL") + "'" + "}");
    }

    public void renderPDF(HttpServletRequest req, HttpServletResponse resp)
            throws MalformedURLException, IOException, ProcessSubtreeException {

    }

    public enum FirstPage {
        IMAGES, TEXT;
    }

    public enum Action {
        SELECTION {
            @SuppressWarnings("unchecked")
            @Override
            public void renderPDF(HttpServletRequest request,
                    HttpServletResponse response,
                    FirstPagePDFService firstPagePDFService,
                    GeneratePDFService pdfService, SolrAccess solrAccess,
                    DocumentService documentService, String titlePage,
                    String imgServletUrl, String i18nUrl) {

                
                List<File> filesToDelete = new ArrayList<File>();
                FileOutputStream generatedPDFFos = null;
                try {

                    long start = System.currentTimeMillis();
                    String par = request.getParameter(PIDS);
                    String srect = request.getParameter(RECT);
                    ParamsParser parser = new ParamsParser(new ParamsLexer(
                            new StringReader(par)));
                    List params = parser.params();

                    boolean tbool = KConfiguration.getInstance()
                            .getConfiguration().getBoolean("turnOffPdfCheck");
                    boolean size = params.size() <= Integer
                            .parseInt(KConfiguration.getInstance().getProperty(
                                    "generatePdfMaxRange"));
                    if (tbool || size) {

                        File tmpFile = File.createTempFile("body", "pdf");
                        filesToDelete.add(tmpFile);
                        FileOutputStream bodyTmpFos = new FileOutputStream(
                                tmpFile);
                        File fpage = File.createTempFile("head", "pdf");
                        filesToDelete.add(fpage);
                        FileOutputStream fpageFos = new FileOutputStream(fpage);

                        int[] irects = srect(srect);

                        FontMap fMap = new FontMap(pdfService.fontsFolder());

                        PreparedDocument rdoc = documentService
                                .buildDocumentFromSelection((String[]) params
                                        .toArray(new String[params.size()]),
                                        irects);
                        LOGGER.fine("creating documents takes "
                                + (System.currentTimeMillis() - start) + " ms ");

                        start = System.currentTimeMillis();
                        firstPagePDFService.selection(rdoc,
                                fpageFos, (String[]) params
                                        .toArray(new String[params.size()]), fMap);
                        LOGGER.fine("generating first page takes "
                                + (System.currentTimeMillis() - start) + " ms ");

                        start = System.currentTimeMillis();
                        pdfService.generateCustomPDF(rdoc, bodyTmpFos, fMap,
                                imgServletUrl, i18nUrl, ImageFetcher.WEB);
                        LOGGER.fine("generating custom pdf takes "
                                + (System.currentTimeMillis() - start) + " ms ");

                        bodyTmpFos.close();
                        fpageFos.close();

                        File generatedPDF = File.createTempFile("rendered",
                                "pdf");
                        generatedPDFFos = new FileOutputStream(generatedPDF);

                        start = System.currentTimeMillis();
                        mergeToOutput(generatedPDFFos, tmpFile, fpage);
                        LOGGER.fine("merging document pdf takes "
                                + (System.currentTimeMillis() - start) + " ms ");

                        outputJSON(response, generatedPDF, generatedPDFFos,
                                tmpFile, fpage);

                    } else {
                        renderErrorTooMuchPages(request, response);
                    }

                } catch (IOException e) {
                    LOGGER.log(Level.SEVERE, e.getMessage(), e);
                    try {
                        renderGenericError(request, response);
                    } catch (ServletException e1) {
                        LOGGER.log(Level.SEVERE, e1.getMessage(), e1);
                    } catch (IOException e1) {
                        LOGGER.log(Level.SEVERE, e1.getMessage(), e1);
                    }
                } catch (ProcessSubtreeException e) {
                    LOGGER.log(Level.SEVERE, e.getMessage(), e);
                    try {
                        renderGenericError(request, response);
                    } catch (ServletException e1) {
                        LOGGER.log(Level.SEVERE, e1.getMessage(), e1);
                    } catch (IOException e1) {
                        LOGGER.log(Level.SEVERE, e1.getMessage(), e1);
                    }
                } catch (RecognitionException e) {
                    LOGGER.log(Level.SEVERE, e.getMessage(), e);
                    try {
                        renderGenericError(request, response);
                    } catch (ServletException e1) {
                        LOGGER.log(Level.SEVERE, e1.getMessage(), e1);
                    } catch (IOException e1) {
                        LOGGER.log(Level.SEVERE, e1.getMessage(), e1);
                    }
                } catch (TokenStreamException e) {
                    LOGGER.log(Level.SEVERE, e.getMessage(), e);
                    try {
                        renderGenericError(request, response);
                    } catch (ServletException e1) {
                        LOGGER.log(Level.SEVERE, e1.getMessage(), e1);
                    } catch (IOException e1) {
                        LOGGER.log(Level.SEVERE, e1.getMessage(), e1);
                    }
                } catch (DocumentException e) {
                    LOGGER.log(Level.SEVERE, e.getMessage(), e);
                    try {
                        renderGenericError(request, response);
                    } catch (ServletException e1) {
                        LOGGER.log(Level.SEVERE, e1.getMessage(), e1);
                    } catch (IOException e1) {
                        LOGGER.log(Level.SEVERE, e1.getMessage(), e1);
                    }
                } catch (ServletException e) {
                    LOGGER.log(Level.SEVERE, e.getMessage(), e);
                    try {
                        renderGenericError(request, response);
                    } catch (ServletException e1) {
                        LOGGER.log(Level.SEVERE, e1.getMessage(), e1);
                    } catch (IOException e1) {
                        LOGGER.log(Level.SEVERE, e1.getMessage(), e1);
                    }
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, e.getMessage(), e);
                    try {
                        renderGenericError(request, response);
                    } catch (ServletException e1) {
                        LOGGER.log(Level.SEVERE, e1.getMessage(), e1);
                    } catch (IOException e1) {
                        LOGGER.log(Level.SEVERE, e1.getMessage(), e1);
                    }
                } finally {
                    for (File file : filesToDelete) {
                        file.delete();
                    }
                    if (generatedPDFFos != null)
                        IOUtils.closeQuietly(generatedPDFFos);
                }
            }

        },
        PARENT {
            @Override
            public void renderPDF(HttpServletRequest request,
                    HttpServletResponse response,
                    FirstPagePDFService firstPagePDFService,
                    GeneratePDFService pdfService, SolrAccess solrAccess,
                    DocumentService documentService, String titlePage,
                    String imgServletUrl, String i18nUrl) {
                List<File> filesToDelete = new ArrayList<File>();
                FileOutputStream generatedPDFFos = null;
                try {
                    FontMap fmap = new FontMap(pdfService.fontsFolder());
                    String howMany = request.getParameter(HOW_MANY);
                    String pid = request.getParameter(PID_FROM);
                    String srect = request.getParameter(RECT);

                    boolean tbool = KConfiguration.getInstance()
                            .getConfiguration().getBoolean("turnOffPdfCheck");
                    boolean size = Integer.parseInt(howMany) <= Integer
                            .parseInt(KConfiguration.getInstance().getProperty(
                                    "generatePdfMaxRange"));
                    if (tbool || size) {

                        File tmpFile = File.createTempFile("body", "pdf");
                        filesToDelete.add(tmpFile);

                        FileOutputStream bodyTmpFos = new FileOutputStream(
                                tmpFile);
                        File fpage = File.createTempFile("head", "pdf");
                        filesToDelete.add(fpage);

                        FileOutputStream fpageFos = new FileOutputStream(fpage);

                        ObjectPidsPath[] paths = solrAccess.getPath(pid);
                        final ObjectPidsPath path = selectOnePath(pid, paths);

                        int[] irects = srect(srect);


                        PreparedDocument rdoc = documentService
                                .buildDocumentAsFlat(path, pid,
                                        Integer.parseInt(howMany), irects);
                        if (rdoc.getPages().isEmpty()) {
                            rdoc = documentService.buildDocumentAsFlat(path,
                                    path.getLeaf(), Integer.parseInt(howMany),
                                    irects);
                        }

                        firstPagePDFService.parent(rdoc,
                                fpageFos, path,   fmap);

                        pdfService.generateCustomPDF(rdoc, bodyTmpFos, fmap,
                                imgServletUrl, i18nUrl, ImageFetcher.WEB);

                        bodyTmpFos.close();
                        fpageFos.close();

                        File generatedPDF = File.createTempFile("rendered",
                                "pdf");
                        generatedPDFFos = new FileOutputStream(generatedPDF);

                        mergeToOutput(generatedPDFFos, tmpFile, fpage);

                        outputJSON(response, generatedPDF, generatedPDFFos,
                                tmpFile, fpage);

                    } else {
                        renderErrorTooMuchPages(request, response);
                    }

                } catch (IOException e) {
                    LOGGER.log(Level.SEVERE, e.getMessage(), e);
                    try {
                        renderGenericError(request, response);
                    } catch (ServletException e1) {
                        LOGGER.log(Level.SEVERE, e1.getMessage(), e1);
                    } catch (IOException e1) {
                        LOGGER.log(Level.SEVERE, e1.getMessage(), e1);
                    }
                } catch (ProcessSubtreeException e) {
                    LOGGER.log(Level.SEVERE, e.getMessage(), e);
                    try {
                        renderGenericError(request, response);
                    } catch (ServletException e1) {
                        LOGGER.log(Level.SEVERE, e1.getMessage(), e1);
                    } catch (IOException e1) {
                        LOGGER.log(Level.SEVERE, e1.getMessage(), e1);
                    }
                } catch (DocumentException e) {
                    LOGGER.log(Level.SEVERE, e.getMessage(), e);
                    try {
                        renderGenericError(request, response);
                    } catch (ServletException e1) {
                        LOGGER.log(Level.SEVERE, e1.getMessage(), e1);
                    } catch (IOException e1) {
                        LOGGER.log(Level.SEVERE, e1.getMessage(), e1);
                    }
                } catch (ServletException e) {
                    LOGGER.log(Level.SEVERE, e.getMessage(), e);
                    try {
                        renderGenericError(request, response);
                    } catch (ServletException e1) {
                        LOGGER.log(Level.SEVERE, e1.getMessage(), e1);
                    } catch (IOException e1) {
                        LOGGER.log(Level.SEVERE, e1.getMessage(), e1);
                    }
                } catch (NumberFormatException e) {
                    LOGGER.log(Level.SEVERE, e.getMessage(), e);
                    try {
                        renderGenericError(request, response);
                    } catch (ServletException e1) {
                        LOGGER.log(Level.SEVERE, e1.getMessage(), e1);
                    } catch (IOException e1) {
                        LOGGER.log(Level.SEVERE, e1.getMessage(), e1);
                    }
                } catch (OutOfRangeException e) {
                    LOGGER.log(Level.SEVERE, e.getMessage(), e);
                    try {
                        renderGenericError(request, response);
                    } catch (ServletException e1) {
                        LOGGER.log(Level.SEVERE, e1.getMessage(), e1);
                    } catch (IOException e1) {
                        LOGGER.log(Level.SEVERE, e1.getMessage(), e1);
                    }
                } finally {
                    for (File file : filesToDelete) {
                        file.delete();
                    }
                    if (generatedPDFFos != null)
                        IOUtils.closeQuietly(generatedPDFFos);
                }
            }
        },
        FILE {

            @Override
            public void renderPDF(HttpServletRequest request,
                    HttpServletResponse response,
                    FirstPagePDFService firstPagePDFService,
                    GeneratePDFService pdfService, SolrAccess solrAccess,
                    DocumentService documentService, String titlePage,
                    String imgServletUrl, String i18nUrl) {
                File f = null;
                FileInputStream fis = null;
                try {
                    response.setContentType("application/pdf");
                    SimpleDateFormat sdate = new SimpleDateFormat(
                            "yyyyMMdd_mmhhss");
                    response.setHeader("Content-disposition",
                            "attachment; filename=" + sdate.format(new Date())
                                    + ".pdf");
                    String handle = request.getParameter("pdfhandle");
                    f = popFile(handle);
                    fis = new FileInputStream(f);
                    cz.incad.kramerius.utils.IOUtils.copyStreams(fis,
                            response.getOutputStream());
                } catch (FileNotFoundException e) {
                    LOGGER.log(Level.SEVERE, e.getMessage(), e);
                } catch (IOException e) {
                    LOGGER.log(Level.SEVERE, e.getMessage(), e);
                } finally {
                    if (fis != null)
                        IOUtils.closeQuietly(fis);
                    f.delete();
                }
            }
        };

        public int[] srect(String srect) {
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

        public abstract void renderPDF(HttpServletRequest request,
                HttpServletResponse response,
                FirstPagePDFService firstPagePDFService,
                GeneratePDFService pdfService, SolrAccess solrAccess,
                DocumentService documentService, String titlePage,
                String imgServletUrl, String i18nUrl);

        public void mergeToOutput(OutputStream fos, File bodyFile,
                File firstPageFile) throws IOException {
            PDFMergerUtility utility = new PDFMergerUtility();
            utility.addSource(firstPageFile);
            utility.addSource(bodyFile);
            utility.setDestinationStream(fos);
            utility.mergeDocuments();
        }

        public void outputJSON(HttpServletResponse response, File generatedPDF,
                FileOutputStream generatedPDFFos, File tmpFile, File fpage)
                throws IOException {
            response.setContentType("text/plain");
            String uuid = UUID.randomUUID().toString();
            pushFile(uuid, generatedPDF);
            response.getWriter()
                    .println("{" + "pdfhandle:'" + uuid + "'" + "}");
        }

        public static ObjectPidsPath selectOnePath(String requestedPid,
                ObjectPidsPath[] paths) {
            ObjectPidsPath path;
            if (paths.length > 0) {
                path = paths[0];
            } else {
                path = new ObjectPidsPath(requestedPid);
            }
            return path;
        }

    }

    private static final String LOGO_FONT = "logo";
    private static final String INF_FONT = "info";

    private static final String PIDS = "pids";
    private static final String RECT = "rect";
}
