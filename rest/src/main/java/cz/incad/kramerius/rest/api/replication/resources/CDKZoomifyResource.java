package cz.incad.kramerius.rest.api.replication.resources;

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

import org.apache.http.nio.client.HttpAsyncClient;
import org.json.JSONException;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;

import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.ObjectPidsPath;
import cz.incad.kramerius.SolrAccess;
import cz.incad.kramerius.rest.api.exceptions.ActionNotAllowed;
import cz.incad.kramerius.rest.api.exceptions.BadRequestException;
import cz.incad.kramerius.rest.api.utils.DisectZoom;
import cz.incad.kramerius.security.IsActionAllowed;
import cz.incad.kramerius.security.SecuredActions;
import cz.incad.kramerius.security.User;
import cz.incad.kramerius.statistics.accesslogs.AggregatedAccessLogs;
import cz.incad.kramerius.utils.RelsExtHelper;

public class CDKZoomifyResource extends AbstractTileResource {

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


	protected boolean permited(IsActionAllowed isActionAllowed, SolrAccess solrAccess,  String pid) throws IOException {
		long start = System.currentTimeMillis();
		try {
			ObjectPidsPath[] paths = solrAccess.getPath(pid);
			boolean permited = false;
			for (ObjectPidsPath pth : paths) {
			    permited = this.actionAllowed.isActionAllowed(userProvider.get(), SecuredActions.READ.getFormalName(), pid, null, pth).flag();
			    if (permited) break;
			}
			return permited;
		} finally {
			LOGGER.fine("Permited took "+(System.currentTimeMillis() - start)+ " ms");
		}
	}


    
    public Response renderZoomifyTile(String pid, String slevel, String x,String y, String ext) throws SQLException, UnsupportedEncodingException, IOException, XPathExpressionException {
    	if (permited(actionAllowed, solrAccess, pid)) {
        	String relsExtUrl = RelsExtHelper.getRelsExtTilesUrl(pid, this.fedoraAccess);
            if (relsExtUrl != null) {
                ResponseBuilder builder = Response.ok();
            	String formatted = String.format("%s/TileGroup0/%s-%s-%s.%s", relsExtUrl,slevel,x,y,ext);
                copyFromImageServer( formatted, new ByteArrayOutputStream(), builder);
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
        	String relsExtUrl = RelsExtHelper.getRelsExtTilesUrl(pid, this.fedoraAccess);
            if (relsExtUrl != null) {
                if (relsExtUrl.endsWith("/")) relsExtUrl = relsExtUrl.substring(0, relsExtUrl.length()-1);
                ResponseBuilder builder = Response.ok();
                copyFromImageServer(relsExtUrl.toString()+"/ImageProperties.xml",new ByteArrayOutputStream() , builder);
                return builder.build();
            } else {
            	throw new BadRequestException("Bad request");
            }
    	} finally {
    		LOGGER.fine("Request to IIP server took "+(System.currentTimeMillis() - start)+" ms ");
    	}
        
    }
}
