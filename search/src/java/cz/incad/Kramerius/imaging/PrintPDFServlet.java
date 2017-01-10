package cz.incad.Kramerius.imaging;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageTypeSpecifier;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.xpath.XPathExpressionException;

import org.json.JSONException;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;
import com.lowagie.text.BadElementException;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfWriter;

import cz.incad.Kramerius.backend.guice.GuiceServlet;
import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.ObjectPidsPath;
import cz.incad.kramerius.SolrAccess;
import cz.incad.kramerius.imaging.ImageStreams;
import cz.incad.kramerius.security.IsActionAllowed;
import cz.incad.kramerius.security.SecuredActions;
import cz.incad.kramerius.security.User;
import cz.incad.kramerius.statistics.ReportedAction;
import cz.incad.kramerius.statistics.StatisticsAccessLog;
import cz.incad.kramerius.utils.ApplicationURL;
import cz.incad.kramerius.utils.FedoraUtils;
import cz.incad.kramerius.utils.IOUtils;
import cz.incad.kramerius.utils.StringUtils;
import cz.incad.kramerius.utils.imgs.ImageMimeType;
import cz.incad.kramerius.utils.imgs.KrameriusImageSupport;

public class PrintPDFServlet extends GuiceServlet {

    public static Logger LOGGER = Logger.getLogger(PrintPDFServlet.class.getName());

    
    
    public static enum Page {

        A4(PageSize.A4), A3(PageSize.A3);

        private Rectangle rect;

        private Page(Rectangle rect) {
            this.rect = rect;
        }

        public Rectangle getRect() {
            return rect;
        }
    }

    
    public static enum ImageOP {
        CUT {
            @Override
            protected void imageData(FedoraAccess fa,String pid, HttpServletRequest req, OutputStream os) throws IOException{
                try {
                    pid = fa.findFirstViewablePid(pid);
                    BufferedImage bufferedImage = KrameriusImageSupport.readImage(pid, ImageStreams.IMG_FULL.getStreamName(), fa, 0);
                    BufferedImage subImage = ImageCutServlet.partOfImage(bufferedImage, req,  pid);
                    KrameriusImageSupport.writeImageToStream(subImage, ImageMimeType.PNG.getDefaultFileExtension(), os);
                } catch (XPathExpressionException e) {
                    LOGGER.severe(e.getMessage());
                } catch (JSONException e) {
                    LOGGER.severe(e.getMessage());
                }
            }
        },
        
        FULL {
            @Override
            protected void imageData(FedoraAccess fa,String pid, HttpServletRequest req, OutputStream os) throws IOException {
                    try {
                        pid = fa.findFirstViewablePid(pid);
                        String mimeTypeForStream = fa.getMimeTypeForStream(pid, ImageStreams.IMG_FULL.getStreamName());
                        ImageMimeType mimeType = ImageMimeType.loadFromMimeType(mimeTypeForStream);
                        if ((!mimeType.equals(ImageMimeType.DJVU)) && (!mimeType.equals(ImageMimeType.XDJVU))&& (!mimeType.equals(ImageMimeType.VNDDJVU)) && (!mimeType.equals(ImageMimeType.PDF))) {
                            IOUtils.copyStreams(fa.getImageFULL(pid), os);
                        } else {
                            BufferedImage bufferedImage = KrameriusImageSupport.readImage(pid, ImageStreams.IMG_FULL.getStreamName(), fa, 0);
                            KrameriusImageSupport.writeImageToStream(bufferedImage, ImageMimeType.PNG.getDefaultFileExtension(), os);
                        }
                    } catch (XPathExpressionException e) {
                        LOGGER.severe(e.getMessage());
                    }
            }
        };

        protected abstract void imageData(FedoraAccess fa, String pid,HttpServletRequest req,  OutputStream os) throws IOException ;

    }

    @Inject
    @Named("securedFedoraAccess")
    FedoraAccess fedoraAccess;

    @Inject
    SolrAccess solrAccess;
    
    @Inject
    IsActionAllowed actionAllowed;

    @Inject
    Provider<User> userProvider;

    @Inject
    StatisticsAccessLog statisticsAccessLog;
    
    
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        List<File> filesToDelete = new ArrayList<File>();
        try {
            resp.setContentType(ImageMimeType.PDF.getValue());
            String pid = req.getParameter("pid");
            String pids = req.getParameter("pids");
            String pageSize = req.getParameter("pagesize");
            String imgop = req.getParameter("imgop");


            if (StringUtils.isAnyString(pid)) {
                if (canBeRead(pid) && canBeRenderedAsPDF(pid)) {

                    Document document = new Document(Page.valueOf(pageSize).getRect());
                    ServletOutputStream sos = resp.getOutputStream();
                    PdfWriter.getInstance(document, sos);
                    document.open();

                    try {
                        this.statisticsAccessLog.reportAccess(pid, FedoraUtils.IMG_FULL_STREAM, ReportedAction.PRINT.name());
                    } catch (Exception e) {
                        LOGGER.severe("cannot write statistic records");
                        LOGGER.log(Level.SEVERE, e.getMessage(),e);
                    }

                    
                    File renderedFile = File.createTempFile("local", "print");
                    filesToDelete.add(renderedFile);
                    FileOutputStream fos = new FileOutputStream(renderedFile);
                    ImageOP.valueOf(imgop).imageData(this.fedoraAccess, pid, req, fos);
                    
                    Image image = Image.getInstance(renderedFile.toURI().toURL());

                    image.scaleToFit(
                            document.getPageSize().getWidth() - document.leftMargin()
                                    - document.rightMargin(),
                            document.getPageSize().getHeight() - document.topMargin()
                                    - document.bottomMargin());
                    document.add(image);
                    document.close();
                    
                } else {
                    resp.sendError(HttpServletResponse.SC_FORBIDDEN);
                }
            } else {
                String[] pds = pids.split(",");
                boolean canBeRendered = false;
                boolean canBePDFRendered = false;
                for (int i = 0; i < pds.length; i++) {
                    if (!canBeRendered) canBeRendered = canBeRead(pds[i]);
                }
                for (int i = 0; i < pds.length; i++) {
                    if (!canBePDFRendered) canBePDFRendered = canBeRenderedAsPDF(pds[i]);
                }
                
                
                if (canBeRendered && canBePDFRendered) {

                    Document document = new Document(Page.valueOf(pageSize).getRect());
                    ServletOutputStream sos = resp.getOutputStream();
                    PdfWriter.getInstance(document, sos);
                    document.open();

                    for (int i = 0; i < pds.length; i++) {
                        File nfile = File.createTempFile("local", "print");
                        filesToDelete.add(nfile);
                
                        try {
                            this.statisticsAccessLog.reportAccess(pds[i], FedoraUtils.IMG_FULL_STREAM, ReportedAction.PRINT.name());
                        } catch (Exception e) {
                            LOGGER.severe("cannot write statistic records");
                            LOGGER.log(Level.SEVERE, e.getMessage(),e);
                        }

                        FileOutputStream fos = new FileOutputStream(nfile);
                        ImageOP.valueOf(imgop).imageData(this.fedoraAccess, pds[i], req, fos);
                        Image image = Image.getInstance(nfile.toURI().toURL());

                        image.scaleToFit(
                                document.getPageSize().getWidth() - document.leftMargin()
                                        - document.rightMargin(),
                                document.getPageSize().getHeight() - document.topMargin()
                                        - document.bottomMargin());
                        document.add(image);        
                        if (i < pds.length-1) {
                            document.newPage();
                        }
                    }
                    document.close();
                } else {
                    resp.sendError(HttpServletResponse.SC_FORBIDDEN);
                }
            }
        } catch (BadElementException e) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        } catch (DocumentException e) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        } finally {
            for (File file : filesToDelete) {
                if (file != null) {
                    file.delete();
                }
            }
        }
    }

    private boolean canBeRead(String pid) throws IOException {
        ObjectPidsPath[] paths = solrAccess.getPath(pid);
        for (ObjectPidsPath pth : paths) {
            if (this.actionAllowed.isActionAllowed(userProvider.get(), SecuredActions.READ.getFormalName(), pid, null, pth)) {
                return true;
            }
        }
        return false;
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
