package cz.incad.kramerius.rest.api.k5.client.debug;

import com.google.inject.Inject;
import com.google.inject.Provider;
import cz.incad.kramerius.ObjectPidsPath;
import cz.incad.kramerius.rest.api.exceptions.ActionNotAllowedXML;
import cz.incad.kramerius.rest.api.k5.client.item.exceptions.PIDNotFound;
import cz.incad.kramerius.security.SecuredActions;
import cz.incad.kramerius.service.ReplicateException;
import cz.incad.kramerius.service.replication.FormatType;
import cz.incad.kramerius.utils.IOUtils;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.util.Enumeration;
import java.util.Properties;

@Path("/v5.0/debug")
public class HTTPHeaders {

    @Inject
    Provider<HttpServletRequest> requestProvider;

    @GET
    @Path("headers")
    @Produces({ MediaType.TEXT_PLAIN + ";charset=utf-8" })
    public String headers() throws IOException {
        Properties properties = new Properties();
        HttpServletRequest httpServletRequest = requestProvider.get();
        Enumeration headerNames = httpServletRequest.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            Object o = headerNames.nextElement();
            String val = httpServletRequest.getHeader(o.toString());
            properties.put(o, val);
        }
        StringWriter writer = new StringWriter();
        properties.store(writer,"HTTP Request headers");
        return writer.toString();
    }

}
