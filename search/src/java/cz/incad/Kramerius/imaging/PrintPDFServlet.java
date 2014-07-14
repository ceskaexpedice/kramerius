package cz.incad.Kramerius.imaging;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.xpath.XPathExpressionException;

import org.json.JSONException;

import com.google.inject.Inject;
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
import cz.incad.kramerius.imaging.ImageStreams;
import cz.incad.kramerius.utils.ApplicationURL;
import cz.incad.kramerius.utils.StringUtils;
import cz.incad.kramerius.utils.imgs.ImageMimeType;

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
            protected String imageURL(FedoraAccess fa,String pid, HttpServletRequest req) throws IOException{
                return cutIMGFULL(pid,req);
            }
        },
        

        FULL {
            @Override
            protected String imageURL(FedoraAccess fa,String pid, HttpServletRequest req) throws IOException {
                    String mimeTypeForStream = fa.getMimeTypeForStream(pid, ImageStreams.IMG_FULL.getStreamName());
                    ImageMimeType mimeType = ImageMimeType.loadFromMimeType(mimeTypeForStream);
                    boolean translate = false;
                    switch(mimeType) {
                        case JPEG: 
                        case PNG: 
                            translate = false;
                            break;
                        default:
                            translate = true;
                            break;
                    }
                    return translate ? translateIMGFULL(pid, req) : rawIMGFULL(pid, req);
            }
        };
        protected abstract String imageURL(FedoraAccess fa, String pid, HttpServletRequest req) throws IOException ;

        private static String imgServlet(HttpServletRequest req) {
            String imgServletUrl = ApplicationURL.applicationURL(req)+"/img";
            return imgServletUrl;
        }
        private static String imgCutServlet(HttpServletRequest req) {
            String imgServletUrl = ApplicationURL.applicationURL(req)+"/imgcut";
            return imgServletUrl;
        }
        
        private static String rawIMGFULL(String objectId, HttpServletRequest req) {
            String imgUrl = imgServlet(req) + "?uuid=" + objectId
                    + "&action=GETRAW&stream="
                    + ImageStreams.IMG_FULL.getStreamName();
            return imgUrl;
        }

        private static String translateIMGFULL(String objectId, HttpServletRequest req) {
            String imgUrl = imgServlet(req) + "?uuid=" + objectId
                    + "&action=TRANSCODE&stream="
                    + ImageStreams.IMG_FULL.getStreamName();
            return imgUrl;
        }

        
        
        private static String cutIMGFULL(String objectId, HttpServletRequest req) {
            String xperct = req.getParameter("xpos");
            String yperct = req.getParameter("ypos");
            String heightperct = req.getParameter("height");
            String widthperct = req.getParameter("width");        
            String url = imgCutServlet(req)+"?pid="+objectId+"&xpos="+xperct+"&ypos="+yperct+"&height="+heightperct+"&width="+widthperct;
            return url;
        }

    }
    
    @Inject
    @Named("securedFedoraAccess")
    FedoraAccess fedoraAccess;
    
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        try {

            resp.setContentType(ImageMimeType.PDF.getValue());
            
            String pid = req.getParameter("pid");
            String pids = req.getParameter("pids");
            String pageSize = req.getParameter("pagesize");
            String imgop = req.getParameter("imgop");
            
            Document document = new Document(Page.valueOf(pageSize).getRect());
            ServletOutputStream sos = resp.getOutputStream();
            PdfWriter.getInstance(document, sos);
            document.open();

            if (StringUtils.isAnyString(pid)) {
                Image image = Image.getInstance(ImageOP.valueOf(imgop).imageURL(this.fedoraAccess, pid, req));

                image.scaleToFit(
                        document.getPageSize().getWidth() - document.leftMargin()
                                - document.rightMargin(),
                        document.getPageSize().getHeight() - document.topMargin()
                                - document.bottomMargin());
                document.add(image);
            } else {
                String[] pds = pids.split(",");
                for (int i = 0; i < pds.length; i++) {
                    Image image = Image.getInstance(ImageOP.valueOf(imgop).imageURL(this.fedoraAccess, pds[i], req));
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
            }
            document.close();
        } catch (BadElementException e) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        } catch (DocumentException e) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

}
