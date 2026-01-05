package cz.inovatika.kramerius.services.iterators.utils;

//import com.sun.jersey.api.client.*;

//import cz.incad.kramerius.rest.api.k5.client.utils.SOLRUtils;
import cz.inovatika.kramerius.services.config.ResponseHandlingConfig;
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
import java.util.Arrays;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;


public class HTTPSolrUtils {

    public static final Logger LOGGER = Logger.getLogger(HTTPSolrUtils.class.getName());

    private HTTPSolrUtils() {}

    // TODO: Replace by SolrUpdateUtils.sendToDest
//    public static String sendToDest(String destSolr, Client jerseyClient, Document batchDoc) {
//        try {
//            StringWriter writer = new StringWriter();
//            XMLUtils.print(batchDoc, writer);
//            return sendBatchToDestJersey(destSolr, jerseyClient, batchDoc, writer);
//        } catch (UniformInterfaceException | ClientHandlerException  | TransformerException | IOException e) {
//            LOGGER.log(Level.SEVERE, e.getMessage(), e);
//            throw new RuntimeException(e);
//        }
//    }

    // TODO: Replace by SolrUpdateUtils.sendToDest
    public static String sendToDest(String destSolr, CloseableHttpClient apacheClient, Document batchDoc) {
        try {
            StringWriter writer = new StringWriter();
            XMLUtils.print(batchDoc, writer);
            return sendBatchToDestApache(destSolr, apacheClient, batchDoc, writer);
        } catch ( TransformerException | IOException e) {
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

//    private static String sendBatchToDestJersey(String destSolr, Client client, Document batchDoc, StringWriter writer) throws TransformerException, IOException {
//        WebResource r = client.resource(destSolr);
//        ClientResponse resp = r.accept(MediaType.TEXT_XML).type(MediaType.TEXT_XML).entity(writer.toString(), MediaType.TEXT_XML).post(ClientResponse.class);
//        if (resp.getStatus() != ClientResponse.Status.OK.getStatusCode()) {
//
//            StringWriter stringWriter = new StringWriter();
//            XMLUtils.print(batchDoc,stringWriter);
//            LOGGER.warning("Problematic batch: ");
//            LOGGER.warning(stringWriter.toString());
//
//            ByteArrayOutputStream bos = new ByteArrayOutputStream();
//            InputStream entityInputStream = resp.getEntityInputStream();
//            IOUtils.copyStreams(entityInputStream, bos);
//            return new String(bos.toByteArray(), "UTF-8");
//        } else {
//            ByteArrayOutputStream bos = new ByteArrayOutputStream();
//            InputStream entityInputStream = resp.getEntityInputStream();
//            IOUtils.copyStreams(entityInputStream, bos);
//            return new String(bos.toByteArray(), "UTF-8");
//        }
//    }

//    public static void printToConsole(Document batchDoc)  {
//        try {
//            StringWriter writer = new StringWriter();
//            XMLUtils.print(batchDoc, writer);
//            System.out.println(writer.toString());
//        } catch (UniformInterfaceException | ClientHandlerException | TransformerException e) {
//            LOGGER.log(Level.SEVERE, e.getMessage(), e);
//            throw new RuntimeException(e);
//        }
//    }



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
        } catch (ParserConfigurationException | TransformerException e) {
            throw new RuntimeException(e);
        }
    }


//    public static void commitJersey(Client client, String destServer)  {
//        try {
//            String destSolr = destServer + "?commit=true";
//            WebResource r = client.resource(destSolr);
//            Document document = XMLUtils.crateDocument("add");
//            StringWriter strWriter = new StringWriter();
//            XMLUtils.print(document, strWriter);
//            String t = r.accept(MediaType.TEXT_XML).type(MediaType.TEXT_XML).entity(strWriter.toString(), MediaType.TEXT_XML).post(String.class);
//        } catch (UniformInterfaceException | ClientHandlerException | ParserConfigurationException | TransformerException e) {
//            throw new RuntimeException(e);
//        }
//    }

    


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
        LOGGER.info(String.format("Executing url,query: %s, %s " ,url, query));
        try {
            String t = executeSolrRequestApache(apacheClient, url, query);
            return getElement(t);
        } catch (ParserConfigurationException | SAXException | IOException e) {
            throw new RuntimeException(e);
        }
    }

//    public static Element executeQueryJersey(Client jerseyClient, String url, String query, ResponseHandlingConfig responseHandlingConfig) {
//        try {
//            String t = executeSolrRequestJersey(jerseyClient, url, query, responseHandlingConfig);
//            return getElement(t);
//        } catch (ParserConfigurationException | SAXException | IOException e) {
//            throw new RuntimeException(e);
//        }
//    }
//
//    public static Element executeQueryJersey(Client jerseyClient, String url, String query) {
//        try {
//            String t = executeSolrRequestJersey(jerseyClient, url, query);
//            return getElement(t);
//        } catch (ParserConfigurationException | SAXException | IOException e) {
//            throw new RuntimeException(e);
//        }
//    }

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

//    private static String executeSolrRequestJersey(Client client, String url, String query) {
//        return executeSolrRequestJersey(client, url, query, null);
//    }
//
//    private static String executeSolrRequestJersey(Client client, String url, String query, ResponseHandlingConfig config) {
//        int maxRetries = (config != null) ? config.getMaxRetries() : 0;
//        int delayMs = (config != null) ? config.getDelayMs() : 0;
//        int[] retryStatusCodes = (config != null) ? config.getRetryStatusCodes() : new int[0];
//        String u = url + (url.endsWith("/") ? "" : "/")+ query;
//        WebResource r = client.resource(u);
//
//        for (int attempt = 0; attempt <= maxRetries; attempt++) {
//            ClientResponse clientResponse = null;
//            try {
//                if (attempt > 0 && delayMs > 0) {
//                    LOGGER.info(String.format("[" + Thread.currentThread().getName() + "] sleeping %d before attempt %d", delayMs, attempt + 1));
//                    Thread.sleep(delayMs);
//                }
//
//                LOGGER.fine(String.format("[" + Thread.currentThread().getName() + "] Attempt %d/%d to URL: %s",
//                        attempt + 1, maxRetries + 1, u));
//                clientResponse = r.accept(MediaType.APPLICATION_XML).get(ClientResponse.class);
//                final int currentStatusCode = clientResponse.getStatus();
//
//                if (currentStatusCode == 200) {
//                    LOGGER.info(String.format("[" + Thread.currentThread().getName() + "] Request successful (Status: 200)."));
//                    return clientResponse.getEntity(String.class);
//                }
//
//                boolean shouldRetry = false;
//                if (config != null) {
//                    shouldRetry = Arrays.stream(retryStatusCodes)
//                            .anyMatch(code -> code == currentStatusCode);
//                }
//
//                if (shouldRetry && attempt < maxRetries) {
//                    LOGGER.warning(String.format("[" + Thread.currentThread().getName() + "] Status %d received. Preparing for retry...", currentStatusCode));
//                    continue;
//                }
//
//                throw new RuntimeException("Request failed after " + (attempt + 1) +
//                        " attempts with status code: " + currentStatusCode);
//
//            } catch (InterruptedException e) {
//                Thread.currentThread().interrupt();
//                throw new RuntimeException("Retry sleep interrupted", e);
//            } finally {
//                if (clientResponse != null) {
//                    clientResponse.close();
//                }
//            }
//        }
//        throw new RuntimeException("Internal error: Retry loop finished unexpectedly.");
//    }
}
