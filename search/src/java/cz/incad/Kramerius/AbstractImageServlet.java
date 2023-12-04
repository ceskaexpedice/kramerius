package cz.incad.Kramerius;

import cz.incad.Kramerius.backend.guice.GuiceServlet;
import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.FedoraNamespaces;
import cz.incad.kramerius.imaging.utils.ImageUtils;
import cz.incad.kramerius.security.SecurityException;
import cz.incad.kramerius.utils.FedoraUtils;
import cz.incad.kramerius.utils.XMLUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;
import cz.incad.kramerius.utils.imgs.ImageMimeType;
import cz.incad.kramerius.utils.imgs.KrameriusImageSupport;
import cz.incad.kramerius.utils.imgs.KrameriusImageSupport.ScalingMethod;
import org.antlr.stringtemplate.StringTemplate;
import org.antlr.stringtemplate.StringTemplateGroup;
import org.antlr.stringtemplate.language.DefaultTemplateLexer;
import org.apache.http.Header;
import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.nio.IOControl;
import org.apache.http.nio.client.HttpAsyncClient;
import org.apache.http.nio.client.methods.AsyncByteConsumer;
import org.apache.http.nio.client.methods.HttpAsyncMethods;
import org.apache.http.protocol.HttpContext;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.xpath.XPathExpressionException;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;

import cz.incad.kramerius.utils.*;

public abstract class AbstractImageServlet extends GuiceServlet {

    protected static final DateFormat[] XSD_DATE_FORMATS = {
            new SafeSimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'.'S'Z'"),
            new SafeSimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'"),
            new SafeSimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'.'S"),
            new SafeSimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss"),
            new SafeSimpleDateFormat("yyyy-MM-dd'Z'"),
            new SafeSimpleDateFormat("yyyy-MM-dd") };

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public static final java.util.logging.Logger LOGGER = java.util.logging.Logger
            .getLogger(AbstractImageServlet.class.getName());

    public static final String SCALE_PARAMETER = "scale";
    public static final String SCALED_HEIGHT_PARAMETER = "scaledHeight";
    public static final String SCALED_WIDTH_PARAMETER = "scaledWidth";
    public static final String OUTPUT_FORMAT_PARAMETER = "outputFormat";


    protected transient KConfiguration configuration = KConfiguration.getInstance();

    @Inject
    @Named("securedFedoraAccess")
    protected transient FedoraAccess fedoraAccess;

    @Inject
    protected transient HttpAsyncClient client;

    // @Inject
    // @Named("fedora3")
    // protected Provider<Connection> fedora3Provider;

    public static BufferedImage scale(BufferedImage img, Rectangle pageBounds,
                                      HttpServletRequest req, ScalingMethod scalingMethod) {
        String spercent = req.getParameter(SCALE_PARAMETER);
        String sheight = req.getParameter(SCALED_HEIGHT_PARAMETER);
        String swidth = req.getParameter(SCALED_WIDTH_PARAMETER);
        if (spercent != null) {
            double percent = 1.0;
            {
                try {
                    percent = Double.parseDouble(spercent);
                } catch (NumberFormatException e) {
                    LOGGER.log(Level.SEVERE, e.getMessage(), e);
                }
            }
            return ImageUtils.scaleByPercent(img, pageBounds, percent,
                    scalingMethod);
        } else if (sheight != null) {
            int height = 200;
            {
                try {
                    height = Integer.parseInt(sheight);
                } catch (NumberFormatException e) {
                    LOGGER.log(Level.SEVERE, e.getMessage(), e);
                }
            }
            return ImageUtils.scaleByHeight(img, pageBounds, height,
                    scalingMethod);
        } else if (swidth != null) {
            int width = 200;
            {
                try {
                    width = Integer.parseInt(swidth);
                } catch (NumberFormatException e) {
                    LOGGER.log(Level.SEVERE, e.getMessage(), e);
                }
            }
            return ImageUtils.scaleByWidth(img, pageBounds, width,
                    scalingMethod);
        } else
            return null;
    }

    protected BufferedImage rawThumbnailImage(String uuid, int page)
            throws XPathExpressionException, IOException, SecurityException,
            SQLException {
        return KrameriusImageSupport.readImage(uuid,
                FedoraUtils.IMG_THUMB_STREAM, this.fedoraAccess, page);
    }

    protected BufferedImage rawFullImage(String uuid,
                                         HttpServletRequest request, int page) throws IOException,
            MalformedURLException, XPathExpressionException {
        return KrameriusImageSupport.readImage(uuid,
                FedoraUtils.IMG_FULL_STREAM, this.fedoraAccess, page);
    }

    protected BufferedImage rawImage(String uuid, String stream,
                                     HttpServletRequest request, int page) throws IOException,
            MalformedURLException, XPathExpressionException {
        return KrameriusImageSupport.readImage(uuid, stream, this.fedoraAccess,
                page);
    }

    protected void writeImage(HttpServletRequest req, HttpServletResponse resp,
                              BufferedImage image, OutputFormats format) throws IOException {
        if ((format.equals(OutputFormats.JPEG))
                || (format.equals(OutputFormats.PNG))) {
            resp.setContentType(format.getMimeType());
            OutputStream os = resp.getOutputStream();
            KrameriusImageSupport.writeImageToStream(image,
                    format.getJavaFormat(), os);
        } else
            throw new IllegalArgumentException("unsupported mimetype '"
                    + format + "'");
    }

    protected void setDateHaders(String pid, String streamName,
                                 HttpServletResponse resp) throws IOException {
        Date lastModifiedDate = lastModified(pid, streamName);
        Calendar instance = Calendar.getInstance();
        instance.roll(Calendar.YEAR, 1);
        resp.setDateHeader("Last-Modified", lastModifiedDate.getTime());
        resp.setDateHeader("Last-Fetched", System.currentTimeMillis());
        resp.setDateHeader("Expires", instance.getTime().getTime());
    }

    private Date lastModified(String pid, String stream) throws IOException {
        return this.fedoraAccess.getStreamLastmodifiedFlag(pid, stream);

    }

    protected void setResponseCode(String pid, String streamName,
                                   HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        long dateHeader = request.getDateHeader("If-Modified-Since");
        if (dateHeader != -1) {
            Date reqDate = new Date(dateHeader);
            Date lastModified = lastModified(pid, streamName);
            if (lastModified.after(reqDate)) {
                response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
            }
        }
    }

    public FedoraAccess getFedoraAccess() {
        return fedoraAccess;
    }

    public void setFedoraAccess(FedoraAccess fedoraAccess) {
        this.fedoraAccess = fedoraAccess;
    }

    public abstract ScalingMethod getScalingMethod();

    public abstract boolean turnOnIterateScaling();


    public void copyFromImageServer(String urlString, final HttpServletResponse resp)
            throws IOException {
        final WritableByteChannel channel = Channels.newChannel(resp.getOutputStream());

        Future<Void> responseFuture = client.execute(HttpAsyncMethods.createGet(urlString), new AsyncByteConsumer<Void>() {
            @Override
            protected void onByteReceived(ByteBuffer byteBuffer, IOControl ioControl) throws IOException {
                try {
                    channel.write(byteBuffer);
                } catch (IOException e) {
                    if ("ClientAbortException".equals(e.getClass().getSimpleName())) {
                        // Do nothing, request was cancelled by client. This is usual image viewers behavior.
                    } else {
                        throw e;
                    }
                }
            }

            @Override
            protected void onResponseReceived(HttpResponse response) throws HttpException, IOException {
                int statusCode = response.getStatusLine().getStatusCode();
                resp.setStatus(statusCode);
                if (statusCode == 200) {
                    resp.setContentType(response.getEntity().getContentType().getValue());
                    LOGGER.fine(String.format("Set access-control-header %s ", "Access-Control-Allow-Origin *"));
                    resp.setHeader("Access-Control-Allow-Origin", "*");
                    Header cacheControl = response.getLastHeader("Cache-Control");
                    if (cacheControl != null) resp.setHeader(cacheControl.getName(), cacheControl.getValue());
                    Header lastModified = response.getLastHeader("Last-Modified");
                    if (lastModified != null) resp.setHeader(lastModified.getName(), lastModified.getValue());

                }
            }

            @Override
            protected Void buildResult(HttpContext httpContext) throws Exception {
                return null;
            }
        }, null);

        try {
            responseFuture.get(); // wait for request
        } catch (InterruptedException e) {
            throw new IOException(e.getMessage());
        } catch (ExecutionException e) {
            throw new IOException(e.getMessage());
        }
    }

    protected String getURLForStream(String uuid, String urlFromRelsExt)
            throws IOException, XPathExpressionException, SQLException {
        StringTemplate template = new StringTemplate(urlFromRelsExt);
        // template.setAttribute("internalstream",
        // getPathForInternalStream(uuid));
        return template.toString();
    }


    static StringTemplateGroup IIP_FORWARD = null;
    static {
        InputStream is = AbstractImageServlet.class
                .getResourceAsStream("imaging/iipforward.stg");
        IIP_FORWARD = new StringTemplateGroup(
                new InputStreamReader(is), DefaultTemplateLexer.class);
    }

    public static StringTemplateGroup stGroup() {
        return IIP_FORWARD;
    }

    public static void setStringTemplateModel(String uuid,
                                              String dataStreamPath, StringTemplate template,
                                              FedoraAccess fedoraAccess) throws UnsupportedEncodingException,
            IOException {

        List<String> folderList = new ArrayList<String>();
        File currentFile = new File(dataStreamPath);
        while (!currentFile.getName().equals("data")) {
            folderList
                    .add(0, URLEncoder.encode(currentFile.getName(), "UTF-8"));
            currentFile = currentFile.getParentFile();
        }

        template.setAttribute("dataPath", KConfiguration.getInstance()
                .getFedoraDataFolderInIIPServer());
        template.setAttribute("folderList", folderList);
        template.setAttribute("iipServer", KConfiguration.getInstance()
                .getUrlOfIIPServer());
        String smimeType = fedoraAccess.getMimeTypeForStream("uuid:" + uuid,
                "IMG_FULL");

        ImageMimeType mimeType = ImageMimeType.loadFromMimeType(smimeType);
        // mimetype a koncovka ! Doplnovat a nedoplnovat
        if (mimeType != null) {
            String extension = mimeType.getDefaultFileExtension();
            if (!dataStreamPath.endsWith("." + extension)) {
                template.setAttribute("extension", "." + extension);
            } else {
                template.setAttribute("extension", "");
            }
        } else {
            template.setAttribute("extension", "");
        }
    }

    public enum OutputFormats {
        JPEG("image/jpeg", "jpg"), PNG("image/png", "png"),

        VNDDJVU("image/vnd.djvu", null), XDJVU("image/x.djvu", null), DJVU(
                "image/djvu", null),

        RAW(null, null);

        String mimeType;
        String javaFormat;

        private OutputFormats(String mimeType, String javaFormat) {
            this.mimeType = mimeType;
            this.javaFormat = javaFormat;
        }

        public String getMimeType() {
            return mimeType;
        }

        public String getJavaFormat() {
            return javaFormat;
        }
    }

}
