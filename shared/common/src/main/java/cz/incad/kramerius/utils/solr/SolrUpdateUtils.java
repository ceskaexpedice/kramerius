package cz.incad.kramerius.utils.solr;

import cz.incad.kramerius.utils.IOUtils;
import cz.incad.kramerius.utils.XMLUtils;
import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.w3c.dom.Document;

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

            WebTarget target = client.target(updateUrl);

            try (Response resp = target
                    .request(MediaType.TEXT_XML)
                    .post(Entity.entity(writer.toString(), MediaType.TEXT_XML))) {

                if (resp.getStatus() != Response.Status.OK.getStatusCode()) {
                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    InputStream entityInputStream = resp.readEntity(InputStream.class);
                    IOUtils.copyStreams(entityInputStream, bos);
                    LOGGER.log(Level.SEVERE, new String(bos.toByteArray()));
                }
            }

        } catch (ProcessingException | IOException | TransformerException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        }
    }
}
