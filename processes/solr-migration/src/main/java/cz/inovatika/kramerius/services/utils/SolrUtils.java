package cz.inovatika.kramerius.services.utils;

import com.sun.jersey.api.client.*;
import cz.incad.kramerius.utils.IOUtils;
import cz.incad.kramerius.utils.XMLUtils;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpHeaders;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.w3c.dom.Document;

import javax.ws.rs.core.MediaType;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;


public class SolrUtils {

    public static final Logger LOGGER = Logger.getLogger(SolrUtils.class.getName());

    private SolrUtils() {}

    // TODO: Replace by SolrUpdateUtils.sendToDest
    public static String sendToDest(String destSolr, Client jerseyClient, Document batchDoc) {
        try {
            StringWriter writer = new StringWriter();
            XMLUtils.print(batchDoc, writer);
            return sendBatchToDestJersey(destSolr, jerseyClient, batchDoc, writer);
        } catch (UniformInterfaceException | ClientHandlerException  | TransformerException | IOException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    // TODO: Replace by SolrUpdateUtils.sendToDest
    public static String sendToDest(String destSolr, CloseableHttpClient apacheClient, Document batchDoc) {
        try {
            StringWriter writer = new StringWriter();
            XMLUtils.print(batchDoc, writer);
            return sendBatchToDestApache(destSolr, apacheClient, batchDoc, writer);
        } catch (UniformInterfaceException | ClientHandlerException  | TransformerException | IOException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }



    private static String sendBatchToDestApache(String destSolr, CloseableHttpClient apacheClient, Document batchDoc, StringWriter writer) throws TransformerException, IOException {
        HttpPost httpPost = new HttpPost(destSolr);

        httpPost.setHeader(HttpHeaders.ACCEPT, MediaType.TEXT_XML);
        httpPost.setHeader(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_XML);
        httpPost.setEntity(new StringEntity(writer.toString(), StandardCharsets.UTF_8));
        try (CloseableHttpResponse response = apacheClient.execute(httpPost)) {
            int statusCode = response.getCode();
            HttpEntity entity = response.getEntity();

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            if (entity != null) {
                try (InputStream entityInputStream = entity.getContent()) {
                    IOUtils.copyStreams(entityInputStream, bos);
                }
            }

            if (statusCode != HttpStatus.SC_OK) {
                StringWriter stringWriter = new StringWriter();
                XMLUtils.print(batchDoc, stringWriter);
                LOGGER.warning("Problematic batch: ");
                LOGGER.warning(stringWriter.toString());
                return bos.toString();
            } else {
                return bos.toString();
            }

        } catch (IOException e) {
            throw new RuntimeException("Error during HTTP POST to Solr", e);
        }
    }

    private static String sendBatchToDestJersey(String destSolr, Client client, Document batchDoc, StringWriter writer) throws TransformerException, IOException {
        WebResource r = client.resource(destSolr);
        ClientResponse resp = r.accept(MediaType.TEXT_XML).type(MediaType.TEXT_XML).entity(writer.toString(), MediaType.TEXT_XML).post(ClientResponse.class);
        if (resp.getStatus() != ClientResponse.Status.OK.getStatusCode()) {

            StringWriter stringWriter = new StringWriter();
            XMLUtils.print(batchDoc,stringWriter);
            LOGGER.warning("Problematic batch: ");
            LOGGER.warning(stringWriter.toString());

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            InputStream entityInputStream = resp.getEntityInputStream();
            IOUtils.copyStreams(entityInputStream, bos);
            return new String(bos.toByteArray(), "UTF-8");
        } else {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            InputStream entityInputStream = resp.getEntityInputStream();
            IOUtils.copyStreams(entityInputStream, bos);
            return new String(bos.toByteArray(), "UTF-8");
        }
    }

    public static void printToConsole(Document batchDoc)  {
        try {
            StringWriter writer = new StringWriter();
            XMLUtils.print(batchDoc, writer);
            System.out.println(writer.toString());
        } catch (UniformInterfaceException | ClientHandlerException | TransformerException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }



    public static void commitApache(CloseableHttpClient closeableHttpClient, String destServer)  {
        try {
            String destSolr = destServer + "?commit=true";
            HttpPost post = new HttpPost(destSolr);
            Document document = XMLUtils.crateDocument("add");

            StringWriter strWriter = new StringWriter();
            XMLUtils.print(document, strWriter);
            try(CloseableHttpResponse response = closeableHttpClient.execute(post)) {
                InputStream is = response.getEntity().getContent();
                String content = org.apache.commons.io.IOUtils.toString(is, "UTF-8");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } catch (UniformInterfaceException | ClientHandlerException | ParserConfigurationException | TransformerException e) {
            throw new RuntimeException(e);
        }
    }


    public static void commitJersey(Client client, String destServer)  {
        try {
            String destSolr = destServer + "?commit=true";
            WebResource r = client.resource(destSolr);
            Document document = XMLUtils.crateDocument("add");
            StringWriter strWriter = new StringWriter();
            XMLUtils.print(document, strWriter);
            String t = r.accept(MediaType.TEXT_XML).type(MediaType.TEXT_XML).entity(strWriter.toString(), MediaType.TEXT_XML).post(String.class);
        } catch (UniformInterfaceException | ClientHandlerException | ParserConfigurationException | TransformerException e) {
            throw new RuntimeException(e);
        }
    }

}
