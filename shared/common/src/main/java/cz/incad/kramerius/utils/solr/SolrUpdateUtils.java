package cz.incad.kramerius.utils.solr;

import com.sun.jersey.api.client.*;
import cz.incad.kramerius.utils.IOUtils;
import cz.incad.kramerius.utils.XMLUtils;
import org.w3c.dom.Document;

import javax.ws.rs.core.MediaType;
import javax.xml.transform.TransformerException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SolrUpdateUtils {

    public static final Logger LOGGER = Logger.getLogger(SolrUpdateUtils.class.getName());

    private SolrUpdateUtils() {
    }

    public static void sendToDest(Client client, Document batchDoc, String updateUrl) {
        try {
            StringWriter writer = new StringWriter();
            XMLUtils.print(batchDoc, writer);
            String shost = updateUrl;//updateUrl();
            WebResource r = client.resource(shost);
            ClientResponse resp = r.accept(MediaType.TEXT_XML).type(MediaType.TEXT_XML).entity(writer.toString(), MediaType.TEXT_XML).post(ClientResponse.class);
            if (resp.getStatus() != ClientResponse.Status.OK.getStatusCode()) {
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                InputStream entityInputStream = resp.getEntityInputStream();
                IOUtils.copyStreams(entityInputStream, bos);
                LOGGER.log(Level.SEVERE, new String(bos.toByteArray()));
            }
        } catch (UniformInterfaceException | ClientHandlerException | IOException | TransformerException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        }
    }
}
