package cz.incad.kramerius.rest.api.k5.client.debug;

import com.google.inject.Inject;
import com.google.inject.Provider;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import java.io.IOException;
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
