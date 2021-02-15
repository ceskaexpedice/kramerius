package cz.incad.kramerius.rest.apiNew.client.v60;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.MostDesirable;
import cz.incad.kramerius.imaging.DeepZoomCacheService;
import cz.incad.kramerius.imaging.DeepZoomTileSupport;
import cz.incad.kramerius.rest.apiNew.exceptions.NotFoundException;
import cz.incad.kramerius.statistics.StatisticsAccessLog;
import cz.incad.kramerius.utils.FedoraUtils;
import cz.incad.kramerius.utils.RelsExtHelper;
import cz.incad.kramerius.utils.XMLUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;
import cz.incad.kramerius.utils.imgs.KrameriusImageSupport;
import org.antlr.stringtemplate.StringTemplate;
import org.antlr.stringtemplate.StringTemplateGroup;
import org.antlr.stringtemplate.language.DefaultTemplateLexer;
import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.nio.client.HttpAsyncClient;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import java.awt.*;
import java.io.*;
import java.net.MalformedURLException;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.Level;

import static org.apache.http.HttpStatus.SC_OK;

public class ZoomifyHelper {

    static java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(ZoomifyHelper.class.getName());

    @Inject
    DeepZoomCacheService cacheService;

    @Inject
    DeepZoomTileSupport tileSupport;

    @Inject
    StatisticsAccessLog accessLog;

    /*@Inject
    RightsResolver rightsResolver;

    @Inject
    Provider<User> userProvider;

    @Inject
    SolrAccess solrAccess;*/

    @Inject
    MostDesirable mostDesirable;

    @javax.inject.Inject
    protected transient HttpAsyncClient client;

    @javax.inject.Inject
    @Named("securedFedoraAccess")
    protected transient FedoraAccess fedoraAccess;


    //TODO: viz tady
    // http://localhost:8080/search/api/client/v6.0/items/uuid:f3d972ba-a723-493b-bd21-cd1a4a041687/image/zoomify/ImageProperties.xml
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
        String tilesUrl = RelsExtHelper.getRelsExtTilesUrl(pid, this.fedoraAccess);
        //no tiles-url
        if (tilesUrl == null || tilesUrl.isEmpty()) {
            throw new NotFoundException("no tiles-url available for object %s", pid);
        }
        //System.out.println("tiles-url: " + tilesUrl);
        Response.ResponseBuilder resp = Response.ok();
        setDateHeaders(resp, imgFullLastModified);

        if (tilesUrl.equals(RelsExtHelper.CACHE_RELS_EXT_LITERAL)) { //tiles-url: kramerius4://deepZoomCache
            return renderEmbededDZIDescriptor(pid, resp);
        }
        try {
            //TODO: tu vyjimku pryc
            return renderIIPrenderXMLDescriptor(pid, resp, tilesUrl);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public Response buildTileResponse(String pid, HttpServletRequest req, int tileGroup, int level, int x, int y) throws IOException, XPathExpressionException {
        Date imgFullLastModified = lastModified(pid, FedoraUtils.IMG_FULL_STREAM);
        //not modified
        if (imageNotModified(req, imgFullLastModified)) {
            return Response.notModified().build();
        }
        String tilesUrl = RelsExtHelper.getRelsExtTilesUrl(pid, this.fedoraAccess);
        //no tiles-url
        if (tilesUrl == null || tilesUrl.isEmpty()) {
            throw new NotFoundException("no tiles-url available for object %s", pid);
        }
        //System.out.println("tiles-url: " + tilesUrl);
        Response.ResponseBuilder resp = Response.ok();
        setDateHeaders(resp, imgFullLastModified);

        if (tilesUrl.equals(RelsExtHelper.CACHE_RELS_EXT_LITERAL)) { //tilesUrl: kramerius4://deepZoomCache
            return renderIIPTile(pid, tileGroup, level, x, y, resp, tilesUrl);
        }
        return renderIIPTile(pid, tileGroup, level, x, y, resp, tilesUrl);
    }

    protected void setDateHeaders(Response.ResponseBuilder resp, Date lastModifiedDate) throws IOException {
        Calendar inOneYear = Calendar.getInstance();
        inOneYear.roll(Calendar.YEAR, 1);
        resp.lastModified(lastModifiedDate);
        resp.expires(inOneYear.getTime());
    }

    boolean imageNotModified(HttpServletRequest request, Date imgFullLastModified) throws IOException {
        long ifModifiedSince;
        try {
            ifModifiedSince = request.getDateHeader("If-Modified-Since");
        } catch (IllegalArgumentException e) {
            LOGGER.log(Level.WARNING, e.getMessage());
            return false;
        }
        if (ifModifiedSince != -1) {
            return false;
        }
        Date clientsVersionFrom = new Date(ifModifiedSince);
        return !imgFullLastModified.after(clientsVersionFrom);
    }

    private Date lastModified(String pid, String stream) throws IOException {
        return this.fedoraAccess.getStreamLastmodifiedFlag(pid, stream);
    }

    private Response renderIIPrenderXMLDescriptor(String uuid, Response.ResponseBuilder resp, String tilesUrl) throws MalformedURLException, IOException, SQLException, XPathExpressionException {
        String urlForStream = tilesUrl;
        System.out.println("urlForStream: " + urlForStream);
        if (useFromReplicated()) { //use zoom servlet from replicated instance
            System.out.println("zoom.useFromReplicated: true");
            Document relsEXT = this.fedoraAccess.getRelsExt(uuid);
            //TODO: ZoomChangeFromReplicated
            urlForStream = ZoomChangeFromReplicated.zoomifyAddress(relsEXT, uuid);
        }
        if (urlForStream != null) {
            if (urlForStream.endsWith("/")) {
                urlForStream = urlForStream.substring(0, urlForStream.length() - 1);
            }
            /*StringTemplate dziUrl = stGroup().getInstanceOf("zoomify");
            dziUrl.setAttribute("url", urlForStream);
            String finalUrl = dziUrl.toString();*/
            String finalUrl = urlForStream + "/ImageProperties.xml";
            //System.out.println("dzi.toString: " + finalUrl);
            //finalUrl = "https://kramerius.difmoe.eu/search/img?pid=uuid:1b7e413a-9ec9-4eb7-95e2-d22f539a846f&stream=IMG_FULL&action=GETRAW";
            //finalUrl = "http://processing.difmoe.eu/pixelprint/Aussiger_Bote/AB_1951.pdf";
            //finalUrl = "https://audio.dev.digitallibrary.cz/kfbz/kpw08515728/09.jpg";
            readFromImageServerNonblocking(finalUrl, resp);
            //readFromImageServerBlocking(finalUrl, resp);
            return resp.build();

            //TODO: actually send dat
            //copyFromImageServer(dziUrl.toString(), resp);
            //return resp.entity("TODO").build();
        } else {
            throw new RuntimeException("TODO");
        }
    }

    //TODO: precejen streamingOutput
    //https://stackoverflow.com/questions/29637151/jersey-streamingoutput-as-response-entity
    private void readFromImageServerBlocking(String url, Response.ResponseBuilder response) throws IOException {
        HttpGet httpGet = new HttpGet(url);
        CloseableHttpClient client = HttpClients.createDefault();
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

    private void readFromImageServerNonblocking(String url, Response.ResponseBuilder response) throws IOException {
        HttpGet httpGet = new HttpGet(url);
        CloseableHttpClient client = HttpClients.createDefault();
        CloseableHttpResponse imgServerResponse = client.execute(httpGet);
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
                            //System.out.println("written " + count);
                        }
                    } finally {
                        imgServerResponse.close();
                        //System.out.println("resp closed");
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

    private boolean useFromReplicated() {
        return KConfiguration.getInstance().getConfiguration().getBoolean("zoom.useFromReplicated", false);
    }

    @Deprecated
    protected String getURLForStream(String uuid, String urlFromRelsExt) throws IOException, XPathExpressionException, SQLException {
        StringTemplate template = new StringTemplate(urlFromRelsExt);
        // template.setAttribute("internalstream",
        // getPathForInternalStream(uuid));
        return template.toString();
    }

    private Response renderEmbededDZIDescriptor(String uuid, Response.ResponseBuilder resp) throws IOException, FileNotFoundException, XPathExpressionException {
        if (!cacheService.isDeepZoomDescriptionPresent(uuid)) {
            Dimension rawDim = KrameriusImageSupport.readDimension(uuid, FedoraUtils.IMG_FULL_STREAM, fedoraAccess, 0);
            cacheService.writeDeepZoomDescriptor(uuid, rawDim, tileSupport.getTileSize());
            cacheService.writeResolution(uuid, rawDim);
        }
        InputStream inputStream = cacheService.getDeepZoomDescriptorStream(uuid);

        //<IMAGE_PROPERTIES WIDTH="8949" HEIGHT="6684" NUMTILES="945" NUMIMAGES="1" VERSION="1.8" TILESIZE="256" />

        //resp.type()
        //resp.setContentType("application/xml");
        resp.type(MediaType.APPLICATION_XML_TYPE);

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
            //IOUtils.copyStreams(new ByteArrayInputStream(buffer.toString().getBytes("UTF-8")), resp.getOutputStream());

            //new ByteArrayInputStream(buffer.toString().getBytes("UTF-8")

            resp.entity(buffer.toString());
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

    private Response renderIIPTile(String uuid, int tileGroup, int level, int x, int y, Response.ResponseBuilder resp, String url) throws IOException {
        //String dataStreamUrl = getURLForStream(uuid, url);
        String dataStreamUrl = url;
        //System.out.println("baseUrl: " + dataStreamUrl);
        if (useFromReplicated()) {
            Document relsEXT = this.fedoraAccess.getRelsExt(uuid);
            dataStreamUrl = ZoomChangeFromReplicated.zoomifyAddress(relsEXT, uuid);
        }
        if (dataStreamUrl != null) {
            //String url = dataStreamUrl + "/TileGroup0/" + level + "/" + x + "/" + y;
            String tileUrl = String.format("%s/TileGroup%d/%d-%d-%d.jpg", dataStreamUrl, tileGroup, level, x, y);
            //System.out.println("tile url: " + tileUrl);
            readFromImageServerNonblocking(tileUrl, resp);
            return resp.build();
        } else {
            throw new RuntimeException("TODO");
        }
    }


    private InputStream readFromImageServer(String url) throws IOException {
        HttpGet httpGet = new HttpGet(url);
        CloseableHttpClient client = HttpClients.createDefault();
        try (CloseableHttpResponse response = client.execute(httpGet)) {
            if (response.getStatusLine().getStatusCode() == SC_OK) {
                try (InputStream src = response.getEntity().getContent()) {
                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    IOUtils.copy(src, bos);
                    return new ByteArrayInputStream(bos.toByteArray());

                }
            } else {
                throw new HttpResponseException(response.getStatusLine().getStatusCode(), response.getStatusLine().getReasonPhrase());
            }
        }
    }

    /*public void copyFromImageServer(String urlString, final Response.ResponseBuilder resp) throws IOException {
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
    }*/

    public static StringTemplateGroup stGroup() {
        InputStream is = ZoomifyHelper.class.getResourceAsStream("/cz/incad/Kramerius/imaging/iipforward.stg");
        StringTemplateGroup grp = new StringTemplateGroup(new InputStreamReader(is), DefaultTemplateLexer.class);
        return grp;
    }


}
