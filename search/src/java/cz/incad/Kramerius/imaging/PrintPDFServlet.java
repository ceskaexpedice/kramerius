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

import jakarta.servlet.ServletException;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import javax.xml.xpath.XPathExpressionException;

import cz.incad.kramerius.security.SecuredAkubraRepository;
import cz.incad.kramerius.statistics.accesslogs.AggregatedAccessLogs;
import org.ceskaexpedice.akubra.AkubraRepository;
import org.ceskaexpedice.akubra.KnownDatastreams;
import org.json.JSONException;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfWriter;

import cz.incad.Kramerius.backend.guice.GuiceServlet;
import cz.incad.kramerius.ObjectPidsPath;
import cz.incad.kramerius.SolrAccess;
import cz.incad.kramerius.imaging.ImageStreams;
import cz.incad.kramerius.security.RightsResolver;
import cz.incad.kramerius.security.SecuredActions;
import cz.incad.kramerius.security.User;
import cz.incad.kramerius.statistics.ReportedAction;
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
            protected void imageData(AkubraRepository akubraRepository,String pid, HttpServletRequest req, OutputStream os) throws IOException{
                try {
                    pid = akubraRepository.re().getFirstViewablePidInTree(pid);
                    BufferedImage bufferedImage = KrameriusImageSupport.readImage(pid, ImageStreams.IMG_FULL.getStreamName(), akubraRepository, 0);
                    BufferedImage subImage = ImageCutServlet.partOfImage(bufferedImage, req,  pid);
                    KrameriusImageSupport.writeImageToStream(subImage, ImageMimeType.PNG.getDefaultFileExtension(), os);
                } catch (XPathExpressionException | JSONException e) {
                    LOGGER.severe(e.getMessage());
                }
            }
        },
        
        FULL {
            @Override
            protected void imageData(AkubraRepository akubraRepository,String pid, HttpServletRequest req, OutputStream os) throws IOException {
                    try {
                        pid = akubraRepository.re().getFirstViewablePidInTree(pid);
                        String mimeTypeForStream = akubraRepository.getDatastreamMetadata(pid, KnownDatastreams.IMG_FULL).getMimetype();
                        ImageMimeType mimeType = ImageMimeType.loadFromMimeType(mimeTypeForStream);
                        if ((!mimeType.equals(ImageMimeType.DJVU)) && (!mimeType.equals(ImageMimeType.XDJVU))&& (!mimeType.equals(ImageMimeType.VNDDJVU)) && (!mimeType.equals(ImageMimeType.PDF))) {
                            IOUtils.copyStreams(akubraRepository.getDatastreamContent(pid, KnownDatastreams.IMG_FULL).asInputStream(), os);
                        } else {
                            BufferedImage bufferedImage = KrameriusImageSupport.readImage(pid, ImageStreams.IMG_FULL.getStreamName(), akubraRepository, 0);
                            KrameriusImageSupport.writeImageToStream(bufferedImage, ImageMimeType.PNG.getDefaultFileExtension(), os);
                        }
                    } catch (XPathExpressionException e) {
                        LOGGER.severe(e.getMessage());
                    }
            }
        };

        protected abstract void imageData(AkubraRepository akubraRepository, String pid, HttpServletRequest req, OutputStream os) throws IOException ;

    }

    @Inject
    SecuredAkubraRepository akubraRepository;

    @Inject
    @Named("new-index")
    SolrAccess solrAccess;
    
    @Inject
    RightsResolver rightsResolver;

    @Inject
    Provider<User> userProvider;

    @Inject
    AggregatedAccessLogs statisticsAccessLog;
    
    
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        List<File> filesToDelete = new ArrayList<>();
        try {
            resp.setContentType(ImageMimeType.PDF.getValue());
            String pid = req.getParameter("pid");
            String pids = req.getParameter("pids");
            String pageSize = req.getParameter("pagesize");
            String imgop = req.getParameter("imgop");
            boolean centerImage = true;//Can be set here manually. Probably should be propagated upwards.

            if (StringUtils.isAnyString(pid)) {
                if (canBeRead(pid) && canBeRenderedAsPDF(pid)) {
                    Document document = new Document(Page.valueOf(pageSize).getRect());
                    ServletOutputStream sos = resp.getOutputStream();
                    PdfWriter.getInstance(document, sos);
                    document.open();
                    reportAccess(pid);
                    File renderedFile = File.createTempFile("local", "print");
                    filesToDelete.add(renderedFile);
                    FileOutputStream fos = new FileOutputStream(renderedFile);
                    ImageOP.valueOf(imgop).imageData(akubraRepository, pid, req, fos);
                    
                    Image image = Image.getInstance(renderedFile.toURI().toURL());

                    image.scaleToFit(
                            document.getPageSize().getWidth()// - document.leftMargin() - document.rightMargin()
                            ,
                            document.getPageSize().getHeight()// - document.topMargin() - document.bottomMargin()
                            );
                    document.add(image);
                    document.close();
                } else {
                    resp.sendError(HttpServletResponse.SC_FORBIDDEN);
                }
            } else {
                String[] pds = pids.split(",");
                boolean canBeRendered = false;
                boolean canBePDFRendered = false;
                for (String pd : pds) {
                    if (canBeRendered) break;
                    canBeRendered = canBeRead(pd);
                }
                for (String pd : pds) {
                    if (canBePDFRendered) break;
                    canBePDFRendered = canBeRenderedAsPDF(pd);
                }
                if (canBeRendered && canBePDFRendered) {
                    Rectangle rect = Page.valueOf(pageSize).getRect();
                    Document document = new Document(rect);
                    document.setMargins(0, 0, 0, 0);
                    ServletOutputStream sos = resp.getOutputStream();
                    PdfWriter.getInstance(document, sos);
                    document.open();

                    for (int i = 0; i < pds.length; i++) {
                        File nfile = File.createTempFile("local", "print");
                        filesToDelete.add(nfile);
                        reportAccess(pds[i]);
                        FileOutputStream fos = new FileOutputStream(nfile);
                        ImageOP.valueOf(imgop).imageData(akubraRepository, pds[i], req, fos);
                        Image image = Image.getInstance(nfile.toURI().toURL());
                        image.scaleToFit(
                                rect.getWidth()//document.getPageSize().getWidth() - document.leftMargin()    - document.rightMargin()
                                ,
                                rect.getHeight()//document.getPageSize().getHeight() - document.bottomMargin() - document.topMargin()//
                            );
                        if(centerImage){
                            image.setAlignment(Image.ALIGN_CENTER);
                        }
                            
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
        } catch (DocumentException e) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }  finally {
            for (File file : filesToDelete) {
                if (file != null) {
                    file.delete();
                }
            }
        }
    }

    private void reportAccess(String pid) {
        try {
            this.statisticsAccessLog.reportAccess(pid, FedoraUtils.IMG_FULL_STREAM, ReportedAction.PRINT.name());
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Can't write statistic records for " + pid, e);
        }
    }

    private boolean canBeRead(String pid) throws IOException {
        ObjectPidsPath[] paths = solrAccess.getPidPaths(pid);
        for (ObjectPidsPath pth : paths) {
            if (this.rightsResolver.isActionAllowed(userProvider.get(), SecuredActions.A_READ.getFormalName(), pid, null, pth.injectRepository()).flag()) {
                return true;
            }
        }
        return false;
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
}
