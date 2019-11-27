/*
 * Copyright (C) 2013 Pavel Stastny
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package cz.incad.kramerius.rest.api.k5.client.pdf;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import javax.xml.xpath.XPathExpressionException;

import org.json.JSONException;
import org.json.JSONObject;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfWriter;

import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.ProcessSubtreeException;
import cz.incad.kramerius.pdf.OutOfRangeException;
import cz.incad.kramerius.pdf.impl.ConfigurationUtils;
import cz.incad.kramerius.pdf.utils.PDFExlusiveGenerateSupport;
import cz.incad.kramerius.rest.api.exceptions.ActionNotAllowed;
import cz.incad.kramerius.rest.api.exceptions.BadRequestException;
import cz.incad.kramerius.rest.api.exceptions.GenericApplicationException;
import cz.incad.kramerius.security.SecurityException;
import cz.incad.kramerius.statistics.ReportedAction;
import cz.incad.kramerius.utils.FedoraUtils;
import cz.incad.kramerius.utils.IOUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;
import cz.incad.kramerius.utils.imgs.ImageMimeType;
import cz.incad.kramerius.utils.imgs.KrameriusImageSupport;

/**
 * Generating pdf documents
 * @author pavels
 */
@Path("/v5.0/pdf")
public class PDFResource extends AbstractPDFResource  {

    public static Logger LOGGER = Logger.getLogger(PDFResource.class.getName());
    

    
    /**
     * Paper size
     * @author pavels
     *
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
    
    
    /**
     * Returns informations about resouce (how many pages can be generated and if resource is busy)
     * @return
     */
    @GET
    @Produces({ "application/json" })
    public Response info() {
        try {
            JSONObject jsonObject = new JSONObject();
            String maxPage = KConfiguration.getInstance().getProperty(
                    "generatePdfMaxRange");
            
            boolean turnOff = KConfiguration.getInstance().getConfiguration().getBoolean("turnOffPdfCheck");
            if (turnOff) {
                jsonObject.put("pdfMaxRange", "unlimited");
            } else {
                jsonObject.put("pdfMaxRange", maxPage);
            }
            boolean acquired =  false;
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
        }
    }

    

    /**
     * Print only part of image 
     * @param pid PID
     * @param xpos Determine x-position. Value is defined by percentage of total width. 
     * @param ypos Determine y-position. Value is defined by percentage of total height.
     * @param width Deterime width. Value is defiend by percentage of total width.
     * @param height Determine height. Value is defined by percentage of total height
     * @param format Page format. Possible values : A0,...A5, B0,...B5, LETTER, POSTCARD
     * @return
     * @throws OutOfRangeException
     */
    @GET
    @Path("part")
    @Produces({ "application/pdf", "application/json" })
    public Response part(@QueryParam("pid") String pid, 
            @QueryParam("xpos") String xpos,
            @QueryParam("ypos") String ypos,
            @QueryParam("width") String width,
            @QueryParam("height") String height,
            @QueryParam("format") String format) throws OutOfRangeException {
        boolean acquired = false;
        try {
            acquired = PDFExlusiveGenerateSupport.PDF_SEMAPHORE.tryAcquire();
            if (acquired) {
                if (pid != null) {
                    File fileToDelete = null;
                    try {
                        pid = this.fedoraAccess.findFirstViewablePid(pid);
                        
                        BufferedImage bufImage = KrameriusImageSupport.readImage(pid,FedoraUtils.IMG_FULL_STREAM, this.fedoraAccess, 0);

                        double xPerctDouble = Double.parseDouble(xpos);
                        double yPerctDouble = Double.parseDouble(ypos);

                        double widthPerctDouble = Double.parseDouble(width);
                        double heightPerctDouble = Double.parseDouble(height);

                        BufferedImage subImage =  KrameriusImageSupport.partOfImage(bufImage, xPerctDouble, yPerctDouble,
                                widthPerctDouble, heightPerctDouble);
            
                        fileToDelete = File.createTempFile("subimage", ".png");
                        FileOutputStream fos = new FileOutputStream(fileToDelete);
                        KrameriusImageSupport.writeImageToStream(subImage, ImageMimeType.PNG.getDefaultFileExtension(), fos);
                        fos.close();
                        
                        try {
                            this.mostDesirable.saveAccess(pid, new Date());
                            this.statisticsAccessLog.reportAccess(pid, FedoraUtils.IMG_FULL_STREAM, ReportedAction.PDF.name());
                        } catch (Exception e) {
                            LOGGER.severe("cannot write statistic records");
                            LOGGER.log(Level.SEVERE, e.getMessage(),e);
                        }
                        
                        StreamingOutput stream = streamingOutput(fileToDelete,format);
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
     * @param pidsParam List of pids
     * @param pageType First page type. Possible values TEXT, IMAGE
     * @param format Page format. Possible values : A0,...A5, B0,...B5, LETTER, POSTCARD
     * @return
     * @throws OutOfRangeException
     */
    @GET
    @Path("selection")
    @Produces({ "application/pdf", "application/json" })
    public Response selection(@QueryParam("pids") String pidsParam,
            @QueryParam("firstPageType") @DefaultValue("TEXT") String pageType,
            @QueryParam("format") String format) throws OutOfRangeException {
        boolean acquired = false;
        try {
            acquired = PDFExlusiveGenerateSupport.PDF_SEMAPHORE.tryAcquire();
            if (acquired) {
                try {

                    AbstractPDFResource.FirstPage fp = pageType != null ? AbstractPDFResource.FirstPage
                            .valueOf(pageType) : AbstractPDFResource.FirstPage.TEXT;

                    String[] pids = pidsParam.split(",");
                    
                    // max number test
                    ConfigurationUtils.checkNumber(pids);
                    
                    Rectangle formatRect = formatRect(format);
                    final File generatedPDF = super.selection(pids, formatRect, fp);
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

                    SimpleDateFormat sdate = new SimpleDateFormat(
                            "yyyyMMdd_mmhhss");
                    return Response
                            .ok()
                            .header("Content-disposition",
                                    "attachment; filename="
                                            + sdate.format(new Date()) + ".pdf")
                            .entity(stream).type("application/pdf").build();

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
                } catch(SecurityException e) {
                    LOGGER.log(Level.INFO, e.getMessage());
                    throw new ActionNotAllowed(e.getMessage());
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
     * @param pid PID of generating document
     * @param number Number of pages (whole document or maximum number of pages)
     * @param pageType Type of firt page. Possible values: TEXT,IMAGE
     * @param format Page format. Possible values : A0,...A5, B0,...B5, LETTER, POSTCARD
     * @return
     */
    @GET
    @Path("parent")
    @Produces({ "application/pdf", "application/json" })
    public Response parent(@QueryParam("pid") String pid,
            @QueryParam("number") String number,
            @QueryParam("firstPageType") @DefaultValue("TEXT") String pageType,
            @QueryParam("format") String format) {
        boolean acquired = false;
        try {
            acquired = PDFExlusiveGenerateSupport.PDF_SEMAPHORE.tryAcquire();
            if (acquired) {
                try {


                    AbstractPDFResource.FirstPage fp = pageType != null ? AbstractPDFResource.FirstPage
                            .valueOf(pageType) : AbstractPDFResource.FirstPage.TEXT;

                    // max number test
                    int n = ConfigurationUtils.checkNumber(number);
                    Rectangle formatRect = formatRect(format);
                    
                    final File generatedPdf = super.parent(pid, n, formatRect, fp);

                    final InputStream fis = new FileInputStream(generatedPdf);
                    StreamingOutput stream = new StreamingOutput() {
                        public void write(OutputStream output)
                                throws IOException, WebApplicationException {
                            try {
                                IOUtils.copyStreams(fis, output);
                            } catch (Exception e) {
                                throw new WebApplicationException(e);
                            } finally {
                                if (generatedPdf != null)
                                    generatedPdf.delete();
                            }
                        }
                    };

                    SimpleDateFormat sdate = new SimpleDateFormat(
                            "yyyyMMdd_mmhhss");
                    return Response
                            .ok()
                            .header("Content-disposition",
                                    "attachment; filename="
                                            + sdate.format(new Date()) + ".pdf")
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
                    LOGGER.log(Level.WARNING,"too much pages for pdf generating - consider changing config attribute (generatePdfMaxRange)");
                    throw new PDFResourceBadRequestException(e1.getMessage());
                } catch (SecurityException e1) {
                    LOGGER.log(Level.INFO, e1.getMessage());
                    throw new ActionNotAllowed(e1.getMessage());
                }
            } else {
                throw new PDFResourceNotReadyException("not ready");
            }
        } finally {
            if (acquired)
                PDFExlusiveGenerateSupport.PDF_SEMAPHORE.release();
        }
    }

    
    public static BufferedImage partOfImage(BufferedImage bufferedImage, HttpServletRequest req,
            String pid) throws MalformedURLException, IOException, JSONException, XPathExpressionException {

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
            public void write(OutputStream output)
                    throws IOException, WebApplicationException {
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
        Rectangle formatRect =  Size.A4.getRectangle();
        if (format != null) {
            Size enumValueOf = Size.valueOf(format);
            try {
                formatRect = enumValueOf.getRectangle();
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, e.getMessage(), e);
                formatRect = Size.A4.getRectangle();
            }
        }
        return formatRect;
    }
    

}
