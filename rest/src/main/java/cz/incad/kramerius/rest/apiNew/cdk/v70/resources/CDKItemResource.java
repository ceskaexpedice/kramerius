package cz.incad.kramerius.rest.apiNew.cdk.v70.resources;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

import org.ceskaexpedice.akubra.AkubraRepository;
import org.ceskaexpedice.akubra.RepositoryException;
import org.json.JSONArray;
import org.json.JSONObject;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;

import cz.incad.kramerius.SolrAccess;
import cz.incad.kramerius.rest.api.exceptions.ActionNotAllowed;
import cz.incad.kramerius.rest.api.k5.client.item.exceptions.PIDNotFound;
import cz.incad.kramerius.rest.api.k5.client.utils.PIDSupport;
import cz.incad.kramerius.rest.apiNew.client.v70.utils.RightRuntimeInformations;
import cz.incad.kramerius.rest.apiNew.client.v70.utils.RightRuntimeInformations.RuntimeInformation;
import cz.incad.kramerius.security.RightsResolver;
import cz.incad.kramerius.security.SecurityException;
import cz.incad.kramerius.utils.FedoraUtils;
import cz.incad.kramerius.utils.IOUtils;

/**
 * Provides endpoints for item resource
 * @author happy
 */
public class CDKItemResource {

    public static final Logger LOGGER = Logger.getLogger(CDKItemResource.class.getName());

    @Inject
    private RightsResolver actionAllowed;

    @Inject
    @Named("new-index")
    private SolrAccess solrAccess;

    @Inject
    Provider<HttpServletRequest> requestProvider;

    /* TODO AK_NEW
    @Inject
    @Named("securedFedoraAccess")
    FedoraAccess fedoraAccess;

     */

    @Inject
    AkubraRepository akubraRepository;

    public Response providedBy(String pid) {
        try {
            RuntimeInformation extractInformations = RightRuntimeInformations.extractInformations(this.actionAllowed, this.solrAccess, pid);
            JSONArray providingLicenses = extractInformations.getProvidingLicensesAsJSONArray();
            
            JSONObject responseJson = new JSONObject();
            responseJson.put("licenses",
                    providingLicenses);
            return Response.ok(responseJson).type("application/json").build();

        } catch (IOException | RepositoryException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            return Response.status(500).build();
        }

    }

    public Response stream(String pid, String dsid) {
        try {
            checkPid(pid);
            if (!FedoraUtils.FEDORA_INTERNAL_STREAMS.contains(dsid)) {
                if (!PIDSupport.isComposedPID(pid)) {
                    // audio streas is not suported
                    if (!FedoraUtils.AUDIO_STREAMS.contains(dsid)) {
                        final InputStream is = akubraRepository.getDatastreamContent(pid, dsid).asInputStream();
                        String mimeTypeForStream = akubraRepository.getDatastreamMetadata(pid, dsid).getMimetype();

                        StreamingOutput stream = new StreamingOutput() {
                            public void write(OutputStream output) throws IOException, WebApplicationException {
                                try {
                                    IOUtils.copyStreams(is, output);
                                } catch (Exception e) {
                                    throw new WebApplicationException(e);
                                }
                            }
                        };
                        return Response.ok().entity(stream).type(mimeTypeForStream).build();
                    } else {
                        throw new PIDNotFound("cannot disseminate stream  " + dsid);
                    }
                } else
                    throw new PIDNotFound("cannot find stream " + dsid);
            } else {
                throw new PIDNotFound("cannot disseminate stream  " + dsid);
            }
        } catch (SecurityException e) {
            throw new ActionNotAllowed(e.getMessage());
        }
    }

    private void checkPid(String pid) throws PIDNotFound {
        try {
            if (PIDSupport.isComposedPID(pid)) {
                String p = PIDSupport.first(pid);
                if (!akubraRepository.exists(p)) {
                    throw new PIDNotFound("pid not found");
                }
            } else {
                if (!akubraRepository.exists(pid)) {
                    throw new PIDNotFound("pid not found");
                }
            }
        } catch (Exception e) {
            throw new PIDNotFound("error while parsing pid (" + pid + ")");
        }
    }

}
