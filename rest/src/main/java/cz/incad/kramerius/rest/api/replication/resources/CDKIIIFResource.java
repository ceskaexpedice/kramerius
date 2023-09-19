package cz.incad.kramerius.rest.api.replication.resources;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.apache.commons.io.IOUtils;
import org.apache.http.nio.client.HttpAsyncClient;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;

import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.ObjectPidsPath;
import cz.incad.kramerius.SolrAccess;
import cz.incad.kramerius.impl.FedoraAccessImpl;
import cz.incad.kramerius.rest.api.exceptions.ActionNotAllowed;
import cz.incad.kramerius.rest.api.exceptions.BadRequestException;
import cz.incad.kramerius.rest.api.k5.client.item.utils.IIIFUtils;
import cz.incad.kramerius.security.IsActionAllowed;
import cz.incad.kramerius.security.SecuredActions;
import cz.incad.kramerius.security.User;
import cz.incad.kramerius.statistics.accesslogs.AggregatedAccessLogs;
import cz.incad.kramerius.utils.RESTHelper;
import cz.incad.kramerius.utils.StringUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;

public class CDKIIIFResource extends AbstractTileResource {

	public static final Logger LOGGER = Logger.getLogger(CDKIIIFResource.class.getName());
	
    static Map<String, String> IIIF_SUPPORTED_MIMETYPES = new HashMap<>();
    static  {
        IIIF_SUPPORTED_MIMETYPES.put("jpg", "image/jpeg");
        IIIF_SUPPORTED_MIMETYPES.put("tif", "image/tiff");
        IIIF_SUPPORTED_MIMETYPES.put("png", "image/png");
        IIIF_SUPPORTED_MIMETYPES.put("jp2", "image/jp2");
        IIIF_SUPPORTED_MIMETYPES.put("pdf", "application/pdf");
        IIIF_SUPPORTED_MIMETYPES.put("webp", "image/webp");
    }

	
    @Inject
    @Named("cachedSolrAccess")
    private SolrAccess solrAccess;

    @Inject
    private IsActionAllowed actionAllowed;

    @Inject
    private Provider<User> userProvider;

    @Inject
    Provider<HttpServletRequest> requestProvider;

    @Inject
    @Named("cachedFedoraAccess")
    private transient FedoraAccess fedoraAccess;


    @Inject
    AggregatedAccessLogs aggregatedAccessLogs;

    
    public Response iiifManifest(String pid) {
        try {
//            String requestURL = this.requestProvider.get().getRequestURL().toString();
//            String zoomUrl = disectZoom(requestURL);
//            zoomUrl = StringUtils.minus(zoomUrl, "v5.0/cdk/forward/iiif/");
//            StringTokenizer tokenizer = new StringTokenizer(zoomUrl, "/");
            pid = URLDecoder.decode(pid, "UTF-8");

            ObjectPidsPath[] paths = solrAccess.getPath(pid);
            boolean permited = false;
            for (ObjectPidsPath pth : paths) {
                permited = this.actionAllowed.isActionAllowed(userProvider.get(), SecuredActions.READ.getFormalName(), pid, null, pth).flag();
                if (permited) break;
            }

            if (permited) {
                try {
                    reportAccess(aggregatedAccessLogs, pid);
                    String u = IIIFUtils.iiifImageEndpoint(pid, this.fedoraAccess);
                    if (u != null) {
                    	if (!u.endsWith("/")) { u = u+"/"; }
                    	u = u +"info.json";

                    	HttpURLConnection con = (HttpURLConnection) RESTHelper.openConnection(u, "", "");
                        InputStream inputStream = con.getInputStream();
                        String json = IOUtils.toString(inputStream, Charset.defaultCharset());
                        JSONObject object = new JSONObject(json);
                        HttpServletRequest req = this.requestProvider.get();
                        String urlRequest = req.getRequestURL().toString();
                        object.put("@id", urlRequest.substring(0, urlRequest.lastIndexOf('/')));
                        
                        return Response.ok().entity(object.toString()).build();

                    } else {
                    	throw new BadRequestException("bad request");
                    }
                } catch (JSONException e) {
                    LOGGER.severe(e.getMessage());
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
                }
            } else {
                throw new ActionNotAllowed("not allowed");
            }
        } catch (IOException e) {
            LOGGER.severe(e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }


    public static String disectZoom(String requestURL) {
        try {
            StringBuffer buffer = new StringBuffer();
            URL url = new URL(requestURL);
            String path = url.getPath();
            String application = path;
            StringTokenizer tokenizer = new StringTokenizer(path, "/");
            if (tokenizer.hasMoreTokens())
                application = tokenizer.nextToken();
            String zoomServlet = path;
            if (tokenizer.hasMoreTokens())
                zoomServlet = tokenizer.nextToken();
            // check handle servlet
            while (tokenizer.hasMoreTokens()) {
                buffer.append(tokenizer.nextElement());
                if (tokenizer.hasMoreTokens())
                    buffer.append("/");
            }
            return buffer.toString();
        } catch (MalformedURLException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            return "<no handle>";
        }
    }

	//0,0,1024,1024/256,/0/default.jpg
    public Response iiifTile(String pid, String region, String size, String rotation, String qf) throws IOException {
    	String u = IIIFUtils.iiifImageEndpoint(pid, this.fedoraAccess);
        if(u != null) {

            String defaultMime = IIIF_SUPPORTED_MIMETYPES.get("jpg");

        	StringBuilder url = new StringBuilder(u);
        	if (!u.endsWith("/")) { url.append("/"); }
        	url.append(String.format("%s/%s/%s/%s", region, size, rotation,qf));
 
            String mime = defaultMime;
            String[] splited = qf.split("\\.");
            if (splited.length > 1) {
                mime =  IIIF_SUPPORTED_MIMETYPES.containsKey(splited[1]) ? IIIF_SUPPORTED_MIMETYPES.get(splited[1]) :  defaultMime;
            }
        	LOGGER.info(String.format("Copy tile from IIIF server %s", url.toString()));
        	ResponseBuilder builder = Response.ok();
            copyFromImageServer(url.toString(),new ByteArrayOutputStream(), builder, mime);
            return builder.build();
       } else {
    	   throw new BadRequestException("bad request");
       }
	}

}
