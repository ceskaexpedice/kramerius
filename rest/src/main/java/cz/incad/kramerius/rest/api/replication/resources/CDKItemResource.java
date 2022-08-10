package cz.incad.kramerius.rest.api.replication.resources;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;

import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.SolrAccess;
import cz.incad.kramerius.rest.api.exceptions.ActionNotAllowed;
import cz.incad.kramerius.rest.api.k5.client.item.exceptions.PIDNotFound;
import cz.incad.kramerius.rest.api.k5.client.utils.PIDSupport;
import cz.incad.kramerius.security.IsActionAllowed;
import cz.incad.kramerius.security.SecurityException;
import cz.incad.kramerius.utils.FedoraUtils;
import cz.incad.kramerius.utils.IOUtils;

public class CDKItemResource {

    @Inject
    IsActionAllowed isActionAllowed;

    @Inject
    SolrAccess solrAccess;

    @Inject
    Provider<HttpServletRequest> requestProvider;

    @Inject
    @Named("securedFedoraAccess")
    FedoraAccess fedoraAccess;

    @GET
    @Path("item/{pid}/streams/{dsid}")
    public Response stream(@PathParam("pid") String pid,@PathParam("dsid") String dsid) {
        try {
        	checkPid(pid);
    		if (!FedoraUtils.FEDORA_INTERNAL_STREAMS.contains(dsid)) {
                if (!PIDSupport.isComposedPID(pid)) {
                    // audio streas is not suported 	
                	if (!FedoraUtils.AUDIO_STREAMS.contains(dsid)) {
                        final InputStream is = this.fedoraAccess.getDataStream(pid,dsid);
                        String mimeTypeForStream = this.fedoraAccess.getMimeTypeForStream(pid, dsid);

                        StreamingOutput stream = new StreamingOutput() {
                            public void write(OutputStream output)
                                    throws IOException, WebApplicationException {
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
        } catch (IOException e) {
            throw new PIDNotFound(e.getMessage());
        } catch (SecurityException e) {
            throw new ActionNotAllowed(e.getMessage());
        }
    }

	
    private void checkPid(String pid) throws PIDNotFound {
        try {
            if (PIDSupport.isComposedPID(pid)) {
                String p = PIDSupport.first(pid);
                if (!this.fedoraAccess.isObjectAvailable(p)) {
                    throw new PIDNotFound("pid not found");
                }
            } else {
                if (!this.fedoraAccess.isObjectAvailable(pid)) {
                    throw new PIDNotFound("pid not found");
                }
            }
        } catch (IOException e) {
            throw new PIDNotFound("pid not found");
        } catch(Exception e) {
            throw new PIDNotFound("error while parsing pid ("+pid+")");
        }
    }

}
