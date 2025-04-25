package cz.incad.kramerius.services.utils;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import org.easymock.EasyMock;
import org.jetbrains.annotations.NotNull;

import javax.ws.rs.core.MediaType;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

public class SolrMockWebCall {
	
	public static final Logger LOGGER = Logger.getLogger(SolrMockWebCall.class.getName());
	
    private SolrMockWebCall() {}

    public static List<Object> webCallExpectXML(Client client, String firstReq, String firstResp) throws MalformedURLException, URISyntaxException {
        return webCallExpectMimeType(client, firstReq, firstResp, MediaType.APPLICATION_XML);
    }

    public static List<Object> webCallExpectJSON(Client client, String firstReq, String firstResp) throws MalformedURLException, URISyntaxException {
        return webCallExpectMimeType(client, firstReq, firstResp, MediaType.APPLICATION_JSON);
    }

    @NotNull
    private static List<Object> webCallExpectMimeType(Client client, String firstReq, String firstResp, String mimeType) throws URISyntaxException, MalformedURLException {
        WebResource firstResource = EasyMock.createMock(WebResource.class);
        WebResource.Builder firstResourceBuilder = EasyMock.createMock(WebResource.Builder.class);

        EasyMock.expect(client.resource(EasyMock.eq(firstReq))).andReturn(firstResource).anyTimes();
        LOGGER.info("Mocking request "+ firstReq);
        EasyMock.expect(firstResource.getURI()).andReturn(new URL(firstReq).toURI()).anyTimes();

        EasyMock.expect(firstResource.accept(mimeType)).andReturn(firstResourceBuilder).anyTimes();
        EasyMock.expect(firstResourceBuilder.get(String.class)).andReturn(firstResp).anyTimes();

        return Arrays.asList(firstResource, firstResourceBuilder);
    }

}
