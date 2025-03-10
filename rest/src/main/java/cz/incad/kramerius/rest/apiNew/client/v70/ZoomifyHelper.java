package cz.incad.kramerius.rest.apiNew.client.v70;

import com.google.inject.Inject;
import cz.incad.kramerius.MostDesirable;
import cz.incad.kramerius.imaging.DeepZoomCacheService;
import cz.incad.kramerius.imaging.DeepZoomTileSupport;
import cz.incad.kramerius.rest.apiNew.exceptions.NotFoundException;
import cz.incad.kramerius.statistics.accesslogs.AggregatedAccessLogs;
import cz.incad.kramerius.utils.FedoraUtils;
import cz.incad.kramerius.utils.XMLUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;
import cz.incad.kramerius.utils.imgs.KrameriusImageSupport;
import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.ceskaexpedice.akubra.AkubraRepository;
import org.ceskaexpedice.akubra.RepositoryNamespaces;
import org.ceskaexpedice.akubra.relsext.RelsExtHandler;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;

import static org.apache.http.HttpStatus.SC_OK;

/**
 * @see cz.incad.Kramerius.imaging.ZoomifyServlet
 */
@SuppressWarnings("JavadocReference")
public class ZoomifyHelper {

    static java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(ZoomifyHelper.class.getName());

    @Inject
    DeepZoomCacheService cacheService;

    @Inject
    DeepZoomTileSupport tileSupport;

    @Inject
    AggregatedAccessLogs accessLog;
    //@Named("database") //TODO: je tohle spravne? jeste existuje AggregatedAccessLogs a DNNTStatisticsAccessLogImpl

    @Inject
    MostDesirable mostDesirable;

    //@javax.inject.Inject
    //protected transient HttpAsyncClient client;

    /* TODO AK_NEW
    @javax.inject.Inject
    @Named("securedFedoraAccess")
    protected transient FedoraAccess fedoraAccess;

     */
    @javax.inject.Inject
    AkubraRepository akubraRepository;

    public Response buildImagePropertiesResponse(String pid, HttpServletRequest req) throws IOException, XPathExpressionException {
        try {
            this.accessLog.reportAccess(pid, FedoraUtils.IMG_FULL_STREAM);
            this.mostDesirable.saveAccess(pid, new java.util.Date());
        } catch (Exception e) {
            LOGGER.severe("cannot write statistic records");
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        }
        Date imgFullLastModified = lastModified(pid, FedoraUtils.IMG_FULL_STREAM);
        //not modified
        if (imageNotModified(req, imgFullLastModified)) {
            return Response.notModified().build();
        }
        String tilesUrl = akubraRepository.re().getTilesUrl(pid);
        //no tiles-url
        if (tilesUrl == null || tilesUrl.isEmpty()) {
            throw new NotFoundException("no tiles-url available for object %s", pid);
        }
        Response.ResponseBuilder resp = Response.ok();
        setNoCacheDateHeaders(resp);

        if (tilesUrl.equals(RelsExtHandler.CACHE_RELS_EXT_LITERAL)) { //kramerius4://deepZoomCache
            return renderEmbededDZIDescriptor(pid, resp);
        } else {//http://imageserver.mzk.cz/NDK/2017/08/540eec00-7200-11e7-aab4-005056827e52/uc_540eec00-7200-11e7-aab4-005056827e52_0002
            return renderImagePropertiesXml(pid, resp, tilesUrl);
        }
    }

    public Response buildTileResponse(String pid, HttpServletRequest req, int tileGroup, int level, int x, int y) throws IOException, XPathExpressionException {
        Date imgFullLastModified = lastModified(pid, FedoraUtils.IMG_FULL_STREAM);
        //not modified
        if (imageNotModified(req, imgFullLastModified)) {
            return Response.notModified().build();
        }
        String tilesUrl = akubraRepository.re().getTilesUrl(pid);
        //no tiles-url
        if (tilesUrl == null || tilesUrl.isEmpty()) {
            throw new NotFoundException("no tiles-url available for object %s", pid);
        }
        Response.ResponseBuilder resp = Response.ok();
        setDateHeaders(resp, imgFullLastModified);

        if (tilesUrl.equals(RelsExtHandler.CACHE_RELS_EXT_LITERAL)) { //kramerius4://deepZoomCache
            return renderEmbededTile(pid, level, x, y, resp);
        } else { //http://imageserver.mzk.cz/NDK/2017/08/540eec00-7200-11e7-aab4-005056827e52/uc_540eec00-7200-11e7-aab4-005056827e52_0002
            return renderTile(pid, tileGroup, level, x, y, resp, tilesUrl);
        }
    }

    private Response renderEmbededTile(String pid, int requestedLevel, int scol, int srow, Response.ResponseBuilder resp) throws IOException {
        try {
            if (!cacheService.isResolutionFilePresent(pid)) {
                Dimension rawDim = KrameriusImageSupport.readDimension(pid, FedoraUtils.IMG_FULL_STREAM, akubraRepository, 0);
                cacheService.writeResolution(pid, rawDim);
            }

            Dimension originalResolution = cacheService.getResolutionFromFile(pid);
            int maxLevels = tileSupport.getLevels(originalResolution, tileSupport.getTileSize());

            int offset = tileSupport.getClosestLevel(originalResolution, tileSupport.getTileSize(), 1);
            //deepzoom level
            int offsetLevel = requestedLevel + (offset);

            boolean tileCached = cacheService.isDeepZoomTilePresent(pid, offsetLevel, srow, scol);
            if (!tileCached) {
                // File dFile = cacheService.getDeepZoomLevelsFile(uuid);
                BufferedImage original = null;
                if (cacheService.isDeepZoomOriginalPresent(pid)) {
                    original = cacheService.getDeepZoomOriginal(pid);
                } else {
                    original = cacheService.createDeepZoomOriginalImageFromFedoraRAW(pid);
                    cacheService.writeDeepZoomOriginalImage(pid, original);
                }

                double scale = tileSupport.getScale(requestedLevel, maxLevels);
                Dimension scaled = tileSupport.getScaledDimension(new Dimension(original.getWidth(null), original.getHeight(null)), scale);
                int rows = tileSupport.getRows(scaled);
                int cols = tileSupport.getCols(scaled);
                int base = srow * cols;
                base = base + scol;
                LOGGER.info("scale is " + scale + " and dimension is " + scaled);

                KrameriusImageSupport.ScalingMethod method = KrameriusImageSupport.ScalingMethod.valueOf(KConfiguration.getInstance().getProperty("deepZoom.scalingMethod", "BICUBIC_STEPPED"));
                boolean iterateScaling = KConfiguration.getInstance().getConfiguration().getBoolean("deepZoom.iterateScaling", true);
                BufferedImage tile = this.tileSupport.getTileFromBigImage(original, requestedLevel, base, tileSupport.getTileSize(), method, iterateScaling);
                cacheService.writeDeepZoomTile(pid, offsetLevel, srow, scol, tile);
            }
            InputStream is = cacheService.getDeepZoomTileStream(pid, offsetLevel, srow, scol);
            StreamingOutput stream = output -> {
                IOUtils.copy(is, output);
            };
            resp.entity(stream);
            resp.type("image/jpeg");
            return resp.build();
        } catch (XPathExpressionException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    protected void setNoCacheDateHeaders(Response.ResponseBuilder resp) {
        CacheControl noCache = new CacheControl();
        noCache.setNoStore(true);
        noCache.setMustRevalidate(true);
        noCache.setNoCache(true);
        resp.cacheControl(noCache);
    }

    protected void setDateHeaders(Response.ResponseBuilder resp, Date lastModifiedDate) {
        Calendar inOneYear = Calendar.getInstance();
        inOneYear.roll(Calendar.YEAR, 1);
        resp.lastModified(lastModifiedDate);
        resp.expires(inOneYear.getTime());
    }

    boolean imageNotModified(HttpServletRequest request, Date imgFullLastModified) {
        long ifModifiedSince = -1l;
        try {
            ifModifiedSince = request.getDateHeader("If-Modified-Since");
        } catch (IllegalArgumentException e) {
            LOGGER.log(Level.WARNING, e.getMessage());
            return false;
        }
        if (ifModifiedSince == -1l) {
            return false;
        }
        Date clientsVersionFrom = new Date(ifModifiedSince);
        return !(imgFullLastModified.getTime() / 1000 > clientsVersionFrom.getTime() / 1000); //imgFullLastModified is not younger then If-Modified-Since (in seconds)
    }

    private Date lastModified(String pid, String stream) throws IOException {
        return this.akubraRepository.getDatastreamMetadata(pid, stream).getLastModified();
    }

    private Response renderImagePropertiesXml(String uuid, Response.ResponseBuilder resp, String tilesUrl) throws IOException {
        if (useFromReplicated()) { //use zoom servlet from replicated instance
            Document relsEXT = akubraRepository.re().get(uuid).asDom(false);
            tilesUrl = getZoomifyBaseUrlFromSomeReplicationSource(relsEXT, uuid);
        }
        if (tilesUrl == null) {
            throw new IOException("tiles-url not found");
        }
        if (tilesUrl.endsWith("/")) {
            tilesUrl = tilesUrl.substring(0, tilesUrl.length() - 1);
        }
        String imagePropertiesUrl = tilesUrl + "/ImageProperties.xml";
        //readFromImageServerNonblocking(imagePropertiesUrl, resp);
        readFromImageServerBlocking(imagePropertiesUrl, resp);
        return resp.build();
    }

    private void readFromImageServerBlocking(String url, Response.ResponseBuilder response) throws IOException {
        HttpGet httpGet = new HttpGet(url);
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            try (CloseableHttpResponse imgServerResponse = client.execute(httpGet)) {
                if (imgServerResponse.getStatusLine().getStatusCode() == SC_OK) {
                    HttpEntity entity = imgServerResponse.getEntity();
                    response.type(entity.getContentType().getValue());
                    try (InputStream imgSrvIn = entity.getContent()) {
                        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                        IOUtils.copy(imgSrvIn, buffer);
                        ByteArrayInputStream bufferIn = new ByteArrayInputStream(buffer.toByteArray());
                        StreamingOutput stream = output -> {
                            IOUtils.copy(bufferIn, output);
                        };
                        response.entity(stream);
                        //response.setHeader("Access-Control-Allow-Origin", "*");
                        Header cacheControl = imgServerResponse.getLastHeader("Cache-Control");
                        if (cacheControl != null) {
                            response.header(cacheControl.getName(), cacheControl.getValue());
                        }
                        Header lastModified = imgServerResponse.getLastHeader("Last-Modified");
                        if (lastModified != null) {
                            response.header(lastModified.getName(), lastModified.getValue());
                        }
                    }
                } else {
                    throw new HttpResponseException(imgServerResponse.getStatusLine().getStatusCode(), imgServerResponse.getStatusLine().getReasonPhrase());
                }
            }
        }
    }

    private void readFromImageServerNonblocking(String url, Response.ResponseBuilder response) throws IOException {
        HttpGet httpGet = new HttpGet(url);
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            try (CloseableHttpResponse imgServerResponse = client.execute(httpGet)) {
                if (imgServerResponse.getStatusLine().getStatusCode() == SC_OK) {
                    HttpEntity entity = imgServerResponse.getEntity();
                    response.type(entity.getContentType().getValue());
                    StreamingOutput sout = new StreamingOutput() {
                        @Override
                        public void write(OutputStream os) throws IOException, WebApplicationException {
                            try (InputStream io = entity.getContent()) {
                                byte[] buff = new byte[16384];
                                int count = 0;
                                while ((count = io.read(buff, 0, buff.length)) != -1) {
                                    os.write(buff, 0, count);
                                }
                            } finally {
                                imgServerResponse.close();
                            }
                        }
                    };
                    response.entity(sout);
                    //response.setHeader("Access-Control-Allow-Origin", "*");
                    Header cacheControl = imgServerResponse.getLastHeader("Cache-Control");
                    if (cacheControl != null) {
                        response.header(cacheControl.getName(), cacheControl.getValue());
                    }
                    Header lastModified = imgServerResponse.getLastHeader("Last-Modified");
                    if (lastModified != null) {
                        response.header(lastModified.getName(), lastModified.getValue());
                    }
                } else {
                    throw new HttpResponseException(imgServerResponse.getStatusLine().getStatusCode(), imgServerResponse.getStatusLine().getReasonPhrase());
                }
            }
        }
    }

    private boolean useFromReplicated() {
        return KConfiguration.getInstance().getConfiguration().getBoolean("zoom.useFromReplicated", false);
    }

    private Response renderEmbededDZIDescriptor(String uuid, Response.ResponseBuilder resp) throws IOException, XPathExpressionException {
        //<IMAGE_PROPERTIES WIDTH="8949" HEIGHT="6684" NUMTILES="945" NUMIMAGES="1" VERSION="1.8" TILESIZE="256" />
        if (!cacheService.isDeepZoomDescriptionPresent(uuid)) {
            Dimension rawDim = KrameriusImageSupport.readDimension(uuid, FedoraUtils.IMG_FULL_STREAM, akubraRepository, 0);
            cacheService.writeDeepZoomDescriptor(uuid, rawDim, tileSupport.getTileSize());
            cacheService.writeResolution(uuid, rawDim);
        }
        InputStream inputStream = cacheService.getDeepZoomDescriptorStream(uuid);
        try {
            Document document = XMLUtils.parseDocument(inputStream);
            Element docelement = document.getDocumentElement();
            String tileSize = docelement.getAttribute("TileSize");
            Element sizeElement = XMLUtils.findElement(docelement, "Size");
            String width = sizeElement.getAttribute("Width");
            String height = sizeElement.getAttribute("Height");

            int iWidth = Integer.parseInt(width);
            int iHeight = Integer.parseInt(height);
            int iTileSize = Integer.parseInt(tileSize);

            double nTilesX = iWidth / iTileSize;
            int iNTilesX = 0;

            if (Math.floor(nTilesX) < nTilesX) {
                iNTilesX = (int) (Math.floor(nTilesX) + 1);
            } else {
                iNTilesX = (int) Math.floor(nTilesX);
            }

            double nTilesY = iHeight / iTileSize;
            int iNTilesY = 0;
            if (Math.floor(nTilesY) < iNTilesY) {
                iNTilesY = (int) (Math.floor(nTilesY) + 1);
            } else {
                iNTilesY = (int) Math.floor(nTilesY);
            }

            StringBuffer buffer = new StringBuffer();
            buffer.append("<IMAGE_PROPERTIES WIDTH=\"").append(width).append('"').append(" HEIGHT=\"").append(height).append('"');
            buffer.append("  NUMIMAGES='1' ");
            buffer.append("  NUMTILES='").append(iNTilesX * iNTilesY).append("'");
            buffer.append("  VERSION='1.8' TILESIZE=\"").append(tileSize).append("\" />");

            resp.entity(buffer.toString());
            resp.type(MediaType.APPLICATION_XML_TYPE);
            return resp.build();
        } catch (ParserConfigurationException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new RuntimeException(e);
        } catch (SAXException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new RuntimeException(e);
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
        }
    }

    private Response renderTile(String uuid, int tileGroup, int level, int x, int y, Response.ResponseBuilder resp, String tilesUrl) throws IOException {
        if (useFromReplicated()) {
            Document relsEXT = akubraRepository.re().get(uuid).asDom(false);
            tilesUrl = getZoomifyBaseUrlFromSomeReplicationSource(relsEXT, uuid);
        }
        if (tilesUrl == null) {
            throw new IOException("tiles-url not found");
        }
        String tileUrl = String.format("%s/TileGroup%d/%d-%d-%d.jpg", tilesUrl, tileGroup, level, x, y);
        //readFromImageServerNonblocking(tileUrl, resp);
        readFromImageServerBlocking(tileUrl, resp);
        return resp.build();
    }

    /**
     * see cz.incad.Kramerius.imaging.utils.ZoomChangeFromReplicated
     */
    private String getZoomifyBaseUrlFromSomeReplicationSource(Document relsExt, String pid) {
        //TODO: uses old ZoomifyServlet
        String replicatedFrom = getFirstReplicatedFrom(relsExt);
        if (replicatedFrom != null) {
            int indexOf = replicatedFrom.indexOf("/handle/");
            String app = replicatedFrom.substring(0, indexOf);
            return app + "/zoomify/" + pid;
        } else
            return null;
    }

    /**
     * Get content of first element "replicatedFrom" or null
     * Example:
     * <rdf:RDF xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#">
     * <rdf:Description rdf:about="info:fedora/uuid:69dec000-490a-11de-8cf2-000d606f5dc6">
     * <hasModel xmlns="info:fedora/fedora-system:def/model#" rdf:resource="info:fedora/model:page"></hasModel>
     * <itemID xmlns="http://www.openarchives.org/OAI/2.0/">uuid:69dec000-490a-11de-8cf2-000d606f5dc6</itemID>
     * <file xmlns="http://www.nsdl.org/ontologies/relationships#">BOA001_3630900040.djvu</file>
     * <handle xmlns="http://www.nsdl.org/ontologies/relationships#">BOA001/915466</handle>
     * <tiles-url xmlns="http://www.nsdl.org/ontologies/relationships#">http://kramerius.mzk.cz/search/zoomify/uuid:69dec000-490a-11de-8cf2-000d606f5dc6</tiles-url>
     * <replicatedFrom xmlns="http://www.nsdl.org/ontologies/relationships#">http://kramerius.mzk.cz/search/handle/uuid:69dec000-490a-11de-8cf2-000d606f5dc6</replicatedFrom>
     * <replicatedFrom xmlns="http://www.nsdl.org/ontologies/relationships#">https://cdk.lib.cas.cz/search/handle/uuid:69dec000-490a-11de-8cf2-000d606f5dc6</replicatedFrom>
     * <rdf:isMemberOfCollection rdf:resource="info:fedora/vc:ff390e7e-05cc-4fcc-98db-89bd9e6e1f41"></rdf:isMemberOfCollection>
     * <rdf:isMemberOfCollection rdf:resource="info:fedora/vc:2626d5c0-cd88-4bd5-8bd5-b592b89b6313"></rdf:isMemberOfCollection>
     * <policy xmlns="http://www.nsdl.org/ontologies/relationships#">policy:private</policy>
     * </rdf:Description>
     * </rdf:RDF>
     *
     * @param relsExt
     * @return
     */
    private String getFirstReplicatedFrom(Document relsExt) {
        Element descElement = XMLUtils.findElement(
                relsExt.getDocumentElement(), "Description",
                RepositoryNamespaces.RDF_NAMESPACE_URI);
        List<Element> delems = XMLUtils.getElements(descElement);
        for (Element del : delems) {
            if (del.getNamespaceURI() != null) {
                if (del.getNamespaceURI().equals(RepositoryNamespaces.KRAMERIUS_URI)
                        && del.getLocalName().equals("replicatedFrom")) {
                    return del.getTextContent();
                }
            }
        }
        return null;
    }

}
