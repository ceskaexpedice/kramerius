package cz.incad.kramerius.rest.api.replication;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;

import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.SolrAccess;
import cz.incad.kramerius.rest.api.exceptions.ActionNotAllowed;
import cz.incad.kramerius.rest.api.replication.exceptions.ObjectNotFound;
import cz.incad.kramerius.security.IsActionAllowed;
import cz.incad.kramerius.security.User;
import cz.incad.kramerius.service.ReplicateException;
import cz.incad.kramerius.service.ReplicationService;
import cz.incad.kramerius.service.ResourceBundleService;
import cz.incad.kramerius.service.replication.FormatType;
import cz.incad.kramerius.utils.XMLUtils;


/**
 * CDK replication resource
 * @author pavels
 */
@Path("/cdk/{pid}")
public class CDKReplicationsResource {

    @Inject
    ReplicationService replicationService;

    @Inject
    ResourceBundleService resourceBundleService;
    
    @Inject
    Provider<Locale> localesProvider;
    
    @Inject
    @Named("securedFedoraAccess")
    FedoraAccess fedoraAccess;
    
    @Inject
    SolrAccess solrAccess;

    @Inject
    IsActionAllowed isActionAllowed;

    @Inject
    Provider<HttpServletRequest> requestProvider;

    @Inject
    Provider<User> userProvider;
    

    /**
     * Returns exported FOXML in xml format
     * @param pid PID of object 
     * @return FOXML as application xml
     * @throws ReplicateException An error has been occured
     * @throws UnsupportedEncodingException  UTF-8 is not supported
     */
    @GET
    @Path("foxml")
    @Produces(MediaType.APPLICATION_XML+";charset=utf-8")
    public Response getExportedFOXML(@PathParam("pid")String pid
    		) throws ReplicateException, UnsupportedEncodingException {
        try {
        	//TODO: permissions
            // musi se vejit do pameti
            byte[] bytes = replicationService.getExportedFOXML(pid, FormatType.CDK);
            return Response.ok().entity(XMLUtils.parseDocument(new ByteArrayInputStream(bytes), true)).build();
        } catch(FileNotFoundException e) {
            throw new ObjectNotFound("cannot find pid '"+pid+"'");
        } catch (IOException e) {
            throw new ReplicateException(e);
        } catch (ParserConfigurationException e) {
            throw new ReplicateException(e);
        } catch (SAXException e) {
            throw new ReplicateException(e);
        }
    }

}
