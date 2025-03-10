package cz.incad.kramerius.rest.apiNew.cdk.v70.resources;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URLDecoder;
import java.sql.SQLException;
import java.util.StringTokenizer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.xml.xpath.XPathExpressionException;

import org.ceskaexpedice.akubra.AkubraRepository;
import org.json.JSONException;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;

import cz.incad.kramerius.ObjectPidsPath;
import cz.incad.kramerius.SolrAccess;
import cz.incad.kramerius.rest.IIPImagesSupport;
import cz.incad.kramerius.rest.api.exceptions.ActionNotAllowed;
import cz.incad.kramerius.rest.api.exceptions.BadRequestException;
import cz.incad.kramerius.rest.api.utils.DisectZoom;
import cz.incad.kramerius.security.RightsResolver;
import cz.incad.kramerius.security.SecuredActions;
import cz.incad.kramerius.security.User;
import cz.incad.kramerius.statistics.accesslogs.AggregatedAccessLogs;

public class CDKZoomifyResource extends AbstractTileResource {

	@Inject
    @Named("cachedSolrAccess")
    private SolrAccess solrAccess;

    @Inject
    private RightsResolver actionAllowed;

    @Inject
    private Provider<User> userProvider;

    @Inject
    Provider<HttpServletRequest> requestProvider;
    
    @Inject
    Provider<HttpServletResponse> responseProvider;

    /* TODO AK_NEW
    @Inject
    @Named("cachedFedoraAccess")
    private transient FedoraAccess fedoraAccess;

     */

    @Inject
    private AkubraRepository akubraRepository;


    @Inject
    AggregatedAccessLogs aggregatedAccessLogs;
    
    
    public Response zoomifyManifest(String pid) {
    	long start = System.currentTimeMillis();
    	long stopBeforePermited = start;
    	long stopAfterReportAccess = start;
    	try {
        	String requestURL = this.requestProvider.get().getRequestURL().toString();
            String zoomUrl = DisectZoom.disectZoom(requestURL);
            StringTokenizer tokenizer = new StringTokenizer(zoomUrl, "/");

            String spid = tokenizer.nextToken();
            String rest = tokenizer.hasMoreTokens() ?  tokenizer.nextToken() : "";

            pid = URLDecoder.decode(pid, "UTF-8");
            stopBeforePermited = System.currentTimeMillis();
            boolean permited = permited(this.actionAllowed, this.solrAccess,  pid);
            if (permited) {
                try {
                    reportAccess(this.aggregatedAccessLogs, pid);
                    stopAfterReportAccess = System.currentTimeMillis();
                    
                    return renderZoomifyXMLDescriptor(pid);
                } catch (JSONException e) {
                    LOGGER.severe(e.getMessage());
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
                } catch (XPathExpressionException e) {
                    LOGGER.severe(e.getMessage());
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
                } catch (SQLException e) {
                    LOGGER.severe(e.getMessage());
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
                }
            } else {
                throw new ActionNotAllowed("not allowed");
            }
        } catch (IOException e) {
            LOGGER.severe(e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        } finally {
        	LOGGER.fine("Track BeforePermited:"+(stopBeforePermited - start)+", AfterReportAcess:"+(stopAfterReportAccess - start)+", In finally block :"+(System.currentTimeMillis() - start));
        }
    }


    protected boolean permited(RightsResolver isActionAllowed, SolrAccess solrAccess, String pid) throws IOException {
        long start = System.currentTimeMillis();
        try {
            ObjectPidsPath[] paths = solrAccess.getPidPaths(pid);
            boolean permited = false;
            for (ObjectPidsPath pth : paths) {
                permited = this.actionAllowed
                        .isActionAllowed(userProvider.get(), SecuredActions.A_READ.getFormalName(), pid, null, pth)
                        .flag();
                if (permited)
                    break;
            }
            return permited;
        } finally {
            LOGGER.fine("Permited took " + (System.currentTimeMillis() - start) + " ms");
        }
    }

    
    public Response renderZoomifyTile(String pid, String slevel, String x,String y, String ext) throws SQLException, UnsupportedEncodingException, IOException, XPathExpressionException {
    	if (permited(actionAllowed, solrAccess, pid)) {
        	String relsExtUrl = akubraRepository.re().getTilesUrl(pid);
            if (relsExtUrl != null) {
                ResponseBuilder builder = Response.ok();
                String formatted = String.format("%s/TileGroup0/%s-%s-%s.%s", relsExtUrl,slevel,x,y,ext);
                // forward; 
                IIPImagesSupport.blockingCopyFromImageServer(getClient(), formatted, new ByteArrayOutputStream(), builder);
                return builder.build();
                
            } else {
            	throw new BadRequestException("Bad request");
            }
    	} else {
            throw new ActionNotAllowed("not allowed");
    	}
    }

    
    
    private Response renderZoomifyXMLDescriptor(String pid) throws MalformedURLException, IOException, SQLException, XPathExpressionException {
    	long start = System.currentTimeMillis();
    	try {
        	String relsExtUrl = akubraRepository.re().getTilesUrl(pid);
            if (relsExtUrl != null) {
                if (relsExtUrl.endsWith("/")) relsExtUrl = relsExtUrl.substring(0, relsExtUrl.length()-1);
                ResponseBuilder builder = Response.ok();
                IIPImagesSupport.blockingCopyFromImageServer(getClient(), relsExtUrl.toString()+"/ImageProperties.xml",new ByteArrayOutputStream() , builder);
                return builder.build();
            } else {
            	throw new BadRequestException("Bad request");
            }
    	} finally {
    		LOGGER.fine("Request to IIP server took "+(System.currentTimeMillis() - start)+" ms ");
    	}
        
    }
}
