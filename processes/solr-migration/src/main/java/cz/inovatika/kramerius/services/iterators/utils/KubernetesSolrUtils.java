package cz.inovatika.kramerius.services.iterators.utils;

import com.sun.jersey.api.client.*;

//import cz.incad.kramerius.rest.api.k5.client.utils.SOLRUtils;
import cz.inovatika.kramerius.services.iterators.IterationItem;
import cz.incad.kramerius.utils.IOUtils;
import cz.incad.kramerius.utils.XMLUtils;
import cz.inovatika.kramerius.services.utils.ResultsUtils;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpHeaders;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.io.entity.StringEntity;
//import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.ws.rs.core.MediaType;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;


public class KubernetesSolrUtils {

    public static final Logger LOGGER = Logger.getLogger(KubernetesSolrUtils.class.getName());

    private KubernetesSolrUtils() {}

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

    


    public static List<IterationItem> prepareIterationItems(Element elm, String source, String identKey) {
        Element result = XMLUtils.findElement(elm, new XMLUtils.ElementsFilter() {
            @Override
            public boolean acceptElement(Element element) {
                String nodeName = element.getNodeName();
                return nodeName.equals("result");
            }
        });
        if (result != null) {
            List<Element> elements = XMLUtils.getElements(result, new XMLUtils.ElementsFilter() {
                @Override
                public boolean acceptElement(Element element) {
                    String nodeName = element.getNodeName();
                    return nodeName.equals("doc");
                }
            });

            return elements.stream().map(ResultsUtils::doc).map(doc-> {
                Object ident = doc.get(identKey);
                if (ident != null) {
                    return new IterationItem(ident.toString(), source, doc);
                } else return null;
            }).collect(Collectors.toList());


        } else return new ArrayList<>();
    }


    public static Element executeQueryApache(CloseableHttpClient apacheClient, String url, String query) {
        try {
            String t = executeSolrRequestApache(apacheClient, url, query);
            return getElement(t);
        } catch (ParserConfigurationException | SAXException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static Element executeQueryJersey(Client jerseyClient, String url, String query) {
        try {
            String t = executeSolrRequestJersey(jerseyClient, url, query);
            return getElement(t);
        } catch (ParserConfigurationException | SAXException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static Element getElement(String t) throws ParserConfigurationException, SAXException, IOException {
        Document parseDocument = XMLUtils.parseDocument(new StringReader(t));
        Stack<Element> stack = new Stack<>();
        stack.push(parseDocument.getDocumentElement());

        while(!stack.isEmpty()) {
            Element pop = stack.pop();
            if (pop.getNodeName().equals("str")) {
                String textContent = pop.getTextContent();
                if (textContent !=null && textContent.startsWith("uuid:") && textContent.contains("@") && !textContent.contains("/")) {
                    String[] split = textContent.split("@");
                    String formatted = String.format("%s/@%s", split[0], split[1]);
                    pop.setTextContent(formatted);
                }
            }
            XMLUtils.getElements(pop).stream().forEach(stack::push);
        }
        return parseDocument.getDocumentElement();
    }

    private static String executeSolrRequestApache(CloseableHttpClient client, String url, String query) {
        String u = url +(url.endsWith("/") ? "" : "/")+ query;
        HttpGet get = new HttpGet(u);
        try(CloseableHttpResponse response = client.execute(get)) {
            InputStream is = response.getEntity().getContent();
            String content = org.apache.commons.io.IOUtils.toString(is, "UTF-8");
            return content;
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    private static String executeSolrRequestJersey(Client client, String url, String query) {
        String u = url +(url.endsWith("/") ? "" : "/")+ query;
        LOGGER.fine(String.format("[" + Thread.currentThread().getName() + "] url %s", u));
        WebResource r = client.resource(u);


        LOGGER.fine(String.format("[" + Thread.currentThread().getName() + "] processing %s", r.getURI().toString()));
        String t = r.accept(MediaType.APPLICATION_XML).get(String.class);
        return t;
    }
}
