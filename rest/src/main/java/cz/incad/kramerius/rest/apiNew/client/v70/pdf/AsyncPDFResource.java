package cz.incad.kramerius.rest.apiNew.client.v70.pdf;


import cz.incad.kramerius.pdf.utils.PDFExlusiveGenerateSupport;
import cz.incad.kramerius.rest.api.k5.client.pdf.PDFResourceNotFound;
import cz.incad.kramerius.utils.IOUtils;
import org.json.JSONException;
import org.json.JSONObject;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;
import java.util.logging.Logger;

/**
 * replaces cz.incad.kramerius.rest.api.k5.client.pdf.AsyncPDFResource
 */
@Path("/client/v7.0/asyncpdf")
public class AsyncPDFResource extends AbstractPDFResource {
    public static Logger LOGGER = Logger.getLogger(AsyncPDFResource.class.getName());

    //private boolean acquired;

    public JSONObject outputJSON(File generatedPDF) throws IOException, JSONException {
        String uuid = UUID.randomUUID().toString();
        PDFExlusiveGenerateSupport.pushFile(uuid, generatedPDF);
        JSONObject obj = new JSONObject();
        obj.put("handle", uuid);
        return obj;
    }

    @GET
    @Path("handle")
    @Produces({"application/pdf"})
    public Response handle(@QueryParam("handle") String handle) {
        if (PDF_ENDPOINTS_DISABLED) {
            return Response.status(Response.Status.SERVICE_UNAVAILABLE).build();
        }
        final File pFile = PDFExlusiveGenerateSupport.popFile(handle);
        if (pFile != null) {
            try {
                final InputStream fis = new FileInputStream(pFile);
                StreamingOutput stream = new StreamingOutput() {
                    public void write(OutputStream output) throws IOException, WebApplicationException {
                        try {
                            IOUtils.copyStreams(fis, output);
                        } catch (Exception e) {
                            throw new WebApplicationException(e);
                        } finally {
                            if (pFile != null) pFile.delete();
                        }
                    }
                };
                SimpleDateFormat sdate = new SimpleDateFormat("yyyyMMdd_mmhhss");
                return Response.ok().header("Content-disposition", "attachment; filename=" + sdate.format(new Date()) + ".pdf").entity(stream).build();
            } catch (FileNotFoundException e) {
                throw new PDFResourceNotFound("uuid not found");
            }
        } else {
            throw new PDFResourceNotFound("uuid not found");
        }
    }


//    @GET
//    @Path("parent")
//    @Produces({ "application/json"})
//    public Response parent(@QueryParam("pid") String pid,
//            @QueryParam("number") String number,
//            @QueryParam("firstPageType") @DefaultValue("TEXT") String pageType,
//            @QueryParam("format") String format) {
//        boolean acquired = false;
//        try {
//            acquired = PDFExlusiveGenerateSupport.PDF_SEMAPHORE.tryAcquire();
//            if (acquired) {
//                try {
//                    String imgServletUrl = ApplicationURL
//                            .applicationURL(this.requestProvider.get())
//                            + "/img";
//                    if ((configuration.getApplicationURL() != null)
//                            && (!configuration.getApplicationURL().equals(""))) {
//                        imgServletUrl = configuration.getApplicationURL()
//                                + "img";
//                    }
//                    String i18nUrl = ApplicationURL
//                            .applicationURL(this.requestProvider.get())
//                            + "/i18n";
//                    if ((configuration.getApplicationURL() != null)
//                            && (!configuration.getApplicationURL().equals(""))) {
//                        i18nUrl = configuration.getApplicationURL() + "i18n";
//                    }
//                    AbstractPDFResource.FirstPage fp = pageType != null ? AbstractPDFResource.FirstPage
//                            .valueOf(pageType) : AbstractPDFResource.FirstPage.TEXT;
//
//                    int pages = Integer.MAX_VALUE;
//                    if ((number != null) && (!number.trim().equals(""))) {
//                        pages = ConfigurationUtils.checkNumber(number);
//                    }
//
//
//                    File f = null;
//                    if (fp == AbstractPDFResource.FirstPage.IMAGES) {
//                        f = parent(pid, pages, this.imageFirstPage,
//                                this.service, solrAccess, documentService,
//                                imgServletUrl, i18nUrl, format);
//                    } else {
//                        f = parent(pid, pages, this.textFirstPage,
//                                this.service, solrAccess, documentService,
//                                imgServletUrl, i18nUrl, format);
//                    }
//
//                    //final File fileToDelete = f;
//                    JSONObject outputJSON = outputJSON(f);
//
//                    return Response.ok().entity(outputJSON.toString()).build();
//                } catch (NumberFormatException e) {
//                    LOGGER.log(Level.SEVERE, e.getMessage(), e);
//                    throw new GenericApplicationException(e.getMessage());
//                } catch (COSVisitorException e) {
//                    LOGGER.log(Level.SEVERE, e.getMessage(), e);
//                    throw new GenericApplicationException(e.getMessage());
//                } catch (FileNotFoundException e) {
//                    LOGGER.log(Level.SEVERE, e.getMessage(), e);
//                    throw new GenericApplicationException(e.getMessage());
//                } catch (DocumentException e) {
//                    LOGGER.log(Level.SEVERE, e.getMessage(), e);
//                    throw new GenericApplicationException(e.getMessage());
//                } catch (IOException e) {
//                    LOGGER.log(Level.SEVERE, e.getMessage(), e);
//                    throw new GenericApplicationException(e.getMessage());
//                } catch (ProcessSubtreeException e) {
//                    LOGGER.log(Level.SEVERE, e.getMessage(), e);
//                    throw new GenericApplicationException(e.getMessage());
//                } catch (JSONException e) {
//                    LOGGER.log(Level.SEVERE, e.getMessage(), e);
//                    throw new GenericApplicationException(e.getMessage());
//                }
//            } else {
//                throw new PDFResourceNotReadyException("not ready");
//
//            }
//
//        } finally {
//            if (acquired)
//                PDFExlusiveGenerateSupport.PDF_SEMAPHORE.release();
//        }
//    }
}
