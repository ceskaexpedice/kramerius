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
import java.util.concurrent.Semaphore;
import java.util.logging.Level;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.pdfbox.exceptions.COSVisitorException;
import org.apache.pdfbox.util.PDFMergerUtility;

import antlr.RecognitionException;
import antlr.TokenStreamException;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.lowagie.text.DocumentException;

import cz.incad.Kramerius.backend.guice.GuiceServlet;
import cz.incad.Kramerius.processes.ParamsLexer;
import cz.incad.Kramerius.processes.ParamsParser;
import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.ObjectPidsPath;
import cz.incad.kramerius.ProcessSubtreeException;
import cz.incad.kramerius.SolrAccess;
import cz.incad.kramerius.document.DocumentService;
import cz.incad.kramerius.document.model.AbstractRenderedDocument;
import cz.incad.kramerius.pdf.FirstPagePDFService;
import cz.incad.kramerius.pdf.GeneratePDFService;
import cz.incad.kramerius.pdf.impl.ImageFetcher;
import cz.incad.kramerius.pdf.utils.pdf.FontMap;
import cz.incad.kramerius.utils.ApplicationURL;
import cz.incad.kramerius.utils.conf.KConfiguration;

public class GeneratePDFServlet extends GuiceServlet {

    
    // controls genrating PDF     
    private static final Semaphore PDF_SEMAPHORE = new Semaphore(KConfiguration.getInstance().getConfiguration().getInt("pdfQueue.activeProcess",5));

    // stores handle for pdf 
    private static HashMap<String, File> PREPARED_FILES = new HashMap<String,File>();
    
	private static final long serialVersionUID = 1L;
	
	public static final java.util.logging.Logger LOGGER = java.util.logging.Logger
			.getLogger(GeneratePDFServlet.class.getName());
	
	public static final String UUID_FROM="uuidFrom";
    public static final String PID_FROM="pidFrom";
	public static final String UUID_TO="uuidTo";
    public static final String HOW_MANY="howMany";
	public static final String PATH="path";
	
	@Inject
	GeneratePDFService service;
	
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
	

	
	public static synchronized void pullFile(String uuid, File renderedPDF) {
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
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException {
        boolean acquired = false; 
	    try {
	        acquired =  PDF_SEMAPHORE.tryAcquire();
	        if (acquired) {
	            try {
	                renderPDF(req, resp);
	            } catch (MalformedURLException e) {
	                LOGGER.log(Level.SEVERE, e.getMessage(),e);
	            } catch (IOException e) {
	                LOGGER.log(Level.SEVERE, e.getMessage(),e);
	            } catch (ProcessSubtreeException e) {
	                LOGGER.log(Level.SEVERE, e.getMessage(),e);
	            }
	        } else {
	            try {
	                LOGGER.fine("sending error to client");
                    renderErrorPagePDF(req, resp);
                } catch (IOException e) {
                    LOGGER.log(Level.SEVERE, e.getMessage(),e);
                }
	        }

	    } finally {
		    if (acquired) PDF_SEMAPHORE.release();
		}
	}

    private void renderErrorPagePDF(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
        LOGGER.info("server busy forward");
        RequestDispatcher dispatcher = req.getRequestDispatcher("serverbusy.jsp");
        dispatcher.forward(req, resp);
    }

    public void renderPDF(HttpServletRequest req, HttpServletResponse resp) throws MalformedURLException, IOException, ProcessSubtreeException {
        String imgServletUrl = ApplicationURL.applicationURL(req)+"/img";
        if ((configuration.getApplicationURL() != null) && (!configuration.getApplicationURL().equals(""))){
        	imgServletUrl = configuration.getApplicationURL()+"img";
        }
        String i18nUrl = ApplicationURL.applicationURL(req)+"/i18n";
        if ((configuration.getApplicationURL() != null) && (!configuration.getApplicationURL().equals(""))){
        	i18nUrl = configuration.getApplicationURL()+"i18n";
        }

        
        String action = req.getParameter("action");
        String imagesOnly = req.getParameter("firstpageType");
        
        FirstPage fp = (imagesOnly != null && (!imagesOnly.trim().equals(""))) ? FirstPage.valueOf(imagesOnly) : FirstPage.TEXT;
        if (fp == FirstPage.IMAGES) {
            Action.valueOf(action).renderPDF(req, resp, this.imageFirstPage, this.service,this.solrAccess, this.documentService, "", imgServletUrl, i18nUrl);
        } else {
            Action.valueOf(action).renderPDF(req, resp, this.textFirstPage ,this.service,this.solrAccess, this.documentService, "", imgServletUrl, i18nUrl);
        }
    }

    
    public enum FirstPage {
        IMAGES, TEXT;
    }
    
    public enum Action {
        SELECTION {
            @SuppressWarnings("unchecked")
            @Override
            public void renderPDF(HttpServletRequest request, HttpServletResponse response, FirstPagePDFService firstPagePDFService, GeneratePDFService pdfService, SolrAccess solrAccess, DocumentService documentService, String titlePage, String imgServletUrl, String i18nUrl) {
                List<File> filesToDelete = new ArrayList<File>();
                FileOutputStream generatedPDFFos = null;
                try {
                    
                    long start = System.currentTimeMillis();
                    String par = request.getParameter(PIDS);
                    String srect = request.getParameter(RECT);
                    ParamsParser parser = new ParamsParser(new ParamsLexer(new StringReader(par)));
                    List params = parser.params();
                    
                    //PDFFontConfigBean configBean = fontConfigParams(fontConfigParams(null, request.getParameter(LOGO_FONT), FontMap.BIG_FONT), request.getParameter(INF_FONT), FontMap.NORMAL_FONT);

                    File tmpFile = File.createTempFile("body", "pdf");
                    filesToDelete.add(tmpFile);
                    FileOutputStream bodyTmpFos = new FileOutputStream(tmpFile);
                    File fpage = File.createTempFile("head", "pdf");
                    filesToDelete.add(fpage);
                    FileOutputStream fpageFos = new FileOutputStream(fpage);

                    int[] irects = srect(srect);
                    AbstractRenderedDocument rdoc = documentService.buildDocumentFromSelection((String[])params.toArray(new String[params.size()]), irects);
                    LOGGER.fine("creating documents takes "+(System.currentTimeMillis() - start)+" ms ");
                    
                    start = System.currentTimeMillis();
                    firstPagePDFService.generateFirstPageForSelection(rdoc, fpageFos, (String[])params.toArray(new String[params.size()]) , imgServletUrl, i18nUrl, FontMap.createFontMap());
                    LOGGER.fine("generating first page takes "+(System.currentTimeMillis() - start)+" ms ");
                    
                    start = System.currentTimeMillis();
                    pdfService.generateCustomPDF(rdoc, bodyTmpFos, imgServletUrl, i18nUrl, ImageFetcher.WEB);
                    LOGGER.fine("generating custom pdf takes "+(System.currentTimeMillis() - start)+" ms ");
                    
                    bodyTmpFos.close();fpageFos.close();

                    File generatedPDF = File.createTempFile("rendered","pdf");
                    generatedPDFFos = new FileOutputStream(generatedPDF);

                    start = System.currentTimeMillis();
                    mergeToOutput(generatedPDFFos, tmpFile, fpage);
                    LOGGER.fine("merging document pdf takes "+(System.currentTimeMillis() - start)+" ms ");

                    outputJSON(response, generatedPDF, generatedPDFFos, tmpFile, fpage);
                    
                    
                } catch (IOException e) {
                    LOGGER.log(Level.SEVERE,e.getMessage(),e);
                } catch (ProcessSubtreeException e) {
                    LOGGER.log(Level.SEVERE,e.getMessage(),e);
                } catch (RecognitionException e) {
                    LOGGER.log(Level.SEVERE,e.getMessage(),e);
                } catch (TokenStreamException e) {
                    LOGGER.log(Level.SEVERE,e.getMessage(),e);
                } catch (COSVisitorException e) {
                    LOGGER.log(Level.SEVERE,e.getMessage(),e);
                } catch (DocumentException e) {
                    LOGGER.log(Level.SEVERE,e.getMessage(),e);
                } finally {
                    for (File file : filesToDelete) {
                        file.delete();
                    }
                    if (generatedPDFFos != null) IOUtils.closeQuietly(generatedPDFFos);
                }
            }

        }, 
        PARENT {
            @Override
            public void renderPDF(HttpServletRequest request, HttpServletResponse response, FirstPagePDFService firstPagePDFService, GeneratePDFService pdfService,SolrAccess solrAccess , DocumentService documentService, String titlePage, String imgServletUrl, String i18nUrl) {
                List<File> filesToDelete = new ArrayList<File>();
                FileOutputStream generatedPDFFos = null;
                try {
                    String howMany = request.getParameter(HOW_MANY);
                    String pid = request.getParameter(PID_FROM);
                    String srect = request.getParameter(RECT);
         
                    File tmpFile = File.createTempFile("body", "pdf");
                    filesToDelete.add(tmpFile);

                    FileOutputStream bodyTmpFos = new FileOutputStream(tmpFile);
                    File fpage = File.createTempFile("head", "pdf");
                    filesToDelete.add(fpage);

                    FileOutputStream fpageFos = new FileOutputStream(fpage);

                    ObjectPidsPath[] paths = solrAccess.getPath(pid);
                    final ObjectPidsPath path = selectOnePath(pid, paths);

                    int[] irects = srect(srect);

                    //PDFFontConfigBean configBean = fontConfigParams(fontConfigParams(null, request.getParameter(LOGO_FONT), FontMap.BIG_FONT), request.getParameter(INF_FONT), FontMap.NORMAL_FONT);


                    AbstractRenderedDocument rdoc = documentService.buildDocumentAsFlat(path, pid, Integer.parseInt(howMany), irects);
                    if (rdoc.getPages().isEmpty()) {
                        rdoc = documentService.buildDocumentAsFlat(path, path.getLeaf(), Integer.parseInt(howMany), irects);
                    }
                    
                    firstPagePDFService.generateFirstPageForParent(rdoc, fpageFos, path, imgServletUrl, i18nUrl, FontMap.createFontMap());
                    
                    pdfService.generateCustomPDF(rdoc, bodyTmpFos, imgServletUrl, i18nUrl, ImageFetcher.WEB);

                    bodyTmpFos.close();
                    fpageFos.close();

                    File generatedPDF = File.createTempFile("rendered","pdf");
                    generatedPDFFos = new FileOutputStream(generatedPDF);

                    mergeToOutput(generatedPDFFos, tmpFile, fpage);

                    outputJSON(response, generatedPDF, generatedPDFFos, tmpFile, fpage);

                } catch (IOException e) {
                    LOGGER.log(Level.SEVERE,e.getMessage(),e);
                } catch (ProcessSubtreeException e) {
                    LOGGER.log(Level.SEVERE,e.getMessage(),e);
                } catch (COSVisitorException e) {
                    LOGGER.log(Level.SEVERE,e.getMessage(),e);
                } catch (DocumentException e) {
                    LOGGER.log(Level.SEVERE,e.getMessage(),e);
                } finally {
                    for (File file : filesToDelete) {
                        file.delete();
                    }
                    if (generatedPDFFos != null) IOUtils.closeQuietly(generatedPDFFos);
                }
            }
        }, 
        FILE {

            @Override
            public void renderPDF(HttpServletRequest request, HttpServletResponse response, FirstPagePDFService firstPagePDFService, GeneratePDFService pdfService, SolrAccess solrAccess, DocumentService documentService, String titlePage, String imgServletUrl, String i18nUrl) {
                File f = null;
                FileInputStream fis = null;
                try {
                    response.setContentType("application/pdf");
                    SimpleDateFormat sdate = new SimpleDateFormat("yyyyMMdd_mmhhss");
                    response.setHeader("Content-disposition","attachment; filename="+sdate.format(new Date())+".pdf");
                    String handle = request.getParameter("pdfhandle");
                    f = popFile(handle);
                    fis = new FileInputStream(f);
                    cz.incad.kramerius.utils.IOUtils.copyStreams(fis, response.getOutputStream());
                } catch (FileNotFoundException e) {
                    LOGGER.log(Level.SEVERE,e.getMessage(),e);
                } catch (IOException e) {
                    LOGGER.log(Level.SEVERE,e.getMessage(),e);
                } finally {
                    if (fis != null) IOUtils.closeQuietly(fis);
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

        public abstract void renderPDF(HttpServletRequest request, HttpServletResponse response, FirstPagePDFService firstPagePDFService, GeneratePDFService pdfService, SolrAccess solrAccess, DocumentService documentService, String titlePage, String imgServletUrl, String i18nUrl);


        public void mergeToOutput(OutputStream fos, File bodyFile, File firstPageFile) throws IOException, COSVisitorException {
            PDFMergerUtility utility = new PDFMergerUtility();
            utility.addSource(firstPageFile);
            utility.addSource(bodyFile);
            utility.setDestinationStream(fos);
            utility.mergeDocuments();
        }

//        public PDFFontConfigBean fontConfigParams(PDFFontConfigBean config, String parameter, String fontMapName) throws RecognitionException, TokenStreamException {
//            if (parameter == null) return config;
//            PDFFontConfigBean pdfFontConfig = config != null ? config : new PDFFontConfigBean();
//            ParamsParser bfparser = new ParamsParser(new ParamsLexer(new StringReader(parameter)));
//            List bfs = bfparser.params();
//            if (!bfs.isEmpty()) {
//                String style = (String) bfs.remove(0);
//                pdfFontConfig.setFontStyle(fontMapName, Integer.parseInt(style));
//                
//                if (!bfs.isEmpty()) {
//                    String size = (String) bfs.remove(0);
//                    pdfFontConfig.setFontSize(fontMapName, Integer.parseInt(size));
//                }
//            }
//            return pdfFontConfig;
//        }

        public void outputJSON(HttpServletResponse response, File generatedPDF, FileOutputStream generatedPDFFos, File tmpFile, File fpage) throws IOException, COSVisitorException {
            response.setContentType("text/plain");
            String uuid = UUID.randomUUID().toString();
            pullFile(uuid, generatedPDF);
            response.getWriter().println("{" +
            	"pdfhandle:'" +uuid+"'"+
            "}");
        }

        public static ObjectPidsPath selectOnePath(String requestedPid, ObjectPidsPath[] paths) {
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
