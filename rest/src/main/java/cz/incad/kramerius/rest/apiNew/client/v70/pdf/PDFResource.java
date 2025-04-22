package cz.incad.kramerius.rest.apiNew.client.v70.pdf;

import com.google.inject.Inject;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfWriter;
import cz.incad.kramerius.ProcessSubtreeException;
import cz.incad.kramerius.pdf.OutOfRangeException;
import cz.incad.kramerius.pdf.utils.PDFExlusiveGenerateSupport;
import cz.incad.kramerius.rest.api.exceptions.ActionNotAllowed;
import cz.incad.kramerius.rest.api.exceptions.BadRequestException;
import cz.incad.kramerius.rest.api.exceptions.GenericApplicationException;
//TODO: move exceptions from cz.incad.kramerius.rest.api.k5
import cz.incad.kramerius.rest.api.k5.client.pdf.PDFResourceBadRequestException;
import cz.incad.kramerius.rest.api.k5.client.pdf.PDFResourceNotReadyException;
import cz.inovatika.monitoring.APICallMonitor;
import cz.inovatika.monitoring.ApiCallEvent;
import cz.incad.kramerius.security.SecurityException;
import cz.incad.kramerius.statistics.ReportedAction;
import cz.incad.kramerius.utils.FedoraUtils;
import cz.incad.kramerius.utils.IOUtils;
import cz.incad.kramerius.utils.StringUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;
import cz.incad.kramerius.utils.imgs.ImageMimeType;
import cz.incad.kramerius.utils.imgs.KrameriusImageSupport;
import org.json.JSONException;
import org.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import javax.xml.xpath.XPathExpressionException;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.MalformedURLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * replaces cz.incad.kramerius.rest.api.k5.client.pdf.PDFResource
 */
@Path("/client/v7.0/pdf")
public class PDFResource extends AbstractPDFResource {
    public static Logger LOGGER = Logger.getLogger(PDFResource.class.getName());


    /**
     * Paper size
     *
     * @author pavels
     */
    static enum Size {

        B6(PageSize.B6),
        B5(PageSize.B5),
        B4(PageSize.B4),
        B3(PageSize.B3),
        B2(PageSize.B2),
        B1(PageSize.B1),
        B0(PageSize.B0),

        A6(PageSize.A6),
        A5(PageSize.A5),
        A4(PageSize.A4),
        A3(PageSize.A3),
        A2(PageSize.A2),
        A1(PageSize.A1),
        A0(PageSize.A0),

        LETTER(PageSize.LETTER),
        POSTCARD(PageSize.POSTCARD),
        TABLOID(PageSize.TABLOID);

        protected Rectangle rect;

        private Size(Rectangle rect) {
            this.rect = rect;
        }

        public Rectangle getRectangle() {
            return rect;
        }
    }

    @Inject
    APICallMonitor apiCallMonitor;

    /**
     * Returns information about resource (how many pages can be generated and if resource is busy)
     *
     * @return
     */
    @GET
    @Produces({"application/json"})
    public Response info() {

        ApiCallEvent event = this.apiCallMonitor.start("/client/v7.0/pdf", "/client/v7.0/pdf", "", "GET");

        try {
            if (PDF_ENDPOINTS_DISABLED) {
                return Response.status(Response.Status.SERVICE_UNAVAILABLE).build();
            }
            JSONObject jsonObject = new JSONObject();
            String maxPage = KConfiguration.getInstance().getProperty("generatePdfMaxRange");

            boolean turnOff = KConfiguration.getInstance().getConfiguration().getBoolean("turnOffPdfCheck");
            jsonObject.put("turnOffPdfCheck", turnOff);
            if (turnOff) {
                jsonObject.put("pdfMaxRange", "unlimited");
            } else {
                jsonObject.put("pdfMaxRange", maxPage);
            }
            boolean acquired = false;
            try {
                acquired = PDFExlusiveGenerateSupport.PDF_SEMAPHORE.tryAcquire();
                if (acquired) {
                    jsonObject.put("resourceBusy", false);
                } else {
                    jsonObject.put("resourceBusy", true);
                }
            } finally {
                if (acquired) {
                    PDFExlusiveGenerateSupport.PDF_SEMAPHORE.release();
                }
            }
            return Response.ok().entity(jsonObject.toString()).build();
        } catch (JSONException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new GenericApplicationException(e.getMessage());
        } finally {
            if (event != null) {
                this.apiCallMonitor.stop(event, userProvider.get().getLoginname());
            }
        }
    }

    /**
     * Print only part of image
     *
     * @param pid    PID
     * @param xpos   Determine x-position. Value is defined by percentage of total width.
     * @param ypos   Determine y-position. Value is defined by percentage of total height.
     * @param width  Deterime width. Value is defiend by percentage of total width.
     * @param height Determine height. Value is defined by percentage of total height
     * @param format Page format. Possible values : A0,...A5, B0,...B5, LETTER, POSTCARD
     * @return
     * @throws OutOfRangeException
     */
    @GET
    @Path("part")
    @Produces({"application/pdf", "application/json"})
    public Response part(@QueryParam("pid") String pid,
                         @QueryParam("xpos") String xpos,
                         @QueryParam("ypos") String ypos,
                         @QueryParam("width") String width,
                         @QueryParam("height") String height,
                         @QueryParam("format") String format) throws OutOfRangeException {
        if (PDF_ENDPOINTS_DISABLED) {
            return Response.status(Response.Status.SERVICE_UNAVAILABLE).build();
        }
        boolean acquired = false;
        try {
            acquired = PDFExlusiveGenerateSupport.PDF_SEMAPHORE.tryAcquire();
            if (acquired) {
                if (pid != null) {
                    List<String> params = new ArrayList<>();
                    if (StringUtils.isAnyString(pid)) {
                        params.add(String.format("pid=%s", pid));
                    }
                    if (StringUtils.isAnyString(xpos)) {
                        params.add(String.format("xpos=%s", xpos));
                    }
                    if (StringUtils.isAnyString(ypos)) {
                        params.add(String.format("ypos=%s", ypos));
                    }
                    if (StringUtils.isAnyString(width)) {
                        params.add(String.format("width=%s", width));
                    }
                    if (StringUtils.isAnyString(height)) {
                        params.add(String.format("height=%s", height));
                    }
                    if (StringUtils.isAnyString(format)) {
                        params.add(String.format("format=%s", format));
                    }
                    ApiCallEvent event = this.apiCallMonitor.start("/client/v7.0/pdf", "/client/v7.0/pdf/part", params.stream().collect(Collectors.joining("&")), "GET");

                    File fileToDelete = null;
                    try {
                        pid = this.fedoraAccess.findFirstViewablePid(pid);

                        BufferedImage bufImage = KrameriusImageSupport.readImage(pid, FedoraUtils.IMG_FULL_STREAM, this.fedoraAccess, 0);

                        double xPerctDouble = Double.parseDouble(xpos);
                        double yPerctDouble = Double.parseDouble(ypos);

                        double widthPerctDouble = Double.parseDouble(width);
                        double heightPerctDouble = Double.parseDouble(height);

                        BufferedImage subImage = KrameriusImageSupport.partOfImage(bufImage,
                                xPerctDouble, yPerctDouble,
                                widthPerctDouble, heightPerctDouble);

                        fileToDelete = File.createTempFile("subimage", ".png");
                        FileOutputStream fos = new FileOutputStream(fileToDelete);
                        KrameriusImageSupport.writeImageToStream(subImage, ImageMimeType.PNG.getDefaultFileExtension(), fos);
                        fos.close();

                        reportAccess(pid);

                        StreamingOutput stream = streamingOutput(fileToDelete, format);
                        return Response
                                .ok()
                                .entity(stream).type("application/pdf").build();
                    } catch (NumberFormatException e) {
                        LOGGER.log(Level.SEVERE, e.getMessage(), e);
                        throw new GenericApplicationException(e.getMessage());
                    } catch (XPathExpressionException e) {
                        LOGGER.log(Level.SEVERE, e.getMessage(), e);
                        throw new GenericApplicationException(e.getMessage());
                    } catch (FileNotFoundException e) {
                        LOGGER.log(Level.SEVERE, e.getMessage(), e);
                        throw new GenericApplicationException(e.getMessage());
                    } catch (IOException e) {
                        LOGGER.log(Level.SEVERE, e.getMessage(), e);
                        throw new GenericApplicationException(e.getMessage());
                    } finally {
                        if (event != null) {
                            this.apiCallMonitor.stop(event, userProvider.get().getLoginname());
                        }
                    }

                } else {
                    LOGGER.log(Level.SEVERE, "No pid defined");
                    throw new BadRequestException("No pid defined");
                }
            } else {
                throw new PDFResourceNotReadyException("not ready");
            }
        } finally {
            if (acquired)
                PDFExlusiveGenerateSupport.PDF_SEMAPHORE.release();
        }
    }

    
    /**
     * Generate pdf from selection
     *
     * @param pidsParam     List of pids
     * @param firstPageType First page type. Possible values TEXT, IMAGE
     * @param format        Page format. Possible values : A0,...A5, B0,...B5, LETTER, POSTCARD
     * @return
     * @throws OutOfRangeException
     */
    @GET
    @Path("selection")
    @Produces({"application/pdf", "application/json"})
    public Response selection(@QueryParam("pids") String pidsParam,
                              @QueryParam("firstPageType") @DefaultValue("TEXT") String firstPageType,
                              @QueryParam("format") String format) throws OutOfRangeException {
        if (PDF_ENDPOINTS_DISABLED) {
            return Response.status(Response.Status.SERVICE_UNAVAILABLE).build();
        }
        boolean acquired = false;
        try {
            acquired = PDFExlusiveGenerateSupport.PDF_SEMAPHORE.tryAcquire();
            if (acquired) {
                List<String> params = new ArrayList<>();
                if (StringUtils.isAnyString(pidsParam)) {
                    params.add(String.format("pids=%s", pidsParam));
                }
                if (StringUtils.isAnyString(firstPageType)) {
                    params.add(String.format("firstPageType=%s", firstPageType));
                }
                if (StringUtils.isAnyString(format)) {
                    params.add(String.format("format=%s", format));
                }
                ApiCallEvent event = this.apiCallMonitor.start("/client/v7.0/pdf", "/client/v7.0/pdf/selection", params.stream().collect(Collectors.joining("&")), "GET");
                try {

                    FirstPageType fistPageTypeEn = extractFirstPageType(firstPageType);
                    if (StringUtils.isAnyString(pidsParam)) {
                        String[] pids = pidsParam.split(",");
                        // max number test
                        int numberOfPages = extractNumberOfPages("" + pids.length);
                        //ConfigurationUtils.checkNumber(pids);
                        //LOGGER.info("number of pages: " + numberOfPages); //TODO: remove for production

                        Rectangle formatRect = formatRect(format);
                        final File generatedPDF = super.selection(pids, formatRect, fistPageTypeEn);
                        final InputStream fis = new FileInputStream(generatedPDF);
                        StreamingOutput stream = new StreamingOutput() {
                            public void write(OutputStream output)
                                    throws IOException, WebApplicationException {
                                try {
                                    IOUtils.copyStreams(fis, output);
                                } catch (Exception e) {
                                    throw new WebApplicationException(e);
                                } finally {
                                    if (generatedPDF != null)
                                        generatedPDF.delete();
                                }
                            }
                        };
                        SimpleDateFormat sdate = new SimpleDateFormat("yyyyMMdd_mmhhss");
                        return Response
                                .ok()
                                .header("Content-disposition",
                                        "attachment; filename=" + sdate.format(new Date()) + ".pdf")
                                .entity(stream).type("application/pdf").build();
                    } else {
                        return Response.status(Response.Status.BAD_REQUEST).build();
                    }
                } catch (MalformedURLException e) {
                    LOGGER.log(Level.SEVERE, e.getMessage(), e);
                    throw new GenericApplicationException(e.getMessage());
                } catch (IOException e) {
                    LOGGER.log(Level.SEVERE, e.getMessage(), e);
                    throw new GenericApplicationException(e.getMessage());
                } catch (ProcessSubtreeException e) {
                    LOGGER.log(Level.SEVERE, e.getMessage(), e);
                    throw new GenericApplicationException(e.getMessage());
                } catch (DocumentException e) {
                    LOGGER.log(Level.SEVERE, e.getMessage(), e);
                    throw new GenericApplicationException(e.getMessage());
                } catch (SecurityException e) {
                    LOGGER.log(Level.INFO, e.getMessage());
                    throw new ActionNotAllowed(e.getMessage());
                } finally {
                    if (event != null) {
                        this.apiCallMonitor.stop(event, userProvider.get().getLoginname());
                    }
                }

            } else {
                throw new PDFResourceNotReadyException("not ready");
            }
        } finally {
            if (acquired)
                PDFExlusiveGenerateSupport.PDF_SEMAPHORE.release();
        }
    }

    /**
     * Generate whole document
     *
     * @param pid           PID of generating document
     * @param numberOfPages Number of pages (whole document or maximum number of pages)
     * @param firstPageType Type of first page. Possible values: TEXT,IMAGE
     * @param format        Page format. Possible values : A0,...A5, B0,...B5, LETTER, POSTCARD
     * @return
     */
    @GET
    @Path("parent")
    @Produces({"application/pdf", "application/json"})
    public Response parent(@QueryParam("pid") String pid,
                           @QueryParam("numberOfPages") String numberOfPages,
                           @QueryParam("firstPageType") @DefaultValue("TEXT") String firstPageType,
                           @QueryParam("format") String format) {
        if (PDF_ENDPOINTS_DISABLED) {
            return Response.status(Response.Status.SERVICE_UNAVAILABLE).build();
        }
        boolean acquired = false;
        try {
            acquired = PDFExlusiveGenerateSupport.PDF_SEMAPHORE.tryAcquire();
            if (acquired) {
                List<String> params = new ArrayList<>();
                if (StringUtils.isAnyString(pid)) {
                    params.add(String.format("pid=%s", pid));
                }
                if (StringUtils.isAnyString(numberOfPages)) {
                    params.add(String.format("numberOfPages=%s", numberOfPages));
                }
                if (StringUtils.isAnyString(firstPageType)) {
                    params.add(String.format("firstPageType=%s", firstPageType));
                }
                if (StringUtils.isAnyString(format)) {
                    params.add(String.format("format=%s", format));
                }
                ApiCallEvent event = this.apiCallMonitor.start("/client/v7.0/pdf", "/client/v7.0/pdf/parent", params.stream().collect(Collectors.joining("&")), "GET");
                try {

                    FirstPageType firstPageTypeEn = extractFirstPageType(firstPageType);

                    // max number test
                    int numberOfPagesInt = extractNumberOfPages(numberOfPages);
                    //int n = ConfigurationUtils.checkNumber(number);
                    LOGGER.info("number of pages: " + numberOfPagesInt); //TODO: remove for production
                    Rectangle formatRect = formatRect(format);

                    final File generatedPdf = super.parent(pid, numberOfPagesInt, formatRect, firstPageTypeEn);

                    final InputStream fis = new FileInputStream(generatedPdf);
                    StreamingOutput stream = new StreamingOutput() {
                        public void write(OutputStream output) throws WebApplicationException {
                            try {
                                IOUtils.copyStreams(fis, output);
                            } catch (Exception e) {
                                throw new WebApplicationException(e);
                            } finally {
                                if (generatedPdf != null) {
                                    generatedPdf.delete();
                                }
                            }
                        }
                    };

                    SimpleDateFormat sdate = new SimpleDateFormat("yyyyMMdd_mmhhss");
                    return Response
                            .ok()
                            .header("Content-disposition", "attachment; filename=" + sdate.format(new Date()) + ".pdf")
                            .entity(stream).type("application/pdf").build();
                } catch (NumberFormatException e) {
                    LOGGER.log(Level.SEVERE, e.getMessage(), e);
                    throw new GenericApplicationException(e.getMessage());
                } catch (FileNotFoundException e) {
                    LOGGER.log(Level.SEVERE, e.getMessage(), e);
                    throw new GenericApplicationException(e.getMessage());
                } catch (DocumentException e) {
                    LOGGER.log(Level.SEVERE, e.getMessage(), e);
                    throw new GenericApplicationException(e.getMessage());
                } catch (IOException e) {
                    LOGGER.log(Level.SEVERE, e.getMessage(), e);
                    throw new GenericApplicationException(e.getMessage());
                } catch (ProcessSubtreeException e) {
                    LOGGER.log(Level.SEVERE, e.getMessage(), e);
                    throw new GenericApplicationException(e.getMessage());
                } catch (OutOfRangeException e1) {
                    LOGGER.log(Level.WARNING, "too much pages for pdf generating - consider changing config attribute (generatePdfMaxRange)");
                    throw new PDFResourceBadRequestException(e1.getMessage());
                } catch (SecurityException e1) {
                    LOGGER.log(Level.INFO, e1.getMessage());
                    throw new ActionNotAllowed(e1.getMessage());
                } finally {
                    if (event != null) {
                        this.apiCallMonitor.stop(event, userProvider.get().getLoginname());
                    }
                }

            } else {
                throw new PDFResourceNotReadyException("not ready");
            }
        } finally {
            if (acquired) {
                PDFExlusiveGenerateSupport.PDF_SEMAPHORE.release();
            }
        }
    }

    public static BufferedImage partOfImage(BufferedImage bufferedImage, HttpServletRequest req, String pid) throws MalformedURLException, IOException, JSONException, XPathExpressionException {

        String xperct = req.getParameter("xpos");
        String yperct = req.getParameter("ypos");
        String heightperct = req.getParameter("height");
        String widthperct = req.getParameter("width");

        double xPerctDouble = Double.parseDouble(xperct);
        double yPerctDouble = Double.parseDouble(yperct);

        double widthPerctDouble = Double.parseDouble(widthperct);
        double heightPerctDouble = Double.parseDouble(heightperct);

        return KrameriusImageSupport.partOfImage(bufferedImage, xPerctDouble, yPerctDouble,
                widthPerctDouble, heightPerctDouble);
    }

    private static StreamingOutput streamingOutput(final File file, final String format) {
        return new StreamingOutput() {
            public void write(OutputStream output) throws IOException, WebApplicationException {
                try {
                    Rectangle formatRect = formatRect(format);
                    Document document = new Document(formatRect);
                    PdfWriter.getInstance(document, output);
                    document.open();

                    Image image = Image.getInstance(file.toURI().toURL());

                    image.scaleToFit(
                            document.getPageSize().getWidth() - document.leftMargin()
                                    - document.rightMargin(),
                            document.getPageSize().getHeight() - document.topMargin()
                                    - document.bottomMargin());
                    document.add(image);

                    document.close();

                } catch (Exception e) {
                    throw new WebApplicationException(e);
                } finally {
                    if (file != null) file.delete();
                }
            }
        };
    }

    public static Rectangle formatRect(String format) {
        Rectangle formatRect = PDFResource.Size.A4.getRectangle();
        if (format != null) {
            PDFResource.Size enumValueOf = PDFResource.Size.valueOf(format);
            try {
                formatRect = enumValueOf.getRectangle();
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, e.getMessage(), e);
                formatRect = PDFResource.Size.A4.getRectangle();
            }
        }
        return formatRect;
    }

    private void reportAccess(String pid) {
        try {
            this.mostDesirable.saveAccess(pid, new Date());
            this.statisticsAccessLog.reportAccess(pid, FedoraUtils.IMG_FULL_STREAM, ReportedAction.PDF.name());
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Can't write statistic records for " + pid, e);
        }
    }
}
