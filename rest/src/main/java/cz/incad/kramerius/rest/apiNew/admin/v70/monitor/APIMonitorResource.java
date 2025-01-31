package cz.incad.kramerius.rest.apiNew.admin.v70.monitor;

import com.google.inject.Inject;
import cz.incad.kramerius.processes.LRProcessManager;
import cz.incad.kramerius.rest.api.exceptions.ActionNotAllowed;
import cz.incad.kramerius.rest.api.exceptions.BadRequestException;
import cz.incad.kramerius.rest.apiNew.exceptions.InternalErrorException;
import cz.incad.kramerius.rest.apiNew.monitoring.APICallMonitor;
import cz.incad.kramerius.security.SecuredActions;
import cz.incad.kramerius.utils.conf.KConfiguration;
import org.apache.http.client.HttpResponseException;
import org.json.JSONException;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.*;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static org.apache.http.HttpStatus.SC_BAD_REQUEST;

@Path("/admin/v7.0/monitor")
public class APIMonitorResource {

    public static final Logger LOGGER = Logger.getLogger(APIMonitorResource.class.getName());

    @Inject
    APICallMonitor monitor;

    @GET
    @Path("search")
    public Response search(@Context UriInfo uriInfo, @Context HttpHeaders headers, @QueryParam("wt") String wt) {
        try {

            this.monitor.commit();

            MultivaluedMap<String, String> queryParameters = uriInfo.getQueryParameters();
            List<String> urlParams = new ArrayList<>();

            Set<String> keys = queryParameters.keySet();
            for (String k : keys) {
                List<String> params = queryParameters.get(k);
                urlParams.add(params.stream().map(value-> {
                    try {
                        String encval = URLEncoder.encode(value, "UTF-8");
                        return String.format("%s=%s", k,encval);
                    } catch (UnsupportedEncodingException e) {
                        LOGGER.log(Level.SEVERE,e.getMessage(),e);
                        return null;
                    }
                }).collect(Collectors.joining("&")));
            }

            String queryString = urlParams.stream().collect(Collectors.joining("&"));

            if ("json".equals(wt)) {
                return Response.ok().type(MediaType.APPLICATION_JSON + ";charset=utf-8").entity(this.monitor.apiMonitorRequestJson(queryString)).build();
            } else if ("xml".equals(wt)) {
                return Response.ok().type(MediaType.APPLICATION_XML + ";charset=utf-8").entity(this.monitor.apiMonitorRequestXML(queryString)).build();
            } else { //format not specified in query param "wt"
                boolean preferXmlAccordingToHeaderAccept = false;
                List<String> headerAcceptValues = headers.getRequestHeader("Accept");
                if (headerAcceptValues != null) { //can be null instead of empty list in some implementations
                    for (String headerValue : headerAcceptValues) {
                        if ("application/xml".equals(headerValue) || "text/xml".equals(headerValue)) {
                            preferXmlAccordingToHeaderAccept = true;
                            break;
                        }
                    }
                }
                if (preferXmlAccordingToHeaderAccept) { //header Accept contains "application/xml" or "text/xml"

                    return Response.ok().type(MediaType.APPLICATION_XML + ";charset=utf-8").entity(this.monitor.apiMonitorRequestXML(queryString)).build();
                } else { //default format: json
                    return Response.ok().type(MediaType.APPLICATION_JSON + ";charset=utf-8").entity(this.monitor.apiMonitorRequestJson(queryString)).build();
                }
            }
        } catch (WebApplicationException e) {
            throw e;
        } catch (Throwable e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new InternalErrorException(e.getMessage());
        }
    }


}
