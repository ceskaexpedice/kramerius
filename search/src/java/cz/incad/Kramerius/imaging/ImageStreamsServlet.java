/*
 * Copyright (C) 2010 Pavel Stastny
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
package cz.incad.Kramerius.imaging;

import static cz.incad.utils.IKeys.PID_PARAMETER;
import static cz.incad.utils.IKeys.UUID_PARAMETER;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.io.IOUtils;
import org.ceskaexpedice.akubra.AkubraRepository;
import org.ceskaexpedice.akubra.KnownDatastreams;
import org.ceskaexpedice.akubra.RepositoryException;
import org.w3c.dom.Document;

import cz.incad.Kramerius.AbstractImageServlet;
import cz.incad.Kramerius.imaging.utils.FileNameUtils;
import cz.incad.kramerius.security.SecurityException;
import cz.incad.kramerius.utils.FedoraUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;
import cz.incad.kramerius.utils.imgs.ImageMimeType;
import cz.incad.kramerius.utils.imgs.KrameriusImageSupport.ScalingMethod;


/**
 * This servlet can manipulate with images stored in fedora streams
 */
public class ImageStreamsServlet extends AbstractImageServlet {

    public static final Logger LOGGER = Logger.getLogger(ImageStreamsServlet.class.getName());
    
    /**
     * Parameter for stream
     */
    public static final String STREAM_PARAMETER = "stream";
    /**
     * Page parameter is for multipage documents (djvu, pdf, etc..)
     */
    public static final String PAGE_PARAMETER = "page";
    /**
     * What the servlet should do
     */
    public static final String ACTION_NAME = "action";

    @Override
    public ScalingMethod getScalingMethod() {
        KConfiguration config = KConfiguration.getInstance();
        ScalingMethod method = ScalingMethod.valueOf(config.getProperty("thumbImage.scalingMethod", "BICUBIC_STEPPED"));
        return method;
    }


    public ScalingMethod getScalingMethod(String stream) {
        KConfiguration config = KConfiguration.getInstance();
        ScalingMethod method = ScalingMethod.valueOf(config.getProperty(stream + ".scalingMethod", "BICUBIC_STEPPED"));
        return method;
    }


    @Override
    public boolean turnOnIterateScaling() {
        KConfiguration config = KConfiguration.getInstance();
        boolean highQuality = config.getConfiguration().getBoolean(
                "fullThumbnail.iterateScaling", true);
        return highQuality;
    }


    public boolean turnOnIterateScaling(String stream) {
        KConfiguration config = KConfiguration.getInstance();
        boolean highQuality = config.getConfiguration().getBoolean(
                stream + ".iterateScaling", true);
        return highQuality;
    }


    @Override
    protected void doHead(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        LOGGER.fine(String.format("Set access-control-header %s ", "Access-Control-Allow-Origin *"));
        resp.setHeader("Access-Control-Allow-Origin", "*");

        resp.setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS, DELETE");
        String pid = req.getParameter(UUID_PARAMETER);
        if (pid == null || pid.trim().equals("")) {
            pid = req.getParameter(PID_PARAMETER);
        }
        String stream = req.getParameter(STREAM_PARAMETER);
        int page = disectPageParam(req);
        if (pid != null && stream != null) {
            // TODO: Change it !!
            pid = cutHREF(pid);
            if (!akubraRepository.datastreamExists(pid, stream)) {
                pid = akubraRepository.re().getFirstViewablePidInTree(pid);
            }
            if (pid != null) {
                boolean accessible = akubraRepository.isContentAccessible(pid);
                String mimeType = akubraRepository.getDatastreamMetadata(pid, stream).getMimetype();
                resp.setContentType(mimeType);
                if (accessible) {
                    resp.setStatus(HttpServletResponse.SC_OK);
                } else {
                    resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                }
            } else {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            }
        } else {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
        }
    }


    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        LOGGER.fine(String.format("Set access-control-header %s ", "Access-Control-Allow-Origin *"));
        resp.setHeader("Access-Control-Allow-Origin", "*");
        resp.setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS, DELETE");

        String pid = req.getParameter(UUID_PARAMETER);
        if (pid == null || pid.trim().equals("")) {
            pid = req.getParameter(PID_PARAMETER);
        }
        String stream = req.getParameter(STREAM_PARAMETER);
        int page = disectPageParam(req);

        if (pid != null && stream != null) {
            // TODO: Change it !!
            pid = cutHREF(pid);
            if (!akubraRepository.datastreamExists(pid, stream)) {
                pid = akubraRepository.re().getFirstViewablePidInTree(pid);
            }
            if (pid != null) {
                Actions actionToDo = Actions.TRANSCODE;
                String actionNameParam = req.getParameter(ACTION_NAME);
                if (actionNameParam != null) {
                    actionToDo = Actions.valueOf(actionNameParam);
                }
                try {
                    actionToDo.doPerform(this, this.akubraRepository, pid, stream, page, req, resp);
                } catch (RepositoryException e1) {
                    // fedora exception
                    LOGGER.log(Level.WARNING, "Missing " + stream + " datastream for " + pid);
                    resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    resp.getWriter().write(e1.getMessage());
                } catch (FileNotFoundException e1) {
                    LOGGER.log(Level.WARNING, e1.getMessage());
                    resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                } catch (IOException e1) {
                    if ("ClientAbortException".equals(e1.getClass().getSimpleName())) {
                        LOGGER.info("Client closed request: " + req.getRequestURL());
                    } else {
                        LOGGER.log(Level.SEVERE, e1.getMessage(), e1);
                        resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    }
                } catch (SecurityException e1) {
                    LOGGER.log(Level.INFO, e1.getMessage());
                    resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                } catch (XPathExpressionException e1) {
                    LOGGER.log(Level.SEVERE, e1.getMessage(), e1);
                    resp.setStatus(HttpServletResponse.SC_NOT_IMPLEMENTED);
                } catch (Exception e1) {
                    LOGGER.log(Level.SEVERE, e1.getMessage(), e1);
                    resp.setStatus(HttpServletResponse.SC_NOT_IMPLEMENTED);
                }
            } else {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        } else {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    // cat href from param
    private String cutHREF(String pid) {
        int indexOf = pid.indexOf('#');
        return indexOf > 0 ? pid.substring(0, indexOf) : pid;
    }


    public int disectPageParam(HttpServletRequest req) {
        int page = 0;
        String spage = req.getParameter(PAGE_PARAMETER);
        if (spage != null) {
            page = Integer.parseInt(spage);
        }
        return page;
    }


    /**
     * Everything what this servlet can do.
     *
     * @author pavels
     */
    enum Actions {

        /**
         * Request to transcode image from one format to another
         */
        TRANSCODE {
            @Override
            void doPerform(ImageStreamsServlet imageStreamsServlet, AkubraRepository akubraRepository, String pid, String stream, int page, HttpServletRequest req, HttpServletResponse resp)
                    throws IOException, SecurityException, XPathExpressionException {
                OutputFormats outputFormat = OutputFormats.JPEG;
                String outputFormatParam = req.getParameter(OUTPUT_FORMAT_PARAMETER);
                if (outputFormatParam != null) {
                    outputFormat = OutputFormats.valueOf(outputFormatParam);
                }

                BufferedImage image = imageStreamsServlet.rawImage(pid, stream, req, page);
                imageStreamsServlet.setDateHaders(pid, stream, resp);
                imageStreamsServlet.setResponseCode(pid, stream, req, resp);
                imageStreamsServlet.writeImage(req, resp, image, outputFormat);
            }

        },

        /**
         * Request to scale original image
         */
        SCALE {
            @Override
            void doPerform(ImageStreamsServlet imageStreamsServlet, AkubraRepository akubraRepository, String pid, String stream, int page, HttpServletRequest req, HttpServletResponse resp)
                    throws IOException, SecurityException, XPathExpressionException {
                BufferedImage image = imageStreamsServlet.rawImage(pid, stream, req, page);
                if (image != null) {
                    Rectangle rectangle = new Rectangle(image.getWidth(null), image.getHeight(null));
                    BufferedImage scale = imageStreamsServlet.scale(image, rectangle, req, imageStreamsServlet.getScalingMethod(stream));
                    if (scale != null) {
                        imageStreamsServlet.setDateHaders(pid, stream, resp);
                        imageStreamsServlet.setResponseCode(pid, stream, req, resp);
                        imageStreamsServlet.writeImage(req, resp, scale, OutputFormats.JPEG);
                    } else resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                } else resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            }

        },

        /**
         * Request to get raw data from image stream
         */
        GETRAW {
            @Override
            void doPerform(ImageStreamsServlet imageStreamsServlet, AkubraRepository akubraRepository, String pid, String stream, int page, HttpServletRequest req, HttpServletResponse resp)
                    throws IOException, SecurityException, XPathExpressionException {
                InputStream is = null;
                if (stream.equals(FedoraUtils.IMG_THUMB_STREAM)) {
                    // small thumb -> no rights
                    is = akubraRepository.getDatastreamContent(pid, KnownDatastreams.IMG_THUMB).asInputStream();
                } else {
                    is = akubraRepository.getDatastreamContent(pid, stream).asInputStream();
                }

                String mimeType = akubraRepository.getDatastreamMetadata(pid, stream).getMimetype();
                ImageMimeType loadedMimeType = ImageMimeType.loadFromMimeType(mimeType);

                resp.setContentType(mimeType);
                imageStreamsServlet.setDateHaders(pid, stream, resp);
                imageStreamsServlet.setResponseCode(pid, stream, req, resp);

                String asFileParam = req.getParameter("asFile");
                if ((asFileParam != null) && (asFileParam.equals("true"))) {
                    Document relsExt = akubraRepository.re().get(pid).asDom(false);
                    String fileNameFromRelsExt = FileNameUtils.disectFileNameFromRelsExt(relsExt);
                    if (fileNameFromRelsExt == null) {
                        LOGGER.severe("no <file.. element in RELS-EXT");
                        fileNameFromRelsExt = "uknown";
                    }
                    if (fileNameFromRelsExt.indexOf('.') > 0) {
                        fileNameFromRelsExt = fileNameFromRelsExt.substring(0, fileNameFromRelsExt.lastIndexOf('.'));
                    }

                    if (loadedMimeType != null) {
                        fileNameFromRelsExt = fileNameFromRelsExt + "." + loadedMimeType.getDefaultFileExtension();
                    }

                    resp.setHeader("Content-disposition", "attachment; filename=" + fileNameFromRelsExt);
                }

                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                IOUtils.copy(is, bos);
                byte[] arr = bos.toByteArray();
                IOUtils.copy(new ByteArrayInputStream(arr), resp.getOutputStream());
            }
        };

        abstract void doPerform(ImageStreamsServlet imageStreamsServlet, AkubraRepository akubraRepository, String pid, String stream, int page, HttpServletRequest req, HttpServletResponse response)
                throws IOException, SecurityException, XPathExpressionException;
    }

}
